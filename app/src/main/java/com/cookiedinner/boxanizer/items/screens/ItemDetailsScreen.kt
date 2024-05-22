package com.cookiedinner.boxanizer.items.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.ImageNotSupported
import androidx.compose.material.icons.outlined.BorderColor
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.cookiedinner.boxanizer.boxes.components.BoxComponent
import com.cookiedinner.boxanizer.boxes.models.BoxListType
import com.cookiedinner.boxanizer.core.components.CameraComponentDefaults
import com.cookiedinner.boxanizer.core.components.CameraDialog
import com.cookiedinner.boxanizer.core.components.CameraImage
import com.cookiedinner.boxanizer.core.models.SharedActions
import com.cookiedinner.boxanizer.core.models.rememberCameraDialogState
import com.cookiedinner.boxanizer.core.navigation.NavigationScreens
import com.cookiedinner.boxanizer.core.navigation.Navigator
import com.cookiedinner.boxanizer.core.utilities.FlowObserver
import com.cookiedinner.boxanizer.core.utilities.koinActivityViewModel
import com.cookiedinner.boxanizer.core.viewmodels.MainViewModel
import com.cookiedinner.boxanizer.database.Box
import com.cookiedinner.boxanizer.database.BoxWithItem
import com.cookiedinner.boxanizer.database.Item
import com.cookiedinner.boxanizer.items.models.ItemListType
import com.cookiedinner.boxanizer.items.viewmodels.ItemDetailsViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun ItemDetailsScreen(
    itemId: Long,
    navigator: Navigator = koinInject(),
    viewModel: ItemDetailsViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinActivityViewModel()
) {
    val currentItem = viewModel.currentItem.collectAsStateWithLifecycle()
    val originalItem = viewModel.originalCurrentItem.collectAsStateWithLifecycle()

    val nameError = viewModel.nameError.collectAsStateWithLifecycle()

    val boxes = viewModel.boxes.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.setSnackbarHost(mainViewModel.snackbarHostState)
        viewModel.getItemDetails(itemId)
    }

    LaunchedEffect(currentItem.value, originalItem.value, nameError.value) {
        mainViewModel.changeFabVisibility(currentItem.value != originalItem.value && !nameError.value)
    }

    FlowObserver(mainViewModel.sharedActionListener) {
        if (it == SharedActions.SAVE_ITEM) {
            viewModel.saveItem {
                navigator.popBackStack()
            }
        }
    }

    ItemDetailsScreenContent(
        item = currentItem.value,
        boxes = boxes.value,
        editItem = {
            viewModel.editCurrentItem(it)
        },
        nameError = nameError.value,
        onBoxClick = {
            navigator.navigateToDeeperScreen("${NavigationScreens.BoxDetailsScreen.route}?boxId=$it")
        }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ItemDetailsScreenContent(
    item: Item?,
    boxes: Map<BoxListType, List<BoxWithItem>>?,
    editItem: (Item?) -> Unit,
    nameError: Boolean,
    onBoxClick: (Long) -> Unit
) {
    val cameraState = rememberCameraDialogState()

    CameraDialog(
        state = cameraState,
        takePhoto = { byteArray ->
            if (byteArray != null) {
                editItem(
                    item?.copy(
                        image = byteArray
                    )
                )
            }
        },
        overlay = {
            CameraComponentDefaults.Overlay()
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
                    image = item?.image,
                    photoLoading = cameraState.takingPhoto || item == null,
                    onEditImage = {
                        cameraState.showPhoto()
                    },
                    onDeleteImage = {
                        editItem(
                            item?.copy(
                                image = null
                            )
                        )
                    },
                    addImageLabel = stringResource(R.string.add_item_picture),
                    imageLabel = stringResource(R.string.item_picture)
                )
            }
            item {
                OutlinedTextField(
                    modifier = Modifier.padding(top = 8.dp),
                    value = item?.name ?: "",
                    label = {
                        Text(text = stringResource(R.string.name))
                    },
                    isError = nameError,
                    supportingText = if (nameError) {
                        { Text(text = stringResource(R.string.name_error_1)) }
                    } else null,
                    onValueChange = {
                        editItem(
                            item?.copy(
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
                    value = item?.description ?: "",
                    label = {
                        Text(text = stringResource(R.string.description))
                    },
                    onValueChange = {
                        editItem(
                            item?.copy(
                                description = it
                            )
                        )
                    }
                )
            }
            boxes?.forEach { boxGroup ->
                if (boxGroup.value.isNotEmpty()) {
                    stickyHeader(
                        key = "${boxGroup.key.name}_header"
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.background)
                                .padding(top = 8.dp)
                        ) {
                            Text(
                                modifier = Modifier.padding(vertical = 12.dp),
                                text = when (boxGroup.key) {
                                    BoxListType.REMOVED_FROM -> stringResource(R.string.removed_from)
                                    else -> stringResource(R.string.in_boxes)
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                            HorizontalDivider()
                        }
                    }
                    itemsIndexed(
                        items = boxGroup.value,
                        key = { _, it ->
                            it.id
                        }
                    ) { index, it ->
                        BoxComponent(
                            modifier = Modifier
                                .padding(top = if (index == 0) 6.dp else 0.dp)
                                .padding(horizontal = 3.dp),
                            boxWithItem = it,
                            onClick = {
                                onBoxClick(it.id)
                            }
                        )
                    }
                }
            }
        }
    }
}