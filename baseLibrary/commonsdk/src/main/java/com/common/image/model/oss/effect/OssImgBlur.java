package com.common.image.model.oss.effect;


import com.common.image.model.oss.IOssParam;

/**
 * 高斯模糊效果
 */
public class OssImgBlur implements IOssParam {

    int r = -1;//模糊半径	[1,50]r 越大图片越模糊。
    int s = -1;//正态分布的标准差	[1,50]r 越大图片越模糊。

    @Override
    public String getOpDesc() {
        StringBuilder sb = new StringBuilder();
        sb.append("/blur");
        if (r > 0) {
            sb.append(",r_").append(r);
        }
        if (s > 0) {
            sb.append(",s_").append(s);
        }
        return sb.toString();
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public int getS() {
        return s;
    }

    public void setS(int s) {
        this.s = s;
    }

    public static class Builder {
        OssImgBlur mParams = new OssImgBlur();

        public Builder() {
        }

        public Builder setR(int r) {
            mParams.setR(r);
            return this;
        }

        public Builder setS(int s) {
            mParams.setS(s);
            return this;
        }

        public OssImgBlur build() {
            return mParams;
        }
    }

}
