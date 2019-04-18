package com.component.busilib.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.component.busilib.R;
import com.component.busilib.friends.GrabFriendsRoomFragment;
import com.module.RouterConstants;

@Route(path = RouterConstants.ACTIVITY_FRIEND_ROOM)
public class GrabFriendRoomActivity extends BaseActivity {
    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.empty_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, GrabFriendsRoomFragment.class)
                .setAddToBackStack(false)
                .setHasAnimation(false)
                .build());
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
