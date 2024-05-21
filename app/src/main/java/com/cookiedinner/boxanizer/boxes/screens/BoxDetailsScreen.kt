package com.cookiedinner.boxanizer.boxes.screens

import android.util.Log
import android.view.KeyEvent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.boxes.viewmodels.BoxDetailsViewModel
import com.cookiedinner.boxanizer.core.components.CameraComponentDefaults
import com.cookiedinner.boxanizer.core.components.CameraDialog
import com.cookiedinner.boxanizer.core.components.CameraImage
import com.cookiedinner.boxanizer.core.models.CameraPhotoPhase
import com.cookiedinner.boxanizer.core.models.CameraType
import com.cookiedinner.boxanizer.core.models.InputErrorType
import com.cookiedinner.boxanizer.core.models.SharedActions
import com.cookiedinner.boxanizer.core.models.rememberCameraDialogState
import com.cookiedinner.boxanizer.core.navigation.NavigationScreens
import com.cookiedinner.boxanizer.core.navigation.Navigator
import com.cookiedinner.boxanizer.core.utilities.FlowObserver
import com.cookiedinner.boxanizer.core.utilities.koinActivityViewModel
import com.cookiedinner.boxanizer.core.viewmodels.MainViewModel
import com.cookiedinner.boxanizer.database.Box
import com.cookiedinner.boxanizer.database.ItemInBox
import com.cookiedinner.boxanizer.items.components.ItemComponent
import com.cookiedinner.boxanizer.items.models.ItemAction
import com.cookiedinner.boxanizer.items.models.ItemInBoxWithTransition
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
        onItemEdited = viewModel::editItemInBox,
        onItemClick = {
            navigator.navigateToScreen("${NavigationScreens.ItemDetailsScreen.route}?itemId=$it")
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BoxDetailsScreenContent(
    box: Box?,
    items: Map<ItemListType, List<ItemInBoxWithTransition>>?,
    editBox: (Box?) -> Unit,
    codeError: InputErrorType,
    nameError: Boolean,
    onItemEdited: (Long, ItemAction, () -> Unit) -> Unit,
    onItemClick: (Long) -> Unit
) {
    val cameraState = rememberCameraDialogState()

    CameraDialog(
        state = cameraState,
        onScanned = { code ->
            editBox(
                box?.copy(
                    code = code
                )
            )
        },
        takePhoto = { byteArray ->
            if (byteArray != null) {
                editBox(
                    box?.copy(
                        image = byteArray
                    )
                )
            }
        },
        overlay = {
            if (cameraState.type == CameraType.PHOTO) {
                CameraComponentDefaults.Overlay()
            }
        }
    )

    val focusManager = LocalFocusManager.current
    val lazyListState = rememberLazyListState()

    Surface {
        LazyColumn(
            modifier = Modifier
                .imePadding()
                .fillMaxSize(),
            state = lazyListState,
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
                    photoLoading = cameraState.takingPhoto || box == null,
                    onEditImage = {
                        cameraState.showPhoto()
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
                                cameraState.showScanner()
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
                items.forEach { itemGroup ->
                    if (itemGroup.value.isNotEmpty()) {
                        stickyHeader(
                            key = "${itemGroup.key.name}_header"
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(top = 8.dp)
                            ) {
                                Text(
                                    modifier = Modifier.padding(vertical = 12.dp),
                                    text = when (itemGroup.key) {
                                        ItemListType.REMOVED -> stringResource(R.string.removed_from_this_box)
                                        else -> stringResource(R.string.in_this_box)
                                    },
                                    style = MaterialTheme.typography.titleMedium
                                )
                                HorizontalDivider()
                            }
                        }
                        itemsIndexed(
                            items = itemGroup.value,
                            key = { _, it ->
                                it.item.id
                            }
                        ) {index, it ->
                            AnimatedVisibility(
                                visibleState = it.transitionState.apply { targetState = true },
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                ItemComponent(
                                    modifier = Modifier
                                        .padding(top = if (index == 0) 12.dp else 0.dp)
                                        .padding(horizontal = 3.dp),
                                    itemInBox = it.item,
                                    onClick = {
                                        onItemClick(it.item.id)
                                    },
                                    onDelete = {
                                        onItemEdited(it.item.id, ItemAction.DELETE) {}
                                    },
                                    onBorrowed = { callback ->
                                        onItemEdited(it.item.id, ItemAction.BORROW, callback)
                                    },
                                    onReturned = { callback ->
                                        onItemEdited(it.item.id, ItemAction.RETURN, callback)
                                    },
                                    onAdded = { callback ->
                                        onItemEdited(it.item.id, ItemAction.ADD, callback)
                                    },
                                    onRemoved = { callback ->
                                        onItemEdited(it.item.id, ItemAction.REMOVE, callback)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}