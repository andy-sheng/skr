package com.component.mediaengine.filter.imgbuf;

import com.component.mediaengine.framework.ImgBufFrame;
import com.component.mediaengine.util.LibraryLoader;

import java.nio.ByteBuffer;

/**
 * @hide
 */

public class ImgPreProcessWrap {
    private boolean mIsFrontCameraMirror = false;
    private int mBeautyLevel = ImgBufBeautyFilter.BEAUTY_LEVEL_1;
    private long mImgProcessInstance = 0;

    public ImgPreProcessWrap() {
        mImgProcessInstance = create();
    }

    /****************************
     * 视频预处理需要的参数设置
     ********************************************/
    public void setPresetInfo(int width, int height) {
        setTargetSize(mImgProcessInstance, width, height);
    }

    public void setIsFrontCameraMirror(boolean isFrontCameraMirror) {
        if (mIsFrontCameraMirror != isFrontCameraMirror) {
            mIsFrontCameraMirror = isFrontCameraMirror;
            updateIsFrontMirror(mImgProcessInstance, isFrontCameraMirror);
        }
    }

    public void setBeautyLevel(int beautyLevel) {
        if (mBeautyLevel != beautyLevel) {
            mBeautyLevel = beautyLevel;
            setBeautyInfo(mImgProcessInstance, mBeautyLevel);
        }
    }

    /************************************************************************/

    public ImgBufFrame processScale(ImgBufFrame src) {
        return doScale(mImgProcessInstance, src);
    }

    public ImgBufFrame processScaleAndConvert2RGBA(ImgBufFrame src) {
        return doScaleAndConvert2RGBA(mImgProcessInstance, src);
    }

    public ImgBufFrame processBeauty(ImgBufFrame src) {
        return doBeauty(mImgProcessInstance, src);
    }

    public ImgBufFrame processMixer(ImgBufFrame[] srcMixerInfo, ImgBufMixerConfig[] params) {
        return doMixer(mImgProcessInstance, srcMixerInfo, srcMixerInfo.length, params, params
                .length);
    }

    public ImgBufFrame convertToNv21(ImgBufFrame src) {
        return convertI420ToNv21(mImgProcessInstance, src);
    }

    public void release() {
        if(mImgProcessInstance != 0) {
            releaseInfo(mImgProcessInstance);
            mImgProcessInstance = 0;
        }
    }

    private native long create();

    private native ImgBufFrame doScale(long instance, ImgBufFrame src);

    private native ImgBufFrame doScaleAndConvert2RGBA(long instance, ImgBufFrame src);

    private native ImgBufFrame doBeauty(long instance, ImgBufFrame src);

    private native ImgBufFrame doMixer(long instance, ImgBufFrame[] srcMixerInfo, int srcNum, ImgBufMixerConfig[] params, int configNum);

    private native void setTargetSize(long instance, int width, int height);

    private native void updateIsFrontMirror(long instance, boolean isFrontMirror);

    private native void setBeautyInfo(long instance, int beautyLevel);

    private native void releaseInfo(long instance);

    //these method just for test
    public native void priteByteBuffer(long instance, ByteBuffer buffer);

    public native void debugScaleFlag(long instance, boolean testScale);

    public native void debugBeautyFlag(long instance, boolean testScale);

    public native void debugMixerFlag(long instance, boolean testScale);

    private native ImgBufFrame convertI420ToNv21(long instance, ImgBufFrame src);


    static public class ImgBufMixerConfig {
        public int x;
        public int y;
        public int w;
        public int h;
        public int color;
        public int alpha;

        public ImgBufMixerConfig(int x, int y, int w, int h, int alpha) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.color = 0;
            this.alpha = alpha;
        }

        public ImgBufMixerConfig(int x, int y, int w, int h, int color, int alpha) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.color = color;
            this.alpha = alpha;
        }

        public ImgBufMixerConfig() {

        }
    }

    static {
        LibraryLoader.load();
    }
}
