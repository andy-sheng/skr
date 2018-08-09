package com.wali.live.watchsdk.watch.view.watchgameview;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.thornbirds.component.IParams;
import com.wali.live.component.BaseSdkView;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.component.presenter.InputAreaPresenter;
import com.wali.live.watchsdk.component.presenter.WatchPlayerPresenter;
import com.wali.live.watchsdk.component.view.InputAreaView;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.WatchGameBottomEditPresenter;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.WatchGameTabPresenter;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.WatchGameZTopPresenter;

import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;

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

    // 播放器和浮层的父布局
    private RelativeLayout mVideShowLayout;

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
    public void startView() {
        super.startView();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
    }

    @Override
    public void setupView() {
        {
            mVideShowLayout = $(mParentView, R.id.video_show_layout);
            mVideShowLayout.post(new Runnable() {
                @Override
                public void run() {
                    resetVideoLayoutSize(false);
                }
            });
        }

        {
            // 播放器
            mVideoView = $(mParentView, R.id.video_view);
            mWatchPlayerPresenter = new WatchPlayerPresenter(mController, mController.getStreamerPresenter());
            registerHybridComponent(mWatchPlayerPresenter, mVideoView);
        }

        {
            mWatchZTopView = $(mParentView, R.id.watch_ztop_view);
            mWatchZTopPresnter = new WatchGameZTopPresenter(mController);
            registerComponent(mWatchZTopView, mWatchZTopPresnter);
        }
    }

    private void resetVideoLayoutSize(boolean isLandscape) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mVideShowLayout.getLayoutParams();
        if (isLandscape) {
            params.topMargin = 0;
            params.width = params.MATCH_PARENT;
            params.height = params.MATCH_PARENT;
        } else {
            params.topMargin = CommonUtils.getStatusBarHeight();
            params.width = params.MATCH_PARENT;
            // 这个方法被调用的时候getScreenWidth还没反应过来 拿到的可能依旧是横屏下的ScreenWidth 这里直接用getPhoneWidth()
            params.height = DisplayUtils.getPhoneWidth() * 9 / 16;
        }
        mVideShowLayout.setLayoutParams(params);
    }

    @Override
    public boolean onEvent(int event, IParams iParams) {
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                // 接收到切换为竖屏通知
                resetVideoLayoutSize(false);
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                // 接收到切换为横屏通知
                resetVideoLayoutSize(true);
                return true;
        }
        return false;
    }
}
