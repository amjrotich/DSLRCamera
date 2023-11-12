package com.cradleshyft.dslrcamera.analyzer;

import android.annotation.SuppressLint;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

public class BarcodeAnalyzer implements ImageAnalysis.Analyzer {

    public interface BarcodeListener {
        void onBarcodeScanComplete(String result);

        void onBarcodeScanFailure(Exception e);
    }

    private final BarcodeListener barcodeListener;

    public BarcodeAnalyzer(BarcodeListener barcodeListener) {
        this.barcodeListener = barcodeListener;
    }

    @SuppressLint("UnsafeOptInUsageError")
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        Image image = imageProxy.getImage();
        if (image != null) {
            scanBarcode(imageProxy);
        }
    }

    /**
     * scan barcode and notify result
     * all barcode formats enabled
     */
    @SuppressLint("UnsafeOptInUsageError")
    private void scanBarcode(ImageProxy imageProxy) {
        InputImage inputImage = InputImage.fromMediaImage(imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees());

        BarcodeScannerOptions options = new BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_ALL_FORMATS)
                .build();

        BarcodeScanner scanner = BarcodeScanning.getClient(options);

        scanner.process(inputImage)
                .addOnSuccessListener(barcodes -> {
                    for (Barcode barcode : barcodes) {
                        barcodeListener.onBarcodeScanComplete(barcode.getRawValue());
                    }
                })
                .addOnFailureListener(barcodeListener::onBarcodeScanFailure)
                .addOnCompleteListener(task -> {
                    imageProxy.getImage().close();
                    imageProxy.close();
                });
    }

}
