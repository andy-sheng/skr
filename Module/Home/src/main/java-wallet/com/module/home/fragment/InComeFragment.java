package com.module.home.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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
import com.common.utils.ToastUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.dialog.view.StrokeTextView;
import com.module.RouterConstants;
import com.module.home.R;
import com.module.home.adapter.RechargeAdapter;
import com.module.home.event.PhoneAuthSuccessEvent;
import com.module.home.event.WithDrawSuccessEvent;
import com.module.home.inter.IBallanceView;
import com.module.home.inter.IInComeView;
import com.module.home.model.ExChangeInfoModel;
import com.module.home.model.RechargeItemModel;
import com.module.home.model.WithDrawInfoModel;
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
                float hz = Float.parseFloat(mTvDqNum.getText().toString());
                if (hz <= 0.0) {
                    ToastUtils.showShort("无可兑换红钻");
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
                            .withString(RouterConstants.KEY_WEB_URL, U.getChannelUtils().getUrlByChannel("http://app.inframe.mobi/face/faceauth"))
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

        mBtnExchangeCash.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                float hz = Float.parseFloat(mTvDqNum.getText().toString());
                if (hz < 1) {
                    ToastUtils.showShort("红钻数量少于1个无法兑换哦");
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
        MyLog.d(TAG, "showRule" + " exChangeInfoModel=" + exChangeInfoModel);
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
