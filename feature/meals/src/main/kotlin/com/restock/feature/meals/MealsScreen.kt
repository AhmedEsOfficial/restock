package com.restock.feature.meals

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.restock.core.ui.theme.RestockTheme

/**
 * Meals screen for viewing items grouped by meal type.
 *
 * Displays items organized by:
 * - Breakfast
 * - Lunch
 * - Dinner
 * - Snack
 *
 * Allows users to quickly add all items for a specific meal
 * to their shopping list.
 *
 * TODO: Implement full functionality in Sprint 9-10
 */
@Composable
fun MealsScreen(
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Meals",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MealsScreenPreview() {
    RestockTheme {
        MealsScreen()
    }
}
