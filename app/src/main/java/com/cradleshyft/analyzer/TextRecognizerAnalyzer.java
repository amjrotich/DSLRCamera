package com.cradleshyft.dslrcamera.analyzer;

import android.annotation.SuppressLint;
import android.media.Image;

import androidx.annotation.NonNull;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

public class TextRecognizerAnalyzer implements ImageAnalysis.Analyzer {

    public interface TextRecognizerListener {
        void onTextRecognizerComplete(String text, String languageCode);

        void onTextRecognizerFailure(Exception e);
    }

    private final TextRecognizerListener textRecognizerListener;

    public TextRecognizerAnalyzer(TextRecognizerListener textRecognizerListener) {
        this.textRecognizerListener = textRecognizerListener;
    }

    @SuppressLint("UnsafeOptInUsageError")
    @Override
    public void analyze(@NonNull ImageProxy imageProxy) {
        Image image = imageProxy.getImage();
        if (image != null) {
            recognizeText(imageProxy);
        }
    }

    /**
     * extract text from image and notify result
     */
    @SuppressLint("UnsafeOptInUsageError")
    private void recognizeText(ImageProxy imageProxy) {
        InputImage inputImage = InputImage.fromMediaImage(imageProxy.getImage(),
                imageProxy.getImageInfo().getRotationDegrees());

        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        recognizer.process(inputImage)
                .addOnSuccessListener(visionText -> {
                    for (Text.TextBlock block : visionText.getTextBlocks()) {
                        String text = block.getText();
                        textRecognizerListener.onTextRecognizerComplete(text, block.getRecognizedLanguage());
                    }
                })
                .addOnFailureListener(textRecognizerListener::onTextRecognizerFailure)
                .addOnCompleteListener(task -> {
                    imageProxy.getImage().close();
                    imageProxy.close();
                });

    }

}
