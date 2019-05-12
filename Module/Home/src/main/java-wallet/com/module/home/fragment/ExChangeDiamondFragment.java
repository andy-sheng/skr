package com.module.home.fragment;

import android.graphics.Color;
import android.os.Bundle;

import android.os.Handler;
import android.support.annotation.Nullable;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;

import com.common.base.BaseFragment;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.common.view.titlebar.CommonTitleBar;
import com.dialog.view.StrokeTextView;
import com.module.home.R;
import com.module.home.inter.IExchangeDiamomdView;
import com.module.home.model.ExChangeInfoModel;
import com.module.home.presenter.ExChangeDiamondPresenter;
import com.zq.toast.CommonToastView;

import static com.module.home.fragment.InComeFragment.DQ_EXCHANGE_REQ;

/**
 * 兑换钻石
 */
public class ExChangeDiamondFragment extends BaseFragment implements IExchangeDiamomdView {
    CommonTitleBar mTitlebar;
    ExTextView mTvMaxExchange;
    NoLeakEditText mEditCashNum;
    ExTextView mTvTip;
    ExTextView mTvExchangeWhole;
    StrokeTextView mIvExchangeBtn;
    ExTextView mTvExchangeRole;

    Handler mUiHandler = new Handler();

    ExChangeDiamondPresenter mExChangeDiamondPresenter;

    //能兑换的最大钻石
    long mMaxDiamond = 0;
    //目前剩余的红钻
    float mDq = 0;

    @Override
    public int initView() {
        return R.layout.exchange_diamond_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mTvMaxExchange = (ExTextView) mRootView.findViewById(R.id.tv_max_exchange);
        mEditCashNum = (NoLeakEditText) mRootView.findViewById(R.id.edit_cash_num);
        mTvTip = (ExTextView) mRootView.findViewById(R.id.tv_tip);
        mTvExchangeWhole = (ExTextView) mRootView.findViewById(R.id.tv_exchange_whole);
        mIvExchangeBtn = mRootView.findViewById(R.id.iv_exchange_btn);
        mTvExchangeRole = (ExTextView) mRootView.findViewById(R.id.tv_exchange_role);

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                finish();
            }
        });

        mIvExchangeBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mExChangeDiamondPresenter.exChange(Long.parseLong(mEditCashNum.getText().toString()));
            }
        });

        mTvExchangeWhole.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mEditCashNum.setText(String.valueOf(mMaxDiamond));
                mEditCashNum.setSelection(mEditCashNum.length());
            }
        });

        mEditCashNum.addTextChangedListener(new TextWatcher() {
            String beforeTextChanged = "";

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                beforeTextChanged = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String editString = s.toString();
                if (TextUtils.isEmpty(editString)) {
                    mIvExchangeBtn.setEnabled(false);
                    mTvTip.setTextColor(Color.parseColor("#ff3b4e79"));
                    mTvTip.setText(String.format("红钻余额%.1f，", mDq));
                    mTvExchangeWhole.setVisibility(View.VISIBLE);
                    return;
                }

                long inputNum = Long.parseLong(editString);
                if (inputNum > mMaxDiamond) {
                    mIvExchangeBtn.setEnabled(false);
                    mTvTip.setTextColor(U.getColor(R.color.red));
                    mTvTip.setText("已超过可兑换红钻余额");
                    mTvExchangeWhole.setVisibility(View.GONE);
                } else {
                    mIvExchangeBtn.setEnabled(true);
                    mTvTip.setTextColor(Color.parseColor("#ff3b4e79"));
                    mTvTip.setText(String.format("红钻余额%.1f，", mDq));
                    mTvExchangeWhole.setVisibility(View.VISIBLE);
                }
            }
        });

        mIvExchangeBtn.setEnabled(false);

        mUiHandler.postDelayed(() -> {
            mEditCashNum.setFocusable(true);
            mEditCashNum.requestFocus();
            U.getKeyBoardUtils().showSoftInputKeyBoard(getActivity(), mEditCashNum);
        }, 500);

        mExChangeDiamondPresenter = new ExChangeDiamondPresenter(this);
        addPresent(mExChangeDiamondPresenter);
        mExChangeDiamondPresenter.getDQBalance();
    }

    @Override
    public void exChangeSuccess() {
        U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                .setImage(R.drawable.touxiangshezhichenggong_icon)
                .setText("兑换成功")
                .build());
        mExChangeDiamondPresenter.getDQBalance();
        if (mFragmentDataListener != null) {
            mFragmentDataListener.onFragmentResult(DQ_EXCHANGE_REQ, 0, null, null);
        }
        finish();
    }

    @Override
    public void exChangeFailed(String errorMsg) {
        U.getToastUtil().showShort(errorMsg);
    }

    @Override
    public void showDQ(ExChangeInfoModel exChangeInfoModel) {
        mDq = Float.parseFloat(exChangeInfoModel.getDqBalance().getTotalAmountStr());
        mMaxDiamond = (long) mDq * exChangeInfoModel.getToZSRatio();
        mTvMaxExchange.setText(String.format("账户最多可兑换%d钻石", mMaxDiamond));
        mTvTip.setText(String.format("红钻余额%.1f，", mDq));
        mTvExchangeRole.setText("兑换汇率：" + exChangeInfoModel.getToZSDesc());
    }

    @Override
    public void onStop() {
        super.onStop();
        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        mUiHandler.removeCallbacksAndMessages(null);
    }
}
