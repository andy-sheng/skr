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
import com.common.utils.ToastUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.common.view.titlebar.CommonTitleBar;
import com.dialog.view.StrokeTextView;
import com.module.home.R;
import com.module.home.inter.IExChangeCashView;
import com.module.home.model.ExChangeInfoModel;
import com.module.home.presenter.ExChangeCashPresenter;
import com.zq.toast.CommonToastView;

import static com.module.home.fragment.InComeFragment.DQ_EXCHANGE_REQ;

/**
 * 兑换现金
 */
public class ExChangeCashFragment extends BaseFragment implements IExChangeCashView {
    public static final int HF = 100000;
    CommonTitleBar mTitlebar;
    ExTextView mTvMaxExchange;
    NoLeakEditText mEditCashNum;
    ExTextView mTvTip;
    ExTextView mTvExchangeWhole;
    StrokeTextView mIvExchangeBtn;
    ExTextView mTvExchangeRole;

    ExChangeCashPresenter mExChangeCashPresenter;

    //单位为毫分
    long mMaxExchangeCash;
    //目前的红钻
    float mDq;

    ExChangeInfoModel mExChangeInfoModel;

    Handler mUiHandler = new Handler();

    @Override
    public int initView() {
        return R.layout.exchange_cash_fragment_layout;
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
                mExChangeCashPresenter.exChange(stringToHaoFen(mEditCashNum.getText().toString()));
            }
        });

        mTvExchangeWhole.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                float f = hfToYuan(mMaxExchangeCash);
                if(f > 10000){
                    ToastUtils.showShort("一次最多兑换10000元");
                    return;
                }

                mEditCashNum.setText(String.format("%.2f", hfToYuan(mMaxExchangeCash)));
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

                if (checkInputNum(editString)) {
                    long cash = stringToHaoFen(editString);
                    if (cash > 10000 * HF) {
                        mEditCashNum.setText(beforeTextChanged);
                        mEditCashNum.setSelection(beforeTextChanged.length() - 1);
                        ToastUtils.showShort("一次最多兑换10000元");
                        return;
                    }

                    if (mExChangeInfoModel == null && !TextUtils.isEmpty(editString)) {
                        mEditCashNum.setText("");
                        return;
                    }

                    if (mExChangeInfoModel != null) {
                        if (TextUtils.isEmpty(mEditCashNum.getText().toString())) {
                            mTvTip.setText(String.format("红钻余额%.1f，", mDq));
                            mTvExchangeWhole.setVisibility(View.VISIBLE);
                            mTvTip.setTextColor(Color.parseColor("#ff3b4e79"));
                            mIvExchangeBtn.setEnabled(false);
                        } else if (cash > mMaxExchangeCash) {
                            mTvTip.setText("已超过可兑换红钻余额");
                            mTvTip.setTextColor(U.getColor(R.color.red));
                            mTvExchangeWhole.setVisibility(View.GONE);
                            mIvExchangeBtn.setEnabled(false);
                        } else {
                            mTvTip.setText(String.format("红钻余额%.1f，", mDq));
                            mTvTip.setTextColor(Color.parseColor("#ff3b4e79"));
                            mTvExchangeWhole.setVisibility(View.VISIBLE);
                            mIvExchangeBtn.setEnabled(true);
                        }

                    }
                }
            }
        });

        mUiHandler.postDelayed(() -> {
            mEditCashNum.setFocusable(true);
            mEditCashNum.requestFocus();
            U.getKeyBoardUtils().showSoftInputKeyBoard(getActivity(), mEditCashNum);
        }, 500);

        mIvExchangeBtn.setEnabled(false);
        mExChangeCashPresenter = new ExChangeCashPresenter(this);
        addPresent(mExChangeCashPresenter);
        mExChangeCashPresenter.getDQBalance();
    }

    @Override
    public void exChangeFailed(String errorMsg) {
        ToastUtils.showShort(errorMsg);
    }

    @Override
    public void exChangeSuccess() {
        U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                .setImage(R.drawable.touxiangshezhichenggong_icon)
                .setText("兑换成功")
                .build());
        mExChangeCashPresenter.getDQBalance();
        if (mFragmentDataListener != null) {
            mFragmentDataListener.onFragmentResult(DQ_EXCHANGE_REQ, 0, null, null);
        }
        finish();
    }

    @Override
    public void showExChangeInfo(ExChangeInfoModel exChangeInfoModel) {
        mExChangeInfoModel = exChangeInfoModel;
        mDq = Float.parseFloat(exChangeInfoModel.getDqBalance().getTotalAmountStr());
        mMaxExchangeCash = (long) (exChangeInfoModel.getToRMBRatio() * mDq);
        mTvMaxExchange.setText(String.format("账户最多可兑换%.2f元人民币", hfToYuan(mMaxExchangeCash)));
        mTvTip.setText(String.format("红钻余额%.1f，", mDq));
        mTvExchangeRole.setText("兑换汇率：" + exChangeInfoModel.getToRMBDesc());
    }

    public float hfToYuan(long hf) {
        hf = hf - (hf % 1000);
        return (float) hf / HF;
    }

    private long stringToHaoFen(String floatString) {
        if (TextUtils.isEmpty(floatString)) {
            return 0;
        }

        return (long) (Float.parseFloat(floatString) * HF);
    }

    /**
     * 检查输入的数字是否合法
     *
     * @param editString
     * @return
     */
    private boolean checkInputNum(String editString) {
        //不可以以 . 开始
        if (editString.startsWith(".")) {
            mEditCashNum.setText("");
            return false;
        }

        if (!TextUtils.isEmpty(editString)) {
            //01 02这样的情况
            if (editString.startsWith("0") && !editString.equals("0") && !editString.startsWith("0.")) {
                mEditCashNum.setText("0");
                mEditCashNum.setSelection("0".length());
                return false;
            }

            if (editString.contains(".") && !editString.endsWith(".")) {
                //小数点后面只能有两位
                String floatNum = editString.split("\\.")[1];
                String intNum = editString.split("\\.")[0];
                if (floatNum.length() > 2) {
                    floatNum = floatNum.substring(0, 2);
                    String text = intNum + "." + floatNum;
                    mEditCashNum.setText(text);
                    mEditCashNum.setSelection(text.length());
                    return false;
                }
            }
        }

        return true;
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
