package com.common.image.model;

import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;

import com.common.image.model.oss.IOssParam;
import com.common.image.model.oss.OssImgFactory;
import com.common.image.model.oss.OssImgResize;
import com.common.utils.ImageUtils;

/**
 * Created by lan on 15-12-14.
 * 网络图片
 */
public class HttpImage extends BaseImage {
    private String mUrl;
    private String originUrl;
    public String fullSizeUrl = "";
    public Bitmap.Config config;
    private boolean canHasLowUri = true;

    /**
     * 使用 ImageFactory来build
     */
    HttpImage(String url) {
        mUrl = url;
        originUrl = url;
        generateUri();
    }

    protected void generateUri() {
        if (!TextUtils.isEmpty(mUrl)) {
            mUri = Uri.parse(mUrl);
            if (mLowImageUri == null) {
                //低分辨率的没有
                if (canHasLowUri) {
                    String lowUrl = OssImgFactory.addOssParams(originUrl, OssImgFactory.newResizeBuilder()
                            .setW(ImageUtils.SIZE.SIZE_160.getW())
                            .build());
                    mLowImageUri = Uri.parse(lowUrl);
                }
            }
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

    public void addOssProcessors(IOssParam... ossProcessors) {
        if (ossProcessors != null) {
            for (IOssParam ossParam : ossProcessors) {
                if (ossParam instanceof OssImgResize) {
                    OssImgResize ossImgResize = (OssImgResize) ossParam;
                    if (ossImgResize.getW() == ImageUtils.SIZE.SIZE_160.getW()) {
                        canHasLowUri = false;
                    }
                }
            }
        }
        mUrl = OssImgFactory.addOssParams(mUrl, ossProcessors);
        generateUri();
    }
}
