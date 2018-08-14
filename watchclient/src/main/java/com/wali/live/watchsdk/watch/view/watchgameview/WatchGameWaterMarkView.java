package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.util.AttributeSet;

import com.wali.live.watchsdk.component.view.WatchWaterMarkView;

/**
 * Created by zhujianning on 18-8-14.
 */

public class WatchGameWaterMarkView extends WatchWaterMarkView {

    public WatchGameWaterMarkView(Context context) {
        super(context);
    }

    public WatchGameWaterMarkView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WatchGameWaterMarkView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onOrientation(boolean isLandscape) {
        //防止父类父类调用方式发生改变,手动给他重写下
    }
}
