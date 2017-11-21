package com.wali.live.watchsdk.fans.pay.decoration;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by lan on 17-4-10.
 */
public class PaySpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int mLeft;

    public PaySpacesItemDecoration(int left) {
        this.mLeft = left;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = mLeft;
        outRect.right = 0;
    }
}
