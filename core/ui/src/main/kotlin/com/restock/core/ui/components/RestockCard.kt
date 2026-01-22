package com.restock.core.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockShapeTokens
import com.restock.core.ui.theme.RestockShapes
import com.restock.core.ui.theme.RestockTheme

/**
 * Standard card component for list items.
 *
 * Uses 14dp corner radius and subtle shadow per design spec.
 *
 * @param modifier Modifier to be applied to the card.
 * @param onClick Optional click handler. If null, card is not clickable.
 * @param content The content of the card.
 */
@Composable
fun RestockCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = RestockShapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = RestockColors.Surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 1.dp,
                pressedElevation = 2.dp
            ),
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = RestockShapes.medium,
            colors = CardDefaults.cardColors(
                containerColor = RestockColors.Surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 1.dp
            ),
            content = content
        )
    }
}

/**
 * Elevated card for suggestions and highlighted content.
 *
 * Uses 16dp corner radius and more prominent shadow.
 *
 * @param modifier Modifier to be applied to the card.
 * @param onClick Optional click handler. If null, card is not clickable.
 * @param content The content of the card.
 */
@Composable
fun RestockElevatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = RestockShapes.large,
            colors = CardDefaults.cardColors(
                containerColor = RestockColors.Surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp,
                pressedElevation = 8.dp
            ),
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = RestockShapes.large,
            colors = CardDefaults.cardColors(
                containerColor = RestockColors.Surface
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            ),
            content = content
        )
    }
}

// Previews
@Preview(showBackground = true)
@Composable
private fun RestockCardPreview() {
    RestockTheme {
        RestockCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Grocery Item",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RestockElevatedCardPreview() {
    RestockTheme {
        RestockElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Suggestion Card",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
