package com.wali.live.watchsdk.channel.holder;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.base.log.MyLog;

/**
 * Created by anping on 17/3/2.
 */

public class LiveLinearLayoutManager extends LinearLayoutManager {

    public LiveLinearLayoutManager(Context context) {
        super(context);
    }

    public LiveLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public LiveLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (IndexOutOfBoundsException e) {
            MyLog.e("Error", "IndexOutOfBoundsException in RecyclerView happens");
        }
    }

    @Override
    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
        int result = 0;
        try {
            result = super.scrollVerticallyBy(dy, recycler, state);
        } catch (Exception e) {
            MyLog.e("error," + e);
        }
        return result;
    }
}
