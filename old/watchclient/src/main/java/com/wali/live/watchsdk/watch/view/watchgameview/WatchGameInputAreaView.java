package com.wali.live.watchsdk.watch.view.watchgameview;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.base.keyboard.KeyboardUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.view.InputAreaView;

/**
 * Created by zhujianning on 18-8-14.
 * 新版游戏直播间竖屏模式下,其他控件不需要根据WatchGameInputAreaView做相应的位置变化
 * 如果需要用inputAreaView
 */

public class WatchGameInputAreaView extends InputAreaView {
    private View mTouchArea;

    public WatchGameInputAreaView(Context context) {
        this(context, null);
    }

    public WatchGameInputAreaView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WatchGameInputAreaView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.watch_game_input_area_view;
    }

    @Override
    protected void bindView() {
        super.bindView();
        mTouchArea = findViewById(R.id.split);
        mTouchArea.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mIsKeyboardShowed) {
                    KeyboardUtils.hideKeyboard((Activity) getContext());
                }
            }
        });

        if(mBarrageSelectBtn != null) {
            mBarrageSelectBtn.setVisibility(GONE);
        }

        if(mBarrageSwitchBtn != null) {
            mBarrageSwitchBtn.setVisibility(GONE);
        }
    }

    @Override
    protected void barrageSelectViewEnable(boolean enable) {

    }
}
