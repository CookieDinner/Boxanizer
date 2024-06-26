package com.cookiedinner.boxanizer.items.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.core.components.ListSkeleton
import com.cookiedinner.boxanizer.core.components.keyboardAsState
import com.cookiedinner.boxanizer.core.models.SharedActions
import com.cookiedinner.boxanizer.core.navigation.NavigationScreens
import com.cookiedinner.boxanizer.core.navigation.Navigator
import com.cookiedinner.boxanizer.core.utilities.FlowObserver
import com.cookiedinner.boxanizer.core.utilities.koinActivityViewModel
import com.cookiedinner.boxanizer.core.viewmodels.MainViewModel
import com.cookiedinner.boxanizer.database.Item
import com.cookiedinner.boxanizer.items.components.ItemComponent
import com.cookiedinner.boxanizer.items.models.ItemListType
import com.cookiedinner.boxanizer.items.viewmodels.ItemsViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun ItemsScreen(
    navigator: Navigator = koinInject(),
    viewModel: ItemsViewModel = koinActivityViewModel(),
    mainViewModel: MainViewModel = koinActivityViewModel()
) {
    val items = viewModel.items.collectAsStateWithLifecycle()

    val searchText = mainViewModel.itemsSearchText.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    LaunchedEffect(searchText.value) {
        viewModel.getItems(searchText.value) {
            coroutineScope.launch {
                lazyListState.animateScrollToItem(0)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.setSnackbarHost(mainViewModel.snackbarHostState)
    }

    FlowObserver(mainViewModel.sharedActionListener) {
        if (it == SharedActions.SCROLL_TO_TOP) {
            lazyListState.animateScrollToItem(0)
        }
    }

    ItemsScreenContent(
        items = items.value,
        lazyListState = lazyListState,
        query = searchText.value,
        onItemClick = {
            navigator.navigateToScreen("${NavigationScreens.ItemDetailsScreen.route}?itemId=$it")
        },
        onItemDelete = {
            viewModel.deleteItem(it)
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ItemsScreenContent(
    items: Map<ItemListType, List<Item>>?,
    lazyListState: LazyListState,
    query: String,
    onItemClick: (Long) -> Unit,
    onItemDelete: (Long) -> Unit
) {
    val keyboardVisible by keyboardAsState()
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        when {
            items == null -> {
                ListSkeleton(itemHeight = 72.dp)
            }

            items.values.all { it.isEmpty() } -> {
                Box(
                    modifier = Modifier
                        .padding(64.dp)
                        .padding(bottom = if (keyboardVisible) 128.dp else 0.dp)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (query.isEmpty())
                            stringResource(R.string.empty_items)
                        else
                            stringResource(R.string.empty_items_query) + " \"$query\"",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(
                        top = 12.dp,
                        bottom = 96.dp,
                    ),
                ) {
                    items.forEach {
                        if (it.value.isNotEmpty()) {
                            stickyHeader(
                                key = "${it.key.name}_header"
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(horizontal = 12.dp)
                                        .padding(top = 8.dp)
                                ) {
                                    Text(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        text = when (it.key) {
                                            ItemListType.REMOVED -> stringResource(R.string.removed_from_boxes)
                                            ItemListType.IN_BOXES -> stringResource(R.string.in_boxes)
                                            ItemListType.REMAINING -> stringResource(R.string.not_in_any_boxes)
                                        },
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                    HorizontalDivider()
                                }
                            }
                            item(
                                key = "${it.key.name}_spacer"
                            ) {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                            items(
                                items = it.value,
                                key = {
                                    it.id
                                }
                            ) {
                                ItemComponent(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    item = it,
                                    onClick = {
                                        onItemClick(it.id)
                                    },
                                    onDelete = {
                                        onItemDelete(it.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}