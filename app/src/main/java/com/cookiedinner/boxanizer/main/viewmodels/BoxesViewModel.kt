package com.cookiedinner.boxanizer.main.viewmodels

import android.app.Application
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookiedinner.boxanizer.Box
import com.cookiedinner.boxanizer.R
import com.cookiedinner.boxanizer.core.data.DataProvider
import com.cookiedinner.boxanizer.core.models.emptyBox
import com.cookiedinner.boxanizer.core.utilities.safelyShowSnackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BoxesViewModel(
    private val context: Application,
    private val dataProvider: DataProvider
) : ViewModel() {
    private val _boxes = MutableStateFlow<SnapshotStateList<Box>?>(null)
    val boxes = _boxes.asStateFlow()

    private val _currentBox = MutableStateFlow(emptyBox)
    val currentBox = _currentBox.asStateFlow()

    private val _codeError = MutableStateFlow(0)
    val codeError = _codeError.asStateFlow()

    private val _nameError = MutableStateFlow(false)
    val nameError = _nameError.asStateFlow()

    private val _originalCurrentBox = MutableStateFlow(emptyBox)
    val originalCurrentBox = _originalCurrentBox.asStateFlow()

    val currentQuery = mutableStateOf("")

    private lateinit var snackbarHostState: SnackbarHostState

    init {
        getBoxes(currentQuery.value)
    }

    fun setSnackbarHost(snackbarHostState: SnackbarHostState) {
        this.snackbarHostState = snackbarHostState
    }

    fun getBoxes(query: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _boxes.value = dataProvider.getBoxes(query).toMutableStateList()
            } catch (ex: Exception) {
                ex.printStackTrace()
                snackbarHostState.safelyShowSnackbar(context.getString(R.string.boxesError))
            }
        }
    }

    fun getBoxDetails(boxId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _codeError.value = 0
                _nameError.value = false
                _currentBox.value = if (boxId == -1L) emptyBox else dataProvider.getBoxDetails(boxId)
                _originalCurrentBox.value = _currentBox.value
            } catch (ex: Exception) {
                ex.printStackTrace()
                snackbarHostState.safelyShowSnackbar(context.getString(R.string.boxDetailsError))
            }
        }
    }

    private var checkBoxJob: Job? = null
    fun editCurrentBox(box: Box) {
        _currentBox.value = box
        checkBoxJob?.cancel()
        checkBoxJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                _nameError.value = box.name.isBlank()
                val codeBox = dataProvider.getBoxByCode(box.code)
                _codeError.value = when {
                    box.code.isBlank() -> 1
                    codeBox != null && codeBox.id != box.id && codeBox.code == box.code -> 2
                    else -> 0
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }

    fun saveBox(callback: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newBox = dataProvider.saveBox(_currentBox.value)
                _originalCurrentBox.value = newBox
                _currentBox.value = newBox
                var mapped = false
                _boxes.value = _boxes.value?.map {
                    if (it.id == newBox.id) {
                        mapped = true
                        _currentBox.value
                    } else it
                }?.toMutableStateList()
                if (!mapped)
                    _boxes.value?.add(newBox)
                viewModelScope.launch(Dispatchers.Main) {
                    callback()
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                snackbarHostState.safelyShowSnackbar(context.getString(R.string.boxSaveError))
            }
        }
    }
}