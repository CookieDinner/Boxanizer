package com.cookiedinner.boxanizer.main.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cookiedinner.boxanizer.Box
import com.cookiedinner.boxanizer.Item
import com.cookiedinner.boxanizer.core.navigation.Navigator
import com.cookiedinner.boxanizer.core.utilities.collectFlowOnLifecycle
import com.cookiedinner.boxanizer.core.utilities.koinActivityViewModel
import com.cookiedinner.boxanizer.main.viewmodels.BoxesViewModel
import com.cookiedinner.boxanizer.main.viewmodels.MainViewModel
import org.koin.compose.koinInject

@Composable
fun BoxDetailsScreen(
    boxId: Long,
    navigator: Navigator = koinInject(),
    viewModel: BoxesViewModel = koinActivityViewModel(),
    mainViewModel: MainViewModel = koinActivityViewModel()
) {
    val currentBox = viewModel.currentBox.collectAsStateWithLifecycle()
    val originalBox = viewModel.originalCurrentBox.collectAsStateWithLifecycle()
    val codeError = viewModel.codeError.collectAsStateWithLifecycle()

    LaunchedEffect(currentBox.value, originalBox.value, codeError.value) {
        mainViewModel.changeFabVisibility(currentBox.value != originalBox.value && !codeError.value)
    }
    LaunchedEffect(Unit) {
        viewModel.getBoxDetails(boxId)
    }
    mainViewModel.fabActionListener.collectFlowOnLifecycle {
        Log.d("Tests", "BoxDetailsScreen: ")
        viewModel.saveBox {
            navigator.popBackStack()
        }
    }

    BoxDetailsScreenContent(
        box = currentBox.value,
        items = listOf(),
        editBox = {
            viewModel.editCurrentBox(it)
        },
        codeError = codeError.value
    )
}

@Composable
private fun BoxDetailsScreenContent(
    box: Box,
    items: List<Item>,
    editBox: (Box) -> Unit,
    codeError: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp)
    ) {
        item {
            OutlinedTextField(
                modifier = Modifier
                    .height(120.dp)
                    .fillMaxWidth()
                    .clickable {
                        Log.d("Tests", "BoxDetailsScreenContent: Click")
                    },
                value = "",
                onValueChange = {},
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledBorderColor = OutlinedTextFieldDefaults.colors().unfocusedIndicatorColor,
                    disabledPlaceholderColor = OutlinedTextFieldDefaults.colors().unfocusedPlaceholderColor
                ),
                placeholder = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = "Add photo")
                    }
                },
            )
        }
        item {
            OutlinedTextField(
                modifier = Modifier.padding(top = 8.dp),
                value = box.code,
                label = {
                    Text(text = "Code")
                },
                isError = codeError,
                supportingText = if (codeError) {
                    { Text(text = "Code already exists") }
                } else null,
                onValueChange = {
                    editBox(
                        box.copy(
                            code = it
                        )
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scanner"
                        )
                    }
                }
            )
        }
        item {
            OutlinedTextField(
                modifier = Modifier.padding(top = 8.dp),
                value = box.name,
                label = {
                    Text(text = "Name")
                },
                onValueChange = {
                    editBox(
                        box.copy(
                            name = it
                        )
                    )
                }
            )
        }
        item {
            OutlinedTextField(
                modifier = Modifier.padding(top = 8.dp),
                value = box.description ?: "",
                label = {
                    Text(text = "Description")
                },
                onValueChange = {
                    editBox(
                        box.copy(
                            description = it
                        )
                    )
                }
            )
        }
    }
}