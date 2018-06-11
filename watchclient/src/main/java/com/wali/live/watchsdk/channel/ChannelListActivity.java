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
import com.wali.live.watchsdk.channel.list.model.ChannelShow;
import com.wali.live.watchsdk.channel.list.presenter.ChannelListPresenter;
import com.wali.live.watchsdk.channel.list.presenter.IChannelListView;
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
public class ChannelListActivity extends BaseSdkActivity implements IChannelListView {

    ChannelListPresenter mChannelListPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public static void openActivity(@NonNull Activity activity) {
        Intent intent = new Intent(activity, ChannelListActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public void listUpdateView(List<? extends ChannelShow> models) {

    }
}
