package com.bytedance.labcv.effectsdk;

public class YUVUtils {
    /**
     * 对YUV数据转换为RGBA格式，同时支持压缩和旋转和翻转
     * @param data yuv输入
     * @param dst RGBA格式输出
     * @param pixel_format 输入数据格式，目前支持YUV420P,NV12和NV21 如果不在这几种范围内，默认当成NV21处理
     * @param image_width 图片宽度
     * @param image_height 图片高度
     * @param dst_width  目标尺寸
     * @param dst_height 目标尺寸
     * @param orientation 输入图片的旋转角度，一般YUV输出有一定旋转角度，需要转正
     * @param isFront 是否是前置摄像头，如果是前置摄像头，会对数据进行左右翻转
     */
    public static   native void YUV2RGBA(byte[]data, byte[]dst, int pixel_format, int image_width, int image_height, int dst_width, int dst_height, int orientation,boolean isFront);
    /**
     * 将RGBA格式数据转为YUV数据
     * @param data RGBA输入
     * @param dst YUV格式输出
     * @param pixel_format 输出YUV数据格式，目前支持YUV420P,NV12和NV21 如果不在这几种范围内，默认当成NV21处理
     * @param image_width 图片宽度
     * @param image_height 图片高度
     */
    public static   native void RGBA2YUV(byte[]data, byte[]dst, int pixel_format, int image_width, int image_height);

}
