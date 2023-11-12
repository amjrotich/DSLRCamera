package com.cradleshyft.dslrcamera.util;

import android.content.Context;
import android.view.WindowManager;

public class CameraUtils {

    /**
     * @return display rotation
     */
    public static int getDisplayRotation(Context context) {
        return ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
    }

}
