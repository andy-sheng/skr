package com.wali.live.watchsdk.channel.adapter;


import android.support.v4.util.LongSparseArray;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;

import com.base.log.MyLog;
import com.mi.live.data.milink.command.MiLinkCommand;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.list.model.ChannelShow;
import com.wali.live.watchsdk.channel.view.LiveChannelView;
import com.wali.live.watchsdk.view.EmptyView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vera on 2018/5/24.
 */

public class ChannelTabPagerAdapter extends PagerAdapter {
    public final String TAG = getClass().getSimpleName();

    private List<? extends ChannelShow> mChannelList ;
    private LongSparseArray<View> mLayoutCacheMap;

    public ChannelTabPagerAdapter() {
        mLayoutCacheMap = new LongSparseArray();
    }

    public void setChannelList(List<? extends ChannelShow> list) {
        mChannelList = list;
        notifyDataSetChanged();
    }

    public ChannelShow getChannelShowByPosition(int position) {
        if (mChannelList != null && mChannelList.size() > position) {
            return mChannelList.get(position);
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (mChannelList != null && mChannelList.size() > position) {
            return mChannelList.get(position).getChannelName();
        } else {
            return "";
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        ChannelShow channelShow = mChannelList.get(position);
        MyLog.d(TAG, "UiType = " + channelShow.getUiType() + "\nchannelUrl = " + channelShow.getUrl());

        View channelView = null;

        // TODO 目前只有热点频道 之后加上关注和小视频等
        if (channelShow.getUiType() == ChannelShow.UI_TYPE_COMMAND) {
            if (channelShow.getUrl().equals(MiLinkCommand.COMMAND_HOT_CHANNEL_LIST)) {
                View cachedView = getViewFromMap(channelShow.getChannelId());
                if (cachedView != null && cachedView instanceof LiveChannelView) {
                    channelView = cachedView;
                } else {
                    channelView = new LiveChannelView(container.getContext());
                    ((LiveChannelView) channelView).setChannelShow(channelShow);
                }
                ((LiveChannelView) channelView).onStart();
            }
        }

        if (channelView == null) {
            channelView = new EmptyView(container.getContext());
            ((EmptyView)channelView).setEmptyTips(R.string.empty_tips);
            ((EmptyView)channelView).setEmptyDrawable(R.drawable.home_empty_icon);
        }

        container.addView(channelView);
        return channelView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object arg2) {
        if (position < mChannelList.size()) {
            MyLog.v(TAG, "destroyItem position=" + position + ",channelName=" + mChannelList.get(position).getChannelName());
        }
        View v = (View) arg2;
        int index = container.indexOfChild(v);
        int count = container.getChildCount();
        if (index >= 0 && index < count) {
            container.removeView(v);
            if (v instanceof LiveChannelView) {
                cacheViewToMap(((LiveChannelView) v).getChannelId(), v);
                ((LiveChannelView) v).onStop();
            }
        } else {
            MyLog.e(TAG, "destroyItem but index is error! index=" + index + ",count=" + count);
        }
    }

    public void destroyMap() {
        int size = mLayoutCacheMap.size();
        for (int i = 0; i < size; ++i) {
            View view = mLayoutCacheMap.get(mLayoutCacheMap.keyAt(i));
            if (view != null) {
                if (view instanceof LiveChannelView) {
                    ((LiveChannelView) view).onDestroy();
                }
            }
        }
    }

    private void cacheViewToMap(long channelId, View view) {
        if (mLayoutCacheMap != null) {
            mLayoutCacheMap.put(channelId, view);
        }
    }

    private View getViewFromMap(long channelId) {
        if (mLayoutCacheMap != null) {
            return mLayoutCacheMap.get(channelId);
        }
        return null;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return mChannelList == null ? 0 : mChannelList.size();
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == (arg1);
    }
}
