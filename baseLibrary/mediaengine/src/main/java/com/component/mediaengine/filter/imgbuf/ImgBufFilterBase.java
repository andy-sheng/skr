package com.component.mediaengine.filter.imgbuf;

import com.component.mediaengine.framework.ImgBufFormat;
import com.component.mediaengine.framework.ImgBufFrame;
import com.component.mediaengine.framework.SinkPin;
import com.component.mediaengine.framework.SrcPin;

import java.util.LinkedList;
import java.util.List;

/**
 * Base class of video filters running on cpu,
 * with multi or one input pins and one output pin.
 */

public abstract class ImgBufFilterBase {
    private static final String TAG = "ImgBufFilterBase";

    protected static final int DEFAULT_SINKPIN_NUM = 1;
    protected static final int DEFAULT_SRCPIN_NUM = 1;
    /**
     * Input pins
     */
    private final List<SinkPin<ImgBufFrame>> mSinkPins;
    /**
     * Output pin
     */
    private final SrcPin<ImgBufFrame> mSrcPin;  //各级的输出数据目前只有一个

    //只有当主输入数据输入时,才开始触发所有输入数据的处理,主输入数据一般都是camera后的数据,而不是附加数据
    protected int mMainSinkPinIndex = 0;
    protected ImgBufFrame[] mInputFrames;  //原始输入数据缓存
    protected ImgBufFrame mOutPutFrame;   //输出数据
    protected ImgPreProcessWrap mImagePreProcess;

    private boolean mReleased = false;
    private ImgBufFormat mLastOutFormat;

    public ImgBufFilterBase(ImgPreProcessWrap imgPreProcessWrap) {
        mSinkPins = new LinkedList<>();
        mSrcPin = new SrcPin<>();
        for (int i = 0; i < getSinkPinNum(); i++) {
            mSinkPins.add(new ImgBufFilterSinkPin(i));
        }

        mInputFrames = new ImgBufFrame[getSinkPinNum()];
        mImagePreProcess = imgPreProcessWrap;
    }

    public ImgBufFilterBase() {
        mSinkPins = new LinkedList<>();
        mSrcPin = new SrcPin<>();
        for (int i = 0; i < getSinkPinNum(); i++) {
            mSinkPins.add(new ImgBufFilterSinkPin(i));
        }

        mInputFrames = new ImgBufFrame[getSinkPinNum()];
        mImagePreProcess = new ImgPreProcessWrap();
    }

    /**
     * Set main input pin, generally the camera input
     *
     * @param index index of the main input pin, default 0
     */
    public final void setMainSinkPinIndex(int index) {
        mMainSinkPinIndex = index;
    }

    public final int getMainSinkPinIndex() {
        return mMainSinkPinIndex;
    }

    /**
     * Get default SinkPin instance.<br/>
     * Same as getSinkPin(mMainSinkPinIndex).
     *
     * @return the default SinkPin instance
     */
    public SinkPin<ImgBufFrame> getSinkPin() {
        return getSinkPin(mMainSinkPinIndex);
    }

    /**
     * Get sink pin by index
     *
     * @param index index
     * @return SinPin object or null
     */
    public SinkPin<ImgBufFrame> getSinkPin(int index) {
        if (mSinkPins.size() > index) {
            return mSinkPins.get(index);
        }

        return null;
    }

    /**
     * Get output pin.
     *
     * @return the output SrcPin instance
     */
    public SrcPin<ImgBufFrame> getSrcPin() {
        return mSrcPin;
    }

    /**
     * Get sink pin number, must be implement by child
     *
     * @return sink pin number
     */
    abstract public int getSinkPinNum();

    /**
     * Handle format changed event by inherit this method.
     *
     * @param idx    index of the format changed pin
     * @param format the new format
     */
    protected void onFormatChanged(int idx, ImgBufFormat format) {
    }

    /**
     * Get output pin format, child class must return valid value after
     * onFormatChanged(mMainSinkPinIndex, ...) called
     *
     * @return request output pin format
     */
    abstract protected ImgBufFormat getSrcPinFormat();

    /**
     * Do actual filter, driven by the frame of main sink pin arrived.
     */
    abstract protected void doFilter();

    synchronized public void release() {
        if(!mReleased) {
            mSinkPins.clear();
            mSrcPin.disconnect(true);
            mReleased = true;
        }
    }

    private class ImgBufFilterSinkPin extends SinkPin<ImgBufFrame> {
        private int mIndex;

        public ImgBufFilterSinkPin(int index) {
            mIndex = index;
        }

        @Override
        public void onFormatChanged(Object format) {
            ImgBufFilterBase.this.onFormatChanged(mIndex, (ImgBufFormat) format);
            if (mIndex == mMainSinkPinIndex) {
                ImgBufFormat outFormat = getSrcPinFormat();
                mSrcPin.onFormatChanged(outFormat);
            }
        }

        @Override
        public void onFrameAvailable(ImgBufFrame frame) {
            mInputFrames[mIndex] = frame;
            if (mIndex == mMainSinkPinIndex) {
                doFilter();
                ImgBufFormat outFormat = mOutPutFrame.format;
                if (mLastOutFormat == null || !outFormat.equals(mLastOutFormat)) {
                    mLastOutFormat = outFormat;
                    mSrcPin.onFormatChanged(outFormat);
                }
                mSrcPin.onFrameAvailable(mOutPutFrame);
            }
        }

        @Override
        public void onDisconnect(boolean recursive) {
            if (mIndex == mMainSinkPinIndex) {
                if (recursive) {
                    release();
                }
            }
        }
    }
}
