package com.wali.live.watchsdk.videodetail;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;

import com.wali.live.component.BaseSdkView;
import com.wali.live.component.ComponentController;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.presenter.VideoDetailPlayerPresenter;
import com.wali.live.watchsdk.component.view.InputAreaView;
import com.wali.live.watchsdk.component.view.VideoDetailPlayerView;
import com.wali.live.watchsdk.videodetail.presenter.CommentInputPresenter;
import com.wali.live.watchsdk.videodetail.presenter.DetailBottomPresenter;
import com.wali.live.watchsdk.videodetail.presenter.DetailInfoPresenter;
import com.wali.live.watchsdk.videodetail.presenter.DetailTabPresenter;
import com.wali.live.watchsdk.videodetail.view.DetailBottomView;
import com.wali.live.watchsdk.videodetail.view.DetailInfoView;
import com.wali.live.watchsdk.videodetail.view.DetailTabView;

import static com.wali.live.component.ComponentController.MSG_INPUT_VIEW_HIDDEN;
import static com.wali.live.component.ComponentController.MSG_INPUT_VIEW_SHOWED;

/**
 * Created by yangli on 2017/5/26.
 */
public class VideoDetailView extends BaseSdkView<VideoDetailController> {

    protected View mTouchView;

    @NonNull
    protected final Action mAction = new Action();

    public VideoDetailView(
            @NonNull Activity activity,
            @NonNull VideoDetailController componentController) {
        super(activity, componentController);
    }

    @Override
    public void setupSdkView() {
        // 信息区域
        {
            View contentView = $(R.id.info_area);
            if (contentView == null) {
                return;
            }
            DetailInfoView view = new DetailInfoView(contentView);
            DetailInfoPresenter presenter = new DetailInfoPresenter(mComponentController,
                    mComponentController.mMyRoomData);
            addComponentView(view, presenter);
        }

        // TAB区域
        {
            DetailTabView view = new DetailTabView(
                    $(android.R.id.content), mComponentController, mComponentController.mMyRoomData);
            DetailTabPresenter presenter = new DetailTabPresenter(mComponentController,
                    mComponentController.mMyRoomData);
            addComponentView(view, presenter);
        }

        // 底部按钮
        {
            View contentView = $(R.id.bottom_button_view);
            if (contentView == null) {
                return;
            }
            DetailBottomView view = new DetailBottomView(contentView);
            DetailBottomPresenter presenter = new DetailBottomPresenter(mComponentController,
                    mComponentController.mMyRoomData);
            addComponentView(view, presenter);
        }

        // 输入框
        {
            InputAreaView view = $(R.id.input_area_view);
            if (view == null) {
                return;
            }
            CommentInputPresenter presenter = new CommentInputPresenter(mComponentController);
            addComponentView(view, presenter);
        }

        // 播放器
        {
            VideoDetailPlayerView view = $(R.id.video_detail_player_view);
            if (view == null) {
                return;
            }
            view.setMyRoomData(mComponentController.mMyRoomData);
            VideoDetailPlayerPresenter presenter = new VideoDetailPlayerPresenter(
                    mComponentController, mComponentController.mMyRoomData, mActivity);
            presenter.setComponentView(view.getViewProxy());
            addComponentView(view, presenter);
        }

        // 触摸
        {
            mTouchView = $(R.id.touch_view);
            mTouchView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        mComponentController.onEvent(ComponentController.MSG_HIDE_INPUT_VIEW);
                    }
                    return false;
                }
            });
        }

        mAction.registerAction(); // 最后注册该Action，任何事件mAction都最后收到
    }

    public class Action implements ComponentPresenter.IAction {

        public void registerAction() {
            mComponentController.registerAction(MSG_INPUT_VIEW_SHOWED, this);
            mComponentController.registerAction(MSG_INPUT_VIEW_HIDDEN, this);
        }

        @Override
        public boolean onAction(int source, @Nullable ComponentPresenter.Params params) {
            switch (source) {
                case MSG_INPUT_VIEW_SHOWED:
                    mTouchView.setVisibility(View.VISIBLE);
                    return true;
                case MSG_INPUT_VIEW_HIDDEN:
                    mTouchView.setVisibility(View.GONE);
                    return true;
                default:
                    break;
            }
            return false;
        }
    }
}
