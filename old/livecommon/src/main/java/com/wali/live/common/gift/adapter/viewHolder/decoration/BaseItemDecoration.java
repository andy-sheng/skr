package com.wali.live.common.gift.adapter.viewHolder.decoration;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.base.global.GlobalData;
import com.base.utils.display.DisplayUtils;
import com.live.module.common.R;

/**
 * Created by zjn on 16-7-6.
 *
 * @module RecyclerView辅助类
 */
public class BaseItemDecoration extends RecyclerView.ItemDecoration {
    public final String TAG = getTAG();

    public static final int HORIZONTAL_LIST = 1;
    public static final int VERTICAL_LIST = 2;

    protected int mOrientation;

    /**
     * item之间分割线的size
     */
    protected int mItemSize;

    /**
     * 绘制item分割线的画笔
     */
    protected Paint mPaint;

    protected String getTAG() {
        return getClass().getSimpleName();
    }

    public BaseItemDecoration(int orientation) {
        initItemSize();
        initPaint();
        setOrientation(orientation);
    }

    public void setOrientation(int orientation) {
        mOrientation = orientation;
    }

    protected void initItemSize() {
        mItemSize = DisplayUtils.dip2px(0.33f);
    }

    protected void initPaint() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(GlobalData.app().getResources().getColor(getResColorId()));
        mPaint.setStyle(Paint.Style.FILL);
    }

    protected int getResColorId() {
        return R.color.color_white_trans_20;
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == VERTICAL_LIST) {
            drawVertical(c, parent, state);
        } else if (mOrientation == HORIZONTAL_LIST) {
            drawHorizontal(c, parent, state);
        }
    }

    protected void drawVertical(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getMeasuredWidth() - parent.getPaddingRight();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + layoutParams.bottomMargin;
            final int bottom = top + mItemSize;
            canvas.drawRect(left, top, right, bottom, mPaint);
        }
    }

    protected void drawHorizontal(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        final int top = parent.getPaddingTop();
        final int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
        final int childSize = parent.getChildCount();
        for (int i = 0; i < childSize; i++) {
            final View child = parent.getChildAt(i);
            RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getRight() + layoutParams.rightMargin;
            final int right = left + mItemSize;
            canvas.drawRect(left, top, right, bottom, mPaint);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == VERTICAL_LIST) {
            outRect.set(0, 0, 0, mItemSize);
        } else if (mOrientation == HORIZONTAL_LIST) {
            outRect.set(0, 0, mItemSize, 0);
        }
    }
}
