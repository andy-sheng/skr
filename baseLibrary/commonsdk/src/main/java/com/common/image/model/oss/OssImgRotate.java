package com.common.image.model.oss;


/**
 * 旋转
 * 通过{@link OssImgFactory 使用}
 */
public class OssImgRotate implements IOssParam {

    int r = -1;//旋转的角度 0-360

    OssImgRotate(){

    }

    @Override
    public String getOpDesc() {
        StringBuilder sb = new StringBuilder();
        sb.append("/rounded-corners");
        if (r > 0) {
            sb.append(",").append(r);
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
        OssImgRotate mParams = new OssImgRotate();

        Builder() {
        }

        public Builder setR(int r) {
            mParams.setR(r);
            return this;
        }

        public OssImgRotate build() {
            return mParams;
        }
    }

}
