package com.wali.live.channel.presenter;

import com.wali.live.channel.viewmodel.BaseViewModel;

import java.util.List;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 频道View的接口，提供刷新，滚动，生命周期的接口
 */
public interface IChannelView {
    void updateView(List<? extends BaseViewModel> models);

    void finishRefresh();

    void doRefresh();
}
