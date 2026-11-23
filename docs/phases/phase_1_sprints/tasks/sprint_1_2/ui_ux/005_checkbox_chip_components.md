# Task: Implement RestockCheckbox and RestockChip Components

**Sprint:** 1-2 (Project Setup & Core Infrastructure)
**Owner:** UI/UX Engineer
**Estimate:** 4 hours (D-3.4 + D-3.5 combined)
**Priority:** P0
**Prerequisites:** 002_theme_implementation.md

---

## Sprint Context

Checkboxes are the primary interaction element for checking off items. Chips are used for meal tags, providing lightweight categorization.

---

## Task Description

Implement RestockCheckbox with custom styling and animation, and RestockChip for meal tag display.

## RestockCheckbox Implementation

### RestockCheckbox.kt

```kotlin
package com.restock.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockPreviewTheme

@Composable
fun RestockCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Animate background color
    val backgroundColor by animateColorAsState(
        targetValue = when {
            !enabled -> RestockColors.Disabled
            checked -> RestockColors.Success
            else -> Color.Transparent
        },
        animationSpec = tween(durationMillis = 200),
        label = "backgroundColor"
    )

    // Animate border color
    val borderColor by animateColorAsState(
        targetValue = when {
            !enabled -> RestockColors.Disabled
            checked -> RestockColors.Success
            else -> RestockColors.Muted
        },
        animationSpec = tween(durationMillis = 200),
        label = "borderColor"
    )

    // Animate checkmark scale
    val checkmarkScale by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(durationMillis = 200),
        label = "checkmarkScale"
    )

    Box(
        modifier = modifier
            .size(48.dp) // Touch target
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = rememberRipple(bounded = true),
                enabled = enabled,
                role = Role.Checkbox,
                onClick = { onCheckedChange(!checked) }
            )
            .semantics {
                contentDescription?.let {
                    this.contentDescription = it
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        // Checkbox visual (smaller than touch target)
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(backgroundColor)
                .border(
                    width = 2.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.Center,
        ) {
            // Checkmark icon
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .size(16.dp)
                    .scale(checkmarkScale)
            )
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun CheckboxUncheckedPreview() {
    RestockPreviewTheme {
        RestockCheckbox(
            checked = false,
            onCheckedChange = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckboxCheckedPreview() {
    RestockPreviewTheme {
        RestockCheckbox(
            checked = true,
            onCheckedChange = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckboxDisabledPreview() {
    RestockPreviewTheme {
        Row(modifier = Modifier.padding(16.dp)) {
            RestockCheckbox(
                checked = false,
                onCheckedChange = {},
                enabled = false,
            )
            Spacer(Modifier.width(16.dp))
            RestockCheckbox(
                checked = true,
                onCheckedChange = {},
                enabled = false,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckboxInteractivePreview() {
    RestockPreviewTheme {
        var checked by remember { mutableStateOf(false) }
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RestockCheckbox(
                checked = checked,
                onCheckedChange = { checked = it },
            )
            Spacer(Modifier.width(8.dp))
            Text("Tap to toggle")
        }
    }
}
```

## RestockChip Implementation

### RestockChip.kt

```kotlin
package com.restock.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockCornerRadius
import com.restock.core.ui.theme.RestockPreviewTheme
import com.restock.core.ui.theme.RestockSpacing

enum class MealType(val displayName: String, val emoji: String) {
    BREAKFAST("Breakfast", "🌅"),
    LUNCH("Lunch", "☀️"),
    DINNER("Dinner", "🌙"),
    SNACK("Snack", "✨");

    val color: Color
        get() = when (this) {
            BREAKFAST -> RestockColors.Breakfast
            LUNCH -> RestockColors.Lunch
            DINNER -> RestockColors.Dinner
            SNACK -> RestockColors.Snack
        }
}

@Composable
fun MealTagChip(
    mealType: MealType,
    modifier: Modifier = Modifier,
    onRemove: (() -> Unit)? = null,
    showEmoji: Boolean = true,
) {
    val chipColor = mealType.color

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(RestockCornerRadius.sm),
        color = chipColor.copy(alpha = 0.15f),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = RestockSpacing.sm,
                vertical = RestockSpacing.xs,
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
                    modifier = Modifier
                        .size(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .clickable { onRemoveClick() }
                )
            }
        }
    }
}

// Generic chip for other uses
@Composable
fun RestockChip(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = RestockColors.Accent,
    onClick: (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.then(
            if (onClick != null) {
                Modifier.clickable { onClick() }
            } else {
                Modifier
            }
        ),
        shape = RoundedCornerShape(RestockCornerRadius.sm),
        color = color.copy(alpha = 0.15f),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = RestockSpacing.sm,
                vertical = RestockSpacing.xs,
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leadingIcon?.invoke()

            if (leadingIcon != null) {
                Spacer(Modifier.width(4.dp))
            }

            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = color,
            )

            if (trailingIcon != null) {
                Spacer(Modifier.width(4.dp))
            }

            trailingIcon?.invoke()
        }
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun MealTagChipPreview() {
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
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MealTagChipNoEmojiPreview() {
    RestockPreviewTheme {
        MealTagChip(
            mealType = MealType.DINNER,
            showEmoji = false,
            modifier = Modifier.padding(16.dp),
        )
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
```

## Specifications

### RestockCheckbox
- Touch target: 48dp × 48dp
- Visual size: 24dp × 24dp
- Corner radius: 6dp
- Border: 2dp
- Unchecked: Transparent bg, Muted border
- Checked: Success bg, Success border, White checkmark
- Animation: 200ms

### MealTagChip
- Corner radius: 8dp
- Background: Meal color @ 15% opacity
- Text color: Meal color
- Padding: 8dp horizontal, 4dp vertical
- Font: Label Small (11sp, Medium)
- Remove icon: 14dp

## Acceptance Criteria

### RestockCheckbox
- [ ] Unchecked state renders correctly
- [ ] Checked state with checkmark
- [ ] Smooth animation between states
- [ ] Disabled state for both checked/unchecked
- [ ] Touch target is 48dp
- [ ] Accessibility role is Checkbox
- [ ] Previews for all states

### MealTagChip
- [ ] All four meal types with correct colors
- [ ] Emoji + text display
- [ ] Optional remove button
- [ ] Color at 15% opacity background
- [ ] Text in full color
- [ ] Previews for variations

## Definition of Done

- [ ] Both components compile without errors
- [ ] Animations are smooth (60fps)
- [ ] Accessibility support included
- [ ] All variants match design specification
- [ ] Previews render correctly
- [ ] Documentation comments added

## Notes

- Checkbox animation makes checking feel satisfying
- Use scale animation for checkmark appearance
- Chips should be compact but readable
- Test color contrast for accessibility
