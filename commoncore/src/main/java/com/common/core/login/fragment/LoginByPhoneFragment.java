package com.common.core.login.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.core.R;
import com.common.core.account.UserAccountManager;
import com.common.core.account.UserAccountServerApi;
import com.common.core.account.event.VerifyCodeErrorEvent;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.FragmentUtils;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.jakewharton.rxbinding2.view.RxView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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

    HandlerTaskTimer mTaskTimer; // 倒计时验证码

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
                        }
                    }
                });


        RxView.clicks(mIvBack)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                        U.getFragmentUtils().popFragment(new FragmentUtils.PopParams.Builder()
                                .setPopFragment(LoginByPhoneFragment.this)
                                .setPopAbove(false)
                                .setHasAnimation(true)
                                .setNotifyShowFragment(LoginFragment.class)
                                .build());
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(VerifyCodeErrorEvent event) {
        MyLog.d(TAG, "onEventMainThread" + " event=" + event);
        if (event.getErrno() == 101) {
            U.getToastUtil().showShort("验证码错误");
        }
    }


    private void sendSmsVerifyCode(final String phoneNumber) {
        MyLog.d(TAG, "sendSmsVerifyCode" + " phoneNumber=" + phoneNumber);
        if (!U.getNetworkUtils().hasNetwork()) {
            U.getToastUtil().showShort("网络异常，请检查网络后重试!");
            return;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
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
                    mCodeInputTv.setFocusable(true);
                    mCodeInputTv.setFocusableInTouchMode(true);
                    mCodeInputTv.requestFocus();
                    startTimeTask();
                } else {
                    U.getToastUtil().showShort(result.getErrmsg());
                }
            }
        }, this);
    }


    /**
     * 更新准备时间倒计时
     */
    public void startTimeTask() {
        mTaskTimer = HandlerTaskTimer.newBuilder()
                .interval(1000)
                .take(60)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        mGetCodeTv.setText("倒计时:" + (60 - integer) + "s");
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        mGetCodeTv.setText("获取验证码");
                        mGetCodeTv.setSelected(false);
                        mGetCodeTv.setClickable(true);
                    }
                });
    }

    public void stopTimeTask() {
        if (mTaskTimer != null) {
            mTaskTimer.dispose();
        }
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
        } else {
            U.getToastUtil().showShort("手机号有误");
        }

        return false;
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public void destroy() {
        super.destroy();
        stopTimeTask();
    }

    @Override
    protected boolean onBackPressed() {
        stopTimeTask();
        U.getFragmentUtils().popFragment(new FragmentUtils.PopParams.Builder()
                .setPopFragment(LoginByPhoneFragment.this)
                .setPopAbove(false)
                .setHasAnimation(true)
                .setNotifyShowFragment(LoginFragment.class)
                .build());
        return true;
    }
}
