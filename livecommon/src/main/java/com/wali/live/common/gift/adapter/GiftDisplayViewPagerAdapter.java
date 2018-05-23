package com.wali.live.common.gift.adapter;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.base.log.MyLog;
import com.wali.live.common.gift.presenter.GiftMallPresenter;
import com.wali.live.common.gift.view.GiftDisplayView;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chengsimin on 16/7/8.
 */
public class GiftDisplayViewPagerAdapter extends PagerAdapter {
    private static final String TAG = "GiftDisplayViewPagerAdapter";

    private List<View> mCacheList = new ArrayList<>();

    List<List<GiftMallPresenter.GiftWithCard>> dataSourceList = new ArrayList<>(); // 数据源

    private SoftReference<Activity> mActRef;

    public GiftDisplayViewPagerAdapter(Activity activity, GiftDisplayRecycleViewAdapter.GiftItemListener giftItemListener) {
        mActRef = new SoftReference<>(activity);
        mGiftItemListener = giftItemListener;
    }

    @Override
    public int getCount() {
        return dataSourceList == null ? 0 : dataSourceList.size();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object arg2) {
        View v = (View) arg2;
        container.removeView(v);
//        mCacheList.add(v);
    }

    private GiftDisplayRecycleViewAdapter.GiftItemListener mGiftItemListener;

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int size = dataSourceList.size();
        List<GiftMallPresenter.GiftWithCard> data = dataSourceList.get(position % size);
        MyLog.d(TAG, "position:" + position + ",data:" + data.size());
        GiftDisplayView giftDisplayView = (GiftDisplayView) mCacheList.get(position);
        giftDisplayView.setDataSource(data);
        giftDisplayView.setTag(position);
        container.addView(giftDisplayView);
        return giftDisplayView;
    }


    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == (arg1);
    }

    public void setDataSource(List<List<GiftMallPresenter.GiftWithCard>> dataSource) {
        dataSourceList.clear();
        dataSourceList.addAll(dataSource);
        while (mCacheList.size() < dataSourceList.size()) {
            mCacheList.add(new GiftDisplayView(mActRef.get(), mGiftItemListener));
        }
        notifyDataSetChanged();
    }

    public List<List<GiftMallPresenter.GiftWithCard>> getDataSource() {
        return dataSourceList;
    }

    public List<View> getCacheListView() {
        return mCacheList;
    }


    public void setSelectedGiftInfo(View selectedView, GiftMallPresenter.GiftWithCard info, int position, int currentViewPagerPosition) {
        MyLog.d(TAG, "setSelectedGiftInfo+mGiftDisplayView" + currentViewPagerPosition);
        ((GiftDisplayView) mCacheList.get(currentViewPagerPosition)).setSelectedGiftInfo(selectedView, info, position);

        clearOtherPagerGiftInfoTipsStatus(currentViewPagerPosition);
    }

    private void clearOtherPagerGiftInfoTipsStatus(int currentViewPagerPosition) {
        for (int i = 0; i < mCacheList.size(); i++) {
            if (i != currentViewPagerPosition) {
                ((GiftDisplayView) mCacheList.get(i)).clearGiftMallItemTips();
            }
        }
    }
}
