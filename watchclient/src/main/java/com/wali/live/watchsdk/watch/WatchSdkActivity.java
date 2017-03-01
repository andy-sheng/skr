package com.wali.live.watchsdk.watch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.ImageView;

import com.base.event.SdkEventClass;
import com.base.fragment.FragmentListener;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.image.fresco.BaseImageView;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.rx.RxRetryAssist;
import com.base.version.VersionCheckTask;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.cache.RoomInfoGlobalCache;
import com.mi.live.data.event.GiftEventClass;
import com.mi.live.data.gift.model.GiftInfoForEnterRoom;
import com.mi.live.data.gift.model.GiftRecvModel;
import com.mi.live.data.location.Location;
import com.mi.live.data.manager.LiveRoomCharactorManager;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.query.model.EnterRoomInfo;
import com.mi.live.data.repository.GiftRepository;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.data.user.User;
import com.mi.live.engine.player.widget.VideoPlayerTextureView;
import com.mi.milink.sdk.base.CustomHandlerThread;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.base.BaseEvent;
import com.wali.live.common.barrage.view.LiveCommentView;
import com.wali.live.common.endlive.UserEndLiveFragment;
import com.wali.live.common.flybarrage.view.FlyBarrageViewGroup;
import com.wali.live.common.gift.presenter.GiftMallPresenter;
import com.wali.live.common.gift.view.GiftAnimationView;
import com.wali.live.common.gift.view.GiftContinueViewGroup;
import com.wali.live.common.pay.fragment.RechargeFragment;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.event.EventClass;
import com.wali.live.manager.WatchRoomCharactorManager;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.statistics.StatisticsWorker;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.component.WatchSdkView;
import com.wali.live.watchsdk.personinfo.fragment.FloatPersonInfoFragment;
import com.wali.live.watchsdk.personinfo.presenter.ForbidManagePresenter;
import com.wali.live.watchsdk.task.IActionCallBack;
import com.wali.live.watchsdk.task.LiveTask;
import com.wali.live.watchsdk.watch.event.LiveEndEvent;
import com.wali.live.watchsdk.watch.model.RoomInfo;
import com.wali.live.watchsdk.watch.presenter.GameModePresenter;
import com.wali.live.watchsdk.watch.presenter.IWatchView;
import com.wali.live.watchsdk.watch.presenter.LiveTaskPresenter;
import com.wali.live.watchsdk.watch.presenter.SdkEndLivePresenter;
import com.wali.live.watchsdk.watch.presenter.TouchPresenter;
import com.wali.live.watchsdk.watch.presenter.UserInfoPresenter;
import com.wali.live.watchsdk.watch.presenter.VideoPlayerPresenterEx;
import com.wali.live.watchsdk.watch.presenter.push.GiftPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomManagerPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomStatusPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomTextMsgPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomViewerPresenter;
import com.wali.live.watchsdk.watch.view.TouchDelegateView;
import com.wali.live.watchsdk.watchtop.view.WatchTopInfoSingleView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observer;
import rx.functions.Action1;


/**
 * Created by lan on 16/11/25.
 */
public class WatchSdkActivity extends BaseComponentSdkActivity implements FloatPersonInfoFragment.FloatPersonInfoClickListener
        , ForbidManagePresenter.IForbidManageProvider, IActionCallBack {

    @Override
    public boolean isKeyboardResize() {
        return false;
    }

    /**
     * view放在这里
     */
    // 播放器
    protected VideoPlayerTextureView mVideoView;
    // 播放器容器
    protected WatchTopInfoSingleView mWatchTopInfoSingleView;
    protected LiveCommentView mLiveCommentView; //弹幕区view
    protected ImageView mCloseBtn;// 关闭按钮
    protected ImageView mRotateBtn;// 关闭

    protected WatchComponentController mComponentController;
    protected WatchSdkView mSdkView;

    // 高斯蒙层
    private BaseImageView mBlurIv;
    protected GiftContinueViewGroup mGiftContinueViewGroup;
    // 礼物特效动画
    protected GiftAnimationView mGiftAnimationView;
    //
    protected TouchDelegateView mTouchDelegateView;
    protected FlyBarrageViewGroup mFlyBarrageViewGroup;
    /**
     * presenter放在这里
     */
    VideoPlayerPresenterEx mVideoPlayerPresenterEx;
    protected RoomTextMsgPresenter mRoomTextMsgPresenter;
    protected GiftPresenter mGiftPresenter;
    private RoomManagerPresenter mRoomManagerPresenter;
    private LiveTaskPresenter mLiveTaskPresenter;
    private GiftMallPresenter mGiftMallPresenter;
    private RoomViewerPresenter mRoomViewerPresenter;
    private RoomStatusPresenter mRoomStatusPresenter;
    private ForbidManagePresenter mForbidManagePresenter;
    protected UserInfoPresenter mUserInfoPresenter;
    private TouchPresenter mTouchPresenter;
    private GameModePresenter mGameModePresenter;

    protected CustomHandlerThread mHandlerThread = new CustomHandlerThread("WatchActivity") {
        @Override
        protected void processMessage(Message message) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (isMIUIV6()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.watchsdk_layout);
        openOrientation();
        initData();

        initView();
        initPresenter();

        //尝试发送关键数据给服务器,允许即使多次调用，成功后就不再发送。
        trySenddataWithServerOnce();
    }

    /**
     * 这里的方法会在初始时调用一次，会在账号或milink刚登录上在基类接受event也会调用，
     * 所以里面的方法依据要求要具备能被不断调用的能力
     */
    public void trySenddataWithServerOnce() {
        mLiveTaskPresenter.enterLive();
        mUserInfoPresenter.updateOwnerInfo();
        startPlayer();
    }

    private void initData() {
        Intent data = getIntent();
        if (data == null) {
            return;
        }

        mRoomInfo = data.getParcelableExtra(EXTRA_ROOM_INFO);
        if (mRoomInfo == null) {
            MyLog.e(TAG, "mRoomInfo is null");
            finish();
            return;
        }
        // 填充 MyRoomData
        mMyRoomData.setRoomId(mRoomInfo.getLiveId());
        mMyRoomData.setUid(mRoomInfo.getPlayerId());
        mMyRoomData.setVideoUrl(mRoomInfo.getVideoUrl());
        mMyRoomData.setLiveType(mRoomInfo.getLiveType());
    }


    private void initView() {
        // 顶部view
        mWatchTopInfoSingleView = $(R.id.watch_top_info_view);
        addBindActivityLifeCycle(mWatchTopInfoSingleView, true);
        mWatchTopInfoSingleView.setMyRoomDataSet(mMyRoomData);
        mWatchTopInfoSingleView.initViewUseData();

        // 封面模糊图
        mBlurIv = $(R.id.blur_iv);
        if (!TextUtils.isEmpty(mRoomInfo.getCoverUrl())) {
            AvatarUtils.loadAvatarByUrl(mBlurIv, mRoomInfo.getCoverUrl(), false, true, 0);
        } else {
            AvatarUtils.loadAvatarByUidTs(mBlurIv, mRoomInfo.getPlayerId(), mRoomInfo.getAvatar(),
                    AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE, false, true);
        }

        // 初始化弹幕区
        mLiveCommentView = $(R.id.comment_rv);
        mLiveCommentView.setSoundEffectsEnabled(false);
        addBindActivityLifeCycle(mLiveCommentView, true);
        mLiveCommentView.setToken(mRoomChatMsgManager.toString());

        mVideoView = $(R.id.video_view);

        mGiftContinueViewGroup = $(R.id.gift_continue_vg);
        addBindActivityLifeCycle(mGiftContinueViewGroup, true);

        initGiftRoomEffectView();

        mGiftAnimationView = $(R.id.gift_animation_player_view);
        addBindActivityLifeCycle(mGiftAnimationView, true);

        //关闭按钮
        mCloseBtn = $(R.id.close_btn);
        RxView.clicks(mCloseBtn)
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        finish();
                    }
                });
        mCloseBtn.setVisibility(View.VISIBLE);

        mRotateBtn = $(R.id.rotate_btn);
        RxView.clicks(mRotateBtn)
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        if (mLandscape) {
                            tempForcePortrait();
                        } else {
                            tempForceLandscape();
                        }
                    }
                });

        mComponentController = new WatchComponentController();
        mSdkView = new WatchSdkView(this, mComponentController, mMyRoomData);
        mSdkView.setupSdkView(mMyRoomData.getLiveType() == LiveManager.TYPE_LIVE_GAME);

        mTouchDelegateView = $(R.id.touch_delegate_view);

        mFlyBarrageViewGroup = $(R.id.fly_barrage_viewgroup);
        addBindActivityLifeCycle(mFlyBarrageViewGroup, true);

        ComponentPresenter.IAction action = new ComponentPresenter.IAction() {
            @Override
            public boolean onAction(int source, @Nullable ComponentPresenter.Params params) {
                switch (source) {
                    case WatchComponentController.MSG_INPUT_VIEW_SHOWED:
                        if (mGiftContinueViewGroup != null) {
                            mGiftContinueViewGroup.onShowInputView();
                        }
                        if (mLiveCommentView != null && isDisplayLandscape()) {
                            mLiveCommentView.setVisibility(View.GONE);
                        }
                        return true;
                    case WatchComponentController.MSG_INPUT_VIEW_HIDDEN:
                        if (mGiftContinueViewGroup != null) {
                            mGiftContinueViewGroup.onHideInputView();
                        }
                        if (mLiveCommentView != null) {
                            mLiveCommentView.setVisibility(View.VISIBLE);
                        }
                        return true;
                    default:
                        break;
                }
                return false;
            }
        };
        mComponentController.registerAction(WatchComponentController.MSG_INPUT_VIEW_SHOWED, action);
        mComponentController.registerAction(WatchComponentController.MSG_INPUT_VIEW_HIDDEN, action);
    }

    private void startPlayer() {
        if (mVideoPlayerPresenterEx != null) {
            if (!mVideoPlayerPresenterEx.isActivate()) {
                mVideoPlayerPresenterEx.play(mRoomInfo.getVideoUrl());//, mVideoContainer, false, VideoPlayerTextureView.TRANS_MODE_CENTER_INSIDE, true, true);
                mVideoPlayerPresenterEx.setTransMode(VideoPlayerTextureView.TRANS_MODE_CENTER_INSIDE);
            }
        }
    }

    private void stopPlayer() {
        if (mVideoPlayerPresenterEx != null) {
            mVideoPlayerPresenterEx.destroy();
        }
    }

    private void initPresenter() {
        mVideoPlayerPresenterEx = new VideoPlayerPresenterEx(this, mVideoView, null, mRotateBtn, true) {
            // 覆盖只为让他不执行
            protected void orientRotateBtn() {
                showPortraitRotateIfNeed();
//                if(mIsLandscape){
//                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mRotateBtn.getLayoutParams();
//                    // 清楚
//                    layoutParams.addRule(RelativeLayout.CENTER_VERTICAL,0);
//
//                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
//                    layoutParams.addRule(RelativeLayout.ALIGN_TOP,R.id.bottom_button_zone);
//                    mRotateBtn.setLayoutParams(layoutParams);
//                }else{
//                    RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mRotateBtn.getLayoutParams();
//                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,0);
//                    layoutParams.addRule(RelativeLayout.ALIGN_TOP,0);
//
//                    layoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
//
//                    mRotateBtn.setLayoutParams(layoutParams);
//                }
            }
        };

        mLiveTaskPresenter = new LiveTaskPresenter(this, mWatchView, mMyRoomData);
        addBindActivityLifeCycle(mLiveTaskPresenter, false);

        mGiftPresenter = new GiftPresenter(mRoomChatMsgManager, false);
        addPushProcessor(mGiftPresenter);

        mRoomTextMsgPresenter = new RoomTextMsgPresenter(mRoomChatMsgManager);
        addPushProcessor(mRoomTextMsgPresenter);

        mRoomManagerPresenter = new RoomManagerPresenter(this, mRoomChatMsgManager, true);
        addPushProcessor(mRoomManagerPresenter);

        mGiftMallPresenter = new GiftMallPresenter(this, getBaseContext(), mMyRoomData, mComponentController);
        addBindActivityLifeCycle(mGiftMallPresenter, true);
        mGiftMallPresenter.setViewStub((ViewStub) findViewById(R.id.gift_mall_view_viewstub));

        mRoomViewerPresenter = new RoomViewerPresenter(mRoomChatMsgManager);
        addPushProcessor(mRoomViewerPresenter);

        mRoomStatusPresenter = new RoomStatusPresenter(mRoomChatMsgManager);
        addPushProcessor(mRoomStatusPresenter);

        mUserInfoPresenter = new UserInfoPresenter(this, mMyRoomData);

        // 点击事件代理，左右滑动隐藏组件的逻辑
        mTouchPresenter = new TouchPresenter(mTouchDelegateView);
        addBindActivityLifeCycle(mTouchPresenter, true);
        TouchPresenter.AnimationParams animationParams = new TouchPresenter.AnimationParams();
        animationParams.views = new View[]{
                mWatchTopInfoSingleView,
                mLiveCommentView,
                mGiftContinueViewGroup,
                mGiftRoomEffectView,
                mGiftAnimationView,
                $(R.id.bottom_button_view)
        };
        mTouchPresenter.setNeedHideViewsPortrait(animationParams);
        mTouchPresenter.setNeedHideViewsLandscape(animationParams);

        mTouchPresenter.setGestureAdapter(new TouchPresenter.GestureApater() {
            @Override
            public boolean onDown() {
                if (mComponentController != null && mComponentController.onEvent(
                        WatchComponentController.MSG_HIDE_INPUT_VIEW)) {
                    return true;
                }
                if (mGameModePresenter != null && mGameModePresenter.ismInputViewShow()) {
                    mGameModePresenter.hideInputArea();
                    return true;
                }
                return false;
            }
        });

        if (mMyRoomData.getLiveType() == LiveManager.TYPE_LIVE_GAME) {
            // 是游戏直播间
            mGameModePresenter = new GameModePresenter(this, mMyRoomData);
            mGameModePresenter.setGameBarrageViewStub((ViewStub) findViewById(R.id.game_barrage_viewstub));
            mGameModePresenter.setGameBottomViewStub((ViewStub) findViewById(R.id.game_bottom_viewstub));
            mGameModePresenter.setCommentView(mLiveCommentView);
            mGameModePresenter.setWatchTopView(mWatchTopInfoSingleView);
            mGameModePresenter.setCloseBtn(mCloseBtn);
            mGameModePresenter.setRotateBtn(mRotateBtn);
            mGameModePresenter.setBottomContainerView($(R.id.bottom_button_view));
            mGameModePresenter.setmTouchPresenter(mTouchPresenter);
            addBindActivityLifeCycle(mGameModePresenter, true);
        }
    }

    protected void leaveLiveToServer() {
        mLiveTaskPresenter.leaveLive();
    }

    @Override
    public boolean isStatusBarDark() {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGiftMallPresenter.onActivityResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGiftMallPresenter.onActivityPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPlayer();
        leaveLiveToServer();
        if (mComponentController != null) {
            mComponentController.release();
            mComponentController = null;
        }
        if (mSdkView != null) {
            mSdkView.releaseSdkView();
            mSdkView = null;
        }
        if (mHandlerThread != null) {
            mHandlerThread.destroy();
        }
    }


    // 直播结束
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LiveEndEvent event) {
        stopPlayer();
        showEndLiveFragment(true);
    }

    protected UserEndLiveFragment userEndLiveFragment;

    /**
     * 显示结束页fragment
     *
     * @param failure
     */
    protected void showEndLiveFragment(boolean failure) {
        //清空room信息
        RoomInfoGlobalCache.getsInstance().leaveCurrentRoom(mMyRoomData.getRoomId());

//      EventBus.getDefault().post(new GiftEventClass(GiftEvent.EVENT_TYPE_GIFT_HIDE_MALL_LIST));
        //显示结束页时主动隐藏一次键盘 防止聊天时弹出结束页 键盘不消失
        MyLog.w(TAG, "showEndLiveFragment viewerCnt = " + mMyRoomData.getViewerCnt());
        MyLog.w(TAG, "FollowOrUnfollowEvent showEndLiveFragment isFocused" + mMyRoomData.getUser().isFocused());

        KeyboardUtils.hideKeyboardImmediately(this);
        if (userEndLiveFragment == null) {
            this.userEndLiveFragment = new UserEndLiveFragment();
            Bundle bundle = UserEndLiveFragment.getBundle(mMyRoomData.getUid(), mMyRoomData.getRoomId(), mMyRoomData.getAvatarTs(),
                    mMyRoomData.getUser(), mMyRoomData.getViewerCnt(), mMyRoomData.getLiveType());
            this.userEndLiveFragment.setPresenter(new SdkEndLivePresenter(userEndLiveFragment, bundle));

            FragmentTransaction transaction = this.getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main_act_container, this.userEndLiveFragment);
            transaction.commitAllowingStateLoss();
//          userEndLiveFragment = UserEndLiveFragment.openFragment(this, R.id.main_act_container,
//                    mMyRoomData.getUid(), mMyRoomData.getRoomId(), mMyRoomData.getAvatarTs(),
//                    mMyRoomData.getUser(), mMyRoomData.getViewerCnt(), mMyRoomData.getLiveType());
        }
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

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(GiftEventClass.GiftMallEvent event) {
        switch (event.eventType) {
            case GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_HIDE_MALL_LIST: {
                mGiftMallPresenter.hideGiftMallView();
                if (mGameModePresenter == null || !mLandscape) {
                    mLiveCommentView.setVisibility(View.VISIBLE);
                }
            }
            break;

            case GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_SHOW_MALL_LIST: {
                mLiveCommentView.setVisibility(View.INVISIBLE);
                mGiftMallPresenter.showGiftMallView();
            }
            break;

            case GiftEventClass.GiftMallEvent.EVENT_TYPE_CLICK_SELECT_GIFT: {
                mLiveCommentView.setVisibility(View.INVISIBLE);
                mGiftMallPresenter.showGiftMallView();
                mGiftMallPresenter.selectGiftView((Integer) event.obj1);
            }
            break;
            case GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_GO_RECHARGE: {
                goToRecharge();
            }
            break;
        }
    }


    private RechargeFragment mRechargeFragment;

    /**
     * 在观看直播的过程中去充值
     */
    public void goToRecharge() {
        mRechargeFragment = (RechargeFragment) FragmentNaviUtils.addFragment(this, R.id.main_act_container, RechargeFragment.class, null, true, true, true);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventClass.FeedsVideoEvent event) {
        switch (event.mType) {
            case EventClass.FeedsVideoEvent.TYPE_PLAYING:
                if (mBlurIv.getVisibility() == View.VISIBLE) {
                    mBlurIv.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(BaseEvent.UserActionEvent event) {
        MyLog.e(TAG, "BaseEvent.UserActionEvent event type=" + event.type);
        // 该类型单独提出用指定的fastdoubleclick，防止fragment的崩溃
        if (event.type == BaseEvent.UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO) {
            startShowFloatPersonInfo((Long) event.obj1);
            return;
        } else if (event.type == BaseEvent.UserActionEvent.EVENT_TYPE_REQUEST_REFRESH_USER_RELATION) {
            mUserInfoPresenter.updateOwnerInfoFromServer();
            return;
        }
        if (CommonUtils.isFastDoubleClick()) {
            return;
        }
//        TODO 打开注释
        switch (event.type) {
//            case BaseEvent.UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_TICKET: {
//                clearTop();
//                long uid = (long) event.obj1;
//                int ticket = (int) event.obj2;
//                String liveId = (String) event.obj3;
//                RankingFragment.openFragment(this, ticket, mMyRoomData.getInitTicket(), uid, liveId, mMyRoomData.isTicketing() ? RankingFragment.PARAM_FROM_CURRENT : RankingFragment.PARAM_FROM_TOTAL, true);
//            }
//            break;
//            case BaseEvent.UserActionEvent.EVENT_TYPE_REQUEST_WANT_FOLLOW_USER: {
//                clearTop();
//                long uid = (long) event.obj1;
//                int ticket = (int) event.obj2;
//                String liveId = (String) event.obj3;
//                RankingFragment.openFragment(this, ticket, mMyRoomData.getInitTicket(), uid, liveId, mMyRoomData.isTicketing() ? RankingFragment.PARAM_FROM_CURRENT : RankingFragment.PARAM_FROM_TOTAL, true);
//            }
//            break;
//            case BaseEvent.UserActionEvent.EVENT_TYPE_TOUCH_DOWN_COMMENT_RC: {
//                mLikePresenter.startOtherHeart();
//                if (canSendLikeMsg()) {
//                    mLastSendLikeTime = System.currentTimeMillis();
//                    //按下屏幕的时候，发送弹幕消息
//                    mRoomChatMsgManager.sendLikeBarrageMessageAsync(mMyRoomData.getRoomId(), mMyRoomData.getUid(), mHeartView.getColorIndex(), mHeartView.getBitmapPath(mHeartView.getColorIndex() - 1));
//                }
//                if (mIsInputMode) {
//                    if (mIsShowSmilyPicker) {
//                        hideInputView("EVENT_TYPE_TOUCH_DOWN_COMMENT_RC");
//                    } else {
//                        KeyboardUtils.hideKeyboard(WatchActivity.this);
//                    }
//                }
//            }
//            break;
            case BaseEvent.UserActionEvent.EVENT_TYPE_REQUEST_LOOK_MORE_VIEWER: {
//                clearTop();
                viewerTopFromServer((RoomBaseDataModel) event.obj1);
            }
            break;
//            case BaseEvent.UserActionEvent.EVENT_TYPE_REQUEST_OPEN_SUPPORT_SELECT_VIEW: {
//                addSwitchAnchorViewIfNeed();
//            }
//            break;
//            case BaseEvent.UserActionEvent.EVENT_TYPE_REQUEST_CLOSE_SUPPORT_SELECT_VIEW: {
//                removeSwitchAnchorViewIfNeed();
//            }
//            break;
//            case BaseEvent.UserActionEvent.EVENT_TYPE_REQUEST_SWITCH_OTHER_ANCHOR: {
//                long uid = (long) event.obj1;
//                // 切换主播
//                if (uid == mPkRoomData.getUid()) {
//                    switchAnchor(mPkRoomData.getUid(), mPkRoomData.getAvatarTs(), mPkRoomData.getNickName(),
//                            mPkRoomData.getRoomId(), mPkRoomData.getVideoUrl(), mPkRoomData.getLocation(),
//                            mStatisChannelId, mPkRoomData.getLiveType());
//                }
//                removeSwitchAnchorViewIfNeed();
//            }
//            break;
//            case BaseEvent.UserActionEvent.EVENT_TYPE_REQUEST_LINE_ACCEPT: {
//                long uid = (long) event.obj1;
//                int mode = (int) event.obj2;
//                boolean isVideo = (boolean) event.obj3;
//                popAllFragment();
//                LineDataUtil.setInfoToVoip(mode, new LineDataUtil.Obj<>(LineDataUtil.KEY_LINE_FROM_ROOM_ID, mMyRoomData.getRoomId()));
//                MakeCallController.acceptCall(isVideo);
//            }
//            break;
//            case BaseEvent.UserActionEvent.EVENT_TYPE_REQUEST_LINE_MIC_STOP: {
//                DialogUtils.showNormalDialog(this, R.string.live_line_end, R.string.live_line_end_query, R.string.ok, R.string.cancel, (DialogInterface dialogInterface, int i) -> {
//                    int mode = (int) event.obj2;
//                    mWatchPanelOperator.cancelCall(mode);
//                }, null);
//            }
//            break;
//            case BaseEvent.UserActionEvent.EVENT_TYPE_REQUEST_LINE_CANCEL: {
//                int mode = (int) event.obj2;
//                mWatchPanelOperator.cancelCall(mode);
//            }
//            break;
//
//            case BaseEvent.UserActionEvent.EVENT_TYPE_CLICK_ATTACHMENT: {
//                String scheme = (String) event.obj1;
//
//                boolean isNeedParams = (Boolean) event.obj2;
//
//                if (scheme == null || scheme.equals("")) {
//                    break;
//                }
//
//                if (scheme.startsWith("walilive:")) {
//
//                    Uri uri = Uri.parse(scheme);
//                    if (uri.getScheme().equals("walilive")) {
//                        String type = uri.getQueryParameter(SchemeConstants.PARAMETER_SHOP_TYPE);
//                        String showType = uri.getQueryParameter(SchemeConstants.PARAMETER_SHOP_SHOW_TYPE);
//                        String id = uri.getQueryParameter(SchemeConstants.PARAMETER_SHOP_GOOD_ID);
//                        String url = uri.getQueryParameter(SchemeConstants.PARAMETER_SHOP_DETAIL_URL);
//                        String pid = uri.getQueryParameter(SchemeConstants.PARAMETER_SHOP_PID);
//                        if (TextUtils.isEmpty(pid)) {
//                            pid = mShopPresenter.getZuPid();
//                        }
//
//                        if (null != type && null != showType) {
//                            switch (type) {
//                                case LiveMallConstant.JD:
//                                    switch (showType) {
//                                        case LiveMallConstant.SHOP_DETAIL:
//                                            //TODO 京东店铺页面
//                                            break;
//                                        case LiveMallConstant.GOODS_DETAIL:
//                                            //TODO 京东商品详情
//                                            break;
//                                        case LiveMallConstant.URL_DETAIL:
//                                            LiveMallUtils.openJDH5ByUrl(url);
//                                            break;
//                                    }
//                                    break;
//                                case LiveMallConstant.MI:
//                                    break;
//                                case LiveMallConstant.TB:
//                                    switch (showType) {
//                                        case LiveMallConstant.SHOP_DETAIL:
//                                            if (id != null) {
//                                                LiveMallUtils.openTaoBaoShopH5(WatchActivity.this, Long.parseLong(id), mMyRoomData.getUid(), UserAccountManager.getInstance().getUuidAsLong(), mMyRoomData.getRoomId(), pid);
//                                            }
//                                            break;
//                                        case LiveMallConstant.GOODS_DETAIL:
//                                            if (id != null) {
//                                                LiveMallUtils.openTaoBaoDetailH5(WatchActivity.this, id, mMyRoomData.getUid(), UserAccountManager.getInstance().getUuidAsLong(), mMyRoomData.getRoomId(), pid);
//                                                LiveMallUtils.tapAdPush(UserAccountManager.getInstance().getUuidAsLong(), mMyRoomData.getUid(), mMyRoomData.getRoomId(), Long.parseLong(id), 2);
//                                            }
//                                            break;
//                                        case LiveMallConstant.URL_DETAIL:
//                                            if (url != null) {
//                                                try {
//                                                    LiveMallUtils.openTBH5ByUrl(WatchActivity.this, URLDecoder.decode(url, "UTF-8"), mMyRoomData.getUid(), UserAccountManager.getInstance().getUuidAsLong(), mMyRoomData.getRoomId(), pid);
//                                                } catch (UnsupportedEncodingException e) {
//                                                    e.printStackTrace();
//                                                }
//                                            }
//                                            break;
//                                    }
//                                    break;
//                            }
//
//                        } else {
//                            try {
//                                Intent intent = Intent.parseUri(scheme, Intent.URI_INTENT_SCHEME);
//                                startActivity(intent);
//                            } catch (URISyntaxException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                } else {
//                    if (isNeedParams) {
//                        if (scheme.indexOf("?") != -1) {
//                            scheme = scheme + "&zuid=" + mMyRoomData.getUid() + "&uuid=" + UserAccountManager.getInstance().getUuidAsLong() + "&lid=" + mMyRoomData.getRoomId();
//                        } else {
//                            scheme = scheme + "?zuid=" + mMyRoomData.getUid() + "&uuid=" + UserAccountManager.getInstance().getUuidAsLong() + "&lid=" + mMyRoomData.getRoomId();
//                        }
//                    }
//                    Intent intent;
//                    if ((int) event.obj3 == 1) {
//                        intent = new Intent(WatchActivity.this, HalfWebViewActivity.class);
//                        intent.putExtra(WebViewActivity.EXTRA_DISPLAY_TYPE, true);
//                    } else {
//                        intent = new Intent(WatchActivity.this, WebViewActivity.class);
//                        intent.putExtra(WebViewActivity.EXTRA_DISPLAY_TYPE, false);
//                    }
//                    intent.putExtra(WebViewActivity.EXTRA_URL, scheme);
//                    intent.putExtra(WebViewActivity.EXTRA_ZUID, (Long) event.obj4);
//                    startActivity(intent);
//                }
//            }
//            break;
//            case BaseEvent.UserActionEvent.EVENT_TYPE_CLICK_PUSH_IMG:
//                long productID = (long) event.obj1;
//                clearTop();
//                LiveMallFragment.openFragment(this, false, MyUserInfoManager.getInstance().getUser().getUid(), mMyRoomData.getRoomId(), mMyRoomData.getUid(), "", LiveMallFragment.OPEN_PRODUCT_INFO, productID);
//                break;
//
//            case BaseEvent.UserActionEvent.EVENT_TYPE_CLICK_SUPPORT_WIDGET:
//
//                Gift gift = GiftRepository.findGiftById((int) event.obj1);
//
//                BarrageMsg pushMsg = GiftRepository.createGiftBarrageMessage(gift.getGiftId(), gift.getName(), gift.getCatagory(),
//                        gift.getSendDescribe(), 1, 0, System.currentTimeMillis(), -1, mMyRoomData.getRoomId(), String.valueOf(mMyRoomData.getUid()), "", "", 0, false);
//                BarrageMessageManager.getInstance().pretendPushBarrage(pushMsg);
//                break;
        }
    }

    private void viewerTopFromServer(RoomBaseDataModel roomData) {
        mHandlerThread.post(LiveTask.viewerTop(roomData, new WeakReference<IActionCallBack>(this)));
    }

    private IWatchView mWatchView = new IWatchView() {
        @Override
        public void enterLive(EnterRoomInfo roomInfo) {
            WatchRoomCharactorManager.getInstance().clear();
            syncRoomEffect(mMyRoomData.getRoomId(), UserAccountManager.getInstance().getUuidAsLong(), mMyRoomData.getUid(), null);
        }
    };

    public static void openActivity(@NonNull Activity activity, RoomInfo roomInfo) {
        Intent intent = new Intent(activity, WatchSdkActivity.class);
        intent.putExtra(EXTRA_ROOM_INFO, roomInfo);
        activity.startActivity(intent);
    }

    @Override
    public void onClickHomepage(User user) {
//        TODO 主页去掉
//        if (user == null || user.getUid() == MyUserInfoManager.getInstance().getUser().getUid()) {
//            return;
//        }
//
//        long uuid = user.getUid();
//        Bundle bundle = new Bundle();
//        bundle.putLong(PersonInfoFragment.EXTRA_IN_USER_UUID, uuid);
//        bundle.putInt(PersonInfoFragment.EXTRA_IN_USER_CERTIFICATION_TYPE, user.getCertificationType());
//        clearTop();
//
//        if (mPersonInfoFragment == null) {
//            mPersonInfoFragment = FragmentNaviUtils.addFragment(this, R.id.main_act_container, PersonInfoFragment.class, bundle, true, true, true);
//        }
//        if (mPersonInfoFragment != null && mPersonInfoFragment instanceof PersonInfoFragment) {
//            PersonInfoFragment personInfoFragment = (PersonInfoFragment) mPersonInfoFragment;
//            personInfoFragment.setPersonInfoClickListener(this);
//        }
    }

    @Override
    public void onClickTopOne(User user) {
//        TODO 打开注释
//        if (user == null) {
//            return;
//        }
//
//        if (user.getUid() == mMyRoomData.getUid()) {
//            RankingFragment.openFragment(this, user.getLiveTicketNum(), mMyRoomData.getInitTicket(), user.getUid(), mMyRoomData.getRoomId(), RankingFragment.PARAM_FROM_TOTAL, true);
//        } else {
//            RankingFragment.openFragment(this, user.getLiveTicketNum(), mMyRoomData.getInitTicket(), user.getUid(), mMyRoomData.getRoomId(), RankingFragment.PARAM_FROM_TOTAL, false);
//        }
    }

    @Override
    public void onClickMainAvatar(User user) {
        //onClickBigAvatar(user);
        //qw 提的需求 点击头像进入主页而不是看大图
        onClickHomepage(user);
    }

    @Override
    public void onClickSixin(User user) {
        VersionCheckTask.checkUpdate(this);
    }

    @Override
    public ForbidManagePresenter provideForbidManagePresenter() {
        if (mForbidManagePresenter == null) {
            mForbidManagePresenter = new ForbidManagePresenter(this);
        }
        return mForbidManagePresenter;
    }

    /*拉取房间礼物属性信息*/
    protected void syncRoomEffect(final String roomId, long uuid, long zuid, Location location) {
        GiftRepository.getRoomEnterGiftInfo(roomId, uuid, zuid, location)
                .compose(this.<GiftInfoForEnterRoom>bindUntilEvent(ActivityEvent.DESTROY))
                .retryWhen(new RxRetryAssist(3, 5, true))
                .subscribe(new Observer<GiftInfoForEnterRoom>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, e);
                    }

                    @Override
                    public void onNext(GiftInfoForEnterRoom giftInfoForEnterRoom) {
//                        MyLog.d(TAG,"-------"+effectGiftModel.getGift());
                        // 不假装push，因为有剩余时长无法封装到barrage里面。
                        if (roomId.equals(mMyRoomData.getRoomId())) {
                            // 验证是否还是请求时的房间，但是还是会有延迟啊，切换房间时，还是会导致在错误的房间内播放的问题，
                            // 应该所有的礼物model都带上roomid，做过滤用后续。
                            // TODO

                            // 房间特效放入
                            for (GiftRecvModel effectGiftModel : giftInfoForEnterRoom.getEnterRoomGiftRecvModelList()) {
                                GiftRepository.processRoomEffectGiftMsgFromEnterRoom(effectGiftModel);
                            }

                            // 初始新票
                            mMyRoomData.setInitTicket(giftInfoForEnterRoom.getInitStarStickCount());

                            mMyRoomData.setTicket(giftInfoForEnterRoom.getInitStarStickCount());//发送刷新的event

                            // 这个房间的的礼物橱窗信息交付
//                            mGiftMallView.setGiftInfoForEnterRoom(giftInfoForEnterRoom.getmGiftInfoForThisRoom());
                            mGiftMallPresenter.setGiftInfoForEnterRoom(giftInfoForEnterRoom.getmGiftInfoForThisRoom());

                            // 星票前10，以及禁言权限
                            List<Long> Top10RankList = giftInfoForEnterRoom.getEnterRoomTicketTop10lList();
                            mRoomChatMsgManager.setRankTop(Top10RankList);
                            MyLog.v("Meg1234 isInspector=" + WatchRoomCharactorManager.getInstance().isInspector());
                            if (!WatchRoomCharactorManager.getInstance().isInspector()) {
                                if (Top10RankList != null && Top10RankList.size() > 0) {
                                    MyLog.w(TAG + " fetchThreeRankUser " + mMyRoomData.getUid() + " " + Top10RankList.get(0));
                                    LiveRoomCharactorManager.getInstance().setTopRank(mMyRoomData.getUid(), Top10RankList.get(0));
                                    //top进入房间会有提示
                                    if ((Top10RankList.get(0) == UserAccountManager.getInstance().getUuidAsLong())) {//TODO  && !mIsPrivate 私密判断
                                        //榜一获取禁言列表
                                        initBanSpeakerListAndShowTest();
                                    }
                                }
                            } else {
                                //巡查员获取禁言列表
                                initBanSpeakerListAndShowTest();
                            }
                        } else {
                            MyLog.e(TAG, "syncRoomEffect different roomid");
                        }
                    }

                });
    }

    private boolean mBanSpeakerListAlreadyGet = false;

    private void initBanSpeakerListAndShowTest() {
        if (!mBanSpeakerListAlreadyGet) {
            mBanSpeakerListAlreadyGet = true;
            mRoomChatMsgManager.sendLocalSystemMsg(getString(R.string.sys_msg), getString(R.string.top1_msg), mMyRoomData.getRoomId(), mMyRoomData.getUid());
            WatchRoomCharactorManager.initBanSpeakerList(UserAccountManager.getInstance().getUuidAsLong(), mMyRoomData.getUid(), mMyRoomData.getRoomId());
        }
    }

    private boolean fragmentBackPressed(Fragment fragment) {
        if (fragment != null && fragment instanceof FragmentListener) {
            if (((FragmentListener) fragment).onBackPressed()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            //退出栈弹出
            String fName = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName();
            if (!TextUtils.isEmpty(fName)) {
                Fragment fragment = fm.findFragmentByTag(fName);
                MyLog.w(TAG, "fragment name=" + fName + ", fragment=" + fragment);

                if (fragmentBackPressed(fragment)) {
                    return;
                }
                FragmentNaviUtils.popFragmentFromStack(this);
            }
        } else {
            if (mComponentController != null && mComponentController.onEvent(
                    WatchComponentController.MSG_ON_BACK_PRESSED)) {
                return;
            } else if (mGiftMallPresenter.isGiftMallViewVisibility()) {
                EventBus.getDefault().post(new GiftEventClass.GiftMallEvent(GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_HIDE_MALL_LIST));
                return;
            } else if (mGameModePresenter != null && mGameModePresenter.ismInputViewShow()) {
                mGameModePresenter.hideInputArea();
                return;
            }
            super.onBackPressed();
        }
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
        if (mComponentController != null) {
            mComponentController.onEvent(WatchComponentController.MSG_ON_ORIENT_LANDSCAPE);
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
        if (mComponentController != null) {
            mComponentController.onEvent(WatchComponentController.MSG_ON_ORIENT_PORTRAIT);
        }
        if (mGameModePresenter != null) {
            if (mCloseBtn.getVisibility() != View.VISIBLE) {
                mCloseBtn.setVisibility(View.VISIBLE);
            }
            if (mWatchTopInfoSingleView.getVisibility() != View.VISIBLE) {
                mWatchTopInfoSingleView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void processAction(String action, int errCode, Object... objects) {
        MyLog.w(TAG, "processAction : " + action + " , errCode : " + errCode);
        switch (action) {
            case MiLinkCommand.COMMAND_LIVE_VIEWER_TOP:
                processViewerTop(errCode, objects);
                break;
        }
    }

    private void processViewerTop(int errCode, Object... objects) {
        switch (errCode) {
            case ErrorCode.CODE_SUCCESS:
                RoomBaseDataModel roomData = (RoomBaseDataModel) objects[0];
                // 更新顶部观众
                roomData.getViewersList().clear();
                roomData.getViewersList().addAll((List) objects[1]);
                roomData.notifyViewersChange("processViewerTop");
                break;

        }
    }
}
