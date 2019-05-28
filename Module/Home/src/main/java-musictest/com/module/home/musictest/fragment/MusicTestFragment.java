package com.module.home.musictest.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.common.base.BaseFragment;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
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

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        mStartTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
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

        U.getSoundUtils().preLoad(TAG, R.raw.normal_back);
    }

    private void initStart() {
        MusicTestServerApi mMusicTestServerApi = ApiManager.getInstance().createService(MusicTestServerApi.class);
        // 检测是否测试过
        ApiMethods.subscribe(mMusicTestServerApi.checkHasTest(), new ApiObserver<ApiResult>() {
            @Override
            public void process(ApiResult result) {
                if (result.getErrno() == 0) {
                    boolean hasTest = result.getData().getBooleanValue("ok");
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
    public void destroy() {
        super.destroy();
        U.getSoundUtils().release(TAG);
    }

    @Override
    protected boolean onBackPressed() {
        if (getActivity() != null) {
            getActivity().finish();
        }
        return true;
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
