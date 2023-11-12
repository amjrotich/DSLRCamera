package com.cradleshyft.dslrcamera.model;

import android.net.Uri;

import androidx.annotation.NonNull;

public class MediaItemObj {
    private final Uri uri;
    private final int mediaType;

    public MediaItemObj(Uri uri, int mediaType) {
        this.uri = uri;
        this.mediaType = mediaType;
    }

    public Uri getUri() {
        return uri;
    }

    public int getMediaType() {
        return mediaType;
    }

    @NonNull
    @Override
    public String toString() {
        return "MediaItemObj{" +
                "uri=" + uri +
                ", mediaType=" + mediaType +
                '}';
    }

}
