package com.mi.liveassistant.data.config;

import android.support.annotation.AnyThread;
import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.common.preference.PreferenceKeys;
import com.mi.liveassistant.common.preference.PreferenceUtils;
import com.mi.liveassistant.dns.DomainListUpdateEvent;
import com.mi.liveassistant.milink.MiLinkClientAdapter;
import com.mi.liveassistant.milink.command.MiLinkCommand;
import com.mi.liveassistant.milink.constant.MiLinkConstant;
import com.mi.liveassistant.proto.ConfigProto;
import com.mi.milink.sdk.aidl.PacketData;
import com.mi.milink.sdk.proto.SystemPacketProto;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
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

    private static final String TAG = GetConfigManager.class.getSimpleName();

    private static final long INTERVAL = 1000 * 60 * 60 * 5;//检测更新时间10小时

    private static GetConfigManager sInstance;

    private Subscription mGetConfigSubscription;
    private long mTimestamp;

    private GetConfigManager() {
        mTimestamp = PreferenceUtils.getSettingLong(PreferenceKeys.PREF_KEY_CONFIG_TIMESTAMP, 0);
    }

    public static synchronized GetConfigManager getInstance() {
        boolean firstIn = false;
        if (sInstance == null) {
            firstIn = true;
            sInstance = new GetConfigManager();
        }
        if (Math.abs(System.currentTimeMillis() - sInstance.mTimestamp) > INTERVAL) {
            sInstance.getConfig();
        } else if (firstIn) {
            if (PreferenceUtils.hasKey(PreferenceKeys.PREF_KEY_CONFIG_JSON)) {
                sInstance.parseJsonConfig(PreferenceUtils.getSettingString(PreferenceKeys.PREF_KEY_CONFIG_JSON, ""));
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
        sInstance.mTimestamp = 0;
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
                            sInstance.mTimestamp = System.currentTimeMillis();
                            PreferenceUtils.setSettingLong(PreferenceKeys.PREF_KEY_CONFIG_TIMESTAMP, sInstance.mTimestamp);
                            parseJsonConfig(response.getJsonConfig());
                            PreferenceUtils.setSettingString(PreferenceKeys.PREF_KEY_CONFIG_JSON, response.getJsonConfig());
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

    public void parseJsonConfig(final String configStr) {
        MyLog.i(TAG, "parseJsonConfig : " + configStr);
        long start = System.currentTimeMillis();
        try {
            JSONObject root = new JSONObject(configStr);

            if (root.has("zhibo_biz")) {
                try {
                    JSONObject disStr = root.getJSONObject("zhibo_biz");
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
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        MyLog.e(TAG, "parse global config cost " + (System.currentTimeMillis() - start) + "ms");
    }
}
