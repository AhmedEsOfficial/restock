package com.restock.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockTheme

/**
 * Custom checkbox component for grocery list items.
 *
 * Uses circular design with animated color transitions.
 * Touch target is minimum 48dp for accessibility.
 *
 * @param checked Whether the checkbox is checked.
 * @param onCheckedChange Called when the checkbox state changes.
 * @param modifier Modifier to be applied to the checkbox.
 * @param enabled Whether the checkbox is enabled.
 * @param size The size of the checkbox circle. Default is 24dp.
 */
@Composable
fun RestockCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    size: Dp = 24.dp
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) RestockColors.Success else Color.Transparent,
        animationSpec = tween(durationMillis = 150),
        label = "checkbox_background"
    )

    val borderColor by animateColorAsState(
        targetValue = if (checked) RestockColors.Success else RestockColors.MutedVariant,
        animationSpec = tween(durationMillis = 150),
        label = "checkbox_border"
    )

    val checkmarkColor by animateColorAsState(
        targetValue = if (checked) RestockColors.OnSuccess else Color.Transparent,
        animationSpec = tween(durationMillis = 150),
        label = "checkbox_checkmark"
    )

    Box(
        modifier = modifier
            .size(48.dp) // Minimum touch target
            .clip(CircleShape)
            .clickable(
                enabled = enabled,
                onClick = { onCheckedChange(!checked) },
                role = Role.Checkbox,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true)
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(backgroundColor)
                .border(
                    width = 2.dp,
                    color = borderColor,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(size * 0.7f),
                    tint = checkmarkColor
                )
            }
        }
    }
}

/**
 * Checkbox with label text.
 *
 * @param checked Whether the checkbox is checked.
 * @param onCheckedChange Called when the checkbox state changes.
 * @param label The label text.
 * @param modifier Modifier to be applied to the row.
 * @param enabled Whether the checkbox is enabled.
 */
@Composable
fun RestockLabeledCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier
            .clickable(
                enabled = enabled,
                onClick = { onCheckedChange(!checked) },
                role = Role.Checkbox
            )
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RestockCheckbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (enabled) RestockColors.OnSurface else RestockColors.Muted
        )
    }
}

// Previews
@Preview(showBackground = true)
@Composable
private fun RestockCheckboxUncheckedPreview() {
    RestockTheme {
        RestockCheckbox(
            checked = false,
            onCheckedChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RestockCheckboxCheckedPreview() {
    RestockTheme {
        RestockCheckbox(
            checked = true,
            onCheckedChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RestockLabeledCheckboxPreview() {
    RestockTheme {
        var checked by remember { mutableStateOf(false) }
        RestockLabeledCheckbox(
            checked = checked,
            onCheckedChange = { checked = it },
            label = "Milk"
        )
    }
}
