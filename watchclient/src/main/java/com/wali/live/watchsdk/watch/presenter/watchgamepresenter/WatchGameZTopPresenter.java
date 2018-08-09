package com.wali.live.watchsdk.watch.presenter.watchgamepresenter;

import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.component.WatchComponentController;
import com.wali.live.watchsdk.watch.view.watchgameview.WatchGameZTopView;

import static com.wali.live.component.BaseSdkController.MSG_FORCE_ROTATE_SCREEN;
import static com.wali.live.component.BaseSdkController.MSG_NEW_GAME_WATCH_EXIST_CLICK;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;

/**
 * Created by vera on 2018/8/8.
 */

public class WatchGameZTopPresenter extends ComponentPresenter<WatchGameZTopView.IView>
        implements WatchGameZTopView.IPresenter {
    private RoomBaseDataModel mMyRoomData;

    public WatchGameZTopPresenter(IEventController controller) {
        super(controller);
        if (controller != null && controller instanceof WatchComponentController) {
            mMyRoomData = ((WatchComponentController) mController).getRoomBaseDataModel();
        }
    }

    @Override
    protected String getTAG() {
        return null;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
    }

    @Override
    public boolean onEvent(int event, IParams iParams) {
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                // 接收到切换为竖屏通知
                mView.reOrient(false);
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                // 接收到切换为横屏通知
                mView.reOrient(true);
                return true;
        }
        return false;
    }

    @Override
    public void forceRotate() {
        postEvent(MSG_FORCE_ROTATE_SCREEN);
    }

    @Override
    public void exitRoom() {
        postEvent(MSG_NEW_GAME_WATCH_EXIST_CLICK);
    }

    @Override
    public void getAnchorInfo() {
        if (mMyRoomData == null) {
            return;
        }

        boolean isFollowed = mMyRoomData.isFocused() || mMyRoomData.getUid() == UserAccountManager.getInstance().getUuidAsLong();
        mView.updateAnchorInfo(mMyRoomData.getUid(), mMyRoomData.getAvatarTs(),
                 mMyRoomData.getNickName(), isFollowed);
    }

    @Override
    public void followAnchor() {

    }
}
