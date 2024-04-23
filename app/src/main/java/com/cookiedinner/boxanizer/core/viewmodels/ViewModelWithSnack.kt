package com.cookiedinner.boxanizer.core.viewmodels

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel

open class ViewModelWithSnack : ViewModel() {
    lateinit var snackbarHostState: SnackbarHostState

    fun setSnackbarHost(snackbarHostState: SnackbarHostState) {
        if (!this::snackbarHostState.isInitialized) {
            this.snackbarHostState = snackbarHostState
        }
    }
}