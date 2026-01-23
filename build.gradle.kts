// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    // Code quality plugins
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
}

// Configure all projects
allprojects {
    // Common configurations can be added here if needed
}

// Configure all subprojects
subprojects {
    // Apply code quality plugins
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    // Configure Detekt
    detekt {
        config.setFrom(files("${rootProject.projectDir}/config/detekt/detekt.yml"))
        buildUponDefaultConfig = true
        allRules = false
        parallel = true
        autoCorrect = false
        ignoreFailures = false
        // Use baseline file if it exists (allows gradual adoption)
        val baselineFile = file("detekt-baseline.xml")
        if (baselineFile.exists()) {
            baseline = baselineFile
        }
    }

    // Add Detekt formatting rules plugin
    dependencies {
        "detektPlugins"(rootProject.libs.detekt.formatting)
    }

    // Configure Ktlint
    ktlint {
        android.set(true)
        outputColorName.set("RED")
        ignoreFailures.set(false)
        enableExperimentalRules.set(false)
        filter {
            exclude("**/generated/**")
            exclude("**/build/**")
        }
    }

    // Apply common configurations after project evaluation
    afterEvaluate {
        // Common Android configurations
        extensions.findByType<com.android.build.gradle.BaseExtension>()?.apply {
            compileSdkVersion(35)

            defaultConfig {
                minSdk = 26
                targetSdk = 35

                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_17
                targetCompatibility = JavaVersion.VERSION_17
            }
        }

        // Common Kotlin configurations
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
            compilerOptions {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
                freeCompilerArgs.addAll(
                    "-opt-in=kotlin.RequiresOptIn",
                    "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                    "-opt-in=kotlinx.coroutines.FlowPreview",
                )
            }
        }
    }
}

// Task to clean the build directory
tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory)
}
