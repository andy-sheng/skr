package com.common.image.model.oss;


/**
 * 裁剪
 * 通过{@link OssPsFactory 使用}
 */
public class OssImgCrop implements IOssParam {

    int x = -1;//指定裁剪起点横坐标（默认左上角为原点）

    int y = -1;//指定裁剪起点纵坐标（默认左上角为原点）

    int w = -1;//指定裁剪宽度

    int h = -1;//指定裁剪高度

    /**
     * 设置裁剪的原点位置，由九宫格的格式，一共有九个地方可以设置，
     * 每个位置位于每个九宫格的左上角
     * [nw, north, ne, west, center, east, sw, south, se]
     */
    String g;

    OssImgCrop(){

    }

    @Override
    public String getOpDesc() {
        StringBuilder sb = new StringBuilder();
        sb.append("/crop");
        if (x > 0) {
            sb.append(",x_").append(x);
        }
        if (y > 0) {
            sb.append(",y_").append(y);
        }
        if (w > 0) {
            sb.append(",w_").append(w);
        }
        if (h > 0) {
            sb.append(",h_").append(h);
        }
        return sb.toString();
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

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public String getG() {
        return g;
    }

    public void setG(String g) {
        this.g = g;
    }

    public static class Builder {
        OssImgCrop mParams = new OssImgCrop();

        Builder() {
        }

        public Builder setW(int w) {
            mParams.setW(w);
            return this;
        }

        public Builder setH(int h) {
            mParams.setH(h);
            return this;
        }

        public Builder setX(int x) {
            mParams.setX(x);
            return this;
        }

        public Builder setY(int y) {
            mParams.setY(y);
            return this;
        }

        public Builder setG(String g) {
            mParams.setG(g);
            return this;
        }

        public OssImgCrop build() {
            return mParams;
        }
    }

}
