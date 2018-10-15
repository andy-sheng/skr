package com.wali.live.watchsdk.income.income;

import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.log.MyLog;
import com.base.utils.language.LocaleUtil;
import com.mi.live.data.config.GetConfigManager;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.income.UserProfit;
import com.wali.live.watchsdk.webview.WebViewActivity;

import static com.wali.live.watchsdk.income.UserProfit.SIGN_STATUS_SIGNED;


/**
 * 印度地区提现
 * Created by rongzhisheng on 16-12-2.
 */

public class IndiaIncomeFragment extends BaseIncomeFragment {
    private View mWithdrawArea;
    private TextView mNotEnoughTicketTipTv;
    private TextView mGetRewardBtn;
    private TextView mToptipBt;
    /**
     * 星票兑换钻石
     */
    protected View mExchangeGemBtn;

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        return inflater.inflate(R.layout.user_income_fragment_india, container, false);
    }

    @Override
    protected void bindOwnView() {
//        mTitleBar.getRightTextBtn().setVisibility(View.GONE);
        loadDataFromServer();
    }

    @Override
    protected void initSignedView() {
        super.initSignedView();
        mToptipBt = $(R.id.after_signed_balance_tip);
        mToptipBt.setVisibility(View.VISIBLE);
    }

    @Override
    protected void initView() {
        super.initView();
        mWithdrawArea = $(R.id.withdraw_area);
        mNotEnoughTicketTipTv = $(R.id.not_enough_ticket_tip);
        mGetRewardBtn = $(R.id.withdraw_india_get_reward);
        mGetRewardBtn.setOnClickListener(this);
        mExchangeGemBtn = $(R.id.exchange_btn);
        mExchangeGemBtn.setTag(ACTION_INCOME_EXCHANGE_GEM);
        mExchangeGemBtn.setOnClickListener(this);
    }

    @Override
    protected void updateProfitView(final UserProfit profit) {
        super.updateProfitView(profit);
        // 如果账户结算中不可提现和兑换
        // 如果是签约主播
        if (profit.getSignStatus() == SIGN_STATUS_SIGNED) {
            mSignedSeeDetailTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String url = profit.getRedirectUrl();
                    if (url != null && !TextUtils.isEmpty(url = url.trim())) {
                        Intent intent = new Intent(getActivity(), WebViewActivity.class);
                        intent.putExtra(WebViewActivity.EXTRA_URL, LocaleUtil.getWebViewUrl(url));
                        startActivity(intent);
                    }
                }
            });
        } else {// 未签约
            if (profit.getAccountStatus() == UserProfit.ACCOUNT_STATUS_UNAVAILABLE) {
                mGetRewardBtn.setEnabled(false);// 禁用提现
                mExchangeGemBtn.setEnabled(false);// 禁用星票兑换钻石
            }
            GetConfigManager.IndiaWithdrawConfig withdrawConfig = GetConfigManager.getInstance().getIndiaWithdrawConfig();
            if (withdrawConfig != null) {
                if (!withdrawConfig.isWithdrawEnable()) {
                    mNotEnoughTicketTipTv.setVisibility(View.GONE);
                } else if (profit.getTicketCount() >= withdrawConfig.getWithdrawThreshold()) {
                    // 展示提现按钮
                    mNotEnoughTicketTipTv.setVisibility(View.GONE);
                    mGetRewardBtn.setVisibility(View.VISIBLE);
                } else {
                    // 票不足以提现
                    mNotEnoughTicketTipTv.setVisibility(View.VISIBLE);
                }
            } else {
                MyLog.e(getTAG(), "india withdraw config is null");
            }

//            mUseableShowTv.setText(String.valueOf(profit.getTicketCount()));
        }
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        int i = v.getId();
        if (i == R.id.withdraw_india_get_reward) {
            Intent intent = new Intent(getActivity(), WebViewActivity.class);
            intent.putExtra(WebViewActivity.EXTRA_URL, "http://activity.zb.mi.com/live/india/tx.html");
            startActivity(intent);

        }
    }
}
