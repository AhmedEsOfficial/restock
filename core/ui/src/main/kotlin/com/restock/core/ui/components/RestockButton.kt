package com.restock.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.restock.core.ui.preview.LightDarkPreview
import com.restock.core.ui.preview.LightPreview
import com.restock.core.ui.preview.RestockPreviewTheme
import com.restock.core.ui.theme.RestockButtonShape
import com.restock.core.ui.theme.RestockColors

/**
 * Button variant types for RestockButton.
 *
 * - [PRIMARY]: Solid background button for main actions
 * - [SECONDARY]: Outlined button for secondary actions
 * - [TEXT]: Text-only button for tertiary actions
 */
enum class RestockButtonVariant {
    PRIMARY,
    SECONDARY,
    TEXT,
}

/**
 * Button size options for RestockButton.
 *
 * | Size   | Height | Horizontal Padding |
 * |--------|--------|--------------------|
 * | SMALL  | 32dp   | 12dp               |
 * | MEDIUM | 44dp   | 16dp               |
 * | LARGE  | 52dp   | 24dp               |
 */
enum class RestockButtonSize {
    SMALL,
    MEDIUM,
    LARGE,
}

/**
 * Unified button component for the Restock app.
 *
 * Supports three variants (Primary, Secondary, Text), three sizes (Small, Medium, Large),
 * loading states, and optional leading/trailing icons.
 *
 * @param text The button text label.
 * @param onClick Called when the button is clicked.
 * @param modifier Modifier to be applied to the button.
 * @param variant The button style variant. Defaults to [RestockButtonVariant.PRIMARY].
 * @param size The button size. Defaults to [RestockButtonSize.MEDIUM].
 * @param enabled Whether the button is enabled. Defaults to true.
 * @param loading Whether to show a loading indicator. When true, the button is disabled.
 * @param leadingIcon Optional composable for an icon before the text.
 * @param trailingIcon Optional composable for an icon after the text.
 *
 * Usage:
 * ```kotlin
 * // Primary button
 * RestockButton(
 *     text = "Add to List",
 *     onClick = { },
 * )
 *
 * // Secondary button with loading state
 * RestockButton(
 *     text = "Cancel",
 *     onClick = { },
 *     variant = RestockButtonVariant.SECONDARY,
 *     loading = isLoading,
 * )
 *
 * // Button with icons
 * RestockButton(
 *     text = "Delete",
 *     onClick = { },
 *     leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null) },
 * )
 * ```
 */
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
    val contentPadding = getContentPadding(size)
    val height = getHeight(size)
    val isEnabled = enabled && !loading

    val accessibilityModifier = modifier.semantics {
        role = Role.Button
        if (loading) {
            stateDescription = "Loading"
        }
    }

    when (variant) {
        RestockButtonVariant.PRIMARY -> {
            PrimaryButton(
                text = text,
                onClick = onClick,
                modifier = accessibilityModifier.height(height),
                enabled = isEnabled,
                loading = loading,
                contentPadding = contentPadding,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
            )
        }
        RestockButtonVariant.SECONDARY -> {
            SecondaryButton(
                text = text,
                onClick = onClick,
                modifier = accessibilityModifier.height(height),
                enabled = isEnabled,
                loading = loading,
                contentPadding = contentPadding,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
            )
        }
        RestockButtonVariant.TEXT -> {
            TextOnlyButton(
                text = text,
                onClick = onClick,
                modifier = accessibilityModifier.height(height),
                enabled = isEnabled,
                loading = loading,
                contentPadding = contentPadding,
                leadingIcon = leadingIcon,
                trailingIcon = trailingIcon,
            )
        }
    }
}

/**
 * Primary button for main actions.
 *
 * Solid background with primary color. Use for the most important action on a screen.
 *
 * @param onClick Called when the button is clicked.
 * @param modifier Modifier to be applied to the button.
 * @param enabled Whether the button is enabled.
 * @param content The content of the button.
 */
@Composable
fun RestockButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .defaultMinSize(minWidth = 48.dp),
        enabled = enabled,
        shape = RestockButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = RestockColors.Primary,
            contentColor = RestockColors.OnPrimary,
            disabledContainerColor = RestockColors.Disabled,
            disabledContentColor = RestockColors.Muted,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        content = content,
    )
}

/**
 * Primary button with text content.
 *
 * @param text The text to display in the button.
 * @param onClick Called when the button is clicked.
 * @param modifier Modifier to be applied to the button.
 * @param enabled Whether the button is enabled.
 */
@Composable
fun RestockButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    RestockButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

/**
 * Secondary outlined button.
 *
 * Transparent background with primary colored border. Use for secondary actions.
 *
 * @param onClick Called when the button is clicked.
 * @param modifier Modifier to be applied to the button.
 * @param enabled Whether the button is enabled.
 * @param content The content of the button.
 */
@Composable
fun RestockOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .defaultMinSize(minWidth = 48.dp),
        enabled = enabled,
        shape = RestockButtonShape,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = RestockColors.Primary,
            disabledContentColor = RestockColors.Muted,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) RestockColors.Primary else RestockColors.Disabled,
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        content = content,
    )
}

/**
 * Secondary outlined button with text content.
 *
 * @param text The text to display in the button.
 * @param onClick Called when the button is clicked.
 * @param modifier Modifier to be applied to the button.
 * @param enabled Whether the button is enabled.
 */
@Composable
fun RestockOutlinedButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    RestockOutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

/**
 * Text button for tertiary actions.
 *
 * No background or border, accent colored text. Use for less prominent actions.
 *
 * @param onClick Called when the button is clicked.
 * @param modifier Modifier to be applied to the button.
 * @param enabled Whether the button is enabled.
 * @param content The content of the button.
 */
@Composable
fun RestockTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .height(48.dp)
            .defaultMinSize(minWidth = 48.dp),
        enabled = enabled,
        shape = RestockButtonShape,
        colors = ButtonDefaults.textButtonColors(
            contentColor = RestockColors.Accent,
            disabledContentColor = RestockColors.Muted,
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        content = content,
    )
}

/**
 * Text button with text content.
 *
 * @param text The text to display in the button.
 * @param onClick Called when the button is clicked.
 * @param modifier Modifier to be applied to the button.
 * @param enabled Whether the button is enabled.
 */
@Composable
fun RestockTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    RestockTextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}

// ==================== INTERNAL COMPONENTS ====================

@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minWidth = 48.dp),
        enabled = enabled,
        shape = RestockButtonShape,
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
            loading = loading,
            loadingIndicatorColor = RestockColors.OnPrimary,
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
    loading: Boolean = false,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minWidth = 48.dp),
        enabled = enabled,
        shape = RestockButtonShape,
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
            loading = loading,
            loadingIndicatorColor = RestockColors.Primary,
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
    loading: Boolean = false,
    contentPadding: PaddingValues = ButtonDefaults.TextButtonContentPadding,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.defaultMinSize(minWidth = 48.dp),
        enabled = enabled,
        shape = RestockButtonShape,
        colors = ButtonDefaults.textButtonColors(
            contentColor = RestockColors.Accent,
            disabledContentColor = RestockColors.Muted,
        ),
        contentPadding = contentPadding,
    ) {
        ButtonContent(
            text = text,
            textColor = if (enabled) RestockColors.Accent else RestockColors.Muted,
            loading = loading,
            loadingIndicatorColor = RestockColors.Accent,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
        )
    }
}

@Composable
private fun ButtonContent(
    text: String,
    loading: Boolean,
    loadingIndicatorColor: Color,
    textColor: Color? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
) {
    Box(
        contentAlignment = Alignment.Center,
    ) {
        // Loading indicator (shown when loading)
        AnimatedVisibility(
            visible = loading,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = loadingIndicatorColor,
                strokeWidth = 2.dp,
            )
        }

        // Content row (hidden when loading)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.then(
                if (loading) {
                    Modifier.padding(0.dp) // Keep layout but invisible
                } else {
                    Modifier
                }
            ),
        ) {
            AnimatedVisibility(
                visible = !loading,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    leadingIcon?.let {
                        it()
                        Spacer(Modifier.width(8.dp))
                    }

                    Text(
                        text = text,
                        style = MaterialTheme.typography.labelLarge,
                        color = textColor ?: Color.Unspecified,
                    )

                    trailingIcon?.let {
                        Spacer(Modifier.width(8.dp))
                        it()
                    }
                }
            }
        }
    }
}

// ==================== HELPER FUNCTIONS ====================

private fun getContentPadding(size: RestockButtonSize): PaddingValues {
    return when (size) {
        RestockButtonSize.SMALL -> PaddingValues(horizontal = 12.dp, vertical = 6.dp)
        RestockButtonSize.MEDIUM -> PaddingValues(horizontal = 16.dp, vertical = 10.dp)
        RestockButtonSize.LARGE -> PaddingValues(horizontal = 24.dp, vertical = 14.dp)
    }
}

private fun getHeight(size: RestockButtonSize): Dp {
    return when (size) {
        RestockButtonSize.SMALL -> 32.dp
        RestockButtonSize.MEDIUM -> 44.dp
        RestockButtonSize.LARGE -> 52.dp
    }
}

// ==================== PREVIEWS ====================

@LightPreview
@Composable
private fun PrimaryButtonPreview() {
    RestockPreviewTheme {
        RestockButton(
            text = "Add to List",
            onClick = {},
            variant = RestockButtonVariant.PRIMARY,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@LightPreview
@Composable
private fun SecondaryButtonPreview() {
    RestockPreviewTheme {
        RestockButton(
            text = "Cancel",
            onClick = {},
            variant = RestockButtonVariant.SECONDARY,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@LightPreview
@Composable
private fun TextButtonPreview() {
    RestockPreviewTheme {
        RestockButton(
            text = "Undo",
            onClick = {},
            variant = RestockButtonVariant.TEXT,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@LightPreview
@Composable
private fun DisabledButtonsPreview() {
    RestockPreviewTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            RestockButton(
                text = "Disabled Primary",
                onClick = {},
                variant = RestockButtonVariant.PRIMARY,
                enabled = false,
            )
            Spacer(Modifier.height(8.dp))
            RestockButton(
                text = "Disabled Secondary",
                onClick = {},
                variant = RestockButtonVariant.SECONDARY,
                enabled = false,
            )
            Spacer(Modifier.height(8.dp))
            RestockButton(
                text = "Disabled Text",
                onClick = {},
                variant = RestockButtonVariant.TEXT,
                enabled = false,
            )
        }
    }
}

@LightPreview
@Composable
private fun LoadingButtonsPreview() {
    RestockPreviewTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            RestockButton(
                text = "Loading Primary",
                onClick = {},
                variant = RestockButtonVariant.PRIMARY,
                loading = true,
            )
            Spacer(Modifier.height(8.dp))
            RestockButton(
                text = "Loading Secondary",
                onClick = {},
                variant = RestockButtonVariant.SECONDARY,
                loading = true,
            )
            Spacer(Modifier.height(8.dp))
            RestockButton(
                text = "Loading Text",
                onClick = {},
                variant = RestockButtonVariant.TEXT,
                loading = true,
            )
        }
    }
}

@LightPreview
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

@LightDarkPreview
@Composable
private fun ButtonVariantsPreview() {
    RestockPreviewTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            RestockButton(
                text = "Primary Button",
                onClick = {},
                variant = RestockButtonVariant.PRIMARY,
            )
            Spacer(Modifier.height(8.dp))
            RestockButton(
                text = "Secondary Button",
                onClick = {},
                variant = RestockButtonVariant.SECONDARY,
            )
            Spacer(Modifier.height(8.dp))
            RestockButton(
                text = "Text Button",
                onClick = {},
                variant = RestockButtonVariant.TEXT,
            )
        }
    }
}

@LightPreview
@Composable
private fun AllSizesAllVariantsPreview() {
    RestockPreviewTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            // Small
            Row {
                RestockButton(
                    text = "Small",
                    onClick = {},
                    size = RestockButtonSize.SMALL,
                    variant = RestockButtonVariant.PRIMARY,
                )
                Spacer(Modifier.width(8.dp))
                RestockButton(
                    text = "Small",
                    onClick = {},
                    size = RestockButtonSize.SMALL,
                    variant = RestockButtonVariant.SECONDARY,
                )
                Spacer(Modifier.width(8.dp))
                RestockButton(
                    text = "Small",
                    onClick = {},
                    size = RestockButtonSize.SMALL,
                    variant = RestockButtonVariant.TEXT,
                )
            }
            Spacer(Modifier.height(8.dp))
            // Medium
            Row {
                RestockButton(
                    text = "Medium",
                    onClick = {},
                    size = RestockButtonSize.MEDIUM,
                    variant = RestockButtonVariant.PRIMARY,
                )
                Spacer(Modifier.width(8.dp))
                RestockButton(
                    text = "Medium",
                    onClick = {},
                    size = RestockButtonSize.MEDIUM,
                    variant = RestockButtonVariant.SECONDARY,
                )
                Spacer(Modifier.width(8.dp))
                RestockButton(
                    text = "Medium",
                    onClick = {},
                    size = RestockButtonSize.MEDIUM,
                    variant = RestockButtonVariant.TEXT,
                )
            }
            Spacer(Modifier.height(8.dp))
            // Large
            Row {
                RestockButton(
                    text = "Large",
                    onClick = {},
                    size = RestockButtonSize.LARGE,
                    variant = RestockButtonVariant.PRIMARY,
                )
                Spacer(Modifier.width(8.dp))
                RestockButton(
                    text = "Large",
                    onClick = {},
                    size = RestockButtonSize.LARGE,
                    variant = RestockButtonVariant.SECONDARY,
                )
                Spacer(Modifier.width(8.dp))
                RestockButton(
                    text = "Large",
                    onClick = {},
                    size = RestockButtonSize.LARGE,
                    variant = RestockButtonVariant.TEXT,
                )
            }
        }
    }
}

// Legacy API previews for backward compatibility
@LightPreview
@Composable
private fun RestockButtonLegacyPreview() {
    RestockPreviewTheme {
        RestockButton(text = "Add to List", onClick = {})
    }
}

@LightPreview
@Composable
private fun RestockOutlinedButtonLegacyPreview() {
    RestockPreviewTheme {
        RestockOutlinedButton(text = "Cancel", onClick = {})
    }
}

@LightPreview
@Composable
private fun RestockTextButtonLegacyPreview() {
    RestockPreviewTheme {
        RestockTextButton(text = "Skip", onClick = {})
    }
}
