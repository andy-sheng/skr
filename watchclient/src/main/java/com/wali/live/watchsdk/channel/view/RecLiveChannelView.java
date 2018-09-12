package com.wali.live.watchsdk.channel.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.wali.live.watchsdk.channel.presenter.IChannelPresenter;
import com.wali.live.watchsdk.channel.presenter.RecChannelPresenter;
import com.wali.live.watchsdk.channel.viewmodel.BaseViewModel;

import java.util.List;

/**
 * Created by liuting on 18-9-12.
 */

public class RecLiveChannelView extends BaseLiveChannelView {

    public RecLiveChannelView(Context context) {
        super(context);
    }

    public RecLiveChannelView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected IChannelPresenter initChannelPresenter() {
        return new RecChannelPresenter(getActivity(), this);
    }

    @Override
    public void updateView(List<? extends BaseViewModel> models, long channelId) {
        setChannelId(channelId); // 首页频道的channelId 是外部set进来的　这里的频道Id是拉取的数据返回
        super.updateView(models, channelId);
    }

    public void setRequestParam(long viewerId, long anchorId, String packageName, long gameId, int recType, int reqFrom) {
        ((RecChannelPresenter)mPresenter).setRequestParam(viewerId, anchorId, packageName, gameId, recType, reqFrom);
    }
}
