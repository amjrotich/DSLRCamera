package com.cradleshyft.dslrcamera.util;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

public class StorageUtils {

    public static String getAvailableInternalMemorySize(Context context) {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        long availableBlocks = statFs.getAvailableBlocksLong();
        long blockSize = statFs.getBlockSizeLong();
        return Formatter.formatFileSize(context, availableBlocks * blockSize);
    }

    public static String getTotalInternalMemorySize(Context context) {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getAbsolutePath());
        long blockSize = statFs.getBlockSizeLong();
        long totalBlocks = statFs.getBlockCountLong();
        return Formatter.formatFileSize(context, totalBlocks * blockSize);
    }

}
