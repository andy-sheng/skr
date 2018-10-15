package com.wali.live.watchsdk.watch.view.watchgameview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.wali.live.watchsdk.component.view.WatchWaterMarkView;

/**
 * Created by zhujianning on 18-8-14.
 */

public class WatchGameWaterMarkView extends WatchWaterMarkView {
    private static final String TAG = "WatchGameWaterMarkView";
    private LayoutParams mIvHuYaLogoLayoutParams;
    private LayoutParams mMiLogoAreaLayoutParams;

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
        mIvHuYaLogoLayoutParams = (LayoutParams) mIvHuYaLogo.getLayoutParams();
        mMiLogoAreaLayoutParams = (LayoutParams) mMiLogoArea.getLayoutParams();
        mIvHuYaLogoLayoutParams.removeRule(ALIGN_PARENT_TOP);
        mIvHuYaLogoLayoutParams.removeRule(ALIGN_PARENT_RIGHT);
        mIvHuYaLogo.setLayoutParams(mIvHuYaLogoLayoutParams);

        mMiLogoAreaLayoutParams.removeRule(ALIGN_PARENT_TOP);
        mMiLogoAreaLayoutParams.removeRule(ALIGN_PARENT_RIGHT);
        mMiLogoArea.setLayoutParams(mMiLogoAreaLayoutParams);
        onOrientation(mIsLandscape);
    }

    @Override
    public void onOrientation(boolean isLandscape) {
        //防止父类父类调用方式发生改变,手动给他重写下
        super.onOrientation(isLandscape);
        MyLog.d(TAG, "onOrientation:" + mIsLandscape);
        if(isLandscape) {
            
            mIvHuYaLogoLayoutParams.removeRule(CENTER_VERTICAL);
            mIvHuYaLogoLayoutParams.addRule(ALIGN_PARENT_BOTTOM);
            mIvHuYaLogoLayoutParams.addRule(ALIGN_PARENT_LEFT);
            mIvHuYaLogo.setLayoutParams(mIvHuYaLogoLayoutParams);

            mMiLogoAreaLayoutParams.removeRule(CENTER_VERTICAL);
            mMiLogoAreaLayoutParams.addRule(ALIGN_PARENT_BOTTOM);
            mMiLogoAreaLayoutParams.addRule(ALIGN_PARENT_LEFT);
            mMiLogoArea.setLayoutParams(mMiLogoAreaLayoutParams);
        } else {
            mIvHuYaLogoLayoutParams.removeRule(ALIGN_PARENT_BOTTOM);
            mIvHuYaLogoLayoutParams.removeRule(ALIGN_PARENT_LEFT);
            mIvHuYaLogoLayoutParams.addRule(CENTER_VERTICAL);
            mIvHuYaLogo.setLayoutParams(mIvHuYaLogoLayoutParams);

            mMiLogoAreaLayoutParams.removeRule(ALIGN_PARENT_BOTTOM);
            mMiLogoAreaLayoutParams.removeRule(ALIGN_PARENT_LEFT);
            mMiLogoAreaLayoutParams.addRule(CENTER_VERTICAL);
            mMiLogoArea.setLayoutParams(mMiLogoAreaLayoutParams);
        }

    }
}
