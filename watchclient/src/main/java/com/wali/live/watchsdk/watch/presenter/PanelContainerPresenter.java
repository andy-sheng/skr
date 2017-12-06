package com.wali.live.watchsdk.watch.presenter;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.activity.BaseActivity;
import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IEventController;
import com.thornbirds.component.IParams;
import com.wali.live.watchsdk.component.presenter.BaseContainerPresenter;
import com.wali.live.watchsdk.component.presenter.panel.MessagePresenter;
import com.wali.live.watchsdk.component.presenter.panel.WatchMenuPresenter;
import com.wali.live.watchsdk.component.view.panel.MessagePanel;
import com.wali.live.watchsdk.component.view.panel.WatchMenuPanel;
import com.wali.live.watchsdk.fans.FansGroupListFragment;

import java.lang.ref.SoftReference;

import static com.wali.live.component.BaseSdkController.MSG_HIDE_BOTTOM_PANEL;
import static com.wali.live.component.BaseSdkController.MSG_ON_BACK_PRESSED;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_FANS_PANEL;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_MENU_PANEL;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_MESSAGE_PANEL;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_SHARE_PANEL;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 观众端底部面板表现
 */
public class PanelContainerPresenter extends BaseContainerPresenter<RelativeLayout> {
    private static final String TAG = "PanelContainerPresenter";
    private RoomBaseDataModel mMyRoomData;

    private SoftReference<MessagePanel> mMessagePanelRef;
    private SoftReference<MessagePresenter> mMessagePresenterRef;
    private SoftReference<WatchMenuPanel> mMenuPanelRef;
    private SoftReference<WatchMenuPresenter> mMenuPresenterRef;

    @Override
    protected final String getTAG() {
        return TAG;
    }

    @Override
    public void setView(@NonNull RelativeLayout relativeLayout) {
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
            @NonNull RoomBaseDataModel myRoomData) {
        super(controller);
        mMyRoomData = myRoomData;
    }

    @Override
    public void startPresenter() {
        super.startPresenter();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_ON_BACK_PRESSED);
        registerAction(MSG_SHOW_SHARE_PANEL);
        registerAction(MSG_SHOW_MESSAGE_PANEL);
        registerAction(MSG_HIDE_BOTTOM_PANEL);
        registerAction(MSG_SHOW_MENU_PANEL);
        registerAction(MSG_SHOW_FANS_PANEL);
    }

    @Override
    public void stopPresenter() {
        super.stopPresenter();
        unregisterAllAction();
        hidePanel(false);
    }

    private void showSharePanel() {
        SnsShareHelper.getInstance().shareToSns(-1, mMyRoomData);
    }

    private void showMessagePanel() {
        MessagePanel panel = deRef(mMessagePanelRef);
        if (panel == null) {
            panel = new MessagePanel(mView);
            mMessagePanelRef = new SoftReference<>(panel);
            MessagePresenter presenter = deRef(mMessagePresenterRef);
            if (presenter == null) {
                presenter = new MessagePresenter(mController);
                mMessagePresenterRef = new SoftReference<>(presenter);
            }
            setupComponent(panel, presenter);
        }
        showPanel(panel, true);
    }

    private void showWatchMenuPanel(int unReadCnt) {
        WatchMenuPanel panel = deRef(mMenuPanelRef);
        if (panel == null) {
            panel = new WatchMenuPanel(mView, unReadCnt, mMyRoomData.getEnableShare());
            mMenuPanelRef = new SoftReference<>(panel);
            WatchMenuPresenter presenter = deRef(mMenuPresenterRef);
            if (presenter == null) {
                presenter = new WatchMenuPresenter(mController);
                mMenuPresenterRef = new SoftReference<>(presenter);
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
            case MSG_SHOW_SHARE_PANEL:
                showSharePanel();
                return true;
            case MSG_SHOW_MESSAGE_PANEL:
                showMessagePanel();
                return true;
            case MSG_HIDE_BOTTOM_PANEL:
            case MSG_ON_BACK_PRESSED:
                return hidePanel(true);
            case MSG_SHOW_MENU_PANEL:
                int unReadCnt = params.getItem(0);
                showWatchMenuPanel(unReadCnt);
                break;
            case MSG_SHOW_FANS_PANEL:
                FansGroupListFragment.open((BaseActivity) mView.getContext());
                break;
            default:
                break;
        }
        return false;
    }
}
