package com.wali.live.watchsdk.component;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.BaseSdkView;
import com.wali.live.component.presenter.InputAreaPresenter;
import com.wali.live.component.view.InputAreaView;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.presenter.BottomButtonPresenter;
import com.wali.live.watchsdk.component.presenter.PanelContainerPresenter;
import com.wali.live.watchsdk.component.view.WatchBottomButton;
import com.wali.live.watchsdk.component.view.WatchPanelContainer;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 游戏直播页面
 */
public class WatchSdkView extends BaseSdkView<WatchComponentController> {
    private static final String TAG = "WatchSdkView";

    protected RoomBaseDataModel mMyRoomData;
    protected boolean mIsGameMode = false;

    public WatchSdkView(
            @NonNull Activity activity,
            @NonNull WatchComponentController componentController,
            @NonNull RoomBaseDataModel myRoomData) {
        super(activity, componentController);
        mMyRoomData = myRoomData;
    }

    public void setupSdkView(boolean isGameMode) {
        mIsGameMode = isGameMode;
        setupSdkView();
    }
    
    @Override
    public void setupSdkView() {
        // 输入框
        {
            InputAreaView view = $(R.id.input_area_view);
            if (view == null) {
                return;
            }
            InputAreaPresenter presenter = new InputAreaPresenter(mComponentController, mMyRoomData);
            addComponentView(view, presenter);
        }

        // 底部面板
        {
            RelativeLayout relativeLayout = $(R.id.bottom_panel_view);
            if (relativeLayout == null) {
                MyLog.e(TAG, "missing R.id.bottom_panel_view");
                return;
            }
            WatchPanelContainer view = new WatchPanelContainer(relativeLayout);
            PanelContainerPresenter presenter = new PanelContainerPresenter(
                    mComponentController, mComponentController.mRoomChatMsgManager);
            addComponentView(view, presenter);
        }

        // 底部按钮
        {
            RelativeLayout relativeLayout = $(R.id.bottom_button_view);
            if (relativeLayout == null) {
                MyLog.e(TAG, "missing R.id.bottom_button_view");
                return;
            }
            relativeLayout.setVisibility(View.VISIBLE);
            WatchBottomButton view = new WatchBottomButton(relativeLayout, mIsGameMode);
            BottomButtonPresenter presenter =
                    new BottomButtonPresenter(mComponentController);
            addComponentView(view, presenter);
        }
    }
}
