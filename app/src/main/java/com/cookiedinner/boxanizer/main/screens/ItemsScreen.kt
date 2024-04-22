package com.cookiedinner.boxanizer.main.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ItemsScreen() {
    ItemsScreenContent()
}

@Composable
private fun ItemsScreenContent() {
    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {

    }
}