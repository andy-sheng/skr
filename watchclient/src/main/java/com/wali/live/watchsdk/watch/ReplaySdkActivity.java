package com.wali.live.watchsdk.watch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.ImageView;

import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.event.SdkEventClass;
import com.mi.live.data.user.User;
import com.mi.milink.sdk.base.CustomHandlerThread;
import com.wali.live.base.BaseEvent;
import com.wali.live.common.barrage.manager.BarrageMessageManager;
import com.wali.live.common.barrage.view.LiveCommentView;
import com.wali.live.common.endlive.UserEndLiveFragment;
import com.wali.live.common.flybarrage.view.FlyBarrageViewGroup;
import com.wali.live.common.gift.view.GiftAnimationView;
import com.wali.live.common.gift.view.GiftContinueViewGroup;
import com.wali.live.event.EventClass;
import com.wali.live.proto.HotSpotProto;
import com.wali.live.proto.LiveMessageProto;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.statistics.StatisticsWorker;
import com.wali.live.utils.ReplayBarrageMessageManager;
import com.mi.live.engine.player.widget.VideoPlayerTextureView;
import com.wali.live.video.widget.player.ReplaySeekBar;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;
import com.wali.live.watchsdk.personinfo.fragment.FloatPersonInfoFragment;
import com.wali.live.watchsdk.watch.model.RoomInfo;
import com.wali.live.watchsdk.watch.presenter.GameModePresenter;
import com.wali.live.watchsdk.watch.presenter.VideoPlayerPresenterEx;
import com.wali.live.watchsdk.watch.presenter.TouchPresenter;
import com.wali.live.watchsdk.watch.presenter.UserInfoPresenter;
import com.wali.live.watchsdk.watch.presenter.push.GiftPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomStatusPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomTextMsgPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomViewerPresenter;
import com.wali.live.watchsdk.watch.view.TouchDelegateView;
import com.wali.live.watchsdk.watchtop.view.WatchTopInfoSingleView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

/**
 * Created by yurui on 2016/12/13.
 */

public class ReplaySdkActivity extends BaseComponentSdkActivity implements FloatPersonInfoFragment.FloatPersonInfoClickListener {

    protected final static String TAG = "ReplaySdkActivity";

    private static final int MSG_ROOM_INFO = 108;               //主播拉取房间信息

    public static final int MSG_UPDATE_QOS = 200;               //金山云调试信息

    /**
     * data放在这里，不要乱放-
     */
    List<HotSpotProto.HotSpotInfo> spotInfoList = new ArrayList<>();
    long mReplayStartTime = 0;
    protected Timer mTimer;//每秒拉取弹幕的timer
    private CustomHandlerThread mHandlerThread = new CustomHandlerThread("ReplayFeedsVideoPlayer") {
        @Override
        protected void processMessage(Message message) {

        }
    };

    /**
     * presenter放在这里，不要乱放-
     */
    protected RoomTextMsgPresenter mRoomTextMsgPresenter;                                           //处理push的presenter
    protected GiftPresenter mGiftPresenter;
    protected RoomViewerPresenter mRoomViewerPresenter;
    protected RoomStatusPresenter mRoomStatusPresenter;
    protected UserInfoPresenter mUserInfoPresenter;
    protected TouchPresenter mTouchPresenter;
    private GameModePresenter mGameModePresenter;

    /**
     * view放在这里，不要乱放-
     */
    // 播放器
    protected VideoPlayerPresenterEx mReplayVideoPresenter;
    // 播放器容器
    protected VideoPlayerTextureView mVideoView;
    // 顶部信息栏，包括观众，星票，主播头像名字和房间人数
    protected WatchTopInfoSingleView mWatchTopInfoSingleView;
    protected LiveCommentView mLiveCommentView;         //弹幕区view
    protected GiftContinueViewGroup mGiftContinueViewGroup;
    // 礼物特效动画
    protected GiftAnimationView mGiftAnimationView;
    protected UserEndLiveFragment userEndLiveFragment;
    protected TouchDelegateView mTouchDelegateView;
    protected FlyBarrageViewGroup mFlyBarrageViewGroup;
    protected ImageView mRotateBtn;
    protected ImageView mClostBtn;
    protected ReplaySeekBar mReplaySeekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (isMIUIV6()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.replaysdk_layout);
        openOrientation();
        initData();

        initView();
        initPresenter();
        ReplayBarrageMessageManager.getInstance().init(mRoomChatMsgManager.toString());//回放弹幕管理

        //尝试发送关键数据给服务器,允许即使多次调用，成功后就不再发送。
        trySenddataWithServerOnce();
    }

    /**
     * 这里的方法会在初始时调用一次，会在账号或milink刚登录上在基类接受event也会调用，
     * 所以里面的方法依据要求要具备能被不断调用的能力
     */
    @Override
    public void trySenddataWithServerOnce() {
        mUserInfoPresenter.updateOwnerInfo();
        startPlayer();
        startGetBarrageTimer();
    }

    @Subscribe
    public void onEvent(SdkEventClass.OrientEvent event) {
        if (event.isLandscape()) {
            orientLandscape();
        } else {
            orientPortrait();
        }
    }

    protected void orientLandscape() {
        if (mLiveCommentView != null) {
            mLiveCommentView.orientComment(true);
        }
        if (mWatchTopInfoSingleView != null) {
            mWatchTopInfoSingleView.onScreenOrientationChanged(true);
        }
        if (mGiftContinueViewGroup != null) {
            mGiftContinueViewGroup.setOrient(true);
        }
    }

    protected void orientPortrait() {
        if (mLiveCommentView != null) {
            mLiveCommentView.orientComment(false);
        }
        if (mWatchTopInfoSingleView != null) {
            mWatchTopInfoSingleView.onScreenOrientationChanged(false);
        }
        if (mGiftContinueViewGroup != null) {
            mGiftContinueViewGroup.setOrient(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPlayer();
        if (null != mRoomChatMsgManager) {
            mRoomChatMsgManager.clearAllCache();
        }
        mHandlerThread.destroy();
        ReplayBarrageMessageManager.getInstance().destory();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    /**
     * 初始化 presenter
     */
    private void initPresenter() {
        mRoomTextMsgPresenter = new RoomTextMsgPresenter(mRoomChatMsgManager);
        addPushProcessor(mRoomTextMsgPresenter);

        mGiftPresenter = new GiftPresenter(mRoomChatMsgManager, false);
        addPushProcessor(mGiftPresenter);

        mRoomViewerPresenter = new RoomViewerPresenter(mRoomChatMsgManager);
        addPushProcessor(mRoomViewerPresenter);

        mRoomStatusPresenter = new RoomStatusPresenter(mRoomChatMsgManager);
        addPushProcessor(mRoomStatusPresenter);
        mUserInfoPresenter = new UserInfoPresenter(this,mMyRoomData);

        mReplayVideoPresenter = new VideoPlayerPresenterEx(this, mVideoView,mReplaySeekBar,mRotateBtn);
//        TODO 回放横竖屏 判断是否需要
//        mFeedsVideoPlayer.setTempForceOrientListener(this);
        mReplayVideoPresenter.setSeekBarHideDelay(4000);
        mReplayVideoPresenter.setSeekBarFullScreenBtnVisible(false);

        // 点击事件代理，左右滑动隐藏组件的逻辑
        mTouchPresenter = new TouchPresenter(mTouchDelegateView);
        addBindActivityLifeCycle(mTouchPresenter,true);
        TouchPresenter.AnimationParams animationParams = new TouchPresenter.AnimationParams();
        animationParams.views = new View[]{
                mWatchTopInfoSingleView,
                mLiveCommentView,
                mGiftContinueViewGroup,
                mGiftRoomEffectView,
                mGiftAnimationView
        };
        mTouchPresenter.setNeedHideViewsPortrait(animationParams);
        mTouchPresenter.setNeedHideViewsLandscape(animationParams);
        mTouchPresenter.setGestureAdapter(new TouchPresenter.GestureApater() {
            @Override
            public void onSingleTap() {
                mReplayVideoPresenter.onSeekBarContainerClick();
            }
        });

        if(mMyRoomData.getLiveType() == LiveManager.TYPE_LIVE_GAME){
            // 是游戏直播间
            mGameModePresenter = new GameModePresenter(this,mMyRoomData);
            mGameModePresenter.setGameBarrageViewStub((ViewStub) findViewById(R.id.game_barrage_viewstub));
            mGameModePresenter.setCommentView(mLiveCommentView);
            mGameModePresenter.setWatchTopView(mWatchTopInfoSingleView);
            mGameModePresenter.setCloseBtn(mClostBtn);
            mGameModePresenter.setRotateBtn(mRotateBtn);
            mGameModePresenter.setmTouchPresenter(mTouchPresenter);
            addBindActivityLifeCycle(mGameModePresenter, true);
        }
    }

    private void initData() {
        Intent data = getIntent();
        if (data == null) {
            return;
        }

        mRoomInfo = (RoomInfo) data.getParcelableExtra(EXTRA_ROOM_INFO);
        if (mRoomInfo == null) {
            MyLog.e(TAG, "mRoomInfo is null");
            finish();
            return;
        }
        // 填充 mMyRoomData
        mMyRoomData.setRoomId(mRoomInfo.getLiveId());
        mMyRoomData.setUid(mRoomInfo.getPlayerId());
        mMyRoomData.setVideoUrl(mRoomInfo.getVideoUrl());
        mMyRoomData.setLiveType(mRoomInfo.getLiveType());
        //TODO 记得去掉
//        mMyRoomData.setLiveType(LiveManager.TYPE_LIVE_GAME);
        mReplayStartTime = mRoomInfo.getStartTime();
    }

    private void initView() {
        mVideoView = $(R.id.video_view);

        // 顶部view
        mWatchTopInfoSingleView = $(R.id.watch_top_info_view);
        addBindActivityLifeCycle(mWatchTopInfoSingleView, true);
        mWatchTopInfoSingleView.setMyRoomDataSet(mMyRoomData);
        mWatchTopInfoSingleView.initViewUseData();

        // 初始化弹幕区
        mLiveCommentView = $(R.id.comment_rv);
        mLiveCommentView.setSoundEffectsEnabled(false);
        addBindActivityLifeCycle(mLiveCommentView, true);
        mLiveCommentView.setToken(mRoomChatMsgManager.toString());

        //礼物特效动画
        mGiftRoomEffectView = $(R.id.gift_room_effect_view);
        addBindActivityLifeCycle(mGiftRoomEffectView, true);

        //initMagicView
        mGiftAnimationView = $(R.id.gift_animation_player_view);
        addBindActivityLifeCycle(mGiftAnimationView, true);

        mGiftContinueViewGroup = $(R.id.gift_continue_vg);
        addBindActivityLifeCycle(mGiftContinueViewGroup, true);

        mTouchDelegateView = $(R.id.touch_delegate_view);

        mFlyBarrageViewGroup = $(R.id.fly_barrage_viewgroup);
        addBindActivityLifeCycle(mFlyBarrageViewGroup,true);

        mReplaySeekBar = $(R.id.replay_seekbar);
        mRotateBtn = $(R.id.rotate_btn);
        RxView.clicks(mRotateBtn)
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if(mLandscape){
                            forcePortrait();
                        }else{
                            forceLandscape();
                        }
                    }
                });
        //关闭按钮
        mClostBtn = $(R.id.close_btn);
        RxView.clicks(mClostBtn)
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        finish();
                    }
                });

    }

    /**
     * 显示个人资料的浮框
     */
    private void startShowFloatPersonInfo(long uid) {
        if (uid <= 0) {
            return;
        }
//        TODO 打开注释
//        clearTop();
        //打点
        StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP, StatisticsKey.KEY_USERINFO_CARD_OPEN, 1);
        FloatPersonInfoFragment.openFragment(this, uid, mMyRoomData.getUid(), mMyRoomData.getRoomId(), mMyRoomData.getVideoUrl(), this, mMyRoomData.getEnterRoomTime());
    }

    private void startPlayer() {
        if (mReplayVideoPresenter != null) {
            if (!mReplayVideoPresenter.isActivate()) {
                mReplayVideoPresenter.play(mRoomInfo.getVideoUrl());//, mVideoContainer, false, VideoPlayerTextureView.TRANS_MODE_CENTER_INSIDE, true, true);
                mReplayVideoPresenter.setTransMode(VideoPlayerTextureView.TRANS_MODE_CENTER_INSIDE);
            }
        }
    }

    private void stopPlayer() {
        if (mReplayVideoPresenter != null) {
            mReplayVideoPresenter.destroy();
        }
    }

    private void startGetBarrageTimer() {
        if (mTimer == null) {
            mTimer = new Timer();
            syncSystemMessage();
            mTimer.schedule(new TickTimerTask(), 0, 1000);
        }
    }

    //定时拉取消息 显示弹幕 push等
    private class TickTimerTask extends TimerTask {
        @Override
        public void run() {
            MyLog.v(TAG, "TickTimerTask mReplayStartTime=" + mReplayStartTime);
            if (mReplayVideoPresenter != null) {
                ReplayBarrageMessageManager.getInstance().getBarrageMessageByReplayTime(mMyRoomData.getRoomId(), mReplayStartTime + mReplayVideoPresenter.getCurrentPosition(), false);
            }
        }
    }

    private void syncSystemMessage() {
        if (!TextUtils.isEmpty(mMyRoomData.getRoomId())) {
            long myId = UserAccountManager.getInstance().getUuidAsLong();
            LiveMessageProto.SyncSysMsgRequest syncSysMsgRequest = LiveMessageProto.SyncSysMsgRequest.newBuilder()
                    .setCid(System.currentTimeMillis()).setFromUser(myId).setRoomId(mMyRoomData.getRoomId()).build();
            BarrageMessageManager.getInstance().sendSyncSystemMessage(syncSysMsgRequest);
        }
    }

    @Override
    public void onClickHomepage(User user) {

    }

    @Override
    public void onClickTopOne(User user) {

    }

    @Override
    public void onClickMainAvatar(User user) {

    }

    @Override
    public void onClickSixin(User user) {

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(BaseEvent.UserActionEvent event) {
        MyLog.e(TAG, "BaseEvent.UserActionEvent");
        // 该类型单独提出用指定的fastdoubleclick，防止fragment的崩溃
        if (event.type == BaseEvent.UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO) {
            startShowFloatPersonInfo((Long) event.obj1);
            return;
        }
        if (CommonUtils.isFastDoubleClick()) {
            return;
        }
    }

    //视频event 刷新播放按钮等操作
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.FeedsVideoEvent event) {
        if (mReplayVideoPresenter != null && event != null) {
            switch (event.mType) {
                case EventClass.FeedsVideoEvent.TYPE_START:
                    break;
                case EventClass.FeedsVideoEvent.TYPE_STOP:
                    break;
                case EventClass.FeedsVideoEvent.TYPE_COMPLETION:
                    break;
                case EventClass.FeedsVideoEvent.TYPE_ON_CLOSE_ENDLIVE:
                case EventClass.FeedsVideoEvent.TYPE_FULLSCREEN:
                    break;
                case EventClass.FeedsVideoEvent.TYPE_PLAYING:
                    break;
                case EventClass.FeedsVideoEvent.TYPE_SET_SEEK:
                    break;
                case EventClass.FeedsVideoEvent.TYPE_ON_FEEDS_PLAY_ACT_DESTORY:
                    break;
                case EventClass.FeedsVideoEvent.TYPE_ERROR:
                    break;
                case EventClass.FeedsVideoEvent.TYPE_ON_CLICK_ROTATE:{
                    if(mLandscape){
                        forcePortrait();
                    }else {
                        forceLandscape();
                    }
                }
                break;
            }
        }
    }

    public static void openActivity(@NonNull Activity activity, RoomInfo roomInfo) {
        Intent intent = new Intent(activity, ReplaySdkActivity.class);
        intent.putExtra(EXTRA_ROOM_INFO, roomInfo);
        activity.startActivity(intent);
    }

}
