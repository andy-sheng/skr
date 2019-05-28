package com.common.image.model.oss.effect;


import com.common.image.model.oss.IOssParam;
import com.common.image.model.oss.OssImgFactory;

/**
 * 图片锐化
 * 通过{@link OssImgFactory 使用}
 */
public class OssImgSharpen implements IOssParam {

    int sharpen = -1;//value	表示进行锐化处理。取值为锐化参数，参数越大，越清晰。	[50, 399] 为达到较优效果，推荐取值为 100。

    OssImgSharpen(){

    }

    @Override
    public String getOpDesc() {
        StringBuilder sb = new StringBuilder();
        sb.append("/sharpen");
        if (sharpen >= 50) {
            sb.append(",").append(sharpen);
        }
        return sb.toString();
    }

    public int getSharpen() {
        return sharpen;
    }

    public void setSharpen(int sharpen) {
        this.sharpen = sharpen;
    }

    public static class Builder {
        OssImgSharpen mParams = new OssImgSharpen();

        public Builder() {
        }

        public Builder setSharpen(int sharpen) {
            mParams.setSharpen(sharpen);
            return this;
        }

        public OssImgSharpen build() {
            return mParams;
        }
    }

}
