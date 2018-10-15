package com.wali.live.pay.handler;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;

import com.base.dialog.MyAlertDialog;
import com.base.log.MyLog;
import com.live.module.common.R;
import com.wali.live.pay.model.Diamond;
import com.wali.live.recharge.data.RechargeInfo;
import com.wali.live.recharge.presenter.RechargePresenter;

import static com.wali.live.pay.constant.PayConstant.ONE_DAY_RECHARGE_QUOTA_MIWALLET;
import static com.wali.live.pay.constant.PayConstant.ONE_DAY_RECHARGE_QUOTA_WEIXIN;

/**
 * 单日充值数额检测<br/>
 * 切换到支付宝后没有设置RechargeRecyclerViewAdapter#mPopupWindowPosition，是基于微信、支付宝、小米钱包的充值列表是一样的前提的<br/>
 * 如果这个前提不成立，则需要重新确定RechargeRecyclerViewAdapter#mPopupWindowPosition
 * Created by rongzhisheng on 16-11-12.
 */

public class OneDayQuotaHandler extends RechargeActionHandler {
    private static final String TAG = OneDayQuotaHandler.class.getSimpleName();

    private Context mContext;
    public OneDayQuotaHandler(@NonNull Context context) {
        mContext = context;
    }

    @Override
    public boolean intercept(final Diamond item) {
        switch (RechargePresenter.getCurPayWay()) {
            case WEIXIN: {
                int todayRechargedAmount = RechargeInfo.getWeiXinTodayAmount();
                if (todayRechargedAmount + item.getPrice() > ONE_DAY_RECHARGE_QUOTA_WEIXIN) {
                    MyLog.w(TAG, String.format("exceed weixin one day quota, %d + %d > %d",
                            todayRechargedAmount, item.getPrice(), ONE_DAY_RECHARGE_QUOTA_WEIXIN));
                    new MyAlertDialog.Builder(mContext)
                            .setTitle(R.string.recharge_exceed_one_day_quota_tip)
                            .setMessage(R.string.recharge_exceed_weixin_one_day_quota)
                            .setPositiveButton(getBigAmountDialogPositiveTip(R.string.recharge_use_alipay), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (mDialogActionListener != null) {
                                        mDialogActionListener.positiveHandle(item);
                                    }
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(R.string.recharge_not_adjust_continue, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (mDialogActionListener != null) {
                                        mDialogActionListener.negativeHandle(item);
                                    }
                                    dialog.dismiss();
                                }
                            })
                            .create().show();
                    return true;
                }
            }
            break;
            case MIWALLET: {
                int todayRechargedAmount = RechargeInfo.getMiWalletTodayAmount();
                if (todayRechargedAmount + item.getPrice() > ONE_DAY_RECHARGE_QUOTA_MIWALLET) {
                    MyLog.w(TAG, String.format("exceed miwallet one day quota, %d + %d > %d",
                            todayRechargedAmount, item.getPrice(), ONE_DAY_RECHARGE_QUOTA_MIWALLET));
                    new MyAlertDialog.Builder(mContext)
                            .setTitle(R.string.recharge_exceed_one_day_quota_tip)
                            .setMessage(R.string.recharge_exceed_miwallet_one_day_quota)
                            .setPositiveButton(getBigAmountDialogPositiveTip(R.string.recharge_use_alipay), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (mDialogActionListener != null) {
                                        mDialogActionListener.positiveHandle(item);
                                    }
                                    dialog.dismiss();
                                }
                            })
                            .setNegativeButton(R.string.recharge_not_adjust_continue, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (mDialogActionListener != null) {
                                        mDialogActionListener.negativeHandle(item);
                                    }
                                    dialog.dismiss();
                                }
                            })
                            .create().show();
                    return true;
                }
            }
            break;
            default:
                break;
        }

        return mNext != null && mNext.intercept(item);
    }
}
