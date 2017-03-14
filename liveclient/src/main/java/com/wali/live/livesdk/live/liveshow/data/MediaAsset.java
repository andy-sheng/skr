package com.wali.live.livesdk.live.liveshow.data;

import android.content.Context;
import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.thread.ThreadPool;
import com.wali.live.livesdk.live.component.utils.ZipUtils;

import java.io.File;

/**
 * Created by yangli on 16-4-27.
 *
 * @module 音效
 */
public class MediaAsset {
    private static final String TAG = "MediaAsset";

    private static final int MEDIA_VERSION = 1;
    private static final String MEDIA_FILE = "atmosphere.zip";
    private static final String EXTERNAL_MEDIA_DIR = "media";

    private String mExternalPath = "";
    private boolean mHasPrepared = false;

    private void unzipMediaFiles(final Context context) {
        MyLog.w(TAG, "unzipMediaFiles");
        synchronized (MediaAsset.class) {
            boolean isUnzipSuccess = PreferenceUtils.getSettingBoolean(context,
                    PreferenceUtils.PREF_KEY_UNZIP_MEDIA_FILE, false);
            if (!isUnzipSuccess) {
                isUnzipSuccess = ZipUtils.unzipAsset(context, MEDIA_FILE, mExternalPath);
            }
            PreferenceUtils.setSettingBoolean(context,
                    PreferenceUtils.PREF_KEY_UNZIP_MEDIA_FILE, isUnzipSuccess);
        }
        MyLog.w(TAG, "unzipMediaFiles done");
    }

    public void prepareMediaAssetAsync(final Context context) {
        MyLog.w(TAG, "prepareMediaAsset mHasPrepared=" + mHasPrepared);
        if (mHasPrepared || context == null) {
            return;
        }
        try {
            File externalFile = context.getDir(EXTERNAL_MEDIA_DIR, Context.MODE_WORLD_READABLE);
            mExternalPath = externalFile.getAbsolutePath();
            int version = PreferenceUtils.getSettingInt(context,
                    PreferenceUtils.PREF_KEY_MEDIA_FILE_VERSION_CODE, 0);
            if (version < MEDIA_VERSION) {
                ThreadPool.runOnPool(new Runnable() {
                    @Override
                    public void run() {
                        unzipMediaFiles(context);
                    }
                });
            }
        } catch (Exception e) {
            MyLog.w(TAG, "prepareMediaAsset failed, exception=" + e);
        }
        mHasPrepared = true;
    }

    public String queryFullPath(final Context context, String mediaFile) {
        MyLog.w(TAG, "queryMediaFile mHasPrepared=" + mHasPrepared);
        if (!mHasPrepared || context == null || TextUtils.isEmpty(mediaFile)) {
            return null;
        }
        String fullPath = mExternalPath + "/" + mediaFile;
        if (new File(fullPath).isFile()) {
            return fullPath;
        }
        return null;
    }
}
