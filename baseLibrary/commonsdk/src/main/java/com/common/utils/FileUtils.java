package com.common.utils;

import android.os.Looper;
import android.text.TextUtils;

/**
 * Created by linjinbin on 15/2/10.
 */
public class FileUtils {

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
                extension = path.substring(indexOfSlash+1);
            }
        }
        return extension;
    }

}

