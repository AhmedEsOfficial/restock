package com.restock.core.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.List
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.restock.core.ui.theme.RestockColors
import com.restock.core.ui.theme.RestockTheme

/**
 * Navigation item data for bottom navigation.
 *
 * @param route The navigation route identifier.
 * @param label The label text shown below the icon.
 * @param selectedIcon The icon shown when this item is selected.
 * @param unselectedIcon The icon shown when this item is not selected.
 * @param contentDescription Accessibility description for the icon.
 */
data class RestockNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val contentDescription: String = label,
)

/**
 * Restock bottom navigation bar following the design specification.
 *
 * Design specs:
 * - Height: 64dp + safe area padding
 * - Background: White with slight transparency for frosted glass effect
 * - Icon size: 24dp
 * - Label size: 11sp (labelSmall)
 * - Active color: Primary (#1F1F1F)
 * - Inactive color: Muted (#8B8680)
 *
 * @param items The list of navigation items.
 * @param selectedRoute The currently selected route.
 * @param onItemSelected Called when a navigation item is selected.
 * @param modifier Modifier to be applied to the navigation bar.
 */
@Composable
fun RestockBottomNavigation(
    items: List<RestockNavItem>,
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        color = RestockColors.Surface.copy(alpha = 0.95f),
        shadowElevation = 8.dp,
    ) {
        NavigationBar(
            modifier = Modifier
                .height(64.dp)
                .windowInsetsPadding(WindowInsets.navigationBars),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
        ) {
            items.forEach { item ->
                val isSelected = item.route == selectedRoute

                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onItemSelected(item.route) },
                    icon = {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.contentDescription,
                            modifier = Modifier.size(24.dp),
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = RestockColors.Primary,
                        selectedTextColor = RestockColors.Primary,
                        unselectedIconColor = RestockColors.Muted,
                        unselectedTextColor = RestockColors.Muted,
                        indicatorColor = RestockColors.Surface,
                    ),
                )
            }
        }
    }
}

/**
 * Individual navigation bar item for custom layouts.
 *
 * @param selected Whether this item is currently selected.
 * @param onClick Called when this item is clicked.
 * @param icon The icon to display.
 * @param label The label text.
 * @param modifier Modifier to be applied to the item.
 */
@Composable
fun RowScope.RestockNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    label: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = icon,
        label = label,
        modifier = modifier,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = RestockColors.Primary,
            selectedTextColor = RestockColors.Primary,
            unselectedIconColor = RestockColors.Muted,
            unselectedTextColor = RestockColors.Muted,
            indicatorColor = RestockColors.Surface,
        ),
    )
}

/**
 * Default navigation items for the Restock app.
 */
object RestockNavItems {
    val List = RestockNavItem(
        route = "list",
        label = "List",
        selectedIcon = Icons.Filled.List,
        unselectedIcon = Icons.Outlined.List,
        contentDescription = "Grocery list",
    )

    val Meals = RestockNavItem(
        route = "meals",
        label = "Meals",
        selectedIcon = Icons.Filled.DateRange,
        unselectedIcon = Icons.Outlined.DateRange,
        contentDescription = "Meals view",
    )

    val History = RestockNavItem(
        route = "history",
        label = "History",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        contentDescription = "Purchase history",
    )

    val defaults = listOf(List, Meals, History)
}

// Previews
@Preview(showBackground = true)
@Composable
private fun RestockBottomNavigationListSelectedPreview() {
    RestockTheme {
        RestockBottomNavigation(
            items = RestockNavItems.defaults,
            selectedRoute = "list",
            onItemSelected = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RestockBottomNavigationMealsSelectedPreview() {
    RestockTheme {
        RestockBottomNavigation(
            items = RestockNavItems.defaults,
            selectedRoute = "meals",
            onItemSelected = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RestockBottomNavigationInteractivePreview() {
    RestockTheme {
        var selectedRoute by remember { mutableIntStateOf(0) }
        val routes = listOf("list", "meals", "history")

        RestockBottomNavigation(
            items = RestockNavItems.defaults,
            selectedRoute = routes[selectedRoute],
            onItemSelected = { route ->
                selectedRoute = routes.indexOf(route)
            },
        )
    }
}
