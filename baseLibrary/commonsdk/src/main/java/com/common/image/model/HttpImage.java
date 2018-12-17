package com.common.image.model;

import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;

import com.common.image.model.oss.IOssParam;
import com.common.image.model.oss.OssImgFactory;

/**
 * Created by lan on 15-12-14.
 * 网络图片
 */
public class HttpImage extends BaseImage {
    private String mUrl;
    public String fullSizeUrl = "";
    public Bitmap.Config config;

    /**
     * 使用 ImageFactory来build
     */
    HttpImage(String url) {
        mUrl = url;
        generateUri();
    }

    HttpImage(String url, String fullSizeUrl) {
        this(url, fullSizeUrl, null);
    }

    HttpImage(String url, String fullSizeUrl, Bitmap.Config config) {
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

    public void addOssProcessors(IOssParam[] ossProcessors) {
        mUrl = OssImgFactory.addOssParams(mUrl,ossProcessors);
        generateUri();
    }
}
