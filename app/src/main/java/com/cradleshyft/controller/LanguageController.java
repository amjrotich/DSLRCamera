package com.cradleshyft.dslrcamera.controller;

import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

public class LanguageController {

    public interface TranslateListener {
        void onTranslateComplete(String translatedText);

        void onTranslateFailure(Exception e);
    }

    /**
     * download language model if is necessary and translate text
     */
    public static void downloadLanguage(String text, String languageCode, String targetLanguageCode,
                                        TranslateListener translateListener) {
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(languageCode)
                .setTargetLanguage(targetLanguageCode)
                .build();

        Translator translator = Translation.getClient(options);

        translator.downloadModelIfNeeded()
                .addOnSuccessListener(aVoid -> LanguageController.translateLanguage(translator, text, translateListener))
                .addOnFailureListener(e -> {
                });
    }

    /**
     * translate text and notify result
     */
    private static void translateLanguage(Translator translator, String text,
                                          TranslateListener translateListener) {
        translator.translate(text)
                .addOnSuccessListener(translateListener::onTranslateComplete)
                .addOnFailureListener(translateListener::onTranslateFailure);
    }

}
