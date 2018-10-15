package com.base.view;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by lan on 15/12/25.
 * 和IconSlidingTabLayout一起使用，用来支持图标
 */
public abstract class IconFragmentPagerAdapter extends FragmentPagerAdapter {
    public IconFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return null;
    }

    @Override
    public int getCount() {
        return 0;
    }

    public abstract int getIconResId(int position);
}
