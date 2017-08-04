package com.wali.live.livesdk.live.liveshow.presenter;

import android.support.annotation.NonNull;

import com.base.log.MyLog;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.livesdk.live.liveshow.view.LiveBottomButton;

import static com.wali.live.componentwrapper.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_SHOW_MAGIC_PANEL;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_SHOW_PLUS_PANEL;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_SHOW_SETTING_PANEL;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_SHOW_SHARE_PANEL;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 底部按钮表现, 游戏直播
 */
public class BottomButtonPresenter extends ComponentPresenter<LiveBottomButton.IView>
        implements LiveBottomButton.IPresenter {
    private static final String TAG = "BottomButtonPresenter";

    private RoomBaseDataModel mMyRoomData;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public BottomButtonPresenter(
            @NonNull IEventController controller,
            @NonNull RoomBaseDataModel myRoomData) {
        super(controller);
        mMyRoomData = myRoomData;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
    }

    @Override
    public void showPlusPanel() {
        postEvent(MSG_SHOW_PLUS_PANEL);
    }

    @Override
    public void showSettingPanel() {
        postEvent(MSG_SHOW_SETTING_PANEL);
    }

    @Override
    public void showMagicPanel() {
        postEvent(MSG_SHOW_MAGIC_PANEL);
    }

    @Override
    public void showShareView() {
        postEvent(MSG_SHOW_SHARE_PANEL);
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                mView.onOrientation(false);
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                mView.onOrientation(true);
                return true;
            default:
                break;
        }
        return false;
    }
}
