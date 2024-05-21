package com.cookiedinner.boxanizer.items.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.core.data.DataProvider
import com.cookiedinner.boxanizer.core.models.emptyItem
import com.cookiedinner.boxanizer.core.utilities.safelyShowSnackbar
import com.cookiedinner.boxanizer.core.viewmodels.ViewModelWithSnack
import com.cookiedinner.boxanizer.database.Item
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItemDetailsViewModel(
    private val context: Application,
    private val dataProvider: DataProvider
): ViewModelWithSnack() {
    private val _currentItem = MutableStateFlow<Item?>(null)
    val currentItem = _currentItem.asStateFlow()

    private val _originalCurrentItem = MutableStateFlow<Item?>(null)
    val originalCurrentItem = _originalCurrentItem.asStateFlow()

    private val _nameError = MutableStateFlow(false)
    val nameError = _nameError.asStateFlow()


    private var initialized = false
    fun getItemDetails(itemId: Long) {
        if (!initialized) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val itemDetails = if (itemId == -1L) emptyItem else dataProvider.getItemDetails(itemId)
                    withContext(Dispatchers.Main) {
                        _nameError.value = false
                        _currentItem.value = itemDetails
                        _originalCurrentItem.value = itemDetails
                    }
                    initialized = true
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    snackbarHostState.safelyShowSnackbar(context.getString(R.string.item_details_error))
                }
            }
        }
    }

    private var checkItemJob: Job? = null
    fun editCurrentItem(item: Item?) {
        _currentItem.value = item
        checkItemJob?.cancel()
        checkItemJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                if (item != null) {
                    _nameError.value = item.name.isBlank()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun saveItem(callback: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _currentItem.update {
                    val newItem = if (it != null) dataProvider.saveItem(it) else null
                    withContext(Dispatchers.Main) {
                        _originalCurrentItem.value = newItem
                        callback()
                    }
                    newItem
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                snackbarHostState.safelyShowSnackbar(context.getString(R.string.item_save_error))
            }
        }
    }
}