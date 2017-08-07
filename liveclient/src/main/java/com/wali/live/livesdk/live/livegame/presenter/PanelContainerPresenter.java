package com.wali.live.livesdk.live.livegame.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.livesdk.live.livegame.LiveComponentController;
import com.wali.live.livesdk.live.livegame.view.panel.GameSettingPanel;
import com.wali.live.watchsdk.component.presenter.BaseContainerPresenter;
import com.wali.live.watchsdk.watch.presenter.SnsShareHelper;

import static com.wali.live.component.BaseSdkController.MSG_HIDE_BOTTOM_PANEL;
import static com.wali.live.component.BaseSdkController.MSG_ON_BACK_PRESSED;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_SETTING_PANEL;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_SHARE_PANEL;

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
            @NonNull IEventController controller,
            @Nullable LiveRoomChatMsgManager liveRoomChatMsgManager,
            @Nullable RoomBaseDataModel myRoomData) {
        super(controller);
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
    public void setView(@Nullable RelativeLayout relativeLayout) {
        super.setView(relativeLayout);
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

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null || CommonUtils.isFastDoubleClick()) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event + " or CommonUtils.isFastDoubleClick() is true");
            return false;
        }
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                onOrientation(false);
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                onOrientation(true);
                return true;
            case MSG_SHOW_SETTING_PANEL:
                showSettingPanel();
                return true;
            case MSG_SHOW_SHARE_PANEL:
                showShareControlPanel();
                break;
            case MSG_ON_BACK_PRESSED:
            case MSG_HIDE_BOTTOM_PANEL:
                return hidePanel(true);
            default:
                break;
        }
        return false;
    }

}
