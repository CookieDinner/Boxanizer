package com.cookiedinner.boxanizer.boxes.viewmodels

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.viewModelScope
import com.cookiedinner.boxanizer.Box
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.core.data.DataProvider
import com.cookiedinner.boxanizer.core.utilities.safelyShowSnackbar
import com.cookiedinner.boxanizer.core.viewmodels.ViewModelWithSnack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BoxesViewModel(
    private val context: Application,
    private val dataProvider: DataProvider
) : ViewModelWithSnack() {
    private val _boxes = MutableStateFlow<SnapshotStateList<Box>?>(null)
    val boxes = _boxes.asStateFlow()

    val currentQuery = mutableStateOf("")

    fun getBoxes(query: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val boxes = dataProvider.getBoxes(query)
                withContext(Dispatchers.Main) {
                    _boxes.value = boxes.toMutableStateList()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                snackbarHostState.safelyShowSnackbar(context.getString(R.string.boxesError))
            }
        }
    }
}