package com.restock

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * Custom AndroidJUnitRunner that uses HiltTestApplication for instrumented tests.
 *
 * This test runner is configured in app/build.gradle.kts:
 * ```
 * testInstrumentationRunner = "com.restock.HiltTestRunner"
 * ```
 *
 * It replaces the standard test application with Hilt's test application,
 * enabling dependency injection in instrumented tests.
 */
class HiltTestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        classLoader: ClassLoader?,
        className: String?,
        context: Context?
    ): Application {
        return super.newApplication(
            classLoader,
            HiltTestApplication::class.java.name,
            context
        )
    }
}
