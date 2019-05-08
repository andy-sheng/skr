package com.zq.mediaengine.filter.imgbuf;

import com.zq.mediaengine.framework.AVConst;
import com.zq.mediaengine.framework.ImgBufFormat;
import com.zq.mediaengine.framework.ImgBufFrame;

import java.util.UnknownFormatFlagsException;


/**
 * Cpu filter to crop, rotate, scale and mirror input frame.
 * With one input pin and one output pin.<br/>
 * <p>
 * Crop, rotate, and scale would be handled automatically by the set target size and
 * the format of input frame. Mirror need to be set by developer.
 */

public class ImgBufScaleFilter extends ImgBufFilterBase {

    private ImgBufFormat mOutFormat;

    public ImgBufScaleFilter(ImgPreProcessWrap imgPreProcessWrap) {
        super(imgPreProcessWrap);
        mOutFormat = new ImgBufFormat(AVConst.PIX_FMT_I420, 0, 0, 0);
    }

    public ImgBufScaleFilter() {
        super();
        mOutFormat = new ImgBufFormat(AVConst.PIX_FMT_I420, 0, 0, 0);
    }

    /**
     * Set output target resolution.
     *
     * @param width  target width
     * @param height target height
     */
    public void setTargetSize(int width, int height) {
        mImagePreProcess.setPresetInfo(width, height);
        mOutFormat.width = width;
        mOutFormat.height = height;
    }

    /**
     * Mirror input frame or not.
     *
     * @param mirror true to enable mirror, false to disable
     */
    public void setMirror(boolean mirror) {
        mImagePreProcess.setIsFrontCameraMirror(mirror);
    }

    /**
     * Set outputFormat after scale, should be ImgBufFormat.FMT_I420 or ImgBufFormat.FMT_RGBA
     * @param outputFormat
     * @throws UnknownFormatFlagsException
     */
    public void setOutputFormat(int outputFormat) throws UnknownFormatFlagsException{
        if (outputFormat != AVConst.PIX_FMT_I420 && outputFormat != AVConst.PIX_FMT_RGBA) {
            throw new UnknownFormatFlagsException("format should be I420 or RGBA");
        }
        mOutFormat.pixFmt = outputFormat;
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
        if (mOutFormat.width == 0 || mOutFormat.height == 0) {
            mOutFormat.width = format.width;
            mOutFormat.height = format.height;
        }
    }

    @Override
    protected void doFilter() {
        ImgBufFrame inputFrame = mInputFrames[mMainSinkPinIndex];
        if (inputFrame.format.equals(mOutFormat)) {
            mOutPutFrame = inputFrame;
            return;
        }

        if (inputFrame.buf == null || inputFrame.buf.limit() == 0) {
            mOutPutFrame = new ImgBufFrame(inputFrame);
            mOutPutFrame.format = mOutFormat;
        } else {
            if (mOutFormat.pixFmt == AVConst.PIX_FMT_I420) {
                mOutPutFrame = mImagePreProcess.processScale(inputFrame);
            } else if (mOutFormat.pixFmt == AVConst.PIX_FMT_RGBA) {
                mOutPutFrame = mImagePreProcess.processScaleAndConvert2RGBA
                        (mInputFrames[mMainSinkPinIndex]);
            }
        }
    }
}
