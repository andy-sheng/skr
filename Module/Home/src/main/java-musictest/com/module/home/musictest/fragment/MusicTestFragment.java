package com.module.home.musictest.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.common.base.BaseFragment;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.home.R;
import com.module.home.musictest.MusicTestServerApi;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

// 音乐测试首页
public class MusicTestFragment extends BaseFragment {

    CommonTitleBar mTitlebar;
    ExTextView mStartTv;

    @Override
    public int initView() {
        return R.layout.music_test_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mStartTv = (ExTextView) mRootView.findViewById(R.id.start_tv);

        RxView.clicks(mTitlebar.getLeftTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getFragmentUtils().popFragment(MusicTestFragment.this);
                    }
                });

        RxView.clicks(mStartTv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        U.getFragmentUtils().addFragment(
                                FragmentUtils.newAddParamsBuilder(getActivity(), MusicQuestionFragment.class)
                                        .setAddToBackStack(true)
                                        .setHasAnimation(false)
                                        .build());

                        U.getFragmentUtils().popFragment(new FragmentUtils.PopParams.Builder()
                                .setPopFragment(MusicTestFragment.this)
                                .setPopAbove(false)
                                .setHasAnimation(false)
                                .build());
                    }
                });

        initStart();
    }

    private void initStart() {
        MusicTestServerApi mMusicTestServerApi = ApiManager.getInstance().createService(MusicTestServerApi.class);
        // 检测是否测试过
        ApiMethods.subscribe(mMusicTestServerApi.checkHasTest(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    boolean hasTest = result.getData().getBoolean("ok");
                    if (hasTest) {
                        mStartTv.setText("重新开始");
                    } else {
                        mStartTv.setText("开始");
                    }
                }
            }
        }, this);
    }

    @Override
    protected boolean onBackPressed() {
        U.getFragmentUtils().popFragment(this);
        return true;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
