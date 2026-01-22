# Task: Implement RestockCard and RestockTextField Components

**Sprint:** 1-2 (Project Setup & Core Infrastructure)
**Owner:** UI/UX Engineer
**Estimate:** 7 hours (D-3.2 + D-3.3 combined)
**Priority:** P0
**Prerequisites:** 002_theme_implementation.md

---

## Sprint Context

Cards and text fields are fundamental components used throughout Restock. Cards display list items and suggestions, while the text field is the primary input for adding items.

---

## Task Description

Implement RestockCard and RestockTextField components with proper styling and accessibility.

## RestockCard Implementation

### RestockCard.kt

```kotlin
package com.restock.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockCornerRadius
import com.restock.core.ui.theme.RestockElevation
import com.restock.core.ui.theme.RestockPreviewTheme
import com.restock.core.ui.theme.RestockSpacing

enum class RestockCardVariant {
    DEFAULT,      // List items
    ELEVATED,     // Suggestions
    OUTLINED,     // Selected states
}

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

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = CardDefaults.cardColors(
                containerColor = containerColor,
                contentColor = contentColor,
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
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
            elevation = CardDefaults.cardElevation(defaultElevation = elevation),
            border = border,
            content = content,
        )
    }
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
```

## RestockTextField Implementation

### RestockTextField.kt

```kotlin
package com.restock.core.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockCornerRadius
import com.restock.core.ui.theme.RestockElevation
import com.restock.core.ui.theme.RestockPreviewTheme
import com.restock.core.ui.theme.RestockSpacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestockTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    isError: Boolean = false,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions(
        capitalization = KeyboardCapitalization.Sentences,
        imeAction = ImeAction.Done,
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    val interactionSource = remember { MutableInteractionSource() }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = MaterialTheme.shapes.large,
        color = RestockColors.Card,
        shadowElevation = RestockElevation.level2,
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = readOnly,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = RestockColors.Primary,
            ),
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            singleLine = singleLine,
            maxLines = maxLines,
            visualTransformation = visualTransformation,
            interactionSource = interactionSource,
            cursorBrush = SolidColor(RestockColors.Accent),
            decorationBox = { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    value = value,
                    innerTextField = innerTextField,
                    enabled = enabled,
                    singleLine = singleLine,
                    visualTransformation = visualTransformation,
                    interactionSource = interactionSource,
                    isError = isError,
                    placeholder = {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyMedium,
                            color = RestockColors.Muted,
                        )
                    },
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent,
                        cursorColor = RestockColors.Accent,
                    ),
                    contentPadding = TextFieldDefaults.contentPaddingWithoutLabel(
                        start = RestockSpacing.md,
                        end = RestockSpacing.md,
                        top = 14.dp,
                        bottom = 14.dp,
                    ),
                )
            }
        )
    }
}

// Specialized text field for adding items
@Composable
fun AddItemTextField(
    value: String,
    onValueChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Add an item...",
) {
    RestockTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Text,
        ),
        keyboardActions = KeyboardActions(
            onDone = { onSubmit() }
        ),
        singleLine = true,
    )
}

// ==================== PREVIEWS ====================

@Preview(showBackground = true)
@Composable
private fun TextFieldEmptyPreview() {
    RestockPreviewTheme {
        var text by remember { mutableStateOf("") }
        RestockTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = "Add an item...",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TextFieldWithTextPreview() {
    RestockPreviewTheme {
        var text by remember { mutableStateOf("Milk") }
        RestockTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = "Add an item...",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddItemTextFieldPreview() {
    RestockPreviewTheme {
        var text by remember { mutableStateOf("") }
        AddItemTextField(
            value = text,
            onValueChange = { text = it },
            onSubmit = { /* Add item */ },
            modifier = Modifier.padding(16.dp),
        )
    }
}
```

## Specifications

### RestockCard
| Variant | Elevation | Border |
|---------|-----------|--------|
| Default | 1dp | None |
| Elevated | 2dp | None |
| Outlined | 0dp | 1dp Divider |

### RestockTextField
- Height: 52dp
- Corner radius: 14dp (large)
- Background: White (#FFFFFF)
- Shadow: Level 2
- Padding: 16dp horizontal
- Placeholder color: Muted (#8B8680)
- Cursor color: Accent (#F59E0B)

## Acceptance Criteria

### RestockCard
- [ ] Default variant with subtle shadow
- [ ] Elevated variant with more prominent shadow
- [ ] Outlined variant with border
- [ ] Clickable option with ripple effect
- [ ] Previews for all variants

### RestockTextField
- [ ] Placeholder text displays correctly
- [ ] Input text styled correctly
- [ ] Cursor color is Accent
- [ ] Shadow renders correctly
- [ ] Leading/trailing icons support
- [ ] Keyboard actions work
- [ ] AddItemTextField convenience wrapper

## Definition of Done

- [ ] Both components compile without errors
- [ ] All variants match design specification
- [ ] Previews render correctly
- [ ] Touch targets adequate for accessibility
- [ ] Documentation comments added

## Notes

- Cards use elevation from RestockElevation
- TextField uses Surface for shadow effect
- Keep consistent with MaterialTheme shapes
