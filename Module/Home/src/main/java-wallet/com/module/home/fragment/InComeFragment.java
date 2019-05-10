package com.module.home.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.core.pay.EPayPlatform;
import com.common.log.MyLog;
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
import com.module.home.event.WithDrawSuccessEvent;
import com.module.home.inter.IBallanceView;
import com.module.home.inter.IInComeView;
import com.module.home.model.ExChangeInfoModel;
import com.module.home.model.RechargeItemModel;
import com.module.home.presenter.BallencePresenter;
import com.module.home.presenter.InComePresenter;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.respicker.view.GridSpacingItemDecoration;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * 余额明细
 */
public class InComeFragment extends BaseFragment implements IInComeView {
    public static final int DQ_EXCHANGE_REQ = 100;

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

    DialogPlus mDqRuleDialogPlus;

    FragmentDataListener mFragmentDataListener = new FragmentDataListener() {
        @Override
        public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {
            if (requestCode == DQ_EXCHANGE_REQ) {
                mInComePresenter.getBalance();
                mInComePresenter.getDqBalance();
            }
        }
    };

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

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

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
                                .setFragmentDataListener(mFragmentDataListener)
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
                                .setFragmentDataListener(mFragmentDataListener)
                                .build());
            }
        });

        mIvAttention.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mDqRuleDialogPlus == null) {
                    mDqRuleDialogPlus = DialogPlus.newDialog(getActivity())
                            .setContentHolder(new ViewHolder(R.layout.dq_rule_layout))
                            .setGravity(Gravity.CENTER)
                            .setContentBackgroundResource(R.color.transparent)
                            .setOverlayBackgroundResource(R.color.black_trans_80)
                            .setExpanded(false)
                            .setCancelable(true)
                            .create();
                }

                mDqRuleDialogPlus.show();
                mInComePresenter.getRule();
            }
        });

        mInComePresenter = new InComePresenter(this);
        addPresent(mInComePresenter);
        mInComePresenter.getBalance();
        mInComePresenter.getDqBalance();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(WithDrawSuccessEvent event) {
        mInComePresenter.getBalance();
    }

    @Override
    public void showCash(String availableBalance) {
        mTvCashNum.setText(availableBalance);
    }

    @Override
    public void showDq(String dq) {
        mTvDqNum.setText(dq);
    }

    @Override
    public void showRule(ExChangeInfoModel exChangeInfoModel) {
        MyLog.d(TAG, "showRule" + " exChangeInfoModel=" + exChangeInfoModel);
        ExTextView toHZDescTv = (ExTextView)mDqRuleDialogPlus.findViewById(R.id.toHZDescTv);
        ExTextView toZSDescTv = (ExTextView)mDqRuleDialogPlus.findViewById(R.id.toZSDescTv);
        ExTextView toCashDescTv = (ExTextView)mDqRuleDialogPlus.findViewById(R.id.toCashDescTv);
        toHZDescTv.setText("钻石红钻兑换汇率：" + exChangeInfoModel.getToDQDesc());
        toZSDescTv.setText("红钻兑换钻石汇率：" + exChangeInfoModel.getToZSDesc());
        toCashDescTv.setText("红钻兑换现金汇率：" + exChangeInfoModel.getToRMBDesc());


    }

    @Override
    public boolean useEventBus() {
        return true;
    }
}
