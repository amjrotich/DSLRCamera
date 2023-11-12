package com.cradleshyft.dslrcamera.analyzer;

import android.annotation.SuppressLint;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

public class ImageLabelingAnalyzer implements ImageAnalysis.Analyzer {

    public interface ImageLabelingListener {
        void onImageLabelingComplete(String result);

        void onImageLabelingFailure(Exception e);
    }

    private final ImageLabelingListener labelingListener;

    public ImageLabelingAnalyzer(ImageLabelingListener labelingListener) {
        this.labelingListener = labelingListener;
    }

    @SuppressLint("UnsafeOptInUsageError")
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        Image image = imageProxy.getImage();
        if (image != null) {
            startImageLabeler(imageProxy);
        }
    }

    /**
     * detect label with with confidence higher than 0.9 and notify result
     *
     */
    @SuppressLint("UnsafeOptInUsageError")
    private void startImageLabeler(ImageProxy imageProxy) {
        InputImage inputImage = InputImage.fromMediaImage(imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees());

        ImageLabelerOptions options = new ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.9f)
                .build();

        ImageLabeler labeler = ImageLabeling.getClient(options);

        labeler.process(inputImage)
                .addOnSuccessListener(labels -> {
                    for (ImageLabel label : labels) {
                        String text = label.getText();
                        labelingListener.onImageLabelingComplete(text);
                    }

                })
                .addOnFailureListener(labelingListener::onImageLabelingFailure)
                .addOnCompleteListener(task -> {
                    imageProxy.getImage().close();
                    imageProxy.close();
                });
    }

}
