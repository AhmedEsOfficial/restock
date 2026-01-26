package com.restock.feature.list.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.restock.core.domain.model.CurrentListItem
import com.restock.core.ui.components.RestockButton
import com.restock.core.ui.components.RestockButtonVariant
import com.restock.core.ui.components.RestockOutlinedTextField
import com.restock.core.ui.preview.LightDarkPreview
import com.restock.core.ui.preview.RestockPreviewTheme
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockSpacing
import kotlinx.coroutines.launch

/**
 * Common unit options for grocery items.
 */
private val UNIT_OPTIONS =
    listOf(
        null to "Each",
        "lb" to "Pounds",
        "oz" to "Ounces",
        "kg" to "Kilograms",
        "g" to "Grams",
        "L" to "Liters",
        "mL" to "Milliliters",
        "gal" to "Gallons",
        "ct" to "Count",
    )

/**
 * Minimum quantity allowed.
 */
private const val MIN_QUANTITY = 1

/**
 * Maximum quantity allowed.
 */
private const val MAX_QUANTITY = 99

/**
 * Bottom sheet for adjusting item quantity and unit.
 *
 * Features:
 * - Increment/decrement quantity with buttons
 * - Direct quantity input
 * - Unit selection chips
 * - Save and cancel actions
 *
 * **Design Specifications:**
 * - Corner radius: 24dp (bottom sheet)
 * - Drag handle shown
 * - Surface color background
 *
 * @param item The item being edited.
 * @param onDismiss Called when the sheet is dismissed without saving.
 * @param onConfirm Called when the user confirms their changes with quantity and unit.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantityBottomSheet(
    item: CurrentListItem,
    onDismiss: () -> Unit,
    onConfirm: (quantity: Int, unit: String?) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val hapticFeedback = LocalHapticFeedback.current

    var quantity by remember { mutableIntStateOf(item.quantity) }
    var selectedUnit by remember { mutableStateOf(item.unit) }
    var quantityText by remember { mutableStateOf(item.quantity.toString()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = RestockColors.Surface,
        dragHandle = {
            Surface(
                modifier =
                    Modifier
                        .padding(vertical = RestockSpacing.sm),
                color = RestockColors.MutedVariant,
                shape = MaterialTheme.shapes.extraSmall,
            ) {
                Spacer(
                    modifier =
                        Modifier
                            .width(32.dp)
                            .height(4.dp),
                )
            }
        },
    ) {
        QuantityBottomSheetContent(
            itemName = item.name,
            quantity = quantity,
            quantityText = quantityText,
            selectedUnit = selectedUnit,
            onQuantityChange = { newQuantity ->
                val coercedQuantity = newQuantity.coerceIn(MIN_QUANTITY, MAX_QUANTITY)
                if (coercedQuantity != quantity) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    quantity = coercedQuantity
                    quantityText = quantity.toString()
                }
            },
            onQuantityTextChange = { text ->
                quantityText = text
                text.toIntOrNull()?.let { parsed ->
                    quantity = parsed.coerceIn(MIN_QUANTITY, MAX_QUANTITY)
                }
            },
            onUnitChange = { newUnit ->
                if (newUnit != selectedUnit) {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    selectedUnit = newUnit
                }
            },
            onCancel = {
                scope.launch {
                    sheetState.hide()
                    onDismiss()
                }
            },
            onConfirm = {
                scope.launch {
                    sheetState.hide()
                    onConfirm(quantity, selectedUnit)
                }
            },
        )
    }
}

/**
 * Content of the quantity bottom sheet.
 *
 * Separated for easier testing and previews.
 */
@Composable
private fun QuantityBottomSheetContent(
    itemName: String,
    quantity: Int,
    quantityText: String,
    selectedUnit: String?,
    onQuantityChange: (Int) -> Unit,
    onQuantityTextChange: (String) -> Unit,
    onUnitChange: (String?) -> Unit,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = RestockSpacing.lg)
                .padding(bottom = RestockSpacing.xl),
    ) {
        // Title
        Text(
            text = itemName,
            style = MaterialTheme.typography.titleLarge,
            color = RestockColors.Primary,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(RestockSpacing.lg))

        // Quantity selector
        Text(
            text = "Quantity",
            style = MaterialTheme.typography.labelLarge,
            color = RestockColors.Muted,
        )

        Spacer(modifier = Modifier.height(RestockSpacing.sm))

        QuantitySelector(
            quantity = quantity,
            quantityText = quantityText,
            onIncrement = { onQuantityChange(quantity + 1) },
            onDecrement = { onQuantityChange(quantity - 1) },
            onTextChange = onQuantityTextChange,
        )

        Spacer(modifier = Modifier.height(RestockSpacing.lg))

        HorizontalDivider(color = RestockColors.Divider)

        Spacer(modifier = Modifier.height(RestockSpacing.lg))

        // Unit selector
        Text(
            text = "Unit (optional)",
            style = MaterialTheme.typography.labelLarge,
            color = RestockColors.Muted,
        )

        Spacer(modifier = Modifier.height(RestockSpacing.sm))

        UnitSelector(
            selectedUnit = selectedUnit,
            onUnitSelect = onUnitChange,
        )

        Spacer(modifier = Modifier.height(RestockSpacing.xl))

        // Action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(RestockSpacing.sm),
        ) {
            RestockButton(
                text = "Cancel",
                onClick = onCancel,
                variant = RestockButtonVariant.SECONDARY,
                modifier = Modifier.weight(1f),
            )
            RestockButton(
                text = "Save",
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Quantity selector with increment/decrement buttons and text input.
 */
@Composable
private fun QuantitySelector(
    quantity: Int,
    quantityText: String,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Decrement button
        Surface(
            onClick = onDecrement,
            modifier = Modifier.size(48.dp),
            shape = MaterialTheme.shapes.small,
            color =
                if (quantity > MIN_QUANTITY) {
                    RestockColors.PrimaryContainer
                } else {
                    RestockColors.Disabled.copy(alpha = 0.5f)
                },
            enabled = quantity > MIN_QUANTITY,
        ) {
            Icon(
                imageVector = Icons.Filled.Remove,
                contentDescription = "Decrease quantity",
                tint =
                    if (quantity > MIN_QUANTITY) {
                        RestockColors.OnPrimary
                    } else {
                        RestockColors.Muted
                    },
                modifier = Modifier.padding(RestockSpacing.sm),
            )
        }

        Spacer(modifier = Modifier.width(RestockSpacing.lg))

        // Quantity display/input
        RestockOutlinedTextField(
            value = quantityText,
            onValueChange = { text ->
                // Only allow digits
                if (text.isEmpty() || text.all { it.isDigit() }) {
                    onTextChange(text)
                }
            },
            modifier = Modifier.width(80.dp),
            singleLine = true,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done,
                ),
            trailingIcon = null,
        )

        Spacer(modifier = Modifier.width(RestockSpacing.lg))

        // Increment button
        Surface(
            onClick = onIncrement,
            modifier = Modifier.size(48.dp),
            shape = MaterialTheme.shapes.small,
            color =
                if (quantity < MAX_QUANTITY) {
                    RestockColors.Accent
                } else {
                    RestockColors.Disabled.copy(alpha = 0.5f)
                },
            enabled = quantity < MAX_QUANTITY,
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Increase quantity",
                tint =
                    if (quantity < MAX_QUANTITY) {
                        RestockColors.OnAccent
                    } else {
                        RestockColors.Muted
                    },
                modifier = Modifier.padding(RestockSpacing.sm),
            )
        }
    }
}

/**
 * Unit selector using filter chips.
 */
@Composable
private fun UnitSelector(
    selectedUnit: String?,
    onUnitSelect: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(RestockSpacing.sm),
    ) {
        // First row of units
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(RestockSpacing.sm),
        ) {
            UNIT_OPTIONS.take(4).forEach { (unitValue, unitLabel) ->
                UnitChip(
                    label = unitLabel,
                    selected = selectedUnit == unitValue,
                    onClick = { onUnitSelect(unitValue) },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Second row of units
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(RestockSpacing.sm),
        ) {
            UNIT_OPTIONS.drop(4).take(5).forEach { (unitValue, unitLabel) ->
                UnitChip(
                    label = unitLabel,
                    selected = selectedUnit == unitValue,
                    onClick = { onUnitSelect(unitValue) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Individual unit selection chip.
 */
@Composable
private fun UnitChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        modifier = modifier,
        colors =
            FilterChipDefaults.filterChipColors(
                containerColor = RestockColors.Surface,
                selectedContainerColor = RestockColors.Accent.copy(alpha = 0.15f),
                labelColor = RestockColors.Primary,
                selectedLabelColor = RestockColors.Accent,
            ),
        border =
            FilterChipDefaults.filterChipBorder(
                enabled = true,
                selected = selected,
                borderColor = RestockColors.MutedVariant,
                selectedBorderColor = RestockColors.Accent,
            ),
    )
}

// ==================== PREVIEWS ====================

@LightDarkPreview
@Composable
private fun QuantityBottomSheetContentPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            QuantityBottomSheetContent(
                itemName = "Eggs",
                quantity = 2,
                quantityText = "2",
                selectedUnit = null,
                onQuantityChange = {},
                onQuantityTextChange = {},
                onUnitChange = {},
                onCancel = {},
                onConfirm = {},
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun QuantityBottomSheetContentWithUnitPreview() {
    RestockPreviewTheme {
        Surface(color = RestockColors.Background) {
            QuantityBottomSheetContent(
                itemName = "Chicken Breast",
                quantity = 2,
                quantityText = "2",
                selectedUnit = "lb",
                onQuantityChange = {},
                onQuantityTextChange = {},
                onUnitChange = {},
                onCancel = {},
                onConfirm = {},
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun QuantitySelectorPreview() {
    RestockPreviewTheme {
        Surface(
            color = RestockColors.Background,
            modifier = Modifier.padding(RestockSpacing.md),
        ) {
            QuantitySelector(
                quantity = 5,
                quantityText = "5",
                onIncrement = {},
                onDecrement = {},
                onTextChange = {},
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun QuantitySelectorMinPreview() {
    RestockPreviewTheme {
        Surface(
            color = RestockColors.Background,
            modifier = Modifier.padding(RestockSpacing.md),
        ) {
            QuantitySelector(
                quantity = 1,
                quantityText = "1",
                onIncrement = {},
                onDecrement = {},
                onTextChange = {},
            )
        }
    }
}

@LightDarkPreview
@Composable
private fun UnitSelectorPreview() {
    RestockPreviewTheme {
        Surface(
            color = RestockColors.Background,
            modifier = Modifier.padding(RestockSpacing.md),
        ) {
            UnitSelector(
                selectedUnit = "lb",
                onUnitSelect = {},
            )
        }
    }
}
