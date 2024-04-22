package com.cookiedinner.boxanizer.main.components

import android.Manifest
import android.content.Context
import android.hardware.display.DisplayManager
import android.util.Log
import android.view.Display
import android.view.View
import android.widget.LinearLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis.Analyzer
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraComponent(
    modifier: Modifier = Modifier.padding(4.dp),
    imageAnalyzer: Analyzer? = null
) {
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small
    ) {
        if (cameraPermissionState.status.isGranted) {
            CameraPreview(
                imageAnalyzer = imageAnalyzer
            )
        } else {
            NoPermissionScreen(cameraPermissionState::launchPermissionRequest)
        }
    }
}

@Composable
private fun CameraPreview(
    imageAnalyzer: Analyzer?
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val cameraController = remember {
        LifecycleCameraController(context)
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = {
            PreviewView(context).apply {
                layoutParams = LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT)
                setBackgroundColor(android.graphics.Color.BLACK)
                scaleType = PreviewView.ScaleType.FILL_CENTER
            }.also { previewView ->
                cameraController.unbind()
                cameraController.bindToLifecycle(lifecycleOwner)
                cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                previewView.controller = cameraController
                if (imageAnalyzer != null)
                    cameraController.setImageAnalysisAnalyzer(ContextCompat.getMainExecutor(context), imageAnalyzer)
            }
        }
    )
}

@Composable
private fun NoPermissionScreen(askForPermission: () -> Unit) {
    LaunchedEffect(Unit) {
        askForPermission()
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Brak uprawnień do korzystania z kamery",
            style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
        )
    }
}