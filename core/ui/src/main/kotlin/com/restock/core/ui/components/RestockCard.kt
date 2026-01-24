package com.restock.core.ui.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.ui.preview.LightDarkPreview
import com.restock.core.ui.preview.RestockPreviewTheme
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockElevation
import com.restock.core.ui.theme.RestockShapes
import com.restock.core.ui.theme.RestockSpacing

/**
 * Variant types for [RestockCard].
 *
 * Each variant provides different visual styling appropriate for different use cases:
 * - [DEFAULT] - Standard card for list items with subtle shadow
 * - [ELEVATED] - More prominent shadow for suggestions and highlighted content
 * - [OUTLINED] - No shadow but with border for selected states
 */
enum class RestockCardVariant {
    /** Standard card for list items with subtle 1dp shadow */
    DEFAULT,
    /** Elevated card for suggestions with more prominent 4dp shadow */
    ELEVATED,
    /** Outlined card with border, no shadow - for selected states */
    OUTLINED,
}

/**
 * Core card component for the Restock design system.
 *
 * RestockCard is a versatile container that supports multiple visual variants
 * for different use cases throughout the app. It uses the Restock theme's
 * elevation and color tokens for consistent styling.
 *
 * ## Variants
 * - **DEFAULT**: List items - subtle shadow, 1dp elevation
 * - **ELEVATED**: Suggestions - more prominent shadow, 4dp elevation
 * - **OUTLINED**: Selected states - 1dp border, no shadow
 *
 * ## Usage
 * ```kotlin
 * // Default card for list items
 * RestockCard {
 *     Text("Grocery Item")
 * }
 *
 * // Elevated card for suggestions
 * RestockCard(variant = RestockCardVariant.ELEVATED) {
 *     Text("Suggested Item")
 * }
 *
 * // Outlined card for selected state
 * RestockCard(variant = RestockCardVariant.OUTLINED) {
 *     Text("Selected Item")
 * }
 *
 * // Clickable card
 * RestockCard(onClick = { handleClick() }) {
 *     Text("Tap me")
 * }
 * ```
 *
 * @param modifier Modifier to be applied to the card.
 * @param variant The visual style variant of the card. Defaults to [RestockCardVariant.DEFAULT].
 * @param onClick Optional click handler. If null, card is not clickable.
 * @param shape The shape of the card. Defaults to [MaterialTheme.shapes.large] (16dp radius).
 * @param containerColor Background color of the card. Defaults to [RestockColors.Card].
 * @param contentColor Default content color for text and icons. Defaults to [RestockColors.Primary].
 * @param content The content to display inside the card.
 */
@Composable
fun RestockCard(
    modifier: Modifier = Modifier,
    variant: RestockCardVariant = RestockCardVariant.DEFAULT,
    onClick: (() -> Unit)? = null,
    shape: Shape = MaterialTheme.shapes.large,
    containerColor: Color = RestockColors.Card,
    contentColor: Color = RestockColors.Primary,
    content: @Composable ColumnScope.() -> Unit,
) {
    val elevation = when (variant) {
        RestockCardVariant.DEFAULT -> RestockElevation.level1
        RestockCardVariant.ELEVATED -> RestockElevation.level2
        RestockCardVariant.OUTLINED -> RestockElevation.level0
    }

    val border = when (variant) {
        RestockCardVariant.OUTLINED -> BorderStroke(1.dp, RestockColors.Divider)
        else -> null
    }

    val pressedElevation = when (variant) {
        RestockCardVariant.DEFAULT -> RestockElevation.level2
        RestockCardVariant.ELEVATED -> RestockElevation.level3
        RestockCardVariant.OUTLINED -> RestockElevation.level0
    }

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = elevation,
                pressedElevation = pressedElevation,
            ),
            border = border,
            content = content,
        )
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = elevation,
            ),
            border = border,
            content = content,
        )
    }
}

/**
 * Elevated card for suggestions and highlighted content.
 *
 * This is a convenience alias for [RestockCard] with [RestockCardVariant.ELEVATED].
 * Use this for suggestion cards and other highlighted content that needs
 * more visual prominence.
 *
 * @param modifier Modifier to be applied to the card.
 * @param onClick Optional click handler. If null, card is not clickable.
 * @param content The content of the card.
 *
 * @see RestockCard
 */
@Composable
fun RestockElevatedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    RestockCard(
        modifier = modifier,
        variant = RestockCardVariant.ELEVATED,
        onClick = onClick,
        shape = RestockShapes.large,
        content = content,
    )
}

/**
 * Outlined card for selected states.
 *
 * This is a convenience alias for [RestockCard] with [RestockCardVariant.OUTLINED].
 * Use this for cards that represent selected items or need a distinct
 * bordered appearance without shadow.
 *
 * @param modifier Modifier to be applied to the card.
 * @param onClick Optional click handler. If null, card is not clickable.
 * @param content The content of the card.
 *
 * @see RestockCard
 */
@Composable
fun RestockOutlinedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    RestockCard(
        modifier = modifier,
        variant = RestockCardVariant.OUTLINED,
        onClick = onClick,
        content = content,
    )
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun DefaultCardPreview() {
    RestockPreviewTheme {
        RestockCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "Default Card",
                modifier = Modifier.padding(RestockSpacing.md),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ElevatedCardPreview() {
    RestockPreviewTheme {
        RestockCard(
            variant = RestockCardVariant.ELEVATED,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "Elevated Card (for suggestions)",
                modifier = Modifier.padding(RestockSpacing.md),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OutlinedCardPreview() {
    RestockPreviewTheme {
        RestockCard(
            variant = RestockCardVariant.OUTLINED,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "Outlined Card",
                modifier = Modifier.padding(RestockSpacing.md),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ClickableCardPreview() {
    RestockPreviewTheme {
        RestockCard(
            onClick = { /* Handle click */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "Clickable Card",
                modifier = Modifier.padding(RestockSpacing.md),
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun AllCardVariantsPreview() {
    RestockPreviewTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
        ) {
            RestockCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Default Card - List Items",
                    modifier = Modifier.padding(RestockSpacing.md),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            RestockCard(
                variant = RestockCardVariant.ELEVATED,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Elevated Card - Suggestions",
                    modifier = Modifier.padding(RestockSpacing.md),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            RestockCard(
                variant = RestockCardVariant.OUTLINED,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Outlined Card - Selected State",
                    modifier = Modifier.padding(RestockSpacing.md),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RestockElevatedCardPreview() {
    RestockPreviewTheme {
        RestockElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "Suggestion Card (RestockElevatedCard)",
                modifier = Modifier.padding(RestockSpacing.md),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RestockOutlinedCardPreview() {
    RestockPreviewTheme {
        RestockOutlinedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "Selected Card (RestockOutlinedCard)",
                modifier = Modifier.padding(RestockSpacing.md),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ClickableElevatedCardPreview() {
    RestockPreviewTheme {
        RestockElevatedCard(
            onClick = { /* Handle click */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text(
                text = "Clickable Suggestion",
                modifier = Modifier.padding(RestockSpacing.md),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
