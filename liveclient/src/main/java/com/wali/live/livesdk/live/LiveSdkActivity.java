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
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.account.channel.HostChannelManager;
import com.mi.live.data.account.event.UserInfoEvent;
import com.mi.live.data.api.ErrorCode;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.cache.RoomInfoGlobalCache;
import com.mi.live.data.location.Location;
import com.mi.live.data.manager.LiveRoomCharacterManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.push.presenter.RoomMessagePresenter;
import com.mi.live.data.query.model.MessageRule;
import com.mi.live.data.repository.GiftRepository;
import com.mi.live.data.repository.RoomMessageRepository;
import com.mi.live.data.repository.datasource.RoomMessageStore;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.data.user.User;
import com.mi.milink.sdk.aidl.PacketData;
import com.mi.milink.sdk.base.CustomHandlerThread;
import com.thornbirds.component.IEventObserver;
import com.thornbirds.component.IParams;
import com.wali.live.common.barrage.manager.BarrageMessageManager;
import com.wali.live.common.flybarrage.view.FlyBarrageViewGroup;
import com.wali.live.common.gift.view.GiftAnimationView;
import com.wali.live.common.gift.view.GiftContinueViewGroup;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.dao.Gift;
import com.wali.live.event.EventClass;
import com.wali.live.event.UserActionEvent;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.api.ZuidActiveRequest;
import com.wali.live.livesdk.live.api.ZuidSleepRequest;
import com.wali.live.livesdk.live.component.BaseLiveController;
import com.wali.live.livesdk.live.component.BaseLiveSdkView;
import com.wali.live.livesdk.live.component.data.StreamerPresenter;
import com.wali.live.livesdk.live.eventbus.LiveEventClass;
import com.wali.live.livesdk.live.fragment.AnchorEndLiveFragment;
import com.wali.live.livesdk.live.fragment.RecipientsSelectFragment;
import com.wali.live.livesdk.live.fragment.RoomAdminFragment;
import com.wali.live.livesdk.live.livegame.GameLiveController;
import com.wali.live.livesdk.live.livegame.GameLiveSdkView;
import com.wali.live.livesdk.live.liveshow.ShowLiveController;
import com.wali.live.livesdk.live.liveshow.ShowLiveSdkView;
import com.wali.live.livesdk.live.presenter.LiveRoomPresenter;
import com.wali.live.livesdk.live.receiver.ScreenStateReceiver;
import com.wali.live.livesdk.live.view.CountDownView;
import com.wali.live.livesdk.live.viewmodel.RoomTag;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.proto.LiveMessageProto;
import com.wali.live.proto.LiveProto;
import com.wali.live.receiver.PhoneStateReceiver;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.statistics.StatisticsWorker;
import com.wali.live.utils.AppNetworkUtils;
import com.wali.live.utils.AvatarUtils;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;
import com.wali.live.watchsdk.personinfo.fragment.FloatInfoFragment;
import com.wali.live.watchsdk.personinfo.presenter.ForbidManagePresenter;
import com.wali.live.watchsdk.ranking.RankingPagerFragment;
import com.wali.live.watchsdk.scheme.SchemeConstants;
import com.wali.live.watchsdk.scheme.SchemeSdkActivity;
import com.wali.live.watchsdk.task.IActionCallBack;
import com.wali.live.watchsdk.task.LiveTask;
import com.wali.live.watchsdk.watch.presenter.SnsShareHelper;
import com.wali.live.watchsdk.watch.presenter.push.GiftPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomManagerPresenter;
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

import static com.wali.live.component.BaseSdkController.MSG_END_LIVE_UNEXPECTED;
import static com.wali.live.component.BaseSdkController.MSG_HIDE_INPUT_VIEW;
import static com.wali.live.component.BaseSdkController.MSG_ON_BACK_PRESSED;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_ON_STREAM_RECONNECT;
import static com.wali.live.component.BaseSdkController.MSG_ON_STREAM_SUCCESS;
import static com.wali.live.component.BaseSdkController.MSG_OPEN_CAMERA_FAILED;
import static com.wali.live.component.BaseSdkController.MSG_OPEN_MIC_FAILED;
import static com.wali.live.livesdk.live.fragment.BasePrepareLiveFragment.EXTRA_LIVE_COVER_URL;
import static com.wali.live.livesdk.live.fragment.BasePrepareLiveFragment.EXTRA_LIVE_QUALITY;
import static com.wali.live.livesdk.live.fragment.BasePrepareLiveFragment.EXTRA_LIVE_TAG_INFO;
import static com.wali.live.livesdk.live.fragment.BasePrepareLiveFragment.EXTRA_LIVE_TITLE;
import static com.wali.live.livesdk.live.fragment.BasePrepareLiveFragment.EXTRA_SNS_TYPE;
import static com.wali.live.livesdk.live.fragment.BasePrepareLiveFragment.MEDIUM_CLARITY;
import static com.wali.live.livesdk.live.livegame.fragment.PrepareLiveFragment.EXTRA_GAME_LIVE_MUTE;
import static com.wali.live.statistics.StatisticsKey.AC_APP;
import static com.wali.live.statistics.StatisticsKey.KEY;
import static com.wali.live.statistics.StatisticsKey.TIMES;

/**
 * Created by chenyong on 2017/2/8.
 */
public class LiveSdkActivity extends BaseComponentSdkActivity implements FragmentDataListener, IActionCallBack,
        FloatInfoFragment.FloatInfoClickListener, ForbidManagePresenter.IForbidManageProvider {

    private static final String EXTRA_IS_GAME_LIVE = "extra_is_game_live";
    private static final String EXTRA_LOCATION = "extra_location";
    private static final String EXTRA_ENABLE_SHARE = "extra_enable_share";

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

    public static boolean sRecording = false; // 将其变为静态并暴露给外面，用于各种跳转判定

    public static final int REQUEST_CODE_PICK_MANAGER = 1000;

    private boolean mIsLiveEnd;
    private Intent mScreenRecordIntent;
    private String mLiveTitle;
    private String mLiveCoverUrl;
    private RoomTag mRoomTag;

    private boolean mIsShare;

    private boolean mEnableShare;
    private boolean mShareSelected;

    private boolean mIsGameLive;
    private Location mLocation;

    private final MyUIHandler mUIHandler = new MyUIHandler(this);

    private GiftPresenter mGiftPresenter;
    private RoomTextMsgPresenter mRoomTextMsgPresenter;
    private RoomViewerPresenter mRoomViewerPresenter;
    private ForbidManagePresenter mForbidManagePresenter;
    private RoomManagerPresenter mRoomManagerPresenter;

    protected BaseImageView mMaskIv; // 高斯蒙层
    protected ImageView mCloseBtn; // 关闭按钮

    protected FlyBarrageViewGroup mFlyBarrageViewGroup;

    protected TextView mTipsTv;

    protected StreamerPresenter mStreamerPresenter;
    protected BaseLiveController mController;
    protected BaseLiveSdkView mSdkView;
    protected final Action mAction = new Action();

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
    private long mHisBeginLiveCnt;
    private long mDuration;
    private long mNewFollowerCnt;

    private MyAlertDialog mTrafficDialog; //流量窗

    protected CustomHandlerThread mHandlerThread = new CustomHandlerThread("LiveSdkActivity") {
        @Override
        protected void processMessage(Message message) {
        }
    };

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
            mController.createStreamer(this, $(R.id.galileo_surface_view), 0, false, null);
        }
        mSdkView.enterPreparePage(this, REQUEST_PREPARE_LIVE, this);
        openOrientation();

        // 封面模糊图
        mMaskIv = $(R.id.mask_iv);
        AvatarUtils.loadAvatarByUidTs(mMaskIv, mMyRoomData.getUid(), mMyRoomData.getAvatarTs(),
                AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE, false, true);
        check4GNet();
    }

    private void initData() {
        Intent data = getIntent();
        if (data != null) {
            mIsGameLive = data.getBooleanExtra(EXTRA_IS_GAME_LIVE, false);
            mLocation = data.getParcelableExtra(EXTRA_LOCATION);
            mEnableShare = data.getBooleanExtra(EXTRA_ENABLE_SHARE, false);
        }
    }

    private void initRoomData() {
        if (mLocation != null) {
            mMyRoomData.setCity(mLocation.getCity());
        }
        mMyRoomData.setEnableShare(mEnableShare);
        mMyRoomData.setLiveType(mIsGameLive ? LiveManager.TYPE_LIVE_GAME : LiveManager.TYPE_LIVE_PUBLIC);
        if (UserAccountManager.getInstance().hasAccount()) {
            mMyRoomData.setUser(MyUserInfoManager.getInstance().getUser());
            mMyRoomData.setUid(UserAccountManager.getInstance().getUuidAsLong());
        }
    }

    private void setupRequiredComponent() {
        mStreamerPresenter = new StreamerPresenter(mMyRoomData);
        if (mIsGameLive) {
            mController = new GameLiveController(
                    mMyRoomData, mRoomChatMsgManager, mStreamerPresenter);
            mSdkView = new GameLiveSdkView(this, (GameLiveController) mController);
        } else {
            mController = new ShowLiveController(
                    mMyRoomData, mRoomChatMsgManager, mStreamerPresenter);
            mSdkView = new ShowLiveSdkView(this, (ShowLiveController) mController);
        }
        mStreamerPresenter.setComponentController(mController);

        addPresent(mStreamerPresenter);
        mAction.registerAction(); // 注册事件，准备页可能需要
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
        MyLog.w(TAG, "trySendDataWithServerOnce mMyRoomData.getUid()=" + mMyRoomData.getUid());
        if (mMyRoomData.getUid() == 0 && !MiLinkClientAdapter.getsInstance().isTouristMode()) {
            mMyRoomData.setUser(MyUserInfoManager.getInstance().getUser());
            mMyRoomData.setUid(UserAccountManager.getInstance().getUuidAsLong());
            AvatarUtils.loadAvatarByUidTs(mMaskIv, mMyRoomData.getUid(), mMyRoomData.getAvatarTs(),
                    AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE, false, true);
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
        if (AppNetworkUtils.is4g()) {
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
            mIsShare = false;
            beginLiveToServer();
        }
        if (!mIsGameLive) {
            resumeStream();
        }
        mController.onActivityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!mIsGameLive) {
            pauseStream();
        }
        mController.onActivityPaused();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mController.onActivityStopped();
    }

    private void pauseStream() {
        mIsPaused = true;
        if (!mIsGameLive) {
            mStreamerPresenter.stopPreview();
        }
        mController.onPauseStream();

        String roomId = mMyRoomData.getRoomId();
        if (sRecording && !TextUtils.isEmpty(roomId)) { // TODO 这里会有耗时，初始化这个pb对象会在static中耗时
            new ZuidSleepRequest(roomId).async();
            mUIHandler.removeMessages(MSG_END_LIVE_FOR_TIMEOUT);
            mUIHandler.sendEmptyMessageDelayed(MSG_END_LIVE_FOR_TIMEOUT, BACKGROUND_TIMEOUT);
        }
    }

    private void resumeStream() {
        mIsPaused = false;
        if (!mIsGameLive) {
            mStreamerPresenter.startPreview();
        }
        mController.onResumeStream();
        String roomId = mMyRoomData.getRoomId();
        if (sRecording && !TextUtils.isEmpty(roomId)) {
            new ZuidActiveRequest(roomId).async();
            mUIHandler.removeMessages(MSG_END_LIVE_FOR_TIMEOUT);
        }
    }

    @Override
    protected void onDestroy() {
        if (sRecording) {
            stopRecord("onDestroy", false);
            sRecording = false;
        }
        super.onDestroy();
        if (mScreenStateReceiver != null) {
            unregisterReceiver(mScreenStateReceiver);
        }
        PhoneStateReceiver.unregisterReceiver(this, mPhoneStateReceiver);
        if (mController != null) {
            mController.release();
            mController = null;
        }
        if (mSdkView != null) {
            mSdkView.stopView();
            mSdkView.release();
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
        if (mController != null) {
            mController.postEvent(MSG_ON_ORIENT_LANDSCAPE);
        }
        if (mGiftContinueViewGroup != null) {
            mGiftContinueViewGroup.setOrient(true);
        }
        orientCloseBtn(true);
    }

    protected void orientPortrait() {
        if (mController != null) {
            mController.postEvent(MSG_ON_ORIENT_PORTRAIT);
        }
        if (mGiftContinueViewGroup != null) {
            mGiftContinueViewGroup.setOrient(false);
        }
        orientCloseBtn(false);
    }

    @Override
    public void onClickTopOne(User user) {
    }

    @Override
    public void onClickMainAvatar(User user) {
    }

    /**
     * 显示个人资料的浮框
     */
    private void startShowFloatPersonInfo(long uid) {
        if (uid <= 0) {
            return;
        }
        StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP, StatisticsKey.KEY_USERINFO_CARD_OPEN, 1);
//        FloatPersonInfoFragment.openFragment(this, uid, mMyRoomData.getUid(),
//                mMyRoomData.getRoomId(), mMyRoomData.getVideoUrl(), this);
        FloatInfoFragment.openFragment(this, uid, mMyRoomData.getUid(),
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
                case REQUEST_CODE_PICK_MANAGER:
                    User pickManager = (User) data.getSerializableExtra(RecipientsSelectFragment.RESULT_SINGLE_OBJECT);
                    if (pickManager != null) {
                        String roomId = TextUtils.isEmpty(mMyRoomData.getRoomId()) ? "" : mMyRoomData.getRoomId();
                        if (LiveRoomCharacterManager.getInstance().getManagerCount() < LiveRoomCharacterManager.MANAGER_CNT && !LiveRoomCharacterManager.getInstance().isManager(pickManager.getUid())) {
                            LiveRoomCharacterManager.setManagerRxTask(this, pickManager, roomId, mMyRoomData.getUid(), true);
                        }
                    }
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
                if (mShareSelected) {
                    getRoomIdToServer();
                } else {
                    beginLiveToServer();
                }
                mLiveRoomPresenter.initManager(UserAccountManager.getInstance().getUuidAsLong());
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
        mLiveTitle = bundle.getString(EXTRA_LIVE_TITLE);
        mRoomTag = (RoomTag) bundle.getSerializable(EXTRA_LIVE_TAG_INFO);
        mLiveCoverUrl = bundle.getString(EXTRA_LIVE_COVER_URL, "");
        mShareSelected = bundle.getBoolean(EXTRA_SNS_TYPE, false);

        mMyRoomData.setLiveTitle(mLiveTitle);
        mLiveRoomPresenter = new LiveRoomPresenter(this);
        addPresent(mLiveRoomPresenter);

        if (mIsGameLive) {
            int quality = bundle.getInt(EXTRA_LIVE_QUALITY, MEDIUM_CLARITY);
            boolean isMute = bundle.getBoolean(EXTRA_GAME_LIVE_MUTE, false);
            mController.createStreamer(this, null, quality, isMute, mScreenRecordIntent);
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
            if (MyUserInfoManager.getInstance().getUser() != null && MyUserInfoManager.getInstance().getUser().getUid() >= 0) {
                mMyRoomData.setUser(MyUserInfoManager.getInstance().getUser());
            } else {
                MyUserInfoManager.getInstance().syncSelfDetailInfo();
            }
        }

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
        addPresent(mGiftPresenter);
        addPushProcessor(mGiftPresenter);
        mRoomTextMsgPresenter = new RoomTextMsgPresenter(mRoomChatMsgManager);
        addPresent(mRoomTextMsgPresenter);
        addPushProcessor(mRoomTextMsgPresenter);
        mRoomViewerPresenter = new RoomViewerPresenter(mRoomChatMsgManager);
        addPresent(mRoomViewerPresenter);
        addPushProcessor(mRoomViewerPresenter);
        mRoomManagerPresenter = new RoomManagerPresenter(this, mRoomChatMsgManager, false, mMyRoomData);
        addPresent(mRoomManagerPresenter);
        addPushProcessor(mRoomManagerPresenter);

        mTipsTv = $(R.id.tips_tv);

        mSdkView.setupView();
        mSdkView.startView();

        mAction.unregisterAction(); // 重新注册，保证mAction最后收到事件
        mAction.registerAction();

        mFlyBarrageViewGroup = $(R.id.fly_barrage_viewgroup);
        addBindActivityLifeCycle(mFlyBarrageViewGroup, true);

        mCountDownView = $(R.id.count_down_view);
        mMyRoomData.setInitTicket(mMyRoomData.getTicket());

        // 用根View的点击事件代替mTouchDelegateView
        $(R.id.main_act_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mController != null) {
                    mController.postEvent(MSG_HIDE_INPUT_VIEW);
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.PhoneStateEvent event) {
        if (event != null && mController != null && !mIsLiveEnd) {
            MyLog.w(TAG, "onEventMainThread PhoneStateEvent type=" + event.type);
            switch (event.type) {
                case EventClass.PhoneStateEvent.TYPE_PHONE_STATE_IDLE:
                    resumeStream();
                    break;
                case EventClass.PhoneStateEvent.TYPE_PHONE_STATE_RING:
                    if (mPhoneInterruptDialog == null || !mPhoneInterruptDialog.isShowing()) {
                        mPhoneInterruptDialog = DialogUtils.showAlertDialog(this, getString(R.string.warm_prompt), getString(R.string.phone_interrupt), getString(R.string.i_know));
                    }
                    pauseStream();
                    break;
                case EventClass.PhoneStateEvent.TYPE_PHONE_STATE_OFFHOOK:
                    pauseStream();
                    break;
                default:
                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LiveEventClass.ScreenStateEvent event) {
        if (event != null && mController != null) {
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
            if (mStreamerPresenter != null && !mIsGameLive) {
                mStreamerPresenter.setAngle(event.orientation);
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
                    break;
                case MiLinkCommand.COMMAND_GET_USER_INFO_BY_ID:
                    processOwnerInfo(errCode, objects);
                    break;
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
                processRoomIdInfo((String) objects[0], (String) objects[1], (List<LiveCommonProto.UpStreamUrl>) objects[2], (String) objects[3]);
                break;
            case ErrorCode.CODE_ZUID_CERTIFY_ERROR:
            case ErrorCode.CODE_ZUID_NOT_ADULT:
            case ErrorCode.CODE_ZUID_CERTIFY_GOING:
            default:
                processPreLive();
                break;
        }
    }

    private void processBeginLive(int errCode, Object... objects) {
        switch (errCode) {
            case ErrorCode.CODE_SUCCESS:
                processStartRecord((String) objects[0], (long) objects[1], (String) objects[2],
                        (List<LiveCommonProto.UpStreamUrl>) objects[3], (String) objects[4]);
                break;
            case ErrorCode.CODE_ZUID_CERTIFY_ERROR:
            case ErrorCode.CODE_ZUID_NOT_ADULT:
            case ErrorCode.CODE_ZUID_CERTIFY_GOING:
            default:
                ToastUtils.showToast(GlobalData.app(), R.string.live_failure);
                //失敗到結束頁
                enterEndFragment();
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
                //參數3是tickets暂时不用
                mHisBeginLiveCnt = (long) objects[4];
                mDuration = (long) objects[5];
                mNewFollowerCnt = (long) objects[6];

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
                    //失敗到結束頁
                    enterEndFragment();
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
        if (!TextUtils.isEmpty(mMyRoomData.getShareUrl())) {
            mIsShare = true;
            SnsShareHelper.getInstance().shareToSns(-1, mMyRoomData);
        } else {
            beginLiveToServer();
        }
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
        String roomId = TextUtils.isEmpty(mMyRoomData.getRoomId()) ? "" : mMyRoomData.getRoomId();
        mLiveRoomPresenter.beginLiveByAppInfo(mLocation, mIsGameLive ? LiveManager.TYPE_LIVE_GAME : LiveManager.TYPE_LIVE_PUBLIC, null, true, mLiveTitle,
                mLiveCoverUrl, roomId, null, 0, mRoomTag, MiLinkConstant.MY_APP_TYPE, false);
    }

    private void processRoomIdInfo(String liveId, String shareUrl, List<LiveCommonProto.UpStreamUrl> upStreamUrlList, String udpUpstreamUrl) {
        mMyRoomData.setRoomId(liveId);
        mMyRoomData.setShareUrl(shareUrl);
        mStreamerPresenter.setOriginalStreamUrl(upStreamUrlList, udpUpstreamUrl);
        processPreLive();
    }

    private void processStartRecord(String liveId, long createTime, String shareUrl,
                                    List<LiveCommonProto.UpStreamUrl> upStreamUrlList, String udpUpstreamUrl) {
        MyLog.w(TAG, "processStartRecord, liveId:" + liveId);
        mMyRoomData.setRoomId(liveId);
        mMyRoomData.setShareUrl(shareUrl);
        RoomInfoGlobalCache.getsInstance().enterCurrentRoom(mMyRoomData.getRoomId());
        mStreamerPresenter.setOriginalStreamUrl(upStreamUrlList, udpUpstreamUrl);
        startRecord();
        syncSystemMessage();
        if (mController != null) {
            mController.postEvent(BaseLiveController.MSG_ON_LIVE_SUCCESS);
        }
    }

    private void syncSystemMessage() {
        String roomId = mMyRoomData.getRoomId();
        if (!TextUtils.isEmpty(roomId)) {
            long myId = UserAccountManager.getInstance().getUuidAsLong();
            LiveMessageProto.SyncSysMsgRequest syncSysMsgRequest = LiveMessageProto.SyncSysMsgRequest.newBuilder()
                    .setCid(System.currentTimeMillis()).setFromUser(myId).setRoomId(roomId).build();
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
            mController.onStartLive();
        }
    }

    /**
     * 停止录制
     *
     * @param reason
     * @param wasKicked 是否因为被踢
     */
    private void stopRecord(String reason, boolean wasKicked) {
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
            mController.onStopLive(wasKicked);
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
                String roomId = mMyRoomData.getRoomId();
                if (TextUtils.isEmpty(roomId)) {
                    return;
                }
                LiveProto.HeartBeatReq.Builder builder = LiveProto.HeartBeatReq.newBuilder()
                        .setLiveId(roomId)
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
        if (mController != null && mController.postEvent(MSG_ON_BACK_PRESSED)) {
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
        AnchorEndLiveFragment.openFragment(LiveSdkActivity.this,
                mMyRoomData.getUser(),
                mMyRoomData.getShareUrl(),
                mMyRoomData.getLiveTitle(),
                mMyRoomData.getCoverUrl(),
                mLocation == null ? "" : mLocation.getCity(),
                mMyRoomData.getViewerCnt(),
                mDuration,
                mNewFollowerCnt,
                mMyRoomData.getTicket() - mMyRoomData.getInitTicket(),
                mMyRoomData.getRoomId(),
                mMyRoomData.getLiveType(),
                mGenerateHistorySucc,
                mGenerateHistoryMsg,
                true, (int) mHisBeginLiveCnt, mEnableShare);
    }

    private void showStopDialog() {
        DialogUtils.showNormalDialog(this, 0,
                R.string.stop_live_dialog_message,
                R.string.ok,
                R.string.cancel,
                new DialogUtils.IDialogCallback() {
                    @Override
                    public void process(DialogInterface dialogInterface, int i) {
                        stopRecord("showStopDialog", false);
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
        stopRecord("endLiveUnexpected", false);
    }

    @Override
    public ForbidManagePresenter provideForbidManagePresenter() {
        if (mForbidManagePresenter == null) {
            mForbidManagePresenter = new ForbidManagePresenter(this);
        }
        return mForbidManagePresenter;
    }

    private void viewerTopFromServer(RoomBaseDataModel roomData) {
        if (TextUtils.isEmpty(roomData.getRoomId())) {
            MyLog.d(TAG, "viewerTop roomId is empty");
            return;
        }
        mHandlerThread.post(LiveTask.viewerTop(roomData, new WeakReference<IActionCallBack>(this)));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(final LiveEventClass.LiveCoverEvent event) {
        if (event != null && mMaskIv.getVisibility() == View.VISIBLE) {
            FrescoWorker.loadImage(mMaskIv,
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
            case UserActionEvent.EVENT_TYPE_REQUEST_LOOK_MORE_VIEWER: {
                viewerTopFromServer((RoomBaseDataModel) event.obj1);
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
            case UserActionEvent.EVENT_TYPE_CLICK_SUPPORT_WIDGET:
                Gift gift = GiftRepository.findGiftById((int) event.obj1);
                if (gift != null) {
                    BarrageMsg pushMsg = GiftRepository.createGiftBarrageMessage(gift.getGiftId(), gift.getName(), gift.getCatagory(),
                            gift.getSendDescribe(), 1, 0, System.currentTimeMillis(), -1, mMyRoomData.getRoomId(), String.valueOf(mMyRoomData.getUid()), "", "", 0);
                    BarrageMessageManager.getInstance().pretendPushBarrage(pushMsg);
                }
                break;
            default:
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(EventClass.OnActivityResultEvent event) {
        onActivityResult(event.requestCode, event.resultCode, event.data);
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
                mMyRoomData.setMsgRule(event.getMsgRule());
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
                    activity.stopRecord("MSG_END_LIVE_FOR_TIMEOUT", false);
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

    private class Action implements IEventObserver {

        private void registerAction() {
            mController.registerObserverForEvent(MSG_END_LIVE_UNEXPECTED, this);
            mController.registerObserverForEvent(MSG_END_LIVE_FOR_TIMEOUT, this);
            mController.registerObserverForEvent(MSG_OPEN_MIC_FAILED, this);
            mController.registerObserverForEvent(MSG_ON_STREAM_RECONNECT, this);
            mController.registerObserverForEvent(MSG_ON_STREAM_SUCCESS, this);
            if (!mIsGameLive) {
                mController.registerObserverForEvent(MSG_OPEN_CAMERA_FAILED, this);
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
                case MSG_END_LIVE_UNEXPECTED: {
                    int resId = params.getItem(0);
                    endLiveUnexpected(resId);
                    return true;
                }
                case MSG_OPEN_CAMERA_FAILED:
                    DialogUtils.showAlertDialog(LiveSdkActivity.this, getString(R.string.setting_dialog_black_title),
                            getString(R.string.camera_occupy), getString(R.string.ok));
                    return true;
                case MSG_OPEN_MIC_FAILED:
                    DialogUtils.showAlertDialog(LiveSdkActivity.this, getString(R.string.setting_dialog_black_title),
                            getString(R.string.mic_occupy), getString(R.string.ok));
                    return true;
                case MSG_ON_STREAM_SUCCESS:
                    mUIHandler.removeMessages(MSG_HIDE_NETWORK_NOT_GOOD_TIPS);
                    mUIHandler.sendEmptyMessage(MSG_HIDE_NETWORK_NOT_GOOD_TIPS);
                    return true;
                case MSG_ON_STREAM_RECONNECT:
                    mUIHandler.sendEmptyMessage(MSG_SHOW_NETWORK_NOT_GOOD_TIPS);
                    mUIHandler.removeMessages(MSG_HIDE_NETWORK_NOT_GOOD_TIPS);
                    return true;
                default:
                    break;
            }
            return false;
        }
    }

    @Override
    protected void onEventShare(EventClass.ShareEvent event) {
        if (event != null) {
            if (mIsShare) {
                mIsShare = false;
                beginLiveToServer();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(UserInfoEvent userInfoEvent) {
        MyLog.w(TAG, "userInfoEvent");
        mMyRoomData.setUser(MyUserInfoManager.getInstance().getUser());
    }

    @Override
    public void onKickEvent(String msg) {
        stopRecord(msg, true);
    }

    /**
     * 秀场直播
     */
    public static void openActivity(Activity activity, Location location) {
        openActivity(activity, location, false, false);
    }

    /**
     * 区分是否是游戏直播还是秀场直播
     */
    public static void openActivity(Activity activity, Location location, boolean enableShare, boolean isGameLive) {
        Intent intent = new Intent(activity, LiveSdkActivity.class);
        if (enableShare) {
            intent.putExtra(EXTRA_ENABLE_SHARE, enableShare);
        }
        if (location != null) {
            intent.putExtra(EXTRA_LOCATION, location);
        }
        intent.putExtra(EXTRA_IS_GAME_LIVE, isGameLive);
        activity.startActivity(intent);
    }
}
