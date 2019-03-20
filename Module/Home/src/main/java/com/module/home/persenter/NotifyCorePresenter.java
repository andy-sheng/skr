package com.module.home.persenter;

import com.common.mvp.RxLifeCyclePresenter;
import com.common.notification.event.FollowNotifyEvent;
import com.common.notification.event.GrabInviteNotifyEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class NotifyCorePresenter extends RxLifeCyclePresenter {

    public NotifyCorePresenter() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(FollowNotifyEvent event) {
        // TODO: 2019/3/20   关注提醒

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabInviteNotifyEvent event) {
        // TODO: 2019/3/20   一场到底邀请
    }
}
