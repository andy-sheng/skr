package com.wali.live.livesdk.live.utils;

import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by lizhigang on 15/4/15.
 */
public class FileUtils {
    public static final int KB = 1024;
    public static final int MB = 1024 * KB;

    private static final HashMap<String, String> mFileTypes = new HashMap<String, String>();

    static {
        // images
        mFileTypes.put("FFD8FF", "jpg");
        mFileTypes.put("89504E47", "png");
        mFileTypes.put("47494638", "gif");
        mFileTypes.put("474946", "gif");    //added by mk
        mFileTypes.put("424D", "bmp");
    }

    public static String getFileType(String filePath) {
        return mFileTypes.get(getFileHeader(filePath));
    }

    // 获取文件头信息
    private static String getFileHeader(String filePath) {
        FileInputStream is = null;
        String value = null;
        try {
            is = new FileInputStream(filePath);
            byte[] b = new byte[3];
            is.read(b, 0, b.length);
            value = bytesToHexString(b);
        } catch (Exception e) {
        } finally {
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }
        return value;
    }

    private static String bytesToHexString(byte[] src) {
        StringBuilder builder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        String hv;
        for (int i = 0; i < src.length; i++) {
            hv = Integer.toHexString(src[i] & 0xFF).toUpperCase();
            if (hv.length() < 2) {
                builder.append(0);
            }
            builder.append(hv);
        }
        return builder.toString();
    }

    public static String getMimeType(String path) {
        String extension = getFileExt(path).toLowerCase();
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return mimeType != null ? mimeType : "*/*";
    }

    /**
     * 获得文件后缀
     * @param path
     * @return
     */
    public static String getFileExt(String path) {
        String extension = "";

        if (!TextUtils.isEmpty(path)) {
            int indexOfSlash = path.lastIndexOf('/');
            int indexOfDot = path.lastIndexOf('.');
            if (indexOfDot > indexOfSlash) {
                extension = path.substring(indexOfDot + 1);
            }
        }

        return extension;
    }

}
