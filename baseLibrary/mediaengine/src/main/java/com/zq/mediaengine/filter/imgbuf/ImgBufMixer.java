package com.zq.mediaengine.filter.imgbuf;

import android.graphics.RectF;

import com.zq.mediaengine.framework.AVConst;
import com.zq.mediaengine.framework.ImgBufFormat;

/**
 * Video frame mixer running on cpu.
 */

public class ImgBufMixer extends ImgBufFilterBase {
    private final static int MIXER_SINK_PIN_NUM = 8;
    private RectF[] mRenderRects;
    private ImgPreProcessWrap.ImgBufMixerConfig[] mMixerConfigParams;
    private boolean mNeedMixer;

    private int mOutWidth, mOutHeight;

    public ImgBufMixer(ImgPreProcessWrap imgPreProcessWrap) {
        super(imgPreProcessWrap);
        mRenderRects = new RectF[getSinkPinNum()];
        mMixerConfigParams = new ImgPreProcessWrap.ImgBufMixerConfig[getSinkPinNum()];
    }

    /**
     * Set mixer output frame resolution.
     *
     * @param outWidth output width
     * @param outHeight output height
     */
    public void setTargetSize(int outWidth, int outHeight) {
        mOutWidth = outWidth;
        mOutHeight = outHeight;
        for (int i = 0; i < mRenderRects.length; i++) {
            RectF rect = mRenderRects[i];
            ImgPreProcessWrap.ImgBufMixerConfig params = mMixerConfigParams[i];
            if (rect != null && params != null) {
                if (params.x == 0)
                    params.x = (int) (rect.left * mOutWidth);
                if (params.y == 0)
                    params.y = (int) (rect.top * mOutHeight);
                if (params.w == 0)
                    params.w = ((int) (rect.width() * mOutWidth)) / 2 * 2;
                if (params.h == 0)
                    params.h = ((int) (rect.height() * mOutHeight)) / 2 * 2;
            }
        }
    }

    /**
     * Set render rect to specific input pin.
     *
     * @param idx dedicated sink pin index
     * @param rect render rect of input frame from this pin, should be in (0, 0, 1, 1)
     * @param alpha alpha value to use while mixing frame from this pin
     */
    public void setRenderRect(int idx, RectF rect, float alpha) {
        if (idx < getSinkPinNum()) {
            mRenderRects[idx] = rect;
            ImgPreProcessWrap.ImgBufMixerConfig params =
                    new ImgPreProcessWrap.ImgBufMixerConfig(
                            (int) (rect.left * mOutWidth),
                            (int) (rect.top * mOutHeight),
                            ((int) (rect.width() * mOutWidth)) / 2 * 2,
                            ((int) (rect.height() * mOutHeight)) / 2 * 2,
                            (int) (alpha * 255));
            mMixerConfigParams[idx] = params;
        }

        if (idx > 0) {
            mNeedMixer = true;
        }
    }

    /**
     * Set render rect to specific input pin.
     *
     * @param idx dedicated sink pin index
     * @param x x position for left top of frame from this pin, should be 0~1
     * @param y y position for left top of frame from this pin, should be 0~1
     * @param w width for frame from this pin to show, should be 0~1
     * @param h height for frame from this pin to show, should be 0~1
     * @param alpha alpha value to use while mixing frame from this pin
     */
    public void setRenderRect(int idx, float x, float y, float w, float h, float alpha) {
        RectF rect = new RectF(x, y, x + w, y + h);
        setRenderRect(idx, rect, alpha);
    }

    /**
     * Get max input pin number of current mixer.
     *
     * @return the max input pin number.
     */
    @Override
    public int getSinkPinNum() {
        return MIXER_SINK_PIN_NUM;
    }

    @Override
    public void release() {
        super.release();
    }

    @Override
    protected ImgBufFormat getSrcPinFormat() {
        return new ImgBufFormat(AVConst.PIX_FMT_I420, mOutWidth, mOutHeight, 0);
    }

    @Override
    protected void onFormatChanged(int idx, ImgBufFormat format) {
        if (mMixerConfigParams[idx] != null) {
//            if (mMixerConfigParams[idx].w == 0) {
//                mMixerConfigParams[idx].w = mMixerConfigParams[idx].h *
//                        format.height / format.width;
//                mMixerConfigParams[idx].w = mMixerConfigParams[idx].w / 2 * 2;
//            } else if (mMixerConfigParams[idx].h == 0) {
//                mMixerConfigParams[idx].h = mMixerConfigParams[idx].w *
//                        format.width / format.height;
//                mMixerConfigParams[idx].h = mMixerConfigParams[idx].h / 2 * 2;
//            }
            mMixerConfigParams[idx].w = format.width;
            mMixerConfigParams[idx].h = format.height;
        }
    }

    @Override
    protected void doFilter() {
        if (mNeedMixer && checkInputFrames()) {
            mOutPutFrame = mImagePreProcess.processMixer(mInputFrames, mMixerConfigParams);
        } else {
            mOutPutFrame = mInputFrames[mMainSinkPinIndex];
        }
    }

    private boolean checkInputFrames() {
        for (int i = 1; i < mInputFrames.length; i++) {
            if (mInputFrames[i] != null) {
                return true;
            }
        }
        return false;
    }
}
