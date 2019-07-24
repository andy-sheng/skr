package com.example.videortc;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.bytedance.labcv.effectsdk.BytedEffectConstants;
import com.bytedance.labcv.effectsdk.RenderManager;
import com.component.mediaengine.util.gles.GLProgramLoadException;
import com.component.mediaengine.util.gles.GlUtil;
import com.component.mediaengine.util.gles.TexTransformUtil;

import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class BytedEffectKit1 {
    private static final String TAG = "BytedEffectKit";
    public static final String LICENSE_NAME = "labcv_test_20190523_20190630_com.bytedance.labcv.demo_labcv_test_v2.4.0.licbag";

    private Context mContext;
    private Handler mMainHandler;

    private GLSurfaceView mGLSurfaceView;
    private int mRenderWidth;
    private int mRenderHeight;

    private ImageLoader mImageLoader;
    private volatile boolean mInited;
    private volatile boolean mGlInited;
    private volatile boolean mStart;
    private int mInTexture;
    private int mTmpTexture;
    private int mTmpFrameBuffer;
    private int mOutTexture;

    private RenderManager mRenderManager;
    private int mProgramId;
    protected int muTexMatrixLoc;
    protected int maPositionLoc;
    protected int maTextureCoordLoc;
    private float[] mMatrix = new float[16];

    public BytedEffectKit1(Context context) {
        mContext = context;
        mMainHandler = new Handler(Looper.getMainLooper());

        mImageLoader = new ImageLoader(context, "/sdcard/bg.jpg");
        mRenderManager = new RenderManager();
    }

    public void setDisplayPreview(GLSurfaceView surfaceView) {
        surfaceView.setEGLContextClientVersion(2);
        surfaceView.setRenderer(mSurfaceViewRenderer);
        surfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        mGLSurfaceView = surfaceView;
    }

    public void start() {
        mStart = true;
        if (mGLSurfaceView != null) {
            mGLSurfaceView.requestRender();
        }
    }

    public void startEffect() {
        if (mGLSurfaceView != null) {
            mGLSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    String path;
                    boolean b;

//                    File[] reshapes = ResourceHelper.getReshapeResource();
//                    File[] beautys = ResourceHelper.getBeautyResources();
//                    if (beautys != null && beautys.length > 0) {
//                        path = beautys[0].getAbsolutePath();
//                        b = filter.setBeauty(path);
//                        Log.e(TAG, "setBeauty path=" + path + " ret=" + b);
//                        filter.updateIntensity(BytedEffectConstants.IntensityType.BeautyWhite.getId(), 1.0f);
//                        filter.updateIntensity(BytedEffectConstants.IntensityType.BeautySmooth.getId(), 1.0f);
//                        filter.updateIntensity(BytedEffectConstants.IntensityType.BeautySharp.getId(), 1.0f);
//                    }
//
//                    if (reshapes != null && reshapes.length > 0) {
//                        path = reshapes[0].getAbsolutePath();
//                        b = filter.setReshape(path);
//                        Log.e(TAG, "setReshape path=" + path + " ret=" + b);
//                        filter.updateReshape(1.0f, 1.0f);
//                    }

//                    File[] makeUpResource = ResourceHelper.getMakeUpResource();
//                    if (makeUpResource != null && makeUpResource.length > 0) {
//                        b = filter.setMakeUp(makeUpResource[0].getAbsolutePath());
//                        Log.e(TAG, "setMakeUp path=" + makeUpResource[0].getAbsolutePath() + " ret=" + b);
//                    }

                    path = ResourceHelper.getFilterResources()[5].getAbsolutePath();
                    b = mRenderManager.setFilter(path);
                    mRenderManager.updateIntensity(BytedEffectConstants.IntensityType.Filter.getId(), 1.0f);
                    Log.e(TAG, "setFilter path=" + path + " ret=" + b);
                    path = ResourceHelper.getStickerPath("6bc53e0a429951da45d55f91f01a9403");
                    b = mRenderManager.setSticker(path);
                    Log.e(TAG, "setSticker path=" + path + " ret=" + b);
                }
            });
        }
    }

    public void stopEffect() {
        if (mGLSurfaceView != null) {
            mGLSurfaceView.queueEvent(new Runnable() {
                @Override
                public void run() {
                    String path;
                    boolean b;

                    mRenderManager.setBeauty("");
                    mRenderManager.setReshape("");

                    path = ResourceHelper.getFilterResources()[0].getAbsolutePath();
                    b = mRenderManager.setFilter(path);
                    mRenderManager.updateIntensity(BytedEffectConstants.IntensityType.Filter.getId(), 0.0f);
                    Log.e(TAG, "setFilter path=" + path + " ret=" + b);
                    path = "";
                    b = mRenderManager.setSticker(path);
                    Log.e(TAG, "setSticker path=" + path + " ret=" + b);
                }
            });
        }
    }

    public void onPause() {
        if (mGLSurfaceView != null) {
            mGLSurfaceView.onPause();
        }
    }

    public void onResume() {
        if (mGLSurfaceView != null) {
            mGLSurfaceView.onResume();
        }
    }

    public void release() {
        mRenderManager.release();
    }

    private void glInit() {
        mInTexture = mImageLoader.getTexture();
        int[] val = createTexture2D(mImageLoader.getWidth(), mImageLoader.getHeight());
        mTmpTexture = val[0];
        mTmpFrameBuffer = val[1];
        mOutTexture = createTexture2D(mImageLoader.getWidth(), mImageLoader.getHeight())[0];
        Log.i(TAG, "glInit mInTexture: " + mInTexture + " mOutTexture: " + mOutTexture);

        int ret = mRenderManager.init(mContext, ResourceHelper.getModelDir(), ResourceHelper.getLicensePath(LICENSE_NAME));
        Log.i(TAG, "filter init ret = " + ret + " modelDir: " + ResourceHelper.getModelDir());
    }

    private void render() {
        GLES20.glViewport(0, 0, mImageLoader.getWidth(), mImageLoader.getHeight());
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mTmpFrameBuffer);
        glRender(mInTexture, mImageLoader.getTexMatrix());
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        boolean b = mRenderManager.process(mTmpTexture, mOutTexture, mImageLoader.getWidth(), mImageLoader.getHeight(), BytedEffectConstants.Rotation.CLOCKWISE_ROTATE_0);
        Log.i(TAG, "process in: " + mInTexture + " tmp: " + mTmpTexture + " out: " + mOutTexture + " ret: " + b);

        GLES20.glViewport(0, 0, mRenderWidth, mRenderHeight);
        Matrix.setIdentityM(mMatrix, 0);
        glRender(mOutTexture, mMatrix);
    }

    private void glRender(int texture, float[] texMatrix) {
        GlUtil.checkGlError("draw start");
        if (!mGlInited) {
            mProgramId = GlUtil.createProgram(GlUtil.BASE_VERTEX_SHADER, GlUtil.FRAGMENT_SHADER_HEADER + GlUtil.BASE_FRAGMENT_SHADER_BODY);
            if (mProgramId == 0) {
                Log.e(TAG, "Created program " + mProgramId + " failed");
                throw new GLProgramLoadException("Unable to create program");
            }
            maPositionLoc = GLES20.glGetAttribLocation(mProgramId, "aPosition");
            GlUtil.checkLocation(maPositionLoc, "aPosition");
            maTextureCoordLoc = GLES20.glGetAttribLocation(mProgramId, "aTextureCoord");
            GlUtil.checkLocation(maTextureCoordLoc, "aTextureCoord");
            muTexMatrixLoc = GLES20.glGetUniformLocation(mProgramId, "uTexMatrix");
            GlUtil.checkLocation(muTexMatrixLoc, "uTexMatrix");

            // Select the program.
            GLES20.glUseProgram(mProgramId);
            GlUtil.checkGlError("glUseProgram");

            GlUtil.checkGlError("onInitialized " + this);
            mInited = true;
        } else {
            // Select the program.
            GLES20.glUseProgram(mProgramId);
            GlUtil.checkGlError("glUseProgram");
        }

        // Set the texture.
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture);
        GlUtil.checkGlError("glBindTexture");

        // Copy the texture transformation matrix over.
        GLES20.glUniformMatrix4fv(muTexMatrixLoc, 1, false, texMatrix, 0);
        GlUtil.checkGlError("glUniformMatrix4fv");

        // Enable the "aPosition" vertex attribute.
        GLES20.glEnableVertexAttribArray(maPositionLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        GLES20.glVertexAttribPointer(maPositionLoc, TexTransformUtil.COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, TexTransformUtil.COORDS_STRIDE, getVertexCoords());
        GlUtil.checkGlError("glVertexAttribPointer");

        // Enable the "aTextureCoord" vertex attribute.
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
        GlUtil.checkGlError("glEnableVertexAttribArray");

        // Connect texBuffer to "aTextureCoord".
        GLES20.glVertexAttribPointer(maTextureCoordLoc, TexTransformUtil.COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, TexTransformUtil.COORDS_STRIDE, getTexCoords());
        GlUtil.checkGlError("glVertexAttribPointer");

        // Draw the rect.
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, TexTransformUtil.COORDS_COUNT);
        GlUtil.checkGlError("glDrawArrays");

        // Done -- disable vertex array, texture, and program.
        GLES20.glDisableVertexAttribArray(maPositionLoc);
        GLES20.glDisableVertexAttribArray(maTextureCoordLoc);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glUseProgram(0);
    }

    private FloatBuffer getVertexCoords() {
        return TexTransformUtil.getVertexCoordsBuf();
    }

    private FloatBuffer getTexCoords() {
        return TexTransformUtil.getTexCoordsBuf();
    }

    private GLSurfaceView.Renderer mSurfaceViewRenderer = new GLSurfaceView.Renderer() {

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.i(TAG, "onSurfaceCreated: ");
            mInited = false;
            mGlInited = false;
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.i(TAG, "onSurfaceChanged: " + width + "x" + height);
            mRenderWidth = width;
            mRenderHeight = height;
            GLES20.glViewport(0, 0, width, height);
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            Log.d(TAG, "onDrawFrame: ");

            if (!mStart) {
                return;
            }

            if (!mInited) {
                glInit();
                mInited = true;
            }

            render();

            mMainHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mGLSurfaceView.requestRender();
                }
            }, 33);
        }
    };

    public int[] createTexture2D(int width, int height) {
        int[] buffers = new int[1];
        int[] textures = new int[1];

        GLES20.glGenFramebuffers(1, buffers, 0);
        GLES20.glGenTextures(1, textures, 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textures[0]);
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, width, height, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D,
                GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, buffers[0]);
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, textures[0], 0);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        int[] ret = new int[2];
        ret[0] = textures[0];
        ret[1] = buffers[0];
        return ret;
    }
}
