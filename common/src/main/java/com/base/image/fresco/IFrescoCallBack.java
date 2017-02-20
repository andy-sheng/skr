package com.base.image.fresco;

import com.facebook.imagepipeline.image.ImageInfo;
import com.base.utils.callback.ICommonCallBack;

/**
 * Created by lan on 16-1-11.
 */
public interface IFrescoCallBack extends ICommonCallBack {
    // 处理ImageInfo，返回对应的信息
    void processWithInfo(ImageInfo info);

    // 处理图片加载失败的情况
    void processWithFailure();
}