package com.wali.live.modulechannel.view;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.log.MyLog;
import com.common.utils.U;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.adapter.ChannelRecyclerAdapter;
import com.wali.live.modulechannel.adapter.holder.StayExposureHolder;
import com.wali.live.modulechannel.event.ChannelEvent;
import com.wali.live.modulechannel.model.viewmodel.ChannelViewModel;
import com.wali.live.modulechannel.presenter.IChannelPresenter;
import com.wali.live.modulechannel.presenter.IChannelView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liuting on 18-9-12.
 * 直播频道列表基类
 */

public abstract class BaseLiveChannelView extends RelativeLayout implements IChannelView, SwipeRefreshLayout.OnRefreshListener{
    protected final String TAG = getClass().getSimpleName();

    protected SwipeRefreshLayout mSwipeRefreshLayout;
    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;
    protected ChannelRecyclerAdapter mRecyclerAdapter;

    protected boolean isVisibleToUser = false;

    protected long mChannelId = -1;
    protected IChannelPresenter mPresenter;

    public BaseLiveChannelView(Context context) {
        super(context);
        init(context);
    }

    public BaseLiveChannelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }


    protected void init(Context context) {
        inflate(context, R.layout.channel_live_channel_layout, this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mPresenter = initChannelPresenter();
    }

    protected abstract IChannelPresenter initChannelPresenter();

    public void setChannelId(long channelId) {
        mChannelId = channelId;
    }

    public void loadData() {
        if (mPresenter != null) {
            mPresenter.start();
        }
    }

    /**
     * 用户手动下拉刷新
     */
    @Override
    public void onRefresh() {
        if (!U.getNetworkUtils().hasNetwork()) {
            U.getToastUtil().showShort(R.string.channel_network_disable);
            if (mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }

        loadData();
    }


    @Override
    public void updateView(List<ChannelViewModel> models, long channelId) {
        if (models != null) {
            if (mRecyclerAdapter == null) {
                mRecyclerAdapter = new ChannelRecyclerAdapter((Activity) getContext());
                mRecyclerView.setAdapter(mRecyclerAdapter);
            }
            mRecyclerAdapter.setData(models, mChannelId);
        }
    }

    @Override
    public void onDataLoadFail() {
       updateView(new ArrayList<ChannelViewModel>(), -1);
    }


    @Override
    public void finishRefresh() {
        if (mSwipeRefreshLayout != null) {
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void doRefresh() {
        loadData();
    }

    private long mResumeTime;
    private long mPauseTime;

    public void onResume() {
        mResumeTime = System.currentTimeMillis();
    }

    public void onPause() {
        mPauseTime = System.currentTimeMillis();
        long aliveTime = mPauseTime - mResumeTime;

        //Todo-暂时去了
//        if (aliveTime > 0 && mResumeTime > 0) {
//            MilinkStatistics.getInstance().statisticAlive(MyUserInfoManager.getInstance().getUuid(),
//                    aliveTime, mChannelId, AliveStatisticItem.ALIVE_BIZ_TYPE_CHANNEL);
//        }
        mResumeTime = 0;
        mPauseTime = 0;
    }


    public void onDestroy() {
        if (mPresenter != null) {
            mPresenter.stop();
        }
        onCurrentHoldersVisible(false);
    }

//    protected RxActivity getActivity() {
//        // Gross way of unwrapping the Activity so we can get the FragmentManager
//        Context context = getContext();
//        while (context instanceof ContextWrapper) {
//            if (context instanceof RxActivity) {
//                return (RxActivity)context;
//            }
//            context = ((ContextWrapper)context).getBaseContext();
//        }
//        throw new IllegalStateException("The LiveChannelView's Context is not an RxActivity.");
//    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ChannelEvent.LiveListActivityLiveCycle event) {
        if (event != null && isVisibleToUser) {
            if (event.liveEvent == ChannelEvent.LiveListActivityLiveCycle.Event.RESUME) {
                onResume();
                onCurrentHoldersVisible(true);
            } else {
                onPause();
                onCurrentHoldersVisible(false);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN, sticky = true)
    public void onSelectChannelEvent(ChannelEvent.SelectChannelEvent event) {
        MyLog.d(TAG, "onSelectChannelEvent channelId=" + event.channelId);
        if (event != null) {
            isVisibleToUser = (event.channelId == mChannelId);
            if (isVisibleToUser) {
                onResume();
            } else {
                onPause();
            }
            onCurrentHoldersVisible(isVisibleToUser);
        }
    }

    private void onCurrentHoldersVisible(boolean visible) {
        if (mLayoutManager != null && mRecyclerView != null) {
            int firstPosition = mLayoutManager.findFirstVisibleItemPosition();
            int lastPosition = mLayoutManager.findLastVisibleItemPosition();
            if (firstPosition == RecyclerView.NO_POSITION || lastPosition == RecyclerView.NO_POSITION ) {
                return;
            }
            for (int i = 0; i <= (lastPosition - firstPosition); i ++) {
                View view = mRecyclerView.getChildAt(i);
                if (view != null) {
                    RecyclerView.ViewHolder holder = mRecyclerView.getChildViewHolder(view);
                    if (holder != null && holder instanceof StayExposureHolder) {
                        if (visible) {
                            ((StayExposureHolder) holder).onHolderAttached();
                        } else {
                            ((StayExposureHolder) holder).onHolderDetached();
                        }

                    }
                }
            }
        }
    }


}
