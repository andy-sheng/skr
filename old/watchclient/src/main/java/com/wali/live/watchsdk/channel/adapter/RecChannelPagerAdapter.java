package com.wali.live.watchsdk.channel.adapter;

import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.mi.live.data.account.MyUserInfoManager;
import com.wali.live.watchsdk.channel.data.ChannelDataStore;
import com.wali.live.watchsdk.channel.view.RecLiveChannelView;

import static com.wali.live.watchsdk.channel.data.ChannelDataStore.GAME_WATCH_CHANNEL_TYPE_FOCUS;
import static com.wali.live.watchsdk.channel.data.ChannelDataStore.GAME_WATCH_CHANNEL_TYPE_RECOMMEND;

/**
 * Created by liuting on 18-9-12.
 * 游戏直播间更多直播Tab 人气推荐和我的关注两个tab
 */

public class RecChannelPagerAdapter extends PagerAdapter {
    private RecLiveChannelView mRecChannelView;
    private RecLiveChannelView mFocusChannelView;

    // param
    private long mViewerId;
    private long mAnchorId;
    private String mPackageName = "";
    private long mGameId;
    @ChannelDataStore.ReqFrom
    private int mReqFrom;

    public void setRequestParam(long anchorId, String packageName, long gameId, @ChannelDataStore.ReqFrom int reqFrom) {
        mViewerId = MyUserInfoManager.getInstance().getUuid();
        mAnchorId = anchorId;
        if (!TextUtils.isEmpty(packageName)) {
            mPackageName = packageName;
        }
        mGameId = gameId;
        mReqFrom = reqFrom;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (position == 0) {
            // 人气推荐
            if (mRecChannelView == null) {
                mRecChannelView = new RecLiveChannelView(container.getContext());
                mRecChannelView.setRequestParam(mViewerId, mAnchorId, mPackageName, mGameId, GAME_WATCH_CHANNEL_TYPE_RECOMMEND, mReqFrom);
                mRecChannelView.loadData();
            }
            container.addView(mRecChannelView);
            return mRecChannelView;
        } else {
            // 我的关注
            if (mFocusChannelView == null) {
                mFocusChannelView = new RecLiveChannelView(container.getContext());
                mFocusChannelView.setRequestParam(mViewerId, mAnchorId, mPackageName, mGameId, GAME_WATCH_CHANNEL_TYPE_FOCUS, mReqFrom);
                mFocusChannelView.loadData();
            }
            container.addView(mFocusChannelView);
            return mFocusChannelView;
        }
    }

    public void reloadData() {
        if (mRecChannelView != null) {
            mRecChannelView.setRequestParam(mViewerId, mAnchorId, mPackageName, mGameId, GAME_WATCH_CHANNEL_TYPE_RECOMMEND, mReqFrom);
            mRecChannelView.loadData();
        }
        if (mFocusChannelView != null) {
            mFocusChannelView.setRequestParam(mViewerId, mAnchorId, mPackageName, mGameId, GAME_WATCH_CHANNEL_TYPE_FOCUS, mReqFrom);
            mFocusChannelView.loadData();
        }
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object arg2) {
        super.destroyItem(container, position, arg2);
    }

    public void destroy() {
        if (mRecChannelView != null) {
            mRecChannelView.onDestroy();
        }
        if (mFocusChannelView != null) {
            mFocusChannelView.onDestroy();
        }
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == (arg1);
    }
}
