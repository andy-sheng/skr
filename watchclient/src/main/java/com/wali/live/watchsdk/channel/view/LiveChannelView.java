package com.wali.live.watchsdk.channel.view;

import android.content.Context;
import android.content.ContextWrapper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.base.activity.RxActivity;
import com.base.log.MyLog;
import com.base.utils.network.NetworkUtils;
import com.base.utils.toast.ToastUtils;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.adapter.ChannelRecyclerAdapter;
import com.wali.live.watchsdk.channel.list.model.ChannelShow;
import com.wali.live.watchsdk.channel.presenter.ChannelPresenter;
import com.wali.live.watchsdk.channel.presenter.IChannelView;
import com.wali.live.watchsdk.channel.viewmodel.BaseViewModel;

import java.util.List;

/**
 * Created by vera on 2018/5/24.
 */

public class LiveChannelView extends RelativeLayout implements IChannelView, SwipeRefreshLayout.OnRefreshListener {
    private final String TAG = getClass().getSimpleName();

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;

    private LinearLayoutManager mLayoutManager;
    protected ChannelRecyclerAdapter mRecyclerAdapter;

    private ChannelPresenter mChannelPresenter;
    private ChannelShow mChannelShow;


    public LiveChannelView(Context context) {
        super(context);
        init(context);
    }

    public LiveChannelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.live_channel_layout, this);

        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLayoutManager = new LinearLayoutManager(context);
        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    public void setChannelId(long channelId) {
        mChannelShow = new ChannelShow();
        mChannelShow.setChannelId(channelId);
        loadData();
    }

    public void setChannelShow(ChannelShow channelShow) {
        mChannelShow = channelShow;
        loadData();
    }

    private void loadData() {
        MyLog.d(TAG, " start loadData");
        if (mChannelPresenter == null) {
            mChannelPresenter = new ChannelPresenter(getActivity(), this);
            mChannelPresenter.setChannelId(mChannelShow.getChannelId());
        }
        mChannelPresenter.start();
    }


    /**
     * 用户手动下拉刷新
     */
    @Override
    public void onRefresh() {
        if (!NetworkUtils.hasNetwork(getContext())) {
            ToastUtils.showToast(R.string.network_disable);
            if (mSwipeRefreshLayout.isRefreshing()) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }

        loadData();
    }


    public long getChannelId() {
        return mChannelShow == null ? 0 : mChannelShow.getChannelId();
    }

    @Override
    public void updateView(List<? extends BaseViewModel> models) {
        if (models != null) {
            if (mRecyclerAdapter == null) {
                mRecyclerAdapter = new ChannelRecyclerAdapter(getActivity(), mChannelShow.getChannelId());
                mRecyclerView.setAdapter(mRecyclerAdapter);
            }
            mRecyclerAdapter.setData(models);
        }
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



    public void onStart() {

    }


    public void onResume() {

    }


    public void onPause() {

    }


    public void onStop() {

    }


    public void onDestroy() {

    }

    private RxActivity getActivity() {
        // Gross way of unwrapping the Activity so we can get the FragmentManager
        Context context = getContext();
        while (context instanceof ContextWrapper) {
            if (context instanceof RxActivity) {
                return (RxActivity)context;
            }
            context = ((ContextWrapper)context).getBaseContext();
        }
        throw new IllegalStateException("The LiveChannelView's Context is not an RxActivity.");
    }
}
