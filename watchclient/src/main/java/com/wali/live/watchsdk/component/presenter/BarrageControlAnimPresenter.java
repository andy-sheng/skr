package com.wali.live.watchsdk.component.presenter;

import android.support.annotation.NonNull;

import com.base.log.MyLog;
import com.mi.live.data.push.model.BarrageMsg;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.component.view.BarrageControlAnimView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.wali.live.component.BaseSdkController.MSG_BARRAGE_VIP_ENTER;

/**
 * Created by zyh on 2018/01/02.
 *
 * @module 弹幕区域入场动画等的view的presenter
 */
public class BarrageControlAnimPresenter extends ComponentPresenter<BarrageControlAnimView.IView>
        implements BarrageControlAnimView.IPresenter {
    private static final String TAG = "BarrageControlAnimPresenter" + "_BarrageAnimView";
    private RoomBaseDataModel mMyRoomData;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public BarrageControlAnimPresenter(@NonNull IEventController controller,
                                       @NonNull RoomBaseDataModel roomBaseDataModel) {
        super(controller);
        mMyRoomData = roomBaseDataModel;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        EventBus.getDefault().register(this);
        registerAction(MSG_BARRAGE_VIP_ENTER);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        EventBus.getDefault().unregister(this);
        unregisterAllAction();
        mView.destroy();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(AnimMsgEvent event) {
        if (event.model != null && event.model.getAnchorId() == mMyRoomData.getUid()) {
            mView.setData(event.model);
        }
    }

    public static class AnimMsgEvent {
        public BarrageMsg model;

        public AnimMsgEvent(BarrageMsg model) {
            this.model = model;
        }
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            case MSG_BARRAGE_VIP_ENTER:
                mView.setJoinEnable((boolean) params.getItem(0));
                break;
            default:
                break;
        }
        return false;
    }
}
