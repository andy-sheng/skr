package com.module.home.updateinfo.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.common.base.BaseFragment;
import com.module.home.R;

public class EditInfoAgeFragment2 extends BaseFragment {

    @Override
    public int initView() {
        return R.layout.edit_info_age_fragment2;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
