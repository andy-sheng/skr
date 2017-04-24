package com.wali.live.sdk.litedemo.fresco.callback;

import com.facebook.imagepipeline.image.ImageInfo;
import com.little.glint.common.callback.ICallback;

/**
 * Created by lan on 16-1-11.
 */
public interface IFrescoCallback extends ICallback {
    void processWithInfo(ImageInfo info);

    void processWithFailure();
}