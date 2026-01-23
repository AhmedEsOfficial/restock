package com.restock.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockShapeTokens
import com.restock.core.ui.theme.RestockTheme

/**
 * Standard text field for input.
 *
 * Uses 16dp corner radius per design spec.
 *
 * @param value The current text value.
 * @param onValueChange Called when the text value changes.
 * @param modifier Modifier to be applied to the text field.
 * @param placeholder Placeholder text shown when empty.
 * @param label Optional label text.
 * @param leadingIcon Optional leading icon.
 * @param trailingIcon Optional trailing icon.
 * @param isError Whether the field is in error state.
 * @param errorMessage Error message to display.
 * @param enabled Whether the field is enabled.
 * @param singleLine Whether the field is single line.
 * @param keyboardOptions Keyboard options.
 * @param keyboardActions Keyboard actions.
 */
@Composable
fun RestockTextField(
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
    keyboardOptions: KeyboardOptions =
        KeyboardOptions.Default.copy(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done,
        ),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        placeholder =
            placeholder?.let {
                { Text(text = it, color = RestockColors.Muted) }
            },
        label =
            label?.let {
                { Text(text = it) }
            },
        leadingIcon = leadingIcon,
        trailingIcon =
            trailingIcon ?: if (value.isNotEmpty()) {
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
        supportingText =
            if (isError && errorMessage != null) {
                { Text(text = errorMessage) }
            } else {
                null
            },
        enabled = enabled,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(RestockShapeTokens.InputFieldRadius),
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = RestockColors.Primary,
                unfocusedBorderColor = RestockColors.MutedVariant,
                focusedContainerColor = RestockColors.Surface,
                unfocusedContainerColor = RestockColors.Surface,
                cursorColor = RestockColors.Primary,
                errorBorderColor = RestockColors.Error,
            ),
    )
}

// Previews
@Preview(showBackground = true)
@Composable
private fun RestockTextFieldEmptyPreview() {
    RestockTheme {
        RestockTextField(
            value = "",
            onValueChange = {},
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            placeholder = "Add an item...",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RestockTextFieldFilledPreview() {
    RestockTheme {
        RestockTextField(
            value = "Milk",
            onValueChange = {},
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            placeholder = "Add an item...",
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RestockTextFieldErrorPreview() {
    RestockTheme {
        RestockTextField(
            value = "",
            onValueChange = {},
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
            placeholder = "Add an item...",
            isError = true,
            errorMessage = "Please enter an item name",
        )
    }
}
