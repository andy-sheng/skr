package com.module.playways.room.gift.model;

import com.alibaba.fastjson.JSON;

public class AnimationGift extends BaseGift {

    AnimationPrams mAnimationPrams;

    public AnimationPrams getAnimationPrams() {
        return mAnimationPrams;
    }

    public void setAnimationPrams(AnimationPrams animationPrams) {
        mAnimationPrams = animationPrams;
    }

//    @Override
//    public String packetToJson() {
//        return JSON.toJSONString(mAnimationPrams);
//    }

    @Override
    public void parseFromJson(String extra) {
        AnimationPrams animationPrams = JSON.parseObject(extra, AnimationPrams.class);
        setAnimationPrams(animationPrams);
    }

    public static class AnimationPrams {
        boolean isFullScreen; //是否全屏
        boolean isFullX;      //true 水平平铺  false 垂直平铺
        long duration;        //播放时长
        int width;            //宽度
        int height;           //高度
        // 距离-1都为无效值
        int left;             //距左边高度
        int right;            //距右边高度
        int top;              //距顶部高度
        int bottom;           //距底部高度

        public boolean isFullScreen() {
            return isFullScreen;
        }

        public void setFullScreen(boolean fullScreen) {
            isFullScreen = fullScreen;
        }

        public boolean isFullX() {
            return isFullX;
        }

        public void setFullX(boolean fullX) {
            isFullX = fullX;
        }

        public long getDuration() {
            return duration;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getLeft() {
            return left;
        }

        public void setLeft(int left) {
            this.left = left;
        }

        public int getRight() {
            return right;
        }

        public void setRight(int right) {
            this.right = right;
        }

        public int getTop() {
            return top;
        }

        public void setTop(int top) {
            this.top = top;
        }

        public int getBottom() {
            return bottom;
        }

        public void setBottom(int bottom) {
            this.bottom = bottom;
        }
    }
}
