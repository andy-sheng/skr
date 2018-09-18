package com.wali.live.watchsdk.channel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lan on 16/11/25.
 */
public class ChannelSdkActivity extends BaseSdkActivity implements IChannelView {
    private static final String EXTRA_CHANNEL_ID = "extra_channel_id";
    private static final String EXTRA_TITLE = "extra_title";

    protected BackTitleBar mBackTitleBar;

    protected SwipeRefreshLayout mRefreshLayout;
    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLayoutManager;
    protected ChannelRecyclerAdapter mRecyclerAdapter;

    private ChannelPresenter mPresenter;
    private long mChannelId = 0;
    private String mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channelsdk_layout);

        initData();
        initViews();
        initPresenters();
        getChannelFromServer();
    }

    private void initData() {
        Intent data = getIntent();
        if (data == null) {
            MyLog.w(TAG, "data is null");
            finish();
            return;
        }
        mChannelId = data.getLongExtra(EXTRA_CHANNEL_ID, 0);
        mTitle = data.getStringExtra(EXTRA_TITLE);
    }

    private void initViews() {
        mBackTitleBar = $(R.id.title_bar);
        if (TextUtils.isEmpty(mTitle)) {
            mBackTitleBar.setTitle(R.string.michannel_type_live);
        } else {
            mBackTitleBar.setTitle(mTitle);
        }
        mBackTitleBar.getBackBtn().setOnClickListener(
                new View.OnClickListener() {
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

        mRecyclerAdapter = new ChannelRecyclerAdapter(this);
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
    public void updateView(List<? extends BaseViewModel> models, long channelId) {
        mRecyclerAdapter.setData(models, mChannelId);
    }

    @Override
    public void onDataLoadFail() {
        updateView(new ArrayList<BaseViewModel>(), -1);
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

    public static void openActivity(@NonNull Activity activity, long channelId, String title) {
        Intent intent = new Intent(activity, ChannelSdkActivity.class);
        intent.putExtra(EXTRA_CHANNEL_ID, channelId);
        intent.putExtra(EXTRA_TITLE, title);
        activity.startActivity(intent);
    }
}
