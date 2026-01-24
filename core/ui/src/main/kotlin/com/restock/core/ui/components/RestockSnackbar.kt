package com.restock.core.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.restock.core.ui.preview.LightDarkPreview
import com.restock.core.ui.preview.LightPreview
import com.restock.core.ui.preview.RestockPreviewTheme
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockCornerRadius
import com.restock.core.ui.theme.RestockSpacing

/**
 * Restock snackbar component following the design specification.
 *
 * Design specs:
 * - Corner radius: 8dp (small)
 * - Background: Primary (#1F1F1F)
 * - Text color: White
 * - Action color: Accent (#F59E0B)
 * - Margin: 16dp
 *
 * @param snackbarData The data for the snackbar to display.
 * @param modifier Modifier to be applied to the snackbar.
 */
@Composable
fun RestockSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
) {
    Snackbar(
        snackbarData = snackbarData,
        modifier = modifier.padding(RestockSpacing.md),
        shape = MaterialTheme.shapes.small,
        containerColor = RestockColors.Primary,
        contentColor = RestockColors.OnPrimary,
        actionColor = RestockColors.Accent,
        actionContentColor = RestockColors.Accent,
        dismissActionContentColor = RestockColors.OnPrimary.copy(alpha = 0.7f),
    )
}

/**
 * Custom snackbar with explicit parameters for more control.
 *
 * This variant allows direct control over the snackbar content without
 * going through SnackbarHostState. Use this for preview purposes or
 * when you need manual control.
 *
 * @param message The message to display.
 * @param modifier Modifier to be applied to the snackbar.
 * @param actionLabel Optional action button label.
 * @param onActionClick Optional action button click handler.
 * @param dismissOnAction Whether to dismiss the snackbar when action is clicked.
 */
@Suppress("UnusedParameter")
@Composable
fun RestockSnackbar(
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    dismissOnAction: Boolean = true,
) {
    Snackbar(
        modifier = modifier.padding(RestockSpacing.md),
        shape = MaterialTheme.shapes.small,
        containerColor = RestockColors.Primary,
        contentColor = RestockColors.OnPrimary,
        action = if (actionLabel != null && onActionClick != null) {
            {
                TextButton(
                    onClick = onActionClick,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = RestockColors.Accent,
                    ),
                ) {
                    Text(
                        text = actionLabel,
                        color = RestockColors.Accent,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        } else {
            null
        },
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

/**
 * Snackbar host that displays RestockSnackbar.
 *
 * Usage:
 * ```kotlin
 * val snackbarHostState = remember { SnackbarHostState() }
 *
 * Scaffold(
 *     snackbarHost = { RestockSnackbarHost(snackbarHostState) }
 * ) { ... }
 *
 * // To show a snackbar:
 * scope.launch {
 *     snackbarHostState.showSnackbar(
 *         message = "Item deleted",
 *         actionLabel = "Undo",
 *         duration = SnackbarDuration.Short
 *     )
 * }
 * ```
 *
 * @param hostState The state of the snackbar host.
 * @param modifier Modifier to be applied to the snackbar host.
 */
@Composable
fun RestockSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier,
        snackbar = { snackbarData ->
            RestockSnackbar(snackbarData = snackbarData)
        },
    )
}

/**
 * Custom snackbar visuals for Restock-specific snackbar types.
 */
data class RestockSnackbarVisuals(
    override val message: String,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    val isUndo: Boolean = false,
) : SnackbarVisuals

/**
 * Extension function to show an undo snackbar.
 *
 * @param message The message to display.
 * @param actionLabel The undo button label. Defaults to "Undo".
 * @param duration The duration for the snackbar. Defaults to 5 seconds (Long).
 * @return SnackbarResult indicating whether the action was performed or dismissed.
 */
suspend fun SnackbarHostState.showUndoSnackbar(
    message: String,
    actionLabel: String = "Undo",
    duration: SnackbarDuration = SnackbarDuration.Long,
): SnackbarResult {
    return showSnackbar(
        message = message,
        actionLabel = actionLabel,
        duration = duration,
        withDismissAction = false,
    )
}

// ==================== PREVIEWS ====================

@LightPreview
@Composable
private fun SnackbarPreview() {
    RestockPreviewTheme {
        // Using the simplified visual preview as in task spec
        Snackbar(
            modifier = Modifier.padding(16.dp),
            shape = MaterialTheme.shapes.small,
            containerColor = RestockColors.Primary,
            contentColor = RestockColors.OnPrimary,
            action = {
                TextButton(onClick = {}) {
                    Text("Undo", color = RestockColors.Accent)
                }
            },
        ) {
            Text("Item deleted")
        }
    }
}

@LightPreview
@Composable
private fun SnackbarNoActionPreview() {
    RestockPreviewTheme {
        Snackbar(
            modifier = Modifier.padding(16.dp),
            shape = MaterialTheme.shapes.small,
            containerColor = RestockColors.Primary,
            contentColor = RestockColors.OnPrimary,
        ) {
            Text("Milk added to list")
        }
    }
}

@LightDarkPreview
@Composable
private fun RestockSnackbarPreview() {
    RestockPreviewTheme {
        RestockSnackbar(
            message = "Item deleted",
        )
    }
}

@LightPreview
@Composable
private fun RestockSnackbarWithActionPreview() {
    RestockPreviewTheme {
        RestockSnackbar(
            message = "Item deleted",
            actionLabel = "Undo",
            onActionClick = {},
        )
    }
}

@LightPreview
@Composable
private fun RestockSnackbarHostPreview() {
    RestockPreviewTheme {
        val snackbarHostState = remember { SnackbarHostState() }

        RestockSnackbarHost(
            hostState = snackbarHostState,
        )

        // Trigger snackbar for preview using LaunchedEffect
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar(
                message = "Preview snackbar",
                actionLabel = "Action",
            )
        }
    }
}
