package com.common.core.login.fragment;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.callback.Callback;
import com.common.core.R;
import com.common.core.account.UserAccountManager;
import com.common.core.account.UserAccountServerApi;
import com.common.core.account.event.LoginApiErrorEvent;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.permission.SkrBasePermission;
import com.common.core.permission.SkrPhoneStatePermission;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.FragmentUtils;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.common.view.ex.drawable.DrawableCreator;
import com.module.RouterConstants;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * 手机方式登陆界面
 */
public class LoginByPhoneFragment extends BaseFragment implements Callback {

    public static final String PREF_KEY_PHONE_NUM = "pref_key_phone_num";

    RelativeLayout mMainActContainer;
    ExImageView mIvBack;
    NoLeakEditText mPhoneInputTv;
    NoLeakEditText mCodeInputTv;
    ExTextView mGetCodeTv;
    ExTextView mErrorHint;
    ExTextView mLoginIv;

    String mPhoneNumber; //发送验证码的电话号码
    String mCode; //验证码

    HandlerTaskTimer mTaskTimer; // 倒计时验证码

    SkrBasePermission mSkrPermission = new SkrPhoneStatePermission();

    Drawable mGrayDrawable = new DrawableCreator.Builder()
            .setCornersRadius(U.getDisplayUtils().dip2px(25))
            .setSolidColor(Color.parseColor("#DBD8CD"))
            .build();

    Drawable mYellowDraable = new DrawableCreator.Builder()
            .setCornersRadius(U.getDisplayUtils().dip2px(25))
            .setSolidColor(Color.parseColor("#FFC15B"))
            .build();

    @Override
    public int initView() {
        return R.layout.core_phone_login_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = mRootView.findViewById(R.id.main_act_container);
        mIvBack = mRootView.findViewById(R.id.iv_back);
        mPhoneInputTv = mRootView.findViewById(R.id.phone_input_tv);
        mCodeInputTv = mRootView.findViewById(R.id.code_input_tv);
        mGetCodeTv = mRootView.findViewById(R.id.get_code_tv);
        mErrorHint = mRootView.findViewById(R.id.error_hint);
        mLoginIv = mRootView.findViewById(R.id.login_iv);

        mPhoneInputTv.setText(U.getPreferenceUtils().getSettingString(PREF_KEY_PHONE_NUM, ""));

        if (TextUtils.isEmpty(mPhoneInputTv.getText().toString().trim())) {
            mPhoneInputTv.requestFocus();
        } else {
            mCodeInputTv.requestFocus();
        }

        mGetCodeTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mPhoneNumber = mPhoneInputTv.getText().toString().trim();
                if (checkPhoneNumber(mPhoneNumber)) {
                    sendSmsVerifyCode(mPhoneNumber);
                }
            }
        });

        mLoginIv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                mPhoneNumber = mPhoneInputTv.getText().toString().trim();
                mCode = mCodeInputTv.getText().toString().trim();
                if (checkPhoneNumber(mPhoneNumber)) {
                    if (!TextUtils.isEmpty(mCode)) {
                        if (!U.getNetworkUtils().hasNetwork()) {
                            setHintText("网络异常，请检查网络之后重试！", true);
                        } else {
                            if (U.getChannelUtils().getChannel().startsWith("MI_SHOP_mimusic")) {
                                // 小米商店渠道，需要获取读取imei权限
                                mSkrPermission.ensurePermission(new Runnable() {
                                    @Override
                                    public void run() {
                                        UserAccountManager.getInstance().loginByPhoneNum(mPhoneNumber, mCode, LoginByPhoneFragment.this);
                                    }
                                }, true);
                            } else {
                                UserAccountManager.getInstance().loginByPhoneNum(mPhoneNumber, mCode, LoginByPhoneFragment.this);
                            }
                        }
                    } else {
                        setHintText("验证码为空", true);
                    }
                }
            }
        });

        mIvBack.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                stopTimeTask();
                U.getFragmentUtils().popFragment(new FragmentUtils.PopParams.Builder()
                        .setPopFragment(LoginByPhoneFragment.this)
                        .setHasAnimation(true)
                        .build());
            }
        });

        mLoginIv.setClickable(false);

        mLoginIv.postDelayed(new Runnable() {
            @Override
            public void run() {
                U.getKeyBoardUtils().showSoftInputKeyBoard(getActivity());
            }
        }, 200);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSkrPermission.onBackFromPermisionManagerMaybe(getActivity());
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LoginApiErrorEvent event) {
        MyLog.d(TAG, "onEventMainThread" + " event=" + event);
        setHintText(event.getErrmsg(), true);
    }

    private void setHintText(String text, boolean isError) {
        if (isError) {
            mErrorHint.setTextColor(Color.parseColor("#EDADC5"));
        } else {
            mErrorHint.setTextColor(Color.WHITE);
        }
        mErrorHint.setText(text);
    }

    private void sendSmsVerifyCode(final String phoneNumber) {
        MyLog.d(TAG, "sendSmsVerifyCode" + " phoneNumber=" + phoneNumber);
        if (!U.getNetworkUtils().hasNetwork()) {
            setHintText("网络异常，请检查网络后重试!", true);
            return;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            return;
        }

        UserAccountServerApi userAccountServerApi = ApiManager.getInstance().createService(UserAccountServerApi.class);

        long timeMs = System.currentTimeMillis();
        String sign = U.getMD5Utils().MD5_32("skrer|sms|" +
                String.valueOf(phoneNumber) + "|" +
                String.valueOf(timeMs));
        if (userAccountServerApi != null) {
            ApiMethods.subscribe(userAccountServerApi.sendSmsVerifyCode(phoneNumber, timeMs, sign), new ApiObserver<ApiResult>() {
                @Override
                public void process(ApiResult result) {
                    if (result.getErrno() == 0) {
                        // 发送验证码成功
                        setHintText("验证码发送成功", false);
                        U.getPreferenceUtils().setSettingString(PREF_KEY_PHONE_NUM, phoneNumber);
                        mGetCodeTv.setSelected(true);
                        mGetCodeTv.setClickable(false);
                        mGetCodeTv.setBackground(mGrayDrawable);
                        mCodeInputTv.setFocusable(true);
                        mCodeInputTv.setFocusableInTouchMode(true);
                        mCodeInputTv.requestFocus();
                        mLoginIv.setClickable(true);
                        mLoginIv.setBackground(mYellowDraable);
                        startTimeTask();
                    } else {
                        setHintText(result.getErrmsg(), true);
                    }
                }
            }, this);
        }
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
                        mGetCodeTv.setText((60 - integer) + "s");
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        mGetCodeTv.setText("获取验证码");
                        mGetCodeTv.setSelected(false);
                        mGetCodeTv.setClickable(true);
                        mGetCodeTv.setBackground(mYellowDraable);

                        mLoginIv.setClickable(false);
                        mLoginIv.setBackground(mGrayDrawable);
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
        if (!TextUtils.isEmpty(phoneNumber)
                && phoneNumber.startsWith("1")
                && phoneNumber.length() == 11
                && TextUtils.isDigitsOnly(phoneNumber)
        ) {
            return true;
        } else if (TextUtils.isEmpty(phoneNumber)) {
            setHintText("手机号为空", true);
        } else {
            setHintText("手机号有误", true);
        }

        return false;
    }

    @Override
    protected void onFragmentVisible() {
        super.onFragmentVisible();
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
        U.getFragmentUtils().popFragment(new FragmentUtils.PopParams.Builder()
                .setPopFragment(LoginByPhoneFragment.this)
                .setHasAnimation(true)
                .build());
        return true;
    }

    @Override
    public void onCallback(int r, Object obj) {
        MyLog.d(TAG, "onCallback" + " r=" + r + " obj=" + obj);
        if (r == 1) {
            // 表示登录成功了，这里判断下跳转
            if (UserAccountManager.getInstance().hasAccount()) {
                if (MyUserInfoManager.getInstance().hasMyUserInfo() && MyUserInfoManager.getInstance().isUserInfoFromServer()) {
                    // 如果有账号了
                    if (MyUserInfoManager.getInstance().isNeedCompleteInfo()) {
                        boolean isUpAc = U.getActivityUtils().getTopActivity().getClass().getSimpleName().equals("UploadAccountInfoActivity");
                        if (!isUpAc) {
                            // 顶层的不是这个activity
                            ARouter.getInstance().build(RouterConstants.ACTIVITY_UPLOAD)
                                    .greenChannel().navigation();
                        } else {
                            MyLog.d(TAG, "顶部已经是UploadAccountInfoActivity");
                        }
                    }
                }
            }
        }
    }
}
