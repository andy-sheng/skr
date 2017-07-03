package com.wali.live.watchsdk.channel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.base.activity.BaseSdkActivity;
import com.base.log.MyLog;
import com.base.view.BackTitleBar;
import com.mi.live.data.milink.event.MiLinkEvent;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.adapter.ChannelRecyclerAdapter;
import com.wali.live.watchsdk.channel.presenter.ChannelPresenter;
import com.wali.live.watchsdk.channel.presenter.IChannelPresenter;
import com.wali.live.watchsdk.channel.presenter.IChannelView;
import com.wali.live.watchsdk.channel.viewmodel.BaseViewModel;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by lan on 16/11/25.
 */
public class ChannelSdkActivity extends BaseSdkActivity implements IChannelView {
    protected BackTitleBar mBackTitleBar;
    protected SwipeRefreshLayout mRefreshLayout;
    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;
    protected ChannelRecyclerAdapter mRecyclerAdapter;

    protected IChannelPresenter mPresenter;
    protected long mChannelId = 111001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channelsdk_layout);

        initViews();
        initPresenters();
        getChannelFromServer();
    }

    private void initViews() {
        mBackTitleBar = $(R.id.title_bar);
        mBackTitleBar.setTitle(R.string.channel_live);
        mBackTitleBar.getBackBtn().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mRefreshLayout = $(R.id.swipe_refresh_layout);
        mRecyclerView = $(R.id.recycler_view);

        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doRefresh();
            }
        });
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerAdapter = new ChannelRecyclerAdapter(this, mChannelId);
        mRecyclerView.setAdapter(mRecyclerAdapter);
    }

    private void initPresenters() {
        mPresenter = new ChannelPresenter(this, this);
        mPresenter.setChannelId(mChannelId);
    }

    private void getChannelFromServer() {
        mPresenter.start();
    }

    @Override
    public void updateView(List<? extends BaseViewModel> models) {
        mRecyclerAdapter.setData(models);
    }

    @Override
    public void finishRefresh() {
        mRefreshLayout.setRefreshing(false);
    }

    @Override
    public void doRefresh() {
        getChannelFromServer();
        mRefreshLayout.setRefreshing(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.stop();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MiLinkEvent.StatusConnected event) {
        MyLog.d(TAG, "milink is connected");
        if (event != null) {
            getChannelFromServer();
        }
    }

    public static void openActivity(@NonNull Activity activity) {
        Intent intent = new Intent(activity, ChannelSdkActivity.class);
        activity.startActivity(intent);
    }
}
