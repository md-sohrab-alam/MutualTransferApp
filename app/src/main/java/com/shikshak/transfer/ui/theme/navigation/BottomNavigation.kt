package com.shikshak.transfer.ui.theme.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigation(
    navController: NavController
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    
    NavigationBar {
        BottomNavigationItems.values().forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}

enum class BottomNavigationItems(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    HOME(Routes.Home, "Home", Icons.Default.Home),
    PROFILE(Routes.Profile, "Profile", Icons.Default.Person),
    REQUEST(Routes.Request, "Request", Icons.Default.List),
    UPDATES(Routes.Updates, "Updates", Icons.Default.Notifications),
    MORE(Routes.More, "More", Icons.Default.Menu)
} 