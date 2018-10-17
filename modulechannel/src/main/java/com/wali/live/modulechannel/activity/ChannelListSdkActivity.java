package com.wali.live.modulechannel.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.common.base.BaseActivity;
import com.wali.live.modulechannel.R;
import com.wali.live.modulechannel.model.channellist.ChannelShowModel;
import com.wali.live.modulechannel.model.viewmodel.BaseViewModel;
import com.wali.live.modulechannel.presenter.ChannelPresenter;
import com.wali.live.modulechannel.presenter.IChannelView;
import com.wali.live.modulechannel.presenter.channellist.ChannelListPresenter;
import com.wali.live.modulechannel.presenter.channellist.IChannelListView;

import java.util.List;

/**
 * Created by zhujianning on 18-10-17.
 */

@Route(path = "/channel/ChannelListSdkActivity")
public class ChannelListSdkActivity extends BaseActivity implements IChannelListView {

    private ChannelListPresenter mChannelListPresenter;

    private ChannelPresenter mChannelPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.channel_main_layout);
        loadData();
    }

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return 0;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
    }

    private void loadData() {
//        if (mChannelListPresenter == null) {
//            mChannelListPresenter = new ChannelListPresenter(this);
//            mChannelListPresenter.setFcId(0);
//        }
//        mChannelListPresenter.start();

        if(mChannelPresenter == null) {
            mChannelPresenter = new ChannelPresenter(new IChannelView() {
                @Override
                public void updateView(List<? extends BaseViewModel> models) {

                }

                @Override
                public void finishRefresh() {

                }

                @Override
                public void doRefresh() {

                }
            });
        }

        mChannelPresenter.start();

    }

    @Override
    public void listUpdateView(List<? extends ChannelShowModel> models) {

    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
