plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.restock.core.data"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // Room schema export for migrations
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
            arg("room.incremental", "true")
            arg("room.generateKotlin", "true")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    // Project modules
    implementation(project(":core:domain"))

    // AndroidX Core
    implementation(libs.androidx.core.ktx)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Room
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    // SQLCipher for database encryption
    implementation(libs.sqlcipher.android)

    // DataStore
    implementation(libs.datastore.preferences)
    implementation(libs.datastore.core)

    // Coroutines
    implementation(libs.bundles.coroutines)

    // Serialization
    implementation(libs.kotlin.serialization.json)

    // Ktor Networking
    implementation(libs.bundles.ktor)
    implementation(libs.okhttp.logging)

    // WorkManager for background processing
    implementation(libs.work.runtime)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // DateTime
    implementation(libs.kotlinx.datetime)

    // Testing
    testImplementation(libs.bundles.testing)
    testImplementation(libs.bundles.testing.room)
    testImplementation(libs.robolectric)
    androidTestImplementation(libs.bundles.testing.android)
    androidTestImplementation(libs.bundles.testing.hilt)
    androidTestImplementation(libs.bundles.testing.room)
    androidTestImplementation(libs.work.testing)
    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.coroutines.test)
    kspAndroidTest(libs.hilt.compiler)
}
