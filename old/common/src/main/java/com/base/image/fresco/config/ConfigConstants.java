package com.base.image.fresco.config;

import com.facebook.common.util.ByteConstants;
import com.facebook.imagepipeline.common.ImageDecodeOptions;

public class ConfigConstants {
    //    public static final int MAX_HEAP_SIZE = (int) Runtime.getRuntime().totalMemory();
    public static final int MAX_DISK_CACHE_SIZE = 40 * ByteConstants.MB;
    public static final int MAX_MEMORY_CACHE_SIZE = 15 * ByteConstants.MB;

    //图片解码
    public static ImageDecodeOptions getImageDecodeOptions() {
        ImageDecodeOptions decodeOptions = ImageDecodeOptions.newBuilder()
//            .setBackgroundColor(Color.TRANSPARENT)//图片的背景颜色
                .setDecodeAllFrames(true)//解码所有帧
                .setDecodePreviewFrame(true)//解码预览框
//            .setForceOldAnimationCode(true)//使用以前动画
//            .setFrom(options)//使用已经存在的图像解码
//            .setMinDecodeIntervalMs(intervalMs)//最小解码间隔（分位单位）
                .setUseLastFrameForPreview(true)//使用最后一帧进行预览
                .build();
        return decodeOptions;
    }
}
