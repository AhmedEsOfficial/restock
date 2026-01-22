# Task: Implement RestockButton Component

**Sprint:** 1-2 (Project Setup & Core Infrastructure)
**Owner:** UI/UX Engineer
**Estimate:** 4 hours
**Priority:** P0
**Prerequisites:** 002_theme_implementation.md

---

## Sprint Context

Core components form the building blocks of all screens. The button component is used throughout the app for primary actions, secondary actions, and text-only actions.

---

## Task Description

Implement RestockButton with three variants: Primary, Secondary, and Text.

## Implementation

### RestockButton.kt

```kotlin
package com.restock.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockPreviewTheme
import com.restock.core.ui.theme.RestockSpacing

enum class RestockButtonVariant {
    PRIMARY,
    SECONDARY,
    TEXT,
}

enum class RestockButtonSize {
    SMALL,
    MEDIUM,
    LARGE,
}

@Composable
fun RestockButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: RestockButtonVariant = RestockButtonVariant.PRIMARY,
    size: RestockButtonSize = RestockButtonSize.MEDIUM,
    enabled: Boolean = true,
    loading: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    val contentPadding = when (size) {
        RestockButtonSize.SMALL -> PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        RestockButtonSize.MEDIUM -> PaddingValues(horizontal = 16.dp, vertical = 10.dp)
        RestockButtonSize.LARGE -> PaddingValues(horizontal = 24.dp, vertical = 14.dp)
    }

    val height = when (size) {
        RestockButtonSize.SMALL -> 32.dp
        RestockButtonSize.MEDIUM -> 44.dp
        RestockButtonSize.LARGE -> 52.dp
    }

    when (variant) {
        RestockButtonVariant.PRIMARY -> {
            PrimaryButton(
                text = text,
                onClick = onClick,
                modifier = modifier.height(height),
                enabled = enabled && !loading,
                contentPadding = contentPadding,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
            )
        }
        RestockButtonVariant.SECONDARY -> {
            SecondaryButton(
                text = text,
                onClick = onClick,
                modifier = modifier.height(height),
                enabled = enabled && !loading,
                contentPadding = contentPadding,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
            )
        }
        RestockButtonVariant.TEXT -> {
            TextOnlyButton(
                text = text,
                onClick = onClick,
                modifier = modifier.height(height),
                enabled = enabled && !loading,
                contentPadding = contentPadding,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
            )
        }
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            containerColor = RestockColors.Primary,
            contentColor = RestockColors.OnPrimary,
            disabledContainerColor = RestockColors.Disabled,
            disabledContentColor = RestockColors.Muted,
        ),
        contentPadding = contentPadding,
    ) {
        ButtonContent(
            text = text,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
        )
    }
}

@Composable
private fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = RestockColors.Primary,
            disabledContentColor = RestockColors.Muted,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) RestockColors.Primary else RestockColors.Disabled,
        ),
        contentPadding = contentPadding,
    ) {
        ButtonContent(
            text = text,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
        )
    }
}

@Composable
private fun TextOnlyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.textButtonColors(
            contentColor = RestockColors.Accent,
            disabledContentColor = RestockColors.Muted,
        ),
        contentPadding = contentPadding,
    ) {
        ButtonContent(
            text = text,
            textColor = if (enabled) RestockColors.Accent else RestockColors.Muted,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
        )
    }
}

@Composable
private fun RowScope.ButtonContent(
    text: String,
    textColor: Color? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    leadingIcon?.let {
        it()
        Spacer(Modifier.width(RestockSpacing.sm))
    }

    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = textColor ?: Color.Unspecified,
    )

    trailingIcon?.let {
        Spacer(Modifier.width(RestockSpacing.sm))
        it()
    }
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun PrimaryButtonPreview() {
    RestockPreviewTheme {
        RestockButton(
            text = "Add to List",
            onClick = {},
            variant = RestockButtonVariant.PRIMARY,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SecondaryButtonPreview() {
    RestockPreviewTheme {
        RestockButton(
            text = "Cancel",
            onClick = {},
            variant = RestockButtonVariant.SECONDARY,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TextButtonPreview() {
    RestockPreviewTheme {
        RestockButton(
            text = "Undo",
            onClick = {},
            variant = RestockButtonVariant.TEXT,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DisabledButtonPreview() {
    RestockPreviewTheme {
        RestockButton(
            text = "Disabled",
            onClick = {},
            enabled = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ButtonSizesPreview() {
    RestockPreviewTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            RestockButton(
                text = "Small",
                onClick = {},
                size = RestockButtonSize.SMALL,
            )
            Spacer(Modifier.height(8.dp))
            RestockButton(
                text = "Medium",
                onClick = {},
                size = RestockButtonSize.MEDIUM,
            )
            Spacer(Modifier.height(8.dp))
            RestockButton(
                text = "Large",
                onClick = {},
                size = RestockButtonSize.LARGE,
            )
        }
    }
}
```

## Specifications

### Primary Button
- Background: Primary (#1F1F1F)
- Text: White
- Corner radius: 12dp
- Shadow: Level 1

### Secondary Button
- Background: Transparent
- Border: 1dp Primary
- Text: Primary
- Corner radius: 12dp

### Text Button
- Background: None
- Text: Accent (#F59E0B)
- No border

### Sizes
| Size | Height | H-Padding |
|------|--------|-----------|
| Small | 32dp | 12dp |
| Medium | 44dp | 16dp |
| Large | 52dp | 24dp |

## Acceptance Criteria

- [ ] Primary variant implemented
- [ ] Secondary variant implemented
- [ ] Text variant implemented
- [ ] All three sizes working
- [ ] Disabled state renders correctly
- [ ] Touch targets minimum 48dp
- [ ] Previews for all variants
- [ ] Icons support (leading/trailing)

## Definition of Done

- [ ] Component compiles without errors
- [ ] All variants match design specification
- [ ] Previews render correctly
- [ ] Accessibility labels work
- [ ] Documentation comments added

## Notes

- Use MaterialTheme.shapes for consistency
- Ensure touch targets are at least 48dp for accessibility
- Loading state can be implemented with circular progress indicator
