package com.wali.live.watchsdk.personalcenter.level.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.base.utils.CommonUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.personalcenter.level.LevelActivity;
import com.wali.live.watchsdk.personalcenter.level.view.LevelPage;
import com.wali.live.watchsdk.personalcenter.level.view.VipLevelPage;

/**
 * Created by zhujianning on 18-6-22.
 */

public class LevelViewPagerAdapter extends PagerAdapter {

    private LayoutInflater mLayoutInflater;
    private Context mContext;

    public LevelViewPagerAdapter(@NonNull Context context) {
        mLayoutInflater = LayoutInflater.from(context);
        mContext = context;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        switch (position) {
            case LevelActivity.PAGE_INDEX_LEVEL:
                LevelPage levelPage = new LevelPage(mContext);
                setLayoutParams(levelPage);
                container.addView(levelPage);
                return getKey(levelPage);
            case LevelActivity.PAGE_INDEX_VIP:
                VipLevelPage vipLevelPage = new VipLevelPage(mContext);
                setLayoutParams(vipLevelPage);
                container.addView(vipLevelPage);
                return getKey(vipLevelPage);
        }
        return super.instantiateItem(container, position);
    }

    private void setLayoutParams(ViewGroup viewGroup) {
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        viewGroup.setLayoutParams(lp);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(getView(object));
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case LevelActivity.PAGE_INDEX_LEVEL:
                return CommonUtils.getString(R.string.level);
            case LevelActivity.PAGE_INDEX_VIP:
                return CommonUtils.getString(R.string.my_vip);
            default:
                return "";
        }
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return getKey(view) == object;
    }

    private Object getKey(View view) {
        return view;
    }

    private View getView(Object key) {
        return (View) key;
    }
}
