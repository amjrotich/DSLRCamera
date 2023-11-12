package com.cradleshyft.dslrcamera.util;

import android.content.Context;
import android.media.MediaPlayer;

public class AudioUtils {

    /**
     * play audio from raw folder
     */
    public static void playSound(int sound, Context context) {
        if (SharedPrefsSettings.getSoundStatus(context)) {
            MediaPlayer.create(context, sound).start();
        }
    }

}
