package com.wali.live.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.base.activity.BaseActivity;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.live.module.common.R;

/**
 * Created by chengsimin on 16/7/7.
 */
public class PlaceHolderView extends View {
    private static final java.lang.String TAG = "PlaceHolderView";

    public PlaceHolderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PlaceHolderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PlaceHolderView(Context context) {
        super(context);
    }

    private boolean mIsLandscape = false;
    private int mHolderHeight = getResources().getDimensionPixelSize(R.dimen.live_bottom_btns_area_height);

    public void setHeightOnPortrait(int height) {
        mHolderHeight = height;
    }

    public void setOrient(boolean isLandscape) {
        mIsLandscape = isLandscape;
        MyLog.d(TAG, "isLandscape:" + isLandscape);
        onHideInputView();
    }


    //由于sogou输入法的手写模式，有时候会把键盘顶出屏幕,注意，是有时顶有时不顶，所以我们矫正回来
    public void onShowInputView(final int mSoftKeyboardHeight) {

        if (getContext() instanceof BaseActivity) {
            BaseActivity activity = (BaseActivity) getContext();
            View view = activity.getWindow().getDecorView();
            int[] location = new int[2];
            view.getLocationOnScreen(location);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
            params.height = mSoftKeyboardHeight + location[1];
            PlaceHolderView.this.setLayoutParams(params);

            postDelayed(new Runnable() {
                @Override
                public void run() {
                    BaseActivity activity = (BaseActivity) getContext();
                    if (activity != null && !activity.isFinishing()) {
                        View view = activity.getWindow().getDecorView();
                        int[] location = new int[2];
                        view.getLocationOnScreen(location);
                        if (location[1] < 0) {
                            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
                            params.height = mSoftKeyboardHeight + location[1];
                            PlaceHolderView.this.setLayoutParams(params);
                        }
                    }
                }
            }, 100);
        } else {
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
            params.height = mSoftKeyboardHeight;
            PlaceHolderView.this.setLayoutParams(params);
        }
    }

    public void onHideInputView() {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) getLayoutParams();
        if (!mIsLandscape) {
            params.height = mHolderHeight;
        } else {
            params.height = 0;
        }
        this.setLayoutParams(params);
    }

}
