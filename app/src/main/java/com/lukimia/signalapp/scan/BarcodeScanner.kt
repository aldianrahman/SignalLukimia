package com.lukimia.signalapp.scan

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class BarcodeScanner(
    private val context: Context
) {
    private val scanner = BarcodeScanning.getClient()

    fun scanImage(image: InputImage): Task<List<Barcode>> {
        return scanner.process(image)
    }
}