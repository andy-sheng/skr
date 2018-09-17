package com.wali.live.watchsdk.channel.view;

import android.content.Context;
import android.content.ContextWrapper;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.activity.RxActivity;
import com.base.log.MyLog;
import com.base.utils.network.NetworkUtils;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.MyUserInfoManager;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.adapter.ChannelRecyclerAdapter;
import com.wali.live.watchsdk.channel.holder.StayExposureHolder;
import com.wali.live.watchsdk.channel.list.model.ChannelShow;
import com.wali.live.watchsdk.channel.presenter.ChannelPresenter;
import com.wali.live.watchsdk.channel.presenter.IChannelPresenter;
import com.wali.live.watchsdk.channel.presenter.IChannelView;
import com.wali.live.watchsdk.channel.viewmodel.BaseViewModel;
import com.wali.live.watchsdk.eventbus.EventClass;
import com.wali.live.watchsdk.statistics.MilinkStatistics;
import com.wali.live.watchsdk.statistics.item.AliveStatisticItem;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * Created by vera on 2018/5/24.
 * 首页频道列表
 */

public class LiveChannelView extends BaseLiveChannelView {
    public LiveChannelView(Context context) {
        super(context);
    }

    public LiveChannelView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected IChannelPresenter initChannelPresenter() {
        return new ChannelPresenter(getActivity(), this);
    }

    @Override
    public void setChannelId(long channelId) {
        super.setChannelId(channelId);
        ((ChannelPresenter)mPresenter).setChannelId(mChannelId);
    }

    public long getChannelId() {
        return mChannelId;
    }
}
