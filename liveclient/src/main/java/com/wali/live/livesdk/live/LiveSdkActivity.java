package com.wali.live.livesdk.live;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.activity.BaseSdkActivity;
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
import com.base.permission.PermissionUtils;
import com.base.preference.PreferenceUtils;
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
import com.mi.live.data.location.Address;
import com.mi.live.data.location.Location;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.live.data.preference.PreferenceKeys;
import com.mi.live.data.query.model.MessageRule;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.mi.live.data.user.User;
import com.mi.live.engine.base.EngineEventClass;
import com.mi.live.engine.base.GalileoConstants;
import com.mi.live.engine.streamer.GalileoStreamer;
import com.mi.live.engine.streamer.IStreamer;
import com.mi.live.engine.streamer.StreamerConfig;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.base.BaseEvent;
import com.wali.live.common.barrage.manager.BarrageMessageManager;
import com.wali.live.common.flybarrage.view.FlyBarrageViewGroup;
import com.wali.live.common.gift.view.GiftAnimationView;
import com.wali.live.common.gift.view.GiftContinueViewGroup;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.component.BaseSdkView;
import com.wali.live.dns.ILiveReconnect;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.api.ZuidActiveRequest;
import com.wali.live.livesdk.live.api.ZuidSleepRequest;
import com.wali.live.livesdk.live.component.BaseLiveController;
import com.wali.live.livesdk.live.component.data.StreamerPresenter;
import com.wali.live.livesdk.live.dns.MultiCdnIpSelectionHelper;
import com.wali.live.livesdk.live.eventbus.LiveEventClass;
import com.wali.live.livesdk.live.fragment.BasePrepareLiveFragment;
import com.wali.live.livesdk.live.fragment.EndLiveFragment;
import com.wali.live.livesdk.live.fragment.PrepareGameLiveFragment;
import com.wali.live.livesdk.live.fragment.RoomAdminFragment;
import com.wali.live.livesdk.live.operator.LiveOperator;
import com.wali.live.livesdk.live.presenter.GameLivePresenter;
import com.wali.live.livesdk.live.presenter.LiveRoomPresenter;
import com.wali.live.livesdk.live.presenter.RoomInfoPresenter;
import com.wali.live.livesdk.live.receiver.ScreenStateReceiver;
import com.wali.live.livesdk.live.task.IActionCallBack;
import com.wali.live.livesdk.live.utils.LocationHelper;
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
import com.wali.live.watchsdk.personinfo.fragment.FloatPersonInfoFragment;
import com.wali.live.watchsdk.personinfo.presenter.ForbidManagePresenter;
import com.wali.live.watchsdk.watch.presenter.push.GiftPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomStatusPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomTextMsgPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomViewerPresenter;
import com.xiaomi.broadcaster.enums.VCSessionErrType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.lang.ref.WeakReference;
import java.util.Arrays;
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
public class LiveSdkActivity extends BaseComponentSdkActivity implements ILiveReconnect, FragmentDataListener, IActionCallBack,
        FloatPersonInfoFragment.FloatPersonInfoClickListener, ForbidManagePresenter.IForbidManageProvider {

    public static final int REQUEST_MEDIA_PROJECTION = 2000;
    public static final int REQUEST_PREPARE_LIVE = 1000;

    private static final int HEARTBEAT_DURATION = 10 * 1000;        //心跳间隔时间
    private static final int HEARTBEAT_TIMEOUT = 120 * 1000;        //心跳超时时间
    private static final int RECONNECT_TIMEOUT = 5 * 1000;          //卡顿超时换IP时间
    private static final int START_STREAM_TIMEOUT = 5 * 1000;       //推流超时时间
    private static final int BACKGROUND_TIMEOUT = 10 * 60 * 1000;   //刷新房间信息区间

    private static final int MSG_SHOW_NETWORK_NOT_GOOD_TIPS = 105;  //显示网络不佳信息
    private static final int MSG_HIDE_NETWORK_NOT_GOOD_TIPS = 106;  //隐藏网络不佳信息
    private static final int MSG_SHOW_NETWORK_CHANGE_TIPS = 108;    //显示网络切换信息
    private static final int MSG_HIDE_NETWORK_CHANGE_TIPS = 109;    //隐藏网络切换信息

    private static final int MSG_HEARTBEAT = 201;                   //心跳
    private static final int MSG_HEARTBEAT_TIMEOUT = 202;           //心跳超时
    private static final int MSG_START_STREAM_TIMEOUT = 203;        //推流超时
    private static final int MSG_ROOM_NOT_EXIT = 204;               //房间不存在
    private static final int MSG_END_LIVE = 205;                    //停止直播
    private static final int MSG_START_STREAM_FAILED = 210;         //推流失败
    private static final int MSG_END_LIVE_FOR_TIMEOUT = 211;        //超时退出房间
    private static final int MSG_SHOW_MIC_OCCUPY = 213;             //麦克风被占用提示
    private static final int MSG_START_STREAM = 214;                //启动流

    public static boolean sRecording = false; //将其变为静态并暴露给外面，用于各种跳转判定
    public static final int[] VIDEO_RATE_360P = new int[]{400, 600, 800};
    private boolean mIsLiveEnd;
    private int mLastTicket;
    private Location mLocation = null;
    private Intent mScreenRecordIntent;
    private String mLiveTitle;
    private RoomTag mRoomTag;
    private int mClarity = PrepareGameLiveFragment.LOW_CLARITY;

    protected IStreamer mStreamer;
    protected GameLivePresenter mGameLivePresenter;
    protected RoomInfoPresenter mRoomInfoPresenter;

    protected boolean mIsGameLive = false;
    protected StreamerPresenter mStreamerPresenter;

    private final MyUIHandler mUIHandler = new MyUIHandler(this);
    private ExecutorService mHeartbeatService;

    private LiveOperator mLiveOperator;

    // 域名解析、重连相关
    private
    @NonNull
    MultiCdnIpSelectionHelper mIpSelectionHelper;
    private boolean mIsStarted = false;

    private GiftPresenter mGiftPresenter;
    private RoomTextMsgPresenter mRoomTextMsgPresenter;
    private RoomViewerPresenter mRoomViewerPresenter;
    private RoomStatusPresenter mRoomStatusPresenter;
    private ForbidManagePresenter mForbidManagePresenter;

    protected BaseImageView mBlurIv; // 高斯蒙层
    protected ImageView mCloseBtn; // 关闭按钮

    protected LiveTopInfoSingleView mTopInfoSingleView;

    protected FlyBarrageViewGroup mFlyBarrageViewGroup;

    protected TextView mTipsTv;

    protected BaseLiveController mComponentController;
    protected BaseSdkView mSdkView;

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
        getLocation();
        setupRequiredComponent();

        if (!mIsGameLive) {
            setupConfig(GalileoConstants.LIVE_LOW_RESOLUTION_WIDTH, GalileoConstants.LIVE_LOW_RESOLUTION_HEIGHT, true);
        }
//        prepareGameLive();
        mComponentController.enterPreparePage(this, REQUEST_PREPARE_LIVE, this);

        openOrientation();
        mMyRoomData.setUser(MyUserInfoManager.getInstance().getUser());
        mMyRoomData.setUid(UserAccountManager.getInstance().getUuidAsLong());
        // 封面模糊图
        mBlurIv = $(R.id.blur_iv);
        AvatarUtils.loadAvatarByUidTs(mBlurIv, mMyRoomData.getUid(), mMyRoomData.getAvatarTs(),
                AvatarUtils.SIZE_TYPE_AVATAR_MIDDLE, false, true);
    }

    private void setupRequiredComponent() {
        if (mIsGameLive) {
            mComponentController = new com.wali.live.livesdk.live.livegame.LiveComponentController(
                    mMyRoomData, mRoomChatMsgManager);
        } else {
            mComponentController = new com.wali.live.livesdk.live.liveshow.LiveComponentController(
                    mMyRoomData, mRoomChatMsgManager);
        }
        mSdkView = mComponentController.createSdkView(this);
        mStreamerPresenter = new StreamerPresenter(mComponentController);
        mLiveOperator = new LiveOperator();
        mIpSelectionHelper = new MultiCdnIpSelectionHelper(this, this);
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

    }

    @Override
    protected void tryClearData() {

    }

    private void prepareGameLive() {
        MyLog.w(TAG, "prepareGameLive");
        com.wali.live.livesdk.live.livegame.fragment.PrepareLiveFragment.openFragment(
                this, REQUEST_PREPARE_LIVE, this);
        mRoomChatMsgManager.setIsGameLiveMode(true);
    }

    private void prepareShowLive() {
        mComponentController.enterPreparePage(this, REQUEST_PREPARE_LIVE, this);
    }

    @Override
    public void finish() {
        MyLog.w(TAG, "finish");
        super.finish();
        overridePendingTransition(R.anim.slide_out_to_bottom, R.anim.slide_out_to_bottom);
        clear();
    }

    public void getLocation() {
        if (PermissionUtils.checkAccessLocation(this)) {
            if (mLocation == null || TextUtils.isEmpty(mLocation.getCity())) {
                LocationHelper.getInstance().getAddress(new LocationHelper.AddressCallback() {
                    @Override
                    public void returnAddress(double latitude, double longitude, com.baidu.location.Address address) {
                        if (address != null) {
                            mLocation = new Location(latitude, longitude, new Address(address.country, address.province, address.city));
                            mMyRoomData.setLocation(mLocation.getCity());
                            EventBus.getDefault().post(new BasePrepareLiveFragment.LocationEvent());
                        }
                    }
                });
            } else {
                mLocation = null;
                mMyRoomData.setLocation("");
                EventBus.getDefault().post(new BasePrepareLiveFragment.LocationEvent());
            }
        } else {
            PermissionUtils.requestPermissionDialog(this, PermissionUtils.PermissionType.ACCESS_COARSE_LOCATION);
        }
    }

    public String getCity() {
        MyLog.w(TAG, "getCity mLocation != null" + (mLocation != null));
        if (mLocation != null) {
            return mLocation.getCity();
        }
        return "";
    }

    @Subscribe
    public void onEvent(SdkEventClass.OrientEvent event) {
        if (event.isLandscape()) {
            orientLandscape();
        } else {
            orientPortrait();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        KeyboardUtils.hideKeyboard(this);
        resumeGameLive();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mIsForeground = true;
    }

    private void pauseStream() {
        mGameLivePresenter.pauseStream();
        mRoomInfoPresenter.pauseTimer();
        mIsForeground = false;
        if (!TextUtils.isEmpty(mMyRoomData.getRoomId()) && sRecording) {
            //TODO 这里会有耗时，初始化这个pb对象会在static中耗时
            new ZuidSleepRequest(mMyRoomData.getRoomId()).async();
            mUIHandler.removeMessages(MSG_END_LIVE_FOR_TIMEOUT);
            mUIHandler.sendEmptyMessageDelayed(MSG_END_LIVE_FOR_TIMEOUT, BACKGROUND_TIMEOUT);
        }
    }

    private void resumeGameLive() {
        // onResume 更新游戏直播静音按钮
        if (mComponentController != null) {
            mComponentController.onEvent(BaseLiveController.MSG_DEFAULT);
        }
    }

    private void resumeStream() {
        mGameLivePresenter.resumeStream();
        mRoomInfoPresenter.resumeTimer();
        mIsForeground = true;
        if (!TextUtils.isEmpty(mMyRoomData.getRoomId()) && sRecording) {
            new ZuidActiveRequest(mMyRoomData.getRoomId()).async();
            mUIHandler.removeMessages(MSG_END_LIVE_FOR_TIMEOUT);
        }
    }

    //引擎的释放统一在clear处理.
    private void clear() {
        if (mStreamer != null) {
            mStreamer.stopMusic();
            mStreamer.destroy();
            mStreamer = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clear();
        if (mScreenStateReceiver != null) {
            unregisterReceiver(mScreenStateReceiver);
        }
        PhoneStateReceiver.unregisterReceiver(this, mPhoneStateReceiver);
        sRecording = false;
        if (mComponentController != null) {
            mComponentController.release();
            mComponentController = null;
        }
        if (mSdkView != null) {
            mSdkView.releaseSdkView();
            mSdkView = null;
        }
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
        if (mGameLivePresenter != null) {
            mGameLivePresenter.onOrientation(true);
        }
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
        if (mGameLivePresenter != null) {
            mGameLivePresenter.onOrientation(false);
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
        FloatPersonInfoFragment.openFragment(this, uid, mMyRoomData.getUid(),
                mMyRoomData.getRoomId(), mMyRoomData.getVideoUrl(), this);
    }


    private void setupConfig(int width, int height, boolean hasMicSource) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

        StreamerConfig.Builder builder = new StreamerConfig.Builder();
        String videoRate = PreferenceUtils.getSettingString(GlobalData.app(), PreferenceKeys.PREF_KEY_VIDEO_RATE, null);
        if (!TextUtils.isEmpty(videoRate)) {
            String[] videoRates = videoRate.split(",");
            MyLog.w(TAG, Arrays.toString(videoRates));
            for (int i = 0; i < videoRates.length; i++) {
                VIDEO_RATE_360P[i] = Integer.valueOf(videoRates[i]);
            }
        }
        if (hasMicSource) {
            builder.setMinAverageVideoBitrate(VIDEO_RATE_360P[0]);
            builder.setMaxAverageVideoBitrate(VIDEO_RATE_360P[2]);
        } else {
            switch (width) {
                case GalileoConstants.GAME_LOW_RESOLUTION_WIDTH:
                    builder.setMinAverageVideoBitrate(500);
                    builder.setMaxAverageVideoBitrate(500);
                    break;
                case GalileoConstants.GAME_MEDIUM_RESOLUTION_WIDTH:
                    builder.setMinAverageVideoBitrate(1000);
                    builder.setMaxAverageVideoBitrate(1000);
                    break;
                case GalileoConstants.GAME_HIGH_RESOLUTION_WIDTH:
                    builder.setMinAverageVideoBitrate(2000);
                    builder.setMaxAverageVideoBitrate(2000);
                    break;
            }
        }
        builder.setAutoAdjustBitrate(true);
        builder.setFrameRate(15);
        builder.setSampleAudioRateInHz(44100);
        MyLog.w(TAG, "create streamer");
        mStreamer = new GalileoStreamer(GlobalData.app(), UserAccountManager.getInstance().getUuid(), width, height, hasMicSource);
        mStreamer.setConfig(builder.build());
        if (!mIsGameLive) {
            mStreamer.setDisplayPreview($(R.id.galileo_surface_view));
            // TODO 设置滤镜参数
//            mStreamer.setVideoFilterIntensity(StreamerUtils.getFilterIntensityInteger() / 100f);
//            mStreamer.setVideoFilter(StreamerUtils.getFilter());
        }
        String clientIp = MiLinkClientAdapter.getsInstance().getClientIp();
        if (!TextUtils.isEmpty(clientIp)) {
            mStreamer.setClientPublicIp(clientIp);
        }
        MyLog.w(TAG, "create streamer over");
        mStreamerPresenter.setStreamer(mStreamer);
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
                beginLiveToServer();
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
        mMyRoomData.setLiveTitle(mLiveTitle);
        mClarity = bundle.getInt(PrepareGameLiveFragment.EXTRA_GAME_LIVE_QUALITY, PrepareGameLiveFragment.MEDIUM_CLARITY);
        mLiveRoomPresenter = new LiveRoomPresenter(this);
        addPresent(mLiveRoomPresenter);
        mRoomTextMsgPresenter = new RoomTextMsgPresenter(mRoomChatMsgManager);
        addPresent(mRoomTextMsgPresenter);
        mGiftPresenter = new GiftPresenter(mRoomChatMsgManager, false);
        addPresent(mGiftPresenter);
        if (mIsGameLive) {
            initGameLivePresenter();
        }
    }

    private void initGameLivePresenter() {
        switch (mClarity) {
            case PrepareGameLiveFragment.LOW_CLARITY:
                setupConfig(GalileoConstants.GAME_LOW_RESOLUTION_WIDTH, GalileoConstants.GAME_LOW_RESOLUTION_HEIGHT, false);
                mGameLivePresenter = new GameLivePresenter(mStreamer, mRoomChatMsgManager, mMyRoomData, GalileoConstants.GAME_LOW_RESOLUTION_WIDTH,
                        GalileoConstants.GAME_LOW_RESOLUTION_HEIGHT, mScreenRecordIntent, mRoomChatMsgManager.toString());
                break;
            case PrepareGameLiveFragment.MEDIUM_CLARITY:
                setupConfig(GalileoConstants.GAME_MEDIUM_RESOLUTION_WIDTH, GalileoConstants.GAME_MEDIUM_RESOLUTION_HEIGHT, false);
                mGameLivePresenter = new GameLivePresenter(mStreamer, mRoomChatMsgManager, mMyRoomData, GalileoConstants.GAME_MEDIUM_RESOLUTION_WIDTH,
                        GalileoConstants.GAME_MEDIUM_RESOLUTION_HEIGHT, mScreenRecordIntent, mRoomChatMsgManager.toString());
                break;
            case PrepareGameLiveFragment.HIGH_CLARITY:
                setupConfig(GalileoConstants.GAME_HIGH_RESOLUTION_WIDTH, GalileoConstants.GAME_HIGH_RESOLUTION_HEIGHT, false);
                mGameLivePresenter = new GameLivePresenter(mStreamer, mRoomChatMsgManager, mMyRoomData, GalileoConstants.GAME_HIGH_RESOLUTION_WIDTH,
                        GalileoConstants.GAME_HIGH_RESOLUTION_HEIGHT, mScreenRecordIntent, mRoomChatMsgManager.toString());
                break;
            default:
                setupConfig(GalileoConstants.GAME_HIGH_RESOLUTION_WIDTH, GalileoConstants.GAME_HIGH_RESOLUTION_HEIGHT, false);
                mGameLivePresenter = new GameLivePresenter(mStreamer, mRoomChatMsgManager, mMyRoomData, GalileoConstants.GAME_HIGH_RESOLUTION_WIDTH,
                        GalileoConstants.GAME_HIGH_RESOLUTION_HEIGHT, mScreenRecordIntent, mRoomChatMsgManager.toString());
                break;
        }
        addPresent(mGameLivePresenter);
        mRoomInfoPresenter = new RoomInfoPresenter(mGameLivePresenter);
        addPresent(mRoomInfoPresenter);
    }

    private void startCountDown() {
        MyLog.w(TAG, "startCountDown");
        mCountDownView.startCountDown();
    }

    private void postPrepare() {
        // 注册监听
        mPhoneStateReceiver = PhoneStateReceiver.registerReceiver(this);
        registerScreenStateReceiver();

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

        mGiftPresenter = new GiftPresenter(mRoomChatMsgManager, false);
        addPushProcessor(mGiftPresenter);
        mRoomTextMsgPresenter = new RoomTextMsgPresenter(mRoomChatMsgManager);
        addPushProcessor(mRoomTextMsgPresenter);
        mRoomViewerPresenter = new RoomViewerPresenter(mRoomChatMsgManager);
        addPushProcessor(mRoomViewerPresenter);
        mRoomStatusPresenter = new RoomStatusPresenter(mRoomChatMsgManager);
        addPushProcessor(mRoomStatusPresenter);

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
        MyLog.w(TAG, "onEventMainThread PhoneStateEvent");
        if (event != null) {
            switch (event.type) {
                case LiveEventClass.SystemEvent.EVENT_TYPE_ACTION_PHONE_STATE_CHANGED_IDLE: {
                    resumeStream();
                }
                break;
                case LiveEventClass.SystemEvent.EVENT_TYPE_ACTION_PHONE_STATE_CHANGED_RING: {
                    if (mPhoneInterruptDialog == null || !mPhoneInterruptDialog.isShowing()) {
                        mPhoneInterruptDialog = DialogUtils.showAlertDialog(this, getString(R.string.warm_prompt), getString(R.string.phone_interrupt), getString(R.string.i_know));
                    }
                    pauseStream();
                }
                break;
                case LiveEventClass.SystemEvent.EVENT_TYPE_ACTION_NEW_OUTGOING_CALL: {
                    pauseStream();
                }
                break;
                default:
                    break;
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LiveEventClass.ScreenStateEvent event) {
        if (event != null) {
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
    public void onEventMainThread(EngineEventClass.StreamerEvent event) {
        MyLog.w(TAG, "onEventMainThread");
        if (event != null) {
            switch (event.type) {
                case EngineEventClass.StreamerEvent.EVENT_TYPE_OPEN_STREAM_SUCC:
                    //推流成功
                    MyLog.w(TAG, "EVENT_TYPE_OPEN_STREAM_SUCC ");
                    mUIHandler.removeMessages(MSG_START_STREAM);
                    mUIHandler.removeMessages(MSG_START_STREAM_TIMEOUT);
                    mUIHandler.removeMessages(MSG_HIDE_NETWORK_NOT_GOOD_TIPS);
                    mUIHandler.sendEmptyMessage(MSG_HIDE_NETWORK_NOT_GOOD_TIPS);
                    mUIHandler.removeMessages(MSG_HEARTBEAT);
                    mUIHandler.sendEmptyMessage(MSG_HEARTBEAT);
                    mUIHandler.removeMessages(MSG_HEARTBEAT_TIMEOUT);
                    mUIHandler.sendEmptyMessageDelayed(MSG_HEARTBEAT_TIMEOUT, HEARTBEAT_TIMEOUT);
                    mIpSelectionHelper.onPushStreamSuccess();
                    mIpSelectionHelper.updateStutterStatus(false);
                    if (mGameLivePresenter != null) {
                        mGameLivePresenter.updateStutterStatus(false);
                    }
                    break;
                case EngineEventClass.StreamerEvent.EVENT_TYPE_OPEN_CAMERA_FAILED:
                    MyLog.e(TAG, "EVENT_TYPE_OPEN_CAMERA_FAILED");
//                    mUIHandler.sendEmptyMessage(MSG_SHOW_CAMERA_OCCUPY);
                    break;
                case EngineEventClass.StreamerEvent.EVENT_TYPE_OPEN_MIC_FAILED:
                    MyLog.e(TAG, "EVENT_TYPE_OPEN_MIC_FAILED");
                    mUIHandler.sendEmptyMessage(MSG_SHOW_MIC_OCCUPY);
                    break;
                case EngineEventClass.StreamerEvent.EVENT_TYPE_ERROR:
                case EngineEventClass.StreamerEvent.EVENT_TYPE_NEED_RECONNECT: // fall through
                    MyLog.e(TAG, (event.type == EngineEventClass.StreamerEvent.EVENT_TYPE_ERROR ? "EVENT_TYPE_ERROR" : "EVENT_TYPE_NEED_RECONNECT"));
                    VCSessionErrType errType = (VCSessionErrType) event.obj;
                    processReconnect(event.type);
                    break;
                case EngineEventClass.StreamerEvent.EVENT_TYPE_ON_STREAM_CLOSED:
                    MyLog.w(TAG, "EVENT_TYPE_ON_STREAM_CLOSED");
                    break;
                default:
                    break;
            }
        }
    }

    private void processReconnect(int type) {
        if (!mIpSelectionHelper.isStuttering()) {
            mUIHandler.sendEmptyMessage(MSG_SHOW_NETWORK_NOT_GOOD_TIPS);
            mUIHandler.removeMessages(MSG_HIDE_NETWORK_NOT_GOOD_TIPS);
        }
        mUIHandler.removeMessages(MSG_START_STREAM_TIMEOUT);
        mUIHandler.sendEmptyMessageDelayed(MSG_START_STREAM_TIMEOUT, START_STREAM_TIMEOUT);
        mIpSelectionHelper.updateStutterStatus(true);
        if (mGameLivePresenter != null) {
            mGameLivePresenter.updateStutterStatus(true);
        }
        mUIHandler.removeMessages(MSG_START_STREAM);
        startReconnect(type);
    }

    @Override
    protected String getTAG() {
        return "LiveSdkActivity" + "@" + this.hashCode();
    }

    @Override
    public void onDnsReady() {
        MyLog.w(TAG, "onDnsReady");
        if (sRecording) {
            doStartStream();
        }
    }

    @Override
    public void doStartStream() {
        if (mStreamer != null && !mIsStarted) {
            MyLog.w(TAG, "doStartStream");
            ipSelect();
            mUIHandler.removeMessages(MSG_START_STREAM);
            mIsStarted = true;
            mStreamer.startStreamEx(mIpSelectionHelper.getRtmpServerInfos());
        } else {
            MyLog.w(TAG, "doStartStream is ignored, mIsStarted=" + mIsStarted);
        }
    }

    @Override
    public void startStream() {
        if (mIpSelectionHelper.isDnsReady()) {
            MyLog.w(TAG, "startStream");
            doStartStream();
        } else {
            mUIHandler.removeMessages(MSG_START_STREAM);
            mUIHandler.sendEmptyMessageDelayed(MSG_START_STREAM, RECONNECT_TIMEOUT); // 等待onDnsReady事件5秒之后，强制启动
            MyLog.w(TAG, "startStream but dns not ready");
        }
    }

    @Override
    public void stopStream() {
        if (mStreamer != null && mIsStarted) {
            MyLog.w(TAG, "stopStream");
            mStreamer.toggleTorch(false);
            mStreamer.stopStream();
            mIsStarted = false;
            clear();
        } else {
            MyLog.w(TAG, "stopStream is ignored, mIsStarted=" + mIsStarted);
        }
    }

    @Override
    public final boolean ipSelect() {
        return mIpSelectionHelper.ipSelect();
    }

    @Override
    public void startReconnect(int code) {
        if (mStreamer != null) {
            MyLog.w(TAG, "startReconnect, start real reconnect");
            mStreamer.stopStream();
            mIsStarted = false;
            mUIHandler.removeMessages(MSG_START_STREAM);
            if (code == EngineEventClass.StreamerEvent.EVENT_TYPE_ERROR) { // 延迟启动
                mUIHandler.sendEmptyMessageDelayed(MSG_START_STREAM, RECONNECT_TIMEOUT);
            } else {
                mUIHandler.sendEmptyMessage(MSG_START_STREAM);
            }
        }
    }

    @Override
    public void processAction(String action, int errCode, Object... objects) {
        MyLog.w(TAG, "processAction : " + action + " , errCode : " + errCode);
        if (!isFinishing()) {
            switch (action) {
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
                        mMyRoomData.getAvatarTs(), 0, LiveManager.TYPE_LIVE_GAME, 0, "", mMyRoomData.getLocation(), mMyRoomData.getUser(), null, null);
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
                            mMyRoomData.getAvatarTs(), 0, LiveManager.TYPE_LIVE_GAME, 0, "", mMyRoomData.getLocation(), mMyRoomData.getUser(), null, null);
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
        beginLiveToServer();
    }

    //开始动画,并且开始推流
    private void beginLiveToServer() {
        startCountDown();
        mLiveRoomPresenter.beginLiveByAppInfo(mLocation, LiveManager.TYPE_LIVE_GAME, null, true, mLiveTitle,
                "", mMyRoomData.getRoomId(), null, 0, mRoomTag, MiLinkConstant.MY_APP_TYPE, true);

    }

    private void processStartRecord(String liveId, long createTime, String shareUrl,
                                    List<LiveCommonProto.UpStreamUrl> upStreamUrlList, String udpUpstreamUrl) {
        MyLog.w(TAG, "processStartRecord");
        mMyRoomData.setRoomId(liveId);
        RoomInfoGlobalCache.getsInstance().enterCurrentRoom(mMyRoomData.getRoomId());
        mIpSelectionHelper.setOriginalStreamUrl(upStreamUrlList, udpUpstreamUrl);
        startRecord();
        syncSystemMessage();
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
            mHeartbeatService = Executors.newSingleThreadExecutor();
            startStream();
            mLiveOperator.onStartRecord(this, mStreamer, mMyRoomData);
            if (mGameLivePresenter != null) {
                mGameLivePresenter.startGameLive();
            }
            if (mRoomInfoPresenter != null) {
                mRoomInfoPresenter.startLiveCover(mMyRoomData.getUid(), mMyRoomData.getRoomId());
            }
            mUIHandler.sendEmptyMessageDelayed(MSG_START_STREAM_TIMEOUT, START_STREAM_TIMEOUT);
        }
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

    private void stopRecord(String reason) {
        MyLog.w(TAG, "stopRecord = " + sRecording + ",from:" + reason);
        // 如果正在进行voip通话
        if (sRecording) {
            sRecording = false;
            mLiveOperator.onStopRecord();
            endLiveToServer();
            mUIHandler.removeCallbacksAndMessages(null);
            // 防止服务器返回太慢,超时1s
            mUIHandler.sendEmptyMessageDelayed(MSG_END_LIVE, 1000);
            if (mGameLivePresenter != null) {
                mGameLivePresenter.stopGameLive();
            }
            if (mRoomInfoPresenter != null) {
                mRoomInfoPresenter.destroy();
            }
        } else {
            enterEndFragment();
        }
    }

    private void endLiveToServer() {
        mLiveRoomPresenter.endLiveByAppInfo(mMyRoomData.getRoomId(), null);
    }

    private void enterEndFragment() {
        if (mIsLiveEnd) {
            return;
        }
        mIsLiveEnd = true;
        Bundle bundle = EndLiveFragment.getBundle(mMyRoomData.getUid(), mMyRoomData.getRoomId(), mMyRoomData.getAvatarTs(), mMyRoomData.getViewerCnt(), 0, mMyRoomData.getTicket() - mLastTicket, "",
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
    public void onEventMainThread(BaseEvent.UserActionEvent event) {
        MyLog.e(TAG, "LiveEventClass.UserActionEvent");
        if (!mIsForeground) {
            return;
        }
        // 该类型单独提出用指定的fastdoubleclick，防止fragment的崩溃
        if (event.type == BaseEvent.UserActionEvent.EVENT_TYPE_REQUEST_LOOK_USER_INFO) {
            startShowFloatPersonInfo((Long) event.obj1);
            return;
        }
        if (CommonUtils.isFastDoubleClick()) {
            return;
        }
        switch (event.type) {
            case BaseEvent.UserActionEvent.EVENT_TYPE_REQUEST_SET_MANAGER: {
                Bundle bundle = new Bundle();
                bundle.putString(RoomAdminFragment.INTENT_LIVE_ROOM_ID, mMyRoomData.getRoomId());
                bundle.putSerializable(RoomAdminFragment.KEY_ROOM_SEND_MSG_CONFIG, mMyRoomData.getmMsgRule() == null ? new MessageRule() : mMyRoomData.getmMsgRule());
                bundle.putLong(RoomAdminFragment.KEY_ROOM_ANCHOR_ID, mMyRoomData == null ? 0 : mMyRoomData.getUid());
                FragmentNaviUtils.addFragment(LiveSdkActivity.this, R.id.main_act_container, RoomAdminFragment.class, bundle, true, true, true);
            }
            break;
            default:
                break;
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
                case MSG_START_STREAM:
                    MyLog.v(TAG, "MSG_START_STREAM");
                    activity.doStartStream();
                    break;
                case MSG_HEARTBEAT:
                    MyLog.w(TAG, "MSG_HEARTBEAT");
                    if (!activity.mHeartbeatService.isShutdown()) {
                        activity.mHeartbeatService.submit(new Runnable() {
                            @Override
                            public void run() {
                                LiveProto.HeartBeatReq.Builder builder = LiveProto.HeartBeatReq.newBuilder()
                                        .setLiveId(activity.mMyRoomData.getRoomId())
                                        .setStatus(activity.mIsForeground ? 0 : 1);
                                builder.setMicuidStatus(0);
                                PacketData data = new PacketData();
                                data.setCommand(MiLinkCommand.COMMAND_LIVE_HB);
                                data.setData(builder.build().toByteArray());
                                MyLog.v(TAG, "LiveHeartbeat request : \n" + builder.toString());
                                PacketData rspData = MiLinkClientAdapter.getsInstance().sendSync(data, MiLinkConstant.TIME_OUT);
                                if (rspData != null) {
                                    try {
                                        LiveProto.HeartBeatRsp rsp = LiveProto.HeartBeatRsp.parseFrom(rspData.getData());
                                        MyLog.w(TAG, "LiveHeartbeat response : \n" + rsp.toString());
                                        switch (rsp.getRetCode()) {
                                            case ErrorCode.CODE_SUCCESS:
                                                removeMessages(MSG_HEARTBEAT_TIMEOUT);
                                                sendEmptyMessageDelayed(MSG_HEARTBEAT_TIMEOUT, HEARTBEAT_TIMEOUT);
                                                break;
                                            case ErrorCode.CODE_ROOM_NOT_EXIST:
                                                sendEmptyMessage(MSG_ROOM_NOT_EXIT);
                                                break;
                                            default:
                                                break;
                                        }
                                    } catch (InvalidProtocolBufferException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    }
                    removeMessages(MSG_HEARTBEAT);
                    sendEmptyMessageDelayed(MSG_HEARTBEAT, HEARTBEAT_DURATION);
                    break;
                case MSG_HEARTBEAT_TIMEOUT:
                    MyLog.w(TAG, "MSG_HEARTBEAT_TIMEOUT");
                    activity.endLiveUnexpected(R.string.network_offline_warning);
                    break;
                case MSG_START_STREAM_TIMEOUT:
                    MyLog.w(TAG, "MSG_START_STREAM_TIMEOUT");
                    activity.processReconnect(0);
                    break;
                case MSG_ROOM_NOT_EXIT:
                    MyLog.w(TAG, "MSG_ROOM_NOT_EXIT");
                    activity.endLiveUnexpected(R.string.network_offline_warning);
                    break;
                case MSG_START_STREAM_FAILED:
                    MyLog.w(TAG, "MSG_START_STREAM_FAILED");
                    activity.endLiveUnexpected(R.string.start_stream_failure);
                    break;
                case MSG_END_LIVE_FOR_TIMEOUT:
                    activity.stopRecord("MSG_END_LIVE_FOR_TIMEOUT");
                    break;
                case MSG_SHOW_MIC_OCCUPY:
                    DialogUtils.showAlertDialog(activity, activity.getString(R.string.setting_dialog_black_title),
                            activity.getString(R.string.mic_occupy), activity.getString(R.string.ok));
                    break;
            }
        }
    }

    public static void openActivity(BaseSdkActivity activity) {
        Intent intent = new Intent(activity, LiveSdkActivity.class);
        activity.startActivity(intent);
    }
}
