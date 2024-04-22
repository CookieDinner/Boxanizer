package com.cookiedinner.boxanizer.core.navigation

import android.view.Surface
import android.view.SurfaceControl
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.core.models.BottomNavItem
import com.cookiedinner.boxanizer.core.utilities.koinActivityViewModel
import com.cookiedinner.boxanizer.main.models.FabActions
import com.cookiedinner.boxanizer.main.viewmodels.BoxesViewModel
import com.cookiedinner.boxanizer.main.viewmodels.MainViewModel
import org.koin.compose.koinInject

@Composable
fun AppNavigationScreen(
    navigator: Navigator = koinInject(),
    navController: NavHostController = rememberNavController(),
    viewModel: MainViewModel = koinActivityViewModel(),
    boxesViewModel: BoxesViewModel = koinActivityViewModel()
) {
    navigator.setNavController(navController)
    val backStackEntry = navController.currentBackStackEntryAsState()

    val fabVisible = viewModel.fabVisible.collectAsStateWithLifecycle()
    val bottomBarVisible = viewModel.bottomBarVisible.collectAsStateWithLifecycle()

    boxesViewModel.setSnackbarHost(viewModel.snackbarHostState)

    AppNavigationScreenContent(
        currentRoute = backStackEntry.value?.destination?.route,
        fabVisible = fabVisible.value,
        bottomBarVisible = bottomBarVisible.value,
        changeScreen = navigator::changeNavigationScreen,
        goToScreen = navigator::navigateToScreen,
        sendFabAction = viewModel::sendFabAction,
        snackbar = {
            SnackbarHost(hostState = viewModel.snackbarHostState) {
                Snackbar(snackbarData = it)
            }
        }
    )
}

@Composable
private fun AppNavigationScreenContent(
    currentRoute: String?,
    fabVisible: Boolean,
    bottomBarVisible: Boolean,
    changeScreen: (String) -> Unit,
    goToScreen: (String) -> Unit,
    sendFabAction: (FabActions) -> Unit,
    snackbar: @Composable () -> Unit
) {
    val bottomNavItems = listOf(
        BottomNavItem(
            name = stringResource(R.string.boxes),
            route = NavigationScreens.BoxesScreen.route,
            unselectedIcon = Icons.Outlined.Widgets,
            selectedIcon = Icons.Filled.Widgets,
            nestedRoutes = listOf(NavigationScreens.BoxDetailsScreen.route)
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
    val navigationScreen = NavigationScreens.fromRoute(currentRoute)
    Scaffold(
        snackbarHost = snackbar,
        floatingActionButton = {
            AnimatedVisibility(
                visible = navigationScreen in listOf(NavigationScreens.BoxesScreen, NavigationScreens.ItemsScreen) ||
                        fabVisible && navigationScreen in listOf(NavigationScreens.BoxDetailsScreen),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FloatingActionButton(
                    modifier = Modifier.padding(8.dp),
                    onClick = {
                        when (navigationScreen) {
                            NavigationScreens.BoxesScreen -> {
                                goToScreen(NavigationScreens.BoxDetailsScreen.route)
                            }

                            NavigationScreens.ItemsScreen -> {
                                //TODO Go to new item details
                            }

                            NavigationScreens.BoxDetailsScreen -> {
                                sendFabAction(FabActions.SAVE_BOX)
                            }

                            else -> {}
                        }
                    }
                ) {
                    AnimatedContent(targetState = navigationScreen) {
                        when (it) {
                            NavigationScreens.BoxesScreen -> {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(id = R.string.add_box)
                                )
                            }

                            NavigationScreens.BoxDetailsScreen -> {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = stringResource(id = R.string.save_box)
                                )
                            }

                            else -> {}
                        }
                    }
                }
            }

        },
        bottomBar = {
            AnimatedVisibility(
                visible = bottomBarVisible && navigationScreen.isMainScreen,
                enter = fadeIn(tween(350)),
                exit = fadeOut(tween(350))
            ){
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
        }
    ) {
        AppNavigationGraph(modifier = Modifier.padding(it))
    }
}