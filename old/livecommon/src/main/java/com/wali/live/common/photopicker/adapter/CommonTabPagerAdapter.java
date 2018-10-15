package com.wali.live.common.photopicker.adapter;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.base.log.MyLog;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 15-11-26.
 */
public class CommonTabPagerAdapter extends PagerAdapter {
    private static final String TAG = CommonTabPagerAdapter.class.getSimpleName();

    private List<String> mTitleList = new ArrayList();
    private List<View> mLayoutList = new ArrayList();

    public View getView(int position) {
        if (position >= 0 && position < mLayoutList.size()) {
            return mLayoutList.get(position);
        } else {
            return null;
        }
    }

    public void addView(String title, View layout) {
        mTitleList.add(title);
        mLayoutList.add(layout);
    }

    public void removeAll() {
        mTitleList.clear();
        mLayoutList.clear();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return mTitleList == null ? 0 : mTitleList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mTitleList.get(position);
    }

    public void updatePageTitle(int postion, String title) {
        if (postion >= 0 && postion < mTitleList.size()) {
            mTitleList.set(postion, title);
        }
    }

    @Override
    public void destroyItem(ViewGroup arg0, int arg1, Object arg2) {
        arg0.removeView(mLayoutList.get(arg1));
    }

    @Override
    public Object instantiateItem(ViewGroup arg0, int arg1) {
        MyLog.d(TAG, "instantiateItem " + arg1);
        arg0.addView(mLayoutList.get(arg1));
        return mLayoutList.get(arg1);
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == (arg1);
    }
}