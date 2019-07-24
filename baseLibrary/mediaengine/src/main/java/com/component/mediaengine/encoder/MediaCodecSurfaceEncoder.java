package com.component.mediaengine.encoder;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.opengl.EGLContext;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;

import com.component.mediaengine.framework.AVConst;
import com.component.mediaengine.framework.ImgPacket;
import com.component.mediaengine.framework.ImgTexFormat;
import com.component.mediaengine.framework.ImgTexFrame;
import com.component.mediaengine.framework.VideoCodecFormat;
import com.component.mediaengine.util.gles.EglCore;
import com.component.mediaengine.util.gles.EglWindowSurface;
import com.component.mediaengine.util.gles.GLRender;
import com.component.mediaengine.util.gles.GlUtil;
import com.component.mediaengine.util.gles.TexTransformUtil;

import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * MediaCodec encoder with surface as input source.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class MediaCodecSurfaceEncoder extends MediaCodecEncoderBase<ImgTexFrame, ImgPacket> {
    private static final String TAG = "HWSurfaceEncoder";
    private static final boolean VERBOSE = false;

    private GLRender mGLRender;
    private boolean mEglInited;
    private EglCore mEglCore;
    private Surface mInputSurface;
    private EglWindowSurface mInputWindowSurface;
    private int mProgramId;

    // for dts calculate
    private float mFps;
    private BlockingQueue<Long> mPtsQueue;

    // out format
    private VideoCodecFormat mOutFormat;

    public MediaCodecSurfaceEncoder(GLRender glRender) {
        mGLRender = glRender;
        mGLRender.addListener(mOnReadyListener);
        mPtsQueue = new ArrayBlockingQueue<>(128);

        // default use sync encoding mode
        setUseSyncMode(true);
    }

    @Override
    public void release() {
        mGLRender.removeListener(mOnReadyListener);
        super.release();
    }

    @Override
    protected int doStart(Object encodeFormat) {
        VideoCodecFormat format = (VideoCodecFormat) encodeFormat;

        String mime;
        if (format.codecId == AVConst.CODEC_ID_AVC) {
            mime = "video/avc";
        } else if (format.codecId == AVConst.CODEC_ID_HEVC) {
            mime = "video/hevc";
        } else {
            return ENCODER_ERROR_UNSUPPORTED;
        }

        try {
            // Create a MediaCodec encoder
            mEncoder = MediaCodec.createEncoderByType(mime);
        } catch (Exception e) {
            if (format.codecId == AVConst.CODEC_ID_HEVC) {
                //fallback to avc
                Log.e(TAG, "do not support hevc, fallback to avc");
                format.codecId = AVConst.CODEC_ID_AVC;
                mime = "video/avc";
            }
            try {
                mEncoder = MediaCodec.createEncoderByType(mime);
            } catch (Exception ex) {
                Log.e(TAG, "Failed to start MediaCodec surface encoder");
                e.printStackTrace();
                return ENCODER_ERROR_UNSUPPORTED;
            }

        }

        MediaFormat mediaFormat = MediaFormat.createVideoFormat(mime,
                (format.width + 15) / 16 * 16, (format.height + 1) / 2 * 2);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, format.bitrate);
        mediaFormat.setInteger("bitrate-mode", format.bitrateMode);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE,
                (int) (format.frameRate + 0.5f));
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) {
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL,
                    (int) (format.iFrameInterval + 0.5f));
        } else {
            mediaFormat.setFloat(MediaFormat.KEY_I_FRAME_INTERVAL, format.iFrameInterval);
        }
        // avc profile
        int profile = MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline;
        if (format.codecId == AVConst.CODEC_ID_AVC) {
            int level = MediaCodecInfo.CodecProfileLevel.AVCLevel31;
            if (format.width * format.height > 1280 * 720) {
                level = MediaCodecInfo.CodecProfileLevel.AVCLevel4;
            }
            switch (format.profile) {
                case VideoCodecFormat.ENCODE_PROFILE_HIGH_PERFORMANCE:
                    profile = MediaCodecInfo.CodecProfileLevel.AVCProfileHigh;
                    break;
                case VideoCodecFormat.ENCODE_PROFILE_BALANCE:
                    profile = MediaCodecInfo.CodecProfileLevel.AVCProfileMain;
                    break;
                default:
                    break;
            }
            mediaFormat.setInteger("profile", profile);
            mediaFormat.setInteger("level", level);
        }
        Log.d(TAG, "MediaFormat: " + mediaFormat);

        try {
            // configure MediaCodec with our format.  Get a Surface
            // we can use for input and wrap it with a class that handles the EGL work.
            try {
                mEncoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            } catch (Exception e) {
                if (format.codecId == AVConst.CODEC_ID_AVC &&
                        profile != MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline) {
                    // retry with baseline profile
                    profile = MediaCodecInfo.CodecProfileLevel.AVCProfileBaseline;
                    mediaFormat.setInteger("profile", profile);
                    mEncoder.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                } else {
                    throw e;
                }
            }
            mInputSurface = mEncoder.createInputSurface();
            mEncoder.start();
        } catch (Exception e) {
            Log.e(TAG, "Failed to start MediaCodec surface encoder");
            e.printStackTrace();
            return ENCODER_ERROR_UNSUPPORTED;
        }

        // for dts calculate
        mFps = format.frameRate;
        mPtsQueue.clear();

        // trigger format changed event
        int width = mediaFormat.getInteger("width");
        int height = mediaFormat.getInteger("height");
        VideoCodecFormat outFormat = new VideoCodecFormat((VideoCodecFormat) mEncodeFormat);
        outFormat.width = width;
        outFormat.height = height;
        mOutFormat = outFormat;
        onEncodedFormatChanged(mOutFormat);

        return 0;
    }

    @Override
    protected void doStop() {
        try {
            mEncoder.signalEndOfInputStream();
        } catch (Exception e) {
            Log.e(TAG, "signalEndOfInputStream failed, ignore");
            e.printStackTrace();
        }
        try {
            //signalEndOfStream();
            drainEncoder(true);
        } catch (Exception e) {
            Log.e(TAG, "signal end of stream failed, ignore");
        }
        try {
            mEncoder.stop();
        } catch (Exception e) {
            // Also for Exynos socs
            Log.w(TAG, "stop encoder failed, ignore");
        }
        mEncoder.release();
        mEncoder = null;
        if (VERBOSE) Log.i(TAG, "MediaCodec released");

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

        mEglInited = false;
    }

    @Override
    protected boolean updateEncodeFormat(Object src, Object dst) {
        ImgTexFormat imgTexFormat = (ImgTexFormat) src;
        VideoCodecFormat encodeFormat = (VideoCodecFormat) dst;
        encodeFormat.width = imgTexFormat.width;
        encodeFormat.height = imgTexFormat.height;
        return true;
    }

    @Override
    protected void doFormatChanged(Object format) {
        ImgTexFormat imgTexFormat = (ImgTexFormat) format;
        VideoCodecFormat encodeFormat = (VideoCodecFormat) mEncodeFormat;
        if (getState() == ENCODER_STATE_ENCODING) {
            if (encodeFormat.width != imgTexFormat.width ||
                    encodeFormat.height != imgTexFormat.height) {
                Log.d(TAG, "restart encoder");
                doFlush();
                doStop();
                encodeFormat.width = imgTexFormat.width;
                encodeFormat.height = imgTexFormat.height;
                doStart(mEncodeFormat);
            }
        }
    }

    @Override
    protected ImgPacket getOutFrame(ByteBuffer buffer, MediaCodec.BufferInfo bufferInfo) {
        boolean isKeyFrame = false;
        boolean ignoreDts = buffer == null || buffer.limit() == 0;
        long pts = bufferInfo.presentationTimeUs / 1000;
        ImgPacket packet = new ImgPacket(mOutFormat, buffer, pts, pts);

        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
            packet.flags |= AVConst.FLAG_END_OF_STREAM;
        }
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_KEY_FRAME) != 0) {
            packet.flags |= AVConst.FLAG_KEY_FRAME;
            isKeyFrame = true;
        }
        if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
            packet.flags |= AVConst.FLAG_CODEC_CONFIG;
            ignoreDts = true;
        }

        if (!ignoreDts) {
            Long val = mPtsQueue.poll();
            if (val != null) {
                if (isKeyFrame && val != packet.pts) {
                    Log.w(TAG, "key frame dts calculate error! pts=" +
                            packet.pts + " val=" + val);
                }
                packet.dts = val - (long) (1000 / mFps);
                packet.dts = Math.min(packet.dts, packet.pts);
            } else {
                Log.e(TAG, "pts queue is empty while trying to cal dts!");
            }
        }
        if (VERBOSE) {
            Log.d(TAG, "video frame encoded size=" + (buffer == null ? 0 : buffer.limit()) +
                    " pts=" + packet.pts + " dts=" + packet.dts);
        }
        return packet;
    }

    @Override
    protected int doFrameAvailable(ImgTexFrame frame) {
        try {
            if (!mEglInited) {
                eglInit(mGLRender.getEGLContext());
                mEglInited = true;
            }
            drainEncoder(false);
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
            render(frame);
            GLES20.glFinish();

            if (mForceKeyFrame) {
                // we request a key frame "soon".
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    Log.d(TAG, "request key frame");
                    Bundle params = new Bundle();
                    params.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);
                    mEncoder.setParameters(params);
                }
                mForceKeyFrame = false;
            }

            mInputWindowSurface.setPresentationTime(frame.pts * 1000 * 1000);
            mInputWindowSurface.swapBuffers();
            if (!mPtsQueue.offer(frame.pts)) {
                Log.e(TAG, "offer pts failed!");
            }
            if (VERBOSE) {
                Log.d(TAG, "video frame in pts=" + frame.pts);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ENCODER_ERROR_UNKNOWN;
        } finally {
            mGLRender.getFboManager().unlock(frame.textureId);
        }
        return 0;
    }

    @Override
    protected void doFrameDropped(ImgTexFrame frame) {
        mGLRender.getFboManager().unlock(frame.textureId);
    }

    @Override
    protected boolean onFrameAvailable(ImgTexFrame frame) {
        // We must do this on some Socs before render to encoder
        GLES20.glFinish();
        mGLRender.getFboManager().lock(frame.textureId);
        return false;
    }

    private void eglInit(EGLContext eglContext) {
        if (mEglCore != null && mInputWindowSurface != null) {
            mInputWindowSurface.makeCurrent();
            mInputWindowSurface.releaseEglSurface();
            mEglCore.release();
            mEglCore = new EglCore(eglContext, EglCore.FLAG_RECORDABLE);
            mInputWindowSurface.recreate(mEglCore);
        } else {
            mEglCore = new EglCore(eglContext, EglCore.FLAG_RECORDABLE);
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
            if (format.colorFormat == ImgTexFormat.COLOR_EXTERNAL_OES) {
                fragmentShader = GlUtil.FRAGMENT_SHADER_OES_HEADER +
                        GlUtil.BASE_FRAGMENT_SHADER_BODY;
            } else {
                fragmentShader = GlUtil.FRAGMENT_SHADER_HEADER +
                        GlUtil.BASE_FRAGMENT_SHADER_BODY;
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

    private GLRender.OnReadyListener mOnReadyListener = new GLRender.OnReadyListener() {
        @Override
        public void onReady() {
            mEglInited = false;
            mProgramId = 0;
        }
    };
}
