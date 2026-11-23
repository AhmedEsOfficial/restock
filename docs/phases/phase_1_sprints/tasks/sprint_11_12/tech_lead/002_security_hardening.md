# Task: Security Hardening

**Sprint:** 11-12 (Launch Preparation)
**Owner:** Tech Lead / Senior Developer
**Estimate:** 19 hours (T-12.1 through T-12.7 combined)
**Priority:** P0
**Prerequisites:** 001_analytics_crashlytics.md

---

## Sprint Context

Security is critical for user trust. This task implements database encryption, secure logging, and completes a security audit.

### Sprint Theme
> *"Ship with confidence."*

---

## Task Description

Integrate SQLCipher for database encryption, configure secure release logging, and complete security audit.

## T-12.1: Integrate SQLCipher for Room (4h)

### Dependencies

```kotlin
// build.gradle.kts (core:data)
dependencies {
    implementation("net.zetetic:android-database-sqlcipher:4.5.4")
    implementation("androidx.sqlite:sqlite-ktx:2.4.0")
}
```

### Database Key Manager

```kotlin
package com.restock.core.data.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.KeyStore
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseKeyManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    fun getOrCreateDatabaseKey(): ByteArray {
        val existingKey = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        val key = existingKey ?: generateAndStoreKey()
        return key.encoded
    }

    private fun generateAndStoreKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )

        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false) // Don't require biometric
            .build()

        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }

    fun clearKey() {
        if (keyStore.containsAlias(KEY_ALIAS)) {
            keyStore.deleteEntry(KEY_ALIAS)
        }
    }

    companion object {
        private const val KEY_ALIAS = "restock_db_encryption_key"
    }
}
```

### Encrypted Database Factory

```kotlin
package com.restock.core.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import net.sqlcipher.database.SupportFactory
import com.restock.core.data.security.DatabaseKeyManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedDatabaseFactory @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keyManager: DatabaseKeyManager,
) {
    fun <T : RoomDatabase> create(
        klass: Class<T>,
        name: String,
    ): T {
        val passphrase = keyManager.getOrCreateDatabaseKey()
        val factory = SupportFactory(passphrase)

        return Room.databaseBuilder(context, klass, name)
            .openHelperFactory(factory)
            .fallbackToDestructiveMigration() // For development
            .build()
    }
}
```

### Update Database Module

```kotlin
// DatabaseModule.kt
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideRestockDatabase(
        factory: EncryptedDatabaseFactory,
    ): RestockDatabase {
        return factory.create(
            klass = RestockDatabase::class.java,
            name = "restock.db"
        )
    }
}
```

## T-12.2: Configure Release Logging (2h)

### Release Timber Tree

```kotlin
package com.restock.core.logging

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import timber.log.Timber

class ReleaseTree : Timber.Tree() {

    override fun isLoggable(tag: String?, priority: Int): Boolean {
        // Only log warnings and errors in release
        return priority >= Log.WARN
    }

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        // Sanitize message
        val sanitizedMessage = sanitize(message)

        // Log to Crashlytics
        val crashlytics = FirebaseCrashlytics.getInstance()
        crashlytics.log("$priority/$tag: $sanitizedMessage")

        // Record exceptions
        if (t != null) {
            crashlytics.recordException(t)
        } else if (priority >= Log.ERROR) {
            crashlytics.recordException(
                Exception("Error without exception: $sanitizedMessage")
            )
        }
    }

    private fun sanitize(message: String): String {
        return message
            // Remove potential email addresses
            .replace(
                Regex("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"),
                "[EMAIL]"
            )
            // Remove potential phone numbers
            .replace(Regex("\\d{10,}"), "[PHONE]")
            // Remove potential card numbers
            .replace(Regex("\\d{13,19}"), "[CARD]")
            // Remove potential SSN
            .replace(Regex("\\d{3}-\\d{2}-\\d{4}"), "[SSN]")
    }
}
```

### Application Configuration

```kotlin
// RestockApplication.kt
@HiltAndroidApp
class RestockApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Configure logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(ReleaseTree())
        }
    }
}
```

## T-12.3: Verify ProGuard Obfuscation (2h)

### ProGuard Rules

```proguard
# proguard-rules.pro

# Keep model classes for serialization
-keep class com.restock.core.domain.model.** { *; }
-keep class com.restock.core.data.database.entity.** { *; }

# Keep Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers class * {
    @androidx.room.* <fields>;
    @androidx.room.* <methods>;
}

# Keep Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.* class * { *; }

# Firebase
-keep class com.google.firebase.** { *; }

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Prevent obfuscation of Crashlytics symbols
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
```

### Verify Obfuscation

```bash
#!/bin/bash
# scripts/verify_obfuscation.sh

# Build release APK
./gradlew assembleRelease

# Check mapping file exists
if [ ! -f "app/build/outputs/mapping/release/mapping.txt" ]; then
    echo "ERROR: Mapping file not found!"
    exit 1
fi

# Decompile and check
apktool d app/build/outputs/apk/release/app-release.apk -o decompiled

# Verify sensitive classes are obfuscated
if grep -r "GroceryItem" decompiled/smali/; then
    echo "WARNING: GroceryItem class name not obfuscated"
fi

# Clean up
rm -rf decompiled

echo "Obfuscation verification complete"
```

## T-12.4: Add Network Security Config (1h)

```xml
<!-- res/xml/network_security_config.xml -->
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- Disallow cleartext traffic in production -->
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>

    <!-- Allow cleartext for local development only -->
    <debug-overrides>
        <trust-anchors>
            <certificates src="user" />
            <certificates src="system" />
        </trust-anchors>
    </debug-overrides>
</network-security-config>
```

```xml
<!-- AndroidManifest.xml -->
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

## T-12.5: Security Audit Checklist (3h)

### Data Storage

- [ ] Database encrypted with SQLCipher
- [ ] Encryption key stored in Android Keystore
- [ ] No sensitive data in SharedPreferences (use encrypted DataStore)
- [ ] No sensitive data in logs
- [ ] No hardcoded secrets in code

### Network

- [ ] HTTPS only (no cleartext traffic)
- [ ] Network security config in place
- [ ] Certificate pinning (consider for Phase 2)

### Code Security

- [ ] ProGuard enabled for release builds
- [ ] No debug code in release builds
- [ ] No test data in release builds
- [ ] No exposed content providers
- [ ] No exported activities without proper permissions

### Authentication (Phase 2)

- [ ] N/A for Phase 1 (local only)

### Audit Script

```kotlin
// SecurityAuditHelper.kt (debug only)
object SecurityAuditHelper {

    fun runAudit(context: Context): List<SecurityIssue> {
        val issues = mutableListOf<SecurityIssue>()

        // Check for debug build
        if (BuildConfig.DEBUG) {
            issues.add(SecurityIssue.INFO, "Running in debug mode")
        }

        // Check database encryption
        val dbFile = context.getDatabasePath("restock.db")
        if (dbFile.exists()) {
            val header = dbFile.inputStream().use { it.readNBytes(16) }
            val isEncrypted = !header.decodeToString().startsWith("SQLite format")
            if (!isEncrypted) {
                issues.add(SecurityIssue.CRITICAL, "Database is not encrypted")
            }
        }

        // Check for exported components
        val packageInfo = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_ACTIVITIES
        )
        packageInfo.activities?.forEach { activity ->
            if (activity.exported) {
                issues.add(
                    SecurityIssue.WARNING,
                    "Exported activity: ${activity.name}"
                )
            }
        }

        return issues
    }

    data class SecurityIssue(
        val level: Level,
        val message: String,
    ) {
        enum class Level { INFO, WARNING, CRITICAL }
    }
}
```

## T-12.6: Fix Security Audit Findings (4h)

Document and fix any issues found during audit.

## T-12.7: Basic Penetration Testing (3h)

### Test Cases

1. **Data at rest:** Verify database is encrypted
2. **Data in transit:** Verify HTTPS only
3. **Logging:** Verify no sensitive data logged
4. **Backup:** Verify android:allowBackup="false"
5. **Debug:** Verify android:debuggable="false" in release

### Backup Prevention

```xml
<!-- AndroidManifest.xml -->
<application
    android:allowBackup="false"
    android:fullBackupContent="false"
    ...>
```

## Acceptance Criteria

- [ ] SQLCipher integrated and working
- [ ] Database encrypted verified
- [ ] No sensitive data in logs
- [ ] ProGuard obfuscation working
- [ ] Network security config in place
- [ ] Security audit passed
- [ ] All findings fixed

## Definition of Done

- [ ] Database encrypted
- [ ] Logging secured
- [ ] Code obfuscated
- [ ] Security audit complete
- [ ] Documentation updated

## Notes

- SQLCipher adds ~5MB to APK size
- First database access will be slower due to encryption
- Keep encryption key backup strategy for Phase 2 cloud sync
- Consider security testing service for launch
