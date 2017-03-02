package com.wali.live.watchsdk.component;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.component.BaseSdkView;
import com.wali.live.watchsdk.component.presenter.InputAreaPresenter;
import com.wali.live.watchsdk.component.view.InputAreaView;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.presenter.BottomButtonPresenter;
import com.wali.live.watchsdk.component.presenter.GameBarragePresenter;
import com.wali.live.watchsdk.component.presenter.GameInputPresenter;
import com.wali.live.watchsdk.component.presenter.PanelContainerPresenter;
import com.wali.live.watchsdk.component.view.GameBarrageView;
import com.wali.live.watchsdk.component.view.GameInputView;
import com.wali.live.watchsdk.component.view.WatchBottomButton;
import com.wali.live.watchsdk.component.view.WatchPanelContainer;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 游戏直播页面
 */
public class WatchSdkView extends BaseSdkView<WatchComponentController> {
    private static final String TAG = "WatchSdkView";

    @NonNull
    protected RoomBaseDataModel mMyRoomData;
    protected boolean mIsGameMode = false;

    public WatchSdkView(
            @NonNull Activity activity,
            @NonNull WatchComponentController componentController,
            @NonNull RoomBaseDataModel myRoomData) {
        super(activity, componentController);
        mMyRoomData = myRoomData;
    }

    public final <T extends View> void addView(
            @NonNull T view,
            @NonNull ViewGroup.LayoutParams params,
            @IdRes int anchorId) {
        ViewGroup rootView = (ViewGroup) mActivity.findViewById(R.id.main_act_container);
        View anchorView = $(anchorId);
        int pos = anchorView != null ? rootView.indexOfChild(anchorView) : -1;
        if (pos >= 0) {
            rootView.addView(view, pos + 1, params);
        } else {
            rootView.addView(view, params);
        }
    }

    public void setupSdkView(boolean isGameMode) {
        mIsGameMode = isGameMode;
        setupSdkView();
        if (mIsGameMode) {
            // 游戏直播横屏输入框
            {
                GameInputView view = new GameInputView(mActivity);
                view.setVisibility(View.GONE);
                GameInputPresenter presenter = new GameInputPresenter(mComponentController, mMyRoomData);
                addComponentView(view, presenter);
                // add view to activity
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                addView(view, layoutParams, R.id.input_area_view);
            }

            // 游戏直播横屏弹幕
            {
                GameBarrageView view = new GameBarrageView(mActivity);
                view.setVisibility(View.GONE);
                GameBarragePresenter presenter = new GameBarragePresenter(mComponentController);
                addComponentView(view, presenter);
                // add view to activity
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtils.dip2px(96.77f));
                layoutParams.bottomMargin = DisplayUtils.dip2px(56f);
                layoutParams.rightMargin = DisplayUtils.dip2px(56f);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                addView(view, layoutParams, R.id.comment_rv);
            }
        }
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
