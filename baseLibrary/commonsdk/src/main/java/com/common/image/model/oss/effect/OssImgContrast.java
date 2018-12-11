package com.common.image.model.oss.effect;


import com.common.image.model.oss.IOssParam;

/**
 * 图片对比度
 */
public class OssImgContrast implements IOssParam {

    int contrast = -101;//对比度调整。0 表示原图对比度，小于 0 表示低于原图对比度，大于 0 表示高于原图对比度。	[-100, 100]

    @Override
    public String getOpDesc() {
        StringBuilder sb = new StringBuilder();
        sb.append("/contrast");
        if (contrast > -101) {
            sb.append(",").append(contrast);
        }
        return sb.toString();
    }

    public int getContrast() {
        return contrast;
    }

    public void setContrast(int contrast) {
        this.contrast = contrast;
    }

    public static class Builder {
        OssImgContrast mParams = new OssImgContrast();

        public Builder() {
        }

        public Builder setContrast(int contrast) {
            mParams.setContrast(contrast);
            return this;
        }

        public OssImgContrast build() {
            return mParams;
        }
    }

}
