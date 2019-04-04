package com.zq.person.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.common.base.BaseFragment;
import com.component.busilib.R;

public class PersonInfoCardFragment extends BaseFragment {
    @Override
    public int initView() {
        return R.layout.person_info_card_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
