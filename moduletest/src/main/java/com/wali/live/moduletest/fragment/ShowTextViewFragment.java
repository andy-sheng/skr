package com.wali.live.moduletest.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.base.BuildConfig;
import com.common.statistics.TimeStatistics;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.wali.live.moduletest.R;

public class ShowTextViewFragment extends BaseFragment {

    @Override
    public int initView() {
        return R.layout.test_fragment_show_textview_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
