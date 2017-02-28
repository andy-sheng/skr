package com.wali.live.livesdk.live.livegame;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.BaseSdkView;
import com.wali.live.component.presenter.InputAreaPresenter;
import com.wali.live.component.view.InputAreaView;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.livegame.presenter.BottomButtonPresenter;
import com.wali.live.livesdk.live.livegame.presenter.PanelContainerPresenter;
import com.wali.live.livesdk.live.livegame.view.LiveBottomButton;
import com.wali.live.livesdk.live.livegame.view.LivePanelContainer;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 游戏直播页面
 */
public class LiveSdkView extends BaseSdkView<LiveComponentController> {
    private static final String TAG = "LiveSdkView";

    protected RoomBaseDataModel mMyRoomData;

    public LiveSdkView(
            @NonNull Activity activity,
            @NonNull LiveComponentController componentController,
            @NonNull RoomBaseDataModel myRoomData) {
        super(activity, componentController);
        mMyRoomData = myRoomData;
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
            LivePanelContainer view = new LivePanelContainer(relativeLayout);
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
            LiveBottomButton view = new LiveBottomButton(relativeLayout);
            BottomButtonPresenter presenter =
                    new BottomButtonPresenter(mComponentController, mComponentController.mGameLivePresenter);
            addComponentView(view, presenter);
        }

        mComponentController.onEvent(LiveComponentController.MSG_SHOW_BARRAGE_SWITCH);
    }
}
