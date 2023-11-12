package com.cradleshyft.dslrcamera.controller;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.CheckBox;
import android.widget.TextView;
import com.google.android.material.chip.ChipGroup;
import com.cradleshyft.dslrcamera.R;
import com.cradleshyft.dslrcamera.util.SharedPrefsSettings;
import com.cradleshyft.dslrcamera.util.StorageUtils;

public class SettingsController {

    /**
     * set storage info
     */
    @SuppressLint("SetTextI18n")
    public static void setMemory(TextView txtMemory, Context context) {
        txtMemory.setText(StorageUtils.getAvailableInternalMemorySize(context)
                + " available / " + StorageUtils.getTotalInternalMemorySize(context) + " in total");
    }

    /**
     * set video resolution
     */
    public static void setVideoResolution(ChipGroup chipGroupVResolution, Context context) {
        switch (SharedPrefsSettings.getVideoSize(context)) {
            case 2160:
                chipGroupVResolution.check(R.id.vResolution1440p);
                break;
            case 1080:
                chipGroupVResolution.check(R.id.vResolution1080p);
                break;
            case 720:
                chipGroupVResolution.check(R.id.vResolution720p);
                break;
            case 480:
                chipGroupVResolution.check(R.id.vResolution480p);
                break;
        }
    }

    /**
     * set image size
     */
    public static void setImageSize(ChipGroup chipGroupImageSize, Context context) {
        switch (SharedPrefsSettings.getImageSizeType(context)) {
            case 1:
                chipGroupImageSize.check(R.id.imgSizeFull);
                break;
            case 2:
                chipGroupImageSize.check(R.id.imgSize169_4032);
                break;
            case 3:
                chipGroupImageSize.check(R.id.imgSize169_2560);
                break;
            case 4:
                chipGroupImageSize.check(R.id.imgSize43_4032);
                break;
            case 5:
                chipGroupImageSize.check(R.id.imgSize43_2880);
                break;
        }
    }

    /**
     * set status sound
     */
    public static void setStatusCBSound(CheckBox cBEnableSound, Context context) {
        cBEnableSound.setChecked(SharedPrefsSettings.getSoundStatus(context));
    }

    /**
     * set status grid lines
     */
    public static void setStatusGridLines(CheckBox checkBoxGridLines, Context context) {
        checkBoxGridLines.setChecked(SharedPrefsSettings.getGridLinesStatus(context));
    }

    /**
     * set image quality
     */
    public static void setImageMaxQuality(CheckBox checkBoxImageMaxQuality, Context context) {
        checkBoxImageMaxQuality.setChecked(SharedPrefsSettings.getImageMaxQuality(context));
    }

}
