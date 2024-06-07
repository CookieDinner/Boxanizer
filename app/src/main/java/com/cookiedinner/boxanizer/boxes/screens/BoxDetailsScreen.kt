package com.cookiedinner.boxanizer.boxes.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.boxes.viewmodels.BoxDetailsViewModel
import com.cookiedinner.boxanizer.core.components.CameraComponentDefaults
import com.cookiedinner.boxanizer.core.components.CameraDialog
import com.cookiedinner.boxanizer.core.components.CameraImage
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
import com.cookiedinner.boxanizer.database.Item
import com.cookiedinner.boxanizer.items.components.ItemComponent
import com.cookiedinner.boxanizer.items.models.ItemAction
import com.cookiedinner.boxanizer.items.models.ItemForQueryInBox
import com.cookiedinner.boxanizer.items.models.ItemInBoxWithTransition
import com.cookiedinner.boxanizer.items.models.ItemListType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
fun BoxDetailsScreen(
    boxId: Long,
    itemId: Long? = null,
    navigator: Navigator = koinInject(),
    viewModel: BoxDetailsViewModel = koinViewModel(),
    mainViewModel: MainViewModel = koinActivityViewModel()
) {
    val currentBox = viewModel.currentBox.collectAsStateWithLifecycle()
    val originalBox = viewModel.originalCurrentBox.collectAsStateWithLifecycle()

    val codeError = viewModel.codeError.collectAsStateWithLifecycle()
    val nameError = viewModel.nameError.collectAsStateWithLifecycle()

    val items = viewModel.items.collectAsStateWithLifecycle()

    val searchItems = viewModel.searchItems.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()
    val lazyListState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.setSnackbarHost(mainViewModel.snackbarHostState)
        viewModel.getBoxDetails(boxId, itemId) { indexInItemList ->
            coroutineScope.launch {
                lazyListState.animateScrollToItem(indexInItemList + 1)
            }
        }
    }

    LaunchedEffect(currentBox.value, originalBox.value, codeError.value, nameError.value) {
        mainViewModel.changeFabVisibility(currentBox.value != originalBox.value && codeError.value == InputErrorType.NONE && !nameError.value)
    }

    FlowObserver(mainViewModel.sharedActionListener) {
        if (it == SharedActions.SAVE_BOX) {
            focusManager.clearFocus()
            viewModel.saveBox {
                navigator.popBackStack()
            }
        }
    }

    BoxDetailsScreenContent(
        box = currentBox.value,
        items = items.value,
        lazyListState = lazyListState,
        searchItems = searchItems.value,
        editBox = {
            viewModel.editCurrentBox(it)
        },
        codeError = codeError.value,
        nameError = nameError.value,
        onItemEdited = viewModel::editItemInBox,
        onItemClick = {
            navigator.navigateToScreen("${NavigationScreens.ItemDetailsScreen.route}?itemId=$it")
        },
        searchForItems = viewModel::searchForItems,
        addItem = viewModel::addItem,
        highlightedItemId = itemId
    )
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun BoxDetailsScreenContent(
    box: Box?,
    items: Map<ItemListType, List<ItemInBoxWithTransition>>?,
    lazyListState: LazyListState,
    searchItems: List<ItemForQueryInBox>,
    editBox: (Box?) -> Unit,
    codeError: InputErrorType,
    nameError: Boolean,
    onItemEdited: (Long, ItemAction, Long, () -> Unit) -> Unit,
    onItemClick: (Long) -> Unit,
    searchForItems: (String) -> Unit,
    addItem: (Item) -> Unit,
    highlightedItemId: Long?
) {
    val cameraState = rememberCameraDialogState()

    CameraDialog(
        state = cameraState,
        onScanned = { code ->
            cameraState.hide()
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

    var highlightedItem by remember(highlightedItemId) {
        mutableStateOf(highlightedItemId != null)
    }

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
                Column {
                    Text(
                        modifier = Modifier.padding(bottom = 12.dp),
                        text = stringResource(R.string.general_info),
                        style = MaterialTheme.typography.titleMedium
                    )
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
            }
            item {
                Column {
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
                    if (box?.id != -1L) {
                        Text(
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                            text = stringResource(R.string.items),
                            style = MaterialTheme.typography.titleMedium
                        )
                        var searchExpanded by remember {
                            mutableStateOf(false)
                        }
                        var searchText by remember {
                            mutableStateOf("")
                        }
                        val focusRequester = remember {
                            FocusRequester()
                        }
                        var dropdownVisibilityOverride by remember {
                            mutableStateOf(false)
                        }
                        LaunchedEffect(searchExpanded) {
                            if (searchExpanded) {
                                delay(200)
                                focusRequester.requestFocus()
                            } else {
                                searchForItems("")
                            }
                        }
                        AnimatedContent(
                            targetState = searchExpanded
                        ) {
                            if (it) {
                                ExposedDropdownMenuBox(
                                    expanded = searchText.isNotBlank() && dropdownVisibilityOverride,
                                    onExpandedChange = {}
                                ) {
                                    OutlinedTextField(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                            .focusRequester(focusRequester),
                                        value = searchText,
                                        onValueChange = {
                                            dropdownVisibilityOverride = true
                                            searchText = it
                                            searchForItems(it)
                                        },
                                        placeholder = { Text(text = stringResource(R.string.add_item) + "...") },
                                        singleLine = true,
                                        trailingIcon = {
                                            IconButton(
                                                onClick = {
                                                    searchText = ""
                                                    searchExpanded = false
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = stringResource(R.string.close_search)
                                                )
                                            }
                                        }
                                    )
                                    ExposedDropdownMenu(
                                        expanded = searchText.isNotBlank() && dropdownVisibilityOverride,
                                        onDismissRequest = { dropdownVisibilityOverride = false },
                                    ) {
                                        val extraItem = if (searchText.isNotBlank() && searchItems.none { it.name == searchText })
                                            listOf(ItemForQueryInBox(id = -1, name = searchText, description = null, image = null, consumable = false, alreadyInBox = false))
                                        else
                                            listOf()
                                        (extraItem + searchItems).forEach {
                                            val item = Item(it.id, it.name, it.description, it.image, it.consumable, 0)
                                            ItemComponent(
                                                item = item,
                                                onClick = {
                                                    addItem(item)
                                                    searchText = ""
                                                    searchExpanded = false
                                                }
                                            )
                                        }
                                    }
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
            items?.forEach { itemGroup ->
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
                    ) { index, it ->
                        val shouldHighlight = highlightedItemId == it.item.id
                        AnimatedVisibility(
                            visibleState = it.transitionState.apply { targetState = true },
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            ItemComponent(
                                modifier = Modifier
                                    .padding(top = if (index == 0) 6.dp else 0.dp)
                                    .padding(horizontal = 3.dp),
                                itemInBox = it.item,
                                onClick = {
                                    if (shouldHighlight)
                                        highlightedItem = false
                                    onItemClick(it.item.id)
                                },
                                onAction = { action, amount, callback ->
                                    if (shouldHighlight)
                                        highlightedItem = false
                                    if (action != null)
                                        onItemEdited(it.item.id, action, amount, callback)
                                },
                                highlighted = highlightedItem && highlightedItemId == it.item.id
                            )
                        }
                    }
                }
            }
        }
    }
}