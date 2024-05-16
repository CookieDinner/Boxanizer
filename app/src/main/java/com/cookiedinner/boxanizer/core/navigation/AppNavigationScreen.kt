package com.cookiedinner.boxanizer.core.navigation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.core.models.BottomNavItem
import com.cookiedinner.boxanizer.core.models.SharedActions
import com.cookiedinner.boxanizer.core.utilities.koinActivityViewModel
import com.cookiedinner.boxanizer.core.viewmodels.MainViewModel
import org.koin.compose.koinInject

@Composable
fun AppNavigationScreen(
    navigator: Navigator = koinInject(),
    navController: NavHostController = rememberNavController(),
    viewModel: MainViewModel = koinActivityViewModel()
) {
    navigator.setNavController(navController)
    val backStackEntry = navController.currentBackStackEntryAsState()

    val fabVisible = viewModel.fabVisible.collectAsStateWithLifecycle()
    val bottomBarVisible = viewModel.bottomBarVisible.collectAsStateWithLifecycle()

    AppNavigationScreenContent(
        currentRoute = backStackEntry.value?.destination?.route,
        fabVisible = fabVisible.value,
        bottomBarVisible = bottomBarVisible.value,
        changeScreen = navigator::changeNavigationScreen,
        goToScreen = navigator::navigateToScreen,
        popBackStack = navigator::popBackStack,
        sendSharedAction = viewModel::sendSharedAction,
        changeFabVisibility = viewModel::changeFabVisibility,
        snackbar = {
            SnackbarHost(hostState = viewModel.snackbarHostState) {
                Snackbar(snackbarData = it)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppNavigationScreenContent(
    currentRoute: String?,
    fabVisible: Boolean,
    bottomBarVisible: Boolean,
    changeScreen: (String) -> Unit,
    goToScreen: (String) -> Unit,
    popBackStack: () -> Unit,
    sendSharedAction: (SharedActions) -> Unit,
    changeFabVisibility: (Boolean) -> Unit,
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
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier
            .navigationBarsPadding()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = snackbar,
        topBar = {
            if (!navigationScreen.isMainScreen) {
                Surface(
                    shadowElevation = if (scrollBehavior.state.overlappedFraction > 0.0) 4.dp else 0.dp,
                ) {
                    TopAppBar(
                        title = {
                            Text(
                                text = when (navigationScreen) {
                                    NavigationScreens.BoxDetailsScreen -> stringResource(R.string.box_details)
                                    NavigationScreens.AddBoxScreen -> stringResource(R.string.add_box)
                                    NavigationScreens.ItemDetailsScreen -> stringResource(R.string.item_details)
                                    NavigationScreens.AddItemScreen -> stringResource(R.string.add_item)
                                    else -> ""
                                }
                            )
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = popBackStack
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Default.ArrowBack,
                                    contentDescription = stringResource(R.string.back_screen)
                                )
                            }
                        },
                        scrollBehavior = scrollBehavior,
                    )
                }
            }
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = navigationScreen in listOf(NavigationScreens.BoxesScreen, NavigationScreens.ItemsScreen) ||
                        fabVisible && navigationScreen in listOf(NavigationScreens.BoxDetailsScreen, NavigationScreens.AddBoxScreen,
                                                                 NavigationScreens.ItemDetailsScreen, NavigationScreens.AddItemScreen),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                FloatingActionButton(
                    modifier = Modifier
                        .padding(8.dp)
                        .imePadding(),
                    onClick = {
                        when (navigationScreen) {
                            NavigationScreens.BoxesScreen -> {
                                goToScreen(NavigationScreens.AddBoxScreen.route)
                            }

                            NavigationScreens.BoxDetailsScreen, NavigationScreens.AddBoxScreen -> {
                                sendSharedAction(SharedActions.SAVE_BOX)
                                changeFabVisibility(false)
                            }

                            NavigationScreens.ItemsScreen -> {
                                goToScreen(NavigationScreens.AddItemScreen.route)
                            }

                            NavigationScreens.ItemDetailsScreen, NavigationScreens.AddItemScreen -> {
                                sendSharedAction(SharedActions.SAVE_ITEM)
                                changeFabVisibility(false)
                            }

                            else -> {}
                        }
                    }
                ) {
                    AnimatedContent(targetState = navigationScreen) {
                        when (it) {
                            NavigationScreens.BoxesScreen, NavigationScreens.ItemsScreen -> {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = stringResource(
                                        id = if (it == NavigationScreens.BoxesScreen)
                                            R.string.add_box
                                        else
                                            R.string.add_item
                                    )
                                )
                            }

                            NavigationScreens.BoxDetailsScreen, NavigationScreens.AddBoxScreen,
                            NavigationScreens.ItemDetailsScreen, NavigationScreens.AddItemScreen -> {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = stringResource(
                                        id = when (it) {
                                            NavigationScreens.BoxDetailsScreen, NavigationScreens.AddBoxScreen -> R.string.save_box
                                            else -> R.string.save_item
                                        }
                                    )
                                )
                            }

                            else -> {}
                        }
                    }
                }
            }

        },
        bottomBar = {
            if (bottomBarVisible && navigationScreen.isMainScreen) {
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