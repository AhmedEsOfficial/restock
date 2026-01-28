package com.restock.feature.meals.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.domain.model.MealType
import com.restock.core.ui.preview.LightDarkPreview
import com.restock.core.ui.preview.RestockPreviewTheme
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockSpacing
import com.restock.core.ui.components.MealType as UiMealType

/**
 * Section header for meal groups in the Meals screen.
 *
 * Displays the meal type emoji, name, and item count.
 * Uses the meal's signature color for visual distinction.
 *
 * **Design Specifications:**
 * - Emoji + meal name with count in parentheses
 * - Text color matches the meal's assigned color
 * - Horizontal padding: md, vertical padding: sm
 * - Typography: titleMedium
 *
 * @param mealType The meal type this header represents.
 * @param count Number of items in this meal category.
 * @param modifier Modifier to be applied to the header.
 */
@Composable
fun MealSectionHeader(
    mealType: MealType,
    count: Int,
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

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = RestockSpacing.md,
                    vertical = RestockSpacing.sm,
                )
                .semantics { heading() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Emoji
        Text(
            text = mealType.emoji,
            style = MaterialTheme.typography.titleMedium,
        )

        Spacer(Modifier.width(RestockSpacing.sm))

        // Meal name with count
        Text(
            text = "${mealType.displayName} ($count)",
            style = MaterialTheme.typography.titleMedium,
            color = color,
        )
    }
}

/**
 * Section header for untagged items in the Meals screen.
 *
 * Displays "Untagged" with a count in parentheses.
 * Uses muted color to indicate these items lack categorization.
 *
 * @param count Number of untagged items.
 * @param modifier Modifier to be applied to the header.
 */
@Composable
fun UntaggedSectionHeader(
    count: Int,
    modifier: Modifier = Modifier,
) {
    Text(
        text = "Untagged ($count)",
        style = MaterialTheme.typography.titleMedium,
        color = RestockColors.Muted,
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    horizontal = RestockSpacing.md,
                    vertical = RestockSpacing.sm,
                )
                .semantics { heading() },
    )
}

// ==================== PREVIEWS ====================

@LightDarkPreview
@Composable
private fun MealSectionHeaderBreakfastPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            MealSectionHeader(
                mealType = MealType.BREAKFAST,
                count = 3,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun MealSectionHeaderLunchPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            MealSectionHeader(
                mealType = MealType.LUNCH,
                count = 2,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun MealSectionHeaderDinnerPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            MealSectionHeader(
                mealType = MealType.DINNER,
                count = 5,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun MealSectionHeaderSnackPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            MealSectionHeader(
                mealType = MealType.SNACK,
                count = 1,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AllMealSectionHeadersPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                MealSectionHeader(MealType.BREAKFAST, 3)
                MealSectionHeader(MealType.LUNCH, 2)
                MealSectionHeader(MealType.DINNER, 5)
                MealSectionHeader(MealType.SNACK, 1)
                UntaggedSectionHeader(4)
            }
        }
    }
}

@LightDarkPreview
@Composable
private fun UntaggedSectionHeaderPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            UntaggedSectionHeader(
                count = 4,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MealSectionHeaderZeroCountPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            MealSectionHeader(
                mealType = MealType.DINNER,
                count = 0,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MealSectionHeaderLargeCountPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            MealSectionHeader(
                mealType = MealType.BREAKFAST,
                count = 42,
                modifier = Modifier.padding(8.dp),
            )
        }
    }
}
