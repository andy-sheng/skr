package com.wali.live.livesdk.live.view.container;

import android.support.annotation.NonNull;
import android.widget.RelativeLayout;

import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.livesdk.live.view.panel.BaseSettingPanel;
import com.wali.live.livesdk.live.view.panel.GameSettingPanel;

/**
 * Created by yangli on 17-2-14.
 *
 * @module 底部面板
 */
public class LivePanelContainer extends BasePanelContainer<RelativeLayout> {

    private LiveRoomChatMsgManager mLiveRoomChatMsgManager;

    private BaseSettingPanel mSettingPanel;

    public LivePanelContainer(@NonNull RelativeLayout panelContainer,
                              @NonNull LiveRoomChatMsgManager liveRoomChatMsgManager) {
        super(panelContainer);
        mLiveRoomChatMsgManager = liveRoomChatMsgManager;
    }

    public void showSettingPanel() {
        if (mSettingPanel == null) {
            mSettingPanel = new GameSettingPanel(mPanelContainer, mLiveRoomChatMsgManager);
        }
        showPanel(mSettingPanel, true);
    }

}
