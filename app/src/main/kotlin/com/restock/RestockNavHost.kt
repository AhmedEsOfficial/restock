package com.restock

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

/**
 * Navigation graph for the Restock application.
 *
 * This composable sets up the navigation structure and defines
 * routes to all feature screens.
 */
@Composable
fun RestockNavHost(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "list",
        modifier = modifier
    ) {
        // Main list screen (home)
        composable("list") {
            // TODO: Replace with actual ListScreen from :feature:list
            PlaceholderScreen("Grocery List")
        }

        // Suggestions screen
        composable("suggestions") {
            // TODO: Replace with actual SuggestionsScreen from :feature:suggestions
            PlaceholderScreen("Smart Suggestions")
        }

        // Meals screen
        composable("meals") {
            // TODO: Replace with actual MealsScreen from :feature:meals
            PlaceholderScreen("Meals")
        }

        // History screen
        composable("history") {
            // TODO: Replace with actual HistoryScreen from :feature:history
            PlaceholderScreen("Purchase History")
        }

        // Settings screen
        composable("settings") {
            // TODO: Replace with actual SettingsScreen from :feature:settings
            PlaceholderScreen("Settings")
        }
    }
}

/**
 * Placeholder screen used during development.
 * Will be replaced with actual feature screens.
 */
@Composable
private fun PlaceholderScreen(
    title: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}
