package com.wali.live.recharge.payway;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.toast.ToastUtils;
import com.live.module.common.R;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.event.EventClass;
import com.wali.live.pay.constant.PayWay;
import com.wali.live.pay.model.Diamond;
import com.wali.live.proto.PayProto;
import com.wali.live.recharge.config.RechargeConfig;
import com.xiaomi.gamecenter.sdk.MiCommplatform;
import com.xiaomi.gamecenter.sdk.MiErrorCode;
import com.xiaomi.gamecenter.sdk.OnLoginProcessListener;
import com.xiaomi.gamecenter.sdk.OnPayProcessListener;
import com.xiaomi.gamecenter.sdk.entry.MiAccountInfo;
import com.xiaomi.gamecenter.sdk.entry.MiAppInfo;
import com.xiaomi.gamecenter.sdk.entry.MiBuyInfo;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import static com.wali.live.recharge.util.PayStatisticUtils.getRechargeTemplate;
import static com.wali.live.statistics.StatisticsKey.AC_APP;
import static com.wali.live.statistics.StatisticsKey.KEY;
import static com.wali.live.statistics.StatisticsKey.Recharge.CANCEL;
import static com.wali.live.statistics.StatisticsKey.Recharge.SUCCESS;
import static com.wali.live.statistics.StatisticsKey.TIMES;

/**
 * 接入游戏支付SDK
 *
 * Created by wuxiaoshan on 17-4-14.
 */
public class MibiPayWay implements IPayWay {

    private static final String TAG = MibiPayWay.class.getSimpleName();

    private Handler mHandle = new Handler();

    @Override
    public PayWay getPayWay() {
        return PayWay.MIBI;
    }

    @Override
    public List<Diamond> parseGemPriceResponse(@NonNull PayProto.GetGemPriceResponse rsp) {
        List<Diamond> priceList = new ArrayList<>();
        for (PayProto.GemGoods goods : rsp.getGemGoodsListList()) {
            if(goods.getPrice() % 100 ==0) {
                priceList.add(Diamond.parse(goods));
            }
        }
        return priceList;
    }

    @Override
    public void init(@NonNull Activity activity) {

    }

    @Override
    public void consumeHoldProduct() {

    }

    @Override
    public boolean postHandleAfterCheckOrder(String receipt) {
        return false;
    }

    @Override
    public void pay(@NonNull final Activity activity,final String orderId, final Diamond diamond, final String userInfo) {
        if (activity == null) {
            MyLog.e(TAG, "activity is null");
            return;
        }
        MiAppInfo appInfo = new MiAppInfo();
        appInfo.setAppId(String.valueOf(RechargeConfig.APP_ID_PAY));
        appInfo.setAppKey(RechargeConfig.APP_KEY_PAY);
        MiCommplatform.Init(GlobalData.app(), appInfo);

        MiCommplatform.getInstance().miLogin(activity, new OnLoginProcessListener() {
            @Override
            public void finishLoginProcess(int code, MiAccountInfo miAccountInfo) {
                MyLog.w(TAG,"MiCommplatform.milogin retCode:"+code);
                EventBus.getDefault().post(new EventClass.HideRechargeProgressEvent());
                switch (code) {
                    case MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS:
                        // 登陆成功
                        //获取用户的登陆后的UID（即用户唯一标识）
                        long uid = miAccountInfo.getUid();
                        String session = miAccountInfo.getSessionId();
                        MiBuyInfo miBuyInfo = new MiBuyInfo();
                        miBuyInfo.setCpOrderId(orderId);//订单号唯一（不为空）
                        miBuyInfo.setCpUserInfo(userInfo); //此参数在用户支付成功后会透传给CP的服务器
                        miBuyInfo.setAmount(diamond.getPrice() / 100);

                        MiCommplatform.getInstance().miUniPay(activity, miBuyInfo, new OnPayProcessListener() {
                            @Override
                            public void finishPayProcess(int code) {
                                MyLog.w(TAG,"MiCommplatform.miUniPay retCode:"+code);
//                                mRechargeView.setMibiRechargeLoginStatus(false);
                                switch (code) {
                                    case MiErrorCode.MI_XIAOMI_PAYMENT_SUCCESS:
                                        //购买成功
                                        showToast(getString(R.string.mibi_pay_success_sync_order));
                                        StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                                                getRechargeTemplate(SUCCESS, com.wali.live.pay.constant.PayWay.MIBI), TIMES, "1");
                                        EventBus.getDefault().post(new EventClass.RechargeCheckOrderEvent(getPayWay(), orderId, userInfo, null, null, true));
                                        break;
                                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_PAY_CANCEL:
                                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_PAY_FAILURE:
                                        //购买失败
//                                        showToast(getErrorMsgByErrorCode(code));
                                        StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                                                getRechargeTemplate(CANCEL, com.wali.live.pay.constant.PayWay.MIBI), TIMES, "1");
                                        break;
                                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_ACTION_EXECUTED:
                                        //操作正在进行中
                                        break;
                                    default:
                                        //购买失败
//                                        showToast(getErrorMsgByErrorCode(code));
                                        StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                                                getRechargeTemplate(CANCEL, com.wali.live.pay.constant.PayWay.MIBI), TIMES, "1");
                                        break;
                                }
                            }
                        });
                        break;
                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_LOGIN_FAIL:
                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_CANCEL:
                    case MiErrorCode.MI_XIAOMI_PAYMENT_ERROR_ACTION_EXECUTED:
                    default:
                        //购买失败
//                        showToast(getErrorMsgByErrorCode(code));
                        StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                                getRechargeTemplate(CANCEL, com.wali.live.pay.constant.PayWay.MIBI), TIMES, "1");
                        break;
                }
            }
        });
    }

    @Override
    public void handlePayResult(int resultCode, Intent intent) {

    }

    @Override
    public void onExitRecharge(@NonNull Activity activity) {

    }

    private void showToast(final String msg) {
            mHandle.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showToast(msg);
                }
            });
    }

    private String getString(@StringRes int stringResId, Object... formatArgs) {
        return GlobalData.app().getString(stringResId, formatArgs);
    }
}
