package com.wali.live.recharge.payway;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.base.dialog.MyAlertDialog;
import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.toast.ToastUtils;
import com.live.module.common.R;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.event.EventClass;
import com.wali.live.pay.constant.PayWay;
import com.wali.live.pay.model.Diamond;
import com.wali.live.proto.PayProto;
import com.wali.live.recharge.config.RechargeConfig;
import com.wali.live.recharge.presenter.RechargePresenter;
import com.xiaomi.game.plugin.stat.MiGamePluginStat;
import com.xiaomi.gamecenter.ucashier.HyUcashierPay;
import com.xiaomi.gamecenter.ucashier.PayResultCallback;
import com.xiaomi.gamecenter.ucashier.purchase.FeePurchase;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

import static com.wali.live.recharge.util.RechargeUtil.getErrorMsgByErrorCode;
import static com.wali.live.statistics.StatisticsKey.AC_APP;
import static com.wali.live.statistics.StatisticsKey.KEY;
import static com.wali.live.statistics.StatisticsKey.Recharge.CANCEL;
import static com.wali.live.statistics.StatisticsKey.Recharge.PAY_ERROR_CODE;
import static com.wali.live.statistics.StatisticsKey.Recharge.SUCCESS;
import static com.wali.live.statistics.StatisticsKey.TIMES;
import static com.wali.live.statistics.StatisticsKeyUtils.getRechargeTemplate;
import static com.xiaomi.gamecenter.ucashier.config.ResultCode.TOAST_PAY_CANCEL;

/**
 * Created by rongzhisheng on 16-12-31.
 */

public class MiWallet extends ChinaPay {
    private static final String TAG = MiWallet.class.getSimpleName();

    @Override
    public PayWay getPayWay() {
        return PayWay.MIWALLET;
    }

    @Override
    public void init(@NonNull Activity activity) {
        //必须加上这个！支付宝SDK和小米钱包SDK就不会被限定一定要在Application的onCreate里调用了
        MiGamePluginStat.setCheckInitEnv(false);
        HyUcashierPay.init(activity, String.valueOf(RechargeConfig.APP_ID_PAY), RechargeConfig.APP_KEY_PAY);
    }

    @Override
    public void pay(@Nullable final Activity activity, final String orderId, final Diamond diamond, final String userInfo) {
        if (activity == null) {
            MyLog.e(TAG, "activity is null");
            return;
        }
        int miWalletLoginAccountType = CommonUtils.getMiWalletLoginAccountType();
        MyLog.w(TAG, "mi wallet login account type from pref:" + miWalletLoginAccountType);
        final int price = diamond.getPrice();
        switch (miWalletLoginAccountType) {
            case CommonUtils.LOGIN_ACCOUNT_TYPE_SYSTEM:
                payByMiWallet0(activity, orderId, price, userInfo, true);
                break;
            case CommonUtils.LOGIN_ACCOUNT_TYPE_OTHER:
                payByMiWallet0(activity, orderId, price, userInfo, false);
                break;
            case CommonUtils.LOGIN_ACCOUNT_TYPE_NONE:
                // 检测是否在系统上登录了小米账号
                Account systemAccount = null;
                AccountManager am = null;
                am = AccountManager.get(GlobalData.app().getApplicationContext());
                Account[] accounts = am.getAccountsByType("com.xiaomi");
                if (accounts.length > 0) {
                    systemAccount = accounts[0];
                } else {
                    // 非MIUI手机或没有登录系统账号
                    // 为了防止MIUI用户在没有登录系统账号时使用过小米钱包，然后登录系统账号，清除数据，再使用小米钱包时弹出对话框，这里记录为没有选择使用系统账号
                    MyLog.w(TAG, "system mi account not detected");
                    CommonUtils.setMiWalletLoginAccountType(CommonUtils.LOGIN_ACCOUNT_TYPE_OTHER);
                    payByMiWallet0(activity, orderId, price, userInfo, false);
                    return;
                }
                final String name = systemAccount.name;
                // 这里只是检测系统账号是否可用，所以写成固定的值也是可以的
                String tokenType = "weblogin:" + "https://account.xiaomi.com/pass/serviceLogin?sid=cashpay-wap" +
                        "&callback=https://m.pay.xiaomi.com/sts?sign=e%2BWpFF%2Br3039Tt5%2BK1jF401f8Ug%3D" +
                        "&followup=https%3A%2F%2Fm.pay.xiaomi.com%2FpayFunc%3Fsafe%3Dc74da2a26aaf4b6b992e3f06488f9aa6%26" +
                        "outOrderId%3D20161474188675162725%26sellerId%3D10000112&bal=";
                am.getAuthToken(systemAccount, tokenType, null, activity, new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        try {
                            String authToken = future.getResult().getString(AccountManager.KEY_AUTHTOKEN);
                            MyLog.d(TAG, "KEY_AUTHTOKEN:" + authToken);
                            if (authToken != null && !TextUtils.isEmpty(name) && TextUtils.isDigitsOnly(name)) {
                                // 提示用户检测到了系统账号，询问用户是否用系统账号支付
                                final MyAlertDialog.Builder builder = new MyAlertDialog.Builder(activity);
                                builder.setTitle(R.string.use_mi_account_login_title);
                                builder.setMessage(GlobalData.app().getString(R.string.use_mi_account_login_miwallet_msg, name));
                                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        CommonUtils.setMiWalletLoginAccountType(CommonUtils.LOGIN_ACCOUNT_TYPE_SYSTEM);
                                        payByMiWallet0(activity, orderId, price, userInfo, true);
                                        dialog.dismiss();
                                    }
                                });

                                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        CommonUtils.setMiWalletLoginAccountType(CommonUtils.LOGIN_ACCOUNT_TYPE_OTHER);
                                        payByMiWallet0(activity, orderId, price, userInfo, false);
                                        dialog.dismiss();
                                    }
                                });
                                builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
                                    @Override
                                    public void onCancel(DialogInterface dialog) {
                                        ToastUtils.showToast(getErrorMsgByErrorCode(TOAST_PAY_CANCEL).toString());
                                    }
                                });
                                int color = GlobalData.app().getResources().getColor(R.color.color_e5aa1e);
                                builder.setPositiveButtonTextColor(color);
                                builder.setAutoDismiss(false).show();
                            } else {
                                // 获取authToken为空
                                MyLog.e(TAG, "authToken:" + authToken + ", name:" + name);
                                CommonUtils.setMiWalletLoginAccountType(CommonUtils.LOGIN_ACCOUNT_TYPE_OTHER);
                                payByMiWallet0(activity, orderId, price, userInfo, false);
                            }
                        } catch (OperationCanceledException | IOException | AuthenticatorException e) {
                            MyLog.e(TAG, "get authToken fail", e);
                            // 没有权限获取到authToken或其他问题
                            CommonUtils.setMiWalletLoginAccountType(CommonUtils.LOGIN_ACCOUNT_TYPE_OTHER);
                            payByMiWallet0(activity, orderId, price, userInfo, false);
                        }
                    }
                }, null);
                break;
            default:
                // 配置文件被人改了……
                MyLog.e(TAG, "unexpected mi wallet login account type:" + miWalletLoginAccountType);
                break;
        }// end of switch
    }

    private void payByMiWallet0(@NonNull final Activity activity, final String orderId, final int price, final String userInfo, final boolean useSystemAccount) {
        FeePurchase feePurchase = new FeePurchase();
        feePurchase.setCpOrderId(orderId);
        feePurchase.setCpUserInfo(userInfo);
        feePurchase.setFeeValue(String.valueOf(price));//单位分
        HyUcashierPay hyUcashierPay = null;
        try {
            hyUcashierPay = HyUcashierPay.getInstance();
        } catch (IllegalStateException e) {
            MyLog.e(TAG, "init miwallet sdk fail, init here", e);
            HyUcashierPay.init(activity, String.valueOf(RechargeConfig.APP_ID_PAY), RechargeConfig.APP_KEY_PAY);
            hyUcashierPay = HyUcashierPay.getInstance();
        }
        hyUcashierPay.ucashierPay(activity, useSystemAccount, feePurchase, new PayResultCallback() {
            @Override
            public void onError(int errorCode, String msg) {
                String errMsg = String.format("msg:%s,errorcode:%d", msg, errorCode);
                MyLog.e(TAG, errMsg);
                ToastUtils.showToast(getErrorMsgByErrorCode(errorCode).toString());
                switch (errorCode) {
                    case TOAST_PAY_CANCEL:
                        StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                                getRechargeTemplate(CANCEL, PayWay.MIWALLET), TIMES, "1");
                        break;
                }

                StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                        getRechargeTemplate(PAY_ERROR_CODE, PayWay.MIWALLET, errorCode), TIMES, "1");
            }

            @Override
            public void onSuccess(String orderId) {
                MyLog.w(TAG, String.format("weixin pay ok, orderId:%s, userInfo:%s", orderId, userInfo));
                ToastUtils.showToast(R.string.miwallet_pay_success_sync_order);
                StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
                        getRechargeTemplate(SUCCESS, PayWay.MIWALLET), TIMES, "1");
                //RechargePresenter.getInstance().checkOrder(getPayWay(), orderId, null, null, null, true);
                EventBus.getDefault().post(new EventClass.RechargeCheckOrderEvent(getPayWay(), orderId, null, null, null, true));
            }
        });
    }

}
