package com.restock.core.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.ui.components.LearningCheckbox
import com.restock.core.ui.components.LearningFeedbackOverlay
import com.restock.core.ui.components.LearningGlow
import com.restock.core.ui.components.LearningLabeledCheckbox
import com.restock.core.ui.components.LearningNotedBadge
import com.restock.core.ui.components.LearningSparkle
import com.restock.core.ui.components.LearningStatusBanner
import com.restock.core.ui.components.MealTagChip
import com.restock.core.ui.components.MealType
import com.restock.core.ui.components.NoSuggestionsStateCompact
import com.restock.core.ui.components.RestockBottomNavigation
import com.restock.core.ui.components.RestockButton
import com.restock.core.ui.components.RestockCard
import com.restock.core.ui.components.RestockCheckbox
import com.restock.core.ui.components.RestockChip
import com.restock.core.ui.components.RestockElevatedCard
import com.restock.core.ui.components.RestockInfoChip
import com.restock.core.ui.components.RestockLabeledCheckbox
import com.restock.core.ui.components.RestockLoadingIndicator
import com.restock.core.ui.components.RestockLoadingIndicatorSmall
import com.restock.core.ui.components.RestockNavItems
import com.restock.core.ui.components.RestockOutlinedButton
import com.restock.core.ui.components.RestockOutlinedTextField
import com.restock.core.ui.components.RestockPulsingDots
import com.restock.core.ui.components.RestockSmartBadge
import com.restock.core.ui.components.RestockSnackbar
import com.restock.core.ui.components.RestockTextButton
import com.restock.core.ui.components.RestockTextField
import com.restock.core.ui.components.SuggestionCardStateless
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockTheme

/**
 * Component preview gallery showcasing all Restock design system components.
 *
 * This file is for documentation and development purposes only.
 * Use Android Studio's Preview panel to view all components in one place.
 */
@Composable
private fun SectionHeader(title: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = RestockColors.Primary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = RestockColors.MutedVariant.copy(alpha = 0.3f))
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ComponentLabel(label: String) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelMedium,
        color = RestockColors.Muted,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}

@Suppress("LongMethod")
@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFBF5,
    widthDp = 400,
    heightDp = 3000,
)
@Composable
fun ComponentGalleryPreview() {
    RestockTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(RestockColors.Background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
        ) {
            // Header
            Text(
                text = "Restock Design System",
                style = MaterialTheme.typography.displayLarge,
                color = RestockColors.Primary,
            )
            Text(
                text = "Component Gallery",
                style = MaterialTheme.typography.bodyLarge,
                color = RestockColors.Muted,
            )

            // Buttons Section
            SectionHeader("Buttons")

            ComponentLabel("Primary Button")
            RestockButton(text = "Add to List", onClick = {})

            Spacer(modifier = Modifier.height(12.dp))
            ComponentLabel("Outlined Button")
            RestockOutlinedButton(text = "Cancel", onClick = {})

            Spacer(modifier = Modifier.height(12.dp))
            ComponentLabel("Text Button")
            RestockTextButton(text = "Skip", onClick = {})

            Spacer(modifier = Modifier.height(12.dp))
            ComponentLabel("Disabled Button")
            RestockButton(text = "Disabled", onClick = {}, enabled = false)

            // Cards Section
            SectionHeader("Cards")

            ComponentLabel("Standard Card (List Items)")
            RestockCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Grocery Item Card",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(16.dp),
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            ComponentLabel("Elevated Card (Suggestions)")
            RestockElevatedCard(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Greek yogurt",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    Text(
                        text = "Usually every 7 days",
                        style = MaterialTheme.typography.bodySmall,
                        color = RestockColors.Muted,
                    )
                }
            }

            // Text Fields Section
            SectionHeader("Text Fields")

            var textValue by remember { mutableStateOf("") }
            ComponentLabel("Empty State")
            RestockTextField(
                value = textValue,
                onValueChange = { textValue = it },
                placeholder = "Add an item...",
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))
            ComponentLabel("Filled State")
            RestockTextField(
                value = "Organic Milk",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(12.dp))
            ComponentLabel("Error State (Outlined)")
            RestockOutlinedTextField(
                value = "",
                onValueChange = {},
                isError = true,
                errorMessage = "Please enter an item name",
                modifier = Modifier.fillMaxWidth(),
            )

            // Checkboxes Section
            SectionHeader("Checkboxes")

            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Column {
                    ComponentLabel("Unchecked")
                    RestockCheckbox(checked = false, onCheckedChange = {})
                }
                Column {
                    ComponentLabel("Checked")
                    RestockCheckbox(checked = true, onCheckedChange = {})
                }
                Column {
                    ComponentLabel("Disabled")
                    RestockCheckbox(checked = false, onCheckedChange = {}, enabled = false)
                }
                Column {
                    ComponentLabel("Dis+Check")
                    RestockCheckbox(checked = true, onCheckedChange = {}, enabled = false)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            ComponentLabel("Labeled Checkbox")
            var labeledChecked by remember { mutableStateOf(false) }
            RestockLabeledCheckbox(
                checked = labeledChecked,
                onCheckedChange = { labeledChecked = it },
                label = "Organic Milk",
            )

            // Learning Feedback Section
            SectionHeader("Learning Feedback")

            ComponentLabel("Learning Checkbox (with sparkle/glow)")
            var learningChecked1 by remember { mutableStateOf(false) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                LearningCheckbox(
                    checked = learningChecked1,
                    onCheckedChange = { learningChecked1 = it },
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Tap to see sparkle effect",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            ComponentLabel("Learning Labeled Checkbox")
            var learningChecked2 by remember { mutableStateOf(false) }
            LearningLabeledCheckbox(
                checked = learningChecked2,
                onCheckedChange = { learningChecked2 = it },
                label = "Milk with learning feedback",
            )

            Spacer(modifier = Modifier.height(12.dp))
            ComponentLabel("Learning Status Banners")
            LearningStatusBanner(shoppingTripCount = 0)
            Spacer(modifier = Modifier.height(4.dp))
            LearningStatusBanner(shoppingTripCount = 1)
            Spacer(modifier = Modifier.height(4.dp))
            LearningStatusBanner(shoppingTripCount = 2)

            Spacer(modifier = Modifier.height(12.dp))
            ComponentLabel("Noted Badge")
            var notedTrigger by remember { mutableStateOf(false) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                RestockCheckbox(
                    checked = notedTrigger,
                    onCheckedChange = { notedTrigger = it },
                )
                Spacer(Modifier.width(8.dp))
                Text("Eggs", style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.width(8.dp))
                LearningNotedBadge(trigger = notedTrigger)
            }

            Spacer(modifier = Modifier.height(12.dp))
            ComponentLabel("Sparkle Animation")
            var sparkleTrigger by remember { mutableStateOf(false) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    RestockCheckbox(
                        checked = sparkleTrigger,
                        onCheckedChange = { sparkleTrigger = it },
                    )
                    LearningSparkle(trigger = sparkleTrigger)
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "Standalone sparkle",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            ComponentLabel("Glow Effect")
            var glowTrigger by remember { mutableStateOf(false) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(64.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    LearningGlow(trigger = glowTrigger, size = 48.dp)
                    RestockCheckbox(
                        checked = glowTrigger,
                        onCheckedChange = { glowTrigger = it },
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "Standalone glow",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            ComponentLabel("Combined Overlay")
            var overlayTrigger by remember { mutableStateOf(false) }
            Row(verticalAlignment = Alignment.CenterVertically) {
                LearningFeedbackOverlay(trigger = overlayTrigger) {
                    RestockCheckbox(
                        checked = overlayTrigger,
                        onCheckedChange = { overlayTrigger = it },
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "Sparkle + Glow overlay",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            // Chips Section
            SectionHeader("Chips")

            ComponentLabel("Meal Tag Chips (with emoji)")
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                MealTagChip(mealType = MealType.BREAKFAST)
                MealTagChip(mealType = MealType.LUNCH)
                MealTagChip(mealType = MealType.DINNER)
                MealTagChip(mealType = MealType.SNACK)
            }

            Spacer(modifier = Modifier.height(12.dp))
            ComponentLabel("Meal Tag Chips (with remove)")
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                MealTagChip(mealType = MealType.BREAKFAST, onRemove = {})
                MealTagChip(mealType = MealType.LUNCH, onRemove = {})
            }

            Spacer(modifier = Modifier.height(12.dp))
            ComponentLabel("Meal Tag Chips (no emoji)")
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                MealTagChip(mealType = MealType.DINNER, showEmoji = false)
                MealTagChip(mealType = MealType.SNACK, showEmoji = false)
            }

            Spacer(modifier = Modifier.height(12.dp))
            ComponentLabel("Info Chip")
            RestockInfoChip(text = "Usually every 7 days")

            Spacer(modifier = Modifier.height(12.dp))
            ComponentLabel("Smart Badge")
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text("Milk", style = MaterialTheme.typography.bodyLarge)
                RestockSmartBadge()
            }

            Spacer(modifier = Modifier.height(12.dp))
            ComponentLabel("Generic Chip")
            RestockChip(text = "Custom Chip", color = RestockColors.Info)

            // Suggestion Cards Section
            SectionHeader("Suggestion Cards")

            ComponentLabel("Standard Suggestion Cards")
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.horizontalScroll(rememberScrollState()),
            ) {
                SuggestionCardStateless(
                    itemName = "Milk",
                    reason = "Usually every 7 days",
                    onAccept = {},
                    onDismiss = {},
                )
                SuggestionCardStateless(
                    itemName = "Eggs",
                    reason = "Last bought 20 days ago",
                    onAccept = {},
                    onDismiss = {},
                )
                SuggestionCardStateless(
                    itemName = "Bread",
                    reason = "Usually every 5 days",
                    onAccept = {},
                    onDismiss = {},
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            ComponentLabel("Long Item Name")
            SuggestionCardStateless(
                itemName = "Extra Large Organic Free-Range Eggs",
                reason = "Usually every 2 weeks",
                onAccept = {},
                onDismiss = {},
            )

            Spacer(modifier = Modifier.height(12.dp))
            ComponentLabel("No Suggestions States")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                NoSuggestionsStateCompact(tripCount = 0)
                NoSuggestionsStateCompact(tripCount = 2)
                NoSuggestionsStateCompact(tripCount = 5)
            }

            // Loading Indicators Section
            SectionHeader("Loading Indicators")

            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Column {
                    ComponentLabel("Standard")
                    RestockLoadingIndicator()
                }
                Column {
                    ComponentLabel("Small")
                    RestockLoadingIndicatorSmall()
                }
                Column {
                    ComponentLabel("Pulsing Dots")
                    RestockPulsingDots()
                }
            }

            // Snackbar Section
            SectionHeader("Snackbar")

            ComponentLabel("With Action")
            RestockSnackbar(
                message = "Item deleted",
                actionLabel = "Undo",
                onActionClick = {},
            )

            Spacer(modifier = Modifier.height(12.dp))
            ComponentLabel("Without Action")
            RestockSnackbar(message = "Item added to list")

            // Bottom Navigation Section
            SectionHeader("Bottom Navigation")

            ComponentLabel("Active: List")
            Box(modifier = Modifier.height(64.dp)) {
                RestockBottomNavigation(
                    items = RestockNavItems.defaults,
                    selectedRoute = "list",
                    onItemSelected = {},
                )
            }

            // Color Palette Section
            SectionHeader("Color Palette")

            ComponentLabel("Primary Colors")
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ColorSwatch("Primary", RestockColors.Primary)
                ColorSwatch("Accent", RestockColors.Accent)
                ColorSwatch("Success", RestockColors.Success)
                ColorSwatch("Error", RestockColors.Error)
            }

            Spacer(modifier = Modifier.height(12.dp))
            ComponentLabel("Meal Colors")
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ColorSwatch("Breakfast", RestockColors.Breakfast)
                ColorSwatch("Lunch", RestockColors.Lunch)
                ColorSwatch("Dinner", RestockColors.Dinner)
                ColorSwatch("Snack", RestockColors.Snack)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ColorSwatch(
    name: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Column {
        Box(
            modifier =
                Modifier
                    .width(60.dp)
                    .height(40.dp)
                    .background(color, MaterialTheme.shapes.small),
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.labelSmall,
            color = RestockColors.Muted,
        )
    }
}

/**
 * Typography preview showing all text styles.
 */
@Preview(
    showBackground = true,
    backgroundColor = 0xFFFFFBF5,
    widthDp = 400,
)
@Composable
fun TypographyPreview() {
    RestockTheme {
        Column(
            modifier =
                Modifier
                    .padding(16.dp)
                    .background(RestockColors.Background),
        ) {
            Text(
                text = "Display Large",
                style = MaterialTheme.typography.displayLarge,
            )
            Text(
                text = "Headline Large",
                style = MaterialTheme.typography.headlineLarge,
            )
            Text(
                text = "Title Large",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = "Title Medium",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "Body Large - Primary content",
                style = MaterialTheme.typography.bodyLarge,
            )
            Text(
                text = "Body Medium - Secondary content",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = "Body Small - Captions, metadata",
                style = MaterialTheme.typography.bodySmall,
            )
            Text(
                text = "Label Large - Buttons",
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = "Label Medium - Tags",
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = "Label Small - Badges, chips",
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
