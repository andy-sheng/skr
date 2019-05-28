package com.common.image.model.oss.format;


import com.common.image.model.oss.IOssParam;
import com.common.image.model.oss.OssImgFactory;

/**
 * 图片格式转换
 * 通过{@link OssImgFactory 使用}
 */
public class OssImgFormat implements IOssParam {

    ImgF format = null;// 模式

    OssImgFormat(){

    }

    @Override
    public String getOpDesc() {
        StringBuilder sb = new StringBuilder();
        sb.append("/format");
        if (format != null) {
            sb.append(",").append(format);
        }
        return sb.toString();
    }

    public ImgF getFormat() {
        return format;
    }

    public void setFormat(ImgF format) {
        this.format = format;
    }

    public static class Builder {
        OssImgFormat mParams = new OssImgFormat();

        public Builder() {
        }

        public Builder setFormat(ImgF format) {
            mParams.setFormat(format);
            return this;
        }

        public OssImgFormat build() {
            return mParams;
        }
    }

    /**
     * 指定缩略的模式：
     * lfit：等比缩放，限制在指定w与h的矩形内的最大图片。
     * mfit：等比缩放，延伸出指定w与h的矩形框外的最小图片。
     * fill：固定宽高，将延伸出指定w与h的矩形框外的最小图片进行居中裁剪。
     * pad：固定宽高，缩略填充。
     * fixed：固定宽高，强制缩略。
     */
    public enum ImgF {
        jpg, png, webp, bmp, gif, tiff
    }
}
