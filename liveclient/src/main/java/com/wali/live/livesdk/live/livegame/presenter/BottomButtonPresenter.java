package com.wali.live.livesdk.live.livegame.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.base.log.MyLog;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.thornbirds.component.presenter.ComponentPresenter;
import com.wali.live.livesdk.live.livegame.view.LiveBottomButton;
import com.wali.live.livesdk.live.presenter.GameLivePresenter;

import static com.wali.live.component.BaseSdkController.MSG_ON_ACTIVITY_RESUMED;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_INPUT_VIEW;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_MESSAGE_PANEL;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_SETTING_PANEL;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_SHARE_PANEL;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 底部按钮表现, 游戏直播
 */
public class BottomButtonPresenter extends ComponentPresenter<LiveBottomButton.IView>
        implements LiveBottomButton.IPresenter {
    private static final String TAG = "BottomButtonPresenter";

    @Nullable
    private GameLivePresenter mGameLivePresenter;
    private RoomBaseDataModel mMyRoomData;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public BottomButtonPresenter(
            @NonNull IEventController controller,
            @NonNull RoomBaseDataModel myRoomData,
            @Nullable GameLivePresenter gameLivePresenter) {
        super(controller);
        mMyRoomData = myRoomData;
        mGameLivePresenter = gameLivePresenter;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_ON_ACTIVITY_RESUMED);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
    }

    @Override
    public void showInputView() {
        postEvent(MSG_SHOW_INPUT_VIEW);
    }

    @Override
    public void showSettingPanel() {
        postEvent(MSG_SHOW_SETTING_PANEL);
    }

    @Override
    public void muteAudio(boolean isMute) {
        if (mGameLivePresenter != null) {
            mGameLivePresenter.muteMic(isMute);
        }
    }

    @Override
    public void showShareView() {
        postEvent(MSG_SHOW_SHARE_PANEL);
    }

    @Override
    public boolean isEnableShare() {
        return mMyRoomData != null ? mMyRoomData.getEnableShare() : false;
    }

    @Override
    public void showMsgCtrlView() {
        postEvent(MSG_SHOW_MESSAGE_PANEL);
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
            case MSG_ON_ACTIVITY_RESUMED:
                if (mGameLivePresenter != null) {
                    mView.updateMuteBtn(mGameLivePresenter.isMuteMic());
                    return true;
                }
                break;
            default:
                break;
        }
        return false;
    }
}
