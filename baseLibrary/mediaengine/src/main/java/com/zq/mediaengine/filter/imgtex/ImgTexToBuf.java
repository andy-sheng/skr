package com.zq.mediaengine.filter.imgtex;

import android.annotation.TargetApi;
import android.graphics.PixelFormat;
import android.media.Image;
import android.media.ImageReader;
import android.opengl.EGLContext;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Build;
import android.os.ConditionVariable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Surface;

import com.zq.mediaengine.util.ColorFormatConvert;
import com.zq.mediaengine.framework.AVConst;
import com.zq.mediaengine.framework.ImgBufFormat;
import com.zq.mediaengine.framework.ImgBufFrame;
import com.zq.mediaengine.framework.ImgTexFormat;
import com.zq.mediaengine.framework.ImgTexFrame;
import com.zq.mediaengine.framework.SinkPin;
import com.zq.mediaengine.framework.SrcPin;
import com.zq.mediaengine.util.FrameBufferCache;
import com.zq.mediaengine.util.gles.EglCore;
import com.zq.mediaengine.util.gles.EglWindowSurface;
import com.zq.mediaengine.util.gles.GLRender;
import com.zq.mediaengine.util.gles.GlUtil;
import com.zq.mediaengine.util.gles.TexTransformUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Get I420/RGBA buffer from texture.
 */
@TargetApi(Build.VERSION_CODES.KITKAT)
public class ImgTexToBuf {
    private static final String TAG = "ImgTexToBuf";
    private static final boolean VERBOSE = false;

    // Simple fragment shader.
    private static final String FRAGMENT_SHADER_BODY = "" +
            "precision mediump float;\n" +
            "varying vec2 vTextureCoord;\n" +
            "\n" +
            "vec3 Rgb2Yuv( vec3 rgb ) {\n" +
            "    lowp float  y = rgb.x *  .299 + rgb.y *  .587 + rgb.z *  .1140 + 0.0;\n" +
            "    lowp float  u = rgb.x * -.169 + rgb.y * -.331 + rgb.z *  .4990 + 0.5;\n" +
            "    lowp float  v = rgb.x *  .499 + rgb.y * -.418 + rgb.z * -.0813 + 0.5;\n" +
            "    return vec3(y,u,v);\n" +
            "}\n" +
            "\n" +
            "void main() {\n" +
            "    gl_FragColor = vec4(Rgb2Yuv(texture2D(sTexture, vTextureCoord).rgb), 1);\n" +
            "}\n";

    public static final int ERROR_UNSUPPORTED = -1;
    public static final int ERROR_UNKNOWN = -2;

    private static final int CMD_FORMAT_CHANGED = 1;
    private static final int CMD_SEND_FRAME = 2;
    private static final int CMD_RELEASE = 3;

    public SinkPin<ImgTexFrame> mSinkPin;
    public SrcPin<ImgBufFrame> mSrcPin;

    private GLRender mGLRender;
    private boolean mInited;
    private EglCore mEglCore;
    private Surface mInputSurface;
    private EglWindowSurface mInputWindowSurface;
    private int mProgramId;

    private int mColorFormat = AVConst.PIX_FMT_I420;
    private ImgTexFormat mInputFormat;
    private ImageReader mImageReader;
    private FrameBufferCache mBufferCache;
    private ImgBufFormat mOutFormat;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private ConditionVariable mSig = new ConditionVariable();
    protected volatile boolean mStarted = false;

    protected AtomicInteger mFrameSent;
    protected AtomicInteger mFrameDropped;

    private final Handler mMainHandler;
    private ErrorListener mErrorListener;

    public interface ErrorListener {
        void onError(ImgTexToBuf imgTexToBuf, int err);
    }

    public ImgTexToBuf(GLRender glRender) {
        mSinkPin = new ImgTexSinkPin();
        mSrcPin = new SrcPin<>();
        mFrameDropped = new AtomicInteger(0);
        mFrameSent = new AtomicInteger(0);
        mMainHandler = new Handler(Looper.getMainLooper());
        mGLRender = glRender;
        mGLRender.addListener(mOnReadyListener);
        initThread();
    }

    public void setErrorListener(ErrorListener listener) {
        mErrorListener = listener;
    }

    /**
     * Set output color format.
     * Only AVConst.PIX_FMT_RGBA, AVConst.PIX_FMT_I420 and AVConst.PIX_FMT_BGR8 supported.
     *
     * @param colorFormat color format to set, default is AVConst.PIX_FMT_I420
     * @throws IllegalArgumentException
     */
    public void setOutputColorFormat(int colorFormat) {
        if (colorFormat != AVConst.PIX_FMT_RGBA && colorFormat != AVConst.PIX_FMT_I420 &&
                colorFormat != AVConst.PIX_FMT_BGR8) {
            throw new IllegalArgumentException("only FMT_RGBA or FMT_I420 supported!");
        }
        mColorFormat = colorFormat;
        mBufferCache = null;
    }

    public void resetFrameStat() {
        mFrameDropped.set(0);
        mFrameSent.set(0);
    }

    public int getFrameDropped() {
        return mFrameDropped.get();
    }

    public int getFrameSent() {
        return mFrameSent.get();
    }

    @Deprecated
    public void start() {
    }

    @Deprecated
    public void stop() {
    }

    public SinkPin<ImgTexFrame> getSinkPin() {
        return mSinkPin;
    }

    public SrcPin<ImgBufFrame> getSrcPin() {
        return mSrcPin;
    }

    public void release() {
        // do not block
        mSig.open();

        // remove GLRender listener
        mGLRender.removeListener(mOnReadyListener);

        // disconnect connected module
        mSrcPin.disconnect(true);

        if (mHandlerThread != null) {
            mHandler.sendEmptyMessage(CMD_RELEASE);
            try {
                mHandlerThread.join();
            } catch (Exception e) {
                Log.d(TAG, "ImgTexToBuf thread interrupted");
            } finally {
                mHandlerThread = null;
            }
        }

    }

    private GLRender.OnReadyListener mOnReadyListener = new GLRender.OnReadyListener() {
        @Override
        public void onReady() {
            mInited = false;
            mProgramId = 0;
        }
    };

    private void sendError(final int err) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mErrorListener != null) {
                    mErrorListener.onError(ImgTexToBuf.this, err);
                }
            }
        });
    }

    private void initThread() {
        mHandlerThread = new HandlerThread("ImgTexToBuf");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CMD_FORMAT_CHANGED: {
                        try {
                            doFormatChanged((ImgTexFormat) msg.obj);
                        } catch (Exception e) {
                            sendError(ERROR_UNSUPPORTED);
                        }
                        break;
                    }
                    case CMD_SEND_FRAME: {
                        try {
                            doSendFrame((ImgTexFrame) msg.obj);
                        } catch (Exception e) {
                            sendError(ERROR_UNKNOWN);
                        } finally {
                            mSig.open();
                        }
                        break;
                    }
                    case CMD_RELEASE: {
                        doRelease();
                        mHandlerThread.quit();
                        break;
                    }
                }
            }
        };
    }

    private class ImgTexSinkPin extends SinkPin<ImgTexFrame> {
        @Override
        synchronized public void onFormatChanged(Object format) {
            mStarted = true;
            Message msg = mHandler.obtainMessage(CMD_FORMAT_CHANGED, format);
            mHandler.sendMessage(msg);
        }

        @Override
        synchronized public void onFrameAvailable(ImgTexFrame frame) {
            if (!mStarted)
                return;
            if (mHandler.hasMessages(CMD_SEND_FRAME)) {
                Log.d(TAG, "total dropped: " + mFrameDropped.get() +
                        " total sent: " + mFrameSent.get());
                mFrameDropped.incrementAndGet();
                return;
            }

            mFrameSent.incrementAndGet();

            // We must do this on some Socs before render to encoder
            GLES20.glFinish();
            // lock fbo
            mGLRender.getFboManager().lock(frame.textureId);

            mSig.close();
            Message msg = mHandler.obtainMessage(CMD_SEND_FRAME, frame);
            mHandler.sendMessage(msg);
            // wait for ImageReader thread render complete
            mSig.block();
        }

        @Override
        synchronized public void onDisconnect(boolean recursive) {
            if (recursive) {
                release();
            }
        }
    }

    protected void doImageAvailable(final ImageReader reader) {
        Image image = reader.acquireNextImage();
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        int rowStride = image.getPlanes()[0].getRowStride();
        if (buffer != null) {
            long pts = image.getTimestamp() / 1000 / 1000;
            if (VERBOSE) {
                Log.d(TAG, "Acquired image buffer, pts:" + pts + " stride:" + rowStride);
            }
            if (mOutFormat == null) {
                mOutFormat = new ImgBufFormat(mColorFormat, mInputFormat.width,
                        mInputFormat.height, 0);
                if (mColorFormat == AVConst.PIX_FMT_RGBA) {
                    mOutFormat.stride = new int[1];
                    mOutFormat.stride[0] = rowStride;
                    mOutFormat.strideNum = 1;
                } else if(mColorFormat == AVConst.PIX_FMT_BGR8) {
                    mOutFormat.stride = new int[4];
                    mOutFormat.stride[0] = mInputFormat.width;
                    mOutFormat.stride[1] = 0;
                    mOutFormat.stride[2] = 0;
                    mOutFormat.stride[3] = 0;
                    mOutFormat.strideNum = 4;
                }
                mSrcPin.onFormatChanged(mOutFormat);
            }
            if (mColorFormat == AVConst.PIX_FMT_RGBA) {
                ImgBufFrame outFrame = new ImgBufFrame(mOutFormat, buffer, pts);
                mSrcPin.onFrameAvailable(outFrame);
            } else if(mColorFormat == AVConst.PIX_FMT_I420){
                int size = mOutFormat.width * mOutFormat.height * 3 / 2;
                if (mBufferCache == null) {
                    mBufferCache = new FrameBufferCache(0, size);
                }
                ByteBuffer outBuffer = mBufferCache.poll(size);
                if (outBuffer != null) {
                    ColorFormatConvert.YUVAToI420(buffer, rowStride, mOutFormat.width,
                            mOutFormat.height, outBuffer);
                    outBuffer.rewind();
                    ImgBufFrame outFrame = new ImgBufFrame(mOutFormat, mBufferCache,
                            outBuffer, pts);
                    mSrcPin.onFrameAvailable(outFrame);
                    outFrame.unref();
                }
            }
        }
        image.close();
    }

    private void doFormatChanged(ImgTexFormat texFormat) {
        if (mInputFormat != null && mImageReader != null &&
                (mInputFormat.width != texFormat.width ||
                        mInputFormat.height != texFormat.height)) {
            doRelease();
        }
        mInputFormat = texFormat;
        mOutFormat = null;
        if (mImageReader == null) {
            if (VERBOSE) {
                Log.d(TAG, "Create ImageReader " + mInputFormat.width + "x" + mInputFormat.height);
            }
            mImageReader = ImageReader.newInstance(mInputFormat.width,
                    mInputFormat.height, PixelFormat.RGBA_8888, 1);
            mInputSurface = mImageReader.getSurface();
            mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    try {
                        doImageAvailable(reader);
                    } catch (Exception e) {
                        sendError(ERROR_UNSUPPORTED);
                    }
                }
            }, mHandler);
        }
    }

    private void doRelease() {
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
        if (mProgramId != 0) {
            GLES20.glDeleteProgram(mProgramId);
            GLES20.glGetError();
            mProgramId = 0;
        }
        if (mInputWindowSurface != null) {
            mInputWindowSurface.release();
            mInputWindowSurface = null;
        }
        if (mEglCore != null) {
            mEglCore.release();
            mEglCore = null;
        }
        mOutFormat = null;
        mBufferCache = null;
        mInited = false;
    }

    private void doSendFrame(ImgTexFrame frame) {
        if (mImageReader != null) {
            if ((frame.flags & AVConst.FLAG_END_OF_STREAM) != 0)  {
                ImgBufFrame outFrame = new ImgBufFrame(mOutFormat, null, 0);
                outFrame.flags |= AVConst.FLAG_END_OF_STREAM;
                mSrcPin.onFrameAvailable(outFrame);
                return;
            }
            if (!mInited) {
                eglInit(mGLRender.getEGLContext());
                mInited = true;
            }
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            render(frame);
            GLES20.glFinish();
            mInputWindowSurface.setPresentationTime(frame.pts * 1000 * 1000);
            mInputWindowSurface.swapBuffers();
        }

        // unlock fbo
        mGLRender.getFboManager().unlock(frame.textureId);
    }

    private void eglInit(EGLContext eglContext) {
        if (mEglCore != null && mInputWindowSurface != null) {
            mInputWindowSurface.makeCurrent();
            mInputWindowSurface.releaseEglSurface();
            mEglCore.release();
            mEglCore = new EglCore(eglContext, 0);
            mInputWindowSurface.recreate(mEglCore);
        } else {
            mEglCore = new EglCore(eglContext, 0);
            mInputWindowSurface = new EglWindowSurface(mEglCore, mInputSurface);
        }
        mInputWindowSurface.makeCurrent();
        GLES20.glViewport(0, 0, mInputWindowSurface.getWidth(), mInputWindowSurface.getHeight());
    }

    private void render(ImgTexFrame frame) {
        ImgTexFormat format = frame.format;
        int textrueTarget = GLES20.GL_TEXTURE_2D;
        int uTexMatrixLoc;
        int aPositionLoc;
        int aTextureCoordLoc;
        int texture = frame.textureId;
        float[] texMatrix = frame.texMatrix;

        if (format.colorFormat == ImgTexFormat.COLOR_EXTERNAL_OES) {
            textrueTarget = GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
        }
        if (mProgramId == 0) {
            String vertexShader = GlUtil.BASE_VERTEX_SHADER;
            String fragmentShader;
            if (mColorFormat == AVConst.PIX_FMT_I420) {
                fragmentShader = FRAGMENT_SHADER_BODY;
            } else {
                fragmentShader = GlUtil.BASE_FRAGMENT_SHADER_BODY;
            }
            if (format.colorFormat == ImgTexFormat.COLOR_EXTERNAL_OES) {
                fragmentShader = GlUtil.FRAGMENT_SHADER_OES_HEADER + fragmentShader;
            } else {
                fragmentShader = GlUtil.FRAGMENT_SHADER_HEADER + fragmentShader;
            }
            mProgramId = GlUtil.createProgram(vertexShader, fragmentShader);
            if (mProgramId == 0) {
                Log.e(TAG, "Created program " + mProgramId + " failed");
                throw new RuntimeException("Unable to create program");
            }
        }
        aPositionLoc = GLES20.glGetAttribLocation(mProgramId, "aPosition");
        GlUtil.checkLocation(aPositionLoc, "aPosition");
        aTextureCoordLoc = GLES20.glGetAttribLocation(mProgramId, "aTextureCoord");
        GlUtil.checkLocation(aTextureCoordLoc, "aTextureCoord");
        uTexMatrixLoc = GLES20.glGetUniformLocation(mProgramId, "uTexMatrix");
        GlUtil.checkLocation(uTexMatrixLoc, "uTexMatrix");

        GlUtil.checkGlError("draw start");
        // Select the program.
        GLES20.glUseProgram(mProgramId);
        GlUtil.checkGlError("glUseProgram");

        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(textrueTarget, texture);

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(uTexMatrixLoc, 1, false, texMatrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(aPositionLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        GLES20.glVertexAttribPointer(aPositionLoc, TexTransformUtil.COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, TexTransformUtil.COORDS_STRIDE,
                TexTransformUtil.getVertexCoordsBuf());
        GlUtil.checkGlError("glVertexAttribPointer");

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(aTextureCoordLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(aTextureCoordLoc, TexTransformUtil.COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, TexTransformUtil.COORDS_STRIDE,
                TexTransformUtil.getTexCoordsBuf());
        GlUtil.checkGlError("glVertexAttribPointer");

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, TexTransformUtil.COORDS_COUNT);
        GlUtil.checkGlError("glDrawArrays");

        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(aPositionLoc);
        GLES20.glDisableVertexAttribArray(aTextureCoordLoc);

        GLES20.glBindTexture(textrueTarget, 0);
        GLES20.glUseProgram(0);
    }
}
