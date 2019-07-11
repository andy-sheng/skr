package com.module.playways.songmanager.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.common.base.BaseFragment;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.titlebar.CommonTitleBar;
import com.module.playways.R;

public class DoubleExistSongManageFragment extends BaseFragment {

    CommonTitleBar mTitlebar;

    @Override
    public int initView() {
        return R.layout.double_exist_song_manage_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().popFragment(DoubleExistSongManageFragment.this);
            }
        });
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
