package com.module.home.persenter;

import com.alibaba.android.arouter.launcher.ARouter;
import com.alibaba.fastjson.JSON;
import com.common.core.scheme.event.GrabInviteFromSchemeEvent;
import com.common.log.MyLog;
import com.common.mvp.RxLifeCyclePresenter;
import com.common.notification.event.FollowNotifyEvent;
import com.common.notification.event.GrabInviteNotifyEvent;
import com.common.rxretrofit.ApiManager;
import com.common.rxretrofit.ApiMethods;
import com.common.rxretrofit.ApiObserver;
import com.common.rxretrofit.ApiResult;
import com.component.busilib.constans.GrabRoomType;
import com.module.RouterConstants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import static com.common.rxretrofit.ApiManager.APPLICATION_JSOIN;

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
        // TODO: 2019/3/20   一场到底邀请 端内
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabInviteFromSchemeEvent event) {
        // TODO: 2019/3/20   一场到底邀请 口令
        if (event.ask == 1) {
            // 需要再次确认弹窗
        } else {
            // 不需要直接进
            ARouter.getInstance().build(RouterConstants.ACTIVITY_GRAB_ROOM)
                    .withInt("roomID", event.roomId)
                    .navigation();
        }
    }

}
