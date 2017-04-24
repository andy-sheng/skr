package com.wali.live.pay.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.mi.milink.sdk.aidl.PacketData;
import com.wali.live.event.EventClass;
import com.wali.live.pay.protocol.QueryBalanceDetailRequest;
import com.wali.live.proto.PayProto;

import org.greenrobot.eventbus.EventBus;

import rx.Observable;
import rx.Subscriber;

/**
 * @module 充值
 * Created by chengsimin on 16/2/22.
 * Google Play部分由rongzhisheng修改 on 16/5/20
 */
public class PayManager {
    public static final int GOODS_NOT_EXIST = 11040;

    public static String TAG = PayManager.class.getSimpleName();

    /* 充值活动小红点 */
    public static void pullRedPointAsync() {
        PayProto.GetRedPointConfigRequest.Builder reqBuilder = PayProto.GetRedPointConfigRequest.newBuilder()
                .setType("recharge");
        RedPointConfig config = getRedPointFromPreference();
        if (!TextUtils.isEmpty(config.uniq)) {
            reqBuilder.setUniq(config.uniq);
        }
        PayProto.GetRedPointConfigRequest req = reqBuilder.build();
        PacketData data = new PacketData();
        data.setCommand(MiLinkCommand.COMMAND_PAY_GET_RED_ICON);
        data.setData(req.toByteArray());
        MiLinkClientAdapter.getsInstance().sendAsync(data);
//        MyLog.v(TAG, "pullRedPointAsync request:" + req.toString());
    }

    private static void updateRedPointInPreference(RedPointConfig r) {
        SharedPreferences settingPreferences = GlobalData.app().getSharedPreferences(
                PREF_RECHARGE_RED_POINT_CONFIG, Context.MODE_PRIVATE);
        PreferenceUtils.setSettingLong(settingPreferences, KEY_START_TIME, r.startTime);
        PreferenceUtils.setSettingLong(settingPreferences, KEY_END_TIME, r.endTime);
        PreferenceUtils.setSettingString(settingPreferences, KEY_UNIQ, r.uniq);
        PreferenceUtils.setSettingBoolean(settingPreferences, KEY_HAS_READ, r.isRead);
    }

    private static RedPointConfig getRedPointFromPreference() {
        SharedPreferences settingPreferences = GlobalData.app().getSharedPreferences(
                PREF_RECHARGE_RED_POINT_CONFIG, Context.MODE_PRIVATE);
        RedPointConfig r = new RedPointConfig();
        r.startTime = settingPreferences.getLong(KEY_START_TIME, 0);
        r.endTime = settingPreferences.getLong(KEY_END_TIME, 0);
        r.uniq = settingPreferences.getString(KEY_UNIQ, "");
        r.isRead = settingPreferences.getBoolean(KEY_HAS_READ, false);
        return r;
    }

    public static void processRedPoint(PayProto.GetRedPointConfigResponse response) {
        if (response == null) {
            return;
        }
        MyLog.d(TAG, "response:" + response);
        RedPointConfig config = RedPointConfig.toRedPointConfig(response.getConfig());
        compareWithCurrent(config);
    }

    private static void compareWithCurrent(RedPointConfig newConfig) {
        RedPointConfig curConfig = getRedPointFromPreference();

        if (curConfig.uniq.equals(newConfig.uniq)
                && curConfig.startTime == newConfig.startTime
                && curConfig.endTime == newConfig.endTime) {
            // 和上次拉取的配置一样，则不更新
            MyLog.d(TAG, "red point not change");
        } else {
            MyLog.d(TAG, "new config");
            // 来了新的配置了,写入新的配置
            updateRedPointInPreference(newConfig);
            long now = System.currentTimeMillis();
            if (now > newConfig.startTime && now < newConfig.endTime) {
                // 活动已经开始且未结束,显示小红点
                EventBus.getDefault().post(new EventClass.ShowRechargeRedPoint());
            }
        }
    }

    public static void setHasReadRedPoint() {
        RedPointConfig r = getRedPointFromPreference();
        r.isRead = true;
        updateRedPointInPreference(r);
    }

    public static boolean isNeedShowRedPoint() {
        long now = System.currentTimeMillis();
        RedPointConfig r = getRedPointFromPreference();
        return now > r.startTime && now < r.endTime && !r.isRead;
    }

    private static String PREF_RECHARGE_RED_POINT_CONFIG = "pref_recharge_red_point_config";
    private static String KEY_START_TIME = "key_start_time";
    private static String KEY_END_TIME = "key_end_time";
    private static String KEY_UNIQ = "key_uniq";
    private static String KEY_HAS_READ = "key_has_read";


    private static class RedPointConfig {
        public long startTime;
        public long endTime;
        String uniq;
        boolean isRead;

        static RedPointConfig toRedPointConfig(PayProto.RedPointConfig config) {
            RedPointConfig r = new RedPointConfig();
            if (config != null) {
                r.startTime = config.getStartTime();
                r.endTime = config.getEndTime();
                r.uniq = config.getUniq();
            }
            return r;
        }
    }

    // 余额详情
    public static Observable<PayProto.QueryBalanceDetailResponse> getBalanceDetailRsp() {
        return Observable.create(new Observable.OnSubscribe<PayProto.QueryBalanceDetailResponse>() {
            @Override
            public void call(Subscriber<? super PayProto.QueryBalanceDetailResponse> subscriber) {
                PayProto.QueryBalanceDetailResponse response = new QueryBalanceDetailRequest().syncRsp();
                subscriber.onNext(response);
                subscriber.onCompleted();
            }
        });
    }

}
