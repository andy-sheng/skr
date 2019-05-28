package com.common.image.model.oss;


/**
 * 圆角
 * 通过{@link OssImgFactory 使用}
 */
public class OssImgRounded implements IOssParam {

    int r = -1;//将图片切出圆角，指定圆角的半径。	[1, 4096] 生成的最大圆角的半径不能超过原图的最小边的一半。

    OssImgRounded(){

    }

    @Override
    public String getOpDesc() {
        StringBuilder sb = new StringBuilder();
        sb.append("/rounded-corners");
        if (r > 0) {
            sb.append(",r_").append(r);
        }
        return sb.toString();
    }

    public int getR() {
        return r;
    }

    public void setR(int r) {
        this.r = r;
    }

    public static class Builder {
        OssImgRounded mParams = new OssImgRounded();

        Builder() {
        }

        public Builder setR(int r) {
            mParams.setR(r);
            return this;
        }

        public OssImgRounded build() {
            return mParams;
        }
    }

}
