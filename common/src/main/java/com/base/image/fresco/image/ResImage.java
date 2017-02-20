package com.base.image.fresco.image;

import android.net.Uri;

/**
 * Created by lan on 15-12-14.
 * 资源图片
 */
public class ResImage extends BaseImage {
    private int mResId;

    public ResImage(int resId) {
        mResId = resId;
        generateUri();
    }

    protected void generateUri() {
        mUri = new Uri.Builder().scheme("res").path(String.valueOf(mResId)).build();
    }
}
