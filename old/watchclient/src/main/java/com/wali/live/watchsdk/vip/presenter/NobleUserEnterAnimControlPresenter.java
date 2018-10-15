package com.wali.live.watchsdk.vip.presenter;

import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.watchsdk.eventbus.EventClass;
import com.wali.live.watchsdk.vip.contact.NobleUserEnterAnimControlContact;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by zhujianning on 18-6-30.
 */

public class NobleUserEnterAnimControlPresenter extends RxLifeCyclePresenter implements NobleUserEnterAnimControlContact.IPresenter {
    private static final String TAG = "NobleUserEnterAnimControlPresenter";

    private NobleUserEnterAnimControlContact.IView mIview;
    private RoomBaseDataModel mRoomData;

    public NobleUserEnterAnimControlPresenter(NobleUserEnterAnimControlContact.IView iView) {
        this.mIview = iView;
        EventBus.getDefault().register(this);
    }

    @Override
    public void destroy() {
        super.destroy();
        EventBus.getDefault().unregister(this);
        mIview.destory();
    }

    public void setRoomData(RoomBaseDataModel roomData) {
        if(roomData == null) {
            MyLog.w(TAG, "roomData is null");
            return;
        }

        mRoomData = roomData;
        if(mIview != null) {
            mIview.setAnchorId(mRoomData.getUid());
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)// 非UI线程的一条特定线程上
    public void onEvent(EventClass.HighLevelUserActionEvent event) {
        if (event != null && event.enterLiveBarrage != null) {
            mIview.putBarrage(event.enterLiveBarrage);
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(EventClass.AddBarrageEvent event) {
        if (event == null || event.barrageMsg == null) {
            return;
        }
        if (mRoomData != null
                && event.barrageMsg.getAnchorId() != mRoomData.getUid()) {
            return;
        }
        mIview.putBarrage(event.barrageMsg);
    }

    //TODO-外面清了先去了把
//    @Subscribe(threadMode = ThreadMode.POSTING)
//    public void onEvent(EventClass.WatchViewsHideEvent event) {
//        if (event == null) {
//            return;
//        }
//        // 如果观众左滑，所有view显示，清理一下堆积的队列
//        if (!event.hideViews && mFlyBarrageControl != null) {
//            mFlyBarrageControl.reset();
//        }
//    }
}
