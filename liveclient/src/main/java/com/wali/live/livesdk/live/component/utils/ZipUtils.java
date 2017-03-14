package com.wali.live.livesdk.live.component.utils;

import android.content.Context;

import com.base.log.MyLog;

import org.apache.http.entity.InputStreamEntity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by yangli on 16-4-27.
 */
public class ZipUtils {
    private static final String TAG = ZipUtils.class.getSimpleName();

    public static boolean unzipAsset(Context context, String assetName, String targetPath) {
        ZipInputStream zipInputStream = null;
        try {
            zipInputStream = new ZipInputStream(context.getAssets().open(assetName));
            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String fileName = entry.getName();
                try {
                    writeTo(zipInputStream, String.format("%s/%s", targetPath, fileName));
                } catch (Exception e) {
                    MyLog.e(TAG, "unzipAsset writeTo exception ", e);
                    return false;
                }
            }
        } catch (IOException e) {
            MyLog.e(TAG, "unzipAsset exception ", e);
            return false;
        } finally {
            if (zipInputStream != null) {
                try {
                    zipInputStream.close();
                } catch (Exception e) {
                    MyLog.e(TAG, "close zip input stream exception", e);
                    return false;
                }
            }
        }
        return true;
    }

    public static void writeTo(InputStream is, String filename) throws IOException {
        new File(filename).getParentFile().mkdirs();

        InputStreamEntity inputEntity = new InputStreamEntity(new BufferedInputStream(is), -1);
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename));
        inputEntity.writeTo(out);
        out.close();
        new File(filename).setLastModified(System.currentTimeMillis());
    }
}
