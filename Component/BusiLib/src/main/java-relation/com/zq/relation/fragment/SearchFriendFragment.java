package com.zq.relation.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.common.base.BaseFragment;
import com.component.busilib.R;

public class SearchFriendFragment extends BaseFragment {

    @Override
    public int initView() {
        return R.layout.search_friends_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
