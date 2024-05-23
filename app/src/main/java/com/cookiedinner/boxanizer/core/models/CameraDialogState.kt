package com.cookiedinner.boxanizer.core.models

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue


@Composable
fun rememberCameraDialogState(
    initialCameraType: CameraType = CameraType.PHOTO
): CameraDialogState {
    return remember {
        CameraDialogState(initialCameraType)
    }

}

class CameraDialogState(
    initialCameraType: CameraType
) {
    var visible by mutableStateOf(false)
    var type by mutableStateOf(initialCameraType)
    var takingPhoto by mutableStateOf(false)
    var scannerFlag by mutableStateOf(false)

    fun showPhoto() {
        type = CameraType.PHOTO
        visible = true
    }

    fun showScanner() {
        type = CameraType.SCANNER
        visible = true
    }

    fun showPreview() {
        type = CameraType.PREVIEW
        visible = true
    }

    fun hide() {
        visible = false
    }

    fun rearmScanner() {
        scannerFlag = !scannerFlag
    }
}

enum class CameraType {
    PREVIEW,
    SCANNER,
    PHOTO
}

enum class CameraPhotoPhase {
    TAKING,
    DONE,
    ERROR
}