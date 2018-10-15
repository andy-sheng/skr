package com.wali.live.recharge.payway;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.live.module.common.R;
import com.mi.live.data.account.XiaoMiOAuth;
import com.wali.live.recharge.util.PayStatisticUtils;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.event.EventClass;
import com.wali.live.pay.constant.PayWay;
import com.wali.live.pay.model.Diamond;
import com.wali.live.recharge.config.RechargeConfig;
import com.wali.live.statistics.MiLinkMonitorScribeWorker;
import com.xiaomi.game.plugin.stat.MiGamePluginStat;
import com.xiaomi.gamecenter.wxwap.HyWxWapPay;

import org.greenrobot.eventbus.EventBus;

import static com.wali.live.recharge.util.RechargeUtil.getErrorMsgByErrorCode;
import static com.wali.live.statistics.StatisticsKey.AC_APP;
import static com.wali.live.statistics.StatisticsKey.KEY;
import static com.wali.live.statistics.StatisticsKey.Recharge.CANCEL;
import static com.wali.live.statistics.StatisticsKey.Recharge.PAY_ERROR_CODE;
import static com.wali.live.statistics.StatisticsKey.Recharge.SUCCESS;
import static com.wali.live.statistics.StatisticsKey.TIMES;
import static com.wali.live.statistics.StatisticsKeyUtils.getRechargeTemplate;
import static com.xiaomi.gamecenter.alipay.config.ResultCode.SCANPAY_CANCEL;
import static com.xiaomi.gamecenter.alipay.config.ResultCode.WXPAY_CANCEL;

/**
 * Created by rongzhisheng on 16-12-23.
 */

public class WeChatPay extends ChinaPay {
    private static final String TAG = WeChatPay.class.getSimpleName();

    @Override
    public PayWay getPayWay() {
        return PayWay.WEIXIN;
    }

    @Override
    public void init(@NonNull Activity activity) {
        MiGamePluginStat.setCheckInitEnv(false);
        HyWxWapPay.init(activity, String.valueOf(RechargeConfig.APP_ID_PAY), RechargeConfig.APP_KEY_PAY);
    }

    @Override
    public void pay(@Nullable final Activity activity,final String orderId,final Diamond diamond,final String userInfo) {
        if (activity == null) {
            MyLog.e(TAG, "activity is null");
            return;
        }
        com.xiaomi.gamecenter.wxwap.purchase.FeePurchase purchase = new com.xiaomi.gamecenter.wxwap.purchase.FeePurchase();
        purchase.setCpOrderId(orderId);
        purchase.setFeeValue(String.valueOf(diamond.getPrice()));
        purchase.setCpUserInfo(userInfo);
        HyWxWapPay hyWxPay = null;
        MyLog.e(TAG, "init HyWxWapPay sdk");
        try {
            hyWxPay = HyWxWapPay.getInstance();
        } catch (IllegalStateException e) {
            MyLog.e(TAG, "init HyWxWapPay sdk fail, init here", e);
            HyWxWapPay.init(activity, String.valueOf(RechargeConfig.APP_ID_PAY), RechargeConfig.APP_KEY_PAY);
            hyWxPay = HyWxWapPay.getInstance();
        }
        MyLog.e(TAG, "hyWxPay.pay");
        hyWxPay.pay(activity, purchase, new com.xiaomi.gamecenter.wxwap.PayResultCallback() {
            @Override
            public void onError(int errorCode, String msg) {
                String errMsg = String.format("msg:%s, errorCode:%d", msg, errorCode);
                MyLog.e(TAG, errMsg);
                ToastUtils.showToast(getErrorMsgByErrorCode(errorCode).toString());
                switch (errorCode) {
                    case WXPAY_CANCEL:
                    case SCANPAY_CANCEL://取消扫码支付
                        StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                                PayStatisticUtils.getRechargeTemplate(CANCEL, com.wali.live.pay.constant.PayWay.WEIXIN), TIMES, "1");
                        break;
                    //case CODE_WX_NOT_INSTALL:
                    //    StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                    //            getRechargeTemplate(APP_NOT_INSTALL, PayWay.WEIXIN), TIMES, "1");
                    //    break;
                }
                StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                        PayStatisticUtils.getRechargeTemplate(PAY_ERROR_CODE, com.wali.live.pay.constant.PayWay.WEIXIN, errorCode), TIMES, "1");
            }

            @Override
            public void onSuccess(String orderId) {
                MyLog.w(TAG, String.format("weixin pay ok, orderId:%s, uid:%s", orderId, userInfo));
                ToastUtils.showToast(R.string.weixin_pay_success_sync_order);
                StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                        PayStatisticUtils.getRechargeTemplate(SUCCESS, com.wali.live.pay.constant.PayWay.WEIXIN), TIMES, "1");
                EventBus.getDefault().post(new EventClass.RechargeCheckOrderEvent(getPayWay(), orderId, userInfo, null, null, true));
            }
        });
    }

}
