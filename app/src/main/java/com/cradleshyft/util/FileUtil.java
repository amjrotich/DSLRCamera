package com.cradleshyft.dslrcamera.util;

import android.content.ContentValues;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import com.google.android.exoplayer2.util.MimeTypes;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class FileUtil {

    private static final String FOLDER_PHOTO = "CameraXPhotos";
    private static final String FOLDER_VIDEO = "CameraXVideos";

    public static String getFileName(String extension) {
        return new SimpleDateFormat("EEE_dd_MMM_yyyy_HH:mm:ss",
                Locale.getDefault()).format(Calendar.getInstance().getTime()) + extension;
    }

    public static ContentValues getPicturesContentValues() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, getFileName(".jpg"));
        contentValues.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        contentValues.put(MediaStore.Images.Media.MIME_TYPE, MimeTypes.IMAGE_JPEG);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + FOLDER_PHOTO);
            contentValues.put(MediaStore.Images.Media.IS_PENDING, true);
        }
        return contentValues;
    }


    public static ContentValues getVideoContentValues() {
        String name = getFileName(".mp4");
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.Video.Media.TITLE, name);
        contentValues.put(MediaStore.Video.Media.DISPLAY_NAME, name);
        contentValues.put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        contentValues.put(MediaStore.Video.Media.MIME_TYPE, MimeTypes.VIDEO_MP4);
        contentValues.put(MediaStore.Video.Media.DATE_TAKEN, System.currentTimeMillis());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + File.separator + FOLDER_VIDEO);
            contentValues.put(MediaStore.Video.Media.IS_PENDING, true);
        }
        return contentValues;
    }

}
