package com.cookiedinner.boxanizer.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.navArgument
import com.cookiedinner.boxanizer.boxes.screens.BoxDetailsScreen
import com.cookiedinner.boxanizer.boxes.screens.BoxesScreen
import com.cookiedinner.boxanizer.items.screens.ItemDetailsScreen
import com.cookiedinner.boxanizer.items.screens.ItemsScreen
import com.cookiedinner.boxanizer.settings.screens.SettingsScreen
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
        customNavigationComposable(NavigationScreens.BoxesScreen.route) {
            BoxesScreen()
        }
        customNavigationComposable(NavigationScreens.ItemsScreen.route) {
            ItemsScreen()
        }
        customNavigationComposable(NavigationScreens.SettingsScreen.route) {
            SettingsScreen()
        }

        customNavigationComposable(
            route = "${NavigationScreens.BoxDetailsScreen.route}?boxId={box_id}&itemId={item_id}",
            arguments = listOf(
                navArgument("box_id") {
                    type = NavType.LongType
                    defaultValue = -1L
                },
                navArgument("item_id") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            val boxId = it.arguments?.getLong("box_id") ?: -1L
            val itemId = it.arguments?.getLong("item_id") ?: -1
            BoxDetailsScreen(
                boxId = boxId,
                itemId = if (itemId == -1L) null else itemId
            )
        }
        customNavigationComposable(NavigationScreens.AddBoxScreen.route) {
            BoxDetailsScreen(boxId = -1L)
        }

        customNavigationComposable(
            route = "${NavigationScreens.ItemDetailsScreen.route}?itemId={item_id}",
            arguments = listOf(
                navArgument("item_id") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) {
            val itemId = it.arguments?.getLong("item_id") ?: -1L
            ItemDetailsScreen(itemId = itemId)
        }
        customNavigationComposable(NavigationScreens.AddItemScreen.route) {
            ItemDetailsScreen(itemId = -1L)
        }
    }
}