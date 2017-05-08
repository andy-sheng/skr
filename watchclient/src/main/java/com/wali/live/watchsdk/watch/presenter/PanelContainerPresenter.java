package com.wali.live.watchsdk.watch.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.ComponentController;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.watchsdk.component.presenter.BaseContainerPresenter;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 观众端底部面板表现
 */
public class PanelContainerPresenter extends BaseContainerPresenter<RelativeLayout> {
    private static final String TAG = "PanelContainerPresenter";

    private BaseBottomPanel mSharePanel;
    private RoomBaseDataModel mMyRoomData;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public PanelContainerPresenter(
            @NonNull IComponentController componentController,
            @NonNull RoomBaseDataModel myRoomData) {
        super(componentController);
        mMyRoomData = myRoomData;
        registerAction(ComponentController.MSG_ON_ORIENT_PORTRAIT);
        registerAction(ComponentController.MSG_ON_ORIENT_LANDSCAPE);
        registerAction(ComponentController.MSG_ON_BACK_PRESSED);
        registerAction(ComponentController.MSG_SHOW_SHARE_PANEL);
        registerAction(ComponentController.MSG_HIDE_BOTTOM_PANEL);
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

    private void showSharePanel() {
//        if (mSharePanel == null) {
//            mSharePanel = new ShareControlPanel(mView, mComponentController, mMyRoomData);
//        }
//        showPanel(mSharePanel, true);
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
            if (mView == null) {
                MyLog.e(TAG, "onAction but mView is null, source=" + source);
                return false;
            }
            switch (source) {
                case ComponentController.MSG_ON_ORIENT_PORTRAIT:
                    onOrientation(false);
                    return true;
                case ComponentController.MSG_ON_ORIENT_LANDSCAPE:
                    onOrientation(true);
                    return true;
                case ComponentController.MSG_SHOW_SHARE_PANEL:
                    showSharePanel();
                    return true;
                case ComponentController.MSG_HIDE_BOTTOM_PANEL:
                case ComponentController.MSG_ON_BACK_PRESSED:
                    return hidePanel(true);
                default:
                    break;

            }
            return false;
        }
    }
}
