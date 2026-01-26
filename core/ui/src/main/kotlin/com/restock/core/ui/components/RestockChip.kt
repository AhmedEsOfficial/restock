package com.restock.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.ui.preview.LightDarkPreview
import com.restock.core.ui.preview.RestockPreviewTheme
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockCornerRadius

/**
 * Background opacity for chip containers.
 */
private const val CHIP_BACKGROUND_ALPHA = 0.15f

/**
 * Size for the remove icon in chips.
 */
private val ChipRemoveIconSize = 14.dp

/**
 * Horizontal padding for chips.
 */
private val ChipHorizontalPadding = 8.dp

/**
 * Vertical padding for chips.
 */
private val ChipVerticalPadding = 4.dp

/**
 * Meal type enum for type-safe chip colors.
 *
 * Each meal type has an associated display name, emoji, and color scheme.
 * Use this enum with [MealTagChip] for consistent meal categorization.
 *
 * **Colors per Design Spec:**
 * - Breakfast: Amber (#F59E0B)
 * - Lunch: Emerald (#10B981)
 * - Dinner: Indigo (#6366F1)
 * - Snack: Pink (#EC4899)
 */
enum class MealType(
    val displayName: String,
    val emoji: String,
) {
    BREAKFAST("Breakfast", "\uD83C\uDF05"), // 🌅
    LUNCH("Lunch", "\u2600\uFE0F"), // ☀️
    DINNER("Dinner", "\uD83C\uDF19"), // 🌙
    SNACK("Snack", "\u2728"), // ✨
    ;

    /**
     * The primary color for this meal type.
     */
    val color: Color
        get() =
            when (this) {
                BREAKFAST -> RestockColors.Breakfast
                LUNCH -> RestockColors.Lunch
                DINNER -> RestockColors.Dinner
                SNACK -> RestockColors.Snack
            }

    /**
     * The container (background) color for this meal type.
     * Used for chip backgrounds with 15% opacity.
     */
    val containerColor: Color
        get() =
            when (this) {
                BREAKFAST -> RestockColors.BreakfastContainer
                LUNCH -> RestockColors.LunchContainer
                DINNER -> RestockColors.DinnerContainer
                SNACK -> RestockColors.SnackContainer
            }
}

/**
 * Meal tag chip component for displaying meal categories on grocery items.
 *
 * Displays the meal type with an optional emoji prefix and optional remove button.
 * Used for tagging items with their associated meals (Breakfast, Lunch, Dinner, Snack).
 *
 * **Specifications:**
 * - Corner radius: 8dp
 * - Background: Meal color @ 15% opacity
 * - Text color: Meal color
 * - Padding: 8dp horizontal, 4dp vertical
 * - Font: Label Small (11sp, Medium)
 * - Remove icon: 14dp
 *
 * @param mealType The type of meal this chip represents.
 * @param modifier Modifier to be applied to the chip.
 * @param showEmoji Whether to display the emoji before the meal name.
 * @param onRemove Optional callback when the remove button is clicked. If null, no remove button is shown.
 *
 * Usage:
 * ```kotlin
 * // Basic meal tag
 * MealTagChip(mealType = MealType.BREAKFAST)
 *
 * // With emoji
 * MealTagChip(
 *     mealType = MealType.LUNCH,
 *     showEmoji = true
 * )
 *
 * // With remove button
 * MealTagChip(
 *     mealType = MealType.DINNER,
 *     onRemove = { /* handle removal */ }
 * )
 * ```
 */
@Composable
fun MealTagChip(
    mealType: MealType,
    modifier: Modifier = Modifier,
    showEmoji: Boolean = true,
    onRemove: (() -> Unit)? = null,
) {
    val chipColor = mealType.color

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(RestockCornerRadius.small),
        color = chipColor.copy(alpha = CHIP_BACKGROUND_ALPHA),
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = ChipHorizontalPadding,
                    vertical = ChipVerticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (showEmoji) {
                Text(
                    text = mealType.emoji,
                    style = MaterialTheme.typography.labelSmall,
                )
                Spacer(Modifier.width(4.dp))
            }

            Text(
                text = mealType.displayName,
                style = MaterialTheme.typography.labelSmall,
                color = chipColor,
            )

            onRemove?.let { onRemoveClick ->
                Spacer(Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Remove ${mealType.displayName} tag",
                    tint = chipColor,
                    modifier =
                        Modifier
                            .size(ChipRemoveIconSize)
                            .clip(RoundedCornerShape(7.dp))
                            .clickable(
                                role = Role.Button,
                                onClick = onRemoveClick,
                            ),
                )
            }
        }
    }
}

/**
 * Alias for [MealTagChip] for API consistency with the design spec.
 *
 * @see MealTagChip
 */
@Composable
fun RestockMealChip(
    mealType: MealType,
    modifier: Modifier = Modifier,
    showEmoji: Boolean = true,
    onRemove: (() -> Unit)? = null,
) {
    MealTagChip(
        mealType = mealType,
        modifier = modifier,
        showEmoji = showEmoji,
        onRemove = onRemove,
    )
}

/**
 * Generic chip component for various use cases.
 *
 * A flexible chip that can be customized with different colors,
 * optional leading/trailing icons, and click handlers.
 *
 * **Specifications:**
 * - Corner radius: 8dp
 * - Background: Content color @ 15% opacity
 * - Padding: 8dp horizontal, 4dp vertical
 * - Font: Label Small (11sp, Medium)
 *
 * @param text The text to display in the chip.
 * @param modifier Modifier to be applied to the chip.
 * @param color The primary color for text and icons. Background uses 15% opacity.
 * @param onClick Optional click handler for the chip.
 * @param leadingIcon Optional composable for the leading icon/content.
 * @param trailingIcon Optional composable for the trailing icon/content.
 *
 * Usage:
 * ```kotlin
 * // Simple text chip
 * RestockChip(text = "Custom Chip")
 *
 * // Chip with leading icon
 * RestockChip(
 *     text = "Tagged",
 *     leadingIcon = {
 *         Icon(Icons.Default.Tag, contentDescription = null)
 *     }
 * )
 *
 * // Clickable chip with trailing icon
 * RestockChip(
 *     text = "Filter",
 *     onClick = { /* handle click */ },
 *     trailingIcon = {
 *         Icon(Icons.Default.Close, contentDescription = "Remove filter")
 *     }
 * )
 * ```
 */
@Composable
fun RestockChip(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = RestockColors.Accent,
    onClick: (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val clickableModifier =
        if (onClick != null) {
            Modifier.clickable(
                role = Role.Button,
                onClick = onClick,
            )
        } else {
            Modifier
        }

    Surface(
        modifier = modifier.then(clickableModifier),
        shape = RoundedCornerShape(RestockCornerRadius.small),
        color = color.copy(alpha = CHIP_BACKGROUND_ALPHA),
    ) {
        Row(
            modifier =
                Modifier.padding(
                    horizontal = ChipHorizontalPadding,
                    vertical = ChipVerticalPadding,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.let { icon ->
                Box(modifier = Modifier.size(ChipRemoveIconSize)) {
                    icon()
                }
                Spacer(Modifier.width(4.dp))
            }

            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
            )

            trailingIcon?.let { icon ->
                Spacer(Modifier.width(4.dp))
                Box(modifier = Modifier.size(ChipRemoveIconSize)) {
                    icon()
                }
            }
        }
    }
}

/**
 * Info chip for displaying pattern information like "Usually every 7 days".
 *
 * Uses the accent color scheme for smart/pattern-related information.
 *
 * @param text The text to display (e.g., "Usually every 7 days").
 * @param modifier Modifier to be applied to the chip.
 *
 * Usage:
 * ```kotlin
 * RestockInfoChip(text = "Usually every 7 days")
 * ```
 */
@Composable
fun RestockInfoChip(
    text: String,
    modifier: Modifier = Modifier,
) {
    RestockChip(
        text = text,
        modifier = modifier,
        color = RestockColors.Accent,
    )
}

/**
 * Smart badge chip indicating pattern-learned items.
 *
 * Displays "SMART" in a highlighted accent color chip.
 *
 * @param modifier Modifier to be applied to the chip.
 *
 * Usage:
 * ```kotlin
 * Row {
 *     Text("Milk")
 *     Spacer(Modifier.width(8.dp))
 *     RestockSmartBadge()
 * }
 * ```
 */
@Composable
fun RestockSmartBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = RestockColors.Accent.copy(alpha = 0.15f),
    ) {
        Text(
            text = "SMART",
            style = MaterialTheme.typography.labelSmall,
            color = RestockColors.Accent,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
        )
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun MealTagChipBreakfastPreview() {
    RestockPreviewTheme {
        MealTagChip(
            mealType = MealType.BREAKFAST,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MealTagChipLunchPreview() {
    RestockPreviewTheme {
        MealTagChip(
            mealType = MealType.LUNCH,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MealTagChipDinnerPreview() {
    RestockPreviewTheme {
        MealTagChip(
            mealType = MealType.DINNER,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MealTagChipSnackPreview() {
    RestockPreviewTheme {
        MealTagChip(
            mealType = MealType.SNACK,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MealTagChipAllTypesPreview() {
    RestockPreviewTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MealTagChip(mealType = MealType.BREAKFAST)
            MealTagChip(mealType = MealType.LUNCH)
            MealTagChip(mealType = MealType.DINNER)
            MealTagChip(mealType = MealType.SNACK)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MealTagChipWithRemovePreview() {
    RestockPreviewTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MealTagChip(
                mealType = MealType.BREAKFAST,
                onRemove = {},
            )
            MealTagChip(
                mealType = MealType.LUNCH,
                onRemove = {},
            )
            MealTagChip(
                mealType = MealType.DINNER,
                onRemove = {},
            )
            MealTagChip(
                mealType = MealType.SNACK,
                onRemove = {},
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MealTagChipNoEmojiPreview() {
    RestockPreviewTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MealTagChip(
                mealType = MealType.BREAKFAST,
                showEmoji = false,
            )
            MealTagChip(
                mealType = MealType.DINNER,
                showEmoji = false,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GenericChipPreview() {
    RestockPreviewTheme {
        RestockChip(
            text = "Custom Chip",
            color = RestockColors.Info,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun GenericChipAccentPreview() {
    RestockPreviewTheme {
        RestockChip(
            text = "Usually every 7 days",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChipWithIconsPreview() {
    RestockPreviewTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RestockChip(
                text = "With trailing",
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Remove",
                        tint = RestockColors.Accent,
                        modifier = Modifier.size(ChipRemoveIconSize),
                    )
                },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InfoChipPreview() {
    RestockPreviewTheme {
        RestockInfoChip(
            text = "Usually every 7 days",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SmartBadgePreview() {
    RestockPreviewTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Milk", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.width(8.dp))
            RestockSmartBadge()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@LightDarkPreview
@Composable
private fun AllChipsPreview() {
    RestockPreviewTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Meal Tag Chips",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MealType.entries.forEach { mealType ->
                    MealTagChip(mealType = mealType)
                }
            }

            Spacer(Modifier.size(24.dp))

            Text(
                text = "With Remove Button",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MealType.entries.forEach { mealType ->
                    MealTagChip(
                        mealType = mealType,
                        onRemove = {},
                    )
                }
            }

            Spacer(Modifier.size(24.dp))

            Text(
                text = "No Emoji",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MealType.entries.forEach { mealType ->
                    MealTagChip(
                        mealType = mealType,
                        showEmoji = false,
                    )
                }
            }

            Spacer(Modifier.size(24.dp))

            Text(
                text = "Generic Chips",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                RestockChip(text = "Accent", color = RestockColors.Accent)
                RestockChip(text = "Info", color = RestockColors.Info)
                RestockChip(text = "Success", color = RestockColors.Success)
                RestockChip(text = "Error", color = RestockColors.Error)
            }

            Spacer(Modifier.size(24.dp))

            Text(
                text = "Smart Badge",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Milk", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.width(8.dp))
                RestockSmartBadge()
            }
        }
    }
}
