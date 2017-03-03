package com.wali.live.common.pay.constant;

import android.support.annotation.NonNull;

import com.base.log.MyLog;
import com.base.utils.Constants;
import com.live.module.common.R;
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

    /**
     * 支付手段和充值列表的映射，比如微信、支付宝、小米钱包是一类，GoogleWallet是一类，PayPal是一类
     */
    public static int getRechargeListType(@NonNull PayWay payWay) {
        switch (payWay) {
            case WEIXIN:
                return PayConstant.RECHARGE_LIST_TYPE_NATIVE;
            case ZHIFUBAO:
                return PayConstant.RECHARGE_LIST_TYPE_NATIVE;
            case MIWALLET:
                return PayConstant.RECHARGE_LIST_TYPE_NATIVE;
//            case GOOGLEWALLET:
//                return PayConstant.RECHARGE_LIST_TYPE_GOOGLE_WALLET;
//            case PAYPAL:
//                return PayConstant.RECHARGE_LIST_TYPE_PAYPAL;
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

    // 列表的顺序影响展示顺序,第一个元素为默认支付方式
//    private static List<PayWay> sInternationalPayWayList = Arrays.asList(PayWay.GOOGLEWALLET, PayWay.PAYPAL, PayWay.WEIXIN, PayWay.ZHIFUBAO, PayWay.MIWALLET);
    private static List<PayWay> sNativePayWayList = Arrays.asList(PayWay.WEIXIN, PayWay.ZHIFUBAO, PayWay.MIWALLET/*, PayWay.GOOGLEWALLET, PayWay.PAYPAL*/);

    static {
        if (Constants.isGooglePlayBuild || Constants.isIndiaBuild) {
//            sInternationalPayWayList = sNativePayWayList = Arrays.asList(PayWay.GOOGLEWALLET);
        } else {
//            sInternationalPayWayList = Arrays.asList(PayWay.GOOGLEWALLET, PayWay.PAYPAL, PayWay.WEIXIN, PayWay.ZHIFUBAO, PayWay.MIWALLET);
//            sNativePayWayList = Arrays.asList(PayWay.WEIXIN, PayWay.ZHIFUBAO, PayWay.MIWALLET/*, PayWay.GOOGLEWALLET, PayWay.PAYPAL*/);
            sNativePayWayList = Arrays.asList(/*PayWay.WEIXIN,*/ PayWay.ZHIFUBAO, PayWay.MIWALLET/*, PayWay.GOOGLEWALLET, PayWay.PAYPAL*/);
        }
    }

//    public static List<PayWay> getInternationalPayWayList() {
//        return sInternationalPayWayList;
//    }

    public static List<PayWay> getNativePayWayList() {
        return sNativePayWayList;
    }

//    public static boolean isInternationalPayWay(PayWay payWay) {
//        return sInternationalPayWayList.contains(payWay);
//    }

    public static boolean isNativePayWay(PayWay payWay) {
        return sNativePayWayList.contains(payWay);
    }

    private static final Map<PayWay, PayWayInfo> sPayWayInfoMap;

    static {
        Map<PayWay, PayWayInfo> map = new HashMap<>();
        map.put(PayWay.WEIXIN, new PayWayInfo(PayWay.WEIXIN, R.drawable.pay_icon_weixin_pressed, R.string.weixin));
        map.put(PayWay.ZHIFUBAO, new PayWayInfo(PayWay.ZHIFUBAO, R.drawable.pay_icon_zhifubao_pressed, R.string.zhifubao));
        map.put(PayWay.MIWALLET, new PayWayInfo(PayWay.MIWALLET, R.drawable.pay_icon_miwallet_pressed, R.string.miwallet));
//        map.put(PayWay.GOOGLEWALLET, new PayWayInfo(PayWay.GOOGLEWALLET, R.drawable.pay_icon_googlewallet_pressed, R.string.payway_google_wallet));
//        map.put(PayWay.PAYPAL, new PayWayInfo(PayWay.PAYPAL, R.drawable.pay_icon_paypal_pressed, R.string.payway_paypal));
        sPayWayInfoMap = Collections.unmodifiableMap(map);
    }

    public static Map<PayWay, PayWayInfo> getPayWayInfoMap() {
        return sPayWayInfoMap;
    }
}
