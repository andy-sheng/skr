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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.MD5;
import com.base.utils.display.DisplayUtils;
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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by yurui on 3/3/16.
 */
public class GetConfigManager {

    private static final String TAG = GetConfigManager.class.getSimpleName();

    private static final long INTERVAL = 1000 * 60 * 60 * 5;//检测更新时间10小时

    private static final String CONFIG_VIDEO_RATE = "video_rate";
    private static final String CONFIG_PRIVATE_ROOM_SIZE = "private_room_size";
    private static final String CONFIG_CONVERGED_LEVEL = "converged_level"; //收敛规则中的　等级配置大于10的　不收敛
    private static final String CONFIG_CONVERGED_RANKING = "converged_ranking"; //收敛规则排名信息
    private static final String CONFIG_CONVERGED_INTERVAL = "converged_interval"; //收敛规则中的时间限制
    private static final String CONFIG_CONVERGED_MSG = "converged_msg"; //收敛规则中的弹幕条数
    public static final String CONFIG_CHANNEL_NAME = "channel_name"; //特殊时期在频道列表新加一个特殊的频道
    public static final String CONFIG_CHANNEL_URL = "channel_url"; //特殊时期在频道列表新加一个特殊的频道
    private static final String CONFIG_WALLPAPER_URL = "zhibo_homepage_wall_paper"; //个人主页推荐背景
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
    private static final String CONFIG_ROOT_ZHIBO_COMMON = "zhibo_comm"; // 和zhibo_biz 同一级
    private static final String CONFIG_GAME_FOLLOW_TS = "game_follow_start_time"; //游戏直播房间内引导关注时机
    private static final String CONFIG_INDIA_WITHDRAW_ENABLE = "inTxEn";
    private static final String CINFIG_INDIA_WITHDRAW_THRESHOLD = "inTxThreshold";
    private static final String CONFIG_INDIA_WITHDRAW_URL = "inTxUrl";

    private static GetConfigManager sInstance;

    private long timestamp;

    private List<LevelItem> lvlDataList;

    private List<LevelItem> lvlDataListLocal;

    private List<CertificationItem> certificationList;

    private List<CertificationItem> certificationListLocal;

    private List<WallPaper> wallPaperUrl;

    private Set<String> mHosts; //webviewactiivity能吊起的host

    private Drawable mAnchorBadge;
    private Drawable mGuestBadge;

    private List<String> mLoopBacksModelList;//支持loopBack的手机机型。
    private boolean mFilterFollowClient = false;
    private boolean mLinkDeviceOn = false;

    public long getRegionTime() {
        return mRegionTime;
    }

    private long mRegionTime = 0;

    // webview的白名单url，防劫持
    private Set<String> whiteListUrlForWebView;
    // 提现配置
    private WithdrawConfig mWithdrawConfig = new WithdrawConfig();
    private volatile IndiaWithdrawConfig mIndiaWithdrawConfig;

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
            firstIn = true;
            sInstance = new GetConfigManager();
        }
        if (Math.abs(System.currentTimeMillis() - sInstance.timestamp) > INTERVAL) {
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

    public CertificationItem getCertificationTypeDrawable(int certificationType) {
        for (CertificationItem item : certificationList) {
            if (certificationType == item.certificationType) {
                return item;
            }
        }
        return getCertificationTypeDrawableLocal(certificationType);
    }

    private CertificationItem getCertificationTypeDrawableLocal(int certificationType) {
        CertificationItem defaultItem = null;
        for (CertificationItem item : certificationListLocal) {
            if (certificationType == item.certificationType) {
                return item;
            } else if (item.certificationType == -1) {
                defaultItem = item;
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
            certificationListLocal.add(defaultItem);
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

        if (mParseJsonConfigSubscription != null && !mParseJsonConfigSubscription.isUnsubscribed()) {
            return;
        }
        Observable.create(new Observable.OnSubscribe<List<LevelItem>>() {
            @Override
            public void call(Subscriber<? super List<LevelItem>> subscriber) {
                List<LevelItem> data = new ArrayList<>();
                List<CertificationItem> dataCertification = new ArrayList<>();
                try {
                    JSONObject root = new JSONObject(str);
                    if (root.has("zhibo_biz")) {
                        JSONObject disStr = root.getJSONObject("zhibo_biz");
                        if (disStr.has("display_level") && root.has("zhibo_level_icon_map_and_v2")) {
                            int level = disStr.getInt("display_level");
                            JSONObject iconMap = root.getJSONObject("zhibo_level_icon_map_and_v2");
                            for (int i = 1; i <= level; i++) {
                                if (iconMap.has("level_icon_map_" + i)) {
                                    LevelItem item = new LevelItem();
                                    String[] itemStr = iconMap.getString("level_icon_map_" + i).split(",");
                                    try {
                                        item.min = Integer.parseInt(itemStr[0]);
                                        item.max = Integer.parseInt(itemStr[1]);
                                        item.drawableBadge = getDrawableFromServer(itemStr[2]);
                                        item.drawableLevel = getDrawableFromServer(itemStr[3]);
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
                                mAnchorBadge = getDrawableFromServer(linkAnchorBadgeUrl);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        if (disStr.has(CONFIG_LINK_GUEST_BADGE_URL)) {
                            try {
                                String linkGuestBadgeUrl = disStr.getString(CONFIG_LINK_GUEST_BADGE_URL);
                                mGuestBadge = getDrawableFromServer(linkGuestBadgeUrl);
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
                        boolean withdrawEnable = disStr.optInt(CONFIG_WITHDRAW_ENABLE) == 1;
                        boolean h5WithdrawEnable = disStr.optInt(CONFIG_H5_WITHDRAW_ENABLE) == 1;
                        String weChatH5WithdrawUrl = disStr.optString(CONFIG_WECHAT_H5_WITHDRAW_URL, null);
                        String payPalH5WithdrawUrl = disStr.optString(CONFIG_PAYPAL_H5_WITHDRAW_URL, null);
                        mWithdrawConfig = new WithdrawConfig(withdrawEnable, h5WithdrawEnable, weChatH5WithdrawUrl, payPalH5WithdrawUrl);

                        boolean indiaWithdrawEnable = disStr.optInt(CONFIG_INDIA_WITHDRAW_ENABLE) != 0;
                        int indiaWithdrawThreshold = Math.max(disStr.optInt(CINFIG_INDIA_WITHDRAW_THRESHOLD), 0);
                        String indiaWithdrawUrl = disStr.optString(CONFIG_INDIA_WITHDRAW_URL);
                        mIndiaWithdrawConfig = new IndiaWithdrawConfig(indiaWithdrawEnable, indiaWithdrawThreshold, indiaWithdrawUrl);


                        StringBuilder convergedData = new StringBuilder();
                        convergedData.append(disStr.optInt(CONFIG_CONVERGED_LEVEL, 10)).append("_").append(disStr.optInt(CONFIG_CONVERGED_RANKING, 10)).append("_")
                                .append(disStr.optInt(CONFIG_CONVERGED_INTERVAL, 5)).append("_").append(disStr.optInt(CONFIG_CONVERGED_MSG, 1));
                        PreferenceUtils.setSettingString(GlobalData.app(), PreferenceKeys.PREF_KEY_CONVERGED, convergedData.toString());

                        List<String> domainList = new ArrayList<>();
                        if (disStr.has("pull_stream")) {
                            String pStream = disStr.getString("pull_stream");
                            if (!TextUtils.isEmpty(pStream)) {
                                String[] pullStream = pStream.split(";");
                                for (String str : pullStream) {
                                    domainList.add(str);
                                }
                            }
                        }
                        if (disStr.has("push_stream")) {
                            String pStream = disStr.getString("push_stream");
                            if (!TextUtils.isEmpty(pStream)) {
                                String[] pushStream = pStream.split(";");
                                for (String str : pushStream) {
                                    domainList.add(str);
                                }
                            }
                        }
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
                        EventBus.getDefault().post(new DomainListUpdateEvent(domainList, domainPortList));

                        if (disStr.has("sys_account")) {
                            String oldAccount = PreferenceUtils.getSettingString(GlobalData.app(), PreferenceKeys.PRE_KEY_SIXIN_SYSTEM_SERVICE_WHITE_LIST, "");
                            if (TextUtils.isEmpty(oldAccount) || !oldAccount.equals(disStr.optString("sys_account"))) {
                                PreferenceUtils.setSettingString(GlobalData.app(), PreferenceKeys.PRE_KEY_SIXIN_SYSTEM_SERVICE_WHITE_LIST, disStr.getString("sys_account"));
                                EventBus.getDefault().post(new SixinWhiteListUpdateEvent(disStr.optString("sys_account")));
                            }
                        }

                        if (disStr.has(CONFIG_LINE_BITRATE)) {
                            String lineBitrate = disStr.getString(CONFIG_LINE_BITRATE);
                            try {
                                PreferenceUtils.setSettingInt(PreferenceKeys.PREF_KEY_LINE_BITRATE, Integer.parseInt(lineBitrate));
                            } catch (NumberFormatException e) {
                                MyLog.e(e);
                            }
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
                        if (disStr.has(CONFIG_BARRAGE_INTERVAL)) {
                            String sendBarrageInterval = disStr.getString(CONFIG_BARRAGE_INTERVAL);
                            if (!TextUtils.isEmpty(sendBarrageInterval))
                                PreferenceUtils.setSettingString(GlobalData.app(), PreferenceKeys.PRE_KEY_SEND_BARRAGE_INTERVAL, sendBarrageInterval);

                        }
                        if (disStr.has(CONFIG_KICK_PERMISSION_ANCHOR)) {
                            String permission = disStr.getString(CONFIG_KICK_PERMISSION_ANCHOR);
                            if (!TextUtils.isEmpty(permission) && TextUtils.isDigitsOnly(permission)) {
                                PreferenceUtils.setSettingInt(PreferenceKeys.PRE_KIK_PERMISSION_ANCHOR, Integer.valueOf(permission));
                            }
                        }
                        if (disStr.has(CONFIG_KICK_PERMISSION_ADMIN)) {
                            String permission = disStr.getString(CONFIG_KICK_PERMISSION_ADMIN);
                            if (!TextUtils.isEmpty(permission) && TextUtils.isDigitsOnly(permission)) {
                                PreferenceUtils.setSettingInt(PreferenceKeys.PRE_KIK_PERMISSION_ADMIN, Integer.valueOf(permission));
                            }
                        }
                        if (disStr.has(CONFIG_KICK_PERMISSION_TOP1)) {
                            String permission = disStr.getString(CONFIG_KICK_PERMISSION_TOP1);
                            if (!TextUtils.isEmpty(permission) && TextUtils.isDigitsOnly(permission)) {
                                PreferenceUtils.setSettingInt(PreferenceKeys.PRE_KIK_PERMISSION_TOP1, Integer.valueOf(permission));
                            }
                        }
                        if (disStr.has(CONFIG_LIVE_SCHEDULE_URL)) {
                            String url = disStr.getString(CONFIG_LIVE_SCHEDULE_URL);
                            if (!TextUtils.isEmpty(url)) {
                                PreferenceUtils.setSettingString(GlobalData.app(), PreferenceKeys.PREF_LIVE_SCHEDULE_URL, url);
                            }
                        }
                        if (disStr.has(CONFIG_FOLLOW_POPUP_USER_NUMBER)) {
                            String userNum = disStr.getString(CONFIG_FOLLOW_POPUP_USER_NUMBER);
                            if (!TextUtils.isEmpty(userNum) && TextUtils.isDigitsOnly(userNum)) {
                                PreferenceUtils.setSettingInt(PreferenceKeys.PRE_FOLLOW_POPUP_USER_NUMBER, Integer.valueOf(userNum));
                            }
                        }
                        if (disStr.has(CONFIG_FOLLOW_POPUP_VIEW_TIME)) {
                            String viewTime = disStr.getString(CONFIG_FOLLOW_POPUP_VIEW_TIME);
                            if (!TextUtils.isEmpty(viewTime) && TextUtils.isDigitsOnly(viewTime)) {
                                PreferenceUtils.setSettingInt(PreferenceKeys.PRE_FOLLOW_POPUP_VIEW_TIME, Integer.valueOf(viewTime));
                            }
                        }
                        if (disStr.has(CONFIG_FOLLOW_CANCEL_TIME)) {
                            String cancelTime = disStr.getString(CONFIG_FOLLOW_CANCEL_TIME);
                            if (!TextUtils.isEmpty(cancelTime) && TextUtils.isDigitsOnly(cancelTime)) {
                                PreferenceUtils.setSettingInt(PreferenceKeys.PRE_FOLLOW_CANCEL_TIME, Integer.valueOf(cancelTime));
                            }
                        }

                        if (disStr.has(CONFIG_REDNAME_SENDSMS_INTERVAL)) {
                            String interval = disStr.getString(CONFIG_REDNAME_SENDSMS_INTERVAL);
                            if (!TextUtils.isEmpty(interval)) {
                                PreferenceUtils.setSettingString(GlobalData.app(), PreferenceKeys.PRE_KEY_REDNAM_SENDSMS_INTERVAL, interval);
                            }
                        }
                    }

                    if (root.has(CONFIG_ROOT_ZHIBO_COMMON)) {
                        JSONObject zhiboCommon = root.getJSONObject(CONFIG_ROOT_ZHIBO_COMMON);
                        MyLog.w(TAG,  CONFIG_ROOT_ZHIBO_COMMON + "=" + zhiboCommon.toString());
                        if (zhiboCommon.has(CONFIG_GAME_FOLLOW_TS)) {
                            int gameFollowTs = zhiboCommon.getInt(CONFIG_GAME_FOLLOW_TS);
                            PreferenceUtils.setSettingInt(PreferenceKeys.PRE_KEY_GAME_FOLLOW_TIME, gameFollowTs);
                        }
                    }
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
                    if (root.has("icon_vip_user_badge_and")) {
                        JSONObject disStr = root.getJSONObject("icon_vip_user_badge_and");
                        Iterator<String> keys = disStr.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            CertificationItem item = new CertificationItem();
                            try {
                                MyLog.v(TAG + " (" + key + ") " + disStr.getString(key));
                                item.certificationType = Integer.parseInt(key);
                                item.certificationDrawable = getDrawableFromServer(disStr.getString(key));
                                item.certificationDrawableLiveComment = getDrawableFromServer(disStr.getString(key));
                                item.certificationDrawableLiveComment.setBounds(10, 0, DisplayUtils.dip2px(20) + 10, DisplayUtils.dip2px(16));
                                dataCertification.add(item);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

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
                certificationList = dataCertification;
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

    public void loadLocalResource() {
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
    }


    public static class LevelItem {
        public int min;
        public int max;
        public Drawable drawableBadge;
        public Drawable drawableLevel;
        public Drawable drawableBG;           //等级背景色

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

    public static class CertificationItem {
        public int certificationType;
        public Drawable certificationDrawable;
        public Drawable certificationDrawableLiveComment;
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

    /*
     * 从网络上获取图片，如果图片在本地存在的话就直接拿，如果不存在再去服务器上下载图片
     * 这里的path是图片的地址
     */
    public static Drawable getDrawableFromServer(String path) throws Exception {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        File dir = new File(LEVEL_DIR);
        if (!dir.exists()) {
            dir.mkdirs(); // 创建文件夹
        }

        String name = MD5.MD5_16(path) + path.substring(path.lastIndexOf("."));
        File file = new File(LEVEL_DIR + "/" + name);
        // 如果图片存在本地缓存目录，则不去服务器下载
        if (file.exists()) {
            return file2Drawable(file);
        } else {
            // 从网络上获取图片
            URL url = new URL(path);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                FileOutputStream fos = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                is.close();
                fos.close();
                return file2Drawable(file);
            }
        }
        return null;
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
            result = "888,999,100000";
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
         * Android没用到
         */
        final boolean mWithdrawEnable;
        /**
         * 默认允许H5提现，但必须有H5Url才能真正通过H5提现
         */
        final boolean mH5WithdrawEnable;
        @Nullable
        public final String mWeChatH5WithdrawUrl;
        @Nullable
        public final String mPayPalH5WithdrawUrl;

        WithdrawConfig() {
            mWithdrawEnable = true;
            mH5WithdrawEnable = true;
            mWeChatH5WithdrawUrl = null;
            mPayPalH5WithdrawUrl = null;
        }

        WithdrawConfig(boolean withdrawEnable, boolean h5WithdrawEnable,
                       @Nullable String weChatH5WithdrawUrl, @Nullable String payPalH5WithdrawUrl) {
            mH5WithdrawEnable = h5WithdrawEnable;
            mWithdrawEnable = withdrawEnable;
            mWeChatH5WithdrawUrl = weChatH5WithdrawUrl;
            mPayPalH5WithdrawUrl = payPalH5WithdrawUrl;
        }

        public boolean isH5WithdrawEnable() {
            return mWithdrawEnable
                    && mH5WithdrawEnable
                    && !TextUtils.isEmpty(mWeChatH5WithdrawUrl)
                    && !TextUtils.isEmpty(mPayPalH5WithdrawUrl);
        }

        public boolean isNativeWithdrawEnable() {
            return mWithdrawEnable && !mH5WithdrawEnable;
        }

        public boolean isWithdrawEnable() {
            return isH5WithdrawEnable() || isNativeWithdrawEnable();
        }

    }

    @NonNull
    public WithdrawConfig getWithdrawConfig() {
        return mWithdrawConfig;
    }

    public IndiaWithdrawConfig getIndiaWithdrawConfig(){
        return mIndiaWithdrawConfig;
    }
}
