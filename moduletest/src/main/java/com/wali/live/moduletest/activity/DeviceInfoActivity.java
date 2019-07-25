package com.wali.live.moduletest.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.module.RouterConstants;
import com.common.log.MyLog;
import com.common.view.viewpager.NestViewPager;
import com.wali.live.moduletest.R;
import com.wali.live.moduletest.fragment.AFragment;
import com.wali.live.moduletest.fragment.BFragment;
import com.wali.live.moduletest.fragment.DeviceInfoFragment;

@Route(path = RouterConstants.ACTIVITY_DEVICE_INFO)
public class DeviceInfoActivity extends BaseActivity {

    NestViewPager mViewpager;

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.test_device_info_activity_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mViewpager = findViewById(R.id.viewpager);
        /**
         * 1.FragmentPagerAdapter和FragmentPagerStateAdapter的区别，使用场景
         * FragmentPagerAdapter将每一个生成的Fragment保存在内存中，limit外Fragment没有销毁，生命周期为onPause->onStop->onDestroyView,onCreateView->onStart->onResume，但Fragment的成员变量都没有变，所以可以缓存根View，避免重复inflate。
         * FragmentStatePagerAdapter对limit外的Fragment销毁，生命周期为onPause->onStop->onDestoryView->onDestory->onDetach, onAttach->onCreate->onCreateView->onStart->onResume。
         */
        FragmentPagerAdapter fragmentPagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                MyLog.d(getTAG(), "getItem" + " position=" + position);

                if (position == 0) {
                    return new DeviceInfoFragment();
                } else if (position == 1) {
                    return new AFragment();
                } else if (position == 2) {
                    return new BFragment();
                }
                return null;
            }

            @Override
            public int getCount() {
                return 3;
            }
        };

        mViewpager.setAdapter(fragmentPagerAdapter);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public boolean canSlide() {
        return true;
    }
}
