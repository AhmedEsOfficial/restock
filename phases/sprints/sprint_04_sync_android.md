# Sprint 4: Sync Implementation - Android Integration
## Phase 2, Month 5, Weeks 3-4

**Sprint Duration:** 2 weeks
**Sprint Number:** 4 of 12 (Phase 2)
**Team:** Tech Lead, UI/UX Engineer, Backend Developers (2), DevOps Engineer

---

## 1. Sprint Overview

This sprint integrates the cloud sync service into the Android application. Users will be able to sync their grocery data across devices, with background sync keeping data fresh automatically. The account management UI will be completed, giving users control over their sync settings.

### Sprint Goal
> *"Deliver seamless cloud sync on Android with background sync, multi-device support, and intuitive account management."*

### Key Outcomes
- Android SyncManager integrated with backend
- Background sync with WorkManager operational
- Multi-device sync tested and working
- Account management UI complete
- Sync status indicators in app
- Cloud sync milestone achieved

### Prerequisites from Sprint 3
- Sync API endpoints functional
- E2E encryption implemented
- Conflict resolution working
- Device management API ready

---

## 2. Sprint Backlog

### 2.1 User Stories

#### US-4.1: Android Sync Repository
**As a** developer
**I want** a clean sync abstraction layer
**So that** the app can sync seamlessly with the backend

**Acceptance Criteria:**
- [ ] SyncRepository interface defined in domain layer
- [ ] SyncRepositoryImpl in data layer with Ktor client
- [ ] Handles encryption/decryption via EncryptionManager
- [ ] Manages sync tokens in DataStore
- [ ] Exposes sync state as StateFlow
- [ ] Error handling with retry logic

**Story Points:** 8
**Assignee:** Tech Lead

**Architecture:**
```kotlin
// Domain layer
interface SyncRepository {
    val syncState: StateFlow<SyncState>
    suspend fun pushChanges(): Result<SyncResult>
    suspend fun pullChanges(): Result<SyncResult>
    suspend fun fullSync(): Result<SyncResult>
    fun observePendingChanges(): Flow<Int>
}

// Data layer
class SyncRepositoryImpl @Inject constructor(
    private val syncApi: SyncApi,
    private val encryptionManager: EncryptionManager,
    private val changeTracker: ChangeTracker,
    private val syncTokenStore: SyncTokenStore,
    private val itemDao: ItemDao,
) : SyncRepository {

    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    override val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    override suspend fun pushChanges(): Result<SyncResult> {
        _syncState.value = SyncState.Syncing

        val pendingChanges = changeTracker.getPendingChanges()
        val encryptedChanges = pendingChanges.map { change ->
            encryptionManager.encrypt(change.toJson())
        }

        return syncApi.push(encryptedChanges)
            .onSuccess { result ->
                changeTracker.markSynced(pendingChanges.map { it.id })
                syncTokenStore.saveToken(result.syncToken)
                _syncState.value = SyncState.Idle
            }
            .onFailure { _syncState.value = SyncState.Error(it) }
    }
}
```

---

#### US-4.2: Change Tracking
**As a** user
**I want** my offline changes to sync when I'm back online
**So that** I never lose any edits

**Acceptance Criteria:**
- [ ] Local changes recorded in pending_changes table
- [ ] Changes tracked: items, current_list, purchases, meal_tags
- [ ] Operations tracked: INSERT, UPDATE, DELETE
- [ ] Timestamps recorded for conflict resolution
- [ ] Changes batched for efficient sync
- [ ] Synced changes cleared from pending table

**Story Points:** 8
**Assignee:** Tech Lead

**Database Schema Addition:**
```sql
-- Add to local Room database
pending_changes (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    entity_type TEXT NOT NULL,
    entity_id TEXT NOT NULL,
    operation TEXT NOT NULL,
    data_json TEXT NOT NULL,
    created_at INTEGER NOT NULL,
    synced_at INTEGER,
    UNIQUE(entity_type, entity_id, operation, created_at)
)

CREATE INDEX idx_pending_unsent ON pending_changes(synced_at) WHERE synced_at IS NULL;
```

**Change Tracker:**
```kotlin
class ChangeTracker @Inject constructor(
    private val pendingChangesDao: PendingChangesDao
) {
    suspend fun trackInsert(entityType: String, entityId: String, data: Any) {
        pendingChangesDao.insert(
            PendingChangeEntity(
                entityType = entityType,
                entityId = entityId,
                operation = "INSERT",
                dataJson = Json.encodeToString(data),
                createdAt = Clock.System.now().toEpochMilliseconds()
            )
        )
    }

    suspend fun trackUpdate(entityType: String, entityId: String, data: Any) { ... }
    suspend fun trackDelete(entityType: String, entityId: String) { ... }

    fun getPendingChanges(): List<PendingChange> =
        pendingChangesDao.getUnsynced()
}
```

---

#### US-4.3: Background Sync
**As a** user
**I want** my data to sync automatically in the background
**So that** it's always up to date without manual action

**Acceptance Criteria:**
- [ ] WorkManager schedules periodic sync (every 15 minutes)
- [ ] Sync triggered on network availability change
- [ ] Sync triggered when app comes to foreground
- [ ] Battery-friendly: respects doze mode
- [ ] Sync only when on WiFi option (user setting)
- [ ] Notification shown for sync errors (optional setting)

**Story Points:** 8
**Assignee:** Backend Developer 1

**WorkManager Setup:**
```kotlin
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val syncRepository: SyncRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Push local changes
            syncRepository.pushChanges()
                .onFailure { return Result.retry() }

            // Pull remote changes
            syncRepository.pullChanges()
                .onFailure { return Result.retry() }

            Result.success()
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }
}

// Schedule periodic sync
class SyncScheduler @Inject constructor(
    private val workManager: WorkManager
) {
    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWork = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 15,
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
            flexTimeInterval = 5,
            flexTimeIntervalUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            "restock_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWork
        )
    }
}
```

---

#### US-4.4: Sync on Data Change
**As a** user
**I want** my changes to sync immediately when possible
**So that** other devices get updates quickly

**Acceptance Criteria:**
- [ ] Sync triggered within 5 seconds of local change
- [ ] Debounced to avoid excessive requests
- [ ] Only if network available
- [ ] Doesn't block UI thread
- [ ] Fails gracefully if offline

**Story Points:** 5
**Assignee:** Backend Developer 1

**Debounced Sync Trigger:**
```kotlin
class SyncTrigger @Inject constructor(
    private val syncRepository: SyncRepository,
    private val connectivityObserver: ConnectivityObserver,
) {
    private val syncScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var syncJob: Job? = null

    fun onLocalChange() {
        syncJob?.cancel()
        syncJob = syncScope.launch {
            delay(5.seconds)  // Debounce
            if (connectivityObserver.isConnected()) {
                syncRepository.pushChanges()
            }
        }
    }
}
```

---

#### US-4.5: Sync Status UI
**As a** user
**I want** to see my sync status
**So that** I know if my data is up to date

**Acceptance Criteria:**
- [ ] Sync indicator in app bar shows current state
- [ ] States: synced, syncing, pending, offline, error
- [ ] Tap on indicator shows details
- [ ] Last sync time displayed
- [ ] Pending changes count shown
- [ ] Manual sync button available

**Story Points:** 5
**Assignee:** UI/UX Engineer

**UI States:**
```kotlin
sealed interface SyncUiState {
    data object Synced : SyncUiState           // ✓ green checkmark
    data object Syncing : SyncUiState          // 🔄 spinning indicator
    data class Pending(val count: Int) : SyncUiState  // ⬆ upload with count
    data object Offline : SyncUiState          // ☁️ crossed out cloud
    data class Error(val message: String) : SyncUiState  // ⚠️ warning icon
}

@Composable
fun SyncStatusIndicator(
    state: SyncUiState,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(onClick = onTap, modifier = modifier) {
        when (state) {
            SyncUiState.Synced -> Icon(Icons.Default.CloudDone, "Synced")
            SyncUiState.Syncing -> CircularProgressIndicator(Modifier.size(24.dp))
            is SyncUiState.Pending -> Badge { Text("${state.count}") }
            SyncUiState.Offline -> Icon(Icons.Default.CloudOff, "Offline")
            is SyncUiState.Error -> Icon(Icons.Default.Warning, "Error")
        }
    }
}
```

---

#### US-4.6: Account Settings UI
**As a** logged-in user
**I want** to manage my account and sync settings
**So that** I have control over my data

**Acceptance Criteria:**
- [ ] Account section in settings
- [ ] Display email and name
- [ ] Edit display name
- [ ] Change password option
- [ ] Linked devices list
- [ ] Remove device option
- [ ] Sign out button
- [ ] Sign out all devices option
- [ ] Delete account option (with confirmation)

**Story Points:** 8
**Assignee:** UI/UX Engineer

---

#### US-4.7: Linked Devices Screen
**As a** user with multiple devices
**I want** to see all my linked devices
**So that** I can manage where my data syncs

**Acceptance Criteria:**
- [ ] List all devices with name, platform, last sync time
- [ ] Current device highlighted
- [ ] Remove device with confirmation
- [ ] Rename device option
- [ ] Free tier shows upgrade prompt if at limit
- [ ] Refresh device list

**Story Points:** 5
**Assignee:** UI/UX Engineer

---

#### US-4.8: Offline Mode Enhancement
**As a** user without internet connection
**I want** the app to work normally offline
**So that** I can manage my grocery list anywhere

**Acceptance Criteria:**
- [ ] All core features work offline
- [ ] Clear indication when offline
- [ ] Changes queued for sync
- [ ] No error toasts for expected offline state
- [ ] Sync automatically when back online

**Story Points:** 5
**Assignee:** Tech Lead

---

#### US-4.9: Multi-Device Testing
**As a** QA engineer
**I want** to verify multi-device sync works correctly
**So that** users have a reliable experience

**Acceptance Criteria:**
- [ ] Add item on Device A → appears on Device B
- [ ] Check item on Device A → reflects on Device B
- [ ] Delete item on Device A → removes from Device B
- [ ] Edit item on both devices → conflict resolved correctly
- [ ] Works with 2+ devices simultaneously
- [ ] Free tier correctly limited to 1 device sync

**Story Points:** 8
**Assignee:** Backend Developer 2, Tech Lead

---

### 2.2 Technical Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies |
|---------|------|----------|------------|--------------|
| T-4.1 | Create SyncRepository interface | Tech Lead | 2 | - |
| T-4.2 | Implement SyncRepositoryImpl | Tech Lead | 10 | T-4.1 |
| T-4.3 | Create pending_changes table migration | Tech Lead | 2 | - |
| T-4.4 | Implement ChangeTracker | Tech Lead | 6 | T-4.3 |
| T-4.5 | Integrate ChangeTracker into existing DAOs | Tech Lead | 6 | T-4.4 |
| T-4.6 | Create SyncWorker with WorkManager | Backend Dev 1 | 8 | T-4.2 |
| T-4.7 | Implement SyncScheduler | Backend Dev 1 | 4 | T-4.6 |
| T-4.8 | Create SyncTrigger for immediate sync | Backend Dev 1 | 4 | T-4.2 |
| T-4.9 | Implement ConnectivityObserver | Backend Dev 1 | 4 | - |
| T-4.10 | Build SyncStatusIndicator composable | UI/UX | 4 | - |
| T-4.11 | Build SyncDetailsSheet composable | UI/UX | 4 | T-4.10 |
| T-4.12 | Build AccountSettingsScreen | UI/UX | 10 | - |
| T-4.13 | Build LinkedDevicesScreen | UI/UX | 6 | - |
| T-4.14 | Build DeviceRemovalDialog | UI/UX | 2 | - |
| T-4.15 | Create SyncViewModel | Backend Dev 1 | 6 | T-4.2 |
| T-4.16 | Create AccountSettingsViewModel | Backend Dev 1 | 6 | - |
| T-4.17 | Create DevicesViewModel | Backend Dev 2 | 4 | - |
| T-4.18 | Integrate sync indicator into main screen | Tech Lead | 2 | T-4.10 |
| T-4.19 | Add sync settings to Settings screen | UI/UX | 4 | - |
| T-4.20 | Backend: optimize sync for large datasets | Backend Dev 2 | 6 | - |
| T-4.21 | Write SyncRepository unit tests | Tech Lead | 6 | T-4.2 |
| T-4.22 | Write SyncWorker instrumented tests | Backend Dev 1 | 4 | T-4.6 |
| T-4.23 | Multi-device E2E test suite | Backend Dev 2 | 10 | All |
| T-4.24 | Performance testing sync with 1000 items | DevOps | 6 | All |

---

## 3. Team Assignments

### Tech Lead
**Focus:** Sync integration, change tracking
- Implement SyncRepository
- Implement ChangeTracker
- Integrate change tracking into existing DAOs
- Integrate sync indicator into main screen
- Write sync unit tests
- Code review all changes

### UI/UX Engineer
**Focus:** Sync and account UI
- Build SyncStatusIndicator
- Build SyncDetailsSheet
- Build AccountSettingsScreen
- Build LinkedDevicesScreen
- Add sync settings to Settings screen

### Backend Developer 1
**Focus:** Background sync and ViewModels
- Implement SyncWorker with WorkManager
- Implement SyncScheduler
- Create SyncTrigger
- Implement ConnectivityObserver
- Create SyncViewModel and AccountSettingsViewModel

### Backend Developer 2
**Focus:** Testing and optimization
- Create DevicesViewModel
- Optimize sync for large datasets
- Build multi-device E2E test suite
- Performance testing
- Bug fixes and edge cases

### DevOps Engineer
**Focus:** Infrastructure and monitoring
- Set up sync monitoring dashboard
- Performance testing infrastructure
- Database optimization recommendations
- Alerting for sync failures

---

## 4. Dependencies

### External Dependencies
| Dependency | Owner | Status | Risk |
|------------|-------|--------|------|
| WorkManager library | Google | Stable | Low |
| Network connectivity APIs | Android | Stable | Low |

### Internal Dependencies
| Dependency | From | To | Notes |
|------------|------|-----|-------|
| Sync API endpoints | Sprint 3 | All Android sync | Core dependency |
| EncryptionManager | Sprint 3 | SyncRepository | Encryption |
| Auth tokens | Sprint 2 | Sync requests | Authentication |

---

## 5. Definition of Done

### Sprint Level
- [ ] User can sync data across 2+ devices
- [ ] Background sync runs every 15 minutes
- [ ] Changes sync within 5 seconds when online
- [ ] Sync status visible in app
- [ ] Account settings screen complete
- [ ] Linked devices screen functional
- [ ] Free tier limited to 1 device
- [ ] Offline mode works with pending changes
- [ ] Sync latency < 2 seconds for 100 items
- [ ] All tests passing with 80%+ coverage

### Story Level
- [ ] Acceptance criteria met
- [ ] Unit tests written
- [ ] UI tested on multiple screen sizes
- [ ] Code reviewed
- [ ] No memory leaks

---

## 6. Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| WorkManager reliability | Low | Medium | Thorough testing; fallback manual sync |
| Battery drain from sync | Medium | High | Optimize sync frequency; respect doze |
| Merge conflicts in data | Medium | Medium | Robust conflict resolution; user notification |
| Large dataset sync slow | Medium | Medium | Pagination; delta sync optimization |
| Offline changes lost | Low | Critical | Persistent local storage; verification |

---

## 7. Testing Requirements

### Unit Tests
- SyncRepository methods
- ChangeTracker operations
- Encryption integration
- Conflict handling

### Integration Tests
- Full sync cycle
- Background sync execution
- Network state changes
- Multi-device scenarios

### UI Tests
- Sync status indicator states
- Account settings navigation
- Device management actions
- Offline mode behavior

### E2E Tests
- Add item → sync → verify on second device
- Multi-device conflict resolution
- Free tier device limit
- Offline → online sync recovery

### Performance Tests
- Sync 100 items < 2 seconds
- Sync 1000 items < 10 seconds
- Background sync battery impact

### Test Coverage Targets
| Component | Target |
|-----------|--------|
| SyncRepository | 85% |
| ChangeTracker | 90% |
| ViewModels | 80% |
| UI components | Key flows |

---

## 8. Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Sync success rate | >99% | Analytics |
| Sync latency (100 items) | <2 seconds | Performance tests |
| Background sync reliability | >95% | WorkManager metrics |
| Data consistency | 100% | E2E tests |
| User sync adoption | >50% of logged-in users | Analytics |

---

## 9. Sprint Ceremonies

| Ceremony | Day | Duration | Attendees |
|----------|-----|----------|-----------|
| Sprint Planning | Day 1 | 2 hours | All |
| Daily Standup | Daily | 15 min | All |
| Tech Sync | Day 5, 10 | 1 hour | Tech Lead, Backend, DevOps |
| Design Review | Day 7 | 30 min | Tech Lead, UI/UX |
| Multi-Device Testing | Day 8 | 2 hours | All |
| Sprint Review | Day 10 | 1 hour | All + stakeholders |
| Retrospective | Day 10 | 45 min | All |

---

## 10. Deliverables Checklist

- [ ] SyncRepository implementation
- [ ] ChangeTracker implementation
- [ ] SyncWorker with WorkManager
- [ ] SyncTrigger for immediate sync
- [ ] SyncStatusIndicator component
- [ ] SyncDetailsSheet component
- [ ] AccountSettingsScreen
- [ ] LinkedDevicesScreen
- [ ] Sync settings integration
- [ ] Multi-device E2E test suite
- [ ] Performance test results
- [ ] Sync monitoring dashboard
- [ ] User documentation for sync feature

---

## 11. Milestone Achievement

### Cloud Sync Milestone Complete
At the end of this sprint, the **M1: Cloud Sync** milestone from Phase 2 should be achieved:

- [ ] Multi-device sync operational
- [ ] E2E encryption protecting user data
- [ ] Background sync keeping devices in sync
- [ ] Account management UI complete
- [ ] Free tier: 1 device, Premium: unlimited
- [ ] 99%+ sync reliability

---

## 12. Handoff to Sprint 5

### Completed Items for Next Sprint
- Cloud sync fully operational
- Users can manage accounts and devices
- Background sync working
- Sync infrastructure ready for premium features

### Prepared Items for Next Sprint
- Ready to implement Google Play Billing
- Subscription tier already enforced (device limits)
- Ready to build premium feature gating

---

*Sprint 4 Document v1.0 | Phase 2: Growth & Intelligence | Restock Android*
