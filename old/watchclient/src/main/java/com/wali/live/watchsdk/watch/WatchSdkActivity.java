package com.wali.live.watchsdk.watch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.ViewStub;
import android.view.WindowManager;

import com.base.dialog.DialogUtils;
import com.base.dialog.MyAlertDialog;
import com.base.event.SdkEventClass;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.CommonUtils;
import com.base.utils.SelfUpdateManager;
import com.base.utils.rx.RxRetryAssist;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.account.channel.HostChannelManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.cache.RoomInfoGlobalCache;
import com.mi.live.data.event.GiftEventClass;
import com.mi.live.data.gamecenter.GameCenterDataManager;
import com.mi.live.data.gamecenter.model.GameInfoModel;
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
import com.mi.milink.sdk.base.CustomHandlerThread;
import com.thornbirds.component.IEventObserver;
import com.thornbirds.component.IParams;
import com.thornbirds.component.Params;
import com.trello.rxlifecycle.ActivityEvent;
import com.wali.live.common.barrage.manager.BarrageMessageManager;
import com.wali.live.common.gift.presenter.GiftMallPresenter;
import com.wali.live.dao.Gift;
import com.wali.live.event.EventClass;
import com.wali.live.event.EventEmitter;
import com.wali.live.event.UserActionEvent;
import com.wali.live.manager.WatchRoomCharactorManager;
import com.wali.live.receiver.NetworkReceiver;
import com.wali.live.receiver.PhoneStateReceiver;
import com.wali.live.recharge.RechargeDirectPayFragment;
import com.wali.live.recharge.view.RechargeFragment;
import com.wali.live.utils.AppNetworkUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.endlive.UserEndLiveFragment;
import com.wali.live.watchsdk.personinfo.fragment.FloatInfoFragment;
import com.wali.live.watchsdk.personinfo.presenter.ForbidManagePresenter;
import com.wali.live.watchsdk.ranking.RankingPagerFragment;
import com.wali.live.watchsdk.receiver.ScreenStateReceiver;
import com.wali.live.watchsdk.scheme.SchemeConstants;
import com.wali.live.watchsdk.scheme.SchemeSdkActivity;
import com.wali.live.watchsdk.statistics.MilinkStatistics;
import com.wali.live.watchsdk.statistics.item.AliveStatisticItem;
import com.wali.live.watchsdk.task.IActionCallBack;
import com.wali.live.watchsdk.task.LiveTask;
import com.wali.live.watchsdk.watch.event.LiveEndEvent;
import com.wali.live.watchsdk.watch.fragment.BaseWatchFragment;
import com.wali.live.watchsdk.watch.fragment.WatchGameFragment;
import com.wali.live.watchsdk.watch.fragment.WatchNormalFragment;
import com.wali.live.watchsdk.watch.model.RoomInfo;
import com.wali.live.watchsdk.watch.model.WatchGameInfoConfig;
import com.wali.live.watchsdk.watch.presenter.IWatchView;
import com.wali.live.watchsdk.watch.presenter.LiveTaskPresenter;
import com.wali.live.watchsdk.watch.presenter.UserInfoPresenter;
import com.wali.live.watchsdk.watch.presenter.VideoShowPresenter;
import com.wali.live.watchsdk.watch.presenter.push.GiftPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomManagerPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomStatusPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomSystemMsgPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomTextMsgPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomViewerPresenter;
import com.wali.live.watchsdk.watch.presenter.push.VipUserActionMsgPresenter;
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

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.wali.live.component.BaseSdkController.MSG_FOLLOW_COUNT_DOWN;
import static com.wali.live.component.BaseSdkController.MSG_FORCE_ROTATE_SCREEN;
import static com.wali.live.component.BaseSdkController.MSG_NEW_VIDEO_URL;
import static com.wali.live.component.BaseSdkController.MSG_ON_BACK_PRESSED;
import static com.wali.live.component.BaseSdkController.MSG_ON_LINK_MIC_START;
import static com.wali.live.component.BaseSdkController.MSG_ON_LIVE_SUCCESS;
import static com.wali.live.component.BaseSdkController.MSG_ON_PK_START;
import static com.wali.live.component.BaseSdkController.MSG_PAGE_DOWN;
import static com.wali.live.component.BaseSdkController.MSG_PAGE_UP;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_COMPLETED;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_READY;
import static com.wali.live.component.BaseSdkController.MSG_PLAYER_SOUND_ON;
import static com.wali.live.component.BaseSdkController.MSG_SWITCH_ROOM;

/**
 * Created by lan on 16/11/25.
 */
public class WatchSdkActivity extends BaseComponentSdkActivity
        implements FloatInfoFragment.FloatInfoClickListener,
        ForbidManagePresenter.IForbidManageProvider, IActionCallBack, IWatchVideoView, WatchSdkActivityInterface {

    public static final String EXTRA_ROOM_INFO_LIST = "extra_room_info_list";
    public static final String EXTRA_ROOM_INFO_POSITION = "extra_room_info_position";
    public static final String ROOM_DATA = "room_data";

    protected WatchComponentController mController;
    protected final Action mAction = new Action();

    /**
     * presenter放在这里
     */
    private RoomTextMsgPresenter mRoomTextMsgPresenter;
    private GiftPresenter mGiftPresenter;
    private RoomManagerPresenter mRoomManagerPresenter;
    private LiveTaskPresenter mLiveTaskPresenter;
    private GiftMallPresenter mGiftMallPresenter;
    private RoomViewerPresenter mRoomViewerPresenter;
    private RoomStatusPresenter mRoomStatusPresenter;
    private ForbidManagePresenter mForbidManagePresenter;
    protected UserInfoPresenter mUserInfoPresenter;
    private VipUserActionMsgPresenter mVipUserActionMsgPresenter;

    private PhoneStateReceiver mPhoneStateReceiver;
    private ScreenStateReceiver mScreenStateReceiver;
    private NetworkReceiver mNetworkReceiver;

    private RoomSystemMsgPresenter mRoomSystemMsgPresenter;
    private VideoShowPresenter mVideoShowPresenter;

    private ArrayList<RoomInfo> mRoomInfoList;
    private int mRoomInfoPosition;

    protected CustomHandlerThread mHandlerThread = new CustomHandlerThread("WatchActivity") {
        @Override
        protected void processMessage(Message message) {
        }
    };

    Handler mUiHandler = new Handler();

    private BaseWatchFragment mBaseWatchFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (isMIUIV6()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_new_watch_sdk);
        openOrientation();

        if (!initData()) {
            finish();
            return;
        }

        mController = new WatchComponentController(mMyRoomData, mRoomChatMsgManager);
        mController.setupController(this);
        mController.setVerticalList(mRoomInfoList, mRoomInfoPosition);

        mAction.registerAction();

        String tag = null;
        if (mMyRoomData.getLiveType() != LiveManager.TYPE_LIVE_GAME
                && mMyRoomData.getLiveType() != LiveManager.TYPE_LIVE_HUYA) {
            mBaseWatchFragment = new WatchNormalFragment();
            tag = WatchNormalFragment.class.getSimpleName();
            mRoomChatMsgManager.setIsGameLiveMode(false);
        } else {
            mBaseWatchFragment = new WatchGameFragment();
            tag = WatchGameFragment.class.getSimpleName();
            mRoomChatMsgManager.setIsGameLiveMode(true);
        }
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.container, mBaseWatchFragment, tag);
        ft.commitAllowingStateLoss();

        initPresenter();
        initReceiver();

//        //尝试发送关键数据给服务器,允许即使多次调用，成功后就不再发送。
        if (!isMyRoom() && !check4GNet()) {
            WatchRoomCharactorManager.getInstance().clear();
            // 放了防止 UI 还没 注册，事件已经发出
            mUiHandler.post(new Runnable() {
                @Override
                public void run() {
                    trySendDataWithServerOnce();
                }
            });
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
            mController.postEvent(MSG_NEW_VIDEO_URL, new Params().putItem(mMyRoomData.getVideoUrl()));
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

    private boolean initData() {
        Intent data = getIntent();
        if (data == null) {
            ToastUtils.showToast("missing Intent");
            MyLog.e(TAG, "Intent is null");
            return false;
        }

        mRoomInfo = data.getParcelableExtra(EXTRA_ROOM_INFO);
        mRoomInfoList = data.getParcelableArrayListExtra(EXTRA_ROOM_INFO_LIST);
        mRoomInfoPosition = data.getIntExtra(EXTRA_ROOM_INFO_POSITION, 0);
        if (mRoomInfo == null && mRoomInfoList != null) {
            mRoomInfo = mRoomInfoList.get(mRoomInfoPosition);
        }
        if (mRoomInfo == null) {
            ToastUtils.showToast("missing RoomInfo");
            MyLog.e(TAG, "mRoomInfo is null");
            return false;
        }

        // 填充 MyRoomData
        mMyRoomData.setRoomId(mRoomInfo.getLiveId());
        mMyRoomData.setUid(mRoomInfo.getPlayerId());
        mMyRoomData.setVideoUrl(mRoomInfo.getVideoUrl());
        mMyRoomData.setLiveType(mRoomInfo.getLiveType());
        mMyRoomData.setGameId(mRoomInfo.getGameId());
        Boolean enableShare = (Boolean) HostChannelManager.getInstance().get(HostChannelManager.KEY_SHARE_ENABLE);
        if (enableShare == null) {
            enableShare = false;
        }
        mMyRoomData.setEnableShare(enableShare);
        Boolean enableFollow = (Boolean) HostChannelManager.getInstance().get(HostChannelManager.KEY_FOLLOW_ENABLE);
        if (enableFollow == null) {
            enableFollow = false;
        }
        mMyRoomData.setEnableRelationChain(enableFollow);
        mMyRoomData.setChannelId(mRoomInfo.getPageChannelId());
        return true;
    }

    private void initPresenter() {
        mLiveTaskPresenter = new LiveTaskPresenter(this, mWatchView, mMyRoomData);
        addBindActivityLifeCycle(mLiveTaskPresenter, false);

        mGiftPresenter = new GiftPresenter(mRoomChatMsgManager, false);
        addPushProcessor(mGiftPresenter);
        addPresent(mGiftPresenter);

        mRoomTextMsgPresenter = new RoomTextMsgPresenter(mRoomChatMsgManager);
        addPushProcessor(mRoomTextMsgPresenter);
        addPresent(mRoomTextMsgPresenter);

        mRoomManagerPresenter = new RoomManagerPresenter(this, mRoomChatMsgManager, true, mMyRoomData);
        addPushProcessor(mRoomManagerPresenter);
        addPresent(mRoomManagerPresenter);
        mRoomManagerPresenter.syncOwnerInfo(mMyRoomData.getUid(), true); // 拉取一下主播信息，同步观看端是否是管理员


        mGiftMallPresenter = new GiftMallPresenter(this, getBaseContext(), mMyRoomData, mController);
        addBindActivityLifeCycle(mGiftMallPresenter, true);

        mRoomViewerPresenter = new RoomViewerPresenter(mRoomChatMsgManager);
        addPushProcessor(mRoomViewerPresenter);
        addPresent(mRoomViewerPresenter);

        mRoomStatusPresenter = new RoomStatusPresenter(mRoomChatMsgManager);
        addPushProcessor(mRoomStatusPresenter);
        addPresent(mRoomStatusPresenter);

        mRoomSystemMsgPresenter = new RoomSystemMsgPresenter(mRoomChatMsgManager);
        addPushProcessor(mRoomSystemMsgPresenter);
        addPresent(mRoomSystemMsgPresenter);

        mVipUserActionMsgPresenter = new VipUserActionMsgPresenter(mRoomChatMsgManager);
        addPushProcessor(mVipUserActionMsgPresenter);
        addPresent(mVipUserActionMsgPresenter);

        mUserInfoPresenter = new UserInfoPresenter(this, mMyRoomData);

        if (mMyRoomData.getLiveType() == LiveManager.TYPE_LIVE_GAME) {
        }
    }

    private void initReceiver() {
        mPhoneStateReceiver = PhoneStateReceiver.registerReceiver(this);
        mScreenStateReceiver = ScreenStateReceiver.registerReceiver(this);
        mNetworkReceiver = NetworkReceiver.registerReceiver(this);
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
        SelfUpdateManager.selfUpdateAsnc(new WeakReference(this));

        initUploadAliveTime();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMyRoomData != null) {
            uploadAlive(mMyRoomData.getRoomId(), mMyRoomData.getChannelId());
        }
    }

    @Override
    public void finish() {
        KeyboardUtils.hideKeyboardImmediately(this);
        super.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        leaveLiveToServer();
        unregisterReceiver();
        mAction.unregisterAction();
        if (mController != null) {
            mController.release();
            mController = null;
        }

        if (mHandlerThread != null) {
            mHandlerThread.destroy();
        }
        if (mUiHandler != null) {
            mUiHandler.removeCallbacksAndMessages(null);
        }
    }

    private void unregisterReceiver() {
        PhoneStateReceiver.unregisterReceiver(this, mPhoneStateReceiver);
        ScreenStateReceiver.unregisterReceiver(this, mScreenStateReceiver);
        NetworkReceiver.unRegisterReceiver(this, mNetworkReceiver);
    }

    // 直播结束
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LiveEndEvent event) {
        MyLog.d(TAG, "liveEndEvent");
        mController.postEvent(MSG_PLAYER_COMPLETED);
        showEndLiveFragment(true, UserEndLiveFragment.ENTER_TYPE_LIVE_END);
    }

    protected UserEndLiveFragment userEndLiveFragment;

    /**
     * 显示结束页fragment
     *
     * @param failure
     */
    protected void showEndLiveFragment(boolean failure, String type) {
//        //清空room信息
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
                    mMyRoomData.getNickName(), hasRoomList, mMyRoomData.isEnableRelationChain());
        }
    }

    /**
     * 显示个人资料的浮框
     */
    private void startShowFloatPersonInfo(long uid) {
        if (uid <= 0) {
            return;
        }
        FloatInfoFragment.openFragment(this, uid, mMyRoomData.getUid(), mMyRoomData.getRoomId(), mMyRoomData.getVideoUrl(), this, mMyRoomData.getEnterRoomTime(), mMyRoomData.isEnableRelationChain());
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
    public void onEventMainThread(com.wali.live.watchsdk.eventbus.EventClass.H5FirstPayEvent event) {
        MyLog.d(TAG, "H5FirstPayEvent");
        if (event == null) {
            return;
        }

        RechargeDirectPayFragment.openFragment(this, R.id.main_act_container, event.gooid, event.gemCnt, event.giveGemCnt, event.goodPrice, event.payType, event.channel);
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
                        true, isDisplayLandscape(), mMyRoomData.isEnableRelationChain());
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
                            gift.getSendDescribe(), 1, 0, System.currentTimeMillis(), -1, mMyRoomData.getRoomId(), String.valueOf(mMyRoomData.getUid()), "", "", 0,
                            false, 1, "");
                    BarrageMessageManager.getInstance().pretendPushBarrage(pushMsg);
                }
                break;
        }
    }

    @Subscribe
    public void onEvent(SdkEventClass.OrientEvent event) {
        MyLog.d(TAG, "sdk OrientEvent");
        mMyRoomData.setLandscape(event.isLandscape());
        if (event.isLandscape()) {
            if (mBaseWatchFragment != null) {
                mBaseWatchFragment.orientLandscape();
            }
        } else {
            if (mBaseWatchFragment != null) {
                mBaseWatchFragment.orientPortrait();
            }
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
        if (event != null && mMyRoomData.isForeground()) { // 只有APP在前台才进行自动切换到下一个房间
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

            MyLog.d(TAG, "enterLive success roomInfo:" + roomInfo);
            if (roomInfo != null) {
                updateVideoUrl(roomInfo.getDownStreamUrl());
                mMyRoomData.setHuyaInfo(roomInfo.getThirdPartyInfo());
                mMyRoomData.setLiveType(roomInfo.getType());
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

            if (mMyRoomData.getLiveType() == LiveManager.TYPE_LIVE_GAME
                    || mMyRoomData.getLiveType() == LiveManager.TYPE_LIVE_HUYA) {
                // 游戏直播
                trySyncGameInfoModel();
            }
        }
    };


    @Override
    public void onClickTopOne(User user) {
    }

    @Override
    public void onClickMainAvatar(User user) {
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
                            mGiftMallPresenter.setGiftInfoForEnterRoom(giftInfoForEnterRoom.getmGiftInfoForThisRoom());
                            mGiftMallPresenter.setPktGiftId(giftInfoForEnterRoom.getPktGiftId());


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


    void trySyncGameInfoModel() {
        Observable.create(new Observable.OnSubscribe<GameInfoModel>() {
            @Override
            public void call(Subscriber<? super GameInfoModel> subscriber) {
                long gameId = 0;
                if (!TextUtils.isEmpty(mMyRoomData.getGameId())) {
                    try {
                        gameId = Long.parseLong(mMyRoomData.getGameId());
                    } catch (Exception e) {
                        gameId = 0;
                    }
                }

                if (gameId > 0 || !TextUtils.isEmpty(mMyRoomData.getGamePackageName())) {
                    GameInfoModel gameInfoModel = GameCenterDataManager.getGameInfo(gameId, mMyRoomData.getGamePackageName());
                    if (gameInfoModel == null) {
                        subscriber.onError(new Exception("pull data error"));
                        return;
                    } else {
                        subscriber.onNext(gameInfoModel);
                    }
                }
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .compose(this.<GameInfoModel>bindUntilEvent(ActivityEvent.DESTROY))
                .retryWhen(new RxRetryAssist(3, 5, false))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<GameInfoModel>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.d(TAG, e);
                    }

                    @Override
                    public void onNext(GameInfoModel gameInfoModel) {
                        if (gameInfoModel != null) {
                            mMyRoomData.setGameInfoModel(gameInfoModel);

                            WatchGameInfoConfig.update(gameInfoModel.getPackageUrl(),
                                    mMyRoomData.getUid(), mMyRoomData.getChannelId(), gameInfoModel.getPackageName(), gameInfoModel.getGameId());
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
            } else if (mGiftMallPresenter != null && mGiftMallPresenter.isGiftMallViewVisibility()) {
                EventBus.getDefault().post(new GiftEventClass.GiftMallEvent(GiftEventClass.GiftMallEvent.EVENT_TYPE_GIFT_HIDE_MALL_LIST));
                return;
            }
            super.onBackPressed();
        }
    }


    private boolean check4GNet() {
        if (true) {
            return false;
        }
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
        mController.postEvent(MSG_PLAYER_COMPLETED);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.KickEvent event) {
        mController.postEvent(MSG_PLAYER_COMPLETED);
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
        if (!TextUtils.isEmpty(videoUrl) && !videoUrl.equals(mMyRoomData.getVideoUrl())) {
            // 这里用都用最新的url做替换，因为存在虎牙直播这种 url 有签名有效期的
            // 而且虎牙直播卡顿时需要重新拉一下 url
            mMyRoomData.setVideoUrl(videoUrl);
            MyLog.d(TAG, "updateVideoUrl startPlayer");
            mController.postEvent(MSG_NEW_VIDEO_URL, new Params().putItem(videoUrl));
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
                mController.registerObserverForEvent(MSG_FORCE_ROTATE_SCREEN, this);
                mController.registerObserverForEvent(MSG_PAGE_DOWN, this);
                mController.registerObserverForEvent(MSG_PAGE_UP, this);
                mController.registerObserverForEvent(MSG_PLAYER_READY, this);
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
                case MSG_FORCE_ROTATE_SCREEN:
                    if (mLandscape) {
                        tempForcePortrait();
                    } else {
                        tempForceLandscape();
                    }
                    break;
                case MSG_PAGE_UP:
                    MyLog.d(TAG, "page up");
                    mController.switchToNextPosition();
                    switchRoom();
//                    if (mSdkView != null) {
//                        MyLog.d(TAG, "page down internal");
//                        mSdkView.switchToNextRoom();
//                    }
                    if (mBaseWatchFragment != null) {
                        mBaseWatchFragment.pageUpEvent();
                    }
                    break;
                case MSG_PAGE_DOWN:
                    MyLog.d(TAG, "page down");
                    mController.switchToLastPosition();
                    switchRoom();
//                    if (mSdkView != null) {
//                        MyLog.d(TAG, "page up internal");
//                        mSdkView.switchToLastRoom();
//                    }
                    if (mBaseWatchFragment != null) {
                        mBaseWatchFragment.PageDownEvent();
                    }
                    break;
                case MSG_PLAYER_READY:
                    MyLog.d(TAG, "MSG_PLAYER_READY");
                    if (mBaseWatchFragment != null) {
                        mBaseWatchFragment.playerReadyEvent();
                    }
//                    if (mMaskIv.getVisibility() == View.VISIBLE) {
//                        mMaskIv.setVisibility(View.GONE);
//                    }
//                    if (mSdkView != null) {
//                        mSdkView.postPrepare();
//                    }
                    break;
                default:
                    break;
            }
            return false;
        }

        private void switchRoom() {
            if (!isFinishing()) {
                // 切换房间之前将上一个房间的观看时间上传
                if (mMyRoomData != null) {
                    uploadAlive(mMyRoomData.getRoomId(), mMyRoomData.getChannelId());
                }

                MyLog.w(TAG, "switch anchor: leave room user=" + mMyRoomData.getUser());
                // 清除管理信息
                WatchRoomCharactorManager.getInstance().clear();
                // 清除房间消息
                mRoomChatMsgManager.clear();

                // 重置Presenter
                mUserInfoPresenter.reset();
                mLiveTaskPresenter.reset();
                if (mVideoShowPresenter != null) {
                    mVideoShowPresenter.reset();
                }

                mGiftMallPresenter.reset();

                // 发送离开房间给服务器
                leaveLiveToServer();

                if (mBaseWatchFragment != null) {
                    mBaseWatchFragment.switchRoom();
                }

                mController.postEvent(MSG_SWITCH_ROOM);

                // 切换房间后，进入当前房间
                mController.switchRoom();

                // 重新获取当前房间信息，并重新进入房间
                trySendDataWithServerOnce();

                // 切换房间后更新打点时间
                initUploadAliveTime();
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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                mController.postEvent(MSG_PLAYER_SOUND_ON);
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                mController.postEvent(MSG_PLAYER_SOUND_ON);
        }
        return super.onKeyDown(keyCode, event);
    }

    private long mResumeTime;
    private long mPauseTime;

    private void initUploadAliveTime() {
        mResumeTime = System.currentTimeMillis();
        mPauseTime = mResumeTime;
        MyLog.d(TAG, "alive onResume time" + mResumeTime);
    }

    private void uploadAlive(String roomId, long channelId) {
        mPauseTime = System.currentTimeMillis();
        long aliveTime = mPauseTime - mResumeTime;

        MyLog.d(TAG, "alive aliveTime " + aliveTime);
        if (aliveTime > 0 && mResumeTime > 0) {
            MilinkStatistics.getInstance().statisticAlive(MyUserInfoManager.getInstance().getUuid(),
                    aliveTime, roomId, channelId, AliveStatisticItem.ALIVE_BIZ_TYPE_LIVE_ROOM);
        }

        mResumeTime = 0;
        mPauseTime = 0;
    }

    public RoomInfo getRoomInfo() {
        return mRoomInfo;
    }

    public RoomBaseDataModel getRoomBaseData() {
        return mMyRoomData;
    }

    public WatchComponentController getController() {
        return mController;
    }

    @Override
    public GiftMallPresenter getGiftMallPresenter() {
        return mGiftMallPresenter;
    }
}
