package com.common.image.fresco;

import com.facebook.imagepipeline.image.ImageInfo;

/**
 * Created by lan on 16-1-11.
 */
public interface IFrescoCallBack {
    // 处理ImageInfo，返回对应的信息
    void processWithInfo(ImageInfo info);

    // 处理图片加载失败的情况
    void processWithFailure();
}