package com.restock.core.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.ui.preview.LightDarkPreview
import com.restock.core.ui.preview.RestockPreviewTheme
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockElevation
import com.restock.core.ui.theme.RestockShapeTokens
import com.restock.core.ui.theme.RestockSpacing

/**
 * Primary text input field for the Restock design system.
 *
 * Uses a Surface with shadow for a floating card appearance, matching the
 * design specification. The field has a fixed height of 52dp and uses
 * the Accent color for the cursor.
 *
 * ## Design Specifications
 * - Height: 52dp
 * - Corner radius: 16dp (large)
 * - Background: White (#FFFFFF)
 * - Shadow: Level 2 (4dp)
 * - Padding: 16dp horizontal
 * - Placeholder color: Muted (#8B8680)
 * - Cursor color: Accent (#F59E0B)
 *
 * ## Usage
 * ```kotlin
 * var text by remember { mutableStateOf("") }
 *
 * RestockTextField(
 *     value = text,
 *     onValueChange = { text = it },
 *     placeholder = "Add an item...",
 * )
 * ```
 *
 * @param value The current text value.
 * @param onValueChange Called when the text value changes.
 * @param modifier Modifier to be applied to the text field.
 * @param placeholder Placeholder text shown when empty.
 * @param enabled Whether the field is enabled.
 * @param readOnly Whether the field is read-only.
 * @param singleLine Whether the field is single line.
 * @param maxLines Maximum number of lines when not single line.
 * @param isError Whether the field is in error state.
 * @param leadingIcon Optional leading icon composable.
 * @param trailingIcon Optional trailing icon composable.
 * @param keyboardOptions Keyboard options for the text field.
 * @param keyboardActions Keyboard actions for the text field.
 * @param visualTransformation Visual transformation to apply (e.g., password masking).
 */
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
            .height(RestockSpacing.inputFieldHeight),
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
            },
        )
    }
}

/**
 * Specialized text field for adding items to the grocery list.
 *
 * This is a convenience wrapper around [RestockTextField] configured
 * specifically for the add item flow with appropriate keyboard settings.
 *
 * ## Features
 * - Sentence capitalization
 * - "Done" IME action that triggers onSubmit
 * - Text keyboard type
 * - Single line input
 *
 * ## Usage
 * ```kotlin
 * var text by remember { mutableStateOf("") }
 *
 * AddItemTextField(
 *     value = text,
 *     onValueChange = { text = it },
 *     onSubmit = {
 *         addItem(text)
 *         text = ""
 *     },
 * )
 * ```
 *
 * @param value The current text value.
 * @param onValueChange Called when the text value changes.
 * @param onSubmit Called when the user presses the "Done" key on the keyboard.
 * @param modifier Modifier to be applied to the text field.
 * @param placeholder Placeholder text shown when empty.
 *
 * @see RestockTextField
 */
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
            onDone = { onSubmit() },
        ),
        singleLine = true,
    )
}

/**
 * Outlined text field variant for forms and dialogs.
 *
 * Use this when you need a more traditional bordered text field style,
 * such as in settings or dialogs. This field supports labels, error states,
 * and auto-clear functionality.
 *
 * ## Usage
 * ```kotlin
 * var text by remember { mutableStateOf("") }
 *
 * RestockOutlinedTextField(
 *     value = text,
 *     onValueChange = { text = it },
 *     label = "Item name",
 *     placeholder = "Enter item name",
 * )
 * ```
 *
 * @param value The current text value.
 * @param onValueChange Called when the text value changes.
 * @param modifier Modifier to be applied to the text field.
 * @param placeholder Placeholder text shown when empty.
 * @param label Optional label text that floats when focused.
 * @param leadingIcon Optional leading icon.
 * @param trailingIcon Optional trailing icon. If null and value is not empty, shows clear button.
 * @param isError Whether the field is in error state.
 * @param errorMessage Error message to display below the field.
 * @param enabled Whether the field is enabled.
 * @param singleLine Whether the field is single line.
 * @param keyboardOptions Keyboard options.
 * @param keyboardActions Keyboard actions.
 */
@Composable
fun RestockOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    label: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default.copy(
        capitalization = KeyboardCapitalization.Sentences,
        imeAction = ImeAction.Done,
    ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder = placeholder?.let {
            { Text(text = it, color = RestockColors.Muted) }
        },
        label = label?.let {
            { Text(text = it) }
        },
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon ?: if (value.isNotEmpty()) {
            {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = RestockColors.Muted,
                    )
                }
            }
        } else {
            null
        },
        isError = isError,
        supportingText = if (isError && errorMessage != null) {
            { Text(text = errorMessage) }
        } else {
            null
        },
        enabled = enabled,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(RestockShapeTokens.InputFieldRadius),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = RestockColors.Primary,
            unfocusedBorderColor = RestockColors.MutedVariant,
            focusedContainerColor = RestockColors.Surface,
            unfocusedContainerColor = RestockColors.Surface,
            cursorColor = RestockColors.Accent,
            errorBorderColor = RestockColors.Error,
        ),
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

@Preview(showBackground = true)
@Composable
private fun AddItemTextFieldWithTextPreview() {
    RestockPreviewTheme {
        var text by remember { mutableStateOf("Eggs") }
        AddItemTextField(
            value = text,
            onValueChange = { text = it },
            onSubmit = { /* Add item */ },
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TextFieldWithLeadingIconPreview() {
    RestockPreviewTheme {
        var text by remember { mutableStateOf("") }
        RestockTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = "Search items...",
            modifier = Modifier.padding(16.dp),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = RestockColors.Muted,
                )
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TextFieldWithTrailingIconPreview() {
    RestockPreviewTheme {
        var text by remember { mutableStateOf("Bread") }
        RestockTextField(
            value = text,
            onValueChange = { text = it },
            placeholder = "Add an item...",
            modifier = Modifier.padding(16.dp),
            trailingIcon = {
                IconButton(onClick = { text = "" }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = RestockColors.Muted,
                    )
                }
            },
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TextFieldDisabledPreview() {
    RestockPreviewTheme {
        RestockTextField(
            value = "Disabled field",
            onValueChange = { },
            placeholder = "Add an item...",
            modifier = Modifier.padding(16.dp),
            enabled = false,
        )
    }
}

@LightDarkPreview
@Composable
private fun TextFieldLightDarkPreview() {
    RestockPreviewTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
        ) {
            var text1 by remember { mutableStateOf("") }
            RestockTextField(
                value = text1,
                onValueChange = { text1 = it },
                placeholder = "Empty text field...",
            )

            var text2 by remember { mutableStateOf("Grocery item") }
            RestockTextField(
                value = text2,
                onValueChange = { text2 = it },
                placeholder = "Add an item...",
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OutlinedTextFieldEmptyPreview() {
    RestockPreviewTheme {
        RestockOutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = "Add an item...",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OutlinedTextFieldFilledPreview() {
    RestockPreviewTheme {
        RestockOutlinedTextField(
            value = "Milk",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = "Add an item...",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OutlinedTextFieldWithLabelPreview() {
    RestockPreviewTheme {
        RestockOutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            label = "Item name",
            placeholder = "Enter item name",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OutlinedTextFieldErrorPreview() {
    RestockPreviewTheme {
        RestockOutlinedTextField(
            value = "",
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = "Add an item...",
            isError = true,
            errorMessage = "Please enter an item name",
        )
    }
}
