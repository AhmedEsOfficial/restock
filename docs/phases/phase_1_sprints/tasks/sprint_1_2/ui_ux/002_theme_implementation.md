# Task: Implement RestockTheme in Compose

**Sprint:** 1-2 (Project Setup & Core Infrastructure)
**Owner:** UI/UX Engineer
**Estimate:** 8 hours (D-2.1 through D-2.8 combined)
**Priority:** P0
**Prerequisites:** 001_design_system_specification.md

---

## Sprint Context

The theme implementation translates the design system specification into Jetpack Compose code. This creates the foundation for consistent styling across all screens and components.

### Technical Decision
100% Jetpack Compose, no XML layouts. Declarative UI is more maintainable and enables better performance with proper optimization.

---

## Task Description

Implement the complete Restock theme including colors, typography, spacing, shapes, and the RestockTheme composable wrapper.

## Implementation

### RestockColors.kt

```kotlin
package com.restock.core.ui.theme

import androidx.compose.ui.graphics.Color

object RestockColors {
    // Primary Palette
    val Primary = Color(0xFF1F1F1F)
    val Accent = Color(0xFFF59E0B)
    val Success = Color(0xFF10B981)
    val Background = Color(0xFFFFFBF5)
    val Card = Color(0xFFFFFFFF)
    val Muted = Color(0xFF8B8680)

    // Meal Tag Colors
    val Breakfast = Color(0xFFF59E0B)  // Amber
    val Lunch = Color(0xFF10B981)      // Emerald
    val Dinner = Color(0xFF6366F1)     // Indigo
    val Snack = Color(0xFFEC4899)      // Pink

    // State Colors
    val Error = Color(0xFFEF4444)
    val Warning = Color(0xFFF59E0B)
    val Info = Color(0xFF3B82F6)
    val Disabled = Color(0xFFD1D5DB)

    // Semantic Aliases
    val OnPrimary = Color.White
    val OnBackground = Primary
    val OnCard = Primary
    val OnAccent = Color.White
    val OnSuccess = Color.White
    val OnError = Color.White

    // Surfaces
    val Surface = Card
    val SurfaceVariant = Background

    // Dividers
    val Divider = Muted.copy(alpha = 0.2f)
}

// Extension for meal type colors
fun MealType.color(): Color = when (this) {
    MealType.BREAKFAST -> RestockColors.Breakfast
    MealType.LUNCH -> RestockColors.Lunch
    MealType.DINNER -> RestockColors.Dinner
    MealType.SNACK -> RestockColors.Snack
}
```

### RestockTypography.kt

```kotlin
package com.restock.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.restock.core.ui.R

val DmSansFontFamily = FontFamily(
    Font(R.font.dm_sans_regular, FontWeight.Normal),
    Font(R.font.dm_sans_medium, FontWeight.Medium),
    Font(R.font.dm_sans_semibold, FontWeight.SemiBold),
    Font(R.font.dm_sans_bold, FontWeight.Bold),
)

val RestockTypography = Typography(
    // Display
    displayLarge = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.25).sp,
    ),

    // Title Large
    titleLarge = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),

    // Title Medium
    titleMedium = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.15.sp,
    ),

    // Title Small
    titleSmall = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp,
    ),

    // Body Large
    bodyLarge = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),

    // Body Medium (primary content)
    bodyMedium = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp,
    ),

    // Body Small
    bodySmall = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.4.sp,
    ),

    // Label Large (buttons)
    labelLarge = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),

    // Label Medium (captions)
    labelMedium = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),

    // Label Small (badges)
    labelSmall = TextStyle(
        fontFamily = DmSansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
    ),
)
```

### RestockSpacing.kt

```kotlin
package com.restock.core.ui.theme

import androidx.compose.ui.unit.dp

object RestockSpacing {
    val xs = 4.dp   // Tight spacing, icon gaps
    val sm = 8.dp   // Between related elements
    val md = 16.dp  // Card padding, item gaps
    val lg = 24.dp  // Section spacing
    val xl = 32.dp  // Major section breaks
    val xxl = 48.dp // Screen margins (top)

    // Common patterns
    val screenHorizontalPadding = md
    val cardPadding = md
    val itemGap = sm
    val sectionGap = lg
}
```

### RestockShapes.kt

```kotlin
package com.restock.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object RestockCornerRadius {
    val sm = 8.dp   // Chips, small buttons
    val md = 12.dp  // Input fields
    val lg = 14.dp  // List item cards
    val xl = 16.dp  // Suggestion cards, modals
}

val RestockShapes = Shapes(
    extraSmall = RoundedCornerShape(RestockCornerRadius.sm),
    small = RoundedCornerShape(RestockCornerRadius.sm),
    medium = RoundedCornerShape(RestockCornerRadius.md),
    large = RoundedCornerShape(RestockCornerRadius.lg),
    extraLarge = RoundedCornerShape(RestockCornerRadius.xl),
)
```

### RestockElevation.kt

```kotlin
package com.restock.core.ui.theme

import androidx.compose.ui.unit.dp

object RestockElevation {
    val level0 = 0.dp    // Flat elements
    val level1 = 1.dp    // List item cards
    val level2 = 2.dp    // Suggestion cards, inputs
    val level3 = 4.dp    // Modals, bottom sheets
}

// For custom shadow implementation (optional)
object RestockShadows {
    // Level 1: 0 1px 3px rgba(0,0,0,0.05)
    // Level 2: 0 2px 8px rgba(0,0,0,0.08)
    // Level 3: 0 4px 16px rgba(0,0,0,0.12)
}
```

### Theme.kt

```kotlin
package com.restock.core.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = RestockColors.Primary,
    onPrimary = RestockColors.OnPrimary,
    primaryContainer = RestockColors.Accent,
    onPrimaryContainer = RestockColors.OnAccent,
    secondary = RestockColors.Accent,
    onSecondary = RestockColors.OnAccent,
    secondaryContainer = RestockColors.Accent.copy(alpha = 0.15f),
    onSecondaryContainer = RestockColors.Accent,
    tertiary = RestockColors.Success,
    onTertiary = RestockColors.OnSuccess,
    error = RestockColors.Error,
    onError = RestockColors.OnError,
    background = RestockColors.Background,
    onBackground = RestockColors.OnBackground,
    surface = RestockColors.Surface,
    onSurface = RestockColors.Primary,
    surfaceVariant = RestockColors.SurfaceVariant,
    onSurfaceVariant = RestockColors.Muted,
    outline = RestockColors.Divider,
    outlineVariant = RestockColors.Divider,
)

// Dark color scheme (stretch goal - implement later)
private val DarkColorScheme = darkColorScheme(
    // To be implemented in later sprint
)

@Composable
fun RestockTheme(
    darkTheme: Boolean = false, // isSystemInDarkTheme() - disabled for MVP
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = RestockTypography,
        shapes = RestockShapes,
        content = content
    )
}
```

### Preview Utilities

```kotlin
package com.restock.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "Light Mode",
    showBackground = true,
    backgroundColor = 0xFFFFFBF5,
)
annotation class LightPreview

@Preview(
    name = "Dark Mode",
    showBackground = true,
    backgroundColor = 0xFF1F1F1F,
)
annotation class DarkPreview

@Preview(
    name = "Large Font",
    showBackground = true,
    fontScale = 1.5f,
)
annotation class LargeFontPreview

/**
 * Wrapper for preview composables
 */
@Composable
fun RestockPreviewTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    RestockTheme(darkTheme = darkTheme) {
        content()
    }
}
```

## Font Assets

Download DM Sans font files and place in:
```
core/ui/src/main/res/font/
├── dm_sans_regular.ttf
├── dm_sans_medium.ttf
├── dm_sans_semibold.ttf
└── dm_sans_bold.ttf
```

## Acceptance Criteria

- [ ] RestockColors object with all color values
- [ ] RestockTypography with all text styles
- [ ] RestockSpacing with spacing tokens
- [ ] RestockShapes with corner radius
- [ ] RestockTheme composable wrapper
- [ ] Status bar color matches theme
- [ ] Preview utilities created
- [ ] Font files included

## Testing

```kotlin
@Test
fun theme_appliesColorsCorrectly() {
    composeTestRule.setContent {
        RestockTheme {
            Text(
                text = "Test",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    // Verify primary color is applied
    // (Visual verification in preview)
}
```

## Definition of Done

- [ ] All theme files compile without errors
- [ ] Theme applies correctly in preview
- [ ] Colors match design specification
- [ ] Typography renders correctly
- [ ] Font files included and loading
- [ ] Preview configurations working
- [ ] Documentation comments added

## Dependent Tasks

This task blocks:
- 003_core_button_components.md
- 004_core_card_components.md
- All component implementation tasks

## Notes

- Dark mode is a stretch goal; focus on light mode first
- Use MaterialTheme tokens in components for consistency
- Preview at different font scales for accessibility
- Test on multiple device sizes
