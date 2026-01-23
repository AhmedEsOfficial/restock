package com.restock

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Main application class for Restock.
 *
 * This class serves as the entry point for Hilt dependency injection
 * and any application-wide initialization.
 */
@HiltAndroidApp
class RestockApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Application-wide initialization can be added here
    }
}
