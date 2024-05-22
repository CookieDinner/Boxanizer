package com.cookiedinner.boxanizer.core.utilities

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.cookiedinner.boxanizer.core.data.DataStoreManager
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer(
    private val barcodeListener: (String) -> Unit,
    private val scannableBarcodes: Set<DataStoreManager.BarcodeTypes>,
    private val canScan: Boolean
) : ImageAnalysis.Analyzer {
    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (canScan) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                val firstBarcodeType = scannableBarcodes.first()
                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(
                        firstBarcodeType.code,
                        *(scannableBarcodes.minusElement(firstBarcodeType).map { it.code }.toIntArray())
                    )
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
                    .addOnFailureListener {
                        imageProxy.close()
                        image.mediaImage?.close()
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                        image.mediaImage?.close()
                    }

            }
        }
    }
}