package com.cookiedinner.boxanizer.core.components

import android.Manifest
import android.content.res.Configuration
import android.graphics.Bitmap
import android.widget.LinearLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis.Analyzer
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.cookiedinner.boxanizer.core.data.DataStoreManager
import com.cookiedinner.boxanizer.core.models.CameraDialogState
import com.cookiedinner.boxanizer.core.models.CameraPhotoPhase
import com.cookiedinner.boxanizer.core.models.CameraType
import com.cookiedinner.boxanizer.core.utilities.BarcodeAnalyzer
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.koin.compose.koinInject
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

object CameraComponentDefaults {
    @Composable
    fun Overlay() {
        val dividerColor = MaterialTheme.colorScheme.surface
        Column(
            modifier = Modifier.height(180.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            HorizontalDivider(
                thickness = Dp.Hairline,
                color = dividerColor
            )
            HorizontalDivider(
                thickness = Dp.Hairline,
                color = dividerColor
            )
        }
        Row(
            modifier = Modifier.width(180.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            VerticalDivider(
                thickness = Dp.Hairline,
                color = dividerColor
            )
            VerticalDivider(
                thickness = Dp.Hairline,
                color = dividerColor
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun CameraDialog(
    state: CameraDialogState,
    onScanned: (String) -> Unit = {},
    takePhoto: (ByteArray?) -> Unit = {},
    dataStoreManager: DataStoreManager = koinInject(),
    overlay: @Composable BoxScope.() -> Unit = {}
) {
    val orientation = LocalConfiguration.current.orientation
    val scannableBarcodes = dataStoreManager.collectBarcodeTypesWithLifecycle()

    var canScan = remember {
        true
    }

    LaunchedEffect(state.visible, state.scannerFlag) {
        if (state.visible)
            canScan = true
    }
    AnimatedVisibility(
        visible = state.visible,
        enter = fadeIn(tween(50)),
        exit = fadeOut(tween(300, 300))
    ) {
        Dialog(
            onDismissRequest = {
                state.hide()
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false
            )
        ) {
            AnimatedVisibility(
                visible = state.visible && transition.currentState == EnterExitState.Visible,
                enter = fadeIn(tween(700)) + scaleIn(tween(700)),
                exit = fadeOut(tween(500)) + scaleOut(tween(700))
            ) {
                OutlinedCard(
                    modifier = Modifier
                        .padding(8.dp)
                        .then(
                            if (orientation == Configuration.ORIENTATION_PORTRAIT)
                                Modifier
                                    .fillMaxWidth(0.95f)
                                    .aspectRatio(3f / 4)
                            else
                                Modifier
                                    .fillMaxHeight(0.95f)
                                    .aspectRatio(4f / 3)
                        )
                ) {
                    if (state.visible && !transition.isRunning) {
                        Box(
                            modifier = Modifier.padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CameraComponent(
                                imageAnalyzer = if (state.type == CameraType.SCANNER) {
                                    BarcodeAnalyzer(
                                        barcodeListener = {
                                            canScan = false
                                            onScanned(it)
                                        },
                                        scannableBarcodes = scannableBarcodes.value,
                                        canScan = canScan
                                    )
                                } else null,
                                takePhoto = if (state.type == CameraType.PHOTO) {
                                    { phase, byteArray ->
                                        when (phase) {
                                            CameraPhotoPhase.TAKING -> {
                                                state.hide()
                                            }

                                            CameraPhotoPhase.DONE -> {
                                                takePhoto(byteArray)
                                            }

                                            CameraPhotoPhase.ERROR -> {
                                                //TODO Camera error handling
                                            }
                                        }
                                    }
                                } else null
                            )
                            overlay()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraComponent(
    modifier: Modifier = Modifier,
    imageAnalyzer: Analyzer? = null,
    takePhoto: ((CameraPhotoPhase, ByteArray?) -> Unit)? = null
) {
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    Surface(
        modifier = Modifier.then(modifier),
        shape = MaterialTheme.shapes.small
    ) {
        if (cameraPermissionState.status.isGranted) {
            CameraPreview(
                imageAnalyzer = imageAnalyzer,
                takePhoto = takePhoto
            )
        } else {
            NoPermissionScreen(cameraPermissionState::launchPermissionRequest)
        }
    }
}

@Composable
private fun CameraPreview(
    imageAnalyzer: Analyzer?,
    takePhoto: ((CameraPhotoPhase, ByteArray?) -> Unit)?
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    val cameraController = remember {
        LifecycleCameraController(context)
    }
    val cameraExecutor = remember {
        Executors.newSingleThreadExecutor()
    }
    Box(contentAlignment = Alignment.BottomCenter) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                PreviewView(context).apply {
                    layoutParams = LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT)
                    setBackgroundColor(android.graphics.Color.BLACK)
                    scaleType = PreviewView.ScaleType.FIT_CENTER
                }.also { previewView ->
                    cameraController.unbind()
                    cameraController.bindToLifecycle(lifecycleOwner)
                    cameraController.cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                    previewView.controller = cameraController
                    if (imageAnalyzer != null)
                        cameraController.setImageAnalysisAnalyzer(cameraExecutor, imageAnalyzer)
                }
            }
        )
        var photoTaken by rememberSaveable {
            mutableStateOf(false)
        }
        if (takePhoto != null) {
            FloatingActionButton(
                modifier = Modifier.padding(bottom = 16.dp),
                shape = CircleShape,
                onClick = {
                    if (!photoTaken) {
                        photoTaken = true
                        cameraController.takePicture(
                            cameraExecutor,
                            object : ImageCapture.OnImageCapturedCallback() {
                                override fun onCaptureSuccess(imageProxy: ImageProxy) {
                                    super.onCaptureSuccess(imageProxy)
                                    takePhoto(CameraPhotoPhase.TAKING, null)
                                    val bitmap = imageProxy.toBitmap()
                                    val byteStream = ByteArrayOutputStream()
                                    val aspectRatio = bitmap.width.toFloat() / bitmap.height
                                    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, (800 * aspectRatio).toInt(), 800, true)
                                    resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteStream)
                                    bitmap.recycle()
                                    resizedBitmap.recycle()
                                    imageProxy.close()
                                    takePhoto(CameraPhotoPhase.DONE, byteStream.toByteArray())
                                }

                                override fun onError(exception: ImageCaptureException) {
                                    super.onError(exception)
                                    exception.printStackTrace()
                                    takePhoto(CameraPhotoPhase.ERROR, null)
                                }
                            }
                        )
                    }
                }
            ) {
                AnimatedContent(
                    targetState = photoTaken
                ) {
                    if (it) {
                        CircularProgressIndicator()
                    } else {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = ""
                        )
                    }
                }
            }
        }
    }
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