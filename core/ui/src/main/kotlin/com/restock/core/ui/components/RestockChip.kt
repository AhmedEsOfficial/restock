package com.restock.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockShapeTokens
import com.restock.core.ui.theme.RestockTheme

/**
 * Meal type enum for type-safe chip colors.
 */
enum class MealType(
    val displayName: String,
    val color: Color,
    val containerColor: Color,
) {
    BREAKFAST("Breakfast", RestockColors.Breakfast, RestockColors.BreakfastContainer),
    LUNCH("Lunch", RestockColors.Lunch, RestockColors.LunchContainer),
    DINNER("Dinner", RestockColors.Dinner, RestockColors.DinnerContainer),
    SNACK("Snack", RestockColors.Snack, RestockColors.SnackContainer),
}

/**
 * Meal tag chip component.
 *
 * Used to display and select meal categories on grocery items.
 *
 * @param mealType The type of meal this chip represents.
 * @param modifier Modifier to be applied to the chip.
 * @param selected Whether the chip is currently selected.
 * @param onClick Optional click handler.
 */
@Composable
fun RestockMealChip(
    mealType: MealType,
    modifier: Modifier = Modifier,
    selected: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    val backgroundColor = if (selected) mealType.containerColor else Color.Transparent
    val textColor = if (selected) mealType.color else RestockColors.Muted

    val clickableModifier =
        if (onClick != null) {
            Modifier.clickable(role = Role.Button, onClick = onClick)
        } else {
            Modifier
        }

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(RestockShapeTokens.ChipRadius))
                .background(backgroundColor)
                .then(clickableModifier)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = mealType.displayName,
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
        )
    }
}

/**
 * Generic chip component.
 *
 * @param text The text to display.
 * @param modifier Modifier to be applied to the chip.
 * @param containerColor Background color of the chip.
 * @param contentColor Text color.
 * @param onClick Optional click handler.
 */
@Composable
fun RestockChip(
    text: String,
    modifier: Modifier = Modifier,
    containerColor: Color = RestockColors.Accent.copy(alpha = 0.1f),
    contentColor: Color = RestockColors.Accent,
    onClick: (() -> Unit)? = null,
) {
    val clickableModifier =
        if (onClick != null) {
            Modifier.clickable(role = Role.Button, onClick = onClick)
        } else {
            Modifier
        }

    Box(
        modifier =
            modifier
                .clip(RoundedCornerShape(RestockShapeTokens.ChipRadius))
                .background(containerColor)
                .then(clickableModifier)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
        )
    }
}

// Previews
@Preview(showBackground = true)
@Composable
private fun RestockMealChipBreakfastPreview() {
    RestockTheme {
        RestockMealChip(mealType = MealType.BREAKFAST)
    }
}

@Preview(showBackground = true)
@Composable
private fun RestockMealChipLunchPreview() {
    RestockTheme {
        RestockMealChip(mealType = MealType.LUNCH)
    }
}

@Preview(showBackground = true)
@Composable
private fun RestockMealChipDinnerPreview() {
    RestockTheme {
        RestockMealChip(mealType = MealType.DINNER)
    }
}

@Preview(showBackground = true)
@Composable
private fun RestockMealChipSnackPreview() {
    RestockTheme {
        RestockMealChip(mealType = MealType.SNACK)
    }
}

@Preview(showBackground = true)
@Composable
private fun RestockChipPreview() {
    RestockTheme {
        RestockChip(text = "Usually every 7 days")
    }
}
