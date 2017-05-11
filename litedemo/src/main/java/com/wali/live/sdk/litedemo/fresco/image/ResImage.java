package com.wali.live.sdk.litedemo.fresco.image;

import android.net.Uri;

/**
 * Created by lan on 15-12-14.
 *
 * @description resource image
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
