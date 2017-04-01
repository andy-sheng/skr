package com.wali.live.common.pay.constant;

import android.content.Context;
import android.support.annotation.NonNull;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.live.module.common.R;
import com.mi.live.data.account.HostChannelManager;
import com.wali.live.common.pay.model.PayWayInfo;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * 充值相关的配置
 * Created by rongzhisheng on 16-11-12.
 */

public class RechargeConfig {
    private static final String TAG = RechargeConfig.class.getSimpleName();

    // 列表的顺序影响展示顺序,第一个元素为默认支付方式
    private static List<PayWay> sNativePayWayList;

    private static Map<PayWay, PayWayInfo> sPayWayInfoMap;

    static{
        initNativePayWays();
    }

    /**
     * 支付手段和充值列表的映射，比如微信、支付宝、小米钱包是一类，GoogleWallet是一类，PayPal是一类
     */
    public static int getRechargeListType(@NonNull PayWay payWay) {
        PayWayInfo payWayInfo = sPayWayInfoMap.get(payWay);
        if (payWay != null) {
            return payWayInfo.mRechargeType;
        }
        IllegalStateException illegalStateException = new IllegalStateException("unexpected payWay:" + payWay);
        MyLog.e(TAG, illegalStateException);
        throw illegalStateException;
    }

    /**
     * 服务器返回的钻石信息是否能被直接用于充值列表展示
     */
    public static boolean isServerDiamondInfoCanDirectlyUse(int rechargeListType) {
        return rechargeListType == PayConstant.RECHARGE_LIST_TYPE_NATIVE;
    }

    public static boolean isMibiPayway(int rechargeListType) {
        return rechargeListType == PayConstant.RECHARGE_LIST_TYPE_MIBI;
    }

    public static List<PayWay> getNativePayWayList() {
        return sNativePayWayList;
    }


    private static void initNativePayWays(){
        sNativePayWayList = Arrays.asList(PayWay.MIBI);
        //初始化图表及标题信息
        Map<PayWay, PayWayInfo> map = new HashMap<>();
        map.put(PayWay.WEIXIN, new PayWayInfo(PayWay.WEIXIN, R.drawable.pay_icon_weixin_pressed, R.string.weixin, PayConstant.RECHARGE_LIST_TYPE_NATIVE));
        map.put(PayWay.ZHIFUBAO, new PayWayInfo(PayWay.ZHIFUBAO, R.drawable.pay_icon_zhifubao_pressed, R.string.zhifubao, PayConstant.RECHARGE_LIST_TYPE_NATIVE));
        map.put(PayWay.MIWALLET, new PayWayInfo(PayWay.MIWALLET, R.drawable.pay_icon_miwallet_pressed, R.string.miwallet, PayConstant.RECHARGE_LIST_TYPE_NATIVE));
        map.put(PayWay.MIBI, new PayWayInfo(PayWay.MIBI, R.drawable.pay_icon_mibi_pressed, R.string.mibi, PayConstant.RECHARGE_LIST_TYPE_MIBI));
        sPayWayInfoMap = Collections.unmodifiableMap(map);
    }

    public static boolean isNativePayWay(PayWay payWay) {
        return sNativePayWayList.contains(payWay);
    }

    public static Map<PayWay, PayWayInfo> getPayWayInfoMap() {
        return sPayWayInfoMap;
    }

    public static String getLastPaywayKey() {
        return PayConstant.SP_KEY_LAST_PAY_WAY + HostChannelManager.getInstance().getChannelId();
    }

    public static String getIsFirstRechargeKey() {
        return PayConstant.SP_KEY_IS_FIRST_RECHARGE + HostChannelManager.getInstance().getChannelId();
    }

    public static String getLastPaywayName() {
        if (isOnlyOnePayway()) {
            return PreferenceUtils.getSettingString(GlobalData.app().getSharedPreferences(PayConstant.SP_FILENAME_RECHARGE_CONFIG, Context.MODE_PRIVATE),
                    RechargeConfig.getLastPaywayKey(), sNativePayWayList.get(0).name().toUpperCase());
        } else {
            return PreferenceUtils.getSettingString(GlobalData.app().getSharedPreferences(PayConstant.SP_FILENAME_RECHARGE_CONFIG, Context.MODE_PRIVATE),
                    RechargeConfig.getLastPaywayKey(), null);
        }
    }

    public static boolean getIsFirstRecharge() {
        if (isOnlyOnePayway()) {
            return PreferenceUtils.getSettingBoolean(
                    GlobalData.app().getSharedPreferences(PayConstant.SP_FILENAME_RECHARGE_CONFIG, Context.MODE_PRIVATE),
                    RechargeConfig.getIsFirstRechargeKey(),
                    false);
        } else {
            return PreferenceUtils.getSettingBoolean(
                    GlobalData.app().getSharedPreferences(PayConstant.SP_FILENAME_RECHARGE_CONFIG, Context.MODE_PRIVATE),
                    RechargeConfig.getIsFirstRechargeKey(),
                    true);
        }
    }

    public static boolean isOnlyOnePayway() {
        return sNativePayWayList != null && sNativePayWayList.size() == 1;
    }


}
