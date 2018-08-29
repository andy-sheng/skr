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
    protected void init(Context context, AttributeSet attrs, int defStyleAttr) {
        super.init(context, attrs, defStyleAttr);

        LayoutParams layoutParams = (LayoutParams) mIvHuYaLogo.getLayoutParams();
        layoutParams.removeRule(ALIGN_PARENT_TOP);
        layoutParams.removeRule(ALIGN_PARENT_RIGHT);
        layoutParams.addRule(ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(ALIGN_PARENT_LEFT);
        mIvHuYaLogo.setLayoutParams(layoutParams);

        layoutParams = (LayoutParams) mMiLogoArea.getLayoutParams();
        layoutParams.removeRule(ALIGN_PARENT_TOP);
        layoutParams.removeRule(ALIGN_PARENT_RIGHT);
        layoutParams.addRule(ALIGN_PARENT_BOTTOM);
        layoutParams.addRule(ALIGN_PARENT_LEFT);
        mMiLogoArea.setLayoutParams(layoutParams);
    }

    @Override
    public void onOrientation(boolean isLandscape) {
        //防止父类父类调用方式发生改变,手动给他重写下
    }
}
