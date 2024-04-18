package com.cookiedinner.boxanizer.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cookiedinner.boxanizer.main.screens.BoxDetailsScreen
import com.cookiedinner.boxanizer.main.screens.BoxesScreen
import com.cookiedinner.boxanizer.main.screens.ItemsScreen
import com.cookiedinner.boxanizer.main.screens.SettingsScreen
import org.koin.compose.koinInject

@Composable
fun AppNavigationGraph(
    modifier: Modifier = Modifier,
    navigator: Navigator = koinInject()
) {
    val navController = navigator.navController ?: return
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = NavigationScreens.BoxesScreen.route
    ) {
        composable(NavigationScreens.BoxesScreen.route) {
            BoxesScreen()
        }
        composable(NavigationScreens.ItemsScreen.route) {
            ItemsScreen()
        }
        composable(NavigationScreens.SettingsScreen.route) {
            SettingsScreen()
        }
        composable(
            route = "${NavigationScreens.BoxDetailsScreen.route}?boxId={box_id}",
            arguments = listOf(
                navArgument("box_id") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            val boxId = it.arguments?.getLong("box_id") ?: -1L
            BoxDetailsScreen(boxId = boxId)
        }
    }
}