package com.wali.live.watchsdk.channel.sublist.presenter;

import com.wali.live.watchsdk.channel.viewmodel.BaseViewModel;

import java.util.List;

/**
 * Created by lan on 16/6/28.
 *
 * @module 频道
 * @description 二级页面View的接口，提供刷新的接口
 */
public interface ISubChannelView {
    void updateView(List<? extends BaseViewModel> models);

    void finishRefresh();
}
