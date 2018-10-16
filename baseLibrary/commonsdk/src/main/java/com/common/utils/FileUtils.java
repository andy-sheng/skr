package com.common.utils;

import android.text.TextUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by linjinbin on 15/2/10.
 */
public class FileUtils {

    public int KB = 1024;
    public int MB = 1024 * KB;

    private final HashMap<String, String> mFileTypes = new HashMap<>();

    {
        // images
        mFileTypes.put("FFD8FF", "jpg");
        mFileTypes.put("89504E47", "png");
        mFileTypes.put("47494638", "gif");
        mFileTypes.put("474946", "gif");    //added by mk
        mFileTypes.put("424D", "bmp");
    }

    FileUtils() {
    }

    /**
     * 传入一个文件路径，返回文件夹路径
     *
     * @param path
     * @return
     */
    public String getSuffixFromFilePath(String path) {
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

    /**
     * 传入一个文件路径，返回文件名
     *
     * @param path
     * @return
     */
    public String getFileNameFromFilePath(String path) {
        String extension = path;
        if (!TextUtils.isEmpty(path)) {
            int indexOfSlash = path.lastIndexOf('/');
            if (indexOfSlash != -1) {
                extension = path.substring(indexOfSlash + 1);
            }
        }
        return extension;
    }

    /**
     * 获取上传文件的类型(仅针对图片类型)
     *
     * @param filePath
     * @return
     */
    public String getImageFileType(String filePath) {
        return mFileTypes.get(getFileHeader(filePath));
    }

    /**
     * 获得文件头信息
     * 
     * @param filePath
     * @return
     */
    private  String getFileHeader(String filePath) {
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

    private String bytesToHexString(byte[] src) {
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
}

