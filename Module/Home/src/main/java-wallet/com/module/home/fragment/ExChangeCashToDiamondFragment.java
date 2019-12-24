package com.module.home.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.common.view.titlebar.CommonTitleBar;
import com.component.toast.CommonToastView;
import com.dialog.view.StrokeTextView;
import com.module.home.R;
import com.module.home.WalletServerApi;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.module.home.fragment.InComeFragment.DQ_EXCHANGE_REQ;

/**
 * 余额兑换钻石
 */
public class ExChangeCashToDiamondFragment extends BaseFragment {
    CommonTitleBar mTitlebar;
    //    ExTextView mTvMaxExchange;
    NoLeakEditText mEditCashNum;
    ExTextView mTvTip;
    ExTextView mTvExchangeWhole;
    StrokeTextView mIvExchangeBtn;
    ExTextView mTvExchangeRole;
    ImageView mClearIv;

    Handler mUiHandler = new Handler();

    WalletServerApi mWalletServerApi = ApiManager.getInstance().createService(WalletServerApi.class);

    //能兑换的最大钻石
    float mMaxDiamond = 0;
    //目前剩余的红钻
    float mDq = 0;

    int toZSRadio = 0;

    float balance = 0; //可用余额


    @Override
    public int initView() {
        return R.layout.exchange_cash_to_diamond_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) getRootView().findViewById(R.id.titlebar);
        mEditCashNum = (NoLeakEditText) getRootView().findViewById(R.id.edit_cash_num);
        mTvTip = (ExTextView) getRootView().findViewById(R.id.tv_tip);
        mTvExchangeWhole = (ExTextView) getRootView().findViewById(R.id.tv_exchange_whole);
        mIvExchangeBtn = getRootView().findViewById(R.id.iv_exchange_btn);
        mTvExchangeRole = (ExTextView) getRootView().findViewById(R.id.tv_exchange_role);
        mClearIv = (ImageView) getRootView().findViewById(R.id.clear_iv);

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                finish();
            }
        });

        mIvExchangeBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                exChange(Float.parseFloat(mEditCashNum.getText().toString()));
            }
        });

        mTvExchangeWhole.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mEditCashNum.setText(String.valueOf(mDq));
                mEditCashNum.setSelection(mEditCashNum.length());
            }
        });

        mClearIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mEditCashNum.setText("");
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
                    if (TextUtils.isEmpty(editString)) {
                        mIvExchangeBtn.setEnabled(false);
                        mTvTip.setTextColor(Color.parseColor("#ff3b4e79"));
                        mTvTip.setText(String.format("余额%.1f元", mDq));
                        mTvExchangeWhole.setVisibility(View.VISIBLE);
                        mClearIv.setVisibility(View.GONE);
                        mEditCashNum.setTextSize(16);
                        return;
                    }

                    mEditCashNum.setTextSize(24);
                    float inputNum = Float.parseFloat(editString);
                    if (inputNum > mDq) {
                        mIvExchangeBtn.setEnabled(false);
                        mTvTip.setTextColor(U.getColor(R.color.red));
                        mTvTip.setText("已超过可兑换余额");
                        mTvExchangeWhole.setVisibility(View.VISIBLE);
                        mClearIv.setVisibility(View.VISIBLE);
                    } else {
                        mTvTip.setTextColor(Color.parseColor("#ff3b4e79"));
                        mTvTip.setText(String.format("预计可兑换%.1f钻石", inputNum * (float) toZSRadio / 1000f));
                        mTvExchangeWhole.setVisibility(View.VISIBLE);
                        mClearIv.setVisibility(View.VISIBLE);
                        if (inputNum == 0) {
                            mIvExchangeBtn.setEnabled(false);
                        } else {
                            mIvExchangeBtn.setEnabled(true);
                        }
                    }
                }
            }
        });

        mIvExchangeBtn.setEnabled(false);

        mUiHandler.postDelayed(() -> {
            mEditCashNum.setFocusable(true);
            mEditCashNum.requestFocus();
            U.getKeyBoardUtils().showSoftInputKeyBoard(getActivity(), mEditCashNum);
        }, 500);

        mDq = balance;
        mMaxDiamond = mDq * 10;
        toZSRadio = 10000;
        mTvTip.setText(String.format("余额%.1f元", mDq));
    }

    public void exChangeSuccess() {
        U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
                .setImage(R.drawable.touxiangshezhichenggong_icon)
                .setText("兑换成功")
                .build());

        if (getFragmentDataListener() != null) {
            getFragmentDataListener().onFragmentResult(DQ_EXCHANGE_REQ, 0, null, null);
        }

        finish();
    }

    @Override
    public void onStop() {
        super.onStop();
        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
    }

    public void exChange(float diamondNum) {
        if (diamondNum == 0) {
            return;
        }

        HashMap<String, Object> map = new HashMap<>();
        map.put("amount", (long) (diamondNum * 1000));

        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(mWalletServerApi.exChangeCashToDiamond(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    exChangeSuccess();
                } else {
                    U.getToastUtil().showShort(result.getErrmsg());
                }
            }

            @Override
            public void onError(Throwable e) {
                U.getToastUtil().showShort(e.getMessage());
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                U.getToastUtil().showShort("网络异常");
            }
        }, this);
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
                if (floatNum.length() > 1) {
                    floatNum = floatNum.substring(0, 1);
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
    public void setData(int type, @org.jetbrains.annotations.Nullable Object data) {
        if (type == 0) {
            balance = (float) data;
        }
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
