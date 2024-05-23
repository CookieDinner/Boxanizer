package com.cookiedinner.boxanizer.items.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.core.data.DataProvider
import com.cookiedinner.boxanizer.core.utilities.safelyShowSnackbar
import com.cookiedinner.boxanizer.core.viewmodels.ViewModelWithSnack
import com.cookiedinner.boxanizer.database.Item
import com.cookiedinner.boxanizer.items.models.ItemListType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItemsViewModel(
    private val context: Application,
    private val dataProvider: DataProvider
) : ViewModelWithSnack() {
    private val _items = MutableStateFlow<Map<ItemListType, List<Item>>?>(null)
    val items = _items.asStateFlow()

    var previousSearchQuery = ""
    fun getItems(
        query: String = "",
        callback: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val items = dataProvider.getItems(query)
                withContext(Dispatchers.Main) {
                    _items.value = items
                }
                if (previousSearchQuery != query) {
                    callback()
                    previousSearchQuery = query
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                snackbarHostState.safelyShowSnackbar(context.getString(R.string.items_error))
            }
        }
    }

    fun deleteItem(itemId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataProvider.deleteItem(itemId)
                _items.update { items ->
                    items?.mapValues {
                        it.value.filterNot { it.id == itemId }
                    }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                snackbarHostState.safelyShowSnackbar(context.getString(R.string.item_delete_error))
            }
        }
    }
}