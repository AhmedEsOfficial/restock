package com.restock.core.ui.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.restock.core.ui.theme.RestockTheme

/**
 * Preview annotation for light mode.
 *
 * Use this annotation for composable previews to display them
 * in light theme with the Restock warm off-white background.
 *
 * Usage:
 * ```kotlin
 * @LightPreview
 * @Composable
 * fun MyComponentPreview() {
 *     RestockPreviewTheme {
 *         MyComponent()
 *     }
 * }
 * ```
 */
@Preview(
    name = "Light Mode",
    showBackground = true,
    // RestockColors.Background
    backgroundColor = 0xFFFFFBF5,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
annotation class LightPreview

/**
 * Preview annotation for dark mode.
 *
 * Use this annotation for composable previews to display them
 * in dark theme with the Restock dark background.
 *
 * Usage:
 * ```kotlin
 * @DarkPreview
 * @Composable
 * fun MyComponentPreview() {
 *     RestockPreviewTheme(darkTheme = true) {
 *         MyComponent()
 *     }
 * }
 * ```
 */
@Preview(
    name = "Dark Mode",
    showBackground = true,
    // RestockDarkColors.Background
    backgroundColor = 0xFF121212,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
annotation class DarkPreview

/**
 * Preview annotation for large font accessibility testing.
 *
 * Use this annotation to test how composables render with
 * increased font scaling (1.5x), ensuring text remains
 * readable and layouts remain functional.
 *
 * Usage:
 * ```kotlin
 * @LargeFontPreview
 * @Composable
 * fun MyComponentPreview() {
 *     RestockPreviewTheme {
 *         MyComponent()
 *     }
 * }
 * ```
 */
@Preview(
    name = "Large Font",
    showBackground = true,
    backgroundColor = 0xFFFFFBF5,
    fontScale = 1.5f,
)
annotation class LargeFontPreview

/**
 * Preview annotation for extra large font accessibility testing.
 *
 * Use this annotation to test extreme font scaling (2.0x),
 * ensuring composables gracefully handle maximum accessibility settings.
 */
@Preview(
    name = "Extra Large Font",
    showBackground = true,
    backgroundColor = 0xFFFFFBF5,
    fontScale = 2.0f,
)
annotation class ExtraLargeFontPreview

/**
 * Preview annotation for small device screens.
 *
 * Use this annotation to test layouts on smaller phone screens,
 * ensuring content fits and scrolls appropriately.
 */
@Preview(
    name = "Small Device",
    showBackground = true,
    backgroundColor = 0xFFFFFBF5,
    device = "spec:width=320dp,height=480dp,dpi=320",
)
annotation class SmallDevicePreview

/**
 * Preview annotation for tablet screens.
 *
 * Use this annotation to test how layouts adapt to larger screens,
 * ensuring optimal use of available space.
 */
@Preview(
    name = "Tablet",
    showBackground = true,
    backgroundColor = 0xFFFFFBF5,
    device = "spec:width=800dp,height=1280dp,dpi=240",
)
annotation class TabletPreview

/**
 * Combined preview annotation for light and dark mode.
 *
 * Use this for quick verification that components work in both themes.
 *
 * Usage:
 * ```kotlin
 * @LightDarkPreview
 * @Composable
 * fun MyComponentPreview() {
 *     RestockPreviewTheme {
 *         MyComponent()
 *     }
 * }
 * ```
 */
@LightPreview
@DarkPreview
annotation class LightDarkPreview

/**
 * Combined preview annotation for font scale testing.
 *
 * Includes normal, large (1.5x), and extra large (2.0x) font scales.
 */
@Preview(
    name = "Normal Font",
    showBackground = true,
    backgroundColor = 0xFFFFFBF5,
    fontScale = 1.0f,
)
@Preview(
    name = "Large Font",
    showBackground = true,
    backgroundColor = 0xFFFFFBF5,
    fontScale = 1.5f,
)
@Preview(
    name = "Extra Large Font",
    showBackground = true,
    backgroundColor = 0xFFFFFBF5,
    fontScale = 2.0f,
)
annotation class FontScalePreview

/**
 * Comprehensive preview annotation for component testing.
 *
 * Combines light mode, dark mode, and large font previews
 * for thorough component verification.
 *
 * Usage:
 * ```kotlin
 * @RestockPreviewSet
 * @Composable
 * fun MyComponentPreview() {
 *     RestockPreviewTheme {
 *         MyComponent()
 *     }
 * }
 * ```
 */
@LightPreview
@DarkPreview
@LargeFontPreview
annotation class RestockPreviewSet

/**
 * Wrapper composable for preview configurations.
 *
 * Applies the RestockTheme with configurable dark mode support.
 * Use this in preview functions to ensure consistent theming.
 *
 * @param darkTheme Whether to use dark theme. Defaults to false.
 * @param content The composable content to be themed.
 *
 * Usage:
 * ```kotlin
 * @LightPreview
 * @Composable
 * fun ButtonPreview() {
 *     RestockPreviewTheme {
 *         RestockButton(text = "Click me", onClick = {})
 *     }
 * }
 *
 * @DarkPreview
 * @Composable
 * fun ButtonDarkPreview() {
 *     RestockPreviewTheme(darkTheme = true) {
 *         RestockButton(text = "Click me", onClick = {})
 *     }
 * }
 * ```
 */
@Composable
fun RestockPreviewTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    RestockTheme(darkTheme = darkTheme) {
        content()
    }
}
