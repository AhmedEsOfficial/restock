# Sprint 3: Sync Implementation - Backend Sync Service
## Phase 2, Month 5, Weeks 1-2

**Sprint Duration:** 2 weeks
**Sprint Number:** 3 of 12 (Phase 2)
**Team:** Tech Lead, UI/UX Engineer, Backend Developers (2), DevOps Engineer

---

## 1. Sprint Overview

This sprint focuses on building the core cloud synchronization service. The backend team will implement the sync API endpoints, end-to-end encryption, delta sync algorithm, and conflict resolution logic. This sprint establishes the foundation for multi-device sync.

### Sprint Goal
> *"Build a robust, secure cloud sync service with E2E encryption and intelligent conflict resolution."*

### Key Outcomes
- Sync API endpoints functional
- End-to-end encryption implemented
- Delta sync algorithm operational
- Conflict resolution logic tested
- Sync service deployed to staging

### Prerequisites from Sprint 2
- User authentication system operational
- Users can register and obtain JWT tokens
- Backend deployment pipeline functional
- Production database configured

---

## 2. Sprint Backlog

### 2.1 User Stories

#### US-3.1: Sync Data Model
**As a** backend developer
**I want** a well-designed sync data model
**So that** we can efficiently track and sync user data across devices

**Acceptance Criteria:**
- [ ] Sync metadata table tracks sync state per user/device
- [ ] Change log table records all data modifications
- [ ] Encrypted data storage table for user grocery data
- [ ] Indexes optimized for sync queries
- [ ] Migrations created and tested
- [ ] Data model documentation complete

**Story Points:** 5
**Assignee:** Backend Developer 1

**Database Schema:**
```sql
-- Sync metadata per device
sync_state (
    user_id TEXT NOT NULL,
    device_id TEXT NOT NULL,
    last_sync_at TIMESTAMP,
    sync_token TEXT,
    PRIMARY KEY (user_id, device_id),
    FOREIGN KEY (user_id) REFERENCES users(id)
)

-- Change log for delta sync
change_log (
    id SERIAL PRIMARY KEY,
    user_id TEXT NOT NULL,
    entity_type TEXT NOT NULL,  -- 'item', 'current_list', 'purchase', 'meal_tag'
    entity_id TEXT NOT NULL,
    operation TEXT NOT NULL,    -- 'INSERT', 'UPDATE', 'DELETE'
    encrypted_data TEXT,        -- E2E encrypted payload
    client_timestamp TIMESTAMP NOT NULL,
    server_timestamp TIMESTAMP DEFAULT NOW(),
    device_id TEXT NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
)

-- Encrypted user data snapshots
encrypted_data (
    id SERIAL PRIMARY KEY,
    user_id TEXT NOT NULL UNIQUE,
    encrypted_payload TEXT NOT NULL,  -- Full encrypted snapshot
    encryption_version INTEGER DEFAULT 1,
    updated_at TIMESTAMP DEFAULT NOW(),
    FOREIGN KEY (user_id) REFERENCES users(id)
)

-- Indices
CREATE INDEX idx_change_log_user_timestamp ON change_log(user_id, server_timestamp);
CREATE INDEX idx_change_log_sync ON change_log(user_id, server_timestamp) WHERE server_timestamp > NOW() - INTERVAL '90 days';
```

---

#### US-3.2: End-to-End Encryption
**As a** privacy-conscious user
**I want** my grocery data encrypted before it leaves my device
**So that** even the server cannot read my shopping habits

**Acceptance Criteria:**
- [ ] Client encrypts all data before sending to server
- [ ] Server stores only encrypted payloads
- [ ] Client decrypts data after receiving from server
- [ ] Key derivation uses Argon2 from user password
- [ ] AES-256-GCM encryption with random IV per message
- [ ] Server has zero knowledge of plaintext data
- [ ] Encryption/decryption performance < 100ms for typical payload

**Story Points:** 13
**Assignee:** Tech Lead, Backend Developer 1

**Encryption Flow:**
```
Client Side (Encrypt):
┌─────────────────────────────────────────────────────────────┐
│ User Password + Salt → Argon2 → Derived Key (256 bits)      │
│                                                              │
│ Plaintext Data + Derived Key + Random IV                    │
│        ↓                                                     │
│ AES-256-GCM Encrypt                                         │
│        ↓                                                     │
│ { iv: base64, ciphertext: base64, tag: base64 }             │
└─────────────────────────────────────────────────────────────┘

Server Side:
┌─────────────────────────────────────────────────────────────┐
│ Receives: { iv, ciphertext, tag } (opaque blob)             │
│ Stores: As-is (cannot decrypt without user's key)           │
│ Returns: Same encrypted blob to other devices               │
└─────────────────────────────────────────────────────────────┘

Client Side (Decrypt):
┌─────────────────────────────────────────────────────────────┐
│ { iv, ciphertext, tag } + Derived Key                       │
│        ↓                                                     │
│ AES-256-GCM Decrypt (verifies tag)                          │
│        ↓                                                     │
│ Plaintext Data                                              │
└─────────────────────────────────────────────────────────────┘
```

**Key Derivation Parameters:**
```kotlin
object EncryptionConfig {
    const val ARGON2_ITERATIONS = 3
    const val ARGON2_MEMORY_KB = 65536  // 64 MB
    const val ARGON2_PARALLELISM = 4
    const val KEY_LENGTH_BYTES = 32     // 256 bits
    const val IV_LENGTH_BYTES = 12      // 96 bits for GCM
}
```

---

#### US-3.3: Push Changes (Client to Server)
**As a** user
**I want** my local changes to sync to the cloud
**So that** I can access them from other devices

**Acceptance Criteria:**
- [ ] POST /api/v1/sync endpoint receives encrypted changes
- [ ] Changes include entity type, operation, and encrypted data
- [ ] Server validates change structure (not content - it's encrypted)
- [ ] Server records changes with server timestamp
- [ ] Server returns sync token for next sync
- [ ] Handles batch of up to 1000 changes per request
- [ ] Returns 413 if payload exceeds size limit (5MB)

**Story Points:** 8
**Assignee:** Backend Developer 1

**API Specification:**
```
POST /api/v1/sync
Authorization: Bearer <access_token>
Content-Type: application/json

Request:
{
    "client_id": "device-uuid",
    "last_sync_token": "previous-sync-token-or-null",
    "changes": [
        {
            "entity_type": "item",
            "entity_id": "item-uuid-123",
            "operation": "INSERT",
            "encrypted_data": "base64-encrypted-payload",
            "client_timestamp": "2026-05-01T10:30:00Z"
        },
        {
            "entity_type": "current_list",
            "entity_id": "list-item-uuid-456",
            "operation": "UPDATE",
            "encrypted_data": "base64-encrypted-payload",
            "client_timestamp": "2026-05-01T10:31:00Z"
        }
    ]
}

Response (200 OK):
{
    "status": "success",
    "changes_accepted": 2,
    "sync_token": "new-sync-token",
    "server_timestamp": "2026-05-01T10:31:05Z"
}

Response (409 Conflict):
{
    "status": "conflict",
    "conflicts": [
        {
            "entity_type": "item",
            "entity_id": "item-uuid-123",
            "client_timestamp": "2026-05-01T10:30:00Z",
            "server_timestamp": "2026-05-01T10:29:50Z",
            "resolution": "server_wins",
            "server_data": "base64-encrypted-payload"
        }
    ],
    "sync_token": "new-sync-token"
}
```

---

#### US-3.4: Pull Changes (Server to Client)
**As a** user
**I want** to receive changes made on other devices
**So that** my grocery list stays in sync everywhere

**Acceptance Criteria:**
- [ ] GET /api/v1/sync returns changes since last sync token
- [ ] Returns only changes from other devices (not originating device)
- [ ] Supports pagination for large change sets
- [ ] Returns encrypted data as stored
- [ ] Includes server timestamp for conflict detection
- [ ] Returns new sync token

**Story Points:** 5
**Assignee:** Backend Developer 1

**API Specification:**
```
GET /api/v1/sync?since_token={sync_token}&limit=100
Authorization: Bearer <access_token>

Response (200 OK):
{
    "changes": [
        {
            "entity_type": "item",
            "entity_id": "item-uuid-789",
            "operation": "INSERT",
            "encrypted_data": "base64-encrypted-payload",
            "server_timestamp": "2026-05-01T11:00:00Z",
            "source_device_id": "other-device-uuid"
        }
    ],
    "has_more": false,
    "sync_token": "new-sync-token",
    "server_timestamp": "2026-05-01T11:05:00Z"
}
```

---

#### US-3.5: Delta Sync Algorithm
**As a** user with limited bandwidth
**I want** only changed data to sync
**So that** sync is fast and efficient

**Acceptance Criteria:**
- [ ] Only changes since last sync are transmitted
- [ ] Sync token encodes last sync timestamp
- [ ] Full sync available if token is invalid/expired
- [ ] Change log pruned after 90 days
- [ ] Snapshot backup created weekly for full sync recovery
- [ ] Sync payload typically < 50KB for daily sync

**Story Points:** 8
**Assignee:** Backend Developer 2

**Delta Sync Flow:**
```
Initial Sync (no token):
┌─────────────┐                    ┌─────────────┐
│   Client    │                    │   Server    │
└──────┬──────┘                    └──────┬──────┘
       │                                  │
       │  POST /sync (token: null)        │
       │─────────────────────────────────►│
       │                                  │
       │     Return all data + token      │
       │◄─────────────────────────────────│

Subsequent Sync (with token):
       │                                  │
       │  POST /sync (token: "abc123")    │
       │─────────────────────────────────►│
       │                                  │
       │     Return only changes          │
       │     since token + new token      │
       │◄─────────────────────────────────│
```

---

#### US-3.6: Conflict Resolution
**As a** user who edits on multiple devices
**I want** conflicts resolved automatically
**So that** I don't lose any data

**Acceptance Criteria:**
- [ ] Conflicts detected by comparing timestamps
- [ ] Last-write-wins as default resolution strategy
- [ ] Conflict resolution is per-entity, not per-sync
- [ ] Conflicts logged for potential manual resolution
- [ ] Client informed of resolved conflicts
- [ ] Deleted items on one device don't resurrect on another

**Story Points:** 8
**Assignee:** Backend Developer 2

**Conflict Resolution Logic:**
```kotlin
fun resolveConflict(
    clientChange: Change,
    serverState: EntityState?
): ConflictResolution {
    // No server state = no conflict
    if (serverState == null) {
        return ConflictResolution.AcceptClient
    }

    // Compare timestamps
    return when {
        // Client change is newer
        clientChange.timestamp > serverState.timestamp ->
            ConflictResolution.AcceptClient

        // Server state is newer
        clientChange.timestamp < serverState.timestamp ->
            ConflictResolution.AcceptServer(serverState.encryptedData)

        // Same timestamp - compare by device ID for determinism
        else -> if (clientChange.deviceId > serverState.deviceId) {
            ConflictResolution.AcceptClient
        } else {
            ConflictResolution.AcceptServer(serverState.encryptedData)
        }
    }
}
```

**Deletion Handling:**
```
Device A: Deletes item X at T1
Device B: Updates item X at T2 (T2 > T1)

Resolution:
- If T2 > T1: Update wins, item is NOT deleted
- If T1 > T2: Delete wins, item is deleted

Tombstone kept for 30 days to prevent resurrection
```

---

#### US-3.7: Device Management
**As a** user with multiple devices
**I want** to see and manage my connected devices
**So that** I can control where my data syncs

**Acceptance Criteria:**
- [ ] List devices endpoint returns all user devices
- [ ] Device includes name, last sync time, platform
- [ ] Remove device endpoint revokes sync access
- [ ] Free tier limited to 1 device sync
- [ ] Premium tier allows unlimited devices
- [ ] Device count enforced on sync request

**Story Points:** 5
**Assignee:** Backend Developer 2

**API Specification:**
```
GET /api/v1/devices
Authorization: Bearer <access_token>

Response:
{
    "devices": [
        {
            "device_id": "uuid",
            "device_name": "Pixel 7 Pro",
            "platform": "android",
            "last_sync_at": "2026-05-01T10:30:00Z",
            "is_current": true
        },
        {
            "device_id": "uuid-2",
            "device_name": "Samsung Galaxy Tab",
            "platform": "android",
            "last_sync_at": "2026-04-30T18:00:00Z",
            "is_current": false
        }
    ],
    "max_devices": 1,  // or "unlimited" for premium
    "subscription_tier": "free"
}

DELETE /api/v1/devices/{device_id}
Authorization: Bearer <access_token>

Response (204 No Content)
```

---

### 2.2 Technical Tasks

| Task ID | Task | Assignee | Est. Hours | Dependencies |
|---------|------|----------|------------|--------------|
| T-3.1 | Create sync_state table with migrations | Backend Dev 1 | 3 | - |
| T-3.2 | Create change_log table with migrations | Backend Dev 1 | 3 | - |
| T-3.3 | Create encrypted_data table | Backend Dev 1 | 2 | - |
| T-3.4 | Implement Argon2 key derivation (Android) | Tech Lead | 6 | - |
| T-3.5 | Implement AES-256-GCM encryption (Android) | Tech Lead | 8 | T-3.4 |
| T-3.6 | Create EncryptionManager class | Tech Lead | 6 | T-3.5 |
| T-3.7 | Implement POST /sync endpoint | Backend Dev 1 | 10 | T-3.1, T-3.2 |
| T-3.8 | Implement GET /sync endpoint | Backend Dev 1 | 6 | T-3.2 |
| T-3.9 | Implement delta sync logic | Backend Dev 2 | 8 | T-3.8 |
| T-3.10 | Implement sync token generation | Backend Dev 2 | 4 | - |
| T-3.11 | Implement conflict detection | Backend Dev 2 | 6 | T-3.7 |
| T-3.12 | Implement conflict resolution | Backend Dev 2 | 8 | T-3.11 |
| T-3.13 | Implement device management endpoints | Backend Dev 2 | 6 | - |
| T-3.14 | Add subscription tier check | Backend Dev 2 | 4 | T-3.13 |
| T-3.15 | Create change log pruning job | Backend Dev 1 | 4 | T-3.2 |
| T-3.16 | Create weekly snapshot backup job | DevOps | 6 | T-3.3 |
| T-3.17 | Set up sync endpoint monitoring | DevOps | 4 | - |
| T-3.18 | Load test sync endpoints | DevOps | 6 | T-3.7, T-3.8 |
| T-3.19 | Design sync status UI components | UI/UX | 8 | - |
| T-3.20 | Design device management UI | UI/UX | 6 | - |
| T-3.21 | Write sync service integration tests | Backend Dev 1 | 8 | T-3.7, T-3.8 |
| T-3.22 | Write encryption unit tests | Tech Lead | 4 | T-3.6 |

---

## 3. Team Assignments

### Tech Lead
**Focus:** Encryption implementation (Android side)
- Implement Argon2 key derivation
- Implement AES-256-GCM encryption/decryption
- Create EncryptionManager class
- Write encryption unit tests
- Review all sync-related code

### UI/UX Engineer
**Focus:** Sync UI preparation
- Design sync status indicators
- Design device management screen
- Design sync error states
- Prepare for account settings integration

### Backend Developer 1
**Focus:** Core sync endpoints
- Create sync database schema
- Implement POST /sync endpoint
- Implement GET /sync endpoint
- Create change log pruning job
- Write integration tests

### Backend Developer 2
**Focus:** Sync algorithms and device management
- Implement delta sync logic
- Implement conflict detection and resolution
- Build device management endpoints
- Add subscription tier enforcement
- Write conflict resolution tests

### DevOps Engineer
**Focus:** Infrastructure and reliability
- Create weekly snapshot backup job
- Set up sync endpoint monitoring
- Conduct load testing
- Optimize database indexes
- Set up alerting for sync failures

---

## 4. Dependencies

### External Dependencies
| Dependency | Owner | Status | Risk |
|------------|-------|--------|------|
| Argon2 library (Android) | Tech Lead | Evaluate | Low |
| BouncyCastle/Tink for AES-GCM | Tech Lead | Evaluate | Low |

### Internal Dependencies
| Dependency | From | To | Notes |
|------------|------|-----|-------|
| Auth system | Sprint 2 | All sync endpoints | JWT validation |
| User tier info | Sprint 2 | Device limiting | Subscription check |
| Database schema | T-3.1, T-3.2, T-3.3 | All backend tasks | Foundation |

---

## 5. Definition of Done

### Sprint Level
- [ ] POST /sync accepts and stores encrypted changes
- [ ] GET /sync returns changes since last sync
- [ ] Delta sync reduces payload by 90%+ vs full sync
- [ ] Conflicts detected and resolved automatically
- [ ] E2E encryption verified (server cannot decrypt)
- [ ] Device management endpoints functional
- [ ] Free tier limited to 1 device
- [ ] Sync endpoints handle 100 requests/second
- [ ] All endpoints have 80% test coverage

### Story Level
- [ ] Acceptance criteria verified
- [ ] Unit tests passing
- [ ] Integration tests passing
- [ ] Code reviewed
- [ ] Security review for encryption code

---

## 6. Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Encryption performance issues | Medium | Medium | Benchmark early; optimize key derivation |
| Conflict resolution edge cases | Medium | Medium | Extensive testing; logging for analysis |
| Change log growth | Medium | Low | Pruning job; archive strategy |
| Sync token invalidation | Low | Medium | Graceful full-sync fallback |
| Data corruption during sync | Low | Critical | Checksums; transaction safety |

---

## 7. Testing Requirements

### Unit Tests
- Encryption/decryption round-trip
- Key derivation consistency
- Conflict resolution logic
- Sync token generation/validation
- Delta calculation

### Integration Tests
- Full sync flow (new device)
- Delta sync flow (existing device)
- Conflict scenarios
- Device limiting
- Multi-device sync

### Load Tests
- 100 concurrent sync requests
- Large payload handling (1000 changes)
- Database performance under load

### Security Tests
- Verify server cannot decrypt data
- Token tampering detection
- Device spoofing prevention

### Test Coverage Targets
| Component | Target |
|-----------|--------|
| Sync service | 80% |
| Encryption module | 90% |
| Conflict resolution | 90% |

---

## 8. Success Metrics

| Metric | Target | Measurement |
|--------|--------|-------------|
| Sync latency (100 items) | <2 seconds | API metrics |
| Encryption overhead | <100ms | Benchmarks |
| Conflict resolution accuracy | 100% | Test results |
| Delta sync efficiency | 90%+ reduction | Payload analysis |
| Sync success rate | >99% | Monitoring |

---

## 9. Sprint Ceremonies

| Ceremony | Day | Duration | Attendees |
|----------|-----|----------|-----------|
| Sprint Planning | Day 1 | 2 hours | All |
| Daily Standup | Daily | 15 min | All |
| Tech Sync | Day 5, 10 | 1 hour | Tech Lead, Backend, DevOps |
| Encryption Review | Day 7 | 1 hour | Tech Lead, Backend Dev 1 |
| Sprint Review | Day 10 | 1 hour | All + stakeholders |
| Retrospective | Day 10 | 45 min | All |

---

## 10. Deliverables Checklist

- [ ] Sync database schema (migrations)
- [ ] POST /api/v1/sync endpoint
- [ ] GET /api/v1/sync endpoint
- [ ] Conflict resolution algorithm
- [ ] Delta sync implementation
- [ ] Device management endpoints
- [ ] Android EncryptionManager class
- [ ] Sync endpoint monitoring dashboard
- [ ] Load test results
- [ ] Sync API documentation
- [ ] Sync status UI designs
- [ ] Device management UI designs

---

## 11. Handoff to Sprint 4

### Completed Items for Next Sprint
- Sync backend fully functional
- E2E encryption implemented and tested
- Conflict resolution working
- Device management API ready

### Prepared Items for Next Sprint
- Ready to integrate sync into Android app
- Ready to implement background sync
- Ready to build account management UI

---

*Sprint 3 Document v1.0 | Phase 2: Growth & Intelligence | Restock Android*
