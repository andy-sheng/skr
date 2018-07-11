package com.wali.live.livesdk.live.liveshow.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.livesdk.live.component.data.StreamerPresenter;
import com.wali.live.livesdk.live.liveshow.presenter.panel.LiveMagicPresenter;
import com.wali.live.livesdk.live.liveshow.presenter.panel.LivePlusPresenter;
import com.wali.live.livesdk.live.liveshow.view.panel.LiveMagicPanel;
import com.wali.live.livesdk.live.liveshow.view.panel.LivePlusPanel;
import com.wali.live.livesdk.live.liveshow.view.panel.LiveSettingPanel;
import com.wali.live.watchsdk.component.presenter.BaseContainerPresenter;
import com.wali.live.watchsdk.component.presenter.panel.MessagePresenter;
import com.wali.live.watchsdk.component.view.panel.MessagePanel;
import com.wali.live.watchsdk.watch.presenter.SnsShareHelper;

import java.lang.ref.WeakReference;

import static com.wali.live.component.BaseSdkController.MSG_HIDE_BOTTOM_PANEL;
import static com.wali.live.component.BaseSdkController.MSG_ON_BACK_PRESSED;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_MAGIC_PANEL;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_MESSAGE_PANEL;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_PLUS_PANEL;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_SETTING_PANEL;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_SHARE_PANEL;

/**
 * Created by yangli on 2017/3/13.
 *
 * @module 秀场直播底部面板表现
 */
public class PanelContainerPresenter extends BaseContainerPresenter<RelativeLayout> {
    private static final String TAG = "PanelContainerPresenter";

    private RoomBaseDataModel mMyRoomData;
    private StreamerPresenter mStreamerPresenter;

    private WeakReference<LiveSettingPanel> mSettingPanelRef;

    private WeakReference<LivePlusPanel> mPlusPanelRef;
    private WeakReference<LivePlusPresenter> mPlusPresenterRef;

    private WeakReference<LiveMagicPanel> mMagicPanelRef;
    private WeakReference<LiveMagicPresenter> mMagicPresenterRef;

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
            @NonNull StreamerPresenter streamerPresenter,
            @NonNull RoomBaseDataModel myRoomData) {
        super(controller);
        mStreamerPresenter = streamerPresenter;
        mMyRoomData = myRoomData;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_ON_BACK_PRESSED);
        registerAction(MSG_SHOW_SETTING_PANEL);
        registerAction(MSG_SHOW_PLUS_PANEL);
        registerAction(MSG_SHOW_MAGIC_PANEL);
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

    @Override
    public void destroy() {
        super.destroy();
        destroyRef(mPlusPresenterRef);
        destroyRef(mMagicPresenterRef);
    }

    private void showSettingPanel() {
        LiveSettingPanel panel = deRef(mSettingPanelRef);
        if (panel == null) {
            panel = new LiveSettingPanel(mView, mStreamerPresenter, mController);
            mSettingPanelRef = new WeakReference<>(panel);
        }
        showPanel(panel, true);
    }

    private void showPlusPanel() {
        LivePlusPanel panel = deRef(mPlusPanelRef);
        if (panel == null) {
            panel = new LivePlusPanel(mView);
            mPlusPanelRef = new WeakReference<>(panel);
            LivePlusPresenter presenter = deRef(mPlusPresenterRef);
            if (presenter == null) {
                presenter = new LivePlusPresenter(mController);
                if(mMyRoomData != null) {
                    presenter.setRoomInfo(mMyRoomData.getRoomId(), mMyRoomData.getUid());
                }
                mPlusPresenterRef = new WeakReference<>(presenter);
            }
            setupComponent(panel, presenter);
        }
        showPanel(panel, true);
    }

    private void showMagicPanel() {
        LiveMagicPanel panel = deRef(mMagicPanelRef);
        if (panel == null) {
            panel = new LiveMagicPanel(mView, mStreamerPresenter);
            mMagicPanelRef = new WeakReference<>(panel);
            LiveMagicPresenter presenter = deRef(mMagicPresenterRef);
            if (presenter == null) {
                presenter = new LiveMagicPresenter();
                mMagicPresenterRef = new WeakReference<>(presenter);
            }
            setupComponent(panel, presenter);
        }
        showPanel(panel, true);
    }

    private void showShareControlPanel() {
        //通知上层分享
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
            case MSG_SHOW_PLUS_PANEL:
                showPlusPanel();
                return true;
            case MSG_SHOW_MAGIC_PANEL:
                showMagicPanel();
                return true;
            case MSG_SHOW_SHARE_PANEL:
                showShareControlPanel();
                return true;
            case MSG_SHOW_MESSAGE_PANEL:
                showMessagePanel();
                return true;
            case MSG_HIDE_BOTTOM_PANEL:
            case MSG_ON_BACK_PRESSED:
                return hidePanel(true);
            default:
                break;
        }
        return false;
    }
}
