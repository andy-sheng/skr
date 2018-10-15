package com.wali.live.watchsdk.income.income;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.global.GlobalData;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.base.utils.display.DisplayUtils;
import com.base.utils.language.LocaleUtil;
import com.base.utils.span.SpanUtils;
import com.base.utils.toast.ToastUtils;
import com.jakewharton.rxbinding.view.RxView;
import com.mi.live.data.config.GetConfigManager;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.income.FillAliAccountActivity;
import com.wali.live.watchsdk.income.FillPayPalAccountActivity;
import com.wali.live.watchsdk.income.FillWXAccountActivity;
import com.wali.live.watchsdk.income.UserProfit;
import com.wali.live.watchsdk.income.WithDrawActivity;
import com.wali.live.watchsdk.webview.WebViewActivity;

import java.util.concurrent.TimeUnit;

import rx.functions.Action1;

import static com.wali.live.watchsdk.income.UserProfit.BUNDLE_PAY_TYPE;
import static com.wali.live.watchsdk.income.UserProfit.KEY_EXCHANGE_MAX_CASH;
import static com.wali.live.watchsdk.income.UserProfit.KEY_EXCHANGE_MIN_CASH;
import static com.wali.live.watchsdk.income.UserProfit.SIGN_STATUS_SIGNED;
import static com.wali.live.watchsdk.income.income.UserIncomeActivity.BUNDLE_BIND_ACCOUNT;
import static com.wali.live.watchsdk.income.income.UserIncomeActivity.BUNDLE_BIND_AVATAR;
import static com.wali.live.watchsdk.income.income.UserIncomeActivity.BUNDLE_TADAY_EXCHANGE_MONEY;
import static com.wali.live.watchsdk.income.income.UserIncomeActivity.BUNDLE_VERIFICATION_STATE;


/**
 * 普通提现
 * Created by rongzhisheng on 16-11-30.
 */

public class NormalIncomeFragment extends BaseIncomeFragment {
    //////
    //view
    //////


    private ViewGroup mTopWithdrawButtonContainer;//上半部分提现按钮容器
    private View mTicketToGemBtn;//兑换钻石
    private TextView mBeforeSignedIncomeTipTv;//下半部分签约主播可提现数字描述
    private TextView mBeforeSignedIncomeTv;//下半部分签约主播可提现数字
    private TextView mSignedTipTv;//签约状态下的提示

//    private View mBottomExchangeMibiArea;//底部兑换米币区域

    //////
    //data
    //////

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.normal_income_fragment, container, false);
    }

    @Override
    protected void bindOwnView() {

    }

    @Override
    protected void initView() {
        super.initView();
//        mShowMoneyTv = $(R.id.show_money);
//        mGameMoneyTv = $(R.id.game_money);
        mUnsignedExchangeableMoneyTv = $(R.id.unsigned_exchangeable_money);
        mTopWithdrawButtonContainer = $(R.id.withdraw_btn_container_top);
        mTicketToGemBtn = $(R.id.ticket_to_gem_btn);
        mSignedTipTv = $(R.id.signed_tip);
//        mAvailableTicketTip = $(R.id.available_coin);
//        mBottomExchangeMibiArea = $(R.id.bottom_exchange_mibi_area);
        adjustWithdrawButtonLayout();
        mTicketToGemBtn.setTag(ACTION_INCOME_EXCHANGE_GEM);
        bindEvent(mTicketToGemBtn);
    }

    @Override
    protected void initSignedView() {
        super.initSignedView();
        mBeforeSignedIncomeTv = $(R.id.before_signed_income_tv);
        mBeforeSignedIncomeTipTv = $(R.id.before_signed_income_tip_tv);
        mTopWithdrawButtonContainer = $(R.id.withdraw_btn_container_top);
        mTicketToGemBtn = $(R.id.ticket_to_gem_btn);
        mTicketToGemBtn.setTag(ACTION_INCOME_EXCHANGE_GEM);
        bindEvent(mTicketToGemBtn);
        adjustWithdrawButtonLayout();
    }

    private void adjustWithdrawButtonLayout() {
        View topWeChatBtn = mTopWithdrawButtonContainer.findViewById(R.id.withdraw_weixin_btn);
        View topPaypalBtn = mTopWithdrawButtonContainer.findViewById(R.id.withdraw_paypal_btn);
        View topWithdrawBtnDivider = mTopWithdrawButtonContainer.findViewById(R.id.withdraw_btn_divider);

        topWeChatBtn.setTag(ACTION_INCOME_GET_WX_MONEY);
        topPaypalBtn.setTag(ACTION_INCOME_GET_PAYPAL_MONEY);

        GetConfigManager.WithdrawConfig withdrawConfig = GetConfigManager.getInstance().getWithdrawConfig();
        if (!withdrawConfig.isWithdrawEnable()) {
            mTopWithdrawButtonContainer.setVisibility(View.GONE);
        } else {
            switch (LocaleUtil.getLocale().toString()) {
                case "zh_CN"://只留下微信
                case "bo_CN": {
                    mTopWithdrawButtonContainer.removeView(topWithdrawBtnDivider);
                    mTopWithdrawButtonContainer.removeView(topPaypalBtn);
                }
                break;
                case "zh_TW"://按照xml布局
                    break;
                default: {//paypal和微信的位置互换
                    mTopWithdrawButtonContainer.removeView(topWeChatBtn);
                    mTopWithdrawButtonContainer.removeView(topWithdrawBtnDivider);
                    mTopWithdrawButtonContainer.addView(topWithdrawBtnDivider);
                    mTopWithdrawButtonContainer.addView(topWeChatBtn);

                    mCurrencyType = CURRENCY_TYPE_USD;
                }
                break;
            }
        }

        bindEvent(topWeChatBtn);
        bindEvent(topPaypalBtn);
    }

    private void bindEvent(@NonNull final View v) {
        RxView.clicks(v).throttleFirst(CLICK_INTERVAL_SECOND, TimeUnit.SECONDS).subscribe(new Action1<Void>() {
            @Override
            public void call(Void aVoid) {
                onClick(v);
            }
        });
    }

    @Override
    protected void updateProfitView(final UserProfit profit) {
        super.updateProfitView(profit);
        //根据签约状态展示可选视图部分
        if (profit.getSignStatus() == SIGN_STATUS_SIGNED) {
            mBeforeSignedIncomeTv.setText(CommonUtils.getHumanReadableMoney(
                    getExchangeableMoneyInfo(profit, UserProfit.TYPE_CNT).second));
            mBeforeSignedIncomeTipTv.setText(getString(R.string.cur_available_cash,
                    getExchangeableMoneyInfo(profit, UserProfit.TYPE_CNT).first));
            //上半部分
//            mUserTicketTotalTv.setText(String.valueOf(profit.getFrozenUsableTicketCount() + profit.getUsableMibiTicketCount()));
            RxView.clicks(mSignedSeeDetailTv).throttleFirst(CLICK_INTERVAL_SECOND,
                    TimeUnit.SECONDS).subscribe(new Action1<Void>() {
                @Override
                public void call(Void aVoid) {
                    String url = profit.getRedirectUrl();
                    if (url != null && !TextUtils.isEmpty(url = url.trim())) {
                        Intent intent = new Intent(getActivity(), WebViewActivity.class);
                        intent.putExtra(WebViewActivity.EXTRA_URL,
                                LocaleUtil.getWebViewUrl(url));
                        startActivity(intent);
                    }
                }
            });

        }
        //根据账号是否被禁止提现和兑换设置界面
//        boolean enable = profit.getAccountStatus() != UserProfit.ACCOUNT_STATUS_UNAVAILABLE;
    }

    private CharSequence getSignedTip() {
        String tip = getString(R.string.signed_tip);
        SpannableStringBuilder ssb = new SpannableStringBuilder(tip + ".");// 点号为任意的图片占位符，长度任意
        Drawable drawable = getResources().getDrawable(R.drawable.withdraw_signed_tip_arrow);
        int paddingLeft = DisplayUtils.dip2px(3.33f);// 10px
        drawable.setBounds(paddingLeft, 0, drawable.getIntrinsicWidth() + paddingLeft, drawable.getIntrinsicHeight());
        SpanUtils.MyImageSpan myImageSpan = new SpanUtils.MyImageSpan(drawable, ImageSpan.ALIGN_BASELINE, DisplayUtils.dip2px(2.667f));// 8px
        ssb.setSpan(myImageSpan, tip.length() - 1, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        if (v.getTag() == null) {
            return;
        }
        int viewAction = Integer.valueOf(String.valueOf(v.getTag()));
        switch (viewAction) {
            case ACTION_INCOME_GET_ALI_MONEY:
                Bundle bundle = new Bundle();
                bundle.putInt(BUNDLE_PAY_TYPE, UserProfit.TYPE_ALI);
                if (null != mProfitData) {
                    bundle.putDouble(BUNDLE_TADAY_EXCHANGE_MONEY, mProfitData.getTodayExchangeCount());
                    if (null != mProfitData.getAliPayAccount()) {
//                            bundle.putString(BUNDLE_ALI_PAY_ACCOUNT, mProfitData.getAliPayAccount().account);
//                            bundle.putString(BUNDLE_ALI_PAY_NAME, mProfitData.getAliPayAccount().name);
                        WithDrawActivity.openActivity(getActivity(), bundle);
                    } else {
                        FillAliAccountActivity.openActivity(getActivity(), bundle);
                    }
                } else {
                    if (!isLoading) {
                        loadDataFromServer();
                    }
                    ToastUtils.showToast(GlobalData.app(), R.string.withdraw_data_unavailable);
                }
                break;
            case ACTION_INCOME_GET_WX_MONEY:
                if (null != mProfitData) {
                    int exchangeMinCashCntOnetime = mProfitData.getExchangeMinCashCntOnetime() / 100 <= 0 ?
                            2 : mProfitData.getExchangeMinCashCntOnetime() / 100;
                    if (mProfitData.getCurExchangeCount() / 100 < exchangeMinCashCntOnetime) {
                        String text = getString(R.string.toast_account_not_enough_money,
                                exchangeMinCashCntOnetime, getResources().getString(R.string.rmb_unit));
                        ToastUtils.showToast(text);
                        return;
                    }
                    Bundle wxBundle = new Bundle();
                    wxBundle.putDouble(BUNDLE_TADAY_EXCHANGE_MONEY, mProfitData.getTodayExchangeCount());
                    wxBundle.putInt(BUNDLE_PAY_TYPE, UserProfit.TYPE_WX);
                    wxBundle.putInt(KEY_EXCHANGE_MIN_CASH, mProfitData.getExchangeMinCashCntOnetime());
                    wxBundle.putInt(KEY_EXCHANGE_MAX_CASH, mProfitData.getExchangeMaxCashCntOnetime());

                    boolean hasBoundWeChat = mProfitData.getWeixinPayAccount() != null;
                    boolean hasWeChatRealNameAuthenticated = hasBoundWeChat && !mProfitData.getWeixinPayAccount().isNeedBind();

                    if (hasBoundWeChat) {
                        wxBundle.putInt(BUNDLE_VERIFICATION_STATE, mProfitData.getWeixinPayAccount().verification);
                        if (!TextUtils.isEmpty(mProfitData.getWeixinPayAccount().name)) {
                            wxBundle.putString(BUNDLE_BIND_ACCOUNT, mProfitData.getWeixinPayAccount().name);
                        }
                        if (!TextUtils.isEmpty(mProfitData.getWeixinPayAccount().headImgUrl)) {
                            wxBundle.putString(BUNDLE_BIND_AVATAR, mProfitData.getWeixinPayAccount().headImgUrl);
                        }

                        if (hasWeChatRealNameAuthenticated) {
                            // 去提现
                            GetConfigManager.WithdrawConfig withdrawConfig = GetConfigManager.getInstance().getWithdrawConfig();
                            if (withdrawConfig.isH5WithdrawEnable()) {
                                MyLog.w(TAG, "open H5 withdraw");
                                Intent intent = new Intent(getActivity(), WebViewActivity.class);
                                intent.putExtra(WebViewActivity.EXTRA_URL, withdrawConfig.mWeChatH5WithdrawUrl);
                                startActivity(intent);
                            } else if (withdrawConfig.isNativeWithdrawEnable()) {
                                MyLog.d(TAG, "open WithDrawActivity withdraw money");
                                WithDrawActivity.openActivity(getActivity(), wxBundle);
                            } else {
                                MyLog.e(TAG, "either H5 nor native withdraw view applied");
                            }
                        } else {
                            // 绑定
                            MyLog.d(TAG, "open FillWXAccountActivity, rebind wx account");
                            FillWXAccountActivity.openActivity(getActivity(), wxBundle);
                        }
                    } else {
                        // 绑定
                        MyLog.d(TAG, "open FillWXAccountActivity, first bind wx account");
                        FillWXAccountActivity.openActivity(getActivity(), wxBundle);
                    }
                } else {
                    if (!isLoading) {
                        loadDataFromServer();
                    }
                    ToastUtils.showToast(R.string.withdraw_data_unavailable);
                }
                break;
            case ACTION_INCOME_GET_PAYPAL_MONEY:
//                    ToastUtils.showToast(getResources().getString(R.string.account_withdraw_warn));
                if (null != mProfitData) {
                    int exchangeMinCashCntOnetime = mProfitData.getExchangeMinUsdCashCntOnetime() / 100 <= 0 ?
                            2 : mProfitData.getExchangeMinUsdCashCntOnetime() / 100;
                    if (mProfitData.getExchangeUsdcash() / 100 < exchangeMinCashCntOnetime) {
                        String text = getString(R.string.toast_account_not_enough_money,
                                exchangeMinCashCntOnetime, getResources().getString(R.string.usd_unit));
                        ToastUtils.showToast(text);
                        return;
                    }
                    Bundle payPalBundle = new Bundle();
                    payPalBundle.putInt(BUNDLE_PAY_TYPE, UserProfit.TYPE_PAYPAL);
                    payPalBundle.putDouble(BUNDLE_TADAY_EXCHANGE_MONEY, mProfitData.getExchangeUsdcashToday());
                    payPalBundle.putInt(KEY_EXCHANGE_MIN_CASH, mProfitData.getExchangeMinUsdCashCntOnetime());
                    payPalBundle.putInt(KEY_EXCHANGE_MAX_CASH, mProfitData.getExchangeMaxUsdCashCntOnetime());
                    if (null == mProfitData.getPaypalPay()
                            || mProfitData.getPaypalPay().getVertification() < UserProfit.PaypalPay.REAL_NAME_NOT_VERIFIED
                            || mProfitData.getPaypalPay().getVertification() > UserProfit.PaypalPay.ALREADY_REAL_NAME) {
                        MyLog.d(TAG, "open FillWXAccountActivity bind paypal account,bundle:" + payPalBundle.toString());
                        FillPayPalAccountActivity.openActivity(getActivity(), payPalBundle);
                    } else {
                        GetConfigManager.WithdrawConfig withdrawConfig = GetConfigManager.getInstance().getWithdrawConfig();
                        if (withdrawConfig.isH5WithdrawEnable()) {
                            MyLog.w(TAG, "open H5 withdraw");
                            Intent intent = new Intent(getActivity(), WebViewActivity.class);
                            intent.putExtra(WebViewActivity.EXTRA_URL, withdrawConfig.mPayPalH5WithdrawUrl);
                            startActivity(intent);
                        } else if (withdrawConfig.isNativeWithdrawEnable()) {
                            payPalBundle.putInt(BUNDLE_VERIFICATION_STATE, mProfitData.getPaypalPay().getVertification());
                            if (!TextUtils.isEmpty(mProfitData.getPaypalPay().getAccount())) {
                                payPalBundle.putString(BUNDLE_BIND_ACCOUNT, mProfitData.getPaypalPay().getAccount());
                            }
                            MyLog.d(TAG, "open WithDrawActivity withdraw money,bundle:" + payPalBundle.toString());
                            WithDrawActivity.openActivity(getActivity(), payPalBundle);
                        } else {
                            MyLog.e(TAG, "either H5 nor native withdraw view applied");
                        }
                    }
                } else {
                    if (!isLoading) {
                        loadDataFromServer();
                    }
                    ToastUtils.showToast(R.string.withdraw_data_unavailable);
                }
                break;
            case ACTION_INCOME_EXCHANGE_HINT:
                ToastUtils.showToast(R.string.withdraw_hint_toast);
                break;
            default:
                break;
        }
    }

}
