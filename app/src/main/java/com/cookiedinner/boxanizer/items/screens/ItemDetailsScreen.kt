package com.cookiedinner.boxanizer.items.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.cookiedinner.boxanizer.database.BoxWithItem
import com.cookiedinner.boxanizer.database.Item
import com.cookiedinner.boxanizer.database.ItemTag
import com.cookiedinner.boxanizer.items.viewmodels.ItemDetailsViewModel
import kotlinx.coroutines.delay
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

    val tags = viewModel.tags.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        viewModel.setSnackbarHost(mainViewModel.snackbarHostState)
        viewModel.getItemDetails(itemId)
    }

    LaunchedEffect(currentItem.value, originalItem.value, nameError.value) {
        mainViewModel.changeFabVisibility(currentItem.value != originalItem.value && !nameError.value)
    }

    FlowObserver(mainViewModel.sharedActionListener) {
        if (it == SharedActions.SAVE_ITEM) {
            focusManager.clearFocus()
            viewModel.saveItem {
                navigator.popBackStack()
            }
        }
    }

    ItemDetailsScreenContent(
        item = currentItem.value,
        boxes = boxes.value,
        tags = tags.value,
        editItem = {
            viewModel.editCurrentItem(it)
        },
        nameError = nameError.value,
        onBoxClick = {
            navigator.navigateToScreen("${NavigationScreens.BoxDetailsScreen.route}?boxId=$it")
        },
        addTag = {
            viewModel.addTag(itemId, it)
        },
        deleteTag = {
            viewModel.removeTag(itemId, it)
        },
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
private fun ItemDetailsScreenContent(
    item: Item?,
    boxes: Map<BoxListType, List<BoxWithItem>>?,
    tags: List<ItemTag>?,
    editItem: (Item?) -> Unit,
    nameError: Boolean,
    onBoxClick: (Long) -> Unit,
    addTag: (String) -> Unit,
    deleteTag: (String) -> Unit
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
                Text(
                    modifier = Modifier.padding(bottom = 12.dp),
                    text = stringResource(R.string.general_info),
                    style = MaterialTheme.typography.titleMedium
                )
            }
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
            item {
                Row(
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .clip(MaterialTheme.shapes.extraSmall)
                        .clickable {
                            editItem(
                                item?.copy(
                                    consumable = !item.consumable
                                )
                            )
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item?.consumable ?: false,
                        onCheckedChange = {
                            editItem(
                                item?.copy(
                                    consumable = !item.consumable
                                )
                            )
                        }
                    )
                    Text(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        text = stringResource(R.string.consumable)
                    )
                }
            }
            item {
                if (tags != null) {
                    Column {
                        Text(
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                            text = stringResource(R.string.tags),
                            style = MaterialTheme.typography.titleMedium
                        )
                        FlowRow(
                            modifier = Modifier
                                .padding(end = 32.dp)
                                .animateContentSize()
                        ) {
                            tags.forEach {
                                InputChip(
                                    modifier = Modifier.padding(end = 10.dp),
                                    selected = false,
                                    onClick = {
                                        deleteTag(it.name)
                                    },
                                    label = {
                                        Text(
                                            modifier = Modifier
                                                .padding(vertical = 10.dp)
                                                .padding(start = 4.dp),
                                            text = it.name
                                        )
                                    },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.delete_tag),
                                        )
                                    }
                                )
                            }
                            var searchExpanded by remember {
                                mutableStateOf(false)
                            }
                            var searchText by remember {
                                mutableStateOf("")
                            }
                            val focusRequester = remember {
                                FocusRequester()
                            }
                            LaunchedEffect(searchExpanded) {
                                if (searchExpanded) {
                                    delay(200)
                                    focusRequester.requestFocus()
                                }
                            }
                            AnimatedContent(
                                targetState = searchExpanded
                            ) {
                                if (it) {
                                    val localStyle = LocalTextStyle.current
                                    val mergedStyle = localStyle.merge(TextStyle(color = LocalContentColor.current))
                                    BasicTextField(
                                        modifier = Modifier.focusRequester(focusRequester),
                                        value = searchText,
                                        onValueChange = {
                                            searchText = it
                                        },
                                        textStyle = mergedStyle,
                                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                        keyboardOptions = KeyboardOptions(
                                            imeAction = ImeAction.Done
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                searchExpanded = false
                                                if (searchText.isNotBlank()) {
                                                    addTag(searchText)
                                                }
                                                searchText = ""
                                            }
                                        ),
                                    ) { innerTextField ->
                                        InputChip(
                                            selected = false,
                                            onClick = {
                                                searchExpanded = false
                                                searchText = ""
                                            },
                                            trailingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = stringResource(R.string.close_search),
                                                )
                                            },
                                            label = {
                                                Box(
                                                    modifier = Modifier
                                                        .padding(bottom = 10.dp, top = 11.dp)
                                                        .padding(start = 4.dp),
                                                ) {
                                                    innerTextField()
                                                }
                                            }
                                        )
                                    }
                                } else {
                                    OutlinedIconButton(
                                        onClick = { searchExpanded = true },
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = stringResource(R.string.add_new_tag),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            item {
                if (boxes?.any { it.value.isNotEmpty() } == true) {
                    Text(
                        modifier = Modifier.padding(top = 16.dp),
                        text = stringResource(R.string.boxes),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
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