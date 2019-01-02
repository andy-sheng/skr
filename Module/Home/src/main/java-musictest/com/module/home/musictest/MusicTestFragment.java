package com.module.home.musictest;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;

import com.common.base.BaseFragment;
import com.common.view.titlebar.CommonTitleBar;
import com.module.home.R;

public class MusicTestFragment extends BaseFragment {

    CommonTitleBar mTitlebar;
    ViewPager mAnswerVp;

    @Override
    public int initView() {
        return R.layout.music_test_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar)mRootView.findViewById(R.id.titlebar);
        mAnswerVp = (ViewPager)mRootView.findViewById(R.id.answer_vp);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
