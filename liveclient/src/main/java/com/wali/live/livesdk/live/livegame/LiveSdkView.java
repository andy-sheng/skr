package com.wali.live.livesdk.live.livegame;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.component.BaseSdkView;
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

    public LiveSdkView(
            @NonNull Activity activity,
            @NonNull LiveComponentController componentController) {
        super(activity, componentController);
    }

    @Override
    public void setupSdkView() {
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
    }
}
