package com.wali.live.common.pay.model;

import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import com.wali.live.common.pay.constant.PayWay;

/**
 * 支付方式信息.
 * Created by rongzhisheng on 16-11-12.
 */

public class PayWayInfo {
    public final PayWay mPayWay;
    public final int mIconId;
    public final int mNameId;
    public final int mRechargeType;

    public PayWayInfo(@NonNull PayWay payWay, @DrawableRes int iconId, @StringRes int nameId, int rechargeType) {
        mPayWay = payWay;
        mIconId = iconId;
        mNameId = nameId;
        mRechargeType = rechargeType;
    }
}