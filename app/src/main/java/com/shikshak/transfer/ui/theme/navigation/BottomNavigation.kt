package com.shikshak.transfer.ui.theme.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.shikshak.transfer.R

@Composable
fun BottomNavigation(
    navController: NavController
) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    
    NavigationBar {
        BottomNavigationItems.values().forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = stringResource(item.titleResId)) },
                label = { Text(stringResource(item.titleResId)) },
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
    val titleResId: Int,
    val icon: ImageVector
) {
    HOME(Routes.Home, R.string.home, Icons.Default.Home),
    PROFILE(Routes.Profile, R.string.profile, Icons.Default.Person),
    REQUEST(Routes.Request, R.string.request, Icons.Default.List),
    UPDATES(Routes.Updates, R.string.updates, Icons.Default.Notifications),
    MORE(Routes.More, R.string.more, Icons.Default.Menu)
} 