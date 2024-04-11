package com.cookiedinner.boxanizer.main.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cookiedinner.boxanizer.core.data.DataStoreManager
import org.koin.compose.koinInject

@Composable
fun BoxesScreen() {
    BoxesScreenContent()
}

@Composable
private fun BoxesScreenContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

    }
}