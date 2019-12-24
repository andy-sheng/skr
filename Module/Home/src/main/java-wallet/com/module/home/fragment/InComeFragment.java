package com.module.home.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.module.RouterConstants;
import com.module.home.R;
import com.module.home.event.ExchangeDiamondSuccessEvent;
import com.module.home.event.PhoneAuthSuccessEvent;
import com.module.home.event.WithDrawSuccessEvent;
import com.module.home.inter.IInComeView;
import com.module.home.model.ExChangeInfoModel;
import com.module.home.model.WithDrawInfoModel;
import com.module.home.presenter.InComePresenter;
import com.orhanobut.dialogplus.DialogPlus;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 余额明细
 */
public class InComeFragment extends BaseFragment implements IInComeView {
    public static final int DQ_EXCHANGE_REQ = 100;

    LinearLayout mMainActContainer;
    ExTextView mTvCashDetail;
    ExTextView mTvCashNum;
    ExTextView mStvWithdraw;
    ExTextView mBtnCashToDiamond;
    ExTextView mTvDqDetail;
    ExTextView mTvDqNum;
    ExTextView mBtnExchangeDiamond;
    ExTextView mBtnExchangeCash;

    WithDrawInfoModel mWithDrawInfoModel;

    InComePresenter mInComePresenter;

    DialogPlus mDqRuleDialogPlus;

    float balance = 0; //可用余额

    FragmentDataListener mFragmentDataListener = new FragmentDataListener() {
        @Override
        public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {
            if (requestCode == DQ_EXCHANGE_REQ) {
                mInComePresenter.getBalance();
                mInComePresenter.getDqBalance();
                EventBus.getDefault().post(new ExchangeDiamondSuccessEvent());
            }
        }
    };

    @Override
    public int initView() {
        return R.layout.income_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = getRootView().findViewById(R.id.main_act_container);
        mTvCashDetail = getRootView().findViewById(R.id.tv_cash_detail);
        mTvCashNum = getRootView().findViewById(R.id.tv_cash_num);
        mStvWithdraw = getRootView().findViewById(R.id.stv_withdraw);
        mBtnCashToDiamond = getRootView().findViewById(R.id.btn_cash_to_diamond);
        mTvDqDetail = getRootView().findViewById(R.id.tv_dq_detail);
        mTvDqNum = getRootView().findViewById(R.id.tv_dq_num);
        mBtnExchangeDiamond = getRootView().findViewById(R.id.btn_exchange_diamond);
        mBtnExchangeCash = getRootView().findViewById(R.id.btn_exchange_cash);

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
                float hz = Float.parseFloat(mTvDqNum.getText().toString());
                if (hz <= 0.0) {
                    U.getToastUtil().showShort("无可兑换红钻");
                    return;
                }

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
                if (balance < 10) {
                    U.getToastUtil().showShort("满10元才能提现哦～");
                } else if (mWithDrawInfoModel == null) {
                    U.getToastUtil().showShort("正在加载数据");
                    mInComePresenter.getWithDrawInfo(0);
                } else if (!mWithDrawInfoModel.isIsPhoneAuth()) {
                    ARouter.getInstance()
                            .build(RouterConstants.ACTIVITY_SMS_AUTH)
                            .navigation();
                } else if (!mWithDrawInfoModel.isIsRealAuth()) {
                    mWithDrawInfoModel = null;
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                            .withString(RouterConstants.KEY_WEB_URL, ApiManager.getInstance().findRealUrlByChannel("https://app.inframe.mobi/oauth?from=cash"))
                            .navigation();
                } else {
                    if (!U.getNetworkUtils().hasNetwork()) {
                        U.getToastUtil().showShort("您网络异常！");
                        return;
                    }

                    ARouter.getInstance()
                            .build(RouterConstants.ACTIVITY_WITH_DRAW)
                            .navigation();
                }
            }
        });

        mBtnCashToDiamond.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (balance <= 0) {
                    U.getToastUtil().showShort("暂无可兑换余额");
                } else if (mWithDrawInfoModel == null) {
                    U.getToastUtil().showShort("正在加载数据");
                    mInComePresenter.getWithDrawInfo(0);
                } else {
                    if (!U.getNetworkUtils().hasNetwork()) {
                        U.getToastUtil().showShort("您网络异常！");
                        return;
                    }

                    U.getFragmentUtils().addFragment(
                            FragmentUtils.newAddParamsBuilder(getActivity(), ExChangeCashToDiamondFragment.class)
                                    .setAddToBackStack(true)
                                    .setHasAnimation(true)
                                    .addDataBeforeAdd(0, balance)
                                    .setFragmentDataListener(mFragmentDataListener)
                                    .build());
                }
            }
        });

        mBtnExchangeCash.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                float hz = Float.parseFloat(mTvDqNum.getText().toString());
                if (hz < 1) {
                    U.getToastUtil().showShort("红钻数量少于1个无法兑换哦");
                    return;
                }

                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(getActivity(), ExChangeCashFragment.class)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .setFragmentDataListener(mFragmentDataListener)
                                .build());
            }
        });

        mInComePresenter = new InComePresenter(this);
        addPresent(mInComePresenter);
        mInComePresenter.getBalance();
        mInComePresenter.getDqBalance();
        mInComePresenter.getWithDrawInfo(0);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mWithDrawInfoModel == null) {
            mInComePresenter.getWithDrawInfo(0);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(WithDrawSuccessEvent event) {
        mInComePresenter.getBalance();
    }

    @Override
    public void showCash(String availableBalance) {
        mTvCashNum.setText(availableBalance);
        if (!TextUtils.isEmpty(availableBalance)) {
            balance = Float.parseFloat(availableBalance);
        }
    }

    @Override
    public void showDq(String dq) {
        mTvDqNum.setText(dq);
    }

    @Override
    public void showRule(ExChangeInfoModel exChangeInfoModel) {
        MyLog.d(getTAG(), "showRule" + " exChangeInfoModel=" + exChangeInfoModel);
        LinearLayout ruleOneArea = (LinearLayout) mDqRuleDialogPlus.findViewById(R.id.rule_one_area);
        ExTextView toHZDescTv = (ExTextView) mDqRuleDialogPlus.findViewById(R.id.toHZDescTv);
        LinearLayout ruleTwoArea = (LinearLayout) mDqRuleDialogPlus.findViewById(R.id.rule_two_area);
        ExTextView toZSDescTv = (ExTextView) mDqRuleDialogPlus.findViewById(R.id.toZSDescTv);
        LinearLayout ruleThreeArea = (LinearLayout) mDqRuleDialogPlus.findViewById(R.id.rule_three_area);
        ExTextView toCashDescTv = (ExTextView) mDqRuleDialogPlus.findViewById(R.id.toCashDescTv);
        LinearLayout ruleFourArea = (LinearLayout) mDqRuleDialogPlus.findViewById(R.id.rule_four_area);
        ExTextView ruleFour = (ExTextView) mDqRuleDialogPlus.findViewById(R.id.rule_four);

        LinearLayout[] linearLayouts = new LinearLayout[]{ruleOneArea, ruleTwoArea, ruleThreeArea, ruleFourArea};
        ExTextView[] exTextViews = new ExTextView[]{toHZDescTv, toZSDescTv, toCashDescTv, ruleFour};

        for (int i = 0; i < (exChangeInfoModel.getRule().size() > 4 ? 4 : exChangeInfoModel.getRule().size()); i++) {
            linearLayouts[i].setVisibility(View.VISIBLE);
            exTextViews[i].setText(exChangeInfoModel.getRule().get(i));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(PhoneAuthSuccessEvent event) {
        mWithDrawInfoModel.setIsPhoneAuth(true);
    }

    @Override
    public void showWithDrawInfo(WithDrawInfoModel withDrawInfoModel) {
        mWithDrawInfoModel = withDrawInfoModel;
    }

    @Override
    public boolean useEventBus() {
        return true;
    }
}
