package com.wali.live.sdk.litedemo.fresco.callback;

import com.facebook.imagepipeline.image.ImageInfo;

/**
 * Created by lan on 16-1-11.
 */
public interface IFrescoCallback {
    void processWithInfo(ImageInfo info);

    void processWithFailure();
}