package com.module.home.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.home.R;
import com.module.home.updateinfo.EditInfoActivity;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;


public class SettingFragment extends BaseFragment {

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    RelativeLayout mEditPerson;
    RelativeLayout mClearCache;
    RelativeLayout mUserFeedback;
    RelativeLayout mServiceAgreen;
    RelativeLayout mComment;
    ExTextView mExitLogin;
    ExTextView mVersion;

    @Override
    public int initView() {
        return R.layout.setting_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mEditPerson = (RelativeLayout) mRootView.findViewById(R.id.edit_person);
        mClearCache = (RelativeLayout) mRootView.findViewById(R.id.clear_cache);
        mUserFeedback = (RelativeLayout) mRootView.findViewById(R.id.user_feedback);
        mServiceAgreen = (RelativeLayout) mRootView.findViewById(R.id.service_agreen);
        mComment = (RelativeLayout) mRootView.findViewById(R.id.comment);
        mExitLogin = (ExTextView) mRootView.findViewById(R.id.exit_login);
        mVersion = (ExTextView) mRootView.findViewById(R.id.version);

        RxView.clicks(mTitlebar.getLeftTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getFragmentUtils().popFragment(SettingFragment.this);
                    }
                });

        RxView.clicks(mEditPerson)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        Intent intent = new Intent(getActivity(), EditInfoActivity.class);
                        startActivity(intent);
                    }
                });

        RxView.clicks(mClearCache)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {

                    }
                });


        RxView.clicks(mUserFeedback)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {

                    }
                });

        RxView.clicks(mServiceAgreen)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {

                    }
                });

        RxView.clicks(mComment)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {

                    }
                });

        RxView.clicks(mExitLogin)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {

                    }
                });
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
