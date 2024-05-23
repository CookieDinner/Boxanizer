package com.cookiedinner.boxanizer.core.viewmodels

import android.app.Application
import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookiedinner.boxanizer.core.data.DataProvider
import com.cookiedinner.boxanizer.core.models.SearchType
import com.cookiedinner.boxanizer.core.models.SharedActions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(
    private val context: Application,
    private val dataProvider: DataProvider
) : ViewModel() {
    private val _fabVisible = MutableStateFlow(true)
    val fabVisible = _fabVisible.asStateFlow()

    private val _bottomBarVisible = MutableStateFlow(true)
    val bottomBarVisible = _bottomBarVisible.asStateFlow()

    private val _sharedActionListener = MutableSharedFlow<SharedActions>(replay = 0)
    val sharedActionListener = _sharedActionListener.asSharedFlow()

    val snackbarHostState = SnackbarHostState()

    var rawBoxesSearchText = TextFieldValue()
    private val _boxesSearchText = MutableStateFlow("")
    val boxesSearchText = _boxesSearchText.asStateFlow()

    var rawItemsSearchText = TextFieldValue()
    private val _itemsSearchText = MutableStateFlow("")
    val itemsSearchText = _itemsSearchText.asStateFlow()

    fun changeFabVisibility(visible: Boolean) {
        _fabVisible.value = visible
    }

    fun changeBottomBarVisibility(visible: Boolean) {
        _bottomBarVisible.value = visible
    }

    fun sendSharedAction(sharedAction: SharedActions) {
        viewModelScope.launch {
            _sharedActionListener.emit(sharedAction)
        }
    }

    private var job: Job? = null
    fun editSearch(
        searchType: SearchType,
        newText: TextFieldValue
    ) {
        job?.cancel()
        job = viewModelScope.launch {
            when (searchType) {
                SearchType.BOXES -> {
                    rawBoxesSearchText = newText
                    delay(300)
                    _boxesSearchText.value = newText.text
                }

                SearchType.ITEMS -> {
                    rawItemsSearchText = newText
                    delay(300)
                    _itemsSearchText.value = newText.text
                }
            }
        }
    }

    fun findBoxIdByCode(
        code: String,
        callback: (Long?) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val foundBox = dataProvider.getBoxByCode(code)
                withContext(Dispatchers.Main) {
                    callback(foundBox?.id)
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }
    }
}