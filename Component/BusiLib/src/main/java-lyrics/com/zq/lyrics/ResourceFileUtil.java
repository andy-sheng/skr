package com.zq.lyrics;

import android.content.Context;

import com.common.log.MyLog;

import java.io.File;

public class ResourceFileUtil {
    public static final String TAG = "ResourceFileUtil";
    /**
     * 获取资源文件的完整路径
     *
     * @param context
     * @param tempFilePath 文件的临时路径
     * @return
     */
    public static String getFilePath(Context context, String tempFilePath, String fileName) {

        if (fileName == null) {
            fileName = "";
        }

        //
        String filePath = tempFilePath + File.separator + fileName;
        MyLog.d(TAG, "getFilePath filepath is " + filePath);
        File file = new File(filePath);
        if(!fileName.equals("")){
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
        }else{
            if(!file.exists()){
                file.mkdirs();
            }
        }

        return filePath;
    }
}
