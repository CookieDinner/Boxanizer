package com.cookiedinner.boxanizer.boxes.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.cookiedinner.boxanizer.boxes.components.BoxComponent
import com.cookiedinner.boxanizer.boxes.viewmodels.BoxesViewModel
import com.cookiedinner.boxanizer.core.components.ListSkeleton
import com.cookiedinner.boxanizer.core.components.keyboardAsState
import com.cookiedinner.boxanizer.core.models.SharedActions
import com.cookiedinner.boxanizer.core.navigation.NavigationScreens
import com.cookiedinner.boxanizer.core.navigation.Navigator
import com.cookiedinner.boxanizer.core.utilities.FlowObserver
import com.cookiedinner.boxanizer.core.utilities.koinActivityViewModel
import com.cookiedinner.boxanizer.core.viewmodels.MainViewModel
import com.cookiedinner.boxanizer.database.Box
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun BoxesScreen(
    navigator: Navigator = koinInject(),
    viewModel: BoxesViewModel = koinActivityViewModel(),
    mainViewModel: MainViewModel = koinActivityViewModel()
) {
    val boxes = viewModel.boxes.collectAsStateWithLifecycle()

    val searchText = mainViewModel.boxesSearchText.collectAsStateWithLifecycle()

    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    LaunchedEffect(searchText.value) {
        viewModel.getBoxes(searchText.value) {
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

    BoxesScreenContent(
        boxes = boxes.value,
        lazyListState = lazyListState,
        query = searchText.value,
        onBoxClick = {
            navigator.navigateToScreen("${NavigationScreens.BoxDetailsScreen.route}?boxId=$it")
        },
        onBoxDelete = {
            viewModel.deleteBox(it)
        }
    )
}

@Composable
private fun BoxesScreenContent(
    boxes: List<Box>?,
    lazyListState: LazyListState,
    query: String,
    onBoxClick: (Long) -> Unit,
    onBoxDelete: (Long) -> Unit
) {
    val keyboardVisible by keyboardAsState()
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        when {
            boxes == null -> {
                ListSkeleton()
            }

            boxes.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .padding(64.dp)
                        .padding(bottom = if (keyboardVisible) 128.dp else 0.dp)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(

                        text = if (query.isEmpty())
                            stringResource(R.string.empty_boxes)
                        else
                            stringResource(R.string.empty_boxes_query) + " \"$query\"",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                LazyColumn(
                    state = lazyListState,
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 12.dp,
                        bottom = 96.dp,
                    ),
                ) {
                    items(
                        items = boxes,
                        key = {
                            it.id
                        }
                    ) {
                        BoxComponent(
                            box = it,
                            onClick = {
                                onBoxClick(it.id)
                            },
                            onDelete = {
                                onBoxDelete(it.id)
                            }
                        )
                    }
                }
            }
        }
    }
}