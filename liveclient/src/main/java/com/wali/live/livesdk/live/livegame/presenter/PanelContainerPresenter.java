package com.wali.live.livesdk.live.livegame.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.livesdk.live.livegame.LiveComponentController;
import com.wali.live.livesdk.live.livegame.view.panel.GameSettingPanel;
import com.wali.live.watchsdk.component.presenter.BaseContainerPresenter;
import com.wali.live.watchsdk.watch.presenter.SnsShareHelper;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 游戏直播底部面板表现
 */
public class PanelContainerPresenter extends BaseContainerPresenter<RelativeLayout> {
    private static final String TAG = "PanelContainerPresenter";
    @Nullable
    protected RoomBaseDataModel mMyRoomData;
    @Nullable
    protected LiveRoomChatMsgManager mLiveRoomChatMsgManager;

    private BaseBottomPanel mSettingPanel;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public PanelContainerPresenter(
            @NonNull IComponentController componentController,
            @Nullable LiveRoomChatMsgManager liveRoomChatMsgManager,
            @Nullable RoomBaseDataModel myRoomData) {
        super(componentController);
        mLiveRoomChatMsgManager = liveRoomChatMsgManager;
        mMyRoomData = myRoomData;
        registerAction(LiveComponentController.MSG_ON_ORIENT_PORTRAIT);
        registerAction(LiveComponentController.MSG_ON_ORIENT_LANDSCAPE);
        registerAction(LiveComponentController.MSG_ON_BACK_PRESSED);
        registerAction(LiveComponentController.MSG_SHOW_SETTING_PANEL);
        registerAction(LiveComponentController.MSG_SHOW_SHARE_PANEL);
        registerAction(LiveComponentController.MSG_HIDE_BOTTOM_PANEL);
    }

    @Override
    public void setComponentView(@Nullable RelativeLayout relativeLayout) {
        super.setComponentView(relativeLayout);
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hidePanel(true);
            }
        });
    }

    private void showSettingPanel() {
        if (mSettingPanel == null) {
            mSettingPanel = new GameSettingPanel(mView, mLiveRoomChatMsgManager);
        }
        showPanel(mSettingPanel, true);
    }

    private void showShareControlPanel() {
        SnsShareHelper.getInstance().shareToSns(-1, mMyRoomData);
    }

    @Nullable
    @Override
    protected IAction createAction() {
        return new Action();
    }

    public class Action implements IAction {
        @Override
        public boolean onAction(int source, @Nullable Params params) {
            if (mView == null || CommonUtils.isFastDoubleClick()) {
                MyLog.e(TAG, "onAction but mView is null, source=" + source + " or CommonUtils.isFastDoubleClick() is true");
                return false;
            }
            switch (source) {
                case LiveComponentController.MSG_ON_ORIENT_PORTRAIT:
                    onOrientation(false);
                    return true;
                case LiveComponentController.MSG_ON_ORIENT_LANDSCAPE:
                    onOrientation(true);
                    return true;
                case LiveComponentController.MSG_SHOW_SETTING_PANEL:
                    showSettingPanel();
                    return true;
                case LiveComponentController.MSG_SHOW_SHARE_PANEL:
                    showShareControlPanel();
                    break;
                case LiveComponentController.MSG_ON_BACK_PRESSED:
                case LiveComponentController.MSG_HIDE_BOTTOM_PANEL:
                    return hidePanel(true);
                default:
                    break;
            }
            return false;
        }
    }
}
