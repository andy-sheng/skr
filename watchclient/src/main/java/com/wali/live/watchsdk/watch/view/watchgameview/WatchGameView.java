package com.wali.live.watchsdk.watch.view.watchgameview;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.thornbirds.component.IParams;
import com.wali.live.component.BaseSdkView;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.component.presenter.InputAreaPresenter;
import com.wali.live.watchsdk.component.presenter.WatchPlayerPresenter;
import com.wali.live.watchsdk.component.view.InputAreaView;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.WatchGameBottomEditPresenter;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.WatchGameTabPresenter;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.WatchGameZTopPresenter;

/**
 * Created by vera on 2018/8/7.
 * 游戏直播观众端View
 */

public class WatchGameView extends BaseSdkView<View, WatchComponentController> {

    // 播放器
    private TextureView mVideoView;
    private WatchPlayerPresenter mWatchPlayerPresenter;

    // 播放器上面的浮层操作View
    private WatchGameZTopView mWatchZTopView;
    private WatchGameZTopPresenter mWatchZTopPresnter;

    // 竖屏时播放器下方的tab页
    private WatchGameTabView mWatchTabView;
    private WatchGameTabPresenter mWatchTabPresenter;

    // 竖屏时底部的弹幕编辑和礼物发送按钮
    private WatchGameBottomEditView mBottomEditView;
    private WatchGameBottomEditPresenter mBottomEditPresenter;

    // 竖屏时真正的输入框
    private InputAreaView mInputArea;
    private InputAreaPresenter mInputPresenter;



    public WatchGameView(@NonNull Activity activity, @NonNull ViewGroup parentView, @NonNull WatchComponentController controller) {
        super(activity, parentView, controller);
    }

    @Override
    protected String getTAG() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void setupView() {

    }

    @Override
    public boolean onEvent(int i, IParams iParams) {
        return false;
    }
}
