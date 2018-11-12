package com.example.emoji;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.common.core.RouterConstants;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.viewpager.NestViewPager;
import com.example.drawer.DrawerFragment;
import com.wali.live.moduletest.R;
import com.wali.live.moduletest.activity.TestSdkActivity;
import com.wali.live.moduletest.fragment.AFragment;
import com.wali.live.moduletest.fragment.BFragment;
import com.wali.live.moduletest.fragment.DeviceInfoFragment;

@Route(path = RouterConstants.ACTIVITY_EMOJI)
public class EmojiActivity extends BaseActivity {

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return 0;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        U.getFragmentUtils().addFragment(FragmentUtils
                .newParamsBuilder(EmojiActivity.this, EmojiFragment.class)
                .setAddToBackStack(false)
                .build());
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public boolean canSlide() {
        return false;
    }
}
