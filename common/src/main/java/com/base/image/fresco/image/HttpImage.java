package com.base.image.fresco.image;

import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by lan on 15-12-14.
 * 网络图片
 */
public class HttpImage extends BaseImage {
    private String mUrl;
    public String fullSizeUrl = "";
    public Bitmap.Config config;

    public HttpImage(String url) {
        mUrl = url;
        generateUri();
    }

    public HttpImage(String url, String fullSizeUrl) {
        this(url, fullSizeUrl, null);
    }

    public HttpImage(String url, String fullSizeUrl, Bitmap.Config config) {
        this.mUrl = url;
        this.config = config;
        this.fullSizeUrl = fullSizeUrl;
    }

    protected void generateUri() {
        if (!TextUtils.isEmpty(mUrl)) {
            mUri = Uri.parse(mUrl);
        } else {
//            mUri = Uri.parse(TestConstants.TEST_IMG_URL);
        }
    }

}
