package com.restock.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.restock.core.ui.R

/**
 * DM Sans font family - primary typeface for Restock.
 *
 * DM Sans is a low-contrast geometric sans serif design, intended for use at
 * smaller text sizes. It provides excellent legibility for grocery list items
 * and maintains visual clarity across different screen densities.
 *
 * Font weights available:
 * - Regular (400): Body text, secondary content
 * - Medium (500): Titles, emphasized content
 * - SemiBold (600): Display text, headers
 * - Bold (700): Strong emphasis, call-to-action
 *
 * @see <a href="https://fonts.google.com/specimen/DM+Sans">DM Sans on Google Fonts</a>
 */
val DmSansFontFamily =
    FontFamily(
        Font(R.font.dm_sans_regular, FontWeight.Normal),
        Font(R.font.dm_sans_medium, FontWeight.Medium),
        Font(R.font.dm_sans_semibold, FontWeight.SemiBold),
        Font(R.font.dm_sans_bold, FontWeight.Bold),
    )

/**
 * Restock typography system following Material 3 conventions with DM Sans.
 *
 * This typography scale is optimized for grocery list applications with
 * clear hierarchy for item names, quantities, and metadata.
 *
 * Typography scale:
 * - Display: 32sp SemiBold - App title, hero text
 * - Headline: 20-24sp Medium - Section headers
 * - Title: 14-20sp Medium - Card titles, list items
 * - Body: 12-15sp Regular - Main content, descriptions
 * - Label: 11-14sp Medium - Captions, badges, chips
 *
 * All styles use DM Sans with appropriate letter spacing for optimal
 * readability at each size.
 */
val RestockTypography =
    Typography(
        // Display styles - for hero text and app title
        displayLarge =
            TextStyle(
                fontFamily = DmSansFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 32.sp,
                lineHeight = 40.sp,
                letterSpacing = (-0.25).sp,
            ),
        displayMedium =
            TextStyle(
                fontFamily = DmSansFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 28.sp,
                lineHeight = 36.sp,
                letterSpacing = 0.sp,
            ),
        displaySmall =
            TextStyle(
                fontFamily = DmSansFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                letterSpacing = 0.sp,
            ),
        // Headline styles - for section headers
        headlineLarge =
            TextStyle(
                fontFamily = DmSansFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.sp,
            ),
        headlineMedium =
            TextStyle(
                fontFamily = DmSansFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                lineHeight = 26.sp,
                letterSpacing = 0.sp,
            ),
        headlineSmall =
            TextStyle(
                fontFamily = DmSansFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.sp,
            ),
        // Title styles - for card titles and list items
        titleLarge =
            TextStyle(
                fontFamily = DmSansFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 24.sp,
                lineHeight = 32.sp,
                letterSpacing = 0.sp,
            ),
        titleMedium =
            TextStyle(
                fontFamily = DmSansFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 20.sp,
                lineHeight = 28.sp,
                letterSpacing = 0.15.sp,
            ),
        titleSmall =
            TextStyle(
                fontFamily = DmSansFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.1.sp,
            ),
        // Body styles - for main content
        bodyLarge =
            TextStyle(
                fontFamily = DmSansFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                lineHeight = 24.sp,
                letterSpacing = 0.5.sp,
            ),
        bodyMedium =
            TextStyle(
                fontFamily = DmSansFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                letterSpacing = 0.25.sp,
            ),
        bodySmall =
            TextStyle(
                fontFamily = DmSansFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.4.sp,
            ),
        // Label styles - for captions, badges, and chips
        labelLarge =
            TextStyle(
                fontFamily = DmSansFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                letterSpacing = 0.1.sp,
            ),
        labelMedium =
            TextStyle(
                fontFamily = DmSansFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                letterSpacing = 0.5.sp,
            ),
        labelSmall =
            TextStyle(
                fontFamily = DmSansFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp,
                lineHeight = 14.sp,
                letterSpacing = 0.5.sp,
            ),
    )
