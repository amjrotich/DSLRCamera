package com.cradleshyft.dslrcamera.util;

import android.net.Uri;
import android.provider.MediaStore;

public class CursorUtil {

    public static Uri getMediaContentUri(int mediaType) {
        if (mediaType == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO) {
            return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else {
            return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
    }

    public static String getSortOrder() {
        return MediaStore.Files.FileColumns.DATE_ADDED + " DESC";
    }

    public static String getSelection() {
        return MediaStore.Files.FileColumns.MEDIA_TYPE + " = " +
                MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE + " OR " +
                MediaStore.Files.FileColumns.MEDIA_TYPE + " = " +
                MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;
    }

    public static Uri getCursorUri() {
        return MediaStore.Files.getContentUri("external");
    }

    public static String[] getProjection() {
        return new String[]{
                MediaStore.Files.FileColumns._ID,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.DATE_ADDED};
    }

}
