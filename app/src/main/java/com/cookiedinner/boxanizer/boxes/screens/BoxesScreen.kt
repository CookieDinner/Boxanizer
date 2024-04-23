package com.cookiedinner.boxanizer.boxes.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cookiedinner.boxanizer.Box
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.boxes.components.BoxComponent
import com.cookiedinner.boxanizer.boxes.viewmodels.BoxesViewModel
import com.cookiedinner.boxanizer.core.components.ListSkeleton
import com.cookiedinner.boxanizer.core.navigation.NavigationScreens
import com.cookiedinner.boxanizer.core.navigation.Navigator
import com.cookiedinner.boxanizer.core.utilities.koinActivityViewModel
import com.cookiedinner.boxanizer.core.viewmodels.MainViewModel
import org.koin.compose.koinInject

@Composable
fun BoxesScreen(
    navigator: Navigator = koinInject(),
    viewModel: BoxesViewModel = koinActivityViewModel(),
    mainViewModel: MainViewModel = koinActivityViewModel()
) {
    val boxes = viewModel.boxes.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.setSnackbarHost(mainViewModel.snackbarHostState)
        viewModel.getBoxes()
    }
    BoxesScreenContent(
        boxes = boxes.value,
        query = viewModel.currentQuery.value,
        onBoxClick = {
            navigator.navigateToScreen("${NavigationScreens.BoxDetailsScreen.route}?boxId=$it")
        }
    )
}

@Composable
private fun BoxesScreenContent(
    boxes: List<Box>?,
    query: String,
    onBoxClick: (Long) -> Unit
) {
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
                        .padding(16.dp)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (query.isEmpty())
                            stringResource(R.string.empty_boxes)
                        else
                            stringResource(R.string.empty_boxes_query),
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 12.dp,
                        bottom = 96.dp,
                    ),
                ) {
                    items(boxes) {
                        BoxComponent(
                            box = it,
                            onClick = {
                                onBoxClick(it.id)
                            }
                        )
                    }
                }
            }
        }
    }
}