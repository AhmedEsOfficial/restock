plugins {
    alias(libs.plugins.kotlin.jvm)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // Coroutines
    implementation(libs.coroutines.core)

    // DateTime for domain models
    api(libs.kotlinx.datetime)

    // Immutable Collections for stable state
    api(libs.kotlinx.collections.immutable)

    // Testing
    testImplementation(libs.bundles.testing)
}
