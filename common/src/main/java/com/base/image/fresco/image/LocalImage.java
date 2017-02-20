package com.base.image.fresco.image;

import android.net.Uri;
import android.text.TextUtils;

import java.io.File;

/**
 * Created by lan on 15-12-14.
 * 本地图片
 */
public class LocalImage extends BaseImage {
    private String mPath;
    public LocalImage() {

    }
    public LocalImage(String path) {
        mPath = path;
        generateUri();
    }

    protected void generateUri() {
        if (!TextUtils.isEmpty(mPath)) {
            mUri = Uri.fromFile(new File(mPath));
        }
    }

    public void setPath(String path) {
        if (!TextUtils.isEmpty(path)) {
            mPath = path;
            mUri = Uri.fromFile(new File(mPath));
        }
    }

    public void setUri(Uri mUri) {
        this.mUri=mUri;
    }
}
