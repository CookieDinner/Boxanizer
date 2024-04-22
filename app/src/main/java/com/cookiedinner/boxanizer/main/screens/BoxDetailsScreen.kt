package com.cookiedinner.boxanizer.main.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.cookiedinner.boxanizer.Box
import com.cookiedinner.boxanizer.Item
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.core.navigation.Navigator
import com.cookiedinner.boxanizer.core.utilities.FlowObserver
import com.cookiedinner.boxanizer.core.utilities.koinActivityViewModel
import com.cookiedinner.boxanizer.main.components.CameraDialog
import com.cookiedinner.boxanizer.main.components.CameraPhotoPhase
import com.cookiedinner.boxanizer.main.components.CameraType
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
    val nameError = viewModel.nameError.collectAsStateWithLifecycle()

    var initialized by rememberSaveable {
        mutableStateOf(false)
    }
    LaunchedEffect(Unit) {
        if (!initialized) {
            viewModel.getBoxDetails(boxId)
            initialized = true
        }
    }

    LaunchedEffect(currentBox.value, originalBox.value, codeError.value, nameError.value) {
        mainViewModel.changeFabVisibility(currentBox.value != originalBox.value && codeError.value == 0 && !nameError.value)
    }

    FlowObserver(mainViewModel.fabActionListener) {
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
        nameError = nameError.value
    )
}

@Composable
private fun BoxDetailsScreenContent(
    box: Box,
    items: List<Item>,
    editBox: (Box) -> Unit,
    codeError: Int,
    nameError: Boolean
) {
    var cameraDialogVisible by rememberSaveable {
        mutableStateOf(false)
    }
    var cameraDialogType by rememberSaveable {
        mutableStateOf(CameraType.SCANNER)
    }
    var photoLoading by remember {
        mutableStateOf(false)
    }

    CameraDialog(
        visible = cameraDialogVisible,
        onDismissRequest = { cameraDialogVisible = false },
        onScanned = if (cameraDialogType == CameraType.SCANNER) {
            { code ->
                cameraDialogVisible = false
                editBox(
                    box.copy(
                        code = code
                    )
                )
            }
        } else null,
        takePhoto = if (cameraDialogType == CameraType.PHOTO) {
            { phase, byteArray ->
                when (phase) {
                    CameraPhotoPhase.TAKING -> {
                        cameraDialogVisible = false
                        photoLoading = true
                    }

                    CameraPhotoPhase.DONE -> {
                        if (byteArray != null) {
                            editBox(
                                box.copy(
                                    image = byteArray
                                )
                            )
                        }
                        photoLoading = false
                    }

                    CameraPhotoPhase.ERROR -> {
                        photoLoading = false
                    }
                }
            }
        } else null,
        overlay = if (cameraDialogType == CameraType.PHOTO) {
            {
                Column(
                    modifier = Modifier.height(180.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    HorizontalDivider()
                    HorizontalDivider()
                }
            }
        } else {
            {}
        }
    )

    Box(
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier
                        .height(180.dp)
                        .fillMaxWidth()
                        .clickable(!photoLoading) {
                            cameraDialogType = CameraType.PHOTO
                            cameraDialogVisible = true
                        },
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.background,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            photoLoading -> CircularProgressIndicator(Modifier.size(48.dp))
                            box.image == null -> {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Icons.Default.AddAPhoto, contentDescription = "Add photo")
                                }
                            }

                            else -> {
                                SubcomposeAsyncImage(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .fillMaxSize()
                                        .clip(MaterialTheme.shapes.extraSmall),
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(box.image)
                                        .crossfade(true)
                                        .size(Size.ORIGINAL)
                                        .build(),
                                    contentScale = ContentScale.FillWidth,
                                    error = {
                                        Icon(
                                            imageVector = Icons.Filled.ImageNotSupported,
                                            tint = Color.LightGray,
                                            contentDescription = "Error"
                                        )
                                    },
                                    loading = {
                                        Box(
                                            modifier = Modifier.fillMaxSize(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(Modifier.size(48.dp))
                                        }
                                    },
                                    contentDescription = ""
                                )
                            }
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    modifier = Modifier.padding(top = 8.dp),
                    value = box.code,
                    label = {
                        Text(text = stringResource(id = R.string.code))
                    },
                    isError = codeError != 0,
                    supportingText = when (codeError) {
                        1 -> {
                            { Text(text = stringResource(R.string.code_error_1)) }
                        }

                        2 -> {
                            { Text(text = stringResource(R.string.code_error_2)) }
                        }

                        else -> null
                    },
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
                                cameraDialogType = CameraType.SCANNER
                                cameraDialogVisible = true
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = stringResource(R.string.scanner)
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
                        Text(text = stringResource(R.string.name))
                    },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text(text = stringResource(R.string.name_error_1)) }
                    } else null,
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
                        Text(text = stringResource(R.string.description))
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
}