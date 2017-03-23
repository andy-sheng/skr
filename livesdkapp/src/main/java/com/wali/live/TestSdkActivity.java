package com.wali.live;

import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.WindowManager;

import com.base.activity.BaseSdkActivity;
import com.base.log.MyLog;
import com.mi.liveassistant.R;
import com.wali.live.channel.presenter.ChannelPresenter;
import com.wali.live.channel.presenter.IChannelView;
import com.wali.live.channel.viewmodel.BaseViewModel;
import com.wali.live.channel.viewmodel.ChannelLiveViewModel;
import com.wali.live.watchsdk.watch.WatchSdkActivity;
import com.wali.live.watchsdk.watch.model.RoomInfo;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by lan on 16/11/25.
 */
public class TestSdkActivity extends BaseSdkActivity implements IChannelView {
    private ChannelPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (isMIUIV6()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        } else {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.jumpsdk_layout);
        initData();
    }

    private void initData() {
        mPresenter = new ChannelPresenter(this, this);
        mPresenter.setChannelId(201);
        mPresenter.start();
    }

    @Override
    public void updateView(List<? extends BaseViewModel> models) {
        // 随便打开一个任意直播
        List<ChannelLiveViewModel.LiveItem> re = new ArrayList<>();
        for (BaseViewModel vm : models) {
            if (vm instanceof ChannelLiveViewModel) {
                ChannelLiveViewModel cvm = (ChannelLiveViewModel) vm;
                for (ChannelLiveViewModel.BaseItem baseItem : cvm.getItemDatas()) {
                    if (baseItem instanceof ChannelLiveViewModel.LiveItem) {
                        re.add((ChannelLiveViewModel.LiveItem) baseItem);
                    }
                }
            }
        }

        for (ChannelLiveViewModel.LiveItem item : re) {
            if (!TextUtils.isEmpty(item.getSchemeUri())) {
                Uri uri = Uri.parse(item.getSchemeUri());

                long playerId = Long.parseLong(uri.getQueryParameter("playerid"));
                String liveId = uri.getQueryParameter("liveid");
                String videoUrl = uri.getQueryParameter("videourl");

                RoomInfo roomInfo = RoomInfo.Builder.newInstance(playerId, liveId, videoUrl)
                        .setAvatar(item.getUser().getAvatar())
                        .setCoverUrl(item.getImageUrl())
                        .setLiveType(6)
                        .build();
                MyLog.d(TAG, "TestSdkActivity WatchSdkActivity.openActivity");
                WatchSdkActivity.openActivity(this, roomInfo);
                finish();
                break;
            }
        }
    }

    @Override
    public void finishRefresh() {
    }

    @Override
    public void doRefresh() {
    }
}
