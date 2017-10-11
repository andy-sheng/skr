package com.wali.live.watchsdk.watch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.ImageView;

import com.base.activity.BaseActivity;
import com.base.dialog.DialogUtils;
import com.base.dialog.MyAlertDialog;
import com.base.event.SdkEventClass;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.CommonUtils;
import com.base.utils.rx.RxRetryAssist;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.cache.RoomInfoGlobalCache;
import com.mi.live.data.event.GiftEventClass;
import com.mi.live.data.gift.model.GiftInfoForEnterRoom;
import com.mi.live.data.gift.model.GiftRecvModel;
import com.mi.live.data.location.Location;
import com.mi.live.data.manager.LiveRoomCharacterManager;
import com.mi.live.data.manager.UserInfoManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.preference.PreferenceKeys;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.query.model.EnterRoomInfo;
import com.mi.live.data.repository.GiftRepository;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.data.room.model.RoomDataChangeEvent;
import com.mi.live.data.user.User;
import com.mi.live.engine.player.widget.VideoPlayerTextureView;
import com.mi.milink.sdk.base.CustomHandlerThread;
import com.thornbirds.component.IEventObserver;
import com.thornbirds.component.IParams;
import com.thornbirds.component.Params;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.common.barrage.manager.BarrageMessageManager;
import com.wali.live.common.flybarrage.view.FlyBarrageViewGroup;
import com.wali.live.common.gift.presenter.GiftMallPresenter;
import com.wali.live.common.gift.view.GiftAnimationView;
import com.wali.live.common.gift.view.GiftContinueViewGroup;
import com.wali.live.dao.Gift;
import com.wali.live.event.EventClass;
import com.wali.live.event.EventEmitter;
import com.wali.live.event.UserActionEvent;
import com.wali.live.manager.WatchRoomCharactorManager;
import com.wali.live.receiver.PhoneStateReceiver;
import com.wali.live.recharge.view.RechargeFragment;
import com.wali.live.utils.AppNetworkUtils;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.component.WatchSdkView;
import com.wali.live.watchsdk.endlive.UserEndLiveFragment;
import com.wali.live.watchsdk.personinfo.fragment.FloatInfoFragment;
import com.wali.live.watchsdk.personinfo.presenter.ForbidManagePresenter;
import com.wali.live.watchsdk.ranking.RankingPagerFragment;
import com.wali.live.watchsdk.scheme.SchemeConstants;
import com.wali.live.watchsdk.scheme.SchemeSdkActivity;
import com.wali.live.watchsdk.task.IActionCallBack;
import com.wali.live.watchsdk.task.LiveTask;
import com.wali.live.watchsdk.watch.event.LiveEndEvent;
import com.wali.live.watchsdk.watch.model.RoomInfo;
import com.wali.live.watchsdk.watch.presenter.IWatchView;
import com.wali.live.watchsdk.watch.presenter.LiveTaskPresenter;
import com.wali.live.watchsdk.watch.presenter.UserInfoPresenter;
import com.wali.live.watchsdk.watch.presenter.VideoPlayerPresenterEx;
import com.wali.live.watchsdk.watch.presenter.VideoShowPresenter;
import com.wali.live.watchsdk.watch.presenter.push.GiftPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomManagerPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomStatusPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomSystemMsgPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomTextMsgPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomViewerPresenter;
import com.wali.live.watchsdk.watch.view.IWatchVideoView;
import com.wali.live.watchsdk.webview.HalfWebViewActivity;
import com.wali.live.watchsdk.webview.WebViewActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observer;
import rx.functions.Action1;

import static com.wali.live.component.BaseSdkController.MSG_FOLLOW_COUNT_DOWN;
import static com.wali.live.component.BaseSdkController.MSG_ON_BACK_PRESSED;
import static com.wali.live.component.BaseSdkController.MSG_ON_LINK_MIC_START;
import static com.wali.live.component.BaseSdkController.MSG_ON_LIVE_SUCCESS;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_ON_PK_START;
import static com.wali.live.component.BaseSdkController.MSG_PAGE_DOWN;
import static com.wali.live.component.BaseSdkController.MSG_PAGE_UP;
import static com.wali.live.component.BaseSdkController.MSG_SWITCH_ROOM;

/**
 * Created by lan on 16/11/25.
 */
public class WatchSdkActivity extends BaseComponentSdkActivity
        implements FloatInfoFragment.FloatInfoClickListener,
        ForbidManagePresenter.IForbidManageProvider, IActionCallBack, IWatchVideoView {

    public static final String EXTRA_ROOM_INFO_LIST = "extra_room_info_list";
    public static final String EXTRA_ROOM_INFO_POSITION = "extra_room_info_position";

    // 播放器
    protected VideoPlayerTextureView mVideoView;

    protected ImageView mCloseBtn;// 关闭按钮
    protected ImageView mRotateBtn;// 关闭

    protected WatchComponentController mController;
    protected WatchSdkView mSdkView;
    protected final Action mAction = new Action();

    // 高斯蒙层
    private BaseImageView mMaskIv;
    protected GiftContinueViewGroup mGiftContinueViewGroup;
    // 礼物特效动画
    protected GiftAnimationView mGiftAnimationView;
    protected FlyBarrageViewGroup mFlyBarrageViewGroup;

    /**
     * presenter放在这里
     */
    private VideoPlayerPresenterEx mVideoPlayerPresenterEx;
    private RoomTextMsgPresenter mRoomTextMsgPresenter;
    private GiftPresenter mGiftPresenter;
    private RoomManagerPresenter mRoomManagerPresenter;
    private LiveTaskPresenter mLiveTaskPresenter;
    private GiftMallPresenter mGiftMallPresenter;
    private RoomViewerPresenter mRoomViewerPresenter;
    private RoomStatusPresenter mRoomStatusPresenter;
    private ForbidManagePresenter mForbidManagePresenter;
    protected UserInfoPresenter mUserInfoPresenter;

    private PhoneStateReceiver mPhoneStateReceiver;
    private RoomSystemMsgPresenter mRoomSystemMsgPresenter;
    private VideoShowPresenter mVideoShowPresenter;

    private ArrayList<RoomInfo> mRoomInfoList;
    private int mRoomInfoPosition;

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
        initReceiver();

        //尝试发送关键数据给服务器,允许即使多次调用，成功后就不再发送。
        if (!isMyRoom() && !check4GNet()) {
            WatchRoomCharactorManager.getInstance().clear();
            trySendDataWithServerOnce();
        }
    }

    public boolean isMyRoom() {
        //自己进入自己房间服务器会返回 CODE_PARAM_ERROR = 5004; //参数错误这个错误码，会有toast房间不存在弹出。在这里拦截比较好。
        if (mMyRoomData.getUid() == UserAccountManager.getInstance().getUuidAsLong()) {
            //自己不能进自己房间
            DialogUtils.showCancelableDialog(this,
                    "",
                    getString(R.string.can_not_enter_room_use_myself),
                    R.string.i_know,
                    0,
                    new DialogUtils.IDialogCallback() {
                        @Override
                        public void process(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    },
                    null);
            return true;
        }
        return false;
    }

    @Override
    public boolean isKeyboardResize() {
        return false;
    }

    /**
     * 这里的方法会在初始时调用一次，会在账号或milink刚登录上在基类接受event也会调用，
     * 所以里面的方法依据要求要具备能被不断调用的能力
     */
    @Override
    protected void trySendDataWithServerOnce() {
        mUserInfoPresenter.updateOwnerInfo();
        if (!MiLinkClientAdapter.getsInstance().isTouristMode()) {
            MyUserInfoManager.getInstance().syncSelfDetailInfo();
        }
        mLiveTaskPresenter.enterLive();
        if (TextUtils.isEmpty(mMyRoomData.getVideoUrl())) {
            getVideoUrlFromServer();
        } else {
            MyLog.d(TAG, "trySendDataWithServerOnce startPlayer");
            startPlayer();
        }
    }

    private void getVideoUrlFromServer() {
        if (mVideoShowPresenter == null) {
            mVideoShowPresenter = new VideoShowPresenter(this);
            addPresent(mVideoShowPresenter);
        }
        mVideoShowPresenter.getVideoUrlByRoomId(mMyRoomData.getUid(), mMyRoomData.getRoomId());
    }

    @Override
    protected void tryClearData() {
        mUserInfoPresenter.clearLoginFlag();
    }

    @Override
    protected String getTAG() {
        return "WatchSdkActivity";
    }

    private void initData() {
        Intent data = getIntent();
        if (data == null) {
            return;
        }

        mRoomInfo = data.getParcelableExtra(EXTRA_ROOM_INFO);
        mRoomInfoList = data.getParcelableArrayListExtra(EXTRA_ROOM_INFO_LIST);
        mRoomInfoPosition = data.getIntExtra(EXTRA_ROOM_INFO_POSITION, 0);
        if (mRoomInfo == null && mRoomInfoList != null) {
            mRoomInfo = mRoomInfoList.get(mRoomInfoPosition);
        }
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
        mMyRoomData.setGameId(mRoomInfo.getGameId());
        mMyRoomData.setEnableShare(mRoomInfo.isEnableShare());
    }

    private void initView() {

        // 封面模糊图
        mMaskIv = $(R.id.mask_iv);
        String url = mRoomInfo.getCoverUrl();
        if (TextUtils.isEmpty(url)) {
            url = AvatarUtils.getAvatarUrlByUidTs(mRoomInfo.getPlayerId(), AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE, mRoomInfo.getAvatar());
        }
        AvatarUtils.loadAvatarByUrl(mMaskIv, url, false, true, R.drawable.rect_loading_bg_24292d);

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
        orientCloseBtn(isDisplayLandscape());

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

        mController = new WatchComponentController(mMyRoomData, mRoomChatMsgManager);
        mController.setVerticalList(mRoomInfoList, mRoomInfoPosition);

        mSdkView = new WatchSdkView(this, mController);
        mSdkView.setupView(mMyRoomData.getLiveType() == LiveManager.TYPE_LIVE_GAME);
        mSdkView.startView();

        mAction.registerAction();

        mFlyBarrageViewGroup = $(R.id.fly_barrage_viewgroup);
        addBindActivityLifeCycle(mFlyBarrageViewGroup, true);
    }

    private void startPlayer() {
        if (mVideoPlayerPresenterEx != null) {
            MyLog.d(TAG, "startPlayer enter");
            // if (!mVideoPlayerPresenterEx.isActivate()) {
            MyLog.d(TAG, "startPlayer start");
            mVideoPlayerPresenterEx.play(mMyRoomData.getVideoUrl());//, mVideoContainer, false, VideoPlayerTextureView.TRANS_MODE_CENTER_INSIDE, true, true);
            mVideoPlayerPresenterEx.setTransMode(VideoPlayerTextureView.TRANS_MODE_CENTER_INSIDE);
            // }
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

        mRoomManagerPresenter = new RoomManagerPresenter(this, mRoomChatMsgManager, true, mMyRoomData);
        addPushProcessor(mRoomManagerPresenter);
        mRoomManagerPresenter.syncOwnerInfo(mMyRoomData.getUid(), true); // 拉取一下主播信息，同步观看端是否是管理员

        mGiftMallPresenter = new GiftMallPresenter(this, getBaseContext(), mMyRoomData, mController);
        addBindActivityLifeCycle(mGiftMallPresenter, true);
        mGiftMallPresenter.setViewStub((ViewStub) findViewById(R.id.gift_mall_view_viewstub));

        mRoomViewerPresenter = new RoomViewerPresenter(mRoomChatMsgManager);
        addPushProcessor(mRoomViewerPresenter);

        mRoomStatusPresenter = new RoomStatusPresenter(mRoomChatMsgManager);
        addPushProcessor(mRoomStatusPresenter);

        mRoomSystemMsgPresenter = new RoomSystemMsgPresenter(this, mRoomChatMsgManager, mVideoPlayerPresenterEx);
        addPushProcessor(mRoomSystemMsgPresenter);

        mUserInfoPresenter = new UserInfoPresenter(this, mMyRoomData);

        if (mMyRoomData.getLiveType() == LiveManager.TYPE_LIVE_GAME) {
        }
    }

    private void initReceiver() {
        mPhoneStateReceiver = PhoneStateReceiver.registerReceiver(this);
    }

    protected void leaveLiveToServer() {
        if (mLiveTaskPresenter != null) {
            mLiveTaskPresenter.leaveLive();
        }
    }

    @Override
    public boolean isStatusBarDark() {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        KeyboardUtils.hideKeyboard(this);

//        // TEST
//        Observable.interval(5, 5, TimeUnit.SECONDS)
//                .observeOn(AndroidSchedulers.mainThread())
//                .take(3)
//                .subscribe(new Action1<Long>() {
//                    @Override
//                    public void call(Long aLong) {
//                        RedEnvelopeModel redEnvelopeModel = new RedEnvelopeModel();
//                        redEnvelopeModel.setRedEnvelopeId("" + aLong);
//                        redEnvelopeModel.setUserId(100067);
//                        redEnvelopeModel.setNickName("biglee");
//                        redEnvelopeModel.setAvatarTimestamp(0);
//                        redEnvelopeModel.setLevel(1);
//                        redEnvelopeModel.setRoomId("123456789");
//                        redEnvelopeModel.setMsg("红包测试");
//                        redEnvelopeModel.setGemCnt((int)(long)aLong + 1);
//                        redEnvelopeModel.setType(((int)(long)aLong % 3) + 1);
//                        EventBus.getDefault().post(new GiftEventClass.GiftAttrMessage.RedEnvelope(redEnvelopeModel));
//                    }
//                }, new Action1<Throwable>() {
//                    @Override
//                    public void call(Throwable throwable) {
//                    }
//                });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void finish() {
        KeyboardUtils.hideKeyboardImmediately(this);
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPlayer();
        leaveLiveToServer();
        unregisterReceiver();

        if (mController != null) {
            mController.release();
            mController = null;
        }
        if (mSdkView != null) {
            mSdkView.stopView();
            mSdkView.release();
            mSdkView = null;
        }
        if (mHandlerThread != null) {
            mHandlerThread.destroy();
        }
    }

    private void unregisterReceiver() {
        PhoneStateReceiver.unregisterReceiver(this, mPhoneStateReceiver);
    }

    // 直播结束
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LiveEndEvent event) {
        MyLog.d(TAG, "liveEndEvent");

        stopPlayer();
        showEndLiveFragment(true, UserEndLiveFragment.ENTER_TYPE_LIVE_END);
    }

    protected UserEndLiveFragment userEndLiveFragment;

    /**
     * 显示结束页fragment
     *
     * @param failure
     */
    protected void showEndLiveFragment(boolean failure, String type) {
        //清空room信息
        RoomInfoGlobalCache.getsInstance().leaveCurrentRoom(mMyRoomData.getRoomId());
        //显示结束页时主动隐藏一次键盘 防止聊天时弹出结束页 键盘不消失
        MyLog.w(TAG, "showEndLiveFragment viewerCnt = " + mMyRoomData.getViewerCnt());
        MyLog.w(TAG, "FollowOrUnfollowEvent showEndLiveFragment isFocused" + mMyRoomData.getUser().isFocused());
        KeyboardUtils.hideKeyboardImmediately(this);

        if (userEndLiveFragment == null) {
            boolean hasRoomList = mController.removeCurrentRoom();
            this.userEndLiveFragment = UserEndLiveFragment.openFragment(this,
                    mMyRoomData.getUid(), mMyRoomData.getRoomId(), mMyRoomData.getAvatarTs(),
                    mMyRoomData.getUser(), mMyRoomData.getViewerCnt(), mMyRoomData.getLiveType(),
                    mGiftMallPresenter.getSpendTicket(), System.currentTimeMillis() - mMyRoomData.getEnterRoomTime(), type,
                    mMyRoomData.getNickName(), hasRoomList);
        }
    }

    /**
     * 显示个人资料的浮框
     */
    private void startShowFloatPersonInfo(long uid) {
        if (uid <= 0) {
            return;
        }
        FloatInfoFragment.openFragment(this, uid, mMyRoomData.getUid(), mMyRoomData.getRoomId(), mMyRoomData.getVideoUrl(), this, mMyRoomData.getEnterRoomTime());
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(GiftEventClass.GiftMallEvent event) {
        switch (event.eventType) {
            case GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_HIDE_MALL_LIST: {
                mGiftMallPresenter.hideGiftMallView();
            }
            break;
            case GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_SHOW_MALL_LIST: {
                mGiftMallPresenter.showGiftMallView();
            }
            break;
            case GiftEventClass.GiftMallEvent.EVENT_TYPE_CLICK_SELECT_GIFT: {
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
                if (mMaskIv.getVisibility() == View.VISIBLE) {
                    mMaskIv.setVisibility(View.GONE);
                }
                if (mSdkView != null) {
                    mSdkView.postPrepare();
                }
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.PhoneStateEvent event) {
        if (mVideoPlayerPresenterEx == null) {
            MyLog.d(TAG, "mVideoPlayerPresenterEx is null");
            return;
        }
        switch (event.type) {
            case EventClass.PhoneStateEvent.TYPE_PHONE_STATE_IDLE:
                mVideoPlayerPresenterEx.enableReconnect(true);
                mVideoPlayerPresenterEx.resume();
                break;
            case EventClass.PhoneStateEvent.TYPE_PHONE_STATE_RING:
                mVideoPlayerPresenterEx.enableReconnect(false);
                mVideoPlayerPresenterEx.pause();
                break;
            case EventClass.PhoneStateEvent.TYPE_PHONE_STATE_OFFHOOK:
                mVideoPlayerPresenterEx.enableReconnect(false);
                mVideoPlayerPresenterEx.pause();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(UserActionEvent event) {
        MyLog.e(TAG, "UserActionEvent event type=" + event.type);
        // 该类型单独提出用指定的fastdoubleclick，防止fragment的崩溃
        if (event.type == UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO) {
            startShowFloatPersonInfo((Long) event.obj1);
            return;
        }
        if (CommonUtils.isFastDoubleClick()) {
            return;
        }
        switch (event.type) {
            case UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_TICKET: {
                long uid = (long) event.obj1;
                int ticket = (int) event.obj2;
                String liveId = (String) event.obj3;
                RankingPagerFragment.openFragment(this, ticket, mMyRoomData.getInitTicket(), uid, liveId,
                        mMyRoomData.isTicketing() ? RankingPagerFragment.PARAM_FROM_CURRENT : RankingPagerFragment.PARAM_FROM_TOTAL,
                        true, isDisplayLandscape());
            }
            break;
            case UserActionEvent.EVENT_TYPE_REQUEST_LOOK_MORE_VIEWER: {
                viewerTopFromServer((RoomBaseDataModel) event.obj1);
            }
            break;
            case UserActionEvent.EVENT_TYPE_CLICK_ATTACHMENT: {
                String scheme = (String) event.obj1;
                boolean isNeedParams = (Boolean) event.obj2;

                MyLog.d(TAG, "scheme=" + scheme + ", isNeedParams=" + isNeedParams);
                if (TextUtils.isEmpty(scheme)) {
                    break;
                }
                if (scheme.startsWith(SchemeConstants.SCHEME_WALILIVE)) {
                    Uri uri = Uri.parse(scheme);
                    if (uri.getScheme().equals(SchemeConstants.SCHEME_WALILIVE)) {
                        String type = uri.getQueryParameter(SchemeConstants.PARAM_SHOP_TYPE);
                        String showType = uri.getQueryParameter(SchemeConstants.PARAM_SHOP_SHOW_TYPE);

                        MyLog.d(TAG, "type=" + type + ", showType=" + showType);
                        if (type == null || showType == null) {
                            SchemeSdkActivity.openActivity(this, uri);
                        }
                    }
                } else {
                    if (isNeedParams) {
                        if (scheme.indexOf("?") != -1) {
                            scheme = scheme + "&zuid=" + mMyRoomData.getUid() + "&uuid=" + UserAccountManager.getInstance().getUuidAsLong() + "&lid=" + mMyRoomData.getRoomId();
                        } else {
                            scheme = scheme + "?zuid=" + mMyRoomData.getUid() + "&uuid=" + UserAccountManager.getInstance().getUuidAsLong() + "&lid=" + mMyRoomData.getRoomId();
                        }
                    }
                    Intent intent;
                    if ((int) event.obj3 == 1) {
                        intent = new Intent(WatchSdkActivity.this, HalfWebViewActivity.class);
                        intent.putExtra(WebViewActivity.EXTRA_DISPLAY_TYPE, true);
                    } else {
                        intent = new Intent(WatchSdkActivity.this, WebViewActivity.class);
                        intent.putExtra(WebViewActivity.EXTRA_DISPLAY_TYPE, false);
                    }
                    intent.putExtra(WebViewActivity.EXTRA_URL, scheme);
                    intent.putExtra(WebViewActivity.EXTRA_ZUID, (Long) event.obj4);
                    startActivity(intent);
                }
            }
            break;
            case UserActionEvent.EVENT_TYPE_CLICK_SUPPORT_WIDGET:
                Gift gift = GiftRepository.findGiftById((int) event.obj1);
                if (gift != null) {
                    BarrageMsg pushMsg = GiftRepository.createGiftBarrageMessage(gift.getGiftId(), gift.getName(), gift.getCatagory(),
                            gift.getSendDescribe(), 1, 0, System.currentTimeMillis(), -1, mMyRoomData.getRoomId(), String.valueOf(mMyRoomData.getUid()), "", "", 0);
                    BarrageMessageManager.getInstance().pretendPushBarrage(pushMsg);
                }
                break;
        }
    }

    @Override
    protected void onEventShare(EventClass.ShareEvent event) {
        if (event != null && event.state == EventClass.ShareEvent.TYPE_SUCCESS) {
            mRoomChatMsgManager.sendShareBarrageMessageAsync(mMyRoomData.getRoomId(), mMyRoomData.getUid());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventEmitter.EnterRoomList event) {
        if (event != null) {
            MyLog.d(TAG, "enterRoomList");
            mController.enterRoomList(this);
            finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RoomDataChangeEvent event) {
        if (event == null) {
            return;
        }
        switch (event.type) {
            case RoomDataChangeEvent.TYPE_CHANGE_USER_INFO_COMPLETE:
                MyLog.d(TAG, "receive TYPE_CHANGE_USER_INFO_COMPLETE");
                RoomBaseDataModel roomBaseDataModel = event.source;
                if (roomBaseDataModel != null && !roomBaseDataModel.isFocused()
                        && (mMyRoomData.getLiveType() == LiveManager.TYPE_LIVE_GAME)) {
                    int guideFollowTs = PreferenceUtils.getSettingInt(PreferenceKeys.PRE_KEY_GAME_FOLLOW_TIME, 0);
                    if (guideFollowTs > 0) {
                        mController.postEvent(MSG_FOLLOW_COUNT_DOWN,
                                new Params().putItem(guideFollowTs));
                    }
                }
                break;
            default:
                break;
        }
    }

    private void viewerTopFromServer(RoomBaseDataModel roomData) {
        if (TextUtils.isEmpty(roomData.getRoomId())) {
            MyLog.d(TAG, "viewerTop roomId is empty");
            return;
        }
        mHandlerThread.post(LiveTask.viewerTop(roomData, new WeakReference<IActionCallBack>(this)));
    }

    private IWatchView mWatchView = new IWatchView() {
        @Override
        public void enterLive(EnterRoomInfo roomInfo) {
            MyLog.d(TAG, "enterLive success");
            if (roomInfo != null) {
                updateVideoUrl(roomInfo.getDownStreamUrl());
            }
            //TODO 这段代码迁移到了switchRoom
            //WatchRoomCharactorManager.getInstance().clear();
            syncRoomEffect(mMyRoomData.getRoomId(), UserAccountManager.getInstance().getUuidAsLong(), mMyRoomData.getUid(), null);
            if (mController != null) {
                mController.postEvent(MSG_ON_LIVE_SUCCESS);
                if (roomInfo.getMicBeginInfo() != null) {
                    mController.postEvent(MSG_ON_LINK_MIC_START, new Params().putItem(roomInfo.getMicBeginInfo()));
                }
                if (roomInfo.getPkStartInfo() != null) {
                    mController.postEvent(MSG_ON_PK_START, new Params().putItem(roomInfo.getPkStartInfo()));
                }
            }
        }
    };

//    @Override
//    public void onClickHomepage(User user) {
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
//    }

    @Override
    public void onClickTopOne(User user) {
//        TODO 打开注释
//        if (user == null) {
//            return;
//        }
//
//        if (user.getUid() == mMyRoomData.getUid()) {
//            RankingPagerFragment.openFragment(this, user.getLiveTicketNum(), mMyRoomData.getInitTicket(), user.getUid(), mMyRoomData.getRoomId(), RankingPagerFragment.PARAM_FROM_TOTAL, true);
//        } else {
//            RankingPagerFragment.openFragment(this, user.getLiveTicketNum(), mMyRoomData.getInitTicket(), user.getUid(), mMyRoomData.getRoomId(), RankingPagerFragment.PARAM_FROM_TOTAL, false);
//        }
    }

    @Override
    public void onClickMainAvatar(User user) {
        //onClickBigAvatar(user);
        //qw 提的需求 点击头像进入主页而不是看大图
        //onClickHomepage(user);
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

                            //临时加的.roomData里的原user星票数一直是0，在这里重新更新了一下user
                            mMyRoomData.setUser(UserInfoManager.getUserInfoByUuid(mMyRoomData.getUid(), false));
                            mMyRoomData.setTicket(giftInfoForEnterRoom.getInitStarStickCount() > mMyRoomData.getTicket() ?
                                    giftInfoForEnterRoom.getInitStarStickCount() : mMyRoomData.getTicket());//发送刷新的event
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
                                    LiveRoomCharacterManager.getInstance().setTopRank(mMyRoomData.getUid(), Top10RankList.get(0));
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
            if (mController != null && mController.postEvent(MSG_ON_BACK_PRESSED)) {
                return;
            } else if (mGiftMallPresenter.isGiftMallViewVisibility()) {
                EventBus.getDefault().post(new GiftEventClass.GiftMallEvent(GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_HIDE_MALL_LIST));
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

    private void orientCloseBtn(boolean isLandscape) {
        if (mCloseBtn == null) {
            return;
        }
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams)
                mCloseBtn.getLayoutParams();
        if (!isLandscape && BaseActivity.isProfileMode()) {
            layoutParams.topMargin = layoutParams.rightMargin + BaseActivity.getStatusBarHeight();
        } else {
            layoutParams.topMargin = layoutParams.rightMargin;
        }
        mCloseBtn.setLayoutParams(layoutParams);
    }

    protected void orientLandscape() {
        if (mGiftContinueViewGroup != null) {
            mGiftContinueViewGroup.setOrient(true);
        }
        if (mController != null) {
            mController.postEvent(MSG_ON_ORIENT_LANDSCAPE);
        }
        orientCloseBtn(true);
    }

    protected void orientPortrait() {
        if (mGiftContinueViewGroup != null) {
            mGiftContinueViewGroup.setOrient(false);
        }
        if (mController != null) {
            mController.postEvent(MSG_ON_ORIENT_PORTRAIT);
        }
        orientCloseBtn(false);
    }

    private boolean check4GNet() {
        if (AppNetworkUtils.is4g()) {
            MyAlertDialog alertDialog = new MyAlertDialog.Builder(this).create();
            alertDialog.setMessage(GlobalData.app().getString(R.string.live_traffic_tip));
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getString(R.string.live_traffic_positive), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    trySendDataWithServerOnce();
                    dialog.dismiss();
                }
            });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.live_traffic_negative), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                    dialog.dismiss();
                }
            });
            alertDialog.setCancelable(false);
            alertDialog.show();
            return true;
        }
        return false;
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

    @Override
    public void onKickEvent(String msg) {
        stopPlayer();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.KickEvent event) {
        stopPlayer();
        DialogUtils.showCancelableDialog(this,
                "",
                GlobalData.app().getResources().getString(R.string.have_been_kicked),
                R.string.i_know,
                0,
                new DialogUtils.IDialogCallback() {
                    @Override
                    public void process(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                },
                null);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventExchange(EventClass.H5ExchangeEvent event) {
        WebViewActivity.open(this, WebViewActivity.GEM_EXCHANGE_H5_URL);
    }

    @Override
    public void updateVideoUrl(String videoUrl) {
        if (TextUtils.isEmpty(mMyRoomData.getVideoUrl()) && !TextUtils.isEmpty(videoUrl)) {
            mMyRoomData.setVideoUrl(videoUrl);
            MyLog.d(TAG, "updateVideoUrl startPlayer");
            startPlayer();
        }
    }

    @Override
    public void updateRoomInfo(String roomId, String videoUrl) {
        // 更新房间id，更新拉流地址，同时因为之前没有房间id，所以再次进一次房间
        mMyRoomData.setRoomId(roomId);
        updateVideoUrl(videoUrl);
        if (mLiveTaskPresenter != null) {
            mLiveTaskPresenter.enterLive();
        }
    }

    @Override
    public void notifyLiveEnd() {
        showEndLiveFragment(true, UserEndLiveFragment.ENTER_TYPE_LATE);
    }

    private class Action implements IEventObserver {

        private void registerAction() {
            if (mController != null) {
                mController.registerObserverForEvent(MSG_PAGE_DOWN, this);
                mController.registerObserverForEvent(MSG_PAGE_UP, this);
            }
        }

        private void unregisterAction() {
            if (mController != null) {
                mController.unregisterObserver(this);
            }
        }

        @Override
        public boolean onEvent(int event, IParams params) {
            switch (event) {
                case MSG_PAGE_UP:
                    MyLog.d(TAG, "page up");
                    mController.switchToNextPosition();
                    switchRoom();
                    if (mSdkView != null) {
                        MyLog.d(TAG, "page down internal");
                        mSdkView.switchToNextRoom();
                    }
                    break;
                case MSG_PAGE_DOWN:
                    MyLog.d(TAG, "page down");
                    mController.switchToLastPosition();
                    switchRoom();
                    if (mSdkView != null) {
                        MyLog.d(TAG, "page up internal");
                        mSdkView.switchToLastRoom();
                    }
                    break;
                default:
                    break;
            }
            return false;
        }

        private void switchRoom() {
            if (!isFinishing()) {
                MyLog.w(TAG, "switch anchor: leave room user=" + mMyRoomData.getUser());
                // 清除管理信息
                WatchRoomCharactorManager.getInstance().clear();
                // 清除房间消息
                mRoomChatMsgManager.clear();

                // 重置Presenter
                mVideoPlayerPresenterEx.reset();
                mUserInfoPresenter.reset();
                mLiveTaskPresenter.reset();
                if (mVideoShowPresenter != null) {
                    mVideoShowPresenter.reset();
                }
                mGiftMallPresenter.reset();

                // 发送离开房间给服务器
                leaveLiveToServer();

                // 重置对应的view
                mFlyBarrageViewGroup.reset();
                mGiftAnimationView.reset();
                mGiftContinueViewGroup.reset();
                mGiftRoomEffectView.reset();
                mSdkView.reset();
                mController.postEvent(MSG_SWITCH_ROOM);

                // 切换房间后，进入当前房间
                mController.switchRoom();
                MyLog.d(TAG, "liveType=" + mMyRoomData.getLiveType() + " @" + mMyRoomData.hashCode());
                mSdkView.postSwitch(mMyRoomData.getLiveType() == LiveManager.TYPE_LIVE_GAME);

                // 重新获取当前房间信息，并重新进入房间
                trySendDataWithServerOnce();
            }
        }
    }

    public static void openActivity(@NonNull Activity activity, RoomInfo roomInfo) {
        Intent intent = new Intent(activity, WatchSdkActivity.class);
        intent.putExtra(EXTRA_ROOM_INFO, roomInfo);
        activity.startActivity(intent);
    }

    public static boolean openActivity(@NonNull Activity activity, ArrayList<RoomInfo> roomInfoList, int position) {
        if (position < 0 || position >= roomInfoList.size()) {
            return false;
        }
        Intent intent = new Intent(activity, WatchSdkActivity.class);
        intent.putExtra(EXTRA_ROOM_INFO_LIST, roomInfoList);
        intent.putExtra(EXTRA_ROOM_INFO_POSITION, position);
        activity.startActivity(intent);
        return true;
    }
}
