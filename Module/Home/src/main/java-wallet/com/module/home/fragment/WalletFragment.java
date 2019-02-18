package com.module.home.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.SpannableStringBuilder;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.home.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

public class WalletFragment extends BaseFragment {

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    TextView mBalanceTv;
    TextView mWithdrawTv;

    @Override
    public int initView() {
        return R.layout.wallet_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mBalanceTv = (TextView) mRootView.findViewById(R.id.balance_tv);
        mWithdrawTv = (TextView) mRootView.findViewById(R.id.withdraw_tv);

        RxView.clicks(mTitlebar.getLeftTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getFragmentUtils().popFragment(WalletFragment.this);
                    }
                });

        RxView.clicks(mTitlebar.getRightTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getToastUtil().showShort("暂无提现记录");
                    }
                });


        RxView.clicks(mWithdrawTv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getToastUtil().showShort("提现功能正在开发哦～");
                    }
                });

        SpannableStringBuilder stringBuilder = new SpanUtils()
                .append("0.00").setFontSize(50, true)
                .append("元").setFontSize(14, true)
                .create();
        mBalanceTv.setText(stringBuilder);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
