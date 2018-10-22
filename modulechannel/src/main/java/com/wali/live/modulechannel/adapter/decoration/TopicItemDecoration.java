package com.wali.live.modulechannel.adapter.decoration;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.common.utils.U;
import com.wali.live.modulechannel.R;

/**
 * Created by lan on 16/11/3.
 *
 * @module 频道
 */
public class TopicItemDecoration extends BaseItemDecoration {
    public static final int GRID_LIST = 3;

    public TopicItemDecoration(int orientation) {
        super(orientation);
    }

    @Override
    protected int getResColorId() {
        return R.color.channel_color_e5e5e5;
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
        int childSize = parent.getChildCount();
        if (childSize == 0) {
            return;
        }
        int rowSize = 3;
        int lineSize = (childSize - 1) / rowSize + 1;

        // 获取单个子view的宽高
        View child = parent.getChildAt(0);
        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) child.getLayoutParams();
        int width = child.getRight() + mlp.rightMargin;
        int height = child.getBottom() + mlp.bottomMargin;

        // 获取父view的边界
        final int left = parent.getPaddingLeft();
        final int right = parent.getMeasuredWidth() - parent.getPaddingRight();
        final int top = parent.getPaddingTop();
        final int bottom = parent.getMeasuredHeight() - parent.getPaddingBottom();

        // 画rowSize - 1条竖线
        for(int j = 0; j < lineSize; j++){
            for (int i = 0; i < rowSize - 1; i++) {
                int l = left + width * (i + 1);
                int t = height * j + top + 40;
                int b = t + U.getDisplayUtils().dip2px(16.67f);
                canvas.drawRect(l, t, l + mItemSize, b, mPaint);
            }
        }
        // 画lineSize - 1条横线
        for (int i = 0; i < lineSize; i++) {
            if(i == 0) continue;
            int t = top + height * i;
            canvas.drawRect(left, t, right, t + mItemSize, mPaint);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        if (mOrientation == GRID_LIST) {
            // nothing to do
        } else {
            super.getItemOffsets(outRect, view, parent, state);
        }
    }
}
