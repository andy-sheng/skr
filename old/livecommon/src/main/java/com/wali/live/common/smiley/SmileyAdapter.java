package com.wali.live.common.smiley;

import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.base.log.MyLog;

import java.util.ArrayList;
import java.util.List;

/**
 * @module smileypick
 * <p/>
 * Created by MK on 15/10/20.
 */
public class SmileyAdapter extends PagerAdapter {

    private List<SmileyPage> mViews = new ArrayList<SmileyPage>();

    private final List<SmileyItem> mSmileyItemCache = new ArrayList<>();

    @Override
    public int getCount() {
        return mViews.size();
    }

    public List<SmileyItem> getSmileyCaches() {
        return mSmileyItemCache;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    @Override
    public void destroyItem(ViewGroup container, int position,
                            Object object) {
        MyLog.v("SmileyAdapter:destroyItem position=" + position);
        if (position < mViews.size()) {
            SmileyPage sp = mViews.get(position);
            sp.removeItems(mSmileyItemCache);
            container.removeView(mViews.get(position));
        }

    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        MyLog.v("SmileyAdapter:instantiateItem position=" + position);
        SmileyPage si = mViews.get(position);
        if (!si.mIsInited) {
            si.load();
        }
        container.addView(si);
        return si;
    }

    public void addView(SmileyPage view) {
        mViews.add(view);
        notifyDataSetChanged();
    }

    public SmileyPage getPage(int index) {
        return mViews.get(index);
    }

    public void clearAllPages() {
        for (SmileyPage sp : mViews) {
            sp.removeAllViews();
        }
        mViews.clear();
        for (SmileyItem si : mSmileyItemCache) {
            si.reset();
        }
        mSmileyItemCache.clear();
    }

}
