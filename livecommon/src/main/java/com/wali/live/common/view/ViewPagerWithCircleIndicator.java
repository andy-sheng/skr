package com.wali.live.common.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

import com.base.utils.display.DisplayUtils;

/**
 * Created by chengsimin on 16/2/20.
 */
public class ViewPagerWithCircleIndicator extends ViewPager {
    private static final String TAG = ViewPagerWithCircleIndicator.class.getSimpleName();

    public static final int MODE_CENTER = 1;
    public static final int MODE_RIGHT = 2;

    private int mMode = MODE_CENTER;

    private int mSelectedColor = 0xffe5aa1e, mUnSelectedColor = 0xffe5e5e5;

    private int mItemWidth = 20;
    private int mItemHeight = 20;

    private int mLimitHeight = 20;

    private int mItemIntever = 8; //item的间距8像素

    private Paint mPaint;

    private boolean mIsRepeatScroll;
    private int mActualCount;


    public ViewPagerWithCircleIndicator(Context context) {
        super(context);
        init(context);
    }

    public ViewPagerWithCircleIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ViewPagerWithCircleIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mPaint = new Paint();
    }

    public void setDrawCycleGravity(int mode) {
        this.mMode = mode;
    }

    public void setDrawCycleColor(int selectedColor, int unSelectedColor) {
        this.mSelectedColor = selectedColor;
        this.mUnSelectedColor = unSelectedColor;
    }

    public void setItemWidth(int itemWidthDp) {
        this.mItemWidth = itemWidthDp;
    }

    public void setItemHeight(int itemHeightDp) {
        this.mItemHeight = itemHeightDp;
    }

    public void setLimitHeight(int limitHeightDp) {
        this.mLimitHeight = limitHeightDp;
    }

    public void setRepeatScroll(boolean repeatScroll) {
        mIsRepeatScroll = repeatScroll;
    }

    public void setActualCount(int count) {
        mActualCount = count;
    }

    public void setmItemIntever(int mItemIntever) {
        this.mItemIntever = mItemIntever;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        drawCycle(canvas);
    }

    // 连续滑动
    private int getCircleCount() {
        if (mIsRepeatScroll) {
            return mActualCount;
        } else {
            if (getAdapter() != null) {
                return getAdapter().getCount();
            }
            return 0;
        }
    }

    // 考虑连续滑动，对连续滑动的
    private int getSelectedItem() {
        int index = getCurrentItem();
        if (mIsRepeatScroll) {
            return index % mActualCount;
        }
        return index;
    }

    private void drawCycle(Canvas canvas) {
        int count = getCircleCount();
        if (count == 0 || count == 1) {
            return;
        }

        canvas.save();
        canvas.translate(getScrollX(), getScrollY());

        float density = DisplayUtils.getDensity();

        int y = getHeight() - (int) ((mLimitHeight >> 1) * density);
        int minItemHeight =  DisplayUtils.dip2px(mItemHeight)/2; //半径

        int x = (getWidth() - (count-1) * 2 * minItemHeight - (count - 1) * mItemIntever) / 2;
        if (mMode == MODE_CENTER) {
            x = (getWidth() - (count-1) * 2 * minItemHeight - (count - 1) * mItemIntever) / 2;
        } else if (mMode == MODE_RIGHT) {
            x = getWidth() - count * 2 * minItemHeight - (count - 1) * mItemIntever - 30;
        }


        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);

        int select = getSelectedItem();
        int lastDrawItemX = x;
        for (int i = 0; i < count; i++) {
            if (select == i) {
                mPaint.setColor(mSelectedColor);
                canvas.drawCircle(lastDrawItemX, y, minItemHeight, mPaint);
            } else {
                mPaint.setColor(mUnSelectedColor);
                canvas.drawCircle(lastDrawItemX, y, minItemHeight, mPaint);
            }
            lastDrawItemX = lastDrawItemX + 2 * minItemHeight + mItemIntever;
        }

        canvas.restore();
    }
}
