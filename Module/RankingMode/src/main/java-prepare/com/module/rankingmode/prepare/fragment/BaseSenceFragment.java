package com.module.rankingmode.prepare.fragment;

import android.view.View;

import com.common.base.BaseFragment;

/**
 * 这里主要处理选歌，准备和匹配界面的一些动画的base，先写着，
 */
public abstract class BaseSenceFragment extends BaseFragment {

    @Override
    public void onResume() {
        super.onResume();
        mRootView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRootView.setVisibility(View.GONE);
    }
}
