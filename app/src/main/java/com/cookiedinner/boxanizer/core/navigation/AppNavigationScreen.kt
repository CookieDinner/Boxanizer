package com.cookiedinner.boxanizer.core.navigation

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SavedSearch
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.core.components.CameraDialog
import com.cookiedinner.boxanizer.core.components.keyboardAsState
import com.cookiedinner.boxanizer.core.models.BottomNavItem
import com.cookiedinner.boxanizer.core.models.CameraDialogState
import com.cookiedinner.boxanizer.core.models.SearchType
import com.cookiedinner.boxanizer.core.models.SharedActions
import com.cookiedinner.boxanizer.core.models.rememberCameraDialogState
import com.cookiedinner.boxanizer.core.utilities.koinActivityViewModel
import com.cookiedinner.boxanizer.core.viewmodels.MainViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    val cameraState = rememberCameraDialogState()

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
        },
        boxesSearchText = viewModel.rawBoxesSearchText,
        itemsSearchText = viewModel.rawItemsSearchText,
        cameraState = cameraState,
        editSearchText = viewModel::editSearch,
        onScannedCode = { code ->
            viewModel.findBoxIdByCode(code) {
                if (it != null) {
                    cameraState.hide()
                    navigator.navigateToScreen(
                        route = "${NavigationScreens.BoxDetailsScreen.route}?boxId=$it",
                        singleTop = true
                    )
                } else {
                    cameraState.rearmScanner()
                }
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
    snackbar: @Composable () -> Unit,
    boxesSearchText: TextFieldValue,
    itemsSearchText: TextFieldValue,
    cameraState: CameraDialogState,
    editSearchText: (SearchType, TextFieldValue) -> Unit,
    onScannedCode: (String) -> Unit
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

    val coroutineScope = rememberCoroutineScope()
    val keyboardVisible by keyboardAsState()
    var searchBarVisible by rememberSaveable {
        mutableStateOf(false)
    }
    var fabExpanded by rememberSaveable {
        mutableStateOf(false)
    }

    var searchBarText by remember {
        mutableStateOf(TextFieldValue())
    }
    val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    LaunchedEffect(navigationScreen) {
        cameraState.hide()
        coroutineScope.launch {
            if (searchBarVisible) {
                searchBarVisible = false
                delay(250)
            }
            fabExpanded = false
            searchBarText = when (navigationScreen) {
                NavigationScreens.BoxesScreen -> boxesSearchText
                NavigationScreens.ItemsScreen -> itemsSearchText
                else -> TextFieldValue()
            }
        }
    }

    BackHandler(
        enabled = fabExpanded || searchBarVisible
    ) {
        coroutineScope.launch {
            if (searchBarVisible) {
                searchBarVisible = false
                delay(250)
            }
            fabExpanded = false
        }
    }

    CameraDialog(
        state = cameraState,
        onScanned = onScannedCode
    )

    Scaffold(
        modifier = Modifier
            .navigationBarsPadding()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = snackbar,
        topBar = {
            Surface(
                shadowElevation = (scrollBehavior.state.overlappedFraction * 15).coerceAtMost(4f).dp,
            ) {
                TopAppBar(
                    modifier = if (!navigationScreen.isMainScreen) Modifier else Modifier.height(statusBarHeight),
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
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = navigationScreen in listOf(NavigationScreens.BoxesScreen, NavigationScreens.ItemsScreen) ||
                        fabVisible && navigationScreen in listOf(
                    NavigationScreens.BoxDetailsScreen, NavigationScreens.AddBoxScreen,
                    NavigationScreens.ItemDetailsScreen, NavigationScreens.AddItemScreen
                ),
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .imePadding(),
                    verticalAlignment = Alignment.Bottom
                ) {
                    AnimatedVisibility(
                        modifier = Modifier
                            .zIndex(1f)
                            .padding(bottom = if (keyboardVisible) 0.dp else 95.dp),
                        visible = searchBarVisible
                    ) {
                        Surface(
                            modifier = Modifier
                                .padding(start = 12.dp)
                                .padding(vertical = 12.dp)
                                .drawWithContent {
                                    val paddingPx = 12.dp.toPx()
                                    clipRect(
                                        left = -paddingPx,
                                        top = -paddingPx,
                                        bottom = size.height + paddingPx,
                                        right = size.width
                                    ) {
                                        this@drawWithContent.drawContent()
                                    }
                                },
                            color = FloatingActionButtonDefaults.containerColor,
                            shape = RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp),
                            shadowElevation = 4.dp
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.padding(6.dp),
                                value = searchBarText,
                                onValueChange = {
                                    searchBarText = it
                                    when (navigationScreen) {
                                        NavigationScreens.BoxesScreen -> editSearchText(SearchType.BOXES, it)
                                        NavigationScreens.ItemsScreen -> editSearchText(SearchType.ITEMS, it)
                                        else -> {}
                                    }
                                },
                                placeholder = { Text(text = stringResource(R.string.search)) },
                                keyboardOptions = KeyboardOptions(
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        coroutineScope.launch {
                                            searchBarVisible = false
                                            delay(250)
                                            fabExpanded = false
                                        }

                                    }
                                ),
                                trailingIcon = {
                                    if (searchBarText.text.isNotBlank()) {
                                        IconButton(
                                            onClick = {
                                                searchBarText = TextFieldValue()
                                                when (navigationScreen) {
                                                    NavigationScreens.BoxesScreen -> editSearchText(SearchType.BOXES, TextFieldValue())
                                                    NavigationScreens.ItemsScreen -> editSearchText(SearchType.ITEMS, TextFieldValue())
                                                    else -> {}
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = stringResource(R.string.delete)
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                    FloatingActionButton(
                        modifier = Modifier.zIndex(0f),
                        onClick = {
                            when (navigationScreen) {
                                NavigationScreens.BoxesScreen, NavigationScreens.ItemsScreen -> {
                                    coroutineScope.launch {
                                        if (searchBarVisible) {
                                            searchBarVisible = false
                                            delay(250)
                                        }
                                        fabExpanded = !fabExpanded
                                    }
                                }

                                NavigationScreens.BoxDetailsScreen, NavigationScreens.AddBoxScreen -> {
                                    sendSharedAction(SharedActions.SAVE_BOX)
                                    changeFabVisibility(false)
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
                                    Column {
                                        AnimatedVisibility(
                                            visible = fabExpanded
                                        ) {
                                            Column {
                                                Surface(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            if (searchBarVisible) {
                                                                searchBarVisible = false
                                                                delay(250)
                                                            }
                                                            fabExpanded = false
                                                            sendSharedAction(SharedActions.SCROLL_TO_TOP)
                                                        }
                                                    },
                                                    color = Color.Transparent,
                                                    shape = MaterialTheme.shapes.small
                                                ) {
                                                    Icon(
                                                        modifier = Modifier.padding(16.dp),
                                                        imageVector = Icons.Default.ArrowUpward,
                                                        contentDescription = ""
                                                    )
                                                }
                                                AnimatedVisibility(
                                                    visible = navigationScreen == NavigationScreens.BoxesScreen,
                                                    enter = fadeIn(tween(delayMillis = 300)) + expandVertically(tween(delayMillis = 300)),
                                                    exit = fadeOut(tween(delayMillis = 300)) + shrinkVertically(tween(delayMillis = 300))
                                                ) {
                                                    Surface(
                                                        onClick = {
                                                            coroutineScope.launch {
                                                                if (searchBarVisible) {
                                                                    searchBarVisible = false
                                                                    delay(250)
                                                                }
                                                                fabExpanded = false
                                                                cameraState.showScanner()
                                                            }
                                                        },
                                                        color = Color.Transparent,
                                                        shape = MaterialTheme.shapes.small
                                                    ) {
                                                        Icon(
                                                            modifier = Modifier.padding(16.dp),
                                                            imageVector = Icons.Default.QrCodeScanner,
                                                            contentDescription = stringResource(R.string.scanner)
                                                        )
                                                    }
                                                }
                                                Surface(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            searchBarVisible = !searchBarVisible
                                                        }
                                                    },
                                                    color = Color.Transparent,
                                                    shape = MaterialTheme.shapes.small
                                                ) {
                                                    Icon(
                                                        modifier = Modifier.padding(16.dp),
                                                        imageVector = if (searchBarText.text.isNotBlank()) Icons.Default.SavedSearch else Icons.Default.Search,
                                                        contentDescription = if (searchBarText.text.isNotBlank()) stringResource(R.string.edit_search) else stringResource(R.string.search2)
                                                    )
                                                }
                                                Surface(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            if (searchBarVisible) {
                                                                searchBarVisible = false
                                                                delay(250)
                                                            }
                                                            fabExpanded = false
                                                            when (navigationScreen) {
                                                                NavigationScreens.BoxesScreen -> {
                                                                    goToScreen(NavigationScreens.AddBoxScreen.route)
                                                                }

                                                                NavigationScreens.ItemsScreen -> {
                                                                    goToScreen(NavigationScreens.AddItemScreen.route)
                                                                }

                                                                else -> {}
                                                            }
                                                        }
                                                    },
                                                    color = Color.Transparent,
                                                    shape = MaterialTheme.shapes.small
                                                ) {
                                                    Icon(
                                                        modifier = Modifier.padding(16.dp),
                                                        imageVector = Icons.Default.Add,
                                                        contentDescription = stringResource(
                                                            id = if (it == NavigationScreens.BoxesScreen)
                                                                R.string.add_box
                                                            else
                                                                R.string.add_item
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                        AnimatedContent(targetState = fabExpanded) {
                                            Surface(color = Color.Transparent) {
                                                BadgedBox(
                                                    modifier = Modifier.padding(16.dp),
                                                    badge = {
                                                        androidx.compose.animation.AnimatedVisibility(
                                                            visible = searchBarText.text.isNotBlank() && !fabExpanded,
                                                            enter = fadeIn(),
                                                            exit = fadeOut()
                                                        ) {
                                                            Box(
                                                                modifier = Modifier
                                                                    .padding(bottom = 12.dp, end = 8.dp)
                                                                    .size(18.dp)
                                                                    .clip(CircleShape)
                                                                    .background(MaterialTheme.colorScheme.primary),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    modifier = Modifier.padding(2.dp),
                                                                    imageVector = Icons.Default.SavedSearch,
                                                                    tint = MaterialTheme.colorScheme.onPrimary,
                                                                    contentDescription = ""
                                                                )
                                                            }
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (it) Icons.Default.Close else Icons.Default.Menu,
                                                        contentDescription = if (it) stringResource(R.string.close_floating_menu) else stringResource(R.string.open_floating_menu)
                                                    )
                                                }
                                            }
                                        }
                                    }
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
            }
        },
        bottomBar = {
            if ((!keyboardVisible || !searchBarVisible) && bottomBarVisible && navigationScreen.isMainScreen) {
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
        Box {
            AppNavigationGraph(modifier = Modifier.padding(it))
            if (fabExpanded) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            coroutineScope.launch {
                                if (searchBarVisible) {
                                    searchBarVisible = false
                                    delay(250)
                                }
                                fabExpanded = false
                            }
                        },
                    color = Color.Transparent
                ) {}
            }
        }
    }
}