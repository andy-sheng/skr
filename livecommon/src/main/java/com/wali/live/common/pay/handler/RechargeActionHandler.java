package com.wali.live.common.pay.handler;

import android.support.annotation.MainThread;
import android.support.annotation.StringRes;

import com.base.global.GlobalData;
import com.base.utils.span.SpanUtils;
import com.live.module.common.R;
import com.wali.live.common.pay.model.Diamond;


/**
 * 当用户点击充斥按钮时需要执行的逻辑<br>
 * Created by rongzhisheng on 16-11-12.
 */

public abstract class RechargeActionHandler {
    protected RechargeActionHandler mNext;
    protected IDialogActionListener mDialogActionListener;

    public void setNext(RechargeActionHandler next) {
        mNext = next;
    }

    public void setDialogActionListener(IDialogActionListener dialogActionListener) {
        mDialogActionListener = dialogActionListener;
    }

    /**
     * 用户点击充值按钮后的额外处理逻辑
     *
     * @param item 用户选择的充值项
     * @return true表示可以处理
     */
    @MainThread
    public abstract boolean intercept(Diamond item);

    protected CharSequence getBigAmountDialogPositiveTip(@StringRes int stringId) {
        return SpanUtils.getHighLightKeywordText(getString(stringId), getString(stringId), R.color.recharge_exchange_btn_color);
    }

    private String getString(@StringRes int stringId) {
        return GlobalData.app().getString(stringId);
    }

    public interface IDialogActionListener {
        /**
         * 肯定处理
         *
         * @param item
         */
        void positiveHandle(Diamond item);

        /**
         * 否定处理
         *
         * @param item
         */
        void negativeHandle(Diamond item);
    }

}
