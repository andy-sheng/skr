package com.wali.live.common.gift.adapter.viewHolder;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.wali.live.common.gift.adapter.viewHolder.decoration.BaseItemDecoration;

/**
 * Created by zjn on 16-7-6.
 */
public class GiftDisplayDividerItemDecoration extends BaseItemDecoration {
    public static final int GRID_LIST = 3;

    public GiftDisplayDividerItemDecoration(int orientation) {
        super(orientation);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == GRID_LIST) {
            drawGrid(c, parent, state);
        } else {
            super.onDraw(c, parent, state);
        }
    }

    private void drawGrid(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        final int childSize = parent.getChildCount();
        if (childSize == 0) {
            return;
        }
        View child = parent.getChildAt(0);
        RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) child.getLayoutParams();
        int width = child.getRight() + layoutParams.rightMargin;
        int height = child.getBottom() + layoutParams.bottomMargin;

        // 画三条线
        final int left = parent.getPaddingLeft();
        final int right = parent.getMeasuredWidth() - parent.getPaddingRight();
        final int top = parent.getPaddingTop();
        final int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();
        // 画两条横线
        canvas.drawRect(left, top + height, right, top + height + mItemSize, mPaint);
        canvas.drawRect(left, bottom - mItemSize, right, bottom, mPaint);
        // 中间的4条竖线
        for (int i = 0; i < 4; i++) {
            int l = left + width * (i + 1) + i;
            canvas.drawRect(l, top, l + mItemSize, bottom, mPaint);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == GRID_LIST) {
            outRect.set(mItemSize, mItemSize, mItemSize, mItemSize);
        } else {
            super.getItemOffsets(outRect, view, parent, state);
        }
    }
}
