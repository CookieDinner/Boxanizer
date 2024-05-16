package com.cookiedinner.boxanizer.boxes.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.core.models.InputErrorType
import com.cookiedinner.boxanizer.core.data.DataProvider
import com.cookiedinner.boxanizer.core.models.emptyBox
import com.cookiedinner.boxanizer.core.utilities.safelyShowSnackbar
import com.cookiedinner.boxanizer.core.viewmodels.ViewModelWithSnack
import com.cookiedinner.boxanizer.database.Box
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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

    private var initialized = false

    fun getBoxDetails(boxId: Long) {
        if (!initialized) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val boxDetails = if (boxId == -1L) emptyBox else dataProvider.getBoxDetails(boxId)
                    withContext(Dispatchers.Main) {
                        _codeError.value = InputErrorType.NONE
                        _nameError.value = false
                        _currentBox.value = boxDetails
                        _originalCurrentBox.value = boxDetails
                    }
                    initialized = true
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    snackbarHostState.safelyShowSnackbar(context.getString(R.string.box_details_error))
                }
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
}