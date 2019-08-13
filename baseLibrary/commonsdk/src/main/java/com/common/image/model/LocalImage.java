package com.common.image.model;

import android.net.Uri;
import android.text.TextUtils;

import java.io.File;

/**
 * Created by lan on 15-12-14.
 * 本地图片
 */
public class LocalImage extends BaseImage {
    private String mPath;

    /**
     * 使用 ImageFactory来build
     */
     LocalImage(String path) {
        mPath = path;
    }

    protected void generateUri() {
        if (!TextUtils.isEmpty(mPath)) {
            mUri = Uri.fromFile(new File(mPath));
        }
    }

    public String getPath() {
        return mPath;
    }
}
