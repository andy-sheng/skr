package com.wali.live.watchsdk.videodetail;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.view.View;

import com.wali.live.component.BaseSdkView;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.presenter.InputAreaPresenter;
import com.wali.live.watchsdk.component.presenter.VideoDetailPlayerPresenter;
import com.wali.live.watchsdk.component.view.InputAreaView;
import com.wali.live.watchsdk.component.view.VideoDetailPlayerView;
import com.wali.live.watchsdk.videodetail.presenter.DetailBottomPresenter;
import com.wali.live.watchsdk.videodetail.presenter.InfoAreaPresenter;
import com.wali.live.watchsdk.videodetail.view.DetailBottomView;
import com.wali.live.watchsdk.videodetail.view.InfoAreaView;

/**
 * Created by yangli on 2017/5/26.
 */
public class VideoDetailView extends BaseSdkView<VideoDetailController> {

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
            InfoAreaView view = new InfoAreaView(contentView);
            InfoAreaPresenter presenter = new InfoAreaPresenter(mComponentController,
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
            InputAreaPresenter presenter = new InputAreaPresenter(
                    mComponentController, mComponentController.mMyRoomData);
            addComponentView(view, presenter);
        }

        {
            VideoDetailPlayerView view = $(R.id.video_detail_player_view);
            if (view == null) {
                return;
            }
            view.setMyRoomData(mComponentController.mMyRoomData);
            VideoDetailPlayerPresenter presenter = new VideoDetailPlayerPresenter(mComponentController, mComponentController.mMyRoomData, mActivity);
            presenter.setComponentView(view.getViewProxy());
            addComponentView(view, presenter);
        }
    }
}
