package com.wali.live.watchsdk.vip.presenter;

import com.base.log.MyLog;
import com.base.presenter.RxLifeCyclePresenter;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.watchsdk.eventbus.EventClass;
import com.wali.live.watchsdk.vip.contact.SuperLevelUserEnterAnimControlContact;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by zhujianning on 18-6-29.
 */

public class SuperLevelUserEnterAnimControlPresenter extends RxLifeCyclePresenter {
    private static final String TAG = "SuperLevelUserEnterAnimControlPresenter";

    private SuperLevelUserEnterAnimControlContact.IView mIView;

    private RoomBaseDataModel mRoomData;

    public SuperLevelUserEnterAnimControlPresenter(SuperLevelUserEnterAnimControlContact.IView iView) {
        this.mIView = iView;
    }

    public void setRoomData(RoomBaseDataModel roomData) {
        if(roomData == null) {
            MyLog.w(TAG, "roomData is null");
            return;
        }

        mRoomData = roomData;
        if(mIView != null) {
            mIView.setAnchorId(mRoomData.getUid());
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        if(mIView != null) {
            mIView.destory();
        }
    }

    @Override
    public void start() {
        super.start();
        EventBus.getDefault().register(this);
    }

    public void reset() {

    }

    @Subscribe(threadMode = ThreadMode.POSTING)// 非UI线程的一条特定线程上
    public void onEvent(EventClass.HighLevelUserActionEvent event) {
        if (event != null
                && event.enterLiveBarrage != null
                && mIView != null) {
            mIView.putBarrage(event.enterLiveBarrage);
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
        mIView.putBarrage(event.barrageMsg);
    }

    //TODO---外面做了控制就不要了
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
