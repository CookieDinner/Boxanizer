package com.cookiedinner.boxanizer.boxes.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.cookiedinner.boxanizer.core.models.InputErrorType
import com.cookiedinner.boxanizer.boxes.viewmodels.BoxDetailsViewModel
import com.cookiedinner.boxanizer.core.components.CameraDialog
import com.cookiedinner.boxanizer.core.components.CameraImage
import com.cookiedinner.boxanizer.core.components.CameraPhotoPhase
import com.cookiedinner.boxanizer.core.components.CameraType
import com.cookiedinner.boxanizer.core.models.SharedActions
import com.cookiedinner.boxanizer.core.navigation.Navigator
import com.cookiedinner.boxanizer.core.utilities.FlowObserver
import com.cookiedinner.boxanizer.core.utilities.koinActivityViewModel
import com.cookiedinner.boxanizer.core.viewmodels.MainViewModel
import com.cookiedinner.boxanizer.database.Box
import com.cookiedinner.boxanizer.database.Item
import com.cookiedinner.boxanizer.database.ItemInBox
import com.cookiedinner.boxanizer.items.components.ItemComponent
import com.cookiedinner.boxanizer.items.models.ItemListType
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

    val items = viewModel.items.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.setSnackbarHost(mainViewModel.snackbarHostState)
        viewModel.getBoxDetails(boxId)
    }

    LaunchedEffect(currentBox.value, originalBox.value, codeError.value, nameError.value) {
        mainViewModel.changeFabVisibility(currentBox.value != originalBox.value && codeError.value == InputErrorType.NONE && !nameError.value)
    }

    FlowObserver(mainViewModel.sharedActionListener) {
        if (it == SharedActions.SAVE_BOX) {
            viewModel.saveBox {
                navigator.popBackStack()
            }
        }
    }

    BoxDetailsScreenContent(
        box = currentBox.value,
        items = items.value,
        editBox = {
            viewModel.editCurrentBox(it)
        },
        codeError = codeError.value,
        nameError = nameError.value,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BoxDetailsScreenContent(
    box: Box?,
    items: Map<ItemListType, List<ItemInBox>>?,
    editBox: (Box?) -> Unit,
    codeError: InputErrorType,
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
                CameraImage(
                    image = box?.image,
                    photoLoading = photoLoading || box == null,
                    onEditImage = {
                        cameraDialogType = CameraType.PHOTO
                        cameraDialogVisible = true
                    },
                    onDeleteImage = {
                        editBox(
                            box?.copy(
                                image = null
                            )
                        )
                    },
                    addImageLabel = stringResource(R.string.add_box_picture),
                    imageLabel = stringResource(R.string.box_picture)
                )
            }
            item {
                OutlinedTextField(
                    modifier = Modifier.padding(top = 8.dp),
                    value = box?.code ?: "",
                    label = {
                        Text(text = stringResource(id = R.string.code))
                    },
                    isError = codeError != InputErrorType.NONE,
                    supportingText = when (codeError) {
                        InputErrorType.EMPTY -> {
                            { Text(text = stringResource(R.string.code_error_1)) }
                        }

                        InputErrorType.ALREADY_EXISTS -> {
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
            if (items == null) {

            } else {
                items.forEach {
                    if (it.value.isNotEmpty()) {
                        stickyHeader {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(top = 8.dp)
                            ) {
                                Text(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    text = when (it.key) {
                                        ItemListType.REMOVED -> stringResource(R.string.removed_from_this_box)
                                        else -> stringResource(R.string.in_this_box)
                                    },
                                    style = MaterialTheme.typography.titleMedium
                                )
                                HorizontalDivider()
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        items(
                            items = it.value,
                            key = {
                                it.id
                            }
                        ) {
                            ItemComponent(
                                modifier = Modifier.padding(horizontal = 3.dp),
                                itemInBox = it,
                                onClick = { /*TODO*/ },
                                onDelete = { /*TODO*/ },
                                onAdded = { /*TODO*/ },
                                onRemoved = { /*TODO*/ },
                                onBorrowed = { /*TODO*/ },
                                onReturned = { /*TODO*/ }
                            )
                        }
                    }
                }
            }
        }
    }
}