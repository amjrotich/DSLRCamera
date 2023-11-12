package com.cradleshyft.dslrcamera.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefsSettings {

    public static final String SHARED_PREFS_SETTINGS = "PrefsSettings";
    //
    public static final String IMAGE_SIZE = "ImageSizeKey";
    public static final String IMAGE_MAX_QUALITY = "ImageMaxQualityKey";
    public static final String VIDEO_RESOLUTION = "VideoResolutionKey";
    public static final String SOUND = "SoundKey";
    public static final String GRID_LINES = "GridLinesKey";
    public static final String PREVIEW_SCALE_TYPE = "PreviewScaleTypeKey";

    public static SharedPreferences getPrefsSettings(Context context) {
        return context.getSharedPreferences(SharedPrefsSettings.SHARED_PREFS_SETTINGS, Context.MODE_PRIVATE);
    }

    public static void setImageSize(int type, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SharedPrefsSettings.SHARED_PREFS_SETTINGS, Context.MODE_PRIVATE).edit();
        editor.putInt(SharedPrefsSettings.IMAGE_SIZE, type);
        editor.apply();
    }

    public static void setVideoSize(int resolution, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SharedPrefsSettings.SHARED_PREFS_SETTINGS, Context.MODE_PRIVATE).edit();
        editor.putInt(SharedPrefsSettings.VIDEO_RESOLUTION, resolution);
        editor.apply();
    }

    public static void setSoundStatus(boolean enable, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SharedPrefsSettings.SHARED_PREFS_SETTINGS, Context.MODE_PRIVATE).edit();
        editor.putBoolean(SharedPrefsSettings.SOUND, enable);
        editor.apply();
    }

    public static void setGridLinesStatus(boolean show, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SharedPrefsSettings.SHARED_PREFS_SETTINGS, Context.MODE_PRIVATE).edit();
        editor.putBoolean(SharedPrefsSettings.GRID_LINES, show);
        editor.apply();
    }

    public static void setImageMaxQuality(boolean maxQuality, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SharedPrefsSettings.SHARED_PREFS_SETTINGS, Context.MODE_PRIVATE).edit();
        editor.putBoolean(SharedPrefsSettings.IMAGE_MAX_QUALITY, maxQuality);
        editor.apply();
    }

    public static void setPreviewScaleType(int scaleType, Context context) {
        SharedPreferences.Editor editor = context.getSharedPreferences(SharedPrefsSettings.SHARED_PREFS_SETTINGS, Context.MODE_PRIVATE).edit();
        editor.putInt(SharedPrefsSettings.PREVIEW_SCALE_TYPE, scaleType);
        editor.apply();
    }

    public static int getVideoSize(Context context) {
        return context.getSharedPreferences(SharedPrefsSettings.SHARED_PREFS_SETTINGS,
                Context.MODE_PRIVATE).getInt(SharedPrefsSettings.VIDEO_RESOLUTION, 1080);
    }

    public static int getImageSizeType(Context context) {
        return context.getSharedPreferences(SharedPrefsSettings.SHARED_PREFS_SETTINGS,
                Context.MODE_PRIVATE).getInt(SharedPrefsSettings.IMAGE_SIZE, 1);
    }

    public static boolean getSoundStatus(Context context) {
        return context.getSharedPreferences(SharedPrefsSettings.SHARED_PREFS_SETTINGS,
                Context.MODE_PRIVATE).getBoolean(SharedPrefsSettings.SOUND, true);
    }

    public static boolean getGridLinesStatus(Context context) {
        return context.getSharedPreferences(SharedPrefsSettings.SHARED_PREFS_SETTINGS,
                Context.MODE_PRIVATE).getBoolean(SharedPrefsSettings.GRID_LINES, false);
    }

    public static boolean getImageMaxQuality(Context context) {
        return context.getSharedPreferences(SharedPrefsSettings.SHARED_PREFS_SETTINGS,
                Context.MODE_PRIVATE).getBoolean(SharedPrefsSettings.IMAGE_MAX_QUALITY, true);
    }

    public static int getPreviewScaleType(Context context) {
        return context.getSharedPreferences(SharedPrefsSettings.SHARED_PREFS_SETTINGS,
                Context.MODE_PRIVATE).getInt(SharedPrefsSettings.PREVIEW_SCALE_TYPE, 1);
    }

    /**
     * @return image size
     */
    public static int[] getImageSize(Context context) {
        int[] widthHeight = new int[2];

        switch (SharedPrefsSettings.getImageSizeType(context)) {
            case 1:
                widthHeight = null;
                break;
            case 2:
                widthHeight[0] = 2268;
                widthHeight[1] = 4032;
                break;
            case 3:
                widthHeight[0] = 1440;
                widthHeight[1] = 2560;
                break;
            case 4:
                widthHeight[0] = 3024;
                widthHeight[1] = 4032;
                break;
            case 5:
                widthHeight[0] = 2160;
                widthHeight[1] = 2880;
                break;
        }
        return widthHeight;
    }

}
