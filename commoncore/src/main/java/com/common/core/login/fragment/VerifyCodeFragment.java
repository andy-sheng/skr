package com.common.core.login.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.TimeUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.core.R;
import com.common.core.account.UserAccountManager;
import com.common.core.account.UserAccountServerApi;
import com.common.core.account.event.VerifyCodeErrorEvent;
import com.common.core.login.view.SeparatedEditText;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.trello.rxlifecycle2.android.FragmentEvent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class VerifyCodeFragment extends BaseFragment {

    public final static String TAG = "VerifyCodeFragment";
    public final static String EXTRA_PHONE_NUMBER = "phone_number";

    public final static int COUNT_DOWN_DEAFAULT = 60;

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    RelativeLayout mMainContainer;
    ExTextView mLogoTv;
    SeparatedEditText mVerifyCodeSpet;  // 验证码框
    ExTextView mCountDownTv; //倒计时和重新发送
    ExTextView mVerifyHintTv;  //验证码发送提示
    ExTextView mVerifyErrorTv;  //验证码验证出错提示

    String phoneNumber; //发送验证码的电话号码

    @Override
    public int initView() {
        return R.layout.core_verify_code_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mMainContainer = (RelativeLayout) mRootView.findViewById(R.id.main_container);
        mLogoTv = (ExTextView) mRootView.findViewById(R.id.logo_tv);
        mVerifyCodeSpet = (SeparatedEditText) mRootView.findViewById(R.id.verify_code_spet);
        mCountDownTv = (ExTextView) mRootView.findViewById(R.id.count_down_tv);
        mVerifyHintTv = (ExTextView) mRootView.findViewById(R.id.verify_hint_tv);
        mVerifyErrorTv = (ExTextView) mRootView.findViewById(R.id.verify_error_tv);

        Bundle bundle = getArguments();
        if (bundle != null) {
            phoneNumber = bundle.getString(EXTRA_PHONE_NUMBER, "");
            sendSmsVerifyCode(phoneNumber);
        }

        mVerifyCodeSpet.setTextChangedListener(new SeparatedEditText.TextChangedListener() {
            @Override
            public void textChanged(CharSequence changeText) {

            }

            @Override
            public void textCompleted(CharSequence text) {
                if (!TextUtils.isEmpty(phoneNumber) && !TextUtils.isEmpty(text)) {
                    stopCountDown();
                    UserAccountManager.getInstance().loginByPhoneNum(phoneNumber, text.toString());
                }
            }
        });

        mCountDownTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (U.getCommonUtils().isFastDoubleClick()) {
                    return;
                }
                // 重新发送验证码
                mVerifyCodeSpet.clearText();
                sendSmsVerifyCode(phoneNumber);
            }
        });
        mCountDownTv.setClickable(false);
    }

    private Disposable disposable;

    /**
     * 初始化倒计时
     */
    private void initCounDown(final int count) {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }

        mCountDownTv.setClickable(false);
        Observable.interval(0, 1, TimeUnit.SECONDS)
                .take(count + 1)
                .map(new Function<Long, Long>() {
                    @Override
                    public Long apply(Long aLong) throws Exception {
                        return count - aLong;
                    }
                }).doOnSubscribe(new Consumer<Disposable>() {
            @Override
            public void accept(Disposable disposable) throws Exception {

            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(Long aLong) {
                        mCountDownTv.setText(String.format("%ds", aLong));
                    }

                    @Override
                    public void onError(Throwable e) {
                    }

                    @Override
                    public void onComplete() {
                        MyLog.d(TAG, "onComplete");
                        mCountDownTv.setClickable(true);
                        mCountDownTv.setText("重新发送");
                        mCountDownTv.setTextColor(U.app().getResources().getColor(R.color.green));
                    }
                });
    }

    /**
     * 停止倒计时
     */
    private void stopCountDown() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
        mCountDownTv.setClickable(true);
        mCountDownTv.setText("重新发送");
        mCountDownTv.setTextColor(U.app().getResources().getColor(R.color.green));
    }

    private void sendSmsVerifyCode(final String phoneNumber) {
        MyLog.d(TAG, "sendSmsVerifyCode" + " phoneNumber=" + phoneNumber);
        if (TextUtils.isEmpty(phoneNumber)) {
            MyLog.d(TAG, "sendSmsVerifyCode" + " phoneNumber=" + phoneNumber);
            return;
        }
        initCounDown(COUNT_DOWN_DEAFAULT);
        UserAccountServerApi userAccountServerApi = ApiManager.getInstance().createService(UserAccountServerApi.class);
        userAccountServerApi.sendSmsVerifyCode(phoneNumber)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this.<ApiResult>bindUntilEvent(FragmentEvent.DESTROY))
                .subscribe(new ApiObserver<ApiResult>() {
                    @Override
                    public void process(ApiResult result) {
                        if (result.getErrno() == 0) {
                            mVerifyHintTv.setVisibility(View.VISIBLE);
                            mVerifyHintTv.setText(String.format("验证码已发送至%s", phoneNumber));
                        }
                    }
                });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(VerifyCodeErrorEvent event) {
        MyLog.d(TAG, "onEventMainThread" + " event=" + event);
        mVerifyErrorTv.setVisibility(View.VISIBLE);
        mVerifyErrorTv.setText("验证码输错了，再确认一下吧 ～");
    }
}
