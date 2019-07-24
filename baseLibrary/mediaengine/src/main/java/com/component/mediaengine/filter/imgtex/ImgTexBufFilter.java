package com.component.mediaengine.filter.imgtex;

import android.os.ConditionVariable;

import com.component.mediaengine.capture.ImgTexSrcPin;
import com.component.mediaengine.util.ColorFormatConvert;
import com.component.mediaengine.framework.AVConst;
import com.component.mediaengine.framework.ImgBufFormat;
import com.component.mediaengine.framework.ImgBufFrame;
import com.component.mediaengine.framework.ImgTexFrame;
import com.component.mediaengine.framework.SinkPin;
import com.component.mediaengine.framework.SrcPin;
import com.component.mediaengine.util.gles.GLRender;

import java.nio.ByteBuffer;

/**
 * format（RGBA or I420） buffer filter inserted in gpu pipe.
 * <p>
 * Use this filter would cause performance drop.
 */
public abstract class ImgTexBufFilter extends ImgFilterBase {
    private static final String TAG = "ImgTexBufFilter";

    private SinkPin<ImgTexFrame> mSinkPin;
    private ImgTexToBuf mImgTexToBuf;
    private ImgTexSrcPin mImgTexSrcPin;

    private ConditionVariable mBufferReady;
    private ImgBufFrame mBufFrame;

    private int mOutColorFormat = AVConst.PIX_FMT_RGBA;
    private ImgBufFormat mOutFormat;

    private ByteBuffer mOutBuffer;

    public ImgTexBufFilter(GLRender glRender, int format) {
        if (format != AVConst.PIX_FMT_I420 && format != AVConst.PIX_FMT_RGBA) {
            throw new IllegalArgumentException("only PIX_FMT_RGBA or PIX_FMT_I420 supported!");
        }

        mBufferReady = new ConditionVariable(true);
        mSinkPin = new SinkPin<ImgTexFrame>() {
            @Override
            public void onFormatChanged(Object format) {
                mImgTexToBuf.mSinkPin.onFormatChanged(format);

                // reset to trigger onFormatChanged event in next onFrameAvailable
                mImgTexSrcPin.reset();
            }

            @Override
            public void onFrameAvailable(ImgTexFrame frame) {
                mBufferReady.close();
                mImgTexToBuf.mSinkPin.onFrameAvailable(frame);
                mBufferReady.block();
                if (mBufFrame != null) {
                    mImgTexSrcPin.updateFrame(mBufFrame.buf, mBufFrame.format.stride[0],
                            mBufFrame.format.width, mBufFrame.format.height, mBufFrame.pts);
                }
                mBufFrame = null;
            }

            @Override
            public void onDisconnect(boolean recursive) {
                mBufferReady.open();
                mImgTexToBuf.mSinkPin.onDisconnect(recursive);
            }
        };

        mImgTexToBuf = new ImgTexToBuf(glRender);
        mImgTexSrcPin = new ImgTexSrcPin(glRender);
        SinkPin<ImgBufFrame> imgBufSinkPin = new SinkPin<ImgBufFrame>() {
            @Override
            public void onFormatChanged(Object format) {
                ImgBufFormat fmt = (ImgBufFormat) format;
                if (mOutFormat != null && (mOutFormat.width != fmt.width || mOutFormat.height !=
                        fmt.height)) {
                    mOutFormat = null;
                }

                if (mOutFormat == null) {
                    if (mOutColorFormat != AVConst.PIX_FMT_RGBA) {
                        int[] strides = new int[3];
                        strides[0] = fmt.width;
                        strides[1] = fmt.width / 2;
                        strides[2] = fmt.width / 2;
                        mOutFormat = new ImgBufFormat(mOutColorFormat, fmt.width, fmt.height,
                                fmt.orientation, strides);
                    } else {
                        mOutFormat = fmt;
                    }
                }

                onSizeChanged(mOutFormat.stride, mOutFormat.width, mOutFormat.height);
            }

            @Override
            public void onFrameAvailable(ImgBufFrame frame) {
                if (mOutColorFormat != AVConst.PIX_FMT_RGBA) {
                    //RGBA -> outColorFormat
                    int outSize = getOutSize(frame.format);
                    if (mOutBuffer != null && mOutBuffer.limit() < outSize) {
                        mOutBuffer = null;
                    }

                    if (mOutBuffer == null) {
                        mOutBuffer = ByteBuffer.allocateDirect(outSize);
                    }
                    if (mOutBuffer != null) {
                        mOutBuffer.clear();
                        rgbaConvertTo(frame);
                        mOutBuffer.rewind();
                    }

                    ByteBuffer filterBuffer = doFilter(mOutBuffer, mOutFormat.stride,
                            mOutFormat.width, mOutFormat.height);

                    frame.buf.clear();
                    convertToRgba(frame, filterBuffer);
                    frame.buf.rewind();
                } else {
                    frame.buf = doFilter(frame.buf, frame.format.stride,
                            frame.format.width, frame.format.height);

                }
                mBufFrame = frame;
                mBufferReady.open();
            }

            @Override
            public void onDisconnect(boolean recursive) {
                mImgTexSrcPin.disconnect(recursive);
            }
        };

        mOutColorFormat = format;
        mImgTexToBuf.setOutputColorFormat(AVConst.PIX_FMT_RGBA);
        mImgTexToBuf.mSrcPin.connect(imgBufSinkPin);
    }

    @Override
    public int getSinkPinNum() {
        return 1;
    }

    @Override
    public SinkPin<ImgTexFrame> getSinkPin(int idx) {
        return mSinkPin;
    }

    @Override
    public SrcPin<ImgTexFrame> getSrcPin() {
        return mImgTexSrcPin;
    }

    /**
     * Notify image size changed.
     *
     * @param stride the row stride for this plane, in bytes
     * @param width  the image width
     * @param height the image height
     */
    abstract protected void onSizeChanged(int[] stride, int width, int height);

    /**
     * Do cpu filter here.
     *
     * @param buffer the image buffer
     * @param stride the row stride for this plane, in bytes
     * @param width  the image width
     * @param height the image height
     * @return ByteBuffer with filtered data in same size as input.
     */
    abstract protected ByteBuffer doFilter(ByteBuffer buffer, int[] stride, int width, int height);

    private int getOutSize(ImgBufFormat srcFormat) {
        if (mOutColorFormat == AVConst.PIX_FMT_I420) {
            return srcFormat.width * srcFormat.height * 3 / 2;
        }
        return 0;
    }

    private void rgbaConvertTo(ImgBufFrame frame) {
        if (mOutColorFormat == AVConst.PIX_FMT_I420) {
            ColorFormatConvert.RGBAToI420(frame.buf, frame.format.stride[0], frame.format.width,
                    frame.format.height, mOutBuffer);
        }
    }

    private void convertToRgba(ImgBufFrame frame, ByteBuffer src) {
        if (mOutColorFormat == AVConst.PIX_FMT_I420) {
            ColorFormatConvert.I420ToRGBA(src, frame.format.stride[0], frame.format.width,
                    frame.format.height, frame.buf);
        }
    }
}
