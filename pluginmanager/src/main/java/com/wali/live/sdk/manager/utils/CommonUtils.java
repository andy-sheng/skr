package com.wali.live.sdk.manager.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by lan on 17/3/1.
 */
public class CommonUtils {
    public static final int FAST_DOUBLE_CLICK_INTERVAL = 500;

    private static long sLastClickTime = 0;

    /**
     * 判断是否是快速点击
     */
    public static boolean isFastDoubleClick() {
        return isFastDoubleClick(FAST_DOUBLE_CLICK_INTERVAL);
    }

    /**
     * 判断是否是快速点击
     *
     * @param time, 时间间隔, 单位为毫秒
     * @return
     */
    private static boolean isFastDoubleClick(final long time) {
        long now = System.currentTimeMillis();
        long delta = now - sLastClickTime;
        sLastClickTime = now;
        return delta > 0 && delta < time;
    }

    public static void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {
                InputStream inStream = new FileInputStream(oldPath);
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread;
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            System.out.println("error  ");
            e.printStackTrace();
        }
    }
}
