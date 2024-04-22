package com.cookiedinner.boxanizer.main.screens

import android.content.res.Configuration
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.camera.view.LifecycleCameraController
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.RemoveCircle
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cookiedinner.boxanizer.Box
import com.cookiedinner.boxanizer.Item
import com.cookiedinner.boxanizer.core.navigation.Navigator
import com.cookiedinner.boxanizer.core.utilities.BarcodeAnalyzer
import com.cookiedinner.boxanizer.core.utilities.collectFlowOnLifecycle
import com.cookiedinner.boxanizer.core.utilities.koinActivityViewModel
import com.cookiedinner.boxanizer.main.components.CameraComponent
import com.cookiedinner.boxanizer.main.viewmodels.BoxesViewModel
import com.cookiedinner.boxanizer.main.viewmodels.MainViewModel
import kotlinx.coroutines.delay
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

    var initialized by rememberSaveable {
        mutableStateOf(false)
    }
    LaunchedEffect(Unit) {
        if (!initialized) {
            viewModel.getBoxDetails(boxId)
            initialized = true
        }
    }

    LaunchedEffect(currentBox.value, originalBox.value, codeError.value) {
        mainViewModel.changeFabVisibility(currentBox.value != originalBox.value && !codeError.value)
    }

    mainViewModel.fabActionListener.collectFlowOnLifecycle {
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
        codeError = codeError.value,
    )
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun BoxDetailsScreenContent(
    box: Box,
    items: List<Item>,
    editBox: (Box) -> Unit,
    codeError: Boolean
) {
    var cameraDialogVisible by rememberSaveable {
        mutableStateOf(false)
    }
    val orientation = LocalConfiguration.current.orientation

    BackHandler(
        enabled = cameraDialogVisible
    ) {
        cameraDialogVisible = false

    }

    Box(
        contentAlignment = Alignment.Center
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
                        IconButton(
                            onClick = {
                                cameraDialogVisible = true
                            }
                        ) {
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

        AnimatedVisibility(
            visible = cameraDialogVisible,
            enter = fadeIn(tween(50)),
            exit = fadeOut(tween(300, 300))
        ) {
            Dialog(
                onDismissRequest = { cameraDialogVisible = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false
                )
            ) {
                AnimatedVisibility(
                    visible = cameraDialogVisible && transition.currentState == EnterExitState.Visible,
                    enter = fadeIn(tween(700)) + scaleIn(tween(700)),
                    exit = fadeOut(tween(500)) + scaleOut(tween(700))
                ) test@{
                    OutlinedCard(
                        modifier = Modifier
                            .padding(8.dp)
                            .then(
                                if (orientation == Configuration.ORIENTATION_PORTRAIT)
                                    Modifier
                                        .fillMaxWidth(0.95f)
                                        .aspectRatio(3f / 4)
                                else
                                    Modifier
                                        .fillMaxHeight(0.95f)
                                        .aspectRatio(4f / 3)
                            )
                    ) {
                        if (cameraDialogVisible && !transition.isRunning) {
                            CameraComponent(
                                imageAnalyzer = BarcodeAnalyzer { barcode ->
                                    cameraDialogVisible = false
                                    editBox(
                                        box.copy(
                                            code = barcode
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}