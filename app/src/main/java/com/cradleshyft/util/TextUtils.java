package com.cradleshyft.dslrcamera.util;

import android.app.Activity;
import android.content.Intent;

public class TextUtils {

    /**
     * share text
     */
    public static void shareText(String text, Activity activity) {
        if (text != null) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, text);
            sendIntent.setType("text/plain");
            Intent shareIntent = Intent.createChooser(sendIntent, null);
            activity.startActivity(shareIntent);
        }
    }

}
