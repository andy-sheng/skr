package com.common.image.fresco;

import android.graphics.drawable.Animatable;

import com.facebook.imagepipeline.image.ImageInfo;

/**
 * Created by lan on 16-1-11.
 */
public interface IFrescoCallBack {
    // 处理ImageInfo，返回对应的信息
    void processWithInfo(ImageInfo info, Animatable animatable);

    // 处理图片加载失败的情况
    void processWithFailure();
}