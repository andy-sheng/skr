package com.wali.live.recharge.config;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.Constants;
import com.wali.live.pay.constant.PayWay;
import com.wali.live.recharge.data.RechargeInfo;
import com.wali.live.recharge.payway.IPayWay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.wali.live.pay.constant.PayConstant.SP_FILENAME_RECHARGE_CONFIG;
import static com.wali.live.pay.constant.PayConstant.SP_KEY_LAST_PAY_WAY;

/**
 * Created by rongzhisheng on 16-12-23.
 */

public class RechargeConfig {
    private static final String TAG = RechargeConfig.class.getSimpleName();
    public static final long APP_ID_PAY;
    public static final String APP_KEY_PAY;

    static {
        if (Constants.isTestBuild) {
            APP_ID_PAY = 2882303761517560526L;
            APP_KEY_PAY = "5121756040526";
        } else {
            APP_ID_PAY = 2882303761517559014L;
            APP_KEY_PAY = "5871755944014";
        }
    }

    private static final String ID_CODE = "ID";//印度尼西亚
    private static List<PayWay> payWayList = new ArrayList<>();
    private static List<IPayWay> payWayImplList = new ArrayList<>();

    /**
     * 充值界面展现之前调用
     */
    @MainThread
    public synchronized static void onEnterRecharge(@NonNull Activity activity) {
        //初始化payWayList
        payWayList.clear();
        payWayImplList.clear();

        payWayList.addAll(initPayWayList());
        for (PayWay payWay : payWayList) {
            IPayWay payWayImpl = payWay.getIPayWay();
            if (payWayImpl == null) {
                MyLog.e(TAG, "no corresponding IPayWay with " + payWay);
                continue;
            }
            payWayImpl.init(activity);
            payWayImplList.add(payWayImpl);
        }
    }

    @NonNull
    private static List<PayWay> initPayWayList() {
        return Arrays.asList(PayWay.MIBI);
    }

    /**
     * 退出充值时调用
     */
    @MainThread
    public synchronized static void onExitRecharge(@NonNull Activity activity) {
        for (IPayWay iPayWay : payWayImplList) {
            iPayWay.onExitRecharge(activity);
        }
        payWayImplList.clear();
        if (null != payWayList) {
            payWayList.clear();
        }
        RechargeInfo.replaceGemCache(null);
    }

    @MainThread
    public static List<PayWay> getPayWayList() {
        return payWayList;
    }

    /**
     * 需要先调用{@link #onEnterRecharge}
     *
     * @param payWay
     * @return
     */
    @Nullable
    public synchronized static IPayWay getPayWayImpl(@NonNull PayWay payWay) {
        if (payWayList == null || !payWayList.contains(payWay)) {
            MyLog.e(TAG, "payWayList does not contain " + payWay);
            return null;
        }
        for (IPayWay iPayWay : payWayImplList) {
            if (iPayWay.getPayWay() == payWay) {
                return iPayWay;
            }
        }
        MyLog.e(TAG, "no corresponding IPayWay with " + payWay);
        return null;
    }

    /**
     * 需要先调用{@link #onEnterRecharge}
     *
     * @return
     */
    @MainThread
    public static PayWay getInitialPayWay() {
        PayWay defaultPayWay = payWayList.get(0);
        PayWay payWay = defaultPayWay;
        String lastPayWayName = PreferenceUtils.getSettingString(GlobalData.app().getSharedPreferences(SP_FILENAME_RECHARGE_CONFIG, Context.MODE_PRIVATE),
                SP_KEY_LAST_PAY_WAY, null);
        if (!TextUtils.isEmpty(lastPayWayName)) {
            try {
                payWay = PayWay.valueOf(lastPayWayName.toUpperCase());
            } catch (Exception e) {
                MyLog.e(TAG, "unexpected saved pay way:" + lastPayWayName);
            }
        }
        // 版本替换可能造成程序保存的用户上次使用的支付方式不适合当前版本
        // 想象这样的情况：小米钱包不能用于国际支付，GoogleWallet和PayPal不能用于国内支付
        if (!payWayList.contains(payWay)) {
            payWay = defaultPayWay;
        }
        return payWay;
    }

    public static int getPayWaysSize() {
        return payWayList == null ? 0 : payWayList.size();
    }

}
