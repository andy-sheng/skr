package com.wali.live.modulechannel.view;

import android.content.Context;
import android.util.AttributeSet;

import com.wali.live.modulechannel.presenter.ChannelPresenter;
import com.wali.live.modulechannel.presenter.IChannelPresenter;

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
        return new ChannelPresenter(this);
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
