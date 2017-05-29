package com.wali.live.watchsdk.component;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.wali.live.component.BaseSdkView;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.presenter.InputAreaPresenter;
import com.wali.live.watchsdk.component.view.InputAreaView;

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
        // 输入框
        {
            InputAreaView view = $(R.id.input_area_view);
            if (view == null) {
                return;
            }
            InputAreaPresenter presenter = new InputAreaPresenter(
                    mComponentController, null/*TODO use mComponentController.mMyRoomData*/);
            addComponentView(view, presenter);
        }
    }
}
