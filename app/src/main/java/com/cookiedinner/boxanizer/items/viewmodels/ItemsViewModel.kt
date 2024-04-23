package com.cookiedinner.boxanizer.items.viewmodels

import android.app.Application
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewModelScope
import com.cookiedinner.boxanizer.Item
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.core.data.DataProvider
import com.cookiedinner.boxanizer.core.utilities.safelyShowSnackbar
import com.cookiedinner.boxanizer.core.viewmodels.ViewModelWithSnack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItemsViewModel(
    private val context: Application,
    private val dataProvider: DataProvider
) : ViewModelWithSnack() {
    private val _items = MutableStateFlow<SnapshotStateList<Item>?>(null)
    val items = _items.asStateFlow()

    fun getItems() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val items = dataProvider.getItems()
                withContext(Dispatchers.Main) {
                    _items.value = items.toMutableStateList()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                snackbarHostState.safelyShowSnackbar(context.getString(R.string.itemsError))
            }
        }
    }
}