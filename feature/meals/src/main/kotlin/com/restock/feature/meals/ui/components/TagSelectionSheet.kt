package com.restock.feature.meals.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.domain.model.MealType
import com.restock.core.ui.preview.LightDarkPreview
import com.restock.core.ui.preview.RestockPreviewTheme
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockCornerRadius
import com.restock.core.ui.theme.RestockSpacing
import com.restock.core.ui.components.MealType as UiMealType

/**
 * Background opacity for unselected tag options.
 */
private const val UNSELECTED_BORDER_ALPHA = 0.3f

/**
 * Bottom sheet for selecting meal tags for an item.
 *
 * Displays all four meal types (Breakfast, Lunch, Dinner, Snack) as
 * selectable options. Supports multi-select, allowing items to be
 * tagged with multiple meals.
 *
 * **Design Specifications:**
 * - Modal bottom sheet with rounded corners
 * - Each option shows emoji, name, and checkmark when selected
 * - Selected options have colored background and border
 * - Unselected options have transparent background with muted border
 *
 * @param itemName The name of the item being tagged (for display).
 * @param currentTags Currently selected meal types for this item.
 * @param onTagToggle Called when a tag is toggled (added or removed).
 * @param onDismiss Called when the sheet is dismissed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagSelectionSheet(
    itemName: String,
    currentTags: List<MealType>,
    onTagToggle: (MealType) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = RestockColors.Card,
        shape =
            RoundedCornerShape(
                topStart = RestockCornerRadius.extraLarge,
                topEnd = RestockCornerRadius.extraLarge,
            ),
    ) {
        TagSelectionSheetContent(
            itemName = itemName,
            currentTags = currentTags,
            onTagToggle = onTagToggle,
        )
    }
}

/**
 * Content of the tag selection sheet.
 *
 * Separated from the ModalBottomSheet wrapper for easier preview and testing.
 */
@Composable
fun TagSelectionSheetContent(
    itemName: String,
    currentTags: List<MealType>,
    onTagToggle: (MealType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(RestockSpacing.lg),
    ) {
        // Title
        Text(
            text = "Tag \"$itemName\"",
            style = MaterialTheme.typography.titleLarge,
            color = RestockColors.Primary,
        )

        Spacer(Modifier.height(RestockSpacing.xs))

        // Subtitle
        Text(
            text = "Select meal categories for this item",
            style = MaterialTheme.typography.bodyMedium,
            color = RestockColors.Muted,
        )

        Spacer(Modifier.height(RestockSpacing.lg))

        // Meal type options
        MealType.entries.forEach { mealType ->
            val isSelected = mealType in currentTags
            MealTagOption(
                mealType = mealType,
                isSelected = isSelected,
                onClick = { onTagToggle(mealType) },
            )
        }

        Spacer(Modifier.height(RestockSpacing.xl))
    }
}

/**
 * Individual meal tag option in the selection sheet.
 *
 * Shows the meal emoji, name, and a checkmark when selected.
 * Uses the meal's color for visual distinction.
 *
 * @param mealType The meal type this option represents.
 * @param isSelected Whether this option is currently selected.
 * @param onClick Called when the option is tapped.
 * @param modifier Modifier to be applied to the option.
 */
@Composable
private fun MealTagOption(
    mealType: MealType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Map domain MealType to UI MealType for color access
    val uiMealType =
        when (mealType) {
            MealType.BREAKFAST -> UiMealType.BREAKFAST
            MealType.LUNCH -> UiMealType.LUNCH
            MealType.DINNER -> UiMealType.DINNER
            MealType.SNACK -> UiMealType.SNACK
        }
    val color = uiMealType.color

    val contentDescription =
        if (isSelected) {
            "${mealType.displayName} selected, tap to remove"
        } else {
            "${mealType.displayName}, tap to select"
        }

    Surface(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = RestockSpacing.xs)
                .semantics {
                    role = Role.Checkbox
                    this.contentDescription = contentDescription
                }
                .clickable(onClick = onClick),
        shape = RoundedCornerShape(RestockCornerRadius.medium),
        color = if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent,
        border =
            BorderStroke(
                width = 1.dp,
                color = if (isSelected) color else RestockColors.Muted.copy(alpha = UNSELECTED_BORDER_ALPHA),
            ),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(RestockSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Emoji
            Text(
                text = mealType.emoji,
                style = MaterialTheme.typography.titleLarge,
            )

            Spacer(Modifier.width(RestockSpacing.sm))

            // Meal name
            Text(
                text = mealType.displayName,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) color else RestockColors.Primary,
            )

            Spacer(Modifier.weight(1f))

            // Checkmark (when selected)
            // contentDescription is null because it's described in parent semantics
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = color,
                )
            }
        }
    }
}

// ==================== PREVIEWS ====================

@LightDarkPreview
@Composable
private fun TagSelectionSheetContentPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            TagSelectionSheetContent(
                itemName = "Eggs",
                currentTags = listOf(MealType.BREAKFAST),
                onTagToggle = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TagSelectionSheetContentEmptyPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            TagSelectionSheetContent(
                itemName = "Milk",
                currentTags = emptyList(),
                onTagToggle = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TagSelectionSheetContentMultiplePreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            TagSelectionSheetContent(
                itemName = "Chicken Breast",
                currentTags = listOf(MealType.LUNCH, MealType.DINNER),
                onTagToggle = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TagSelectionSheetContentAllSelectedPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            TagSelectionSheetContent(
                itemName = "Bread",
                currentTags = MealType.entries,
                onTagToggle = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MealTagOptionUnselectedPreview() {
    RestockPreviewTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MealType.entries.forEach { mealType ->
                MealTagOption(
                    mealType = mealType,
                    isSelected = false,
                    onClick = {},
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MealTagOptionSelectedPreview() {
    RestockPreviewTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MealType.entries.forEach { mealType ->
                MealTagOption(
                    mealType = mealType,
                    isSelected = true,
                    onClick = {},
                )
            }
        }
    }
}
