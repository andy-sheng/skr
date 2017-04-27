package com.wali.live.livesdk.live;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.dialog.DialogUtils;
import com.base.dialog.MyAlertDialog;
import com.base.event.SdkEventClass;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.FragmentListener;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.image.fresco.BaseImageView;
import com.base.image.fresco.FrescoWorker;
import com.base.image.fresco.image.ImageFactory;
import com.base.image.fresco.processor.BlurPostprocessor;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.base.utils.network.Network;
import com.base.utils.toast.ToastUtils;
import com.google.protobuf.InvalidProtocolBufferException;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.account.HostChannelManager;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.cache.RoomInfoGlobalCache;
import com.mi.live.data.location.Location;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.live.data.push.presenter.RoomMessagePresenter;
import com.mi.live.data.query.model.MessageRule;
import com.mi.live.data.repository.RoomMessageRepository;
import com.mi.live.data.repository.datasource.RoomMessageStore;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.data.user.User;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.common.barrage.manager.BarrageMessageManager;
import com.wali.live.common.flybarrage.view.FlyBarrageViewGroup;
import com.wali.live.common.gift.view.GiftAnimationView;
import com.wali.live.common.gift.view.GiftContinueViewGroup;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.component.BaseSdkView;
import com.wali.live.component.ComponentController;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.event.EventClass;
import com.wali.live.event.UserActionEvent;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.api.ZuidActiveRequest;
import com.wali.live.livesdk.live.api.ZuidSleepRequest;
import com.wali.live.livesdk.live.component.BaseLiveController;
import com.wali.live.livesdk.live.component.data.StreamerPresenter;
import com.wali.live.livesdk.live.eventbus.LiveEventClass;
import com.wali.live.livesdk.live.fragment.BasePrepareLiveFragment;
import com.wali.live.livesdk.live.fragment.EndLiveFragment;
import com.wali.live.livesdk.live.fragment.RoomAdminFragment;
import com.wali.live.livesdk.live.livegame.fragment.PrepareLiveFragment;
import com.wali.live.livesdk.live.presenter.LiveRoomPresenter;
import com.wali.live.livesdk.live.receiver.ScreenStateReceiver;
import com.wali.live.livesdk.live.task.IActionCallBack;
import com.wali.live.livesdk.live.view.CountDownView;
import com.wali.live.livesdk.live.view.topinfo.LiveTopInfoSingleView;
import com.wali.live.livesdk.live.viewmodel.RoomTag;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.proto.LiveMessageProto;
import com.wali.live.proto.LiveProto;
import com.wali.live.receiver.PhoneStateReceiver;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.statistics.StatisticsWorker;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;
import com.wali.live.watchsdk.ipc.service.ShareInfo;
import com.wali.live.watchsdk.personinfo.fragment.FloatPersonInfoFragment;
import com.wali.live.watchsdk.personinfo.presenter.ForbidManagePresenter;
import com.wali.live.watchsdk.ranking.RankingPagerFragment;
import com.wali.live.watchsdk.schema.SchemeActivity;
import com.wali.live.watchsdk.schema.SchemeConstants;
import com.wali.live.watchsdk.watch.presenter.SnsShareHelper;
import com.wali.live.watchsdk.watch.presenter.push.GiftPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomTextMsgPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomViewerPresenter;
import com.wali.live.watchsdk.webview.HalfWebViewActivity;
import com.wali.live.watchsdk.webview.WebViewActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

import static com.wali.live.statistics.StatisticsKey.AC_APP;
import static com.wali.live.statistics.StatisticsKey.KEY;
import static com.wali.live.statistics.StatisticsKey.TIMES;

/**
 * Created by chenyong on 2017/2/8.
 */
public class LiveSdkActivity extends BaseComponentSdkActivity implements FragmentDataListener, IActionCallBack,
        FloatPersonInfoFragment.FloatPersonInfoClickListener, ForbidManagePresenter.IForbidManageProvider {

    private static final String EXTRA_IS_GAME_LIVE = "extra_is_game_live";
    private static final String EXTRA_LOCATION = "extra_location";

    public static final int REQUEST_MEDIA_PROJECTION = 2000;
    public static final int REQUEST_PREPARE_LIVE = 1000;

    private static final int HEARTBEAT_DURATION = 10 * 1000;        // 心跳间隔时间
    private static final int HEARTBEAT_TIMEOUT = 120 * 1000;        // 心跳超时时间
    private static final int BACKGROUND_TIMEOUT = 10 * 60 * 1000;   // 刷新房间信息区间

    private static final int MSG_SHOW_NETWORK_NOT_GOOD_TIPS = 105;  // 显示网络不佳信息
    private static final int MSG_HIDE_NETWORK_NOT_GOOD_TIPS = 106;  // 隐藏网络不佳信息
    private static final int MSG_SHOW_NETWORK_CHANGE_TIPS = 108;    // 显示网络切换信息
    private static final int MSG_HIDE_NETWORK_CHANGE_TIPS = 109;    // 隐藏网络切换信息

    private static final int MSG_END_LIVE = 201;                    // 停止直播
    private static final int MSG_END_LIVE_FOR_TIMEOUT = 202;        // 超时退出房间
    private static final int MSG_HEARTBEAT = 203;                   // 心跳
    private static final int MSG_HEARTBEAT_TIMEOUT = 204;           // 心跳超时
    private static final int MSG_ROOM_NOT_EXIT = 205;               // 房间不存在

    public static boolean sRecording = false; //将其变为静态并暴露给外面，用于各种跳转判定

    private boolean mIsLiveEnd;
    private int mLastTicket;
    private Intent mScreenRecordIntent;
    private String mLiveTitle;
    private String mLiveCoverUrl;
    private RoomTag mRoomTag;
    private int mSnsType = -1;
    //TODO 该标志位用于区分分享类别
    private int mSnsTypeFlag = 1;
    private boolean mIsShare;

    private boolean mIsGameLive;
    private Location mLocation;

    private final MyUIHandler mUIHandler = new MyUIHandler(this);

    private GiftPresenter mGiftPresenter;
    private RoomTextMsgPresenter mRoomTextMsgPresenter;
    private RoomViewerPresenter mRoomViewerPresenter;
    private ForbidManagePresenter mForbidManagePresenter;

    protected BaseImageView mBlurIv; // 高斯蒙层
    protected ImageView mCloseBtn; // 关闭按钮

    protected LiveTopInfoSingleView mTopInfoSingleView;

    protected FlyBarrageViewGroup mFlyBarrageViewGroup;

    protected TextView mTipsTv;

    protected StreamerPresenter mStreamerPresenter;
    protected BaseLiveController mComponentController;
    protected BaseSdkView mSdkView;

    protected RoomMessagePresenter mPullRoomMessagePresenter;
    protected ExecutorService mHeartbeatService;
    protected boolean mIsPaused = false;

    protected GiftContinueViewGroup mGiftContinueViewGroup;
    protected GiftAnimationView mGiftAnimationView; // 礼物特效动画

    protected CountDownView mCountDownView;
    private LiveRoomPresenter mLiveRoomPresenter;
    private TextView mToHomeBtn;

    private PhoneStateReceiver mPhoneStateReceiver = null; //监听电话打入
    private MyAlertDialog mPhoneInterruptDialog;

    private ScreenStateReceiver mScreenStateReceiver; //屏幕状态监听

    private boolean mGenerateHistorySucc;
    private String mGenerateHistoryMsg;
    private MyAlertDialog mTrafficDialog; //流量窗

    @Override
    public boolean isKeyboardResize() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (isMIUIV6()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.livesdk_layout);
        overridePendingTransition(R.anim.slide_in_from_bottom, 0);

        initData();
        initRoomData();

        setupRequiredComponent();

        if (!mIsGameLive) {
            mComponentController.createStreamer(this, $(R.id.galileo_surface_view), 0, false, null);
        }
        mComponentController.enterPreparePage(this, REQUEST_PREPARE_LIVE, this);
        openOrientation();

        // 封面模糊图
        mBlurIv = $(R.id.blur_iv);
        AvatarUtils.loadAvatarByUidTs(mBlurIv, mMyRoomData.getUid(), mMyRoomData.getAvatarTs(),
                AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE, false, true);
        check4GNet();
    }

    private void initData() {
        Intent data = getIntent();
        if (data != null) {
            mIsGameLive = data.getBooleanExtra(EXTRA_IS_GAME_LIVE, false);
            mLocation = data.getParcelableExtra(EXTRA_LOCATION);
        }
    }

    private void initRoomData() {
        if (mLocation != null) {
            mMyRoomData.setCity(mLocation.getCity());
        }
        if (UserAccountManager.getInstance().hasAccount()) {
            mMyRoomData.setUser(MyUserInfoManager.getInstance().getUser());
            mMyRoomData.setUid(UserAccountManager.getInstance().getUuidAsLong());
        }
    }

    private void setupRequiredComponent() {
        mStreamerPresenter = new StreamerPresenter(mMyRoomData);
        if (mIsGameLive) {
            mComponentController = new com.wali.live.livesdk.live.livegame.LiveComponentController(
                    mMyRoomData, mRoomChatMsgManager, mStreamerPresenter);
        } else {
            mComponentController = new com.wali.live.livesdk.live.liveshow.LiveComponentController(
                    mMyRoomData, mRoomChatMsgManager, mStreamerPresenter);
        }
        mStreamerPresenter.setComponentController(mComponentController);
        mSdkView = mComponentController.createSdkView(this);

        addPresent(mStreamerPresenter);

        Action action = new Action();
        mComponentController.registerAction(ComponentController.MSG_END_LIVE_UNEXPECTED, action);
        mComponentController.registerAction(ComponentController.MSG_END_LIVE_FOR_TIMEOUT, action);
        mComponentController.registerAction(ComponentController.MSG_OPEN_MIC_FAILED, action);
        if (!mIsGameLive) {
            mComponentController.registerAction(ComponentController.MSG_OPEN_CAMERA_FAILED, action);
        }
    }

    private void registerScreenStateReceiver() {
        mScreenStateReceiver = new ScreenStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        registerReceiver(mScreenStateReceiver, filter);
    }

    @Override
    public void trySendDataWithServerOnce() {
        if (mMyRoomData.getUid() == 0) {
            mMyRoomData.setUser(MyUserInfoManager.getInstance().getUser());
            mMyRoomData.setUid(UserAccountManager.getInstance().getUuidAsLong());
        }
    }

    @Override
    protected void tryClearData() {
    }

    @Override
    public void finish() {
        MyLog.w(TAG, "finish");
        super.finish();
        overridePendingTransition(R.anim.slide_out_to_bottom, R.anim.slide_out_to_bottom);
    }

    @Subscribe
    public void onEvent(SdkEventClass.BringFrontEvent event) {
        MyLog.d(TAG, "bring front event");
        ActivityManager am = (ActivityManager) getSystemService(Activity.ACTIVITY_SERVICE);
        am.moveTaskToFront(getTaskId(), ActivityManager.MOVE_TASK_WITH_HOME);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.NetWorkChangeEvent event) {
        MyLog.w(TAG, "EventClass.NetWorkChangeEvent");
        if (null != event) {
            check4GNet();
        }
    }

    private boolean check4GNet() {
        if (is4g()) {
            if (mTrafficDialog == null) {
                mTrafficDialog = DialogUtils.showAlertDialog(this, getResources().getString(R.string.warm_prompt),
                        getResources().getString(R.string.network_change_tip), getResources().getString(R.string.i_know));
            }
            if (!mTrafficDialog.isShowing()) {
                mTrafficDialog.show();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isStatusBarDark() {
        return false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        KeyboardUtils.hideKeyboard(this);
        if (mIsShare) {
            if (mSnsType != 0) {
                preLiveShareSNS();
            } else {
                beginLiveToServer();
            }
        }
        if (!mIsGameLive) {
            resumeStream();
        }
        mComponentController.onActivityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!mIsGameLive) {
            pauseStream();
        }
        mComponentController.onActivityPaused();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mComponentController.onActivityStopped();
    }

    private void pauseStream() {
        mIsPaused = true;
        if (!mIsGameLive) {
            mStreamerPresenter.stopPreview();
        }
        mComponentController.onPauseStream();
        if (sRecording && !TextUtils.isEmpty(mMyRoomData.getRoomId())) { // TODO 这里会有耗时，初始化这个pb对象会在static中耗时
            new ZuidSleepRequest(mMyRoomData.getRoomId()).async();
            mUIHandler.removeMessages(MSG_END_LIVE_FOR_TIMEOUT);
            mUIHandler.sendEmptyMessageDelayed(MSG_END_LIVE_FOR_TIMEOUT, BACKGROUND_TIMEOUT);
        }
    }

    private void resumeStream() {
        mIsPaused = false;
        if (!mIsGameLive) {
            mStreamerPresenter.startPreview();
        }
        mComponentController.onResumeStream();
        if (sRecording && !TextUtils.isEmpty(mMyRoomData.getRoomId())) {
            new ZuidActiveRequest(mMyRoomData.getRoomId()).async();
            mUIHandler.removeMessages(MSG_END_LIVE_FOR_TIMEOUT);
        }
    }

    @Override
    protected void onDestroy() {
        if (sRecording) {
            stopRecord("onDestroy");
            sRecording = false;
        }
        super.onDestroy();
        if (mScreenStateReceiver != null) {
            unregisterReceiver(mScreenStateReceiver);
        }
        PhoneStateReceiver.unregisterReceiver(this, mPhoneStateReceiver);
        if (mComponentController != null) {
            mComponentController.release();
            mComponentController = null;
        }
        if (mSdkView != null) {
            mSdkView.releaseSdkView();
            mSdkView = null;
        }
        MyUserInfoManager.getInstance().getUser().setRoomId("");
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
        if (mTopInfoSingleView != null) {
            mTopInfoSingleView.onScreenOrientationChanged(true);
        }
        if (mComponentController != null) {
            mComponentController.onEvent(BaseLiveController.MSG_ON_ORIENT_LANDSCAPE);
        }
        if (mGiftContinueViewGroup != null) {
            mGiftContinueViewGroup.setOrient(true);
        }
        orientCloseBtn(true);
    }

    protected void orientPortrait() {
        if (mTopInfoSingleView != null) {
            mTopInfoSingleView.onScreenOrientationChanged(false);
        }
        if (mComponentController != null) {
            mComponentController.onEvent(BaseLiveController.MSG_ON_ORIENT_PORTRAIT);
        }
        if (mGiftContinueViewGroup != null) {
            mGiftContinueViewGroup.setOrient(false);
        }
        orientCloseBtn(false);
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

    /**
     * 显示个人资料的浮框
     */
    private void startShowFloatPersonInfo(long uid) {
        if (uid <= 0) {
            return;
        }
        StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP, StatisticsKey.KEY_USERINFO_CARD_OPEN, 1);
        FloatPersonInfoFragment.openFragment(this, uid, mMyRoomData.getUid(),
                mMyRoomData.getRoomId(), mMyRoomData.getVideoUrl(), this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MyLog.w(TAG, "onActivityResult " + requestCode + " resultCode=" + resultCode + "data =" + data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_MEDIA_PROJECTION:
                    mScreenRecordIntent = data;
                    if (mToHomeBtn == null) {
                        ViewStub stub = (ViewStub) findViewById(R.id.game_live_home_btn);
                        mToHomeBtn = (TextView) stub.inflate();
                        mToHomeBtn.setTranslationY(DisplayUtils.dip2px(22));
                        mToHomeBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                toHome();
                            }
                        });

                        stub = $(R.id.game_live_home_tv);
                        View view = stub.inflate();
                        view.setTranslationY(DisplayUtils.dip2px(-22));
                    }
                    EventBus.getDefault().post(new LiveEventClass.HidePrepareGameLiveEvent());
                    break;
                default:
                    break;
            }
        } else if (requestCode == REQUEST_MEDIA_PROJECTION) {
            ToastUtils.showToast(R.string.forbid_capture);
        }
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle) {
        if (resultCode != Activity.RESULT_OK) {
            MyLog.w(TAG, "resultCode != Activity.RESULT_OK");
            return;
        }
        switch (requestCode) {
            case REQUEST_PREPARE_LIVE:
                initPrepareData(bundle);
                postPrepare();
                if (mSnsType != 0) {
                    getRoomIdToServer();
                } else {
                    beginLiveToServer();
                }
                break;
            default:
                break;
        }
    }

    private void toHome() {
        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addCategory(Intent.CATEGORY_HOME);
        GlobalData.app().startActivity(i);
        StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                String.format(StatisticsKey.KEY_LIVESDK_PLUG_FLOW_CLICK_GODESK, HostChannelManager.getInstance().getChannelId()),
                TIMES, "1");
    }

    private void initPrepareData(Bundle bundle) {
        mLiveTitle = bundle.getString(BasePrepareLiveFragment.EXTRA_LIVE_TITLE);
        mRoomTag = (RoomTag) bundle.getSerializable(BasePrepareLiveFragment.EXTRA_LIVE_TAG_INFO);
        mLiveCoverUrl = bundle.getString(BasePrepareLiveFragment.EXTRA_LIVE_COVER_URL, "");
        mSnsType = bundle.getInt(BasePrepareLiveFragment.EXTRA_SNS_TYPE, 0);

        mMyRoomData.setLiveTitle(mLiveTitle);
        mLiveRoomPresenter = new LiveRoomPresenter(this);
        addPresent(mLiveRoomPresenter);
        mRoomTextMsgPresenter = new RoomTextMsgPresenter(mRoomChatMsgManager);
        addPresent(mRoomTextMsgPresenter);
        mGiftPresenter = new GiftPresenter(mRoomChatMsgManager, false);
        addPresent(mGiftPresenter);

        if (mIsGameLive) {
            int quality = bundle.getInt(PrepareLiveFragment.EXTRA_GAME_LIVE_QUALITY, PrepareLiveFragment.MEDIUM_CLARITY);
            boolean isMute = bundle.getBoolean(PrepareLiveFragment.EXTRA_GAME_LIVE_MUTE, false);
            mComponentController.createStreamer(this, null, quality, isMute, mScreenRecordIntent);
        }
    }

    private void startCountDown() {
        MyLog.w(TAG, "startCountDown");
        mCountDownView.startCountDown();
    }

    private void postPrepare() {
        // 注册监听
        mPhoneStateReceiver = PhoneStateReceiver.registerReceiver(this);
        registerScreenStateReceiver();

        if (mMyRoomData.getUser() == null || mMyRoomData.getUser().getUid() <= 0 || TextUtils.isEmpty(mMyRoomData.getUser().getNickname())) {
            mMyRoomData.setUser(MyUserInfoManager.getInstance().getUser());
        }
        // 顶部view
        mTopInfoSingleView = $(R.id.live_top_info_view);
        addBindActivityLifeCycle(mTopInfoSingleView, true);
        mTopInfoSingleView.setMyRoomDataSet(mMyRoomData);
        mTopInfoSingleView.initViewUseData();
        mTopInfoSingleView.setVisibility(View.VISIBLE);

        // 礼物
        mGiftContinueViewGroup = $(R.id.gift_continue_vg);
        addBindActivityLifeCycle(mGiftContinueViewGroup, true);
        initGiftRoomEffectView();
        mGiftAnimationView = $(R.id.gift_animation_player_view);
        addBindActivityLifeCycle(mGiftAnimationView, true);

        //关闭按钮
        mCloseBtn = $(R.id.close_btn);
        mCloseBtn.setVisibility(View.VISIBLE);
        RxView.clicks(mCloseBtn)
                .throttleFirst(200, TimeUnit.MILLISECONDS)
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        showStopDialog();
                        StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                                String.format(StatisticsKey.KEY_LIVESDK_PLUG_FLOW_CLICK_CLOSE, HostChannelManager.getInstance().getChannelId()),
                                TIMES, "1");
                    }
                });
        orientCloseBtn(isDisplayLandscape());

        mGiftPresenter = new GiftPresenter(mRoomChatMsgManager, false);
        addPushProcessor(mGiftPresenter);
        mRoomTextMsgPresenter = new RoomTextMsgPresenter(mRoomChatMsgManager);
        addPushProcessor(mRoomTextMsgPresenter);
        mRoomViewerPresenter = new RoomViewerPresenter(mRoomChatMsgManager);
        addPushProcessor(mRoomViewerPresenter);

        mTipsTv = $(R.id.tips_tv);

        mSdkView.setupSdkView();

        mFlyBarrageViewGroup = $(R.id.fly_barrage_viewgroup);
        addBindActivityLifeCycle(mFlyBarrageViewGroup, true);

        mCountDownView = $(R.id.count_down_view);

        mLastTicket = mMyRoomData.getTicket();

        // 用根View的点击事件代替mTouchDelegateView
        $(R.id.main_act_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mComponentController != null) {
                    mComponentController.onEvent(BaseLiveController.MSG_HIDE_INPUT_VIEW);
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LiveEventClass.SystemEvent event) {
        if (event != null && mComponentController != null) {
            MyLog.w(TAG, "onEventMainThread PhoneStateEvent");
            switch (event.type) {
                case LiveEventClass.SystemEvent.EVENT_TYPE_ACTION_PHONE_STATE_CHANGED_IDLE:
                    resumeStream();
                    break;
                case LiveEventClass.SystemEvent.EVENT_TYPE_ACTION_PHONE_STATE_CHANGED_RING:
                    if (mPhoneInterruptDialog == null || !mPhoneInterruptDialog.isShowing()) {
                        mPhoneInterruptDialog = DialogUtils.showAlertDialog(this, getString(R.string.warm_prompt), getString(R.string.phone_interrupt), getString(R.string.i_know));
                    }
                    pauseStream();
                    break;
                case LiveEventClass.SystemEvent.EVENT_TYPE_ACTION_NEW_OUTGOING_CALL:
                    pauseStream();
                    break;
                default:
                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LiveEventClass.ScreenStateEvent event) {
        if (event != null && mComponentController != null) {
            MyLog.w(TAG, "onEvent ScreenStateEvent state=" + event.screenState);
            switch (event.screenState) {
                case LiveEventClass.ScreenStateEvent.ACTION_SCREEN_OFF:
                    pauseStream();
                    break;
                case LiveEventClass.ScreenStateEvent.ACTION_SCREEN_ON:
                    resumeStream();
                    break;
                default:
                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(SdkEventClass.OrientEvent event) {
        MyLog.w(TAG, "OrientEvent");
        if (event != null) {
            if (event.isLandscape()) {
                orientLandscape();
            } else {
                orientPortrait();
            }

            if (mStreamerPresenter != null) {
                if (!mIsGameLive) {
                    mStreamerPresenter.setAngle(event.orientation);
                }
            }
        }
    }

    @Override
    protected String getTAG() {
        return "LiveSdkActivity" + "@" + this.hashCode();
    }

    @Override
    public void processAction(String action, int errCode, Object... objects) {
        MyLog.w(TAG, "processAction : " + action + " , errCode : " + errCode);
        if (!isFinishing()) {
            switch (action) {
                case MiLinkCommand.COMMAND_LIVE_GET_ROOM_ID:
                    processGetRoomId(errCode, objects);
                    break;
                case MiLinkCommand.COMMAND_LIVE_BEGIN:
                    processBeginLive(errCode, objects);
                    break;
                case MiLinkCommand.COMMAND_LIVE_END:
                    processEndLive(errCode, objects);
                    break;
                case MiLinkCommand.COMMAND_LIVE_VIEWER_TOP:
                    processViewerTop(errCode, objects);
                    break;
                case MiLinkCommand.COMMAND_LIVE_VIEWERINFO:
                    processViewerInfo(errCode, objects);
//                    managerIsInRoomInfoFromServer(mMyRoomData);
                    break;
                case MiLinkCommand.COMMAND_GET_USER_INFO_BY_ID:
                    processOwnerInfo(errCode, objects);
                    break;
//                case MiLinkCommand.COMMAND_LIVE_ISINROOM:
//                    processManagerIsInRoom(errCode, objects);
//                    break;
                case MiLinkCommand.COMMAND_LIVE_ROOM_INFO:
                    processRoomInfo(errCode, objects);
                    break;
                default:
                    break;
            }
        }
    }

    private void processGetRoomId(int errCode, Object... objects) {
        switch (errCode) {
            case ErrorCode.CODE_SUCCESS:
            case ErrorCode.CODE_ZUID_CERTIFY_ERROR:
            case ErrorCode.CODE_ZUID_NOT_ADULT:
            case ErrorCode.CODE_ZUID_CERTIFY_GOING:
                processRoomIdInfo((String) objects[0], (String) objects[1], (List<LiveCommonProto.UpStreamUrl>) objects[2], (String) objects[3]);
                break;
            default:
                processPreLive();
                break;
        }
    }

    private void processBeginLive(int errCode, Object... objects) {
        switch (errCode) {
            case ErrorCode.CODE_SUCCESS:
            case ErrorCode.CODE_ZUID_CERTIFY_ERROR:
            case ErrorCode.CODE_ZUID_NOT_ADULT:
            case ErrorCode.CODE_ZUID_CERTIFY_GOING:
                processStartRecord((String) objects[0], (long) objects[1], (String) objects[2],
                        (List<LiveCommonProto.UpStreamUrl>) objects[3], (String) objects[4]);
                break;
            default:
                ToastUtils.showToast(GlobalData.app(), R.string.live_failure);
                EndLiveFragment.openFragmentWithFailure(this, R.id.main_act_container, mMyRoomData.getUid(), mMyRoomData.getRoomId(),
                        mMyRoomData.getAvatarTs(), mMyRoomData.getViewerCnt(), LiveManager.TYPE_LIVE_GAME, mMyRoomData.getTicket(), mShareUrl, mMyRoomData.getCity(), mMyRoomData.getUser(), mLiveCoverUrl, mLiveTitle);
                break;
        }
    }

    private void processEndLive(int errCode, Object... objects) {
        switch (errCode) {
            case ErrorCode.CODE_SUCCESS:
                // 这里返回的观看人数的历史进入房间的观看人数
                int viewerCnt = (int) objects[0];
                if (viewerCnt > 0) { // 被踢时由于房间不存在，viewerCnt为0，此时使用本地数据
                    mMyRoomData.setViewerCnt(viewerCnt);
                }
                mGenerateHistorySucc = (boolean) objects[1];
                mGenerateHistoryMsg = (String) objects[2];
                RoomInfoGlobalCache.getsInstance().leaveCurrentRoom(mMyRoomData.getRoomId());
                mUIHandler.removeMessages(MSG_END_LIVE);
                enterEndFragment();
                break;
            default:
                MyLog.w(TAG, "endLiveToServer errCode = " + errCode);
                mGenerateHistorySucc = true;
                mUIHandler.removeMessages(MSG_END_LIVE);
                enterEndFragment();
                break;
        }
    }

    private void processViewerTop(int errCode, Object... objects) {
        switch (errCode) {
            case ErrorCode.CODE_SUCCESS:
                RoomBaseDataModel roomData = (RoomBaseDataModel) objects[0];
                roomData.getViewersList().clear();
                roomData.getViewersList().addAll((List) objects[1]);
                roomData.notifyViewersChange("processViewerTop");
                break;
        }
    }

    private void processViewerInfo(int errCode, Object... objects) {
        RoomBaseDataModel roomData = (RoomBaseDataModel) objects[0];
        switch (errCode) {
            case ErrorCode.CODE_SUCCESS:
                roomData.setViewerCnt((int) objects[1]);
                roomData.getViewersList().clear();
                roomData.getViewersList().addAll((List) objects[2]);
                roomData.notifyViewersChange("processViewerInfo");
                mLocation = (Location) objects[3];
                break;
            default:
                break;
        }
    }

    private void processOwnerInfo(int errCode, Object... objects) {
        RoomBaseDataModel roomData = (RoomBaseDataModel) objects[0];
        switch (errCode) {
            case ErrorCode.CODE_SUCCESS:
                String roomId = roomData.getRoomId();
                roomData.setUser((User) objects[1]);
                // 如果得到的房间id是null，用老的
                if (TextUtils.isEmpty(roomData.getRoomId())) {
                    roomData.setRoomId(roomId);
                }
                break;
            default:
                break;
        }
    }

    private void processRoomInfo(int errCode, Object... objects) {
        switch (errCode) {
            case ErrorCode.CODE_SUCCESS:
                // 用于观看端直播中获取服务器的拉流地址
                break;
            case ErrorCode.CODE_ROOM_NOT_EXIST:
                // 这个标记是指直播是否结束
                try {
                    EndLiveFragment.openFragmentWithFailure(this, R.id.main_act_container, mMyRoomData.getUid(), mMyRoomData.getRoomId(),
                            mMyRoomData.getAvatarTs(), 0, LiveManager.TYPE_LIVE_GAME, 0, "", mMyRoomData.getCity(), mMyRoomData.getUser(), null, null);
                } catch (Exception e) {
                    MyLog.e(TAG + "process room info" + e);
                }
                break;
            default:
                break;
        }
    }

    private boolean judgeNetwork() {
        if (!Network.hasNetwork(this)) {
            ToastUtils.showToast(this, R.string.network_offline_warning);
            return false;
        }
        return true;
    }

    private void processPreLive() {
        if (!judgeNetwork()) {
            return;
        }
        if (mSnsType != 0 && !TextUtils.isEmpty(mShareUrl)) {
            mIsShare = true;
            preLiveShareSNS();
        } else {
            beginLiveToServer();
        }
    }

    private int getShareType(int snsTypeFlag) {
        switch (snsTypeFlag) {
            case PrepareLiveFragment.WEI_XIN:
                return ShareInfo.TYPE_WECHAT;
            case PrepareLiveFragment.MOMENT:
                return ShareInfo.TYPE_MOMENT;
            case PrepareLiveFragment.QQ:
                return ShareInfo.TYPE_QQ;
            case PrepareLiveFragment.QZONE:
                return ShareInfo.TYPE_QZONE;
            case PrepareLiveFragment.WEIBO:
                return ShareInfo.TYPE_WEIBO;
            case PrepareLiveFragment.FACEBOOK:
                return ShareInfo.TYPE_FACEBOOK;
            case PrepareLiveFragment.TWITTER:
                return ShareInfo.TYPE_TWITTER;
            case PrepareLiveFragment.INSTAGRAM:
                return ShareInfo.TYPE_INSTAGRAM;
            case PrepareLiveFragment.WHATSAPP:
                return ShareInfo.TYPE_WHATSAPP;
            case PrepareLiveFragment.MILIAO:
                return ShareInfo.TYPE_MILIAO;
            case PrepareLiveFragment.MILIAO_FEEDS:
                return ShareInfo.TYPE_MILIAO_FEEDS;
            default:
                break;
        }
        return -1;
    }

    private void preLiveShareSNS() {
        MyLog.w(TAG, "preLiveShareSNS mSnsType=" + mSnsType);
        while ((mSnsType & mSnsTypeFlag) == 0) {
            MyLog.w(TAG, "mSnsType = " + mSnsType + " mSnsTypeFlag=" + mSnsTypeFlag);
            mSnsTypeFlag <<= 1;
        }
        //分享
        SnsShareHelper.getInstance().shareToSns(getShareType(mSnsTypeFlag), mShareUrl, mMyRoomData);
        mSnsType &= (~mSnsTypeFlag);
        mSnsTypeFlag <<= 1;
        MyLog.w(TAG, "mCountDownView state" + mCountDownView.getVisibility());
    }

    private void getRoomIdToServer() {
        mLiveRoomPresenter.getRoomIdByAppInfo(null);
    }

    //开始动画,并且开始推流
    private void beginLiveToServer() {
        MyLog.w(TAG, "beginLiveToServer");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        startCountDown();
        mLiveRoomPresenter.beginLiveByAppInfo(mLocation, mIsGameLive ? LiveManager.TYPE_LIVE_GAME : LiveManager.TYPE_LIVE_PUBLIC, null, true, mLiveTitle,
                mLiveCoverUrl, "", null, 0, mRoomTag, MiLinkConstant.MY_APP_TYPE, true);
    }

    private void processRoomIdInfo(String liveId, String shareUrl, List<LiveCommonProto.UpStreamUrl> upStreamUrlList, String udpUpstreamUrl) {
        mMyRoomData.setRoomId(liveId);
        mShareUrl = shareUrl;
        mStreamerPresenter.setOriginalStreamUrl(upStreamUrlList, udpUpstreamUrl);
        processPreLive();
    }

    private void processStartRecord(String liveId, long createTime, String shareUrl,
                                    List<LiveCommonProto.UpStreamUrl> upStreamUrlList, String udpUpstreamUrl) {
        MyLog.w(TAG, "processStartRecord,liveId:" + liveId);
        mMyRoomData.setRoomId(liveId);
        mShareUrl = shareUrl;
        RoomInfoGlobalCache.getsInstance().enterCurrentRoom(mMyRoomData.getRoomId());
        mStreamerPresenter.setOriginalStreamUrl(upStreamUrlList, udpUpstreamUrl);
        startRecord();
        syncSystemMessage();
        if (mComponentController != null) {
            mComponentController.onEvent(BaseLiveController.MSG_ON_LIVE_SUCCESS);
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

    private void startRecord() {
        MyLog.w(TAG, "startRecord sRecording=" + sRecording);
        if (!sRecording) {
            sRecording = true;
            if (mHeartbeatService == null) {
                mHeartbeatService = Executors.newSingleThreadExecutor();
            }
            mUIHandler.sendEmptyMessage(MSG_HEARTBEAT);
            mUIHandler.sendEmptyMessageDelayed(MSG_HEARTBEAT_TIMEOUT, HEARTBEAT_TIMEOUT);
            if (mPullRoomMessagePresenter == null) {
                mPullRoomMessagePresenter = new RoomMessagePresenter(
                        mMyRoomData, new RoomMessageRepository(new RoomMessageStore()), this);
            }
            mPullRoomMessagePresenter.startWork();
            mStreamerPresenter.startLive();
            mComponentController.onStartLive();
        }
    }

    private void stopRecord(String reason) {
        MyLog.w(TAG, "stopRecord = " + sRecording + ",from:" + reason);
        // 如果正在进行voip通话
        if (sRecording) {
            sRecording = false;
            mUIHandler.removeMessages(MSG_HEARTBEAT);
            mUIHandler.removeMessages(MSG_HEARTBEAT_TIMEOUT);
            if (mHeartbeatService != null) {
                mHeartbeatService.shutdownNow();
                mHeartbeatService = null;
            }
            if (mPullRoomMessagePresenter != null) {
                mPullRoomMessagePresenter.stopWork();
                mPullRoomMessagePresenter.destroy();
                mPullRoomMessagePresenter = null;
            }
            mStreamerPresenter.stopLive();
            mComponentController.onStopLive();
            endLiveToServer();
            mUIHandler.removeCallbacksAndMessages(null);
            // 防止服务器返回太慢,超时1s
            mUIHandler.sendEmptyMessageDelayed(MSG_END_LIVE, 1000);
        } else {
            enterEndFragment();
        }
    }

    protected void sendHeartBeat() {
        if (mHeartbeatService == null) {
            return;
        }
        mHeartbeatService.execute(new Runnable() {
            @Override
            public void run() {
                LiveProto.HeartBeatReq.Builder builder = LiveProto.HeartBeatReq.newBuilder()
                        .setLiveId(mMyRoomData.getRoomId())
                        .setStatus((mIsGameLive || mIsForeground) && !mIsPaused ? 0 : 1);
                builder.setMicuidStatus(0);
                PacketData data = new PacketData();
                data.setCommand(MiLinkCommand.COMMAND_LIVE_HB);
                data.setData(builder.build().toByteArray());
                MyLog.v(TAG, "LiveHeartbeat request : \n" + builder.toString());
                PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
                if (rspData == null) {
                    return;
                }
                try {
                    LiveProto.HeartBeatRsp rsp = LiveProto.HeartBeatRsp.parseFrom(rspData.getData());
                    MyLog.w(TAG, "LiveHeartbeat response : \n" + rsp.toString());
                    switch (rsp.getRetCode()) {
                        case ErrorCode.CODE_SUCCESS:
                            mUIHandler.removeMessages(MSG_HEARTBEAT_TIMEOUT);
                            mUIHandler.sendEmptyMessageDelayed(MSG_HEARTBEAT_TIMEOUT, HEARTBEAT_TIMEOUT);
                            break;
                        case ErrorCode.CODE_ROOM_NOT_EXIST:
                            mUIHandler.sendEmptyMessage(MSG_ROOM_NOT_EXIT);
                            break;
                        default:
                            break;
                    }
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mComponentController != null && mComponentController.onEvent(
                BaseLiveController.MSG_ON_BACK_PRESSED)) {
            return;
        }
        processBack(true);
    }

    private void processBack(boolean isBackPressed) {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            //退出栈弹出
            String fName = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName();
            if (!TextUtils.isEmpty(fName)) {
                Fragment fragment = fm.findFragmentByTag(fName);
                if (null != fragment && fragment instanceof FragmentListener) {
                    //特定的返回事件处理
                    if (((FragmentListener) fragment).onBackPressed()) {
                        return;
                    }
                }
                try {
                    FragmentNaviUtils.popFragmentFromStack(this);
                } catch (Exception e) {
                    MyLog.e(e);
                }
            }
        } else {
            if (isBackPressed) {
                showStopDialog();
            }
        }
    }

    private void endLiveToServer() {
        MyLog.w(TAG, "endLiveToServer");
        mLiveRoomPresenter.endLiveByAppInfo(mMyRoomData.getRoomId(), null);
    }

    private void enterEndFragment() {
        if (mIsLiveEnd) {
            return;
        }
        mIsLiveEnd = true;
        Bundle bundle = EndLiveFragment.getBundle(mMyRoomData.getUid(), mMyRoomData.getRoomId(), mMyRoomData.getAvatarTs(), mMyRoomData.getViewerCnt(), mMyRoomData.getLiveType(), mMyRoomData.getTicket() - mLastTicket, mShareUrl,
                (mLocation == null) ? "" : mLocation.getCity(), mMyRoomData.getUser(), mMyRoomData.getCoverUrl(), mMyRoomData.getLiveTitle());
        bundle.putBoolean(EndLiveFragment.EXTRA_GENERATE_HISTORY, mGenerateHistorySucc);
        bundle.putString(EndLiveFragment.EXTRA_GENERATE_HISTORY_MSG, mGenerateHistoryMsg);
        EndLiveFragment.openFragment(LiveSdkActivity.this, bundle);
    }

    private void showStopDialog() {
        DialogUtils.showNormalDialog(this, 0, R.string.stop_live_dialog_message, R.string.ok, R.string.cancel, new DialogUtils.IDialogCallback() {
            @Override
            public void process(DialogInterface dialogInterface, int i) {
                stopRecord("showStopDialog");
            }
        }, null);
    }

    private void showInfoTips(String tips) {
        mTipsTv.setText(tips);
        mTipsTv.setVisibility(View.VISIBLE);
    }

    private void hideInfoTips(String tips) {
        if (tips.equals(mTipsTv.getText().toString())) {
            mTipsTv.setVisibility(View.GONE);
        }
    }

    private void endLiveUnexpected(int resId) {
        ToastUtils.showToast(GlobalData.app(), resId);
        stopRecord("endLiveUnexpected");
    }

    @Override
    public ForbidManagePresenter provideForbidManagePresenter() {
        if (mForbidManagePresenter == null) {
            mForbidManagePresenter = new ForbidManagePresenter(this);
        }
        return mForbidManagePresenter;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final LiveEventClass.LiveCoverEvent event) {
        if (event != null && mBlurIv.getVisibility() == View.VISIBLE) {
            FrescoWorker.loadImage(mBlurIv,
                    ImageFactory.newHttpImage(event.url)
                            .setPostprocessor(new BlurPostprocessor())
                            .build());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(UserActionEvent event) {
        MyLog.e(TAG, "LiveEventClass.UserActionEvent");
        if (!mIsForeground) {
            return;
        }
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
            case UserActionEvent.EVENT_TYPE_REQUEST_SET_MANAGER: {
                Bundle bundle = new Bundle();
                bundle.putString(RoomAdminFragment.INTENT_LIVE_ROOM_ID, mMyRoomData.getRoomId());
                bundle.putSerializable(RoomAdminFragment.KEY_ROOM_SEND_MSG_CONFIG, mMyRoomData.getMsgRule() == null ? new MessageRule() : mMyRoomData.getMsgRule());
                bundle.putLong(RoomAdminFragment.KEY_ROOM_ANCHOR_ID, mMyRoomData == null ? 0 : mMyRoomData.getUid());
                FragmentNaviUtils.addFragment(LiveSdkActivity.this, R.id.main_act_container, RoomAdminFragment.class, bundle, true, true, true);
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
                        String type = uri.getQueryParameter(SchemeConstants.PARAMETER_SHOP_TYPE);
                        String showType = uri.getQueryParameter(SchemeConstants.PARAMETER_SHOP_SHOW_TYPE);

                        MyLog.d(TAG, "type=" + type + ", showType=" + showType);
                        if (type == null || showType == null) {
                            SchemeActivity.openActivity(this, uri);
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
                        intent = new Intent(LiveSdkActivity.this, HalfWebViewActivity.class);
                        intent.putExtra(WebViewActivity.EXTRA_DISPLAY_TYPE, true);
                    } else {
                        intent = new Intent(LiveSdkActivity.this, WebViewActivity.class);
                        intent.putExtra(WebViewActivity.EXTRA_DISPLAY_TYPE, false);
                    }
                    intent.putExtra(WebViewActivity.EXTRA_URL, scheme);
                    intent.putExtra(WebViewActivity.EXTRA_ZUID, (Long) event.obj4);
                    startActivity(intent);
                }
            }
            break;
            default:
                break;
        }
    }

    /**
     * 接收（更新房间内发言频率、是否重复）设置成功事件
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LiveEventClass.UpdateMsgRuleEvent event) {
        if (event != null && mMyRoomData.getRoomId().equals(event.getRoomId())) {
            if (event.isUpdated()) {
                mMyRoomData.setmMsgRule(event.getMsgRule());
                ToastUtils.showToast(GlobalData.app().getApplicationContext(), getString(R.string.change_room_setting_success));
            } else {
                ToastUtils.showToast(GlobalData.app().getApplicationContext(), getString(R.string.change_room_setting_fail));
            }
            MyLog.w(TAG, "recevie UpdateMsgRuleEvent:" + event.toString());
        }
    }

    private static class MyUIHandler extends Handler {
        private final WeakReference<LiveSdkActivity> mActivity;
        private String TAG = "LiveSdkActivity";

        public MyUIHandler(LiveSdkActivity activity) {
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            final LiveSdkActivity activity = mActivity.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }
            if (msg.what == MSG_END_LIVE) {
                MyLog.v(TAG, "MSG_END_LIVE");
                activity.enterEndFragment();
            }
            if (!sRecording) {
                return;
            }
            switch (msg.what) {
                case MSG_SHOW_NETWORK_NOT_GOOD_TIPS:
                    MyLog.v(TAG, "MSG_SHOW_TIPS");
                    activity.showInfoTips(activity.getString(R.string.network_not_good_tip));
                    break;
                case MSG_HIDE_NETWORK_NOT_GOOD_TIPS:
                    MyLog.v(TAG, "MSG_HIDE_TIPS");
                    activity.hideInfoTips(activity.getString(R.string.network_not_good_tip));
                    break;
                case MSG_SHOW_NETWORK_CHANGE_TIPS:
                    MyLog.v(TAG, "MSG_SHOW_TIPS");
                    activity.showInfoTips(activity.getString(R.string.network_change_tip));
                    break;
                case MSG_HIDE_NETWORK_CHANGE_TIPS:
                    MyLog.v(TAG, "MSG_HIDE_TIPS");
                    activity.hideInfoTips(activity.getString(R.string.network_change_tip));
                    break;
                case MSG_END_LIVE_FOR_TIMEOUT:
                    MyLog.w(TAG, "MSG_END_LIVE_FOR_TIMEOUT");
                    activity.stopRecord("MSG_END_LIVE_FOR_TIMEOUT");
                    break;
                case MSG_HEARTBEAT:
                    MyLog.w(TAG, "MSG_HEARTBEAT");
                    activity.sendHeartBeat();
                    removeMessages(MSG_HEARTBEAT);
                    sendEmptyMessageDelayed(MSG_HEARTBEAT, HEARTBEAT_DURATION);
                    break;
                case MSG_HEARTBEAT_TIMEOUT:
                    MyLog.w(TAG, "MSG_HEARTBEAT_TIMEOUT");
                    activity.endLiveUnexpected(R.string.network_offline_warning);
                    break;
                default:
                    break;
            }
        }
    }

    private class Action implements ComponentPresenter.IAction {
        @Override
        public boolean onAction(int source, @Nullable ComponentPresenter.Params params) {
            switch (source) {
                case ComponentController.MSG_END_LIVE_UNEXPECTED: {
                    int resId = params.getItem(0);
                    endLiveUnexpected(resId);
                    return true;
                }
                case ComponentController.MSG_OPEN_CAMERA_FAILED:
                    DialogUtils.showAlertDialog(LiveSdkActivity.this, getString(R.string.setting_dialog_black_title),
                            getString(R.string.camera_occupy), getString(R.string.ok));
                    return true;
                case ComponentController.MSG_OPEN_MIC_FAILED:
                    DialogUtils.showAlertDialog(LiveSdkActivity.this, getString(R.string.setting_dialog_black_title),
                            getString(R.string.mic_occupy), getString(R.string.ok));
                    return true;
                case ComponentController.MSG_ON_STREAM_SUCCESS:
                    mUIHandler.removeMessages(MSG_HIDE_NETWORK_NOT_GOOD_TIPS);
                    mUIHandler.sendEmptyMessage(MSG_HIDE_NETWORK_NOT_GOOD_TIPS);
                    return true;
                case ComponentController.MSG_ON_STREAM_RECONNECT:
                    mUIHandler.sendEmptyMessage(MSG_SHOW_NETWORK_NOT_GOOD_TIPS);
                    mUIHandler.removeMessages(MSG_HIDE_NETWORK_NOT_GOOD_TIPS);
                    return true;
                default:
                    break;
            }
            return false;
        }
    }

    /**
     * 秀场直播
     */
    public static void openActivity(Activity activity, Location location) {
        openActivity(activity, location, false);
    }

    /**
     * 区分是否是游戏直播还是秀场直播
     */
    public static void openActivity(Activity activity, Location location, boolean isGameLive) {
        Intent intent = new Intent(activity, LiveSdkActivity.class);
        if (location != null) {
            intent.putExtra(EXTRA_LOCATION, location);
        }
        intent.putExtra(EXTRA_IS_GAME_LIVE, isGameLive);
        activity.startActivity(intent);
    }
}
