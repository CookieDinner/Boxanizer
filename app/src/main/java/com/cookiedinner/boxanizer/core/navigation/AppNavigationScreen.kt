package com.cookiedinner.boxanizer.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.core.models.BottomNavItem
import org.koin.compose.koinInject

@Composable
fun AppNavigationScreen(
    navigator: Navigator = koinInject(),
    navController: NavHostController = rememberNavController()
) {
    navigator.setNavController(navController)
    val backStackEntry = navController.currentBackStackEntryAsState()

    AppNavigationScreenContent(
        currentRoute = backStackEntry.value?.destination?.route,
        changeScreen = {
            navigator.changeNavigationScreen(it)
        }
    )
}

@Composable
private fun AppNavigationScreenContent(
    currentRoute: String?,
    changeScreen: (String) -> Unit
) {
    val bottomNavItems = listOf(
        BottomNavItem(
            name = stringResource(R.string.boxes),
            route = NavigationScreens.BoxesScreen.route,
            unselectedIcon = Icons.Outlined.Widgets,
            selectedIcon = Icons.Filled.Widgets
        ),
        BottomNavItem(
            name = stringResource(R.string.items),
            route = NavigationScreens.ItemsScreen.route,
            unselectedIcon = Icons.AutoMirrored.Outlined.ViewList,
            selectedIcon = Icons.AutoMirrored.Filled.ViewList
        ),
        BottomNavItem(
            name = stringResource(R.string.settings),
            route = NavigationScreens.SettingsScreen.route,
            unselectedIcon = Icons.Outlined.Settings,
            selectedIcon = Icons.Filled.Settings
        )
    )
    Scaffold(
        bottomBar = {
            Surface(shadowElevation = 12.dp) {
                BottomAppBar {
                    bottomNavItems.forEach {
                        val selected = it.route == NavigationScreens.fromRoute(currentRoute).route
                        val selectedIfNested = it.nestedRoutes?.contains(NavigationScreens.fromRoute(currentRoute).route) ?: false
                        NavigationBarItem(
                            selected = selected || selectedIfNested,
                            onClick = {
                                if (!selected)
                                    changeScreen(it.route)
                            },
                            label = {
                                Text(
                                    text = it.name,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (selected || selectedIfNested) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = if (selected || selectedIfNested)
                                        it.selectedIcon
                                    else
                                        it.unselectedIcon,
                                    contentDescription = it.name
                                )
                            }
                        )
                    }
                }
            }
        }
    ) {
        AppNavigationGraph(modifier = Modifier.padding(it))
    }
}