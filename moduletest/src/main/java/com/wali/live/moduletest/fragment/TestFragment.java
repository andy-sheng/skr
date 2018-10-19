package com.wali.live.moduletest.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.utils.U;
import com.wali.live.moduletest.R;

public class TestFragment extends BaseFragment {

    TextView mDescTv;

    RecyclerView mRecyclerView;
    @Override
    public int initView() {
        return R.layout.test_test_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mDescTv = (TextView) mRootView.findViewById(R.id.desc_tv);
        mDescTv.setText(getRequestCode() + "");

    }

    @Override
    protected boolean onBackPressed() {
        U.getFragmentUtils().popFragment(getActivity());
        return true;
    }

    @Override
    public boolean useEventBus() {
        return  false;
    }
}
