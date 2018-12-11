package com.common.image.model.oss;


public class OssImgResize implements IOssParam {

    ResizeMode m = null;// 模式

    int w = -1;//指定目标缩略图的宽度。	1-4096

    int h = -1;//指定目标缩略图的高度。	1-4096

    /**
     * 当缩放模式选择为 pad（缩略填充）时，
     * 可以选择填充的颜色(默认是白色)参数的填写方式：
     * 采用 16 进制颜色码表示，如 00FF00（绿色）。
     */
    int color = -1;

    @Override
    public String getOpDesc() {
        StringBuilder sb = new StringBuilder();
        sb.append("/resize");
        if (m != null) {
            sb.append(",m_").append(m.toString());
        }
        if (w > 0) {
            sb.append(",w_").append(w);
        }
        if (h > 0) {
            sb.append(",h_").append(h);
        }
        if(color>0){
            sb.append(",color_").append(color);
        }
        return sb.toString();
    }

    public ResizeMode getM() {
        return m;
    }

    public void setM(ResizeMode m) {
        this.m = m;
    }

    public int getW() {
        return w;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public static class Builder {
        OssImgResize mParams = new OssImgResize();

        Builder() {
        }

        public Builder setM(ResizeMode m) {
            mParams.setM(m);
            return this;
        }

        public Builder setW(int w) {
            mParams.setW(w);
            return this;
        }

        public Builder setH(int h) {
            mParams.setH(h);
            return this;
        }

        public OssImgResize build() {
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
