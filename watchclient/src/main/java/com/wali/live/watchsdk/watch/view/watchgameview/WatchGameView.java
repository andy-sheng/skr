package com.wali.live.watchsdk.watch.view.watchgameview;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.base.activity.BaseActivity;
import com.base.log.MyLog;
import com.base.dialog.MyAlertDialog;
import com.base.global.GlobalData;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.mi.live.data.event.GiftEventClass;
import com.thornbirds.component.IParams;
import com.wali.live.component.BaseSdkView;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.component.presenter.EnvelopePresenter;
import com.wali.live.watchsdk.component.presenter.InputAreaPresenter;
import com.wali.live.watchsdk.component.presenter.WatchPlayerPresenter;
import com.wali.live.watchsdk.component.view.GameBarrageView;
import com.wali.live.watchsdk.component.view.InputAreaView;
import com.wali.live.watchsdk.envelope.SendEnvelopeFragment;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.BaseEnterRoomSyncResPresenter;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.GameNewBarrageViewPresenter;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.WatchGameBottomEditPresenter;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.WatchGameTabPresenter;
import com.wali.live.watchsdk.watch.presenter.watchgamepresenter.WatchGameZTopPresenter;

import org.greenrobot.eventbus.EventBus;

import static com.wali.live.component.BaseSdkController.MSG_NEW_GAME_WATCH_EXIST_CLICK;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_PAUSE;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_RECONNECT;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_START;
import static com.wali.live.component.BaseSdkController.MSG_POP_INSUFFICIENT_TIPS;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_SEND_ENVELOPE;

/**
 * Created by vera on 2018/8/7.
 * 游戏直播观众端View
 */

public class WatchGameView extends BaseSdkView<View, WatchComponentController> {
    private boolean mIsLandscape = false; // 当前是否是横屏

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
    private WatchGameInputAreaView mInputArea;
    private InputAreaPresenter mInputPresenter;

    private BaseEnterRoomSyncResPresenter mBaseEnterRoomSyncResPresenter;

    private MyAlertDialog mBalanceInsufficientDialog;
    private GameBarrageView mGameBarrageView;
    private GameNewBarrageViewPresenter mGameBarragePresenter;

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
    }

    @Override
    public void stopView() {
        super.stopView();
    }

    public void setupView(boolean isLandscape) {
        mIsLandscape = isLandscape;
        setupView();
    }

    @Override
    public void setupView() {
        {
            mVideShowLayout = $(mParentView, R.id.video_show_layout);
            mVideShowLayout.post(new Runnable() {
                @Override
                public void run() {
                    resetVideoLayoutSize(mIsLandscape);
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
            mWatchZTopView.setEnableFollow(mController.getRoomBaseDataModel().isEnableRelationChain());
            registerComponent(mWatchZTopView, mWatchZTopPresnter);
        }

        {
            mBottomEditView = (WatchGameBottomEditView) mParentView.findViewById(R.id.watch_edit_view);
            mBottomEditPresenter = new WatchGameBottomEditPresenter(mController);
            registerComponent(mBottomEditView, mBottomEditPresenter);
        }

        {
            mInputArea =(WatchGameInputAreaView) mParentView.findViewById(R.id.input_area_view);
            mInputPresenter = new InputAreaPresenter(
                    mController, mController.getRoomBaseDataModel(), mController.getLiveRoomChatMsgManager(), true);
            registerComponent(mInputArea, mInputPresenter);
        }

        {
            mBaseEnterRoomSyncResPresenter = new BaseEnterRoomSyncResPresenter(mController, mController.getRoomBaseDataModel(), false);
            registerComponent(mBaseEnterRoomSyncResPresenter);
        }

        {
            mWatchTabView = (WatchGameTabView) mParentView.findViewById(R.id.watch_game_tab_view);
            mWatchTabView.setComponentControler(mController);
            mWatchTabView.init(mParentView.getContext());

            // presenter
            mWatchTabPresenter = new WatchGameTabPresenter(mController);
            registerComponent(mWatchTabView, mWatchTabPresenter);
        }

        {
            mGameBarrageView = (GameBarrageView) mParentView.findViewById(R.id.game_barrage_view);
            mGameBarragePresenter = new GameNewBarrageViewPresenter(mController);
            registerComponent(mGameBarrageView, mGameBarragePresenter);
        }

        {
            WatchGameWaterMarkView watchGameWaterMarkView = (WatchGameWaterMarkView) mParentView.findViewById(R.id.watch_mark_view);
            watchGameWaterMarkView.setRoomData(mController.getRoomBaseDataModel());
        }

        // 抢红包
        {
            RelativeLayout relativeLayout = (RelativeLayout) mParentView.findViewById(R.id.envelope_view);
            EnvelopePresenter presenter = new EnvelopePresenter(mController, mController.getRoomBaseDataModel());
            registerHybridComponent(presenter, relativeLayout);
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

    /**
     * 接收横竖屏切换通知
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
        if (mBalanceInsufficientDialog == null) {
            mBalanceInsufficientDialog = new MyAlertDialog.Builder(mActivity).create();
            mBalanceInsufficientDialog.setTitle(R.string.account_withdraw_pay_user_account_not_enough);
            mBalanceInsufficientDialog.setMessage(GlobalData.app().getResources().getString(R.string.account_withdraw_pay_user_account_not_enough_tip));
            mBalanceInsufficientDialog.setButton(AlertDialog.BUTTON_POSITIVE, GlobalData.app().getResources().getString(R.string.recharge), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    EventBus.getDefault().post(new GiftEventClass.GiftMallEvent(GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_GO_RECHARGE));
                    dialog.dismiss();
                }
            });
            mBalanceInsufficientDialog.setButton(AlertDialog.BUTTON_NEGATIVE, GlobalData.app().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        }

        mBalanceInsufficientDialog.setCancelable(false);
        mBalanceInsufficientDialog.show();
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
                mWatchPlayerPresenter.resumePlay();
                break;
            case MSG_PLAYER_PAUSE:
                mWatchPlayerPresenter.pausePlay();
                break;
            case MSG_PLAYER_RECONNECT:
                mWatchPlayerPresenter.startReconnect();
                break;
            case MSG_SHOW_SEND_ENVELOPE:
                SendEnvelopeFragment.openFragment((BaseActivity) mActivity, mController.getRoomBaseDataModel());
                return true;
        }
        return false;
    }
}
