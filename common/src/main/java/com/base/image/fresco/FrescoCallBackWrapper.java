package com.base.image.fresco;

import com.base.image.fresco.IFrescoCallBack;
import com.facebook.imagepipeline.image.ImageInfo;

/**
 * Created by lan on 16-1-11.
 * IFrescoCallBack的包装器
 */
public class FrescoCallBackWrapper implements IFrescoCallBack {
    // 正常处理返回值
    public void process(Object object) {
    }

    // 处理ImageInfo，返回对应的信息
    public void processWithInfo(ImageInfo info) {
    }

    // 处理图片加载失败的情况
    public void processWithFailure() {
    }
}