package com.mi.live.data.config;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.NinePatch;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Environment;
import android.support.annotation.AnyThread;
import android.support.annotation.CheckResult;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.MD5;
import com.base.utils.display.DisplayUtils;
import com.base.utils.language.LocaleUtil;
import com.base.view.RoundRectDradable;
import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.live.data.R;
import com.mi.live.data.config.event.DomainListUpdateEvent;
import com.mi.live.data.config.event.SixinWhiteListUpdateEvent;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.live.data.milink.constant.MiLinkConstant;
import com.mi.live.data.preference.PreferenceKeys;
import com.mi.live.data.user.User;
import com.mi.milink.sdk.aidl.PacketData;
import com.mi.milink.sdk.proto.SystemPacketProto;
import com.wali.live.proto.ConfigProto;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by yurui on 3/3/16.
 */
public class GetConfigManager {

    private static final String TAG = "GetConfigManager";

    private static final long INTERVAL = 1000 * 60 * 60 * 5;//检测更新时间10小时
    private static final int MAX_MEDAL_MAP_SIZE = 600 * 1024; //可以存30张

    private static final String CONFIG_VIDEO_RATE = "video_rate";
    private static final String CONFIG_PRIVATE_ROOM_SIZE = "private_room_size";
    private static final String CONFIG_CONVERGED_LEVEL = "converged_level"; //收敛规则中的　等级配置大于10的　不收敛
    private static final String CONFIG_CONVERGED_RANKING = "converged_ranking"; //收敛规则排名信息
    private static final String CONFIG_CONVERGED_INTERVAL = "converged_interval"; //收敛规则中的时间限制
    private static final String CONFIG_CONVERGED_MSG = "converged_msg"; //收敛规则中的弹幕条数
    public static final String CONFIG_CHANNEL_NAME = "channel_name"; //特殊时期在频道列表新加一个特殊的频道
    public static final String CONFIG_CHANNEL_URL = "channel_url"; //特殊时期在频道列表新加一个特殊的频道
    private static final String CONFIG_WALLPAPER_URL = "zhibo_homepage_wall_paper"; //个人主页推荐背景
    private static final String CONFIG_FEEDS_DISPLAY = "feedsChannelDisplay_and"; //动态页面显示
    private static final String CONFIG_LINK_ANCHOR_BADGE_URL = "link_anchor_badge";
    private static final String CONFIG_LINK_GUEST_BADGE_URL = "link_guest_badge";
    private static final String CONFIG_LINE_BITRATE = "line_bitrate";
    private static final String CONFIG_BARRAGE_INTERVAL = "sendmsg_interval"; //发言频率限制
    private static final String CONFIG_KICK_PERMISSION_ANCHOR = "kick_permission_anchor"; //主播是否有踢人权限 0：否 1:是
    private static final String CONFIG_KICK_PERMISSION_ADMIN = "kick_permission_admin"; //管理员是否有踢人权限 0：否 1:是
    private static final String CONFIG_KICK_PERMISSION_TOP1 = "kick_permission_top1"; //榜一是否有踢人权限 0：否 1:是
    private static final String CONFIG_LIVE_SCHEDULE_URL = "live_schedule_url";
    private static final String CONFIG_FOLLOW_POPUP_USER_NUMBER = "follow_popup_user_number";   //关注引导浮层显示的人数限制
    private static final String CONFIG_FOLLOW_POPUP_VIEW_TIME = "follow_popup_view_time";   //关注引导浮层显示时间间隔配置
    private static final String CONFIG_FOLLOW_CANCEL_TIME = "follow_popup_cancel_time"; //关注引导浮层显示时长配置
    private static final String CONFIG_LOOPBACK_AUDIO_MODELS = "loopback_audio_models";
    private static final String CONFIG_USE_FILTER = "use_filter_config";
    private static final String CONFIG_CAMERA_CONNECT_ON = "camera_connect_on";
    private static final String CONFIG_REDNAME_SENDSMS_INTERVAL = "redname_sendmsg_interval"; //红名状态下，发送消息的时间频率限制
    private static final String CONFIG_REGION_TIME = "region_ts";//地区列表时间戳
    private static final String CONFIG_ZHIBO_AND_CONFIG = "zhibo_and_config";
    private static final String CONFIG_SCHEME_HOSTS = "scheme_hosts";
    private static final String CONFIG_WITHDRAW_ENABLE = "isTX";
    private static final String CONFIG_H5_WITHDRAW_ENABLE = "isTXH5";
    private static final String CONFIG_WECHAT_H5_WITHDRAW_URL = "wechatTXUrl";
    private static final String CONFIG_PAYPAL_H5_WITHDRAW_URL = "ppalTXUrl";
    private static final String CONFIG_PERMIT_TIMES = "push_permit_popup_times";
    private static final String CONFIG_PERMIT_POPUP_TIMES_NOTIFY = "push_permit_popup_times_notify";
    private static final String CONFIG_PERMIT_POPUP_TIMES_BACKLIVE = "push_permit_popup_times_backlive";
    private static final String CONFIG_PERMIT_LONG = "push_permit_popup_long";
    private static final String CONFIG_PERMIT_BACKLIVE_LONG = "push_permit_popup_long_backlive";
    private static final String CONFIG_PERMIT_NOTIFY_LONG = "push_permit_popup_long_notify";
    private static final String CONFIG_INDIA_WITHDRAW_ENABLE = "inTxEn";
    private static final String CINFIG_INDIA_WITHDRAW_THRESHOLD = "inTxThreshold";
    private static final String CONFIG_INDIA_WITHDRAW_URL = "inTxUrl";
    private static final String CONFIG_MEDAL_ICON_PREFIXL = "medal_and_icon_prefix";
    private static final String CONFIG_MEDAL_PRO_AND_ICON_PREFIX = "medal_pro_and_icon_prefix";
    private static final String CONFIG_MIBI_RECHARGE_ENABLE = "is_mibi_pay";
    private static final String CONFIG_ROOT_ZHIBO_BIZ = "zhibo_biz";
    private static final String CONFIG_ROOT_ZHIBO_COMMON = "zhibo_comm"; // 和zhibo_biz 同一级
    private static final String CONFIG_STATISTICS_TRIGGER_SUM = "zhibo_ai_trigger_sum"; // MILINK recommend 打点, 累计数量到sum时，开始上传给服务器
    private static final String CONFIG_STATISTICS_TRIGGER_TIME = "zhibo_ai_trigger_time"; // MILINK RECOMMEND 打点 每隔一段时间开始上传给服务器
    private static final String CONFIG_TICKET_EXCHANGE_TYPE = "ticket_exchange_type";//
    private static final String CONFIG_GAME_FOLLOW = "game_follow_start_time";//
    private static final String CONFIG_TICKET_EXCHANGE_NOTICE = "ticket_exchange_notice";//
    private static final String CONFIG_FAN_GROUP_GLOBAL_OPEN = "fg_global_open"; // 群聊是否开放
    private static final String CONFIG_FAN_GROUP_WHITE_LIST = "fg_white_list"; //  开发的白名单
    private static final String CONFIG_SMART_BARRAGE_NUM = "st_barrage"; //  智能弹目配置
    private static final String CONFIG_UPGRADE_FLAG = "upgrade_flag"; // 升级使用哪个系统 1 表示新系统
    private static final String CONFIG_VIP_LEVEL_ICON_URL_PREFIX = "viplevel_icon_url_prefix_and";
    private static final String CONFIG_ENDLIVE_RECOMMEND_TIME = "endlive_recommend_time";//结束页推荐倒计时
    private static final String CONFIG_COMMON_VIP_ENTER_ROOM_MSG = "vip_enter_room_msg";// VIP入场提示语
    private static final String CONFIG_COMMON_IS_EXCHANGE_H5 = "is_exchange_h5"; // 兑换钻石页面是否要使用h5 1表示使用
    private static final String CONFIG_COMMON_EXCHANGE_H5_URL = "exchange_h5_url"; // 跳转钻石重值页面url
    private static final String CONFIG_HOME_PAGE_GUIDE_COIN = "gcoin_guide"; //　未登录时的首页引导红包中的数额

    private static GetConfigManager sInstance;

    private long timestamp;

    private List<LevelItem> lvlDataList;

    private List<LevelItem> lvlDataListLocal;

    private List<CertificationItem> certificationList;

    private List<CertificationItem> certificationListLocal;

    private List<WallPaper> wallPaperUrl;
    private List<Integer> feedsChannle = new ArrayList<>();

    private Set<String> mHosts; //webviewactiivity能吊起的host

    private Drawable mAnchorBadge;
    private Drawable mGuestBadge;

    private List<String> mLoopBacksModelList;//支持loopBack的手机机型。
    private boolean mFilterFollowClient = false;
    private boolean mLinkDeviceOn = false;
    private Drawable drawable;

    public long getRegionTime() {
        return mRegionTime;
    }

    private long mRegionTime = 0;

    // webview的白名单url，防劫持
    private Set<String> whiteListUrlForWebView;
    // 提现配置
    private volatile WithdrawConfig mWithdrawConfig = new WithdrawConfig();
    private volatile IndiaWithdrawConfig mIndiaWithdrawConfig;
    private volatile boolean mMibiRechargeEnable;

    private String medalAndIconPrefix;//直播间弹幕勋章前缀以及直播间头像浅醉
    private String medalAndProIconPrefix;//个人详情页前缀
    private volatile SmartBarrageTime mSmartBarrageTime = new SmartBarrageTime();
    private volatile String mVipLevelIconUrlPrefix;// VIP等级图片地址前缀
    private Map<String, List<String>> mLanguage2WelcomeVipEnterRoomTipsMap = new HashMap<>();//TODO 一写多读，先写后读

    private GetConfigManager() {
        lvlDataList = new ArrayList<>();
        lvlDataListLocal = new ArrayList<>();
        certificationList = new ArrayList<>();
        certificationListLocal = new ArrayList<>();
        wallPaperUrl = new ArrayList<>();
        whiteListUrlForWebView = new HashSet<>();
        whiteListUrlForWebView.add(".mi.com");
        whiteListUrlForWebView.add(".xiaomi.com");
        timestamp = PreferenceUtils.getSettingLong(PreferenceKeys.PREFERENCE_KEY_CONFIG_TIMESTAMP, 0);
        MyLog.d(TAG, "PREFERENCE_KEY_CONFIG_TIMESTAMP time : " + timestamp);
        mLoopBacksModelList = new ArrayList<>();

        mHosts = new HashSet<>(5);
        mHosts.add("migamecenter");
        mHosts.add("walilive");
        mHosts.add("tmall");

        loadLocalResource();
    }

    public static synchronized GetConfigManager getInstance() {
        boolean firstIn = false;
        if (sInstance == null) {
            synchronized (GetConfigManager.class) {
                if(sInstance==null) {
                    firstIn = true;
                    sInstance = new GetConfigManager();
                }
            }
        }
        long timediff = Math.abs(System.currentTimeMillis() - sInstance.timestamp);
        MyLog.d(TAG, "timediff: " + timediff + "Interval: " + INTERVAL);
        if (timediff > INTERVAL) {
            sInstance.getConfig();
        } else if (firstIn) {
            if (PreferenceUtils.hasKey(GlobalData.app(), PreferenceKeys.PREFERENCE_KEY_CONFIG_JSON)) {
                sInstance.parseJsonConfig(PreferenceUtils.getSettingString(GlobalData.app(), PreferenceKeys.PREFERENCE_KEY_CONFIG_JSON, ""));
            } else {
                sInstance.getConfig();
            }
        }
        return sInstance;
    }

    public static synchronized void reload() {
        MyLog.e(TAG, "reload config");
        if (sInstance == null) {
            sInstance = new GetConfigManager();
        }
        sInstance.timestamp = 0;
        sInstance.getConfig();
    }

    /**
     * 为避免在服务器主从库同步期间拉取到不正确的配置，采用延迟的方法
     *
     * @param delay 单位：毫秒
     */
    @AnyThread
    public static void reload(final int delay) {
        Observable.just(null)
                .observeOn(Schedulers.io())
                .subscribe(new Action1<Object>() {
                    @Override
                    public void call(Object o) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(delay);
                        } catch (InterruptedException e) {
                            MyLog.e(TAG, e);
                        }
                        reload();
                    }
                });
    }

    public Drawable getAnchorBadge() {
        return mAnchorBadge;
    }

    public Drawable getGuestBadge() {
        return mGuestBadge;
    }

    public boolean isFilterFollowClient() {
        return mFilterFollowClient;
    }

    public boolean isLinkDeviceOn() {
        return mLinkDeviceOn;
    }

    public List<String> getLoopBacksModelList() {
        return mLoopBacksModelList;
    }

    public Set<String> getWhiteListUrlForWebView() {
        return whiteListUrlForWebView;
    }

    public CertificationItem getCertificationTypeDrawable(int certificationType, boolean needNoCache) {
        if (needNoCache) {
            for (CertificationItem item : certificationList) {
                if (certificationType == item.certificationType) {
                    return item;
                }
            }
        }
        return getCertificationTypeDrawableLocal(certificationType, needNoCache);
    }

    public CertificationItem getCertificationTypeDrawable(int certificationType) {
        return getCertificationTypeDrawable(certificationType, true);
    }

    private CertificationItem getCertificationTypeDrawableLocal(int certificationType, boolean needNoCache) {
        CertificationItem defaultItem = null;
        if (!needNoCache) {
            for (CertificationItem item : certificationListLocal) {
                if (certificationType == item.certificationType) {
                    return item;
                } else if (item.certificationType == -1) {
                    defaultItem = item;
                }
            }
        }
        if (defaultItem == null) {
            defaultItem = new CertificationItem();
            if (certificationType == User.CERTIFICATION_TV) {
                defaultItem.certificationType = User.CERTIFICATION_TV;
                defaultItem.certificationDrawable = GlobalData.app().getResources().getDrawable(R.drawable.certification_type_6);
                defaultItem.certificationDrawableLiveComment = GlobalData.app().getResources().getDrawable(R.drawable.certification_type_6);
            } else {
                defaultItem.certificationType = -1;
                defaultItem.certificationDrawable = GlobalData.app().getResources().getDrawable(R.drawable.certification_type_1_3);
                defaultItem.certificationDrawableLiveComment = GlobalData.app().getResources().getDrawable(R.drawable.certification_type_1_3);
            }

            defaultItem.certificationDrawableLiveComment.setBounds(10, 0, DisplayUtils.dip2px(20) + 10, DisplayUtils.dip2px(16));
            if (!needNoCache) {
                certificationListLocal.add(defaultItem);
            }
        }
        return defaultItem;
    }

    public Drawable getLevelSmallDrawable(int lvl) {
        for (LevelItem item : lvlDataList) {
            if (lvl >= item.min && lvl <= item.max) {
                if (item.drawableBadge != null) {
                    return item.drawableBadge;
                } else {
                    break;
                }
            }
        }
        return getLevelSmallDrawableLocal(lvl);
    }

    private Drawable getLevelSmallDrawableLocal(int lvl) {
        for (LevelItem item : lvlDataListLocal) {
            if (lvl >= item.min && lvl <= item.max) {
                return item.drawableBadge;
            }
        }
        return GlobalData.app().getResources().getDrawable(R.drawable.lv_1_circular);
    }

    public LevelItem getLevelItem(int lvl) {
        for (LevelItem item : lvlDataList) {
            if (lvl >= item.min && lvl <= item.max) {
                if (!item.isDrawablEmpty()) {
                    return item;
                } else {
                    break;
                }
            }
        }
        return getLevelItemLocal(lvl);
    }

    private LevelItem getLevelItemLocal(int lvl) {
        LevelItem itemDefault = null;
        for (LevelItem item : lvlDataListLocal) {
            if (lvl >= item.min && lvl <= item.max) {
                return item;
            } else if (item.max == -1) {
                itemDefault = item;
            }
        }
        if (itemDefault == null) {
            itemDefault = new LevelItem();
            itemDefault.min = -1;
            itemDefault.max = -1;
            itemDefault.drawableBadge = GlobalData.app().getResources().getDrawable(R.drawable.lv_1_circular);
            itemDefault.drawableLevel = GlobalData.app().getResources().getDrawable(R.drawable.lv_1);
            itemDefault.drawableLevel.setBounds(0, 0, DisplayUtils.dip2px(12), DisplayUtils.dip2px(12));
            int color = Color.parseColor("#58d4eb");
            itemDefault.drawableBG = new RoundRectDradable(color);
            lvlDataListLocal.add(itemDefault);
        }
        return itemDefault;
    }


    private Subscription mGetConfigSubscription;

    public void getConfig() {
        if (mGetConfigSubscription != null && !mGetConfigSubscription.isUnsubscribed()) {
            return;
        }
        mGetConfigSubscription = Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {
                ConfigProto.MiLinkGetConfigReq request = ConfigProto.MiLinkGetConfigReq.newBuilder().setTimeStamp(0).build();
                PacketData packetData = new PacketData();
                packetData.setCommand(MiLinkCommand.COMMAND_GET_CONFIG);
                packetData.setData(request.toByteArray());
                PacketData responseData = MiLinkClientAdapter.getsInstance().sendSync(packetData, MiLinkConstant.TIME_OUT);
                MyLog.v(TAG + "getConfig:" + responseData);
                try {
                    if (responseData != null) {
                        SystemPacketProto.MiLinkGetConfigRsp response = SystemPacketProto.MiLinkGetConfigRsp.parseFrom(responseData.getData());
                        MyLog.w(TAG + "getConfig result:" + response.getTimeStamp() + " " + response.getJsonConfig());
                        if (response.getTimeStamp() != 0 && !TextUtils.isEmpty(response.getJsonConfig())) {
                            sInstance.timestamp = System.currentTimeMillis();
                            PreferenceUtils.setSettingLong(PreferenceKeys.PREFERENCE_KEY_CONFIG_TIMESTAMP, sInstance.timestamp);
                            if (PreferenceUtils.hasKey(GlobalData.app(), PreferenceKeys.PREFERENCE_KEY_CONFIG_JSON) && !response.getJsonConfig().equals(PreferenceUtils.getSettingString(GlobalData.app(), PreferenceKeys.PREFERENCE_KEY_CONFIG_JSON, ""))) {
                                clearFile();
                            }
                            parseJsonConfig(response.getJsonConfig());
                            PreferenceUtils.setSettingString(GlobalData.app(), PreferenceKeys.PREFERENCE_KEY_CONFIG_JSON, response.getJsonConfig());
                        }
                    }
                } catch (InvalidProtocolBufferException e) {
                    MyLog.e(e);
                }
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onCompleted() {
                        mGetConfigSubscription = null;
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Object o) {

                    }
                });
    }

    private Subscription mParseJsonConfigSubscription;

    public void parseJsonConfig(final String str) {
        MyLog.i(TAG, "parseJsonConfig : " + str);
        if (mParseJsonConfigSubscription != null && !mParseJsonConfigSubscription.isUnsubscribed()) {
            return;
        }
        Observable.create(new Observable.OnSubscribe<List<LevelItem>>() {
            @Override
            public void call(Subscriber<? super List<LevelItem>> subscriber) {
                long start = System.currentTimeMillis();
                int level = 0;
                List<LevelItem> data = new ArrayList<>();
                List<CertificationItem> dataCertification = new ArrayList<>();
                try {
                    JSONObject root = new JSONObject(str);
                    if (root.has(CONFIG_ROOT_ZHIBO_BIZ)) {
                        try {
                            JSONObject disStr = root.getJSONObject(CONFIG_ROOT_ZHIBO_BIZ);
                            try {
                                if (disStr.has(CONFIG_PERMIT_TIMES)) {
                                    int times = disStr.getInt(CONFIG_PERMIT_TIMES);
                                    long time = disStr.getLong(CONFIG_PERMIT_LONG);
                                    PreferenceUtils.setSettingLong(PreferenceKeys.PRE_KEY_PERMIT_LONG, time);
                                    PreferenceUtils.setSettingInt(PreferenceKeys.PRE_KEY_PERMIT_TIMES, times);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (disStr.has(CONFIG_PERMIT_POPUP_TIMES_NOTIFY)) {
                                    int times = disStr.getInt(CONFIG_PERMIT_POPUP_TIMES_NOTIFY);
                                    PreferenceUtils.setSettingInt(PreferenceKeys.PRE_KEY_PERMIT_TIMES_NOTIFY, times);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (disStr.has(CONFIG_PERMIT_POPUP_TIMES_BACKLIVE)) {
                                    int times = disStr.getInt(CONFIG_PERMIT_POPUP_TIMES_BACKLIVE);
                                    PreferenceUtils.setSettingInt(PreferenceKeys.PER_KEY_PERMIT_TIMES_BACKLIVE, times);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (disStr.has(CONFIG_PERMIT_BACKLIVE_LONG)) {
                                    long time = disStr.getLong(CONFIG_PERMIT_BACKLIVE_LONG);
                                    PreferenceUtils.setSettingLong(PreferenceKeys.PER_KEY_PERMIT_LONG_BACKLIVE, time);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (disStr.has(CONFIG_PERMIT_NOTIFY_LONG)) {
                                    long time = disStr.getLong(CONFIG_PERMIT_NOTIFY_LONG);
                                    PreferenceUtils.setSettingLong(PreferenceKeys.PER_KEY_PERMIT_LONG_NOTIFY, time);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (disStr.has(CONFIG_TICKET_EXCHANGE_TYPE)) {
                                    int type = disStr.getInt(CONFIG_TICKET_EXCHANGE_TYPE);
                                    PreferenceUtils.setSettingInt(PreferenceKeys.PER_KEY_PERMIT_LONG_NOTIFY, type);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (disStr.has(CONFIG_TICKET_EXCHANGE_NOTICE)) {
                                    int type = disStr.getInt(CONFIG_TICKET_EXCHANGE_NOTICE);
                                    PreferenceUtils.setSettingInt(PreferenceKeys.PER_KEY_PERMIT_LONG_NOTIFY, type);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            try {
                                if (disStr.has("display_level")) {
                                    level = disStr.getInt("display_level");
                                    JSONObject iconMap = root.getJSONObject("zhibo_level_icon_map_and_v2");
                                    for (int i = 1; i <= level; i++) {
                                        if (iconMap.has("level_icon_map_" + i)) {
                                            LevelItem item = new LevelItem();
                                            String[] itemStr = iconMap.getString("level_icon_map_" + i).split(",");
                                            try {
                                                item.min = Integer.parseInt(itemStr[0]);
                                                item.max = Integer.parseInt(itemStr[1]);
                                                item.drawableBadge = getDrawableFromServer(itemStr[2], true);
                                                item.drawableLevel = getDrawableFromServer(itemStr[3], true);
//                                            if (item.max >= 100) {
                                                item.drawableLevel.setBounds(0, 0, DisplayUtils.dip2px(12), DisplayUtils.dip2px(12));
//                                            } else if (item.max >= 10) {
//                                                item.drawableLevel.setBounds(0, 0, DisplayUtils.dip2px(13.5f), DisplayUtils.dip2px(12));
//                                            } else {
//                                                item.drawableLevel.setBounds(0, 0, DisplayUtils.dip2px(16), DisplayUtils.dip2px(12));
//                                            }
                                                item.drawableBG = new RoundRectDradable(Color.parseColor(itemStr[4]));
                                                data.add(item);
                                                MyLog.v(TAG + " (" + level + ") " + itemStr[0] + " " + itemStr[1] + " " + itemStr[2] + " " + itemStr[3] + " " + itemStr[4]);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            } catch (JSONException e) {
                                MyLog.e(TAG, "display_level：" + e);
                            }
                            if (disStr.has(CONFIG_REGION_TIME)) {
                                try {
                                    //camera_connect_on＝1是开，0关闭
                                    long time = disStr.getLong(CONFIG_REGION_TIME);
                                    mRegionTime = time;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            if (disStr.has(CONFIG_CAMERA_CONNECT_ON)) {
                                try {
                                    //camera_connect_on＝1是开，0关闭
                                    String linkDeviceOn = disStr.getString(CONFIG_CAMERA_CONNECT_ON);
                                    if (!TextUtils.isEmpty(linkDeviceOn)) {
                                        MyLog.w(TAG, "linkDeviceOn=" + linkDeviceOn);
                                        mLinkDeviceOn = Integer.parseInt(linkDeviceOn) == 1;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            if (disStr.has(CONFIG_USE_FILTER)) {
                                try {
                                    String useFilter = disStr.getString(CONFIG_USE_FILTER);
                                    if (!TextUtils.isEmpty(useFilter)) {
                                        MyLog.w(TAG, "useFilter=" + useFilter);
                                        // mUseFilter true 表示用服務器的參數。
                                        mFilterFollowClient = (Integer.parseInt(useFilter) == 0);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            if (disStr.has(CONFIG_LOOPBACK_AUDIO_MODELS)) {
                                try {
                                    //loopback_audio_models, value 为 model列表，逗号分隔
                                    String phoneModels = disStr.getString(CONFIG_LOOPBACK_AUDIO_MODELS);
                                    if (!TextUtils.isEmpty(phoneModels)) {
                                        String[] phoneModelList = phoneModels.split(",");
                                        for (String s : phoneModelList) {
                                            mLoopBacksModelList.add(s);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            if (disStr.has(CONFIG_LINK_ANCHOR_BADGE_URL)) {
                                try {
                                    String linkAnchorBadgeUrl = disStr.getString(CONFIG_LINK_ANCHOR_BADGE_URL);
                                    mAnchorBadge = getDrawableFromServer(linkAnchorBadgeUrl, true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            if (disStr.has(CONFIG_LINK_GUEST_BADGE_URL)) {
                                try {
                                    String linkGuestBadgeUrl = disStr.getString(CONFIG_LINK_GUEST_BADGE_URL);
                                    mGuestBadge = getDrawableFromServer(linkGuestBadgeUrl, true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            if (disStr.has(CONFIG_VIDEO_RATE)) {
                                String videoRate = disStr.getString(CONFIG_VIDEO_RATE);
                                PreferenceUtils.setSettingString(GlobalData.app(), PreferenceKeys.PREF_KEY_VIDEO_RATE, videoRate);
                            }
                            if (disStr.has(CONFIG_PRIVATE_ROOM_SIZE)) {
                                int n = disStr.getInt(CONFIG_PRIVATE_ROOM_SIZE);
                                if (n > 0) {
                                    PreferenceUtils.setSettingString(GlobalData.app(), PreferenceKeys.PREFERENCE_KEY_PRIVATE_ROOM_SIZE, n + "");
                                }
                            }

                            // 提现配置
                            boolean h5WithdrawEnable = disStr.optInt(CONFIG_H5_WITHDRAW_ENABLE) != 0;
                            String weChatH5WithdrawUrl = disStr.optString(CONFIG_WECHAT_H5_WITHDRAW_URL, null);
                            String payPalH5WithdrawUrl = disStr.optString(CONFIG_PAYPAL_H5_WITHDRAW_URL, null);
                            mWithdrawConfig = new WithdrawConfig(h5WithdrawEnable, weChatH5WithdrawUrl, payPalH5WithdrawUrl);

                            // 首页金币红包引导数额
                            if (disStr.has(CONFIG_HOME_PAGE_GUIDE_COIN)) {
                                float guideCoin = Float.parseFloat(disStr.getString(CONFIG_HOME_PAGE_GUIDE_COIN));
                                PreferenceUtils.setSettingFloat(GlobalData.app(), PreferenceKeys.PRE_KEY_HOME_PAGE_GUIDE_COIN, guideCoin);
                            }


                            boolean indiaWithdrawEnable = disStr.optInt(CONFIG_INDIA_WITHDRAW_ENABLE) != 0;
                            int indiaWithdrawThreshold = Math.max(disStr.optInt(CINFIG_INDIA_WITHDRAW_THRESHOLD), 0);
                            String indiaWithdrawUrl = disStr.optString(CONFIG_INDIA_WITHDRAW_URL);
                            mIndiaWithdrawConfig = new IndiaWithdrawConfig(indiaWithdrawEnable, indiaWithdrawThreshold, indiaWithdrawUrl);

                            mMibiRechargeEnable = disStr.optInt(CONFIG_MIBI_RECHARGE_ENABLE) != 0;

                            StringBuilder convergedData = new StringBuilder();
                            convergedData.append(disStr.optInt(CONFIG_CONVERGED_LEVEL, 10)).append("_").append(disStr.optInt(CONFIG_CONVERGED_RANKING, 10)).append("_")
                                    .append(disStr.optInt(CONFIG_CONVERGED_INTERVAL, 5)).append("_").append(disStr.optInt(CONFIG_CONVERGED_MSG, 1));
                            PreferenceUtils.setSettingString(GlobalData.app(), PreferenceKeys.PREF_KEY_CONVERGED, convergedData.toString());
                            try {

                                // 提取下发的端口信息
                                List<String> domainPortList = new ArrayList<>();
                                if (disStr.has("pull_domain")) {
                                    String pDomain = disStr.getString("pull_domain");
                                    if (!TextUtils.isEmpty(pDomain)) {
                                        String[] domainPorts = pDomain.split(";");
                                        for (String str : domainPorts) {
                                            domainPortList.add(str);
                                        }
                                    }
                                }
                                if (disStr.has("push_domain")) {
                                    String pDomain = disStr.getString("push_domain");
                                    if (!TextUtils.isEmpty(pDomain)) {
                                        String[] domainPorts = pDomain.split(";");
                                        for (String str : domainPorts) {
                                            domainPortList.add(str);
                                        }
                                    }
                                }
                                EventBus.getDefault().post(new DomainListUpdateEvent(domainPortList));
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if (disStr.has("sys_account")) {
                                String oldAccount = PreferenceUtils.getSettingString(GlobalData.app(), PreferenceKeys.PRE_KEY_SIXIN_SYSTEM_SERVICE_WHITE_LIST, "");
                                if (TextUtils.isEmpty(oldAccount) || !oldAccount.equals(disStr.optString("sys_account"))) {
                                    PreferenceUtils.setSettingString(GlobalData.app(), PreferenceKeys.PRE_KEY_SIXIN_SYSTEM_SERVICE_WHITE_LIST, disStr.getString("sys_account"));
                                    EventBus.getDefault().post(new SixinWhiteListUpdateEvent(disStr.optString("sys_account")));
                                }
                            }
                            try {
                                if (disStr.has(CONFIG_LINE_BITRATE)) {
                                    String lineBitrate = disStr.getString(CONFIG_LINE_BITRATE);
                                    try {
                                        PreferenceUtils.setSettingInt(PreferenceKeys.PREF_KEY_LINE_BITRATE, Integer.parseInt(lineBitrate));
                                    } catch (NumberFormatException e) {
                                        MyLog.e(e);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (disStr.has("webview_hosts")) {
                                if (whiteListUrlForWebView == null) {
                                    whiteListUrlForWebView = new HashSet<>();
                                } else {
                                    whiteListUrlForWebView.clear();
                                }
                                String[] content = disStr.getString("webview_hosts").split(";");
                                for (String str : content) {
                                    whiteListUrlForWebView.add(str);
                                }
                            }
//                            if (disStr.has(CONFIG_CHANNEL_NAME) && disStr.has(CONFIG_CHANNEL_URL)) {
//                                String channelName = disStr.getString(CONFIG_CHANNEL_NAME);
//                                String channelUrl = disStr.getString(CONFIG_CHANNEL_URL);
//                                PreferenceUtils.setSettingString(GlobalData.app(), CONFIG_CHANNEL_NAME, channelName);
//                                PreferenceUtils.setSettingString(GlobalData.app(), CONFIG_CHANNEL_URL, channelUrl);
//                                if (!TextUtils.isEmpty(channelName) && !TextUtils.isEmpty(channelUrl)) {
//                                    ChannelShow channelShow = new ChannelShow(channelName, channelUrl);
//                                    EventBus.getDefault().post(new EventClass.SpecialChannelEvent(channelShow));
//                                } else {
//                                    PreferenceUtils.setSettingString(GlobalData.app(), CONFIG_CHANNEL_NAME, "");
//                                    PreferenceUtils.setSettingString(GlobalData.app(), CONFIG_CHANNEL_URL, "");
//                                    EventBus.getDefault().post(new EventClass.SpecialChannelEvent(null));
//                                }
//                            } else {
//                                //删除这个频道
//                                PreferenceUtils.setSettingString(GlobalData.app(), CONFIG_CHANNEL_NAME, "");
//                                PreferenceUtils.setSettingString(GlobalData.app(), CONFIG_CHANNEL_URL, "");
//                                EventBus.getDefault().post(new EventClass.SpecialChannelEvent(null));
//                            }
                            try {
                                if (disStr.has(CONFIG_BARRAGE_INTERVAL)) {
                                    String sendBarrageInterval = disStr.getString(CONFIG_BARRAGE_INTERVAL);
                                    if (!TextUtils.isEmpty(sendBarrageInterval))
                                        PreferenceUtils.setSettingString(GlobalData.app(), PreferenceKeys.PRE_KEY_SEND_BARRAGE_INTERVAL, sendBarrageInterval);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (disStr.has(CONFIG_KICK_PERMISSION_ANCHOR)) {
                                    String permission = disStr.getString(CONFIG_KICK_PERMISSION_ANCHOR);
                                    if (!TextUtils.isEmpty(permission) && TextUtils.isDigitsOnly(permission)) {
                                        PreferenceUtils.setSettingInt(PreferenceKeys.PRE_KIK_PERMISSION_ANCHOR, Integer.valueOf(permission));
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (disStr.has(CONFIG_KICK_PERMISSION_ADMIN)) {
                                    String permission = disStr.getString(CONFIG_KICK_PERMISSION_ADMIN);
                                    if (!TextUtils.isEmpty(permission) && TextUtils.isDigitsOnly(permission)) {
                                        PreferenceUtils.setSettingInt(PreferenceKeys.PRE_KIK_PERMISSION_ADMIN, Integer.valueOf(permission));
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (disStr.has(CONFIG_KICK_PERMISSION_TOP1)) {
                                    String permission = disStr.getString(CONFIG_KICK_PERMISSION_TOP1);
                                    if (!TextUtils.isEmpty(permission) && TextUtils.isDigitsOnly(permission)) {
                                        PreferenceUtils.setSettingInt(PreferenceKeys.PRE_KIK_PERMISSION_TOP1, Integer.valueOf(permission));
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (disStr.has(CONFIG_LIVE_SCHEDULE_URL)) {
                                    String url = disStr.getString(CONFIG_LIVE_SCHEDULE_URL);
                                    if (!TextUtils.isEmpty(url)) {
                                        PreferenceUtils.setSettingString(GlobalData.app(), PreferenceKeys.PREF_LIVE_SCHEDULE_URL, url);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (disStr.has(CONFIG_FOLLOW_POPUP_USER_NUMBER)) {
                                    String userNum = disStr.getString(CONFIG_FOLLOW_POPUP_USER_NUMBER);
                                    if (!TextUtils.isEmpty(userNum) && TextUtils.isDigitsOnly(userNum)) {
                                        PreferenceUtils.setSettingInt(PreferenceKeys.PRE_FOLLOW_POPUP_USER_NUMBER, Integer.valueOf(userNum));
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (disStr.has(CONFIG_FOLLOW_POPUP_VIEW_TIME)) {
                                    String viewTime = disStr.getString(CONFIG_FOLLOW_POPUP_VIEW_TIME);
                                    if (!TextUtils.isEmpty(viewTime) && TextUtils.isDigitsOnly(viewTime)) {
                                        PreferenceUtils.setSettingInt(PreferenceKeys.PRE_FOLLOW_POPUP_VIEW_TIME, Integer.valueOf(viewTime));
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (disStr.has(CONFIG_FOLLOW_CANCEL_TIME)) {
                                    String cancelTime = disStr.getString(CONFIG_FOLLOW_CANCEL_TIME);
                                    if (!TextUtils.isEmpty(cancelTime) && TextUtils.isDigitsOnly(cancelTime)) {
                                        PreferenceUtils.setSettingInt(PreferenceKeys.PRE_FOLLOW_CANCEL_TIME, Integer.valueOf(cancelTime));
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (disStr.has(CONFIG_REDNAME_SENDSMS_INTERVAL)) {
                                    String interval = disStr.getString(CONFIG_REDNAME_SENDSMS_INTERVAL);
                                    if (!TextUtils.isEmpty(interval)) {
                                        PreferenceUtils.setSettingString(GlobalData.app(), PreferenceKeys.PRE_KEY_REDNAM_SENDSMS_INTERVAL, interval);
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (disStr.has(CONFIG_MEDAL_ICON_PREFIXL)) {
                                    medalAndIconPrefix = disStr.getString(CONFIG_MEDAL_ICON_PREFIXL);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (disStr.has(CONFIG_MEDAL_PRO_AND_ICON_PREFIX)) {
                                    medalAndProIconPrefix = disStr.getString(CONFIG_MEDAL_PRO_AND_ICON_PREFIX);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        if (root.has(CONFIG_ZHIBO_AND_CONFIG)) {
                            JSONObject disStr = root.getJSONObject(CONFIG_ZHIBO_AND_CONFIG);
                            if (disStr.has(CONFIG_SCHEME_HOSTS)) {
                                String schemeHosts = disStr.getString(CONFIG_SCHEME_HOSTS);
                                if (!TextUtils.isEmpty(schemeHosts)) {
                                    PreferenceUtils.setSettingString(GlobalData.app(), CONFIG_SCHEME_HOSTS, schemeHosts);
                                    parseHostsData();
                                }
                            }
                        }
                    } catch (JSONException e) {
                        MyLog.e(e);
                    }

                    try {
                        if (root.has("icon_vip_user_badge_and")) {
                            JSONObject disStr = root.getJSONObject("icon_vip_user_badge_and");
                            Iterator<String> keys = disStr.keys();
                            while (keys.hasNext()) {
                                String key = keys.next();
                                CertificationItem item = new CertificationItem();
                                try {
                                    MyLog.v(TAG + " (" + key + ") " + disStr.getString(key));
                                    item.certificationType = Integer.parseInt(key);
                                    item.certificationDrawable = getDrawableFromServer(disStr.getString(key), true);
                                    item.certificationDrawableLiveComment = getDrawableFromServer(disStr.getString(key), true);
                                    item.certificationDrawableLiveComment.setBounds(10, 0, DisplayUtils.dip2px(20) + 10, DisplayUtils.dip2px(16));
                                    dataCertification.add(item);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (root.has(CONFIG_WALLPAPER_URL)) {
                            JSONObject wallStr = root.getJSONObject(CONFIG_WALLPAPER_URL);
                            Iterator<String> keys = wallStr.keys();
                            while (keys.hasNext()) {
                                Integer key = Integer.parseInt(keys.next());
                                MyLog.v(TAG + " wall (" + key + ") " + wallStr.getString(String.valueOf(key)));
                                WallPaper wallPaper = new WallPaper(key, wallStr.getString(String.valueOf(key)));
                                if (wallPaperUrl.contains(wallPaper)) {
                                    wallPaperUrl.remove(wallPaper);
                                }
                                wallPaperUrl.add(wallPaper);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (root.has(CONFIG_FEEDS_DISPLAY)) {
                            JSONObject wallStr = root.getJSONObject(CONFIG_FEEDS_DISPLAY);

                            String value = wallStr.getString(LocaleUtil.getLanguageCode());

                            if (!TextUtils.isEmpty(value)) {
                                String[] channel = value.split(",");

                                if (!feedsChannle.isEmpty()) {
                                    feedsChannle.clear();
                                }

                                for (int i = 0; i < channel.length; i++) {
                                    feedsChannle.add(Integer.parseInt(channel[i]));
                                }
                            }

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (root.has(CONFIG_ROOT_ZHIBO_COMMON)) {
                            JSONObject comm = root.getJSONObject(CONFIG_ROOT_ZHIBO_COMMON);
                            try {
                                if (comm.has(CONFIG_TICKET_EXCHANGE_TYPE)) {
                                    int type = comm.getInt(CONFIG_TICKET_EXCHANGE_TYPE);
                                    PreferenceUtils.setSettingInt(PreferenceKeys.PER_KEY_TICKET_EXCHANGE_TYPE, type);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (comm.has(CONFIG_GAME_FOLLOW)) {
                                    int type = comm.getInt(CONFIG_GAME_FOLLOW);
                                    PreferenceUtils.setSettingInt(PreferenceKeys.PER_KEY_GAME_SHOW_TIME, type);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            try {
                                if (comm.has(CONFIG_TICKET_EXCHANGE_NOTICE)) {
                                    int type = comm.getInt(CONFIG_TICKET_EXCHANGE_NOTICE);
                                    PreferenceUtils.setSettingInt(PreferenceKeys.PER_KEY_TICKET_EXCHANGE_NOTICE, type);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            try {
                                if (comm.has(CONFIG_ENDLIVE_RECOMMEND_TIME)) {
                                    int time = comm.getInt(CONFIG_ENDLIVE_RECOMMEND_TIME);
                                    PreferenceUtils.setSettingInt(PreferenceKeys.PER_KEY_ENDLIVE_RECOMMEND_TIME, time);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            String time = comm.getString(CONFIG_STATISTICS_TRIGGER_TIME);
                            String sum = comm.getString(CONFIG_STATISTICS_TRIGGER_SUM);

                            if (!TextUtils.isEmpty(time)) {
                                PreferenceUtils.setSettingString(GlobalData.app(), PreferenceKeys.PRE_KEY_STATISTICS_TRIGGER_TIME, time);
                            }
                            if (!TextUtils.isEmpty(sum)) {
                                PreferenceUtils.setSettingString(GlobalData.app(), PreferenceKeys.PRE_KEY_STATISTICS_TRIGGER_SUM, sum);
                            }

                            MyLog.d("白名单 传过来的是" + comm.optInt(CONFIG_FAN_GROUP_GLOBAL_OPEN));
                            PreferenceUtils.setSettingInt(PreferenceKeys.PRE_KEY_FANS_GROUP_GLOBAL_OPEN, comm.optInt(CONFIG_FAN_GROUP_GLOBAL_OPEN));

                            if (!TextUtils.isEmpty(comm.getString(CONFIG_FAN_GROUP_WHITE_LIST))) {
                                PreferenceUtils.setSettingString(GlobalData.app(), PreferenceKeys.PRE_KEY_FANS_GROUP_WHITE_LIST, comm.getString(CONFIG_FAN_GROUP_WHITE_LIST));
                            }

                            int upgradeFlag = comm.getInt(CONFIG_UPGRADE_FLAG);
                            PreferenceUtils.setSettingInt(PreferenceKeys.PRE_KEY_UPGRADE_FLAG, upgradeFlag);

                            int exchangeUseH5 = comm.getInt(CONFIG_COMMON_IS_EXCHANGE_H5);
                            PreferenceUtils.setSettingInt(PreferenceKeys.PRE_KEY_EXCHANGE_USE_H5_FLAG, exchangeUseH5);

                            String exchangeH5Url = comm.getString(CONFIG_COMMON_EXCHANGE_H5_URL);
                            PreferenceUtils.setSettingString(GlobalData.app(), PreferenceKeys.PRE_KEY_EXCHANGE_H5_URL, exchangeH5Url);
                            MyLog.d(TAG + "exchangeH5 flag : " + exchangeUseH5 + " url: " + exchangeH5Url);
                            try {
                                if (comm.has(CONFIG_SMART_BARRAGE_NUM)) {
                                    String disStr = comm.getString(CONFIG_SMART_BARRAGE_NUM);
                                    String str[] = disStr.split(";");
                                    HashMap<String, String> map = new HashMap<>();
                                    mSmartBarrageTime = new SmartBarrageTime();
                                    for (int i = 0; i < str.length; i++) {
                                        String keyValue[] = str[i].split(":");
                                        map.put(keyValue[0], keyValue[1]);
                                    }

                                    mSmartBarrageTime.gbitl = Integer.parseInt(map.get("gbitl"));
                                    mSmartBarrageTime.seitl = Integer.parseInt(map.get("seitl"));
                                    mSmartBarrageTime.olvcot = Integer.parseInt(map.get("olvcot"));
                                    mSmartBarrageTime.tlvcot = Integer.parseInt(map.get("tlvcot"));
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (comm.has(CONFIG_VIP_LEVEL_ICON_URL_PREFIX)) {
                                mVipLevelIconUrlPrefix = comm.optString(CONFIG_VIP_LEVEL_ICON_URL_PREFIX);
                                MyLog.d(TAG, "mVipLevelIconUrlPrefix: " + mVipLevelIconUrlPrefix);
                            }
                            //if (comm.has(CONFIG_COMMON_VIP_ENTER_ROOM_MSG)) {
                            //    extraWelcomeVipEnterRoomTips(comm.optString(CONFIG_COMMON_VIP_ENTER_ROOM_MSG));
                            //}
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                certificationList = dataCertification;
                MyLog.e(TAG, "parse global config cost " + (System.currentTimeMillis() - start) + "ms");
                subscriber.onNext(data);
                subscriber.onCompleted();
            }
        })
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<List<LevelItem>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<LevelItem> result) {
                        lvlDataList = result;
                    }
                });
    }

    public List<WallPaper> getWallPaperUrls() {
        return wallPaperUrl;
    }

    public List<Integer> getFeedsChannle() {
        return feedsChannle;
    }

    private void loadLocalResource() {
        {
            LevelItem item = new LevelItem();
            item.min = 0;
            item.max = 5;
            int color = Color.parseColor("#58d4eb");
            item.drawableBadge = GlobalData.app().getResources().getDrawable(R.drawable.lv_1_circular);
            item.drawableLevel = GlobalData.app().getResources().getDrawable(R.drawable.lv_1);
            item.drawableLevel.setBounds(0, 0, DisplayUtils.dip2px(12), DisplayUtils.dip2px(12));
            item.drawableBG = new RoundRectDradable(color);
            lvlDataListLocal.add(item);
            item = new LevelItem();
            item.min = 6;
            item.max = 15;
            color = Color.parseColor("#b1ca4d");
            item.drawableBadge = GlobalData.app().getResources().getDrawable(R.drawable.lv_2_circular);
            item.drawableLevel = GlobalData.app().getResources().getDrawable(R.drawable.lv_2);
            item.drawableLevel.setBounds(0, 0, DisplayUtils.dip2px(12f), DisplayUtils.dip2px(12));
            item.drawableBG = new RoundRectDradable(color);
            lvlDataListLocal.add(item);
            item = new LevelItem();
            item.min = 16;
            item.max = 30;
            color = Color.parseColor("#fec648");
            item.drawableBadge = GlobalData.app().getResources().getDrawable(R.drawable.lv_3_circular);
            item.drawableLevel = GlobalData.app().getResources().getDrawable(R.drawable.lv_3);
            item.drawableLevel.setBounds(0, 0, DisplayUtils.dip2px(12f), DisplayUtils.dip2px(12));
            item.drawableBG = new RoundRectDradable(color);
            lvlDataListLocal.add(item);
            item = new LevelItem();
            item.min = 31;
            item.max = 50;
            color = Color.parseColor("#ffa420");
            item.drawableBadge = GlobalData.app().getResources().getDrawable(R.drawable.lv_4_1_circular);
            item.drawableLevel = GlobalData.app().getResources().getDrawable(R.drawable.lv_4_1);
            item.drawableLevel.setBounds(0, 0, DisplayUtils.dip2px(12f), DisplayUtils.dip2px(12));
            item.drawableBG = new RoundRectDradable(color);
            lvlDataListLocal.add(item);
            item = new LevelItem();
            item.min = 51;
            item.max = 70;
            color = Color.parseColor("#fe862a");
            item.drawableBadge = GlobalData.app().getResources().getDrawable(R.drawable.lv_4_2_circular);
            item.drawableLevel = GlobalData.app().getResources().getDrawable(R.drawable.lv_4_2);
            item.drawableLevel.setBounds(0, 0, DisplayUtils.dip2px(12f), DisplayUtils.dip2px(12));
            item.drawableBG = new RoundRectDradable(color);
            lvlDataListLocal.add(item);
            item = new LevelItem();
            item.min = 71;
            item.max = 90;
            color = Color.parseColor("#ff8020");
            item.drawableBadge = GlobalData.app().getResources().getDrawable(R.drawable.lv_4_3_circular);
            item.drawableLevel = GlobalData.app().getResources().getDrawable(R.drawable.lv_4_3);
            item.drawableLevel.setBounds(0, 0, DisplayUtils.dip2px(12f), DisplayUtils.dip2px(12));
            item.drawableBG = new RoundRectDradable(color);
            lvlDataListLocal.add(item);
            item = new LevelItem();
            item.min = 91;
            item.max = 110;
            color = Color.parseColor("#ff6a49");
            item.drawableBadge = GlobalData.app().getResources().getDrawable(R.drawable.lv_5_1_circular);
            item.drawableLevel = GlobalData.app().getResources().getDrawable(R.drawable.lv_5_1);
            item.drawableLevel.setBounds(0, 0, DisplayUtils.dip2px(12), DisplayUtils.dip2px(12));
            item.drawableBG = new RoundRectDradable(color);
            lvlDataListLocal.add(item);
            item = new LevelItem();
            item.min = 111;
            item.max = 130;
            color = Color.parseColor("#ff603d");
            item.drawableBadge = GlobalData.app().getResources().getDrawable(R.drawable.lv_5_2_circular);
            item.drawableLevel = GlobalData.app().getResources().getDrawable(R.drawable.lv_5_2);
            item.drawableLevel.setBounds(0, 0, DisplayUtils.dip2px(12), DisplayUtils.dip2px(12));
            item.drawableBG = new RoundRectDradable(color);
            lvlDataListLocal.add(item);

            item = new LevelItem();
            item.min = 131;
            item.max = 150;
            color = Color.parseColor("#fe5733");
            item.drawableBadge = GlobalData.app().getResources().getDrawable(R.drawable.lv_5_3_circular);
            item.drawableLevel = GlobalData.app().getResources().getDrawable(R.drawable.lv_5_3);
            item.drawableLevel.setBounds(0, 0, DisplayUtils.dip2px(12), DisplayUtils.dip2px(12));
            item.drawableBG = new RoundRectDradable(color);
            lvlDataListLocal.add(item);

        }
        {
            CertificationItem item = new CertificationItem();
            item.certificationType = 1;
            item.certificationDrawable = GlobalData.app().getResources().getDrawable(R.drawable.certification_type_1_3);
            item.certificationDrawableLiveComment = GlobalData.app().getResources().getDrawable(R.drawable.certification_type_1_3);
            item.certificationDrawableLiveComment.setBounds(10, 0, DisplayUtils.dip2px(20) + 10, DisplayUtils.dip2px(16));
            certificationListLocal.add(item);
            item = new CertificationItem();
            item.certificationType = 2;
            item.certificationDrawable = GlobalData.app().getResources().getDrawable(R.drawable.certification_type_2);
            item.certificationDrawableLiveComment = GlobalData.app().getResources().getDrawable(R.drawable.certification_type_2);
            item.certificationDrawableLiveComment.setBounds(10, 0, DisplayUtils.dip2px(20) + 10, DisplayUtils.dip2px(16));
            certificationListLocal.add(item);
            item = new CertificationItem();
            item.certificationType = 3;
            item.certificationDrawable = GlobalData.app().getResources().getDrawable(R.drawable.certification_type_1_3);
            item.certificationDrawableLiveComment = GlobalData.app().getResources().getDrawable(R.drawable.certification_type_1_3);
            item.certificationDrawableLiveComment.setBounds(10, 0, DisplayUtils.dip2px(20) + 10, DisplayUtils.dip2px(16));
            certificationListLocal.add(item);
            item = new CertificationItem();
            item.certificationType = 4;
            item.certificationDrawable = GlobalData.app().getResources().getDrawable(R.drawable.certification_type_4);
            item.certificationDrawableLiveComment = GlobalData.app().getResources().getDrawable(R.drawable.certification_type_4);
            item.certificationDrawableLiveComment.setBounds(10, 0, DisplayUtils.dip2px(20) + 10, DisplayUtils.dip2px(16));
            certificationListLocal.add(item);
        }

        parseHostsData();
        loadLocalMedalResource();
    }

    public static class LevelItem {
        public int min;
        public int max;
        public Drawable drawableBadge;
        public Drawable drawableLevel;
        public Drawable drawableBG;

        //等级背景色
        public LevelItem() {
        }

        public LevelItem(@DrawableRes int id) {
            drawableBG = GlobalData.app().getResources().getDrawable(id);
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof LevelItem) {
                LevelItem item = (LevelItem) o;
                if (item.min == this.min && item.max == this.max) {
                    return true;
                }
            }
            return false;
        }

        public boolean isDrawablEmpty() {
            return !(drawableBadge != null && drawableLevel != null && drawableBG != null);
        }
    }

    public static class CertificationItem {
        public int certificationType;
        public Drawable certificationDrawable;
        public Drawable certificationDrawableLiveComment;
    }

    //智能弹目时间间隔配置
    public static class SmartBarrageTime {
        //抓取弹目间隔  单位是妙
        public int gbitl = 5 * 60;
        //有场景弹目后再抓取弹目的时间间隔   单位是妙
        public int seitl = 5 * 60;
        //一级人数   单位是妙
        public int olvcot = 600;
        //二级人数    单位是妙
        public int tlvcot = 2000;
    }

    public static class WallPaper {
        public int key;
        public String url;

        public WallPaper(int k, String url) {
            this.key = k;
            this.url = url;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof WallPaper) {
                WallPaper w = (WallPaper) o;
                if (w != null && w.key == this.key) {
                    return true;
                }
            }
            return false;
        }
    }

    public static String LEVEL_DIR = Environment.getExternalStorageDirectory() + "/Xiaomi/WALI_LIVE/levelPic";
    public static String MEDAL_DIR = Environment.getExternalStorageDirectory() + "/Xiaomi/WALI_LIVE/medalPic";//勋章对应的目录-在app启动时l开启子线程load一遍

    /*
     * 从网络上获取图片，如果图片在本地存在的话就直接拿，如果不存在再去服务器上下载图片
     * 这里的path是图片的地址
     */
    public static Drawable getDrawableFromServer(String path, boolean isFromLevelDir) throws Exception {
        MyLog.d(TAG, "path:" + path);
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        File dir = null;
        if (isFromLevelDir) {
            dir = new File(LEVEL_DIR);
        } else {
            dir = new File(MEDAL_DIR);
        }

        if (!dir.exists()) {
            dir.mkdirs(); // 创建文件夹
        }

        //根据服务器返回是否存在后缀构造路径
        String name;
        if (isFromLevelDir) {
            name = MD5.MD5_16(path) + path.substring(path.lastIndexOf("."));
        } else {
            name = MD5.MD5_16(path);
        }
        File file = null;
        if (isFromLevelDir) {
            file = new File(LEVEL_DIR + "/" + name);
        } else {
            file = new File(MEDAL_DIR + "/" + name);
        }
        MyLog.d(TAG, "file" + file.getPath());
        // 如果图片存在本地缓存目录，则不去服务器下载
        if (file.exists()) {
            return file2Drawable(file);
        } else {
            // 从网络上获取图片
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.setDoInput(true);
            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                try {
                    conn.getOutputStream().close();
                } catch (Exception e) {
                }
                is.close();
                fos.close();
                return file2Drawable(file);
            }
        }
        return null;
    }

    public String getMedalKey(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        File dir = new File(MEDAL_DIR);
        if (!dir.exists()) {
            dir.mkdirs(); // 创建文件夹
        }

        String name = MD5.MD5_16(url);
        File file = new File(MEDAL_DIR + "/" + name);

        return file.getPath();
    }

    private LruCache<String, Drawable> mMedalProPrefixHashmap; //给个500 kb
    private Subscription mgetMedalSubscription;

    public final static String MEDAL_TYPE_NOBLE_ONE = "_1";
    public final static String MEDAL_TYPE_NOBLE_TWO = "_2";
    public final static String MEDAL_TYPE_NOBLE_THREE = "_3";


    public Drawable getMedalProIconPrefix(String picId) {
        return getMedalProIconPrefix(picId, "");
    }

    public String getMedalUrl(String picId, String type) {
        MyLog.d(TAG, "getMedalProIconPrefix url : :" + String.valueOf(medalAndIconPrefix + picId + type));
        return String.valueOf(medalAndIconPrefix + picId + type);
    }

    /**
     * 弹幕处调用加载勋章
     *
     * @param picId
     * @param type  活动有，1,2,3三种
     * @return
     */
    public Drawable getMedalProIconPrefix(String picId, String type) {
//        String url = String.valueOf("http://zbtupian.zb.mi.com/"+picId+".png");
//        medalAndIconPrefix = "http://zbtupian.zb.mi.com/staging_medal_and_icon_prefix_";
        final String url = getMedalUrl(picId, type);
        if (mMedalProPrefixHashmap == null) {
            mMedalProPrefixHashmap = new LruCache<String, Drawable>(MAX_MEDAL_MAP_SIZE) {
                protected int sizeOf(String key, Drawable value) {
                    if (value instanceof BitmapDrawable) {
                        BitmapDrawable bitmapDrawable = (BitmapDrawable) value;
                        Bitmap bitmap = bitmapDrawable.getBitmap();
                        int size = bitmap.getHeight() * bitmap.getRowBytes();
                        MyLog.v("testMedalSize" + size);
                        return size;
                    }
                    return 20;
                }
            };
        }

        final Drawable[] drawable = new Drawable[1];
        drawable[0] = mMedalProPrefixHashmap.get(getMedalKey(url));
        if (drawable[0] == null) {
            if (mgetMedalSubscription != null && !mgetMedalSubscription.isUnsubscribed()) {
                return drawable[0];
            }

            mgetMedalSubscription = Observable.create(
                    new Observable.OnSubscribe<Object>() {
                        @Override
                        public void call(Subscriber<? super Object> subscriber) {
                            try {
                                drawable[0] = getDrawableFromServer(url, false);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            subscriber.onCompleted();
                        }
                    })
                    .subscribeOn(Schedulers.io())
                    .subscribe(new Observer<Object>() {
                        @Override
                        public void onCompleted() {
                            mMedalProPrefixHashmap.put(getMedalKey(url), drawable[0]);
                        }

                        @Override
                        public void onError(Throwable e) {

                        }

                        @Override
                        public void onNext(Object o) {

                        }
                    });
            return drawable[0];
        } else {
            return drawable[0];
        }
    }

    private void loadLocalMedalResource() {
        MyLog.d(TAG, "loadLocalMedalResource");
        File dir = new File(MEDAL_DIR);
        if (!dir.exists()) {
            return;
        }

        final File[] files = dir.listFiles();
        Observable.create(
                new Observable.OnSubscribe<Object>() {
                    @Override
                    public void call(Subscriber<? super Object> subscriber) {
                        for (int i = 0; i < files.length; i++) {
                            if (files[i].exists()) {
                                MyLog.d(TAG, "loadLocalMedalResource:" + files[i].getPath());
                                setMedalToMedalMap(files[i].getPath(), file2Drawable(files[i]));
                            }
                        }
                        subscriber.onCompleted();
                    }
                })
                .subscribeOn(Schedulers.io())
                .subscribe(new Observer<Object>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Object o) {

                    }
                });
    }


    public static Drawable file2Drawable(File file) {
        if (file != null && file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
            byte[] chunk = bitmap.getNinePatchChunk();
            boolean result = NinePatch.isNinePatchChunk(chunk);
            if (result) {
                return new NinePatchDrawable(bitmap, chunk, new Rect(), null);
            } else {
                return new BitmapDrawable(bitmap);
            }
        }
        return null;
    }

    private void clearFile() {
        File dir = new File(LEVEL_DIR);
        if (!dir.exists()) {
            dir.mkdir(); // 创建文件夹
            return;
        }
        deleteFile(dir, 0);
    }

    private void deleteFile(File file, int deep) {
        if (file.exists()) {
            if (file.isFile()) {
                file.delete();
            } else if (file.isDirectory()) {
                File[] childFile = file.listFiles();
                if (childFile != null && childFile.length > 0) {
                    for (File f : childFile) {
                        deleteFile(f, deep + 1);
                    }
                }
                if (deep != 0) {
                    file.delete();
                }
            }
        }
    }

    //获取私信系统服务白名单
    public List<Long> getSixinSystemServiceNumWhiteList() {
        String result = PreferenceUtils.getSettingString(GlobalData.app(), PreferenceKeys.PRE_KEY_SIXIN_SYSTEM_SERVICE_WHITE_LIST, "");
        if (TextUtils.isEmpty(result)) {
            result = "666,888,999,100000";
        }
        String[] data = result.split(",");
        List<Long> results = new ArrayList<>();
        for (String item : data) {
            try {
                long itemAsLong = Long.parseLong(item);
                results.add(itemAsLong);
            } catch (Exception e) {
                MyLog.e(e);
            }
        }
        return results;
    }

    private void parseHostsData() {
        String hosts = PreferenceUtils.getSettingString(GlobalData.app(), CONFIG_SCHEME_HOSTS, "");
        if (!TextUtils.isEmpty(hosts)) {
            String[] hostsList = hosts.split(",");
            if (hostsList != null && hostsList.length > 0) {
                for (String item : hostsList) {
                    mHosts.add(item);
                }
            }
        }
    }

    public boolean isValidHost(String url) {
        boolean isValidHost = false;
        if (mHosts != null) {
            for (String item : mHosts) {
                isValidHost = url.startsWith(item);
                if (isValidHost) {
                    break;
                }
            }
        }
        return isValidHost;
    }

    public static class WithdrawConfig {
        /**
         * 默认允许H5提现，但必须有H5Url才能真正通过H5提现
         */
        final boolean mH5WithdrawEnable;
        @Nullable
        public final String mWeChatH5WithdrawUrl;
        @Nullable
        public final String mPayPalH5WithdrawUrl;

        WithdrawConfig() {
            mH5WithdrawEnable = true;
            mWeChatH5WithdrawUrl = null;
            mPayPalH5WithdrawUrl = null;
        }

        WithdrawConfig(boolean h5WithdrawEnable,
                       @Nullable String weChatH5WithdrawUrl, @Nullable String payPalH5WithdrawUrl) {
            mH5WithdrawEnable = h5WithdrawEnable;
            mWeChatH5WithdrawUrl = weChatH5WithdrawUrl;
            mPayPalH5WithdrawUrl = payPalH5WithdrawUrl;
        }

        public boolean isH5WithdrawEnable() {
            return mH5WithdrawEnable
                    && !TextUtils.isEmpty(mWeChatH5WithdrawUrl)
                    && !TextUtils.isEmpty(mPayPalH5WithdrawUrl);
        }

        public boolean isNativeWithdrawEnable() {
            return !mH5WithdrawEnable;
        }

        public boolean isWithdrawEnable() {
            return isH5WithdrawEnable() || isNativeWithdrawEnable();
        }

    }

    @NonNull
    public WithdrawConfig getWithdrawConfig() {
        return mWithdrawConfig;
    }

    public static class IndiaWithdrawConfig {
        private boolean mWithdrawEnable;
        private int mWithdrawThreshold;// 可提现星票阈值
        private String mWithdrawUrl;

        public IndiaWithdrawConfig(boolean withdrawEnable, int withdrawThreshold, String withdrawUrl) {
            mWithdrawEnable = withdrawEnable;
            mWithdrawThreshold = withdrawThreshold;
            mWithdrawUrl = withdrawUrl;
        }

        public boolean isWithdrawEnable() {
            return mWithdrawEnable;
        }

        public int getWithdrawThreshold() {
            return mWithdrawThreshold;
        }

        @Nullable
        public String getWithdrawUrl() {
            return mWithdrawUrl;
        }
    }

    @Nullable
    public IndiaWithdrawConfig getIndiaWithdrawConfig() {
        return mIndiaWithdrawConfig;
    }

    public boolean getMibiRechargeEnable() {
        return mMibiRechargeEnable;
    }

    public String getMedalAndProIconPrefix() {
        return medalAndProIconPrefix;
    }

    public SmartBarrageTime getSmartBarrageTime() {
        return mSmartBarrageTime;
    }

    public String getMedalAndIconPrefix() {
        return medalAndIconPrefix;
    }

    public Drawable getDrawableFromMedalMap(String key) {
        if (mMedalProPrefixHashmap != null && !TextUtils.isEmpty(key)) {
            return drawable = mMedalProPrefixHashmap.get(key);
        }
        return null;
    }

    public void setMedalToMedalMap(String key, Drawable drawable) {
        if (mMedalProPrefixHashmap == null) {
            mMedalProPrefixHashmap = new LruCache<String, Drawable>(MAX_MEDAL_MAP_SIZE) {
                protected int sizeOf(String key, Drawable value) {
                    if (value instanceof BitmapDrawable) {
                        BitmapDrawable bitmapDrawable = (BitmapDrawable) value;
                        Bitmap bitmap = bitmapDrawable.getBitmap();
                        int size = bitmap.getHeight() * bitmap.getRowBytes();
                        MyLog.v("testMedalSize" + size);
                        return size;
                    }
                    return 20;
                }
            };
        }

        if (!TextUtils.isEmpty(key) && drawable != null && mMedalProPrefixHashmap.get(key) != null) {
            mMedalProPrefixHashmap.put(key, drawable);
        }
    }

    public void clearMedalMap() {
        if (mMedalProPrefixHashmap != null) {
            if (mMedalProPrefixHashmap.size() > 0) {
                mMedalProPrefixHashmap.evictAll();
            }
            mMedalProPrefixHashmap = null;
        }
    }

    /**
     * 是否可以显示 群聊入口
     *
     * @param userId
     * @return
     */
    public static boolean canShowGroupIngress(long userId) {
        int isOpen = PreferenceUtils.getSettingInt(PreferenceKeys.PRE_KEY_FANS_GROUP_GLOBAL_OPEN, 0);
        MyLog.d(TAG, "白名单isOpen=" + isOpen);
        if (isOpen == 1) {
            return true;
        } else {
            String whiteList = PreferenceUtils.getSettingString(GlobalData.app(), PreferenceKeys.PRE_KEY_FANS_GROUP_WHITE_LIST, "");
            if (!TextUtils.isEmpty(whiteList)) {
                MyLog.d(TAG, "白名单=" + whiteList);
                String[] whiteListAsArray = whiteList.split(";");
                if (whiteListAsArray != null && whiteListAsArray.length > 0) {
                    for (String item : whiteListAsArray) {
                        if (item.equals(String.valueOf(userId))) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * <pre>
     * 后面拼接lv_1表示VIP等级图标
     *        pr_1表示特权1
     * </pre>
     *
     * @return
     */
    public String getVipLevelIconUrlPrefix() {
        return mVipLevelIconUrlPrefix;
    }

    private void extraWelcomeVipEnterRoomTips(String value) throws JSONException {
        MyLog.d(TAG, "welcome vip enter room tips: " + value);
        if (TextUtils.isEmpty(value)) {
            return;
        }
        JSONObject json = new JSONObject(value);// 例如，{"zh_CN":["大驾光临", "莅临视察"]}
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();//zh_CN
            JSONArray array = json.optJSONArray(key);//["大驾光临", "莅临视察"]
            if (array == null) {
                continue;
            }
            List<String> tips = new ArrayList<>();
            for (int i = 0; i < array.length(); i++) {
                String tip = array.optString(i);
                if (TextUtils.isEmpty(tip)) {
                    continue;
                }
                tips.add(tip);
            }
            mLanguage2WelcomeVipEnterRoomTipsMap.put(key, tips);
        }
        MyLog.d(TAG, "mLanguage2WelcomeVipEnterRoomTipsMap: " + mLanguage2WelcomeVipEnterRoomTipsMap);
    }

    @Deprecated
    @CheckResult
    @Nullable
    public String getWelcomeVipEnterRoomTip() {
        String language = LocaleUtil.getLanguageCode();
        List<String> stringList = mLanguage2WelcomeVipEnterRoomTipsMap.get(language);
        return stringList == null
                ? null
                : stringList.get((int) (System.currentTimeMillis() % stringList.size()));
    }

}
