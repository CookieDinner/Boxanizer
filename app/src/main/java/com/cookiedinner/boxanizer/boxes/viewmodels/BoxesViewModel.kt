package com.cookiedinner.boxanizer.boxes.viewmodels

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.core.data.DataProvider
import com.cookiedinner.boxanizer.core.utilities.safelyShowSnackbar
import com.cookiedinner.boxanizer.core.viewmodels.ViewModelWithSnack
import com.cookiedinner.boxanizer.database.Box
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BoxesViewModel(
    private val context: Application,
    private val dataProvider: DataProvider
) : ViewModelWithSnack() {
    private val _boxes = MutableStateFlow<List<Box>?>(null)
    val boxes = _boxes.asStateFlow()

    fun getBoxes(
        query: String = "",
        callback: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val boxes = dataProvider.getBoxes(query)
                withContext(Dispatchers.Main) {
                    _boxes.value = boxes
                }
                callback()
            } catch (ex: Exception) {
                ex.printStackTrace()
                snackbarHostState.safelyShowSnackbar(context.getString(R.string.boxes_error))
            }
        }
    }

    fun deleteBox(boxId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                dataProvider.deleteBox(boxId)
                _boxes.update { boxes ->
                    boxes?.filterNot { it.id == boxId }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                snackbarHostState.safelyShowSnackbar(context.getString(R.string.box_delete_error))
            }
        }
    }
}