package com.common.image.model.oss.format;


import com.common.image.model.oss.IOssParam;
import com.common.image.model.oss.OssPsFactory;

/**
 * 图片质量压缩
 * 对于 jpg和webp 支持质量压缩
 * 通过{@link OssPsFactory 使用}
 */
public class OssImgQuality implements IOssParam {

    int aq = -1;//决定图片的绝对质量，把原图质量压到rq%，如果原图质量小于指定数字，则不压缩。1-100

    int rq = -1;//决定图片的相对质量，对原图按照 aq% 进行质量压缩。 1-100

    OssImgQuality(){

    }

    @Override
    public String getOpDesc() {
        StringBuilder sb = new StringBuilder();
        sb.append("/quality");
        if (rq > 0) {
            sb.append(",q_").append(rq);
        }
        if (aq > 0) {
            sb.append(",Q_").append(aq);
        }
        return sb.toString();
    }

    public int getAq() {
        return aq;
    }

    public void setAq(int aq) {
        this.aq = aq;
    }

    public int getRq() {
        return rq;
    }

    public void setRq(int rq) {
        this.rq = rq;
    }

    public static class Builder {
        OssImgQuality mParams = new OssImgQuality();

        public Builder() {
        }

        public Builder setAq(int aq) {
            mParams.setAq(aq);
            return this;
        }

        public Builder setRq(int rq) {
            mParams.setRq(rq);
            return this;
        }

        public OssImgQuality build() {
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
    public enum ResizeMode {
        lfit, mfit, fill, pad, fixed
    }
}
