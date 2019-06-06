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
 * Adjust skin color filter.
 *
 * @hide
 */

public class ImgBeautyAdjustSkinColorFilter extends ImgTexFilter {

    private Context mContext;
    private String mWhitenBitmapUri;
    private Bitmap mWhitenBitmap;
    private String mRuddyBitmapUri;
    private Bitmap mRuddyBitmap;

    private int[] mWhitenTex = new int[]{ImgTexFrame.NO_TEXTURE};
    private int[] mRuddyTex = new int[]{ImgTexFrame.NO_TEXTURE};

    private int mWhitenTexLoc;
    //private int mRuddyTexLoc;
    private int mRuddyRatioLoc;

    public ImgBeautyAdjustSkinColorFilter(GLRender glRender, Context context,
                                          String whitenUri, String ruddyUri)
            throws IllegalArgumentException {
        super(glRender, BASE_VERTEX_SHADER, CredtpModel.BEAUTY_ADJ_SKIN_COLOR_FILTER);

        mWhitenBitmap = BitmapLoader.loadBitmap(context, whitenUri, 0, 0);
        if (mWhitenBitmap == null || mWhitenBitmap.isRecycled()) {
            throw new IllegalStateException("Resource bitmap not valid!");
        }
        mWhitenBitmapUri = whitenUri;

        //mRuddyBitmap = BitmapLoader.loadBitmap(context, ruddyUri, 0, 0);
        //if (mRuddyBitmap == null || mRuddyBitmap.isRecycled()) {
        //    throw new IllegalStateException("Resource bitmap not valid!");
        //}
        //mRuddyBitmapUri = ruddyUri;

        mContext = context;
    }

    @Override
    public boolean isRuddyRatioSupported() {
        return true;
    }

    /**
     * Set the ruddy ratio.
     *
     * @param ratio the ratio between -1.0f~1.0f
     */
    @Override
    public void setRuddyRatio(float ratio) {
        super.setRuddyRatio(ratio);
    }

    @Override
    protected void onInitialized() {
        mWhitenTexLoc = getUniformLocation("whitenTexture");
        //mRuddyTexLoc = getUniformLocation("ruddyTexture");
        mRuddyRatioLoc = getUniformLocation("skinColorRatio");

        if (mWhitenBitmap == null || mWhitenBitmap.isRecycled()) {
            mWhitenBitmap = BitmapLoader.loadBitmap(mContext, mWhitenBitmapUri, 0, 0);
            if (mWhitenBitmap == null || mWhitenBitmap.isRecycled()) {
                throw new IllegalStateException("Resource bitmap not valid!");
            }
        }
        mWhitenTex[0] = GlUtil.loadTexture(mWhitenBitmap, ImgTexFrame.NO_TEXTURE);
        mWhitenBitmap.recycle();
        mWhitenBitmap = null;

        //if (mRuddyBitmap == null || mRuddyBitmap.isRecycled()) {
        //    mRuddyBitmap = BitmapLoader.loadBitmap(mContext, mRuddyBitmapUri, 0, 0);
        //    if (mRuddyBitmap == null || mRuddyBitmap.isRecycled()) {
        //        throw new IllegalStateException("Resource bitmap not valid!");
        //    }
        //}
        //mRuddyTex[0] = GlUtil.loadTexture(mRuddyBitmap, ImgTexFrame.NO_TEXTURE);
        //mRuddyBitmap.recycle();
        //mRuddyBitmap = null;
    }

    @Override
    protected void onDrawArraysPre() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mWhitenTex[0]);
        GLES20.glUniform1i(mWhitenTexLoc, 2);

        //GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mRuddyTex[0]);
        //GLES20.glUniform1i(mRuddyTexLoc, 3);

        GLES20.glUniform1f(mRuddyRatioLoc, mRuddyRatio);
    }

    @Override
    protected void onDrawArraysAfter() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);

        //GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    @Override
    protected void onRelease() {
        super.onRelease();

        GLES20.glDeleteTextures(1, mWhitenTex, 0);
        mWhitenTex[0] = ImgTexFrame.NO_TEXTURE;

        //GLES20.glDeleteTextures(1, mRuddyTex, 0);
        //mRuddyTex[0] = ImgTexFrame.NO_TEXTURE;
    }
}
