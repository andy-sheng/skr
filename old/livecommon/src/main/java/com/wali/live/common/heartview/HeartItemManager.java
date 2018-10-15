package com.wali.live.common.heartview;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

import com.base.global.GlobalData;
import com.live.module.common.R;

import java.io.InputStream;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by chengsimin on 16/8/12.
 */
public class HeartItemManager {

    public static int[] sResIds = new int[]{
            R.drawable.live_icon_star_1,
            R.drawable.live_icon_star_2,
            R.drawable.live_icon_star_3,
            R.drawable.live_icon_star_4,
            R.drawable.live_icon_star_5
    };

    private static Bitmap[] sBitmaps = new Bitmap[5];

    static {
        for (int i = 0; i < sResIds.length; i++) {
            sBitmaps[i] = decode(sResIds[i]);
        }
    }

    private static Bitmap decode(int resId) {
        InputStream is = GlobalData.app().getResources().openRawResource(resId);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        return BitmapFactory.decodeStream(is, null, options);
    }

    private Bitmap[] mDrawBitMaps = sBitmaps;

    private float mScales[] = {0.8f, 1, 1.2f};

    private float mMaxAlpha = 1;

    private CopyOnWriteArrayList<HeartItem> mHeartItemCache;

    private int mMaxCount = 10;

    private int mWidth, mHeight;

    private Random mRandom;

    public HeartItemManager(int maxCount) {
        this.mMaxCount = maxCount;
        mHeartItemCache = new CopyOnWriteArrayList<>();
        mRandom = new Random();
    }

    public void setSize(int mWidth, int mHeight) {
        this.mWidth = mWidth;
        this.mHeight = mHeight;
    }

    public static int getResId(int index) {
        if (index < 0 || index >= sResIds.length) {
            return 0;
        }
        return sResIds[index];
    }

    public boolean addHeartItem(int colorIndex) {
        if (mWidth == 0 || mHeight == 0) {
            return false;
        }
        int len = mDrawBitMaps.length;

        Bitmap bitmap = mDrawBitMaps[colorIndex % len];

        if (bitmap == null) {
            return false;
        }
        for (HeartItem item : mHeartItemCache) {
            if (!item.isWorking) {
                item.init(bitmap, mWidth, mHeight);
                return true;
            }
        }
        if (mHeartItemCache.size() <= mMaxCount) {
            HeartItem item = new HeartItem(this);
            item.init(bitmap, mWidth, mHeight);
            mHeartItemCache.add(item);
            return true;
        }
        return false;
    }


    public boolean addHeartItemRandom() {
        int len = mDrawBitMaps.length;
        return addHeartItem(mRandom.nextInt(len));
    }

    public float getRandomScale() {
        int len = mScales.length;
        return mScales[mRandom.nextInt(len)];
    }

    public float getStarAlpha() {
        return mMaxAlpha;
    }

    // 画一帧
    public void onDraw(Canvas canvas) {
        for (HeartItem item : mHeartItemCache) {
            if (item.isWorking) {
                item.onDraw(canvas);
            }
        }
    }

    public void setDrawBitmaps(List<Bitmap> drawBitmaps) {
        if (drawBitmaps != null && !drawBitmaps.isEmpty()) {
            mDrawBitMaps = new Bitmap[drawBitmaps.size()];
            for (int i = 0; i < mDrawBitMaps.length && i < drawBitmaps.size(); i++) {
                mDrawBitMaps[i] = drawBitmaps.get(i);
            }
        }
    }

    public void resetDrawBitmaps() {
        mDrawBitMaps = sBitmaps;
    }

    public void setMaxAlpha(float maxAlpha) {
        this.mMaxAlpha = maxAlpha;
        if (mMaxAlpha < 0.0f) {
            mMaxAlpha = 0.0f;
        } else if (mMaxAlpha > 1.0f) {
            mMaxAlpha = 1.0f;
        }
    }
}
