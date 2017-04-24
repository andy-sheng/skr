package com.wali.live.pay.handler;

import android.content.Context;
import android.support.annotation.NonNull;

import com.wali.live.pay.model.Diamond;
import com.wali.live.recharge.presenter.RechargePresenter;

/**
 * 单笔充值数额检测<br/>
 * 切换到支付宝后没有设置RechargeRecyclerViewAdapter#mPopupWindowPosition，是基于微信、支付宝、小米钱包的充值列表是一样的前提的<br/>
 * 如果这个前提不成立，则需要重新确定RechargeRecyclerViewAdapter#mPopupWindowPosition
 * Created by rongzhisheng on 16-11-12.
 */

public class SingleDealQuotaHandler extends RechargeActionHandler {
    private Context mContext;
    public SingleDealQuotaHandler(@NonNull Context context) {
        mContext = context;
    }

    @Override
    public boolean intercept(final Diamond item) {
        switch (RechargePresenter.getCurPayWay()) {
            case MIBI:
                break;
//            case WEIXIN:
//                if (item.getPrice() > SINGLE_DEAL_QUOTA_WEIXIN) {
//                    //用户选择的金额超出了微信和小米钱包3000的限制，则会弹出一个提示框，提示框展示一次打一次
//                    StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
//                            StatisticsKeyUtils.getRechargeTemplate(EXCEED_SINGLE_DEAL_QUOTA, RechargePresenter.getCurPayWay()), TIMES, "1");
//                    new MyAlertDialog.Builder(mContext)
//                            .setTitle(R.string.recharge_single_big_amount_tip)
//                            .setMessage(R.string.recharge_weixin_single_amount_too_large)
//                            .setPositiveButton(getBigAmountDialogPositiveTip(R.string.recharge_use_alipay), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    if (mDialogActionListener != null) {
//                                        mDialogActionListener.positiveHandle(item);
//                                    }
//                                    dialog.dismiss();
//                                }
//                            })
//                            .setNegativeButton(R.string.recharge_adjust_amount, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    if (mDialogActionListener != null) {
//                                        mDialogActionListener.negativeHandle(item);
//                                    }
//                                    dialog.dismiss();
//                                }
//                            })
//                            .create().show();
//                    return true;
//                }
//                break;
//            case MIWALLET:
//                if (item.getPrice() > SINGLE_DEAL_QUOTA_MIWALLET) {
//                    //用户选择的金额超出了微信和小米钱包3000的限制，则会弹出一个提示框，提示框展示一次打一次
//                    StatisticsAlmightyWorker.getsInstance().recordDelay(AC_APP, KEY,
//                            StatisticsKeyUtils.getRechargeTemplate(EXCEED_SINGLE_DEAL_QUOTA, RechargePresenter.getCurPayWay()), TIMES, "1");
//                    new MyAlertDialog.Builder(mContext)
//                            .setTitle(R.string.recharge_single_big_amount_tip)
//                            .setMessage(R.string.recharge_miwallet_single_amount_too_large)
//                            .setPositiveButton(getBigAmountDialogPositiveTip(R.string.recharge_use_alipay), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    if (mDialogActionListener != null) {
//                                        mDialogActionListener.positiveHandle(item);
//                                    }
//                                    dialog.dismiss();
//                                }
//                            })
//                            .setNegativeButton(R.string.recharge_adjust_amount, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    if (mDialogActionListener != null) {
//                                        mDialogActionListener.negativeHandle(item);
//                                    }
//                                    dialog.dismiss();
//                                }
//                            })
//                            .create().show();
//                    return true;
//                }
//                break;
            default:
                break;
        }

        return mNext != null && mNext.intercept(item);
    }
}
