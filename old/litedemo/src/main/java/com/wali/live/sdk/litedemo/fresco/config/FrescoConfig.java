package com.wali.live.sdk.litedemo.fresco.config;

import com.facebook.imagepipeline.common.ImageDecodeOptions;

/**
 * Created by lan on 16/12/6.
 */
public class FrescoConfig {
    // picture decode
    public static ImageDecodeOptions getImageDecodeOptions() {
        ImageDecodeOptions decodeOptions = ImageDecodeOptions.newBuilder()
                .setDecodeAllFrames(true)           //解码所有帧
                .setDecodePreviewFrame(true)        //解码预览框
                .setUseLastFrameForPreview(true)    //使用最后一帧进行预览
                .build();
        return decodeOptions;
    }
}
