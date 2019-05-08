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
        super.parseFromJson(extra);
    }

    static class AnimationPrams {
        long duration;
        int width;
        int height;
        int left;
        int right;
        int top;
        int bottom;

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
