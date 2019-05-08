package com.zq.mediaengine.filter.imgtex;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.zq.mediaengine.framework.CredtpModel;
import com.zq.mediaengine.framework.ImgTexFrame;
import com.zq.mediaengine.util.BitmapLoader;
import com.zq.mediaengine.util.gles.GLRender;
import com.zq.mediaengine.util.gles.GlUtil;

/**
 * Texture look up filter.
 *
 * @hide
 */

public class ImgBeautyLookUpFilter extends ImgTexFilter {
    private final Object mLookupBitmapLock = new Object();
    private Context mContext;
    private String mLookupBitmapUri;
    private Bitmap mLookupBitmap;
    private int mLookupTex[] = new int[]{ImgTexFrame.NO_TEXTURE};
    private float mIntensity = 0.5f;
    private int mLookupTexLoc;
    private int mIntensityLoc;

    public ImgBeautyLookUpFilter(GLRender glRender, Context context) {
        super(glRender, BASE_VERTEX_SHADER, CredtpModel.BEAUTY_LOOK_UP_FILTER);
        mContext = context;
    }

    public void setLookupBitmap(String uri) throws IllegalArgumentException {
        Bitmap bitmap = BitmapLoader.loadBitmap(mContext, uri, 0, 0);
        if (bitmap == null || bitmap.isRecycled()) {
            throw new IllegalArgumentException("Resource bitmap not valid!");
        }
        synchronized (mLookupBitmapLock) {
            mLookupBitmapUri = uri;
            mLookupBitmap = bitmap;
        }
    }

    public void setIntensity(float intensity) {
        mIntensity = intensity;
    }

    @Override
    protected void onInitialized() {
        mLookupTexLoc = getUniformLocation("lookUpTexture");
        mIntensityLoc = getUniformLocation("intensity");
        synchronized (mLookupBitmapLock) {
            if (mLookupBitmap == null || mLookupBitmap.isRecycled()) {
                mLookupBitmap = BitmapLoader.loadBitmap(mContext, mLookupBitmapUri, 0, 0);
                if (mLookupBitmap == null || mLookupBitmap.isRecycled()) {
                    throw new IllegalArgumentException("Resource bitmap not valid!");
                }
            }
            mLookupTex[0] = GlUtil.loadTexture(mLookupBitmap, ImgTexFrame.NO_TEXTURE);
            mLookupBitmap.recycle();
            mLookupBitmap = null;
        }
    }

    @Override
    protected void onDrawArraysPre() {
        // update lookup texture
        synchronized (mLookupBitmapLock) {
            if (mLookupBitmap != null && !mLookupBitmap.isRecycled()) {
                mLookupTex[0] = GlUtil.loadTexture(mLookupBitmap, mLookupTex[0]);
                mLookupBitmap.recycle();
                mLookupBitmap = null;
            }
        }

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mLookupTex[0]);
        GLES20.glUniform1i(mLookupTexLoc, 2);

        GLES20.glUniform1f(mIntensityLoc, mIntensity);
    }

    @Override
    protected void onDrawArraysAfter() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    @Override
    protected void onRelease() {
        super.onRelease();
        GLES20.glDeleteTextures(1, mLookupTex, 0);
        mLookupTex[0] = ImgTexFrame.NO_TEXTURE;
    }
}
