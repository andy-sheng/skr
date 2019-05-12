package com.module.playways.room.gift.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.common.log.MyLog;
import com.module.playways.room.gift.model.BaseGift;
import com.module.playways.room.gift.view.GiftOnePageView;
import com.module.playways.room.gift.view.GiftDisplayView;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GiftViewPagerAdapter extends PagerAdapter {
    public final static String TAG = "GiftViewPagerAdapter";
    HashMap<Integer, GiftOnePageView> mGiftOnePageViewHashMap = new HashMap<>();
    HashMap<Integer, List<BaseGift>> mGiftDataHashMap = new HashMap<>();
    Context mContext;
    GiftDisplayView.IGiftOpListener mIGiftOpListener;

    public GiftViewPagerAdapter(Context context, GiftDisplayView.IGiftOpListener iGiftOpListener) {
        mContext = context;
        mIGiftOpListener = iGiftOpListener;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        MyLog.d(TAG, "destroyItem" + " container=" + container + " position=" + position + " object=" + object);
        container.removeView((View) object);
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        MyLog.d(TAG, "instantiateItem" + " container=" + container + " position=" + position);
        View view = mGiftOnePageViewHashMap.get(position);
        if (container.indexOfChild(view) == -1) {
            container.addView(view);
        }
        return view;
    }

    public void setData(HashMap<Integer, List<BaseGift>> giftDataHashMap) {
        if (giftDataHashMap == null) {
            return;
        }

        mGiftDataHashMap = giftDataHashMap;
        Iterator<Map.Entry<Integer, List<BaseGift>>> entries = giftDataHashMap.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<Integer, List<BaseGift>> entry = entries.next();
            GiftOnePageView giftOnePageView = mGiftOnePageViewHashMap.get(entry.getKey());
            if (giftOnePageView == null) {
                giftOnePageView = new GiftOnePageView(mContext);
                giftOnePageView.setIGiftOpListener(mIGiftOpListener);
                mGiftOnePageViewHashMap.put(entry.getKey(), giftOnePageView);
            }

            giftOnePageView.setData(entry.getValue());
        }

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mGiftDataHashMap == null ? 0 : mGiftDataHashMap.size();
    }

    public void destroy() {
        Iterator<Map.Entry<Integer, GiftOnePageView>> integerGiftOnePageViewIterator = mGiftOnePageViewHashMap.entrySet().iterator();
        while (integerGiftOnePageViewIterator.hasNext()) {
            integerGiftOnePageViewIterator.next().getValue().destroy();
        }
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (object);
    }
}
