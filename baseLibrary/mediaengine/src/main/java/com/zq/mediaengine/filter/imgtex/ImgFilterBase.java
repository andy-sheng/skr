package com.zq.mediaengine.filter.imgtex;

import com.zq.mediaengine.framework.ImgTexFrame;
import com.zq.mediaengine.framework.SinkPin;
import com.zq.mediaengine.framework.SrcPin;

/**
 * The base class of gpu filters.
 */
abstract public class ImgFilterBase {

    /**
     * The main sinkPin index.
     */
    protected int mMainSinkPinIndex;
    /**
     * The error listener.
     */
    protected OnErrorListener mErrorListener;

    /**
     * The grind ratio.
     */
    protected float mGrindRatio = 0.5f;
    /**
     * The whiten ratio.
     */
    protected float mWhitenRatio = 0.5f;
    /**
     * The ruddy ratio.
     */
    protected float mRuddyRatio = 0.5f;

    /**
     * Error interface.
     */
    public interface OnErrorListener {
        /**
         * On error.
         *
         * @param filter the filter
         * @param errno  the errno
         */
        void onError(ImgTexFilterBase filter, int errno);
    }

    /**
     * Get sink pin number, must be implement by child
     *
     * @return sink pin number
     */
    abstract public int getSinkPinNum();

    /**
     * Get the input port of filter module by index.
     *
     * @param idx index of SinkPin to get
     * @return the sinkPin
     */
    abstract public SinkPin<ImgTexFrame> getSinkPin(int idx);

    /**
     * Get the default input port of filter module.
     *
     * @return the sinkPin
     */
    public SinkPin<ImgTexFrame> getSinkPin() {
        return getSinkPin(mMainSinkPinIndex);
    }

    /**
     * Get the output port of filter module.
     *
     * @return the srcPin
     */
    abstract public SrcPin<ImgTexFrame> getSrcPin();

    /**
     * Set error listener, app should use other filter instead of current one after error occur.
     *
     * @param listener error listener.
     */
    public void setOnErrorListener(OnErrorListener listener) {
        mErrorListener = listener;
    }

    /**
     * Set main input pin, generally the camera input
     *
     * @param index index of the main input pin, default 0
     */
    public final void setMainSinkPinIndex(int index) {
        mMainSinkPinIndex = index;
    }

    /**
     * Is grind ratio supported to be changed in this filter.
     *
     * @return the boolean
     */
    public boolean isGrindRatioSupported() {
        return false;
    }

    /**
     * Is whiten ratio supported to be changed in this filter.
     *
     * @return the boolean
     */
    public boolean isWhitenRatioSupported() {
        return false;
    }

    /**
     * Is ruddy ratio supported to be changed in this filter.
     *
     * @return the boolean
     */
    public boolean isRuddyRatioSupported() {
        return false;
    }

    /**
     * Set grind ratio.
     * Only take effect when {@link #isGrindRatioSupported()} returned true.
     *
     * @param ratio the ratio normally between 0.0f~1.0f
     */
    public void setGrindRatio(float ratio) {
        mGrindRatio = ratio;
    }

    /**
     * Gets grind ratio.
     *
     * @return the grind ratio
     */
    public float getGrindRatio() {
        return mGrindRatio;
    }

    /**
     * Set whiten ratio.
     * Only take effect when {@link #isWhitenRatioSupported()} returned true.
     *
     * @param ratio the ratio normally between 0.0f~1.0f
     */
    public void setWhitenRatio(float ratio) {
        mWhitenRatio = ratio;
    }

    /**
     * Gets whiten ratio.
     *
     * @return the whiten ratio
     */
    public float getWhitenRatio() {
        return mWhitenRatio;
    }

    /**
     * Set ruddy ratio.
     * Only take effect when {@link #isRuddyRatioSupported()} returned true.
     *
     * @param ratio the ratio normally between 0.0f~1.0f
     */
    public void setRuddyRatio(float ratio) {
        mRuddyRatio = ratio;
    }

    /**
     * Gets ruddy ratio.
     *
     * @return the ruddy ratio
     */
    public float getRuddyRatio() {
        return mRuddyRatio;
    }

    /**
     * Version number of current filter.
     *
     * @return version number like "1.0"
     */
    public String getVersion() {
        return "1.0";
    }

    /**
     * Release current filter.
     */
    public void release() {
    }
}
