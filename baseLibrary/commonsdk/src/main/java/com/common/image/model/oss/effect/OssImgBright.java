package com.common.image.model.oss.effect;


import com.common.image.model.oss.IOssParam;
import com.common.image.model.oss.OssPsFactory;

/**
 * 图片亮度
 * 通过{@link OssPsFactory 使用}
 */
public class OssImgBright implements IOssParam {

    int bright = -101;//亮度调整。0 表示原图亮度，小于 0 表示低于原图亮度，大于 0 表示高于原图亮度。	[-100, 100]

    OssImgBright(){

    }

    @Override
    public String getOpDesc() {
        StringBuilder sb = new StringBuilder();
        sb.append("/bright");
        if (bright > -101) {
            sb.append(",").append(bright);
        }
        return sb.toString();
    }

    public int getBright() {
        return bright;
    }

    public void setBright(int bright) {
        this.bright = bright;
    }

    public static class Builder {
        OssImgBright mParams = new OssImgBright();

        public Builder() {
        }

        public Builder setBright(int bright) {
            mParams.setBright(bright);
            return this;
        }

        public OssImgBright build() {
            return mParams;
        }
    }

}
