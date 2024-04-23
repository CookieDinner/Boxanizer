package com.cookiedinner.boxanizer.core.utilities

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer(
    private val barcodeListener: (String) -> Unit
) : ImageAnalysis.Analyzer {
    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val options = BarcodeScannerOptions.Builder()
                .enableAllPotentialBarcodes()
                .build()
            val barcodeScanner = BarcodeScanning.getClient(options)
            barcodeScanner.process(image)
                .addOnSuccessListener { barcodes ->
                    if (barcodes.isNotEmpty()) {
                        val barcode = barcodes.first().rawValue
                        if (!barcode.isNullOrBlank()) {
                            barcodeListener(barcode)
                        }
                    }
                }
                .addOnFailureListener { }
                .addOnCompleteListener {
                    imageProxy.close()
                    image.mediaImage?.close()
                }

        }
    }
}