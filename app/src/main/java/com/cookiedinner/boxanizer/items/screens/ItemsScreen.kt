package com.cookiedinner.boxanizer.items.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.cookiedinner.boxanizer.core.navigation.Navigator
import com.cookiedinner.boxanizer.core.utilities.koinActivityViewModel
import com.cookiedinner.boxanizer.core.viewmodels.MainViewModel
import com.cookiedinner.boxanizer.items.viewmodels.ItemsViewModel
import org.koin.compose.koinInject

@Composable
fun ItemsScreen(
    navigator: Navigator = koinInject(),
    viewModel: ItemsViewModel = koinActivityViewModel(),
    mainViewModel: MainViewModel = koinActivityViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.setSnackbarHost(mainViewModel.snackbarHostState)
        viewModel.getItems()
    }
    ItemsScreenContent()
}

@Composable
private fun ItemsScreenContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

    }
}