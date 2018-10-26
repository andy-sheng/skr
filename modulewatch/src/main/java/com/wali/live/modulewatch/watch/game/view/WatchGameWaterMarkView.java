package com.wali.live.modulewatch.watch.game.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.wali.live.modulewatch.watch.normal.WatchWaterMarkView;

import static android.widget.RelativeLayout.ALIGN_PARENT_RIGHT;
import static android.widget.RelativeLayout.ALIGN_PARENT_TOP;

/**
 * Created by zhujianning on 18-8-14.
 */

public class WatchGameWaterMarkView extends WatchWaterMarkView {
    private static final String TAG = "WatchGameWaterMarkView";
    private RelativeLayout.LayoutParams mIvHuYaLogoLayoutParams;
    private RelativeLayout.LayoutParams mMiLogoAreaLayoutParams;

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
        mIvHuYaLogoLayoutParams = (RelativeLayout.LayoutParams) mIvHuYaLogo.getLayoutParams();
        mMiLogoAreaLayoutParams = (RelativeLayout.LayoutParams) mMiLogoArea.getLayoutParams();
        mIvHuYaLogoLayoutParams.addRule(ALIGN_PARENT_TOP, 0);
        mIvHuYaLogoLayoutParams.addRule(ALIGN_PARENT_RIGHT, 0);
        mIvHuYaLogo.setLayoutParams(mIvHuYaLogoLayoutParams);

        mMiLogoAreaLayoutParams.addRule(ALIGN_PARENT_TOP, 0);
        mMiLogoAreaLayoutParams.addRule(ALIGN_PARENT_RIGHT, 0);
        mMiLogoArea.setLayoutParams(mMiLogoAreaLayoutParams);
        onOrientation(mIsLandscape);
    }

    @Override
    public void onOrientation(boolean isLandscape) {
        //防止父类父类调用方式发生改变,手动给他重写下
        super.onOrientation(isLandscape);
        MyLog.d(TAG, "onOrientation:" + mIsLandscape);
        if (isLandscape) {

            mIvHuYaLogoLayoutParams.addRule(CENTER_VERTICAL, 0);
            mIvHuYaLogoLayoutParams.addRule(ALIGN_PARENT_BOTTOM);
            mIvHuYaLogoLayoutParams.addRule(ALIGN_PARENT_LEFT);
            mIvHuYaLogo.setLayoutParams(mIvHuYaLogoLayoutParams);

            mMiLogoAreaLayoutParams.addRule(CENTER_VERTICAL, 0);
            mMiLogoAreaLayoutParams.addRule(ALIGN_PARENT_BOTTOM);
            mMiLogoAreaLayoutParams.addRule(ALIGN_PARENT_LEFT);
            mMiLogoArea.setLayoutParams(mMiLogoAreaLayoutParams);
        } else {
            mIvHuYaLogoLayoutParams.addRule(ALIGN_PARENT_BOTTOM, 0);
            mIvHuYaLogoLayoutParams.addRule(ALIGN_PARENT_LEFT, 0);
            mIvHuYaLogoLayoutParams.addRule(CENTER_VERTICAL);
            mIvHuYaLogo.setLayoutParams(mIvHuYaLogoLayoutParams);

            mMiLogoAreaLayoutParams.addRule(ALIGN_PARENT_BOTTOM, 0);
            mMiLogoAreaLayoutParams.addRule(ALIGN_PARENT_LEFT, 0);
            mMiLogoAreaLayoutParams.addRule(CENTER_VERTICAL);
            mMiLogoArea.setLayoutParams(mMiLogoAreaLayoutParams);
        }

    }
}
