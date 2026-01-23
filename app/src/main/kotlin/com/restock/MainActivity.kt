package com.restock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.restock.core.ui.theme.RestockTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for Restock application.
 *
 * This is the single-activity entry point that hosts the Compose UI.
 * Navigation between screens is handled by Navigation Compose.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RestockTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // Navigation graph will be added here
                    // For now, showing a placeholder
                    RestockNavHost()
                }
            }
        }
    }
}
