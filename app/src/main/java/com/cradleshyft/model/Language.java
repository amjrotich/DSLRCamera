package com.cradleshyft.dslrcamera.model;

import androidx.annotation.NonNull;

import java.util.Locale;

public class Language {

    private final String language, code;

    public Language(String code) {
        this.language = Locale.forLanguageTag(code).getDisplayName();
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    @NonNull
    @Override
    public String toString() {
        return language;
    }

}
