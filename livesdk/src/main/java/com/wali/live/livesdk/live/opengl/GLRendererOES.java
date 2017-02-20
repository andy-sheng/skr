package com.wali.live.livesdk.live.opengl;

import android.annotation.TargetApi;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.os.Build;

import com.base.log.MyLog;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Created by chenyong on 2017/1/11.
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class GLRendererOES {

    private static final String TAG = "GLRendererOES";

    private static final float COORDS[] = {
            -1.0F, 1.0F,
            0.0F, -1.0F,
            -1.0F, 0.0F,
            1.0F, -1.0F,
            0.0F, 1.0F,
            1.0F, 0.0F
    };
    private static final float TEX_COORDS[] = {
            0.0F, 1.0F,
            0.0F, 1.0F,
            0.0F, 0.0F,
            0.0F, 1.0F,
            1.0F, 0.0F,
            0.0F, 1.0F,
            1.0F, 1.0F,
            0.0F, 1.0F
    };

    private static final String VERTEX_SHADER =
            "attribute vec4 a_Position;\n" +
                    "attribute vec4 a_texCoord;\n" +
                    "uniform mat4 u_st_matrix;\n" +
                    "varying vec2 v_texCoord;\n" +
                    "void main() {\n" +
                    "    gl_Position = a_Position;\n" +
                    "    v_texCoord = (u_st_matrix * a_texCoord).xy;\n" +
                    "}\n";

    private static final String FRAGMENT_SHADER_EXT =
            "#extension GL_OES_EGL_image_external : require\n" +
                    "precision mediump float;\n" +
                    "varying vec2 v_texCoord;\n" +
                    "uniform samplerExternalOES s_texture;\n" +
                    "void main() {\n" +
                    "    gl_FragColor = texture2D(s_texture, v_texCoord);\n" +
                    "}\n";

    private FloatBuffer mPositionBuffer;
    private FloatBuffer mTexCoordBuffer;
    private int mAPosition;
    private int mATexCoord;
    private int mSTexture;
    private int mUSTMatrix;
    private boolean mHasInit = false;
    private int[] mTextures;
    private int mWidth;
    private int mHeight;
    private MOpenGL14 mMOpenGL14;
    private int mProgramHandle;

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        checkGlError("glCreateShader type=" + shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            MyLog.e(TAG, "Could not compile shader " + shaderType + ":");
            MyLog.e(TAG, " " + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
        }
        return shader;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int pixelShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        if (pixelShader == 0) {
            return 0;
        }

        int program = GLES20.glCreateProgram();
        checkGlError("glCreateProgram");
        if (program == 0) {
            MyLog.e(TAG, "Could not create program");
        }
        GLES20.glAttachShader(program, vertexShader);
        checkGlError("glAttachShader");
        GLES20.glAttachShader(program, pixelShader);
        checkGlError("glAttachShader");
        GLES20.glLinkProgram(program);
        int[] linkStatus = new int[1];
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            MyLog.e(TAG, "Could not link program: ");
            MyLog.e(TAG, GLES20.glGetProgramInfoLog(program));
            GLES20.glDeleteProgram(program);
            program = 0;
        }
        return program;
    }

    private void checkGlError(String op) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String msg = op + ": glError 0x" + Integer.toHexString(error);
            MyLog.e(TAG, msg);
            throw new RuntimeException(msg);
        }
    }

    public int[] createTextureObject() {
        int[] textures = new int[1];
        GLES20.glGenTextures(1, textures, 0);
        if (textures[0] == 0) {
            return null;
        }
        checkGlError("glGenTextures");
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textures[0]);
        checkGlError("glBindTexture " + textures[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MIN_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_S,
                GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, GLES20.GL_TEXTURE_WRAP_T,
                GLES20.GL_CLAMP_TO_EDGE);
        checkGlError("glTexParameter");
//        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
//        GLES20.glDisable(GLES20.GL_BLEND);
        return textures;
    }

    private void deleteTextureObject() {
        if (mTextures != null) {
            GLES20.glDeleteTextures(1, mTextures, 0);
            mTextures = null;
        }
    }

    public void release() {
        deleteTextureObject();
        if (mMOpenGL14 != null) {
            mMOpenGL14.release();
            mMOpenGL14 = null;
        }
        mHasInit = false;
    }

    public void draw(float[] matrix, long presentationTime) {
        if (!mHasInit && matrix == null) {
            return;
        }
        GLES20.glViewport(0, 0, mWidth, mHeight);
        checkGlError("glViewport");

        // Select the program.
        GLES20.glUseProgram(mProgramHandle);
        checkGlError("glUseProgram");

        GLES20.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        checkGlError("GL_COLOR_BUFFER_BIT");

        GLES20.glUniform1i(mSTexture, 0);
        checkGlError("glUniform1i, mTexSamplerHandle");
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        checkGlError("glActiveTexture, GL_TEXTURE0");
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, mTextures[0]);
        checkGlError("glBindTexture, GL_TEXTURE0");

        GLES20.glVertexAttribPointer(mAPosition, 3, GLES20.GL_FLOAT, false, 12, mPositionBuffer);
        checkGlError("glVertexAttribPointer, mVertexHandle");
        GLES20.glEnableVertexAttribArray(mAPosition);
        checkGlError("glEnableVertexAttribArray, mVertexHandle");
        GLES20.glVertexAttribPointer(mATexCoord, 4, GLES20.GL_FLOAT, false, 16, mTexCoordBuffer);
        checkGlError("glVertexAttribPointer, mTextureCoordHandle");
        GLES20.glEnableVertexAttribArray(mATexCoord);
        checkGlError("glEnableVertexAttribArray, mTextureCoordHandle");
        GLES20.glUniformMatrix4fv(mUSTMatrix, 1, false, matrix, 0);
        checkGlError("glUniformMatrix4fv");

//        mMOpenGL14.setPresentationTime(presentationTime);
//        mMOpenGL14.swapBuffers();
//        checkGlError("swapBuffers");
    }

    public boolean init(int width, int height) {
        mWidth = width;
        mHeight = height;
        if (mHasInit) {
            return false;
        }
        mMOpenGL14 = new MOpenGL14();
        mMOpenGL14.setSurface(width, height);

        ByteBuffer buffer = ByteBuffer.allocateDirect(COORDS.length * 4);
        buffer.order(ByteOrder.nativeOrder());
        mPositionBuffer = buffer.asFloatBuffer();
        mPositionBuffer.put(COORDS);
        mPositionBuffer.position(0);

        buffer = ByteBuffer.allocateDirect(TEX_COORDS.length * 4);
        buffer.order(ByteOrder.nativeOrder());
        mTexCoordBuffer = buffer.asFloatBuffer();
        mTexCoordBuffer.put(TEX_COORDS);
        mTexCoordBuffer.position(0);

        mProgramHandle = createProgram(VERTEX_SHADER, FRAGMENT_SHADER_EXT);
        if (mProgramHandle == 0) {
            release();
            return false;
        }
        mAPosition = GLES20.glGetAttribLocation(mProgramHandle, "a_Position");
        mATexCoord = GLES20.glGetAttribLocation(mProgramHandle, "a_texCoord");
        mSTexture = GLES20.glGetUniformLocation(mProgramHandle, "s_texture");
        mUSTMatrix = GLES20.glGetUniformLocation(mProgramHandle, "u_st_matrix");
        mTextures = createTextureObject();
        if (mTextures == null) {
            release();
            return false;
        }
        mHasInit = true;
        return true;
    }

    public int getVideoTexture() {
        return mTextures[0];
    }
}
