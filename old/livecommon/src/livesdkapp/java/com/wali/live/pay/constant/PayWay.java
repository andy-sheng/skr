package com.wali.live.pay.constant;

import android.support.annotation.CheckResult;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.base.global.GlobalData;
import com.base.utils.CommonUtils;
import com.live.module.common.R;
import com.mi.live.data.api.ErrorCode;
import com.wali.live.pay.model.Diamond;
import com.wali.live.proto.PayProto;
import com.wali.live.recharge.constants.RechargeConstants;
import com.wali.live.recharge.payway.IPayWay;
import com.wali.live.recharge.payway.MibiPayWay;
import com.wali.live.statistics.StatisticsKey;

/**
 * Created by rongzhisheng on 16-11-12.
 * 可能的支付手段，约定此枚举的命名全部大写
 */
public enum PayWay {
    MIBI{
        @Nullable
        @Override
        public IPayWay getIPayWay() {
            return new MibiPayWay();
        }

        @Override
        public int getRechargeListType() {
            return RechargeConstants.RechargeListType.CHINA;
        }

        @Override
        public int getIcon() {
            return R.drawable.pay_icon_mibi_pressed;
        }

        @Override
        public int getName() {
            return R.string.mibi;
        }

        @Override
        public PayProto.RChannel getChannel() {
            return PayProto.RChannel.AND_CH;
        }

        @Override
        public PayProto.PayType getPayType() {
            return PayProto.PayType.MIPAY;
        }

        @Override
        public CharSequence getGemPriceText(@NonNull Diamond diamond) {
            return GlobalData.app().getResources().getQuantityString(R.plurals.recharge_money_amount, diamond.getPrice() / 100,
                    CommonUtils.getHumanReadableMoney(diamond.getPrice()));
        }

        @Override
        public boolean canCheckOrder(int code) {
            return code == ErrorCode.CODE_SUCCESS;
        }

        @Override
        public String getAbbr() {
            return StatisticsKey.Recharge.PayWay.Mibi;
        }
    };

    @Nullable
    public abstract IPayWay getIPayWay();

    /**
     * 获取支付方式对应的充值列表类型，比如微信、支付宝、小米钱包是一类，在充值列表类型相同的支付方式之间切换无需再从服务器拉取
     * @return
     */
    public abstract int getRechargeListType();
    /**
     * 支付方式的图标
     * @return
     */
    @DrawableRes
    public abstract int getIcon();

    /**
     * 支付方式的名字
     * @return
     */
    @StringRes
    public abstract int getName();

    public abstract PayProto.RChannel getChannel();

    public abstract PayProto.PayType getPayType();

    public abstract CharSequence getGemPriceText(@NonNull Diamond diamond);

    /**
     * 是否可以继续checkOrder
     * @param code
     * @return
     */
    public abstract boolean canCheckOrder(int code);

    /**
     * 打点时的简称
     * @return
     */
    @CheckResult
    public abstract String getAbbr();
}
