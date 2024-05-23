package com.cookiedinner.boxanizer.boxes.viewmodels

import android.app.Application
import androidx.compose.animation.core.MutableTransitionState
import androidx.lifecycle.viewModelScope
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.core.data.DataProvider
import com.cookiedinner.boxanizer.core.models.InputErrorType
import com.cookiedinner.boxanizer.core.models.emptyBox
import com.cookiedinner.boxanizer.core.utilities.safelyShowSnackbar
import com.cookiedinner.boxanizer.core.viewmodels.ViewModelWithSnack
import com.cookiedinner.boxanizer.database.Box
import com.cookiedinner.boxanizer.items.models.ItemAction
import com.cookiedinner.boxanizer.items.models.ItemInBoxWithTransition
import com.cookiedinner.boxanizer.items.models.ItemListType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BoxDetailsViewModel(
    private val context: Application,
    private val dataProvider: DataProvider
) : ViewModelWithSnack() {
    private val _currentBox = MutableStateFlow<Box?>(null)
    val currentBox = _currentBox.asStateFlow()

    private val _originalCurrentBox = MutableStateFlow<Box?>(null)
    val originalCurrentBox = _originalCurrentBox.asStateFlow()

    private val _codeError = MutableStateFlow(InputErrorType.NONE)
    val codeError = _codeError.asStateFlow()

    private val _nameError = MutableStateFlow(false)
    val nameError = _nameError.asStateFlow()

    private val _items = MutableStateFlow<Map<ItemListType, List<ItemInBoxWithTransition>>?>(null)
    val items = _items.asStateFlow()

    fun getBoxDetails(boxId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val boxDetails = if (boxId == -1L) emptyBox else dataProvider.getBoxDetails(boxId)
                withContext(Dispatchers.Main) {
                    _codeError.value = InputErrorType.NONE
                    _nameError.value = false
                    _currentBox.value = boxDetails
                    _originalCurrentBox.value = boxDetails
                }
                if (boxId != -1L) {
                    val boxItems = dataProvider.getBoxItems(boxId)
                    withContext(Dispatchers.Main) {
                        _items.value = boxItems.mapValues {
                            it.value.map { ItemInBoxWithTransition(it) }
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                snackbarHostState.safelyShowSnackbar(context.getString(R.string.box_details_error))
            }
        }
    }

    private var checkBoxJob: Job? = null
    fun editCurrentBox(box: Box?) {
        _currentBox.value = box
        checkBoxJob?.cancel()
        checkBoxJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                if (box != null) {
                    val codeBox = dataProvider.getBoxByCode(box.code)
                    withContext(Dispatchers.Main) {
                        _nameError.value = box.name.isBlank()
                        _codeError.value = when {
                            box.code.isBlank() -> InputErrorType.EMPTY
                            codeBox != null && codeBox.id != box.id && codeBox.code == box.code -> InputErrorType.ALREADY_EXISTS
                            else -> InputErrorType.NONE
                        }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun saveBox(callback: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _currentBox.update {
                    val newBox = if (it != null) dataProvider.saveBox(it) else null
                    withContext(Dispatchers.Main) {
                        _originalCurrentBox.value = newBox
                        if (it?.id == -1L)
                            callback()
                    }
                    newBox
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                snackbarHostState.safelyShowSnackbar(context.getString(R.string.box_save_error))
            }
        }
    }

    fun editItemInBox(
        itemId: Long,
        action: ItemAction,
        callback: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _items.update { map ->
                    val itemToMove = map?.values?.flatten()?.first { it.item.id == itemId } ?: throw Exception()
                    dataProvider.editItemInBox(itemId, _currentBox.value?.id ?: -1, action, itemToMove.item)
                    when (action) {
                        /**
                         * When the item, upon taking it out of the box, has to move between sections, we initialize the exit animation, wait a moment
                         * and then remove it from its previous section. Afterwards it gets added to the updated section with its amountRemovedFromBox changed accordingly to
                         * the action.
                         * When it doesn't have to move sections, we just simply increment/decrement the counter.
                         */
                        ItemAction.BORROW, ItemAction.RETURN -> {
                            when {
                                action == ItemAction.BORROW && itemToMove.item.amountRemovedFromBox == 0L -> map.mapValues {
                                    when (it.key) {
                                        ItemListType.IN_BOXES -> {
                                            itemToMove.transitionState.targetState = false
                                            delay(200)
                                            it.value.filterNot { itemInBox -> itemInBox.item.id == itemId }
                                        }

                                        else -> listOf(
                                            ItemInBoxWithTransition(
                                                item = itemToMove.item.copy(amountRemovedFromBox = 1),
                                                transitionState = MutableTransitionState(false)
                                            )
                                        ) + it.value
                                    }
                                }

                                action == ItemAction.RETURN && itemToMove.item.amountRemovedFromBox == 1L -> map.mapValues {
                                    when (it.key) {
                                        ItemListType.REMOVED -> {
                                            itemToMove.transitionState.targetState = false
                                            delay(200)
                                            it.value.filterNot { itemInBox -> itemInBox.item.id == itemId }
                                        }

                                        else -> listOf(
                                            ItemInBoxWithTransition(
                                                item = itemToMove.item.copy(amountRemovedFromBox = 0),
                                                transitionState = MutableTransitionState(false)
                                            )
                                        ) + it.value
                                    }
                                }

                                else -> map.mapValues {
                                    it.value.map { itemInBox ->
                                        if (itemInBox.item.id == itemId) {
                                            itemInBox.copy(
                                                item = itemInBox.item.copy(
                                                    amountRemovedFromBox = itemInBox.item.amountRemovedFromBox + when (action) {
                                                        ItemAction.BORROW -> 1
                                                        else -> -1
                                                    }
                                                )
                                            )
                                        } else
                                            itemInBox
                                    }
                                }
                            }
                        }

                        ItemAction.ADD, ItemAction.REMOVE -> {
                            if (action == ItemAction.REMOVE && itemToMove.item.amountInBox == 1L) {
                                map
                            } else {
                                map.mapValues {
                                    it.value.map { itemInBox ->
                                        if (itemInBox.item.id == itemId) {
                                            itemInBox.copy(
                                                item = itemInBox.item.copy(
                                                    amountInBox = itemInBox.item.amountInBox + when (action) {
                                                        ItemAction.ADD -> 1
                                                        else -> -1
                                                    }
                                                )
                                            )
                                        } else
                                            itemInBox
                                    }
                                }
                            }
                        }

                        ItemAction.DELETE -> {
                            map.mapValues {
                                it.value.filterNot { it.item.id == itemId }
                            }
                        }
                    }
                }
                callback()
            } catch (ex: Exception) {
                ex.printStackTrace()
                callback()
                snackbarHostState.safelyShowSnackbar(
                    when (action) {
                        ItemAction.BORROW -> context.getString(R.string.item_borrow_error)
                        ItemAction.RETURN -> context.getString(R.string.item_return_error)
                        ItemAction.REMOVE -> context.getString(R.string.item_remove_error)
                        ItemAction.ADD -> context.getString(R.string.item_add_error)
                        ItemAction.DELETE -> context.getString(R.string.item_delete_box_error)
                    }
                )
            }
        }
    }
}