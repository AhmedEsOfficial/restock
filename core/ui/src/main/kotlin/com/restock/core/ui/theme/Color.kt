package com.restock.core.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Restock color palette following the design system specification.
 *
 * Colors are organized by their semantic purpose:
 * - Primary colors for main UI elements
 * - Semantic colors for states and feedback
 * - Meal tag colors for categorization
 */
object RestockColors {
    // Primary colors
    val Primary = Color(0xFF1F1F1F) // Text, icons, primary buttons
    val OnPrimary = Color(0xFFFFFFFF) // Text on primary surfaces
    val PrimaryContainer = Color(0xFF333333) // Container for primary elements

    // Accent colors
    val Accent = Color(0xFFF59E0B) // Suggestions, smart badges, highlights
    val OnAccent = Color(0xFF1F1F1F) // Text on accent surfaces

    // Background colors
    val Background = Color(0xFFFFFBF5) // Main background (warm off-white)
    val OnBackground = Color(0xFF1F1F1F) // Text on background
    val Surface = Color(0xFFFFFFFF) // Cards, inputs, elevated surfaces
    val OnSurface = Color(0xFF1F1F1F) // Text on surfaces

    // Semantic colors
    val Success = Color(0xFF10B981) // Checked items, confirmations
    val OnSuccess = Color(0xFFFFFFFF) // Text on success surfaces
    val Error = Color(0xFFEF4444) // Error states
    val OnError = Color(0xFFFFFFFF) // Text on error surfaces

    // Muted colors
    val Muted = Color(0xFF8B8680) // Secondary text, placeholders
    val MutedVariant = Color(0xFFB8B4AF) // Borders, dividers

    // Overlay colors
    val Scrim = Color(0x80000000) // Modal overlays (50% black)

    // Meal tag colors (per design spec)
    val Breakfast = Color(0xFFF59E0B) // Amber
    val Lunch = Color(0xFF10B981) // Emerald
    val Dinner = Color(0xFF6366F1) // Indigo
    val Snack = Color(0xFFEC4899) // Pink

    // Meal tag container colors (lighter variants for backgrounds)
    val BreakfastContainer = Color(0xFFFEF3C7)
    val LunchContainer = Color(0xFFD1FAE5)
    val DinnerContainer = Color(0xFFE0E7FF)
    val SnackContainer = Color(0xFFFCE7F3)
}

/**
 * Dark theme colors - placeholder for future implementation.
 * Currently mirrors light theme with inverted values.
 */
object RestockDarkColors {
    val Primary = Color(0xFFFFFFFF)
    val OnPrimary = Color(0xFF1F1F1F)
    val PrimaryContainer = Color(0xFF404040)

    val Accent = Color(0xFFF59E0B)
    val OnAccent = Color(0xFF1F1F1F)

    val Background = Color(0xFF121212)
    val OnBackground = Color(0xFFE0E0E0)
    val Surface = Color(0xFF1E1E1E)
    val OnSurface = Color(0xFFE0E0E0)

    val Success = Color(0xFF10B981)
    val OnSuccess = Color(0xFF1F1F1F)
    val Error = Color(0xFFEF4444)
    val OnError = Color(0xFFFFFFFF)

    val Muted = Color(0xFF9CA3AF)
    val MutedVariant = Color(0xFF6B7280)

    val Scrim = Color(0x80000000)

    val Breakfast = Color(0xFFF59E0B)
    val Lunch = Color(0xFF10B981)
    val Dinner = Color(0xFF6366F1)
    val Snack = Color(0xFFEC4899)

    val BreakfastContainer = Color(0xFF78350F)
    val LunchContainer = Color(0xFF064E3B)
    val DinnerContainer = Color(0xFF312E81)
    val SnackContainer = Color(0xFF831843)
}
