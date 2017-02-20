package com.wali.live.common.barrage.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.ListView;

/**
 * Created by chengsimin on 16/6/23.
 */
public class MyListView extends RecyclerView {
    final static String TAG = "MyRecyclerView";

    public MyListView(Context context) {
        super(context);
    }

    public MyListView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public MyListView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    public void requestLayout() {
        super.requestLayout();
    }


    /**
     * 因为recycleview 23.2.0 之后就一个bug，在notifydatachange后会自动重绘滚动到某个位置，
     * 这个重载控制
     *
     * @param s
     */
    public void setAllowRequestLayout(boolean s) {
//        MyLog.d(TAG, "setAllowRequestLayout s=" + s);
        this.mSwitch = s;
    }

    private boolean mSwitch = true;
}
