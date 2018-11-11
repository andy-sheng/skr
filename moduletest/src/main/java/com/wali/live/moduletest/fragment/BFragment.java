package com.wali.live.moduletest.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.base.BuildConfig;
import com.common.statistics.TimeStatistics;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.wali.live.moduletest.R;
import com.xiaomi.mistatistic.sdk.MiStatInterface;

public class BFragment extends BaseFragment {


    @Override
    public int initView() {
        return 0;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mRootView = new ExTextView(getContext());
        ((ExTextView) mRootView).setText("BFragment");
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
