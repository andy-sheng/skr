package com.base.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.base.log.MyLog;

import org.greenrobot.eventbus.EventBus;

/**
 * Created by lan on 15-11-4.
 */
public abstract class BaseEventBusFragment extends MyRxFragment implements FragmentListener {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        MyLog.d(TAG, "onCreate : register eventbus");
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    public void onDestroy() {
        MyLog.d(TAG, "onDestroy : unregister eventbus");
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
