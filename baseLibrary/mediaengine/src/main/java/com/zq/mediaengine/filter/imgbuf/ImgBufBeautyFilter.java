package com.zq.mediaengine.filter.imgbuf;

import com.zq.mediaengine.framework.ImgBufFormat;

/**
 * Denoise beauty filter running on cpu.
 */

public class ImgBufBeautyFilter extends ImgBufFilterBase {

    /**
     * Slightly beauty level.
     */
    public final static int BEAUTY_LEVEL_0 = 0;  //轻度美颜
    /**
     * Normal level.
     */
    public final static int BEAUTY_LEVEL_1 = 1;

    private ImgBufFormat mOutFormat;

    public ImgBufBeautyFilter(ImgPreProcessWrap imgPreProcessWrap) {
        super(imgPreProcessWrap);
    }

    /**
     * Set beauty level.
     *
     * @param beautyLevel available value: {@link #BEAUTY_LEVEL_1} {@link #BEAUTY_LEVEL_1}
     */
    public void setBeautyLevel(int beautyLevel) {
        mImagePreProcess.setBeautyLevel(beautyLevel);
    }

    @Override
    public int getSinkPinNum() {
        return DEFAULT_SINKPIN_NUM;
    }

    @Override
    public void release() {
        super.release();
    }

    @Override
    protected ImgBufFormat getSrcPinFormat() {
        return mOutFormat;
    }

    @Override
    protected void onFormatChanged(int idx, ImgBufFormat format) {
        mOutFormat = format;
    }

    @Override
    protected void doFilter() {
        mOutPutFrame = mImagePreProcess.processBeauty(mInputFrames[mMainSinkPinIndex]);
    }
}
