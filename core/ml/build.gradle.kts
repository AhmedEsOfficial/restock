plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.restock.core.ml"
    compileSdk = 35

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")

        // Add ndk abiFilters for TensorFlow Lite (optional - for smaller APK)
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
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

    // Coroutines
    implementation(libs.bundles.coroutines)

    // TensorFlow Lite for pattern learning
    implementation(libs.bundles.tensorflow)

    // WorkManager for background pattern processing
    implementation(libs.work.runtime)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // DateTime for pattern calculations
    implementation(libs.kotlinx.datetime)

    // Testing
    testImplementation(libs.bundles.testing)
    androidTestImplementation(libs.bundles.testing.android)
    androidTestImplementation(libs.bundles.testing.hilt)
    androidTestImplementation(libs.work.testing)
    kspAndroidTest(libs.hilt.compiler)
}
