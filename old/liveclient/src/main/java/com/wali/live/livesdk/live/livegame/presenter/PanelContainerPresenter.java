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
import com.wali.live.livesdk.live.livegame.view.panel.GameSettingPanel;
import com.wali.live.watchsdk.component.presenter.BaseContainerPresenter;
import com.wali.live.watchsdk.component.presenter.panel.MessagePresenter;
import com.wali.live.watchsdk.component.view.panel.MessagePanel;
import com.wali.live.watchsdk.watch.presenter.SnsShareHelper;

import java.lang.ref.WeakReference;

import static com.wali.live.component.BaseSdkController.MSG_HIDE_BOTTOM_PANEL;
import static com.wali.live.component.BaseSdkController.MSG_ON_BACK_PRESSED;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_MESSAGE_PANEL;
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

    private WeakReference<GameSettingPanel> mSettingPanelRef;

    private WeakReference<MessagePanel> mMessagePanelRef;
    private WeakReference<MessagePresenter> mMessagePresenterRef;

    @Override
    protected final String getTAG() {
        return TAG;
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

    public PanelContainerPresenter(
            @NonNull IEventController controller,
            @Nullable LiveRoomChatMsgManager liveRoomChatMsgManager,
            @Nullable RoomBaseDataModel myRoomData) {
        super(controller);
        mLiveRoomChatMsgManager = liveRoomChatMsgManager;
        mMyRoomData = myRoomData;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_ON_BACK_PRESSED);
        registerAction(MSG_SHOW_SETTING_PANEL);
        registerAction(MSG_SHOW_SHARE_PANEL);
        registerAction(MSG_SHOW_MESSAGE_PANEL);
        registerAction(MSG_HIDE_BOTTOM_PANEL);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
        hidePanel(false);
    }

    private void showSettingPanel() {
        GameSettingPanel panel = deRef(mSettingPanelRef);
        if (panel == null) {
            panel = new GameSettingPanel(mView, mLiveRoomChatMsgManager);
            mSettingPanelRef = new WeakReference<>(panel);
        }
        showPanel(panel, true);
    }

    private void showShareControlPanel() {
        SnsShareHelper.getInstance().shareToSns(-1, mMyRoomData);
    }

    private void showMessagePanel() {
        MessagePanel panel = deRef(mMessagePanelRef);
        if (panel == null) {
            panel = new MessagePanel(mView);
            mMessagePanelRef = new WeakReference<>(panel);
            MessagePresenter presenter = deRef(mMessagePresenterRef);
            if (presenter == null) {
                presenter = new MessagePresenter(mController);
                mMessagePresenterRef = new WeakReference<>(presenter);
            }
            setupComponent(panel, presenter);
        }
        showPanel(panel, true);
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event);
            return false;
        }
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                onOrientation(false);
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                onOrientation(true);
                return true;
        }
        if (CommonUtils.isFastDoubleClick()) {
            MyLog.w(TAG, "onAction but isFastDoubleClick, event=" + event);
            return false;
        }
        switch (event) {
            case MSG_SHOW_SETTING_PANEL:
                showSettingPanel();
                return true;
            case MSG_SHOW_SHARE_PANEL:
                showShareControlPanel();
                return true;
            case MSG_SHOW_MESSAGE_PANEL:
                showMessagePanel();
                return true;
            case MSG_ON_BACK_PRESSED:
            case MSG_HIDE_BOTTOM_PANEL:
                return hidePanel(true);
            default:
                break;
        }
        return false;
    }

}
