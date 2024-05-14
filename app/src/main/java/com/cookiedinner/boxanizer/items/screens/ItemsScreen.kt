package com.cookiedinner.boxanizer.items.screens

import android.util.Log
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
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.boxes.components.BoxComponent
import com.cookiedinner.boxanizer.core.components.ListSkeleton
import com.cookiedinner.boxanizer.core.navigation.Navigator
import com.cookiedinner.boxanizer.core.utilities.koinActivityViewModel
import com.cookiedinner.boxanizer.core.viewmodels.MainViewModel
import com.cookiedinner.boxanizer.database.Item
import com.cookiedinner.boxanizer.items.components.ItemComponent
import com.cookiedinner.boxanizer.items.models.ItemListType
import com.cookiedinner.boxanizer.items.viewmodels.ItemsViewModel
import org.koin.compose.koinInject

@Composable
fun ItemsScreen(
    navigator: Navigator = koinInject(),
    viewModel: ItemsViewModel = koinActivityViewModel(),
    mainViewModel: MainViewModel = koinActivityViewModel()
) {
    val items = viewModel.items.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.setSnackbarHost(mainViewModel.snackbarHostState)
        viewModel.getItems()
    }

    ItemsScreenContent(
        items = items.value,
        onItemClick = {

        }
    )
}

@Composable
private fun ItemsScreenContent(
    items: Map<ItemListType, List<Item>>?,
    onItemClick: (Long) -> Unit
) {
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
                        .padding(16.dp)
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.empty_items),
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
                    items.forEach {
                        item {
                            Text(text = it.key.name)
                        }
                        items(
                            items = it.value,
                            key = {
                                it.id
                            }
                        ) {
                            ItemComponent() //TODO: properly fill component
                        }
                    }
                }
            }
        }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

    }
}