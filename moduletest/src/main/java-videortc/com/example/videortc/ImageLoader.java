package com.example.videortc;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.zq.mediaengine.util.BitmapLoader;
import com.zq.mediaengine.util.gles.GlUtil;
import com.zq.mediaengine.util.gles.TexTransformUtil;

public class ImageLoader {
    private static final String TAG = "ImageLoader";

    Context mContext;
    String mUrl;
    int mTextureId = -1;
    int mWidth;
    int mHeight;
    float[] mTexMatrix = new float[16];

    public ImageLoader(Context context, String url) {
        mContext = context;
        mUrl = url;
    }

    public int getTexture() {
        if (mTextureId == -1) {
            mTextureId = loadTexture();
        }
        return mTextureId;
    }

    private int loadTexture() {
        Bitmap bitmap = BitmapLoader.loadBitmap(mContext, mUrl);
        mWidth = bitmap.getWidth();
        mHeight = bitmap.getHeight();
        mTextureId = GlUtil.loadTexture(bitmap, mTextureId);
        bitmap.recycle();
        TexTransformUtil.calTransformMatrix(mTexMatrix, 1, 1, 0);
//        Matrix.setIdentityM(mTexMatrix, 0);
        Log.d(TAG, mUrl + " load to texture: " + mTextureId);
        return mTextureId;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public float[] getTexMatrix() {
        return mTexMatrix;
    }
}
