package com.wali.live.modulewatch.watch.game.view;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;

import com.common.log.MyLog;
import com.thornbirds.component.IParams;
import com.wali.live.modulewatch.R;
import com.wali.live.modulewatch.base.view.BaseSdkView;
import com.wali.live.modulewatch.watch.game.presenter.GameNewBarrageViewPresenter;
import com.wali.live.modulewatch.watch.normal.WatchComponentController;

import static com.wali.live.modulewatch.base.component.BaseSdkController.MSG_NEW_GAME_WATCH_EXIST_CLICK;
import static com.wali.live.modulewatch.base.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.modulewatch.base.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.modulewatch.base.component.BaseSdkController.MSG_PLAYER_PAUSE;
import static com.wali.live.modulewatch.base.component.BaseSdkController.MSG_PLAYER_RECONNECT;
import static com.wali.live.modulewatch.base.component.BaseSdkController.MSG_PLAYER_SOUND_OFF;
import static com.wali.live.modulewatch.base.component.BaseSdkController.MSG_PLAYER_SOUND_ON;
import static com.wali.live.modulewatch.base.component.BaseSdkController.MSG_PLAYER_START;
import static com.wali.live.modulewatch.base.component.BaseSdkController.MSG_POP_INSUFFICIENT_TIPS;
import static com.wali.live.modulewatch.base.component.BaseSdkController.MSG_SHOW_FULLSCREEN_MORE_LIVE_VIEW;
import static com.wali.live.modulewatch.base.component.BaseSdkController.MSG_SHOW_SEND_ENVELOPE;

/**
 * Created by vera on 2018/8/7.
 * 游戏直播观众端View
 */

public class WatchGameView extends BaseSdkView<View, WatchComponentController> {
    private boolean mIsLandscape = false; // 当前是否是横屏

//    // 播放器
//    private TextureView mVideoView;
//    private WatchPlayerPresenter mWatchPlayerPresenter;
//
//    // 播放器上面的浮层操作View
//    private WatchGameZTopView mWatchZTopView;
//    private WatchGameZTopPresenter mWatchZTopPresnter;
//
//    // 播放器和浮层的父布局
//    private RelativeLayout mVideShowLayout;
//
//    private WatchGameFullScreenMoreView mFullScreenMoreLiveView;
//
//    // 竖屏时播放器下方的tab页
//    private WatchGameTabView mWatchTabView;
//    private WatchGameTabPresenter mWatchTabPresenter;
//
//    // 竖屏时底部的弹幕编辑和礼物发送按钮
//    private WatchGameBottomEditView mBottomEditView;
//    private WatchGameBottomEditPresenter mBottomEditPresenter;
//
//    // 竖屏时真正的输入框
//    private WatchGameInputAreaView mInputArea;
//    private InputAreaPresenter mInputPresenter;
//
//    private BaseEnterRoomSyncResPresenter mBaseEnterRoomSyncResPresenter;
//
//    private MyAlertDialog mBalanceInsufficientDialog;
    private GameBarrageView mGameBarrageView;
    private GameNewBarrageViewPresenter mGameBarragePresenter;
//
//    private WatchGameControllerView mWatchGameControllerView;

    Runnable mResetRunnable = new Runnable() {
        @Override
        public void run() {
            resetVideoLayoutSize(mIsLandscape);
        }
    };

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
        registerAction(MSG_NEW_GAME_WATCH_EXIST_CLICK);
        registerAction(MSG_POP_INSUFFICIENT_TIPS);
        registerAction(MSG_PLAYER_START);
        registerAction(MSG_PLAYER_PAUSE);
        registerAction(MSG_PLAYER_RECONNECT);
        registerAction(MSG_SHOW_SEND_ENVELOPE);
        registerAction(MSG_PLAYER_SOUND_OFF);
        registerAction(MSG_PLAYER_SOUND_ON);
        registerAction(MSG_SHOW_FULLSCREEN_MORE_LIVE_VIEW);
    }

    @Override
    public void stopView() {
        super.stopView();
//        if (mVideShowLayout != null) {
//            mVideShowLayout.removeCallbacks(mResetRunnable);
//        }
//        if (mFullScreenMoreLiveView != null) {
//            mFullScreenMoreLiveView.stopView();
//        }
    }

    public void setupView(boolean isLandscape) {
        mIsLandscape = isLandscape;
        setupView();
    }

    public void onResume() {
//        if (mWatchPlayerPresenter != null
//                && mWatchPlayerPresenter.isPause()) {
//            //如果暂停就尝试让它播放下
//            mController.postEvent(BaseSdkController.MSG_PLAYER_START);
//        }
    }

    @Override
    public void setupView() {
//        {
//            mVideShowLayout = $(mParentView, R.id.video_show_layout);
//            mVideShowLayout.post(mResetRunnable);
//        }
//
//        {
//            // 播放器
//            mVideoView = $(mParentView, R.id.video_view);
//            mWatchPlayerPresenter = new WatchPlayerPresenter(mController, mController.getStreamerPresenter());
//            registerHybridComponent(mWatchPlayerPresenter, mVideoView);
//        }
//
//        {
//            mWatchZTopView = $(mParentView, R.id.watch_ztop_view);
//            mWatchZTopPresnter = new WatchGameZTopPresenter(mController);
//            mWatchZTopView.setEnableFollow(mController.getRoomBaseDataModel().isEnableRelationChain());
//            registerComponent(mWatchZTopView, mWatchZTopPresnter);
//        }
//
//        {
//            mBottomEditView = (WatchGameBottomEditView) mParentView.findViewById(R.id.watch_edit_view);
//            mBottomEditPresenter = new WatchGameBottomEditPresenter(mController);
//            registerComponent(mBottomEditView, mBottomEditPresenter);
//        }
//
//        {
//            mInputArea = (WatchGameInputAreaView) mParentView.findViewById(R.id.input_area_view);
//            mInputPresenter = new InputAreaPresenter(
//                    mController, mController.getRoomBaseDataModel(), mController.getLiveRoomChatMsgManager(), true);
//            registerComponent(mInputArea, mInputPresenter);
//        }
//
//        {
//            mBaseEnterRoomSyncResPresenter = new BaseEnterRoomSyncResPresenter(mController, mController.getRoomBaseDataModel(), false);
//            registerComponent(mBaseEnterRoomSyncResPresenter);
//        }
//
//        {
//            mWatchTabView = (WatchGameTabView) mParentView.findViewById(R.id.watch_game_tab_view);
//            mWatchTabView.setComponentControler(mController);
//            mWatchTabView.init(mParentView.getContext());
//
//            // presenter
//            mWatchTabPresenter = new WatchGameTabPresenter(mController);
//            registerComponent(mWatchTabView, mWatchTabPresenter);
//        }

        {
            mGameBarrageView = (GameBarrageView) mParentView.findViewById(R.id.game_barrage_view);
            mGameBarragePresenter = new GameNewBarrageViewPresenter(mController);
            registerComponent(mGameBarrageView, mGameBarragePresenter);
        }

//        {
//            WatchGameWaterMarkView watchGameWaterMarkView = (WatchGameWaterMarkView) mParentView.findViewById(R.id.watch_mark_view);
//            watchGameWaterMarkView.setRoomData(mController.getRoomBaseDataModel());
//        }
//
//        // 抢红包
//        {
//            RelativeLayout relativeLayout = (RelativeLayout) mParentView.findViewById(R.id.envelope_view);
//            EnvelopePresenter presenter = new EnvelopePresenter(mController, mController.getRoomBaseDataModel());
//            registerHybridComponent(presenter, relativeLayout);
//        }
//
//        // 控制音量和亮度
//        {
//            mWatchGameControllerView = (WatchGameControllerView) mParentView.findViewById(R.id.controller_view);
//        }
    }

    private void resetVideoLayoutSize(boolean isLandscape) {
//        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mVideShowLayout.getLayoutParams();
//        if (isLandscape) {
//            params.topMargin = 0;
//            params.width = params.MATCH_PARENT;
//            params.height = params.MATCH_PARENT;
//        } else {
//            params.topMargin = U.getStatusBarUtil().getStatusBarHeight(this.mActivity);
//            params.width = params.MATCH_PARENT;
//            // 这个方法被调用的时候getScreenWidth还没反应过来 拿到的可能依旧是横屏下的ScreenWidth 这里直接用getPhoneWidth()
//            params.height = U.getDisplayUtils().getPhoneWidth() * 9 / 16;
//        }
//        mVideShowLayout.setLayoutParams(params);
    }

    /**
     * 在横屏下第一次点击更多直播时　
     * 接收到MSG_SHOW_FULLSCREEN_MORE_LIVE_VIEW将View add到布局里
     * 之后弹出更多直播都由该View内部自己处理
     */
    private void showMoreLiveViewIfNeed() {
        if (!mIsLandscape) {
            return;
        }
        mParentView.post(new Runnable() {
            // 不要去掉这个postRunable的使用
            // 目的是要fix一个postEvent的同时新new出来的对象同时注册该event引起的Concurrent bug
            @Override
            public void run() {
//                if (mFullScreenMoreLiveView == null) {
//                    mFullScreenMoreLiveView = new WatchGameFullScreenMoreView(mParentView.getContext(), mController);
//                    mFullScreenMoreLiveView.addSelfToWatchLayoutAndShow(mParentView );
//                }
            }
        });
    }

    /**
     * 接收横竖屏切换通知
     *
     * @param isLandscape
     */
    private void onReOrient(boolean isLandscape) {
        MyLog.d(TAG, "change to" + (isLandscape ? "landscape" : "portrait"));
        if (mIsLandscape != isLandscape) {
            // 横竖屏相互切换
            mIsLandscape = isLandscape;
            resetVideoLayoutSize(mIsLandscape);
        } else {
            // 横屏切换到反向横屏　或者竖屏切换到反向竖屏
        }
    }

    private void popInsufficientTips() {
//        if (mBalanceInsufficientDialog == null) {
//            mBalanceInsufficientDialog = new MyAlertDialog.Builder(mActivity).create();
//            mBalanceInsufficientDialog.setTitle(R.string.account_withdraw_pay_user_account_not_enough);
//            mBalanceInsufficientDialog.setMessage(GlobalData.app().getResources().getString(R.string.account_withdraw_pay_user_account_not_enough_tip));
//            mBalanceInsufficientDialog.setButton(AlertDialog.BUTTON_POSITIVE, GlobalData.app().getResources().getString(R.string.recharge), new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    EventBus.getDefault().post(new GiftEventClass.GiftMallEvent(GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_GO_RECHARGE));
//                    dialog.dismiss();
//                }
//            });
//            mBalanceInsufficientDialog.setButton(AlertDialog.BUTTON_NEGATIVE, GlobalData.app().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//                    dialog.dismiss();
//                }
//            });
//        }
//
//        mBalanceInsufficientDialog.setCancelable(false);
//        mBalanceInsufficientDialog.show();
    }

    @Override
    public boolean onEvent(int event, IParams iParams) {
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                // 接收到切换为竖屏通知
                onReOrient(false);
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                // 接收到切换为横屏通知
                onReOrient(true);
                return true;
            case MSG_NEW_GAME_WATCH_EXIST_CLICK:
                mActivity.finish();
                return true;
            case MSG_POP_INSUFFICIENT_TIPS:
                popInsufficientTips();
                break;
            case MSG_PLAYER_START:
//                if (mWatchPlayerPresenter != null) {
//                    mWatchPlayerPresenter.resumePlay();
//                }
                break;
            case MSG_PLAYER_PAUSE:
//                if (mWatchPlayerPresenter != null) {
//                    mWatchPlayerPresenter.pausePlay();
//                }
                break;
            case MSG_PLAYER_SOUND_OFF:
//                if (mWatchPlayerPresenter != null) {
//                    mWatchPlayerPresenter.mutePlay(true);
//                }
                break;
            case MSG_PLAYER_SOUND_ON:
//                if (mWatchPlayerPresenter != null) {
//                    mWatchPlayerPresenter.mutePlay(false);
//                }
                break;
            case MSG_PLAYER_RECONNECT:
//                if (mWatchPlayerPresenter != null) {
//                    mWatchPlayerPresenter.startReconnect();
//                }
                break;
            case MSG_SHOW_SEND_ENVELOPE:
//                SendEnvelopeFragment.openFragment((BaseActivity) mActivity, mController.getRoomBaseDataModel());
                return true;
            case MSG_SHOW_FULLSCREEN_MORE_LIVE_VIEW:
                showMoreLiveViewIfNeed();
                return true;
        }
        return false;
    }
}
