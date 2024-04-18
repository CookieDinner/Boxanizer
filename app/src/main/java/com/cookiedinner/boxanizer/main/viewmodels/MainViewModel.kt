package com.cookiedinner.boxanizer.main.viewmodels

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cookiedinner.boxanizer.main.models.FabActions
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val _fabVisible = MutableStateFlow(true)
    val fabVisible = _fabVisible.asStateFlow()

    private val _fabActionListener = MutableSharedFlow<FabActions>(replay = 0)
    val fabActionListener = _fabActionListener.asSharedFlow()

    val snackbarHostState = SnackbarHostState()

    fun changeFabVisibility(visible: Boolean) {
        _fabVisible.value = visible
    }

    fun sendFabAction(fabAction: FabActions) {
        viewModelScope.launch {
            _fabVisible.value = false
            _fabActionListener.emit(fabAction)
        }
    }
}