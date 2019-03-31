package com.common.view.ex.shadow;

/**
 * Created by wanjian on 2017/9/14.
 */

public class ShadowConfig {

    private ShadowConfig() {
    }

    public static ShadowConfig obtain() {
        return new ShadowConfig();
    }

    public static ShadowConfig obtain(ShadowConfig config) {
        ShadowConfig b = obtain();
        b.color = config.color;
        b.xOffset = config.xOffset;
        b.yOffset = config.yOffset;
        b.radius = config.radius;
        b.leftTopCorner = config.leftTopCorner;
        b.rightTopCorner = config.rightTopCorner;
        b.rightBottomCorner = config.rightBottomCorner;
        b.leftBottomCorner = config.leftBottomCorner;
        return b;
    }

    int color;
    int xOffset;
    int yOffset;
    float radius;
    int leftTopCorner;
    int rightTopCorner;
    int rightBottomCorner;
    int leftBottomCorner;

    /**
     * @param color 阴影颜色
     * @return this
     */
    public ShadowConfig color(int color) {
        this.color = color;
        return this;
    }

    /**
     * @param xOffset x轴偏移量
     * @return this
     */
    public ShadowConfig xOffset(int xOffset) {
        this.xOffset = xOffset;
        return this;
    }

    /**
     * @param yOffset y轴偏移量
     * @return this
     */
    public ShadowConfig yOffset(int yOffset) {
        this.yOffset = yOffset;
        return this;
    }

    /**
     * @param radius 模糊半径
     * @return this
     */
    public ShadowConfig radius(float radius) {
        if (radius < 1e-6) {
            radius = 0.01f;
        }
        this.radius = radius;
        return this;
    }

    public ShadowConfig leftTopCorner(int leftTopCorner) {
        this.leftTopCorner = leftTopCorner;
        return this;
    }

    public ShadowConfig rightTopCorner(int rightTopCorner) {
        this.rightTopCorner = rightTopCorner;
        return this;
    }

    public ShadowConfig rightBottomCorner(int rightBottomCorner) {
        this.rightBottomCorner = rightBottomCorner;
        return this;
    }

    public ShadowConfig leftBottomCorner(int leftBottomCorner) {
        this.leftBottomCorner = leftBottomCorner;
        return this;
    }

    void recycle() {
//        color = 0;
//        xOffset = 0;
//        yOffset = 0;
//        radius = 0;
//        leftTopCorner = 0;
//        rightTopCorner = 0;
//        rightBottomCorner = 0;
//        leftBottomCorner = 0;
    }

    @Override
    public String toString() {
        return "ShadowConfig{" +
                "color=" + color +
                ", xOffset=" + xOffset +
                ", yOffset=" + yOffset +
                ", radius=" + radius +
                ", leftTopCorner=" + leftTopCorner +
                ", rightTopCorner=" + rightTopCorner +
                ", rightBottomCorner=" + rightBottomCorner +
                ", leftBottomCorner=" + leftBottomCorner +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ShadowConfig)) {
            return false;
        }
        ShadowConfig b = ((ShadowConfig) obj);
        if (obj == this ||
                b.color == color &&
                        b.xOffset == xOffset &&
                        b.yOffset == yOffset &&
                        b.radius == radius &&
                        b.leftTopCorner == leftTopCorner &&
                        b.rightTopCorner == rightTopCorner &&
                        b.rightBottomCorner == rightBottomCorner &&
                        b.leftBottomCorner == leftBottomCorner
        ) {
            return true;
        }

        return false;
    }
}