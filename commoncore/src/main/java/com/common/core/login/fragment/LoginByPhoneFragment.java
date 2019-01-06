package com.common.core.login.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.common.base.BaseFragment;
import com.common.core.R;
import com.common.core.account.UserAccountManager;
import com.common.core.account.UserAccountServerApi;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.FragmentUtils;
import com.common.utils.ToastUtils;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

/**
 * 手机方式登陆界面
 */
public class LoginByPhoneFragment extends BaseFragment {

    RelativeLayout mMainActContainer;
    ExImageView mIvBack;
    NoLeakEditText mPhoneInputTv;
    NoLeakEditText mCodeInputTv;
    ExTextView mGetCodeTv;
    ExTextView mLoginTv;

    String mPhoneNumber; //发送验证码的电话号码
    String mCode; //验证码

    @Override
    public int initView() {
        return R.layout.core_phone_login_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mIvBack = (ExImageView) mRootView.findViewById(R.id.iv_back);
        mPhoneInputTv = (NoLeakEditText) mRootView.findViewById(R.id.phone_input_tv);
        mCodeInputTv = (NoLeakEditText) mRootView.findViewById(R.id.code_input_tv);
        mGetCodeTv = (ExTextView) mRootView.findViewById(R.id.get_code_tv);
        mLoginTv = (ExTextView) mRootView.findViewById(R.id.login_tv);

        RxView.clicks(mGetCodeTv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        mPhoneNumber = mPhoneInputTv.getText().toString().trim();
                        if (checkPhoneNumber(mPhoneNumber)) {
                            sendSmsVerifyCode(mPhoneNumber);
                        }
                    }
                });

        RxView.clicks(mLoginTv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                        mPhoneNumber = mPhoneInputTv.getText().toString().trim();
                        mCode = mCodeInputTv.getText().toString().trim();
                        if (checkPhoneNumber(mPhoneNumber)) {
                            if (!TextUtils.isEmpty(mCode)) {
                                UserAccountManager.getInstance().loginByPhoneNum(mPhoneNumber, mCode);
                            } else {
                                U.getToastUtil().showShort("验证码为空");
                            }
                        } else {
                            U.getToastUtil().showShort("手机号有误");
                        }

                    }
                });


        RxView.clicks(mIvBack)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getFragmentUtils().popFragment(new FragmentUtils.PopParams.Builder()
                                .setPopFragment(LoginByPhoneFragment.this)
                                .setPopAbove(false)
                                .setHasAnimation(true)
                                .setNotifyShowFragment(LoginFragment.class)
                                .build());
                    }
                });
    }


    private void sendSmsVerifyCode(final String phoneNumber) {
        MyLog.d(TAG, "sendSmsVerifyCode" + " phoneNumber=" + phoneNumber);
        if (TextUtils.isEmpty(phoneNumber)) {
            MyLog.d(TAG, "sendSmsVerifyCode" + " phoneNumber=" + phoneNumber);
            return;
        }

        UserAccountServerApi userAccountServerApi = ApiManager.getInstance().createService(UserAccountServerApi.class);
        ApiMethods.subscribe(userAccountServerApi.sendSmsVerifyCode(phoneNumber), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    // 发送验证码成功
                    mGetCodeTv.setSelected(true);
                    mGetCodeTv.setClickable(false);
                }
            }
        }, this);
    }

    /**
     * 检查手机号是否正确
     *
     * @return
     */
    private boolean checkPhoneNumber(String phoneNumber) {
        if (!TextUtils.isEmpty(phoneNumber) && TextUtils.isDigitsOnly(phoneNumber)
                && phoneNumber.length() == 11 && phoneNumber.startsWith("1")) {
            return true;
        }

        return false;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    protected boolean onBackPressed() {
        U.getFragmentUtils().popFragment(new FragmentUtils.PopParams.Builder()
                .setPopFragment(LoginByPhoneFragment.this)
                .setPopAbove(false)
                .setHasAnimation(true)
                .setNotifyShowFragment(LoginFragment.class)
                .build());
        return true;
    }
}
