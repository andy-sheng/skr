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

    protected void generateUri() {
        if (!TextUtils.isEmpty(mUrl)) {
            mUri = Uri.parse(mUrl);
        } else {
//            mUri = Uri.parse(TestConstants.TEST_IMG_URL);
        }
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getFullSizeUrl() {
        return fullSizeUrl;
    }

    public void setFullSizeUrl(String fullSizeUrl) {
        this.fullSizeUrl = fullSizeUrl;
    }

    public Bitmap.Config getConfig() {
        return config;
    }

    public void setConfig(Bitmap.Config config) {
        this.config = config;
    }

    public void addOssProcessors(IOssParam[] ossProcessors) {
        mUrl = OssImgFactory.addOssParams(mUrl,ossProcessors);
        generateUri();
    }
}
