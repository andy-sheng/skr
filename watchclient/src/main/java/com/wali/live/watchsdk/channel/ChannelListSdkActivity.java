package com.wali.live.watchsdk.channel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.base.activity.BaseSdkActivity;
import com.wali.live.watchsdk.channel.list.model.ChannelShow;
import com.wali.live.watchsdk.channel.list.presenter.ChannelListPresenter;
import com.wali.live.watchsdk.channel.list.presenter.IChannelListView;

import java.util.List;

/**
 * Created by lan on 16/11/25.
 */
public class ChannelListSdkActivity extends BaseSdkActivity implements IChannelListView {

    ChannelListPresenter mChannelListPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public static void openActivity(@NonNull Activity activity) {
        Intent intent = new Intent(activity, ChannelListSdkActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public void listUpdateView(List<? extends ChannelShow> models) {

    }
}
