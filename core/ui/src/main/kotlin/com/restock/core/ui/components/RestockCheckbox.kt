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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
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
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.restock.core.ui.preview.LightDarkPreview
import com.restock.core.ui.preview.RestockPreviewTheme
import com.restock.core.ui.theme.RestockColors

/**
 * Animation duration for checkbox state changes.
 */
private const val CHECKBOX_ANIMATION_DURATION_MS = 200

/**
 * Default visual size of the checkbox.
 */
private val CheckboxSize = 24.dp

/**
 * Default touch target size for accessibility.
 */
private val CheckboxTouchTarget = 48.dp

/**
 * Corner radius for the checkbox shape.
 */
private val CheckboxCornerRadius = 6.dp

/**
 * Border width for the checkbox outline.
 */
private val CheckboxBorderWidth = 2.dp

/**
 * Checkmark icon size relative to checkbox size.
 */
private val CheckmarkSize = 16.dp

/**
 * Custom checkbox component for grocery list items.
 *
 * Features:
 * - Animated color transitions for smooth state changes
 * - Scale animation for checkmark appearance
 * - 48dp touch target for accessibility compliance
 * - Rounded corner design (6dp radius)
 * - Support for enabled/disabled states
 *
 * **Specifications:**
 * - Touch target: 48dp × 48dp
 * - Visual size: 24dp × 24dp
 * - Corner radius: 6dp
 * - Border: 2dp
 * - Animation: 200ms
 *
 * @param checked Whether the checkbox is checked.
 * @param onCheckedChange Called when the checkbox state changes.
 * @param modifier Modifier to be applied to the checkbox container.
 * @param enabled Whether the checkbox is enabled and interactive.
 * @param size The visual size of the checkbox. Default is 24dp.
 * @param contentDescription Accessibility description for the checkbox.
 *
 * Usage:
 * ```kotlin
 * var isChecked by remember { mutableStateOf(false) }
 *
 * RestockCheckbox(
 *     checked = isChecked,
 *     onCheckedChange = { isChecked = it },
 *     contentDescription = "Mark milk as purchased"
 * )
 * ```
 */
@Composable
fun RestockCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = CheckboxSize,
    contentDescription: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Animate background color
    val backgroundColor by animateColorAsState(
        targetValue =
            when {
                !enabled -> RestockColors.Disabled
                checked -> RestockColors.Success
                else -> Color.Transparent
            },
        animationSpec = tween(durationMillis = CHECKBOX_ANIMATION_DURATION_MS),
        label = "checkbox_background",
    )

    // Animate border color
    val borderColor by animateColorAsState(
        targetValue =
            when {
                !enabled -> RestockColors.Disabled
                checked -> RestockColors.Success
                else -> RestockColors.Muted
            },
        animationSpec = tween(durationMillis = CHECKBOX_ANIMATION_DURATION_MS),
        label = "checkbox_border",
    )

    // Animate checkmark scale for pop-in effect
    val checkmarkScale by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(durationMillis = CHECKBOX_ANIMATION_DURATION_MS),
        label = "checkbox_checkmark_scale",
    )

    val checkboxShape = RoundedCornerShape(CheckboxCornerRadius)

    Box(
        modifier =
            modifier
                .size(CheckboxTouchTarget) // Touch target
                .clip(RoundedCornerShape(8.dp))
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true),
                    enabled = enabled,
                    role = Role.Checkbox,
                    onClick = { onCheckedChange(!checked) },
                )
                .semantics {
                    contentDescription?.let {
                        this.contentDescription = it
                    }
                    stateDescription = if (checked) "Checked" else "Unchecked"
                },
        contentAlignment = Alignment.Center,
    ) {
        // Checkbox visual (smaller than touch target)
        Box(
            modifier =
                Modifier
                    .size(size)
                    .clip(checkboxShape)
                    .background(backgroundColor)
                    .border(
                        width = CheckboxBorderWidth,
                        color = borderColor,
                        shape = checkboxShape,
                    ),
            contentAlignment = Alignment.Center,
        ) {
            // Checkmark icon with scale animation
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier =
                    Modifier
                        .size(CheckmarkSize)
                        .scale(checkmarkScale),
            )
        }
    }
}

/**
 * Checkbox with an associated label text.
 *
 * The entire row is clickable, making it easier for users to toggle the checkbox.
 * Useful for settings or when the checkbox needs descriptive text.
 *
 * @param checked Whether the checkbox is checked.
 * @param onCheckedChange Called when the checkbox state changes.
 * @param label The label text displayed next to the checkbox.
 * @param modifier Modifier to be applied to the row container.
 * @param enabled Whether the checkbox is enabled and interactive.
 * @param contentDescription Accessibility description for the checkbox.
 *
 * Usage:
 * ```kotlin
 * var isChecked by remember { mutableStateOf(false) }
 *
 * RestockLabeledCheckbox(
 *     checked = isChecked,
 *     onCheckedChange = { isChecked = it },
 *     label = "Milk"
 * )
 * ```
 */
@Composable
fun RestockLabeledCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentDescription: String? = null,
) {
    Row(
        modifier =
            modifier
                .clickable(
                    enabled = enabled,
                    onClick = { onCheckedChange(!checked) },
                    role = Role.Checkbox,
                )
                .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RestockCheckbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            contentDescription = contentDescription,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color =
                when {
                    !enabled && checked -> RestockColors.Muted
                    !enabled -> RestockColors.Disabled
                    checked -> RestockColors.Muted
                    else -> RestockColors.OnSurface
                },
            textDecoration =
                if (checked) {
                    androidx.compose.ui.text.style.TextDecoration.LineThrough
                } else {
                    null
                },
        )
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
            modifier = Modifier.padding(16.dp),
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
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckboxDisabledUncheckedPreview() {
    RestockPreviewTheme {
        RestockCheckbox(
            checked = false,
            onCheckedChange = {},
            enabled = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckboxDisabledCheckedPreview() {
    RestockPreviewTheme {
        RestockCheckbox(
            checked = true,
            onCheckedChange = {},
            enabled = false,
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CheckboxDisabledStatesPreview() {
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

@Preview(showBackground = true)
@Composable
private fun LabeledCheckboxUncheckedPreview() {
    RestockPreviewTheme {
        RestockLabeledCheckbox(
            checked = false,
            onCheckedChange = {},
            label = "Milk",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LabeledCheckboxCheckedPreview() {
    RestockPreviewTheme {
        RestockLabeledCheckbox(
            checked = true,
            onCheckedChange = {},
            label = "Milk",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LabeledCheckboxDisabledPreview() {
    RestockPreviewTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            RestockLabeledCheckbox(
                checked = false,
                onCheckedChange = {},
                label = "Unchecked Disabled",
                enabled = false,
            )
            RestockLabeledCheckbox(
                checked = true,
                onCheckedChange = {},
                label = "Checked Disabled",
                enabled = false,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LabeledCheckboxInteractivePreview() {
    RestockPreviewTheme {
        var checked by remember { mutableStateOf(false) }
        RestockLabeledCheckbox(
            checked = checked,
            onCheckedChange = { checked = it },
            label = "Milk",
            modifier = Modifier.padding(16.dp),
        )
    }
}

@LightDarkPreview
@Composable
private fun CheckboxAllStatesPreview() {
    RestockPreviewTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Checkbox States",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp),
            )
            Row {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Unchecked", style = MaterialTheme.typography.labelSmall)
                    RestockCheckbox(checked = false, onCheckedChange = {})
                }
                Spacer(Modifier.width(24.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Checked", style = MaterialTheme.typography.labelSmall)
                    RestockCheckbox(checked = true, onCheckedChange = {})
                }
                Spacer(Modifier.width(24.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Disabled", style = MaterialTheme.typography.labelSmall)
                    RestockCheckbox(
                        checked = false,
                        onCheckedChange = {},
                        enabled = false,
                    )
                }
                Spacer(Modifier.width(24.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Dis+Check", style = MaterialTheme.typography.labelSmall)
                    RestockCheckbox(
                        checked = true,
                        onCheckedChange = {},
                        enabled = false,
                    )
                }
            }
        }
    }
}
