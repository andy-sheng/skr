package com.wali.live.watchsdk.watch.presenter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.base.utils.CommonUtils;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.IParams;
import com.wali.live.component.ComponentController;
import com.wali.live.componentwrapper.BaseSdkController;
import com.wali.live.watchsdk.component.presenter.BaseContainerPresenter;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 观众端底部面板表现
 */
public class PanelContainerPresenter extends BaseContainerPresenter<RelativeLayout> {
    private static final String TAG = "PanelContainerPresenter";
    private RoomBaseDataModel mMyRoomData;

    @Override
    protected String getTAG() {
        return TAG;
    }

    public PanelContainerPresenter(
            @NonNull BaseSdkController controller,
            @NonNull RoomBaseDataModel myRoomData) {
        super(controller);
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
        SnsShareHelper.getInstance().shareToSns(-1, mMyRoomData);
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        if (mView == null || CommonUtils.isFastDoubleClick()) {
            MyLog.e(TAG, "onAction but mView is null, event=" + event + " or CommonUtils.isFastDoubleClick() is true");
            return false;
        }
        switch (event) {
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
