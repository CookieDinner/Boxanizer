package com.cookiedinner.boxanizer.core.viewmodels

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookiedinner.boxanizer.core.models.SharedActions
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _fabVisible = MutableStateFlow(true)
    val fabVisible = _fabVisible.asStateFlow()

    private val _bottomBarVisible = MutableStateFlow(true)
    val bottomBarVisible = _bottomBarVisible.asStateFlow()

    private val _sharedActionListener = MutableSharedFlow<SharedActions>(replay = 0)
    val sharedActionListener = _sharedActionListener.asSharedFlow()

    val snackbarHostState = SnackbarHostState()

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
}