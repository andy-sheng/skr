package com.wali.live;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;

/**
 * Created by yangli on 2017/11/4.
 */
public class Utils {
    private static final String TAG = "Utils";

    private Utils() {
    }

    public static String newExternalPath(@NonNull String subPath) {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.w(TAG, "newExternalPath but externalStorage is not mounted");
            return null;
        }
        File path = new File(Environment.getExternalStorageDirectory(), subPath);
        boolean success = path.mkdirs();
        if ((success || path.exists()) && path.canWrite()) {
            return path.getAbsolutePath();
        }
        return null;
    }

    public static String formatTime(long time) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss.SSS");
        return formatter.format(time);
    }

    public static String joinPath(String path, String subPath, String... extraPath) {
        StringBuilder stringBuilder = new StringBuilder(path);
        stringBuilder.append(File.separator).append(subPath);
        if (extraPath != null) {
            for (String elem : extraPath) {
                stringBuilder.append(File.separator).append(elem);
            }
        }
        return stringBuilder.toString();
    }
}
