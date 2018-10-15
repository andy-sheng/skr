package com.wali.live.common.smiley;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.base.utils.display.DisplayUtils;
import com.live.module.common.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @module smileypick 点点
 * <p/>
 * Created by MK on 15/10/20.
 */
public class SmileyPoint extends LinearLayout {
    private int mPageNum;
    private int mPageIndex;
    private List<View> views = new ArrayList<View>();
    public int mBgResId = 0;

    public SmileyPoint(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setPageNum(int pageNum, int pageIndex) {

        if (pageNum == 1) {
            pageNum = 0;
        }
        if (mPageNum > pageNum) {
            for (int i = mPageNum; i > pageNum; i--) {
                removeView(views.get(i - 1));
                views.remove(i - 1);
            }
        } else if (mPageNum < pageNum) {
            for (int i = mPageNum; i < pageNum; i++) {
                View view = new View(getContext());
                LayoutParams params = new LayoutParams(DisplayUtils.dip2px(5), DisplayUtils.dip2px(5));
                view.setLayoutParams(params);
                params.setMargins(DisplayUtils.dip2px(2.5f), 0, DisplayUtils.dip2px(2.5f), 0);
                view.setBackgroundDrawable(getResources().getDrawable(
                        mBgResId != 0 ? mBgResId : R.drawable.smiley_point_shape));
                addView(view);
                views.add(view);
            }
        }
        mPageNum = pageNum;
        if (mPageIndex < mPageNum) {
            views.get(mPageIndex).setSelected(false);
        }
        if (pageIndex < mPageNum) {
            views.get(pageIndex).setSelected(true);
        }
        mPageIndex = pageIndex;
    }
}
