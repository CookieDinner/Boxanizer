package com.cookiedinner.boxanizer.boxes.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.outlined.BorderColor
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import coil.size.Size
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.boxes.viewmodels.BoxDetailsViewModel
import com.cookiedinner.boxanizer.core.components.CameraDialog
import com.cookiedinner.boxanizer.core.components.CameraPhotoPhase
import com.cookiedinner.boxanizer.core.components.CameraType
import com.cookiedinner.boxanizer.core.navigation.Navigator
import com.cookiedinner.boxanizer.core.utilities.FlowObserver
import com.cookiedinner.boxanizer.core.utilities.koinActivityViewModel
import com.cookiedinner.boxanizer.core.viewmodels.MainViewModel
import com.cookiedinner.boxanizer.database.Box
import com.cookiedinner.boxanizer.database.Item
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun BoxDetailsScreen(
    boxId: Long,
    navigator: Navigator = koinInject(),
    viewModel: BoxDetailsViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinActivityViewModel()
) {
    val currentBox = viewModel.currentBox.collectAsStateWithLifecycle()
    val originalBox = viewModel.originalCurrentBox.collectAsStateWithLifecycle()

    val codeError = viewModel.codeError.collectAsStateWithLifecycle()
    val nameError = viewModel.nameError.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.setSnackbarHost(mainViewModel.snackbarHostState)
        viewModel.getBoxDetails(boxId)
    }

    LaunchedEffect(currentBox.value, originalBox.value, codeError.value, nameError.value) {
        mainViewModel.changeFabVisibility(currentBox.value != originalBox.value && codeError.value == 0 && !nameError.value)
    }

    FlowObserver(mainViewModel.sharedActionListener) {
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
        nameError = nameError.value,
    )
}

@Composable
private fun BoxDetailsScreenContent(
    box: Box?,
    items: List<Item>,
    editBox: (Box?) -> Unit,
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
                    box?.copy(
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
                                box?.copy(
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
                Box {
                    Column(
                        modifier = Modifier.height(180.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        HorizontalDivider()
                        HorizontalDivider()
                    }
                    Column(
                    ) {
                        HorizontalDivider()
                        HorizontalDivider()
                    }
                }
            }
        } else {
            {}
        }
    )

    val focusManager = LocalFocusManager.current

    Surface {
        LazyColumn(
            modifier = Modifier
                .imePadding()
                .fillMaxSize(),
            contentPadding = PaddingValues(
                start = 12.dp,
                end = 12.dp,
                top = 12.dp,
                bottom = 96.dp,
            )
        ) {
            item {
                var expanded by remember {
                    mutableStateOf(false)
                }
                Surface(
                    modifier = Modifier
                        .clickable(!photoLoading) {
                            if (box?.image != null) {
                                expanded = !expanded
                            } else {
                                cameraDialogType = CameraType.PHOTO
                                cameraDialogVisible = true
                            }
                        },
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.background,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    AnimatedContent(
                        targetState = when {
                            photoLoading || box == null -> 0
                            box.image == null -> 1
                            else -> 2
                        }
                    ) {
                        when (it) {
                            0 -> {
                                Box(
                                    modifier = Modifier
                                        .height(180.dp)
                                        .fillMaxWidth(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(Modifier.size(48.dp))
                                }
                            }

                            1 -> {
                                Box(
                                    modifier = Modifier.size(80.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AddAPhoto,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        contentDescription = stringResource(R.string.add_box_picture)
                                    )
                                }
                            }

                            else -> {
                                AnimatedContent(
                                    targetState = expanded,
                                    transitionSpec = {
                                        if (targetState) {
                                            (fadeIn(animationSpec = tween(220, 90)) +
                                                    scaleIn(initialScale = 0.92f, animationSpec = tween(220, 90)) +
                                                    slideInVertically(tween(220, 90)) { -it / 2 })
                                                .togetherWith(fadeOut(animationSpec = tween(90)))
                                        } else {
                                            (fadeIn(animationSpec = tween(220, 90)) +
                                                    scaleIn(initialScale = 0.92f, animationSpec = tween(220, 90)) +
                                                    slideInVertically(tween(220, 90)) { it / 2 })
                                                .togetherWith(fadeOut(animationSpec = tween(90)))
                                        }
                                    }
                                ) {
                                    Box(contentAlignment = Alignment.CenterEnd) {
                                        SubcomposeAsyncImage(
                                            modifier = Modifier
                                                .padding(4.dp)
                                                .then(if (it) Modifier else Modifier.height(180.dp))
                                                .fillMaxWidth()
                                                .clip(MaterialTheme.shapes.extraSmall),
                                            model = ImageRequest.Builder(LocalContext.current)
                                                .data(box?.image)
                                                .crossfade(true)
                                                .size(Size.ORIGINAL)
                                                .build(),
                                            contentScale = ContentScale.FillWidth,
                                            error = {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        modifier = Modifier.size(48.dp),
                                                        imageVector = Icons.Filled.ImageNotSupported,
                                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        contentDescription = stringResource(R.string.error)
                                                    )
                                                }
                                            },
                                            loading = {
                                                Box(
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CircularProgressIndicator(Modifier.size(48.dp))
                                                }
                                            },
                                            contentDescription = stringResource(R.string.box_picture)
                                        )
                                        Column(
                                            modifier = Modifier.matchParentSize(),
                                            verticalArrangement = Arrangement.SpaceBetween,
                                            horizontalAlignment = Alignment.End
                                        ) {
                                            OutlinedIconButton(
                                                modifier = Modifier.padding(4.dp),
                                                onClick = {
                                                    editBox(
                                                        box?.copy(
                                                            image = null
                                                        )
                                                    )
                                                },
                                                colors = IconButtonDefaults.filledIconButtonColors(
                                                    containerColor = MaterialTheme.colorScheme.background,
                                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Delete,
                                                    contentDescription = ""
                                                )
                                            }
                                            OutlinedIconButton(
                                                modifier = Modifier.padding(4.dp),
                                                onClick = {
                                                    cameraDialogType = CameraType.PHOTO
                                                    cameraDialogVisible = true
                                                },
                                                colors = IconButtonDefaults.filledIconButtonColors(
                                                    containerColor = MaterialTheme.colorScheme.background,
                                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                                ),
                                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.BorderColor,
                                                    contentDescription = ""
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    modifier = Modifier.padding(top = 8.dp),
                    value = box?.code ?: "",
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
                            box?.copy(
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
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )
            }
            item {
                OutlinedTextField(
                    modifier = Modifier.padding(top = 8.dp),
                    value = box?.name ?: "",
                    label = {
                        Text(text = stringResource(R.string.name))
                    },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text(text = stringResource(R.string.name_error_1)) }
                    } else null,
                    onValueChange = {
                        editBox(
                            box?.copy(
                                name = it
                            )
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )
            }
            item {
                OutlinedTextField(
                    modifier = Modifier.padding(top = 8.dp),
                    value = box?.description ?: "",
                    label = {
                        Text(text = stringResource(R.string.description))
                    },
                    onValueChange = {
                        editBox(
                            box?.copy(
                                description = it
                            )
                        )
                    }
                )
            }
        }
    }
}