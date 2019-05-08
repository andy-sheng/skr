package com.zq.mediaengine.filter.imgtex;

import android.content.Context;

import com.zq.mediaengine.util.gles.GLRender;

/**
 * Special effects filter.
 */

public class ImgBeautySpecialEffectsFilter extends ImgBeautyLookUpFilter {
    private static final String KSY_RES_PATH = "assets://Resource/";

    public static final int IMG_SPECIAL_EFFECT_FRESHY = 1;
    public static final int IMG_SPECIAL_EFFECT_BEAUTY = 2;
    public static final int IMG_SPECIAL_EFFECT_SWEETY = 3;
    public static final int IMG_SPECIAL_EFFECT_SEPIA = 4;
    public static final int IMG_SPECIAL_EFFECT_BLUE = 5;
    public static final int IMG_SPECIAL_EFFECT_NOSTALGIA = 6;
    public static final int IMG_SPECIAL_EFFECT_SAKURA = 7;
    public static final int IMG_SPECIAL_EFFECT_SAKURA_NIGHT = 8;
    public static final int IMG_SPECIAL_EFFECT_RUDDY_NIGHT = 9;
    public static final int IMG_SPECIAL_EFFECT_SUNSHINE_NIGHT = 10;
    public static final int IMG_SPECIAL_EFFECT_RUDDY = 11;
    public static final int IMG_SPECIAL_EFFECT_SUSHINE = 12;
    public static final int IMG_SPECIAL_EFFECT_NATURE = 13;
    public static final int IMG_SPECIAL_EFFECT_AMATORKA = 14;
    public static final int IMG_SPECIAL_EFFECT_ELEGANCE = 15;

    private int mSpecialType = 0;
    private String mSpecialName = null;
    private boolean mTakeEffect = false;

    public ImgBeautySpecialEffectsFilter(GLRender glRender, Context context, int idx)
            throws IllegalArgumentException {
        super(glRender, context);
        setSpecialEffect(idx);
    }

    ImgBeautySpecialEffectsFilter(GLRender glRender, Context context, String resName)
            throws IllegalArgumentException {
        super(glRender, context);
        setSpecialEffect(resName);
    }

    public void setSpecialEffect(int idx) {
        if (mSpecialType != idx) {
            mSpecialType = idx;
            setLookupBitmap(getBitmapUriFromIndex(idx));
        }
    }

    @Override
    protected void onRelease() {
        super.onRelease();
        mTakeEffect = false;
        mSpecialType = 0;
        mSpecialName = null;
    }

    public void setTakeEffect(boolean takeEffect) {
        mTakeEffect = takeEffect;
    }

    public String getSpecialName() {
        return mSpecialName;
    }

    private void setSpecialEffect(String resName) {
        setLookupBitmap(KSY_RES_PATH + resName);
    }

    private String getBitmapUriFromIndex(int idx) {
        String name;
        switch (idx) {
            case IMG_SPECIAL_EFFECT_FRESHY:
                name = "1_freshy";
                break;
            case IMG_SPECIAL_EFFECT_BEAUTY:
                name = "2_beauty";
                break;
            case IMG_SPECIAL_EFFECT_SWEETY:
                name = "3_sweety";
                break;
            case IMG_SPECIAL_EFFECT_SEPIA:
                name = "4_sepia";
                break;
            case IMG_SPECIAL_EFFECT_BLUE:
                name = "5_blue";
                break;
            case IMG_SPECIAL_EFFECT_NOSTALGIA:
                name = "6_nostalgia";
                break;
            case IMG_SPECIAL_EFFECT_SAKURA:
                name = "7_sakura";
                break;
            case IMG_SPECIAL_EFFECT_SAKURA_NIGHT:
                name = "8_sakura_night";
                break;
            case IMG_SPECIAL_EFFECT_RUDDY_NIGHT:
                name = "9_ruddy_night";
                break;
            case IMG_SPECIAL_EFFECT_SUNSHINE_NIGHT:
                name = "10_sunshine_night";
                break;
            case IMG_SPECIAL_EFFECT_RUDDY:
                name = "11_ruddy";
                break;
            case IMG_SPECIAL_EFFECT_SUSHINE:
                name = "12_sunshine";
                break;
            case IMG_SPECIAL_EFFECT_NATURE:
                name = "13_nature";
                break;
            case IMG_SPECIAL_EFFECT_AMATORKA:
                name = "14_amatorka";
                break;
            case IMG_SPECIAL_EFFECT_ELEGANCE:
                name = "15_elegance_1";
                break;
            default:
                name = null;
                break;
        }
        mSpecialName = name;
        return KSY_RES_PATH + name + ".png";
    }
}
