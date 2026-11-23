# Task: Launch Operations & Release

**Sprint:** 11-12 (Launch Preparation)
**Owner:** Tech Lead / Senior Developer
**Estimate:** 22 hours (T-13.1 through T-13.8, T-14.1 through T-14.8 combined)
**Priority:** P0
**Prerequisites:** 002_security_hardening.md

---

## Sprint Context

This is the final push to launch. Configure release signing, set up Play Console, create monitoring dashboard, and execute the launch.

### Sprint Theme
> *"Ship with confidence."*

---

## Task Description

Configure release signing, Play Console, staged rollout, and execute launch with proper monitoring.

## T-13.1: Configure Release Signing (2h)

### Create Keystore

```bash
#!/bin/bash
# scripts/create_keystore.sh

keytool -genkey -v \
    -keystore restock-release.keystore \
    -alias restock \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -dname "CN=Restock, OU=Mobile, O=Restock Inc, L=City, S=State, C=US"

echo "Keystore created: restock-release.keystore"
echo "IMPORTANT: Store this securely and backup!"
```

### Configure Signing

```kotlin
// app/build.gradle.kts
android {
    signingConfigs {
        create("release") {
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "../restock-release.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: ""
            keyAlias = System.getenv("KEY_ALIAS") ?: "restock"
            keyPassword = System.getenv("KEY_PASSWORD") ?: ""
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ... other config
        }
    }
}
```

### CI/CD Secrets

```yaml
# .github/workflows/release.yml
env:
  KEYSTORE_PATH: ${{ secrets.KEYSTORE_PATH }}
  KEYSTORE_PASSWORD: ${{ secrets.KEYSTORE_PASSWORD }}
  KEY_ALIAS: ${{ secrets.KEY_ALIAS }}
  KEY_PASSWORD: ${{ secrets.KEY_PASSWORD }}
```

## T-13.2: Set Up Play Console Project (2h)

### Play Console Setup Checklist

- [ ] Create developer account ($25 one-time fee)
- [ ] Create new app in Play Console
- [ ] Set up app details
- [ ] Configure store listing settings
- [ ] Set up pricing and distribution
- [ ] Create internal testing track

### App Details

```
App name: Restock
Default language: English (US)
App type: App
Category: Productivity
Tags: Shopping, Lists, Grocery
```

## T-13.3: Configure Staged Rollout (1h)

### Rollout Strategy

| Stage | Percentage | Duration | Criteria to Advance |
|-------|------------|----------|---------------------|
| Internal | 100% (team) | 1 week | No P0 bugs |
| Closed Testing | 100 users | 1 week | Crash-free > 99% |
| Open Testing | 500 users | 1 week | Crash-free > 99.5% |
| Production (5%) | 5% | 48 hours | Crash-free > 99%, ratings > 4.0 |
| Production (25%) | 25% | 48 hours | Stable metrics |
| Production (50%) | 50% | 48 hours | No regressions |
| Production (100%) | 100% | — | Full launch |

## T-13.4: Create Release AAB (1h)

### Build Script

```bash
#!/bin/bash
# scripts/build_release.sh

set -e

# Clean
./gradlew clean

# Run tests
./gradlew testReleaseUnitTest

# Build release AAB
./gradlew bundleRelease

# Verify AAB
bundletool validate --bundle=app/build/outputs/bundle/release/app-release.aab

echo "Release build complete!"
echo "AAB: app/build/outputs/bundle/release/app-release.aab"
```

## T-13.5: Upload to Play Console (1h)

### Upload Checklist

- [ ] Upload AAB to internal testing track
- [ ] Add release notes
- [ ] Complete content rating questionnaire
- [ ] Complete data safety form
- [ ] Submit for review

## T-13.6: Create Monitoring Dashboard (3h)

### Firebase Dashboard Setup

```kotlin
// Metrics to monitor
object LaunchMetrics {
    // Crashlytics
    const val CRASH_FREE_USERS = "crash_free_users"
    const val ANR_RATE = "anr_rate"

    // Analytics
    const val DAU = "daily_active_users"
    const val ITEMS_ADDED = "items_added"
    const val SUGGESTIONS_ACCEPTED = "suggestions_accepted"
    const val RETENTION_D1 = "retention_d1"
    const val RETENTION_D7 = "retention_d7"
}
```

### Alert Thresholds

| Metric | Threshold | Alert |
|--------|-----------|-------|
| Crash-free users | < 99% | Immediate |
| ANR rate | > 0.5% | Within 4h |
| DAU drop | > 20% | Within 24h |
| Rating average | < 4.0 | Within 24h |

### Monitoring Script

```kotlin
// MonitoringHelper.kt
object MonitoringHelper {

    fun checkHealthMetrics(): HealthStatus {
        // This would integrate with Firebase/Play Console APIs
        return HealthStatus(
            crashFreeRate = 99.5,
            anrRate = 0.1,
            activeUsers = 1000,
            averageRating = 4.5,
        )
    }

    data class HealthStatus(
        val crashFreeRate: Double,
        val anrRate: Double,
        val activeUsers: Int,
        val averageRating: Double,
    ) {
        val isHealthy: Boolean
            get() = crashFreeRate >= 99 &&
                    anrRate <= 0.5 &&
                    averageRating >= 4.0
    }
}
```

## T-13.7: Document Rollback Procedure (2h)

### Rollback Playbook

```markdown
# Rollback Procedure

## When to Rollback
- Crash-free rate < 98%
- Critical bug affecting >5% of users
- Data loss reported
- Security vulnerability discovered

## Steps

### 1. Assess Severity
- P0 (Critical): Immediate rollback
- P1 (Major): Rollback within 4 hours
- P2 (Minor): Fix forward

### 2. Halt Rollout
1. Go to Play Console > Release > Manage
2. Click "Halt rollout"
3. Confirm halt

### 3. Communicate
- Notify team via Slack #restock-alerts
- Document in incident log

### 4. Fix
1. Identify root cause
2. Implement fix
3. Test thoroughly
4. Get code review

### 5. Resume
1. Create new release with fix
2. Upload to Play Console
3. Start at 5% rollout
4. Monitor closely

## Rollback Limitations
- Users who already updated cannot be rolled back
- They will get the fix in the next update
```

## T-13.8: Set Up On-Call Rotation (1h)

### On-Call Schedule

| Day | Primary | Backup |
|-----|---------|--------|
| Launch Day | Tech Lead | UI/UX |
| Day 2 | Tech Lead | UI/UX |
| Day 3-4 | UI/UX | Tech Lead |
| Day 5-7 | Rotate | — |

### Escalation Path

```
User Report / Alert
       ↓
On-Call Reviews (15 min)
       ↓
  ┌────┴────┐
  ↓         ↓
P2/P3     P0/P1
Log &     Alert Team
Monitor   Fix < 24h
```

## T-14.1-14.6: Final Testing & Launch (19h)

### T-14.1: Full Regression Testing (6h)

```markdown
## Regression Test Checklist

### List Feature
- [ ] Add item
- [ ] Add item with quantity
- [ ] Check off item
- [ ] Uncheck item
- [ ] Delete item (swipe)
- [ ] Undo delete
- [ ] Reorder items
- [ ] Edit quantity

### Suggestions
- [ ] Suggestions appear after 3 trips
- [ ] Accept suggestion
- [ ] Dismiss suggestion
- [ ] 3 dismissals stops suggesting

### Meal Tags
- [ ] Add tag
- [ ] Remove tag
- [ ] View Meals tab
- [ ] Items grouped correctly

### Offline
- [ ] Add items offline
- [ ] Check items offline
- [ ] App works on airplane mode

### Performance
- [ ] Cold start < 2s
- [ ] Scroll is smooth
- [ ] No visible jank
```

### T-14.2: Device Compatibility Testing (4h)

| Device | OS | Priority | Status |
|--------|-----|----------|--------|
| Pixel 8 | Android 14 | P0 | ☐ |
| Pixel 6 | Android 13 | P0 | ☐ |
| Samsung S24 | Android 14 | P0 | ☐ |
| Samsung S21 | Android 13 | P0 | ☐ |
| Samsung A52 | Android 12 | P1 | ☐ |
| OnePlus 9 | Android 13 | P1 | ☐ |
| Pixel 4a | Android 12 | P1 | ☐ |

### T-14.5: Launch Readiness Review (2h)

```markdown
## Launch Readiness Checklist

### Technical
- [ ] All P0 bugs fixed
- [ ] All P1 bugs fixed
- [ ] Performance targets met
- [ ] Security audit passed
- [ ] Analytics verified
- [ ] Crashlytics verified

### Store
- [ ] App icon approved
- [ ] Screenshots approved
- [ ] Description approved
- [ ] Privacy policy live
- [ ] Data safety complete
- [ ] Content rating obtained

### Operations
- [ ] Monitoring dashboard ready
- [ ] On-call rotation set
- [ ] Rollback procedure documented
- [ ] Support email configured
```

### T-14.6: Submit for Review (1h)

1. Final AAB upload
2. Release notes added
3. Submit to Production (5% rollout)
4. Wait for review (typically 1-3 days)

### T-14.8: Launch! (1h)

1. Verify app approved
2. Push production release live
3. Monitor initial metrics
4. Celebrate! 🎉

## Acceptance Criteria

- [ ] Release signing configured
- [ ] Play Console set up
- [ ] Staged rollout configured
- [ ] Monitoring dashboard ready
- [ ] Rollback procedure documented
- [ ] Full regression passed
- [ ] Device testing passed
- [ ] Launch readiness approved
- [ ] App submitted and approved

## Definition of Done

- [ ] App live on Play Store
- [ ] Monitoring active
- [ ] First 24h stable
- [ ] Team celebrated

## Notes

- Keep the team available for launch day
- Monitor closely for first 48 hours
- Respond quickly to reviews
- Document lessons learned
