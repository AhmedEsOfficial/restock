package com.restock.feature.list

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
 * Main grocery list screen.
 *
 * This is the primary screen of the app where users:
 * - View their current shopping list
 * - Add new items
 * - Check off items as they shop
 * - Reorder items via drag-and-drop
 *
 * TODO: Implement full functionality in Sprint 3-4
 */
@Composable
fun ListScreen(
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
                text = "Grocery List",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ListScreenPreview() {
    RestockTheme {
        ListScreen()
    }
}
