package com.base.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.text.TextUtils;

import com.base.log.MyLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 15-8-13.
 */
public class FileIOUtils {
    private static final String TAG = FileIOUtils.class.getSimpleName();

    public static final String ASSET_TEST_FILE = "test.mp3";

    public static List<String> readFiles(String path) {
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {
            List<String> files = new ArrayList<>();
            for (File f : file.listFiles()) {
                files.add(f.getAbsolutePath());
            }
            return files;
        }
        return null;
    }

    public static boolean deletePath(String path) {
        File file = new File(path);
        if (file.exists()) {
            if (file.isDirectory()) {
                for (File f : file.listFiles()) {
                    f.delete();
                }
            } else {
                file.delete();
            }
            return true;
        }
        return false;
    }

    //TODO 测试代码 从asset根目录复制特定文件到自己应用的文件
    public static void copyAssetTestFileToDataDir(final Context context) {
        try {
            copyAssetToFile(context, "", context.getFilesDir().getPath(), ASSET_TEST_FILE);
        } catch (Exception e) {
            MyLog.d(TAG, e);
        }
    }

    /**
     * 从asset根目录复制特定文件到自己应用的文件
     */
    public static void copyAssetFileToDataDir(final Context context, final String assetFile) {
        try {
            copyAssetToFile(context, "", context.getFilesDir().getPath(), assetFile);
        } catch (Exception e) {
            MyLog.d(TAG, e);
        }
    }

    /**
     * 从asset特定目录复制所有文件到自己应用的文件
     */
    public static void copyAssetDirToDataDir(final Context context, final String assetDir) {
        AssetManager assetManager = context.getAssets();
        try {
            String[] files = assetManager.list(assetDir);
            for (String file : files) {
                copyAssetToFile(context, assetDir, context.getFilesDir().getPath(), file);
            }
        } catch (Exception e) {
            MyLog.d(TAG, e);
        }
    }

    /**
     * 从asset复制到文件夹
     */
    public static void copyAssetToFile(final Context context, final String srcDir, final String destDir, final String filename) {
        if (TextUtils.isEmpty(destDir) || TextUtils.isEmpty(filename)) {
            MyLog.d(TAG, "copyAssetToFile is empty");
            return;
        }
        InputStream is = null;
        FileOutputStream fos = null;

        String asset;
        if (TextUtils.isEmpty(srcDir)) {
            asset = filename;
        } else {
            asset = srcDir + File.separator + filename;
        }
        try {
            is = context.getResources().getAssets().open(asset);
            File dir = new File(destDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String file = destDir + File.separator + filename;
            fos = new FileOutputStream(file);
            byte[] buffer = new byte[4096];
            int count = 0;
            while ((count = is.read(buffer)) > 0) {
                fos.write(buffer, 0, count);
            }
            fos.flush();
        } catch (IOException e) {
            MyLog.d(TAG, "copyAssetToFile is empty", e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                // ignore
            }
        }
    }

    //读数据
    public static String readFile(String fileName) throws IOException {
        String res = "";
        FileInputStream fin = null;
        try {
            fin = new FileInputStream(fileName);
            int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
            res = new String(buffer, "UTF-8");
            fin.close();
        } catch (Exception e) {
        } finally {
            if (fin != null) {
                fin.close();
            }
        }
        return res;
    }
}
