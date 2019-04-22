package com.module.home.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.core.pay.EPayPlatform;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.dialog.view.StrokeTextView;
import com.module.RouterConstants;
import com.module.home.R;
import com.module.home.adapter.RechargeAdapter;
import com.module.home.inter.IBallanceView;
import com.module.home.inter.IInComeView;
import com.module.home.model.RechargeItemModel;
import com.module.home.presenter.BallencePresenter;
import com.module.home.presenter.InComePresenter;
import com.respicker.view.GridSpacingItemDecoration;

import java.util.List;

/**
 * 余额明细
 */
public class InComeFragment extends BaseFragment implements IInComeView {

    LinearLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    ExTextView mTvCashDetail;
    ExTextView mTvCashNum;
    StrokeTextView mStvWithdraw;
    ImageView mIvAttention;
    ExTextView mTvDqDetail;
    ExTextView mTvDqNum;
    StrokeTextView mBtnExchangeDiamond;
    StrokeTextView mBtnExchangeCash;

    InComePresenter mInComePresenter;

    @Override
    public int initView() {
        return R.layout.income_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (LinearLayout) mRootView.findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mTvCashDetail = (ExTextView) mRootView.findViewById(R.id.tv_cash_detail);
        mTvCashNum = (ExTextView) mRootView.findViewById(R.id.tv_cash_num);
        mStvWithdraw = (StrokeTextView) mRootView.findViewById(R.id.stv_withdraw);
        mIvAttention = (ImageView) mRootView.findViewById(R.id.iv_attention);
        mTvDqDetail = (ExTextView) mRootView.findViewById(R.id.tv_dq_detail);
        mTvDqNum = (ExTextView) mRootView.findViewById(R.id.tv_dq_num);
        mBtnExchangeDiamond = (StrokeTextView) mRootView.findViewById(R.id.btn_exchange_diamond);
        mBtnExchangeCash = (StrokeTextView) mRootView.findViewById(R.id.btn_exchange_cash);

        mTvCashDetail.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(getActivity(), CashDetailFragment.class)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .build());
            }
        });

        mTvDqDetail.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(getActivity(), DqDetailFragment.class)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .build());
            }
        });

        mBtnExchangeDiamond.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(getActivity(), ExChangeDiamondFragment.class)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .build());
            }
        });

        mStvWithdraw.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_WITH_DRAW)
                        .navigation();
            }
        });

        mBtnExchangeCash.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(getActivity(), ExChangeCashFragment.class)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .build());
            }
        });

        mInComePresenter = new InComePresenter(this);
        addPresent(mInComePresenter);
        mInComePresenter.getBalance();
        mInComePresenter.getDqBalance();
    }

    @Override
    public void showCash(String availableBalance) {
        mTvCashNum.setText(availableBalance);
    }

    @Override
    public void showDq(float dq) {
        mTvDqNum.setText(String.format("%.1f", dq));
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
