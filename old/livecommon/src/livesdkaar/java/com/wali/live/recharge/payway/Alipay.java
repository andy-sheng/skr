package com.wali.live.recharge.payway;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.live.module.common.R;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.event.EventClass;
import com.wali.live.pay.constant.PayWay;
import com.wali.live.pay.model.Diamond;
import com.wali.live.recharge.config.RechargeConfig;
import com.xiaomi.game.plugin.stat.MiGamePluginStat;
import com.xiaomi.gamecenter.alipay.HyAliPay;
import com.xiaomi.gamecenter.alipay.purchase.FeePurchase;

import org.greenrobot.eventbus.EventBus;

import static com.wali.live.recharge.util.RechargeUtil.getErrorMsgByErrorCode;
import static com.wali.live.statistics.StatisticsKey.AC_APP;
import static com.wali.live.statistics.StatisticsKey.KEY;
import static com.wali.live.statistics.StatisticsKey.Recharge.CANCEL;
import static com.wali.live.statistics.StatisticsKey.Recharge.PAY_ERROR_CODE;
import static com.wali.live.statistics.StatisticsKey.Recharge.SUCCESS;
import static com.wali.live.statistics.StatisticsKey.TIMES;
import static com.wali.live.statistics.StatisticsKeyUtils.getRechargeTemplate;
import static com.xiaomi.gamecenter.alipay.config.ResultCode.ALIPAY_CANCEL;
import static com.xiaomi.gamecenter.alipay.config.ResultCode.SCANPAY_CANCEL;
import static com.xiaomi.gamecenter.alipay.config.ResultCode.WXPAY_CANCEL;

/**
 * Created by rongzhisheng on 16-12-31.
 */

public class Alipay extends ChinaPay {
    private static final String TAG = Alipay.class.getSimpleName();

    @Override
    public PayWay getPayWay() {
        return PayWay.ZHIFUBAO;
    }

    @Override
    public void init(@NonNull Activity activity) {
        //必须加上这个！支付宝SDK和小米钱包SDK就不会被限定一定要在Application的onCreate里调用了
        MiGamePluginStat.setCheckInitEnv(false);
        HyAliPay.init(activity, String.valueOf(RechargeConfig.APP_ID_PAY), RechargeConfig.APP_KEY_PAY);
    }

    @Override
    public void pay(@Nullable final Activity activity, final String orderId, final Diamond diamond, final String userInfo) {
        if (activity == null) {
            MyLog.e(TAG, "activity is null");
            return;
        }
        FeePurchase purchase = new FeePurchase();
        purchase.setCpOrderId(orderId);
        purchase.setFeeValue(String.valueOf(diamond.getPrice()));
        purchase.setCpUserInfo(userInfo);
        HyAliPay hyAliPay = null;
        try {
            hyAliPay = HyAliPay.getInstance();
        } catch (IllegalStateException e) {
            MyLog.e(TAG, "init alipay sdk fail, init here", e);
            HyAliPay.init(activity, String.valueOf(RechargeConfig.APP_ID_PAY), RechargeConfig.APP_KEY_PAY);
            hyAliPay = HyAliPay.getInstance();
        }

        hyAliPay.aliPay(activity, purchase, new com.xiaomi.gamecenter.alipay.PayResultCallback() {
            @Override
            public void onError(int errorCode, String msg) {
                String errMsg = String.format("msg:%s, errorCode:%d", msg, errorCode);
                MyLog.e(TAG, errMsg);
                ToastUtils.showToast(getErrorMsgByErrorCode(errorCode).toString());
                switch (errorCode) {
                    case WXPAY_CANCEL:
                    case SCANPAY_CANCEL:
                    case ALIPAY_CANCEL:
                        StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                                getRechargeTemplate(CANCEL, PayWay.ZHIFUBAO), TIMES, "1");
                        break;
                }
                StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                        getRechargeTemplate(PAY_ERROR_CODE, PayWay.ZHIFUBAO, errorCode), TIMES, "1");
            }

            @Override
            public void onSuccess(String orderId) {
                MyLog.w(TAG, String.format("alipay pay ok, orderId:%s, userInfo:%s", orderId, userInfo));
                ToastUtils.showToast(R.string.alipay_pay_success_sync_order);
                StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                        getRechargeTemplate(SUCCESS, PayWay.ZHIFUBAO), TIMES, "1");
                //RechargePresenter.getInstance().checkOrder(getPayWay(), orderId, userInfo, null, null, true);
                EventBus.getDefault().post(new EventClass.RechargeCheckOrderEvent(getPayWay(), orderId, userInfo, null, null, true));
            }
        });
    }

}
