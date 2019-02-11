package com.module.home.setting.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.core.login.view.SeparatedEditText;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.common.view.titlebar.CommonTitleBar;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.home.R;
import com.module.home.feedback.FeedbackFragment;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

public class InviteCodeFragment extends BaseFragment {

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    SeparatedEditText mInviteCodeSpet;
    NoLeakEditText mPhoneInputTv;
    NoLeakEditText mCodeInputTv;
    ExTextView mGetCodeTv;
    ExTextView mErrorHint;
    ExTextView mSubmitTv;

    @Override
    public int initView() {
        return R.layout.invite_code_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mInviteCodeSpet = (SeparatedEditText) mRootView.findViewById(R.id.invite_code_spet);
        mPhoneInputTv = (NoLeakEditText) mRootView.findViewById(R.id.phone_input_tv);
        mCodeInputTv = (NoLeakEditText) mRootView.findViewById(R.id.code_input_tv);
        mGetCodeTv = (ExTextView) mRootView.findViewById(R.id.get_code_tv);
        mErrorHint = (ExTextView) mRootView.findViewById(R.id.error_hint);
        mSubmitTv = (ExTextView) mRootView.findViewById(R.id.submit_tv);

        RxView.clicks(mTitlebar.getLeftTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                        U.getFragmentUtils().popFragment(InviteCodeFragment.this);
                    }
                });
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
