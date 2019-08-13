package com.common.image.model;

import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;

import com.common.image.fresco.FrescoWorker;
import com.common.image.model.oss.IOssParam;
import com.common.image.model.oss.OssImgFactory;
import com.common.image.model.oss.OssImgResize;
import com.common.utils.ImageUtils;

import java.io.File;

/**
 * Created by lan on 15-12-14.
 * 网络图片
 */
public class HttpImage extends BaseImage {
    private String mUrl; // 处理后的的url
    private String originUrl;//原始的url
    public Bitmap.Config config;
    private boolean canHasLowUri = true;

    /**
     * 使用 ImageFactory来build
     */
    HttpImage(String url) {
        mUrl = url;
        originUrl = url;
        //generateUri();
    }

    protected void generateUri() {
        if (!TextUtils.isEmpty(mUrl)) {
            mUri = Uri.parse(mUrl);
            if (mLowImageUri == null) {
                if (mLowImageSize != null) {
                    String lowUrl = OssImgFactory.addOssParams(originUrl, OssImgFactory.newResizeBuilder()
                            .setW(mLowImageSize.getW())
                            .build());
                    if(!lowUrl.equals(mUrl)){
                        // lowUrl 和 加载 url 一样 没必要设置 lowImageUri 了
                        mLowImageUri = Uri.parse(lowUrl);
                    }
                } else {
                    //低分辨率的没有
                    if (canHasLowUri) {
                        String lowUrl_320 = OssImgFactory.addOssParams(originUrl, OssImgFactory.newResizeBuilder()
                                .setW(ImageUtils.SIZE.SIZE_320.getW())
                                .build());
                        File file = FrescoWorker.getCacheFileFromFrescoDiskCache(lowUrl_320);
                        if (file != null && file.exists()) {
                            mLowImageUri = Uri.parse(lowUrl_320);
                        } else {
                            String lowUrl_160 = OssImgFactory.addOssParams(originUrl, OssImgFactory.newResizeBuilder()
                                    .setW(ImageUtils.SIZE.SIZE_160.getW())
                                    .build());
                            if(!lowUrl_160.equals(mUrl)){
                                // lowUrl 和 加载 url 一样 没必要设置 lowImageUri 了
                                mLowImageUri = Uri.parse(lowUrl_160);
                            }
                        }
                    }else{
                        // 不需要设置
                    }
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
                    if (ossImgResize.getW() <= ImageUtils.SIZE.SIZE_320.getW()) {
                        canHasLowUri = false;
                    }
                }
            }
        }
        mUrl = OssImgFactory.addOssParams(mUrl, ossProcessors);
    }

    @Override
    public String toString() {
        return "HttpImage{" +
                "mUri=" + mUri +
                ", mLowImageUri=" + mLowImageUri +
                '}';
    }
}
