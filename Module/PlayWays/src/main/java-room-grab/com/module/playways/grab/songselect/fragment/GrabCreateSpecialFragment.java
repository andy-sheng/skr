package com.module.playways.grab.songselect.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.common.base.BaseFragment;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.module.playways.grab.songselect.view.SpecialSelectView;
import com.module.rank.R;

/**
 * 选择房间属性
 */
public class GrabCreateSpecialFragment extends BaseFragment {

    ExImageView mIvBack;
    SpecialSelectView mSpecialView;


    @Override
    public int initView() {
        return R.layout.grab_create_specail_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mIvBack = (ExImageView) mRootView.findViewById(R.id.iv_back);
        mSpecialView = (SpecialSelectView) mRootView.findViewById(R.id.special_view);

        mIvBack.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                U.getFragmentUtils().popFragment(GrabCreateSpecialFragment.this);
            }
        });
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
