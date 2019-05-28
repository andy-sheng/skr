package com.zq.mediaengine.filter.imgtex;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.util.Log;

import com.zq.mediaengine.framework.CredtpModel;
import com.zq.mediaengine.framework.ImgTexFrame;
import com.zq.mediaengine.util.BitmapLoader;
import com.zq.mediaengine.util.gles.GLRender;
import com.zq.mediaengine.util.gles.GlUtil;

/**
 * Beauty style filter
 */

public class ImgBeautyStylizeFilter extends ImgTexFilter {
    private final static String TAG = "ImgBeautyStylizeFilter";
    public static final String KSY_RES_PATH = "assets://KSYResource/";

    public static final int KSY_FILTER_STYLE_1977 = 0;
    public static final int KSY_FILTER_STYLE_AMARO = 1;
    public static final int KSY_FILTER_STYLE_BRANNAN = 2;
    public static final int KSY_FILTER_STYLE_EARLY_BIRD = 3;
    public static final int KSY_FILTER_STYLE_HEFE = 4;
    public static final int KSY_FILTER_STYLE_HUDSON = 5;
    public static final int KSY_FILTER_STYLE_INK = 6;
    public static final int KSY_FILTER_STYLE_LOMO = 7;
    public static final int KSY_FILTER_STYLE_LORD_KELVIN = 8;
    public static final int KSY_FILTER_STYLE_NASHVILLE = 9;
    public static final int KSY_FILTER_STYLE_RISE = 10;
    public static final int KSY_FILTER_STYLE_SIERRA = 11;
    public static final int KSY_FILTER_STYLE_SUTRO = 12;
    public static final int KSY_FILTER_STYLE_TOASTER = 13;
    public static final int KSY_FILTER_STYLE_VALENCIA = 14;
    public static final int KSY_FILTER_STYLE_WALDEN = 15;
    public static final int KSY_FILTER_STYLE_XPROLL = 16;

    protected int[] GL_TEXTURES = {GLES20.GL_TEXTURE3, GLES20.GL_TEXTURE4, GLES20.GL_TEXTURE5,
            GLES20.GL_TEXTURE6, GLES20.GL_TEXTURE7, GLES20.GL_TEXTURE8};

    private static String mBitmapArray[][] = {
            {"map_1977"},
            {"blackboard_1024", "overlay_map", "amaro_map"},
            {"brannan_process", "brannan_blowout", "brannan_contrast", "brannan_luma", "brannan_screen"},
            {"early_bird_curves", "earlybird_overlay_map", "vignette_map", "earlybird_blowout", "earlybird_map"},
            {"edge_burn", "hefe_map", "hefe_soft_light", "hefe_metal"},
            {"hudson_background", "overlay_map", "hudson_map"},
            {"inkwell_map"},
            {"lomo_map", "vignette_map"},
            {"kelvin_map"},
            {"nashwi"},
            {"blackboard_1024", "overlay_map", "rise_map"},
            {"sierra_vignette", "overlay_map", "sierra_map"},
            {"vignette_map", "sutro_metal", "soft_light", "sutro_edge_burn", "sutro_curves"},
            {"toaster_metal", "toaster_soft_light", "toaster_curves", "toaster_overlay_map_warm", "toaster_color_shift"},
            {"valencia_map", "valencia_gradient_map"},
            {"walden_map", "vignette_map"},
            {"xpro_map", "vignette_map"}};

    private static int mFragmentShader[] = {CredtpModel.BEAUTY_1977_FILTER,
            CredtpModel.BEAUTY_AMARO_FILTER, CredtpModel.BEAUTY_BRANNAN_FILTER,
            CredtpModel.BEAUTY_EARLY_BIRD_FILTER, CredtpModel.BEAUTY_HEFE_FILTER,
            CredtpModel.BEAUTY_HUDSON_FILTER, CredtpModel.BEAUTY_INK_FILTER,
            CredtpModel.BEAUTY_LOMO_FILTER, CredtpModel.BEAUTY_LORD_KELVIN_FILTER,
            CredtpModel.BEAUTY_NASHVILLE_FILTER, CredtpModel.BEAUTY_RISE_FILTER,
            CredtpModel.BEAUTY_SIERRA_FILTER, CredtpModel.BEAUTY_SUTRO_FILTER,
            CredtpModel.BEAUTY_TOASTER_FILTER, CredtpModel.BEAUTY_VALENCIA_FILTER,
            CredtpModel.BEAUTY_WALDEN_FILTER, CredtpModel.BEAUTY_XPROLL_FILTER,
    };

    protected int mTexNum;
    protected int[] mInputTexLoc;
    protected int[] mInputTex;
    protected Bitmap[] mBitmaps;
    private int mFilterId;

    public ImgBeautyStylizeFilter(GLRender glRender, Context context, int id) {
        super(glRender, BASE_VERTEX_SHADER, mFragmentShader[id]);
        mFilterId = id;
        String bitmaps[] = mBitmapArray[id];
        mTexNum = bitmaps.length;
        mInputTexLoc = new int[mTexNum];
        mInputTex = new int[mTexNum];
        mBitmaps = new Bitmap[mTexNum];

        for (int i = 0; i < mTexNum; i++) {
            mInputTex[i] = ImgTexFrame.NO_TEXTURE;
            mBitmaps[i] = BitmapLoader.loadBitmap(context, KSY_RES_PATH + bitmaps[i] + ".png");
            Log.d(TAG, "ImgStyleBaseFilter: bitmap " + bitmaps[i]);
        }
    }

    @Override
    public void onInitialized() {
        super.onInitialized();
        int k;
        for (int i = 0; i < mTexNum; i++) {
            k = i + 2;
            mInputTexLoc[i] = getUniformLocation(String.format("inputImageTexture%d", k));
            GlUtil.checkGlError(String.format("inputImageTexture%d", k));

            if (mBitmaps[i] != null && !mBitmaps[i].isRecycled()) {
                GLES20.glActiveTexture(GL_TEXTURES[i]);
                mInputTex[i] = GlUtil.loadTexture(mBitmaps[i], ImgTexFrame.NO_TEXTURE);

                mBitmaps[i].recycle();
                mBitmaps[i] = null;
            }
        }
    }

    @Override
    protected void onDrawArraysPre() {
        for (int i = 0; i < mTexNum; i++) {
            GLES20.glActiveTexture(GL_TEXTURES[i]);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mInputTex[i]);
            GLES20.glUniform1i(mInputTexLoc[i], i + 3);
        }
    }

    @Override
    public void onRelease() {
        super.onRelease();
        if (mTexNum > 0) {
            GLES20.glDeleteTextures(mTexNum, mInputTex, 0);
        }
    }

    public int getStyleFilterId() {
        return mFilterId;
    }
}
