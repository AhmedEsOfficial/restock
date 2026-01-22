package com.restock.feature.suggestions

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
 * Smart suggestions screen.
 *
 * Displays AI-powered suggestions based on:
 * - Purchase patterns (items due for repurchase)
 * - Learned preferences
 * - Seasonal patterns
 *
 * TODO: Implement full functionality in Sprint 7-8
 */
@Composable
fun SuggestionsScreen(
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
                text = "Smart Suggestions",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SuggestionsScreenPreview() {
    RestockTheme {
        SuggestionsScreen()
    }
}
