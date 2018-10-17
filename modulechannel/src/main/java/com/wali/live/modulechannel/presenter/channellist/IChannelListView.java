package com.wali.live.modulechannel.presenter.channellist;

import com.wali.live.modulechannel.model.channellist.ChannelShowModel;

import java.util.List;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 频道View的接口，提供刷新，滚动，生命周期的接口
 */
public interface IChannelListView {
    void listUpdateView(List<? extends ChannelShowModel> models);
}
