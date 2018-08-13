package com.wali.live.watchsdk.watch.presenter.watchgamepresenter;

import android.support.annotation.NonNull;

import com.base.log.MyLog;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.watchsdk.component.presenter.InputPresenter;
import com.wali.live.watchsdk.watch.view.watchgameview.GameNewLandscapeInputView;

/**
 * Created by zhujianning on 18-8-13.
 */

public class GameNewLandscapeInputViewPresenter extends InputPresenter<GameNewLandscapeInputView.IView>
        implements GameNewLandscapeInputView.IPresenter {
    private static final String TAG = "GameNewLandscapeInputViewPresenter";

    public GameNewLandscapeInputViewPresenter(@NonNull IEventController controller, @NonNull RoomBaseDataModel myRoomData, LiveRoomChatMsgManager liveRoomChatMsgManager) {
        super(controller, myRoomData, liveRoomChatMsgManager);
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        MyLog.d(TAG, "startPresenter");
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        MyLog.d(TAG, "stopPresenter");
    }

    @Override
    protected String getTAG() {
        return TAG;
    }

    @Override
    public boolean onEvent(int i, IParams iParams) {
        return false;
    }
}
