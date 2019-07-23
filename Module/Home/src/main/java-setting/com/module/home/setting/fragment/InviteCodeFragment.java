package com.module.home.setting.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.fastjson.JSON;
import com.common.base.BaseFragment;
import com.common.core.login.view.SeparatedEditText;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.common.view.titlebar.CommonTitleBar;
import com.module.home.R;
import com.module.home.setting.InviteServerApi;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class InviteCodeFragment extends BaseFragment {

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    SeparatedEditText mInviteCodeSpet;
    NoLeakEditText mPhoneInputTv;
    NoLeakEditText mCodeInputTv;
    ExTextView mGetCodeTv;
    ExTextView mErrorHint;
    ExTextView mSubmitTv;

    String mPhoneNumber; //发送验证码的电话号码
    String mSmsCode;     //验证码
    String mInviteCode;  //邀请码

    HandlerTaskTimer mTaskTimer; // 倒计时验证码

    @Override
    public int initView() {
        return R.layout.invite_code_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) getRootView().findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) getRootView().findViewById(R.id.titlebar);
        mInviteCodeSpet = (SeparatedEditText) getRootView().findViewById(R.id.invite_code_spet);
        mPhoneInputTv = (NoLeakEditText) getRootView().findViewById(R.id.phone_input_tv);
        mCodeInputTv = (NoLeakEditText) getRootView().findViewById(R.id.code_input_tv);
        mGetCodeTv = (ExTextView) getRootView().findViewById(R.id.get_code_tv);
        mErrorHint = (ExTextView) getRootView().findViewById(R.id.error_hint);
        mSubmitTv = (ExTextView) getRootView().findViewById(R.id.submit_tv);


        mInviteCodeSpet.setTextChangedListener(new SeparatedEditText.TextChangedListener() {
            @Override
            public void textChanged(CharSequence changeText) {
                mInviteCode = changeText.toString();
            }

            @Override
            public void textCompleted(CharSequence text) {
                mInviteCode = text.toString();
                mPhoneInputTv.requestFocus();
            }
        });

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                U.getFragmentUtils().popFragment(InviteCodeFragment.this);
            }
        });
        mGetCodeTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mPhoneNumber = mPhoneInputTv.getText().toString().trim();
                if (check(mPhoneNumber, mInviteCode)) {
                    getInviteSmsCode(mPhoneNumber, mInviteCode);
                }
            }
        });
        mSubmitTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                sumbitInvite();
            }
        });

        mSubmitTv.setClickable(false);
    }

    private void sumbitInvite() {
        mPhoneNumber = mPhoneInputTv.getText().toString().trim();
        mSmsCode = mCodeInputTv.getText().toString().trim();

        if (!check(mPhoneNumber, mInviteCode)) {
            return;
        }

        if (TextUtils.isEmpty(mSmsCode)) {
            U.getToastUtil().showShort("验证码为空");
            return;
        }

        InviteServerApi inviteServerApi = ApiManager.getInstance().createService(InviteServerApi.class);
        HashMap<String, Object> map = new HashMap<>();
        map.put("phoneNum", mPhoneNumber);
        map.put("smsCode", mSmsCode);
        map.put("inviteCode", mInviteCode);
        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
        ApiMethods.subscribe(inviteServerApi.submitInviteCode(body), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    boolean isSucess = result.getData().getBoolean("isSuccess");
                    if (isSucess) {
                        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                        U.getToastUtil().showShort("提交邀请码成功");
                        U.getFragmentUtils().popFragment(InviteCodeFragment.this);
                    } else {
                        String failedMsg = result.getData().getString("failedMsg");
                        U.getToastUtil().showShort(failedMsg);
                    }
                }
            }

            @Override
            public void onNetworkError(ErrorType errorType) {
                super.onNetworkError(errorType);
                U.getToastUtil().showShort("网络异常，请检查网络后重试!");
            }
        }, this);

    }

    private void getInviteSmsCode(final String phoneNumber, String inviteCode) {
        MyLog.d(getTAG(), "getInviteSmsCode" + " phoneNumber=" + phoneNumber + " inviteCode=" + inviteCode);
        if (!U.getNetworkUtils().hasNetwork()) {
            U.getToastUtil().showShort("网络异常，请检查网络后重试!");
            return;
        }

        InviteServerApi inviteServerApi = ApiManager.getInstance().createService(InviteServerApi.class);

        long timeMs = System.currentTimeMillis();
        String sign = U.getMD5Utils().MD5_32("skrer|invite|" +
                String.valueOf(phoneNumber) + "|" +
                String.valueOf(timeMs));

        ApiMethods.subscribe(inviteServerApi.getInviteSmsCode(phoneNumber, String.valueOf(timeMs), sign), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    // 发送验证码成功
                    U.getToastUtil().showShort("验证码发送成功");
                    mGetCodeTv.setSelected(true);
                    mGetCodeTv.setClickable(false);
                    mCodeInputTv.setFocusable(true);
                    mCodeInputTv.setFocusableInTouchMode(true);
                    mCodeInputTv.requestFocus();
                    mSubmitTv.setClickable(true);
                    mSubmitTv.setBackgroundResource(com.common.core.R.drawable.img_btn_bg_yellow);
                    startTimeTask();
                } else {
                    U.getToastUtil().showShort(result.getErrmsg());
                }
            }
        }, this);
    }

    /**
     * 更新验证码倒计时
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

    private boolean check(String phoneNumber, String inviteCode) {
        if (TextUtils.isEmpty(inviteCode)) {
            U.getToastUtil().showShort("邀请码为空");
            return false;
        }

        if (inviteCode.length() != 6) {
            U.getToastUtil().showShort("邀请码有误");
            return false;
        }

        if (checkPhoneNumber(phoneNumber)) {
            return true;
        }

        return false;
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
        } else if (TextUtils.isEmpty(phoneNumber)) {
            U.getToastUtil().showShort("手机号为空");
        } else {
            U.getToastUtil().showShort("手机号有误");
        }

        return false;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        stopTimeTask();
    }

    @Override
    public boolean onBackPressed() {
        stopTimeTask();
        return super.onBackPressed();
    }
}
