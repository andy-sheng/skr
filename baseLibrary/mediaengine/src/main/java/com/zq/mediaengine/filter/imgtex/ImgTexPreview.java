package com.zq.mediaengine.filter.imgtex;

import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.ConditionVariable;
import android.util.Log;
import android.view.TextureView;
import android.view.View;

import com.zq.mediaengine.framework.ImgTexFormat;
import com.zq.mediaengine.framework.ImgTexFrame;
import com.zq.mediaengine.framework.SinkPin;
import com.zq.mediaengine.util.gles.GLRender;
import com.zq.mediaengine.util.gles.GlUtil;
import com.zq.mediaengine.util.gles.TexTransformUtil;

/**
 * Texture frame previewer.
 */

public class ImgTexPreview {
    private static final String TAG = "ImgTexPreview";

    private SinkPin<ImgTexFrame> mSinkPin;

    private GLRender mParentGLRender;
    private GLRender mGLRender;

    private View mViewToInit;
    private final Object mInitViewLock = new Object();

    private int mProgramId;
    private ImgTexFrame mImgTexFrame;
    private ConditionVariable mSig = new ConditionVariable();

    private ImgTexFrame mLastImgTexFrame;
    private boolean mKeepFrameOnResume = false;

    public ImgTexPreview(GLRender glRender) {
        mParentGLRender = glRender;
        mSinkPin = new ImgTexPreviewSinkPin();
        mGLRender = new GLRender();

        if (mParentGLRender != null) {
            mParentGLRender.addListener(mOnParentReadyListener);
        }
        mGLRender.addListener(mOnReadyListener);
        mGLRender.addListener(mOnSizeChangedListener);
        mGLRender.addListener(mOnDrawFrameListener);
        mGLRender.addListener(mOnReleasedListener);
    }

    public SinkPin<ImgTexFrame> getSinkPin() {
        return mSinkPin;
    }

    public GLRender getGLRender() {
        return mGLRender;
    }

    public void setKeepFrameOnResume(boolean keepFrameOnResume) {
        mKeepFrameOnResume = keepFrameOnResume;
    }

    public void setDisplayPreview(GLSurfaceView glSurfaceView) {
        if (glSurfaceView == null) {
            mGLRender.release();
        } else {
            synchronized (mInitViewLock) {
                if (mParentGLRender == null) {
                    mGLRender.init(glSurfaceView);
                } else if (mParentGLRender.getState() == GLRender.STATE_READY) {
                    mViewToInit = null;
                    mGLRender.setInitEGL10Context(mParentGLRender.getEGL10Context());
                    mGLRender.init(glSurfaceView);
                } else {
                    mViewToInit = glSurfaceView;
                }
            }
        }
    }

    public void setDisplayPreview(TextureView textureView) {
        if (textureView == null) {
            mGLRender.release();
        } else {
            synchronized (mInitViewLock) {
                if (mParentGLRender == null) {
                    mGLRender.init(textureView);
                } else if (mParentGLRender.getState() == GLRender.STATE_READY) {
                    mViewToInit = null;
                    mGLRender.setInitEGL10Context(mParentGLRender.getEGL10Context());
                    mGLRender.init(textureView);
                } else {
                    mViewToInit = textureView;
                }
            }
        }
    }

    public View getDisplayPreview() {
        return mGLRender.getCurrentView();
    }

    public void onPause() {
        mGLRender.onPause();
    }

    public void onResume() {
        mGLRender.onResume();
    }

    public void release() {
        if (mParentGLRender != null) {
            mParentGLRender.removeListener(mOnParentReadyListener);
        }
        mGLRender.release();
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
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
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
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private GLRender.OnReadyListener mOnParentReadyListener = new GLRender.OnReadyListener() {
        @Override
        public void onReady() {
            Log.d(TAG, "onParentReady");
            synchronized (mInitViewLock) {
                if (mViewToInit != null) {
                    mGLRender.setInitEGL10Context(mParentGLRender.getEGL10Context());
                    if (mViewToInit instanceof GLSurfaceView) {
                        mGLRender.init((GLSurfaceView) mViewToInit);
                    } else if (mViewToInit instanceof TextureView) {
                        mGLRender.init((TextureView) mViewToInit);
                    }
                    mViewToInit = null;
                }
            }
        }
    };

    private GLRender.OnReadyListener mOnReadyListener = new GLRender.OnReadyListener() {
        @Override
        public void onReady() {
            Log.d(TAG, "onReady");
            mProgramId = 0;
            if (mKeepFrameOnResume && mLastImgTexFrame != null) {
                mGLRender.requestRender();
            }
        }
    };

    private GLRender.OnSizeChangedListener mOnSizeChangedListener =
            new GLRender.OnSizeChangedListener() {
        @Override
        public void onSizeChanged(int width, int height) {
            Log.d(TAG, "onSizeChanged " + width + "x" + height);
        }
    };

    private GLRender.OnDrawFrameListener mOnDrawFrameListener = new GLRender.OnDrawFrameListener() {
        @Override
        public void onDrawFrame() {
            if (mImgTexFrame != null) {
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                render(mImgTexFrame);
                GLES20.glFinish();
                mImgTexFrame = null;
                mSig.open();
            }
            if (mKeepFrameOnResume && mLastImgTexFrame != null) {
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                render(mLastImgTexFrame);
                GLES20.glFinish();
            }
        }
    };

    private GLRender.OnReleasedListener mOnReleasedListener = new GLRender.OnReleasedListener() {
        @Override
        public void onReleased() {
            mSig.open();
        }
    };

    private class ImgTexPreviewSinkPin extends SinkPin<ImgTexFrame> {

        @Override
        public void onFormatChanged(Object format) {

        }

        @Override
        public void onFrameAvailable(ImgTexFrame frame) {
            if (mGLRender != null) {
                mImgTexFrame = frame;
                mSig.close();
                if (mGLRender.getState() == GLRender.STATE_READY) {
                    // We must do this on some Socs before render
                    GLES20.glFinish();
                    mGLRender.requestRender();
                    mSig.block();
                    mLastImgTexFrame = frame;
                }
            }
        }

        @Override
        public synchronized void onDisconnect(boolean recursive) {
            super.onDisconnect(recursive);
            if (recursive) {
                release();
            }
        }
    }
}
