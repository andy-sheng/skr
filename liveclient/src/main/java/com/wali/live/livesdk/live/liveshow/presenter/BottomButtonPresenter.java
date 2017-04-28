package com.wali.live.livesdk.live.liveshow.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.livesdk.live.liveshow.LiveComponentController;
import com.wali.live.livesdk.live.liveshow.view.LiveBottomButton;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 底部按钮表现, 游戏直播
 */
public class BottomButtonPresenter extends
        ComponentPresenter<LiveBottomButton.IView> implements LiveBottomButton.IPresenter {
    private static final String TAG = "BottomButtonPresenter";

    private RoomBaseDataModel mMyRoomData;

    public BottomButtonPresenter(
            @NonNull IComponentController componentController,
            RoomBaseDataModel myRoomData) {
        super(componentController);
        registerAction(LiveComponentController.MSG_ON_ORIENT_PORTRAIT);
        registerAction(LiveComponentController.MSG_ON_ORIENT_LANDSCAPE);
        mMyRoomData = myRoomData;
    }

    @Override
    public void showPlusPanel() {
        mComponentController.onEvent(LiveComponentController.MSG_SHOW_PLUS_PANEL);
    }

    @Override
    public void showSettingPanel() {
        mComponentController.onEvent(LiveComponentController.MSG_SHOW_SETTING_PANEL);
    }

    @Override
    public void showMagicPanel() {
        mComponentController.onEvent(LiveComponentController.MSG_SHOW_MAGIC_PANEL);
    }

    @Override
    public void showShareView() {
        mComponentController.onEvent(LiveComponentController.MSG_SHOW_SHARE_PANEL);
    }

    @Nullable
    @Override
    protected IAction createAction() {
        return new Action();
    }

    public class Action implements IAction {
        @Override
        public boolean onAction(int source, @Nullable Params params) {
            if (mView == null) {
                MyLog.e(TAG, "onAction but mView is null, source=" + source);
                return false;
            }
            switch (source) {
                case LiveComponentController.MSG_ON_ORIENT_PORTRAIT:
                    mView.onOrientation(false);
                    return true;
                case LiveComponentController.MSG_ON_ORIENT_LANDSCAPE:
                    mView.onOrientation(true);
                    return true;
                default:
                    break;
            }
            return false;
        }
    }
}
