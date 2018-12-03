package com.zq.lyrics;

import android.content.Context;

import com.common.log.MyLog;
import com.zq.lyrics.model.StorageInfo;
import com.zq.lyrics.utils.StorageListUtil;

import java.io.File;
import java.util.List;

public class ResourceFileUtil {
    public static final String TAG = "ResourceFileUtil";
    /**
     * 文件的基本路径
     */
    private static String baseFilePath = null;

    /**
     * 获取资源文件的完整路径
     *
     * @param context
     * @param tempFilePath 文件的临时路径
     * @return
     */
    public static String getFilePath(Context context, String tempFilePath, String fileName) {

        if (baseFilePath == null) {
            List<StorageInfo> storageInfos = StorageListUtil.listAvaliableStorage(context);
            for (int i = 0; i < storageInfos.size(); i++) {
                StorageInfo temp = storageInfos.get(i);
                if (!temp.isRemoveable) {
                    baseFilePath = temp.path;
                    break;
                }
            }
        }

        if (fileName == null) {
            fileName = "";
        }

        //
        String filePath = baseFilePath + File.separator + tempFilePath + File.separator + fileName;
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
