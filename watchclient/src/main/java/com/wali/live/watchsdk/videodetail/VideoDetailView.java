package com.wali.live.watchsdk.videodetail;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.wali.live.component.BaseSdkView;
import com.wali.live.component.ComponentController;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.R;
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
 *
 * @module 详情播放半屏
 */
public class VideoDetailView extends BaseSdkView<VideoDetailController> {
    private static final String TAG = "VideoDetailView";

    @NonNull
    protected ViewGroup mParentView;
    @NonNull
    protected View mContentView;

    protected View mTouchView;

    @NonNull
    protected final Action mAction = new Action();

    @Override
    protected final <V extends View> V $(@IdRes int id) {
        return (V) mContentView.findViewById(id);
    }

    @Override
    protected String getTAG() {
        return "VideoDetailView";
    }

    public VideoDetailView(
            @NonNull Activity activity,
            @NonNull VideoDetailController componentController) {
        super(activity, componentController);
    }

    @Override
    public void setupSdkView() {
        mParentView = (ViewGroup) mActivity.findViewById(android.R.id.content);
        mParentView.setBackgroundColor(Color.BLACK);
        mContentView = mParentView.findViewById(R.id.main_act_container);

        // 信息区域
        {
            View contentView = $(R.id.info_area);
            if (contentView == null) {
                MyLog.e(TAG, "missing R.id.info_area");
                return;
            }
            DetailInfoView view = new DetailInfoView(contentView);
            DetailInfoPresenter presenter = new DetailInfoPresenter(mComponentController,
                    mComponentController.mMyRoomData);
            addComponentView(view, presenter);
        }

        // TAB区域
        {
            DetailTabView view = new DetailTabView(mContentView);
            DetailTabPresenter presenter = new DetailTabPresenter(mComponentController,
                    mComponentController.mMyRoomData);
            addComponentView(view, presenter);
        }

        // 底部按钮
        {
            View contentView = $(R.id.bottom_button_view);
            if (contentView == null) {
                MyLog.e(TAG, "missing R.id.bottom_button_view");
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
                MyLog.e(TAG, "missing R.id.input_area_view");
                return;
            }
            CommentInputPresenter presenter = new CommentInputPresenter(mComponentController);
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
    }

    private Animation mShowAnimation;

    @Override
    public void startSdkView() {
        MyLog.w(TAG, "startSdkView");
        if (mParentView.indexOfChild(mContentView) == -1) {
            mParentView.addView(mContentView);
            if (mShowAnimation == null) {
                mShowAnimation = new AlphaAnimation(0, 1);
                mShowAnimation.setDuration(400);
            }
            mContentView.startAnimation(mShowAnimation);
        }
        for (ComponentPresenter presenter : mComponentPresenterSet) {
            presenter.startPresenter();
        }
        mAction.registerAction();

        // 添加播放器View
        VideoDetailPlayerView view = mComponentController.mPlayerView;
        if (view == null) {
            MyLog.e(TAG, "missing mComponentController.mPlayerView");
            return;
        }
        view.switchToFullScreen(false);
        view.showOrHideFullScreenBtn(true);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.height = (int) mActivity.getResources().getDimension(R.dimen.view_dimen_608);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.status_bar);
        addViewUnderAnchor(view, layoutParams, null);
    }

    @Override
    public void stopSdkView() {
        MyLog.w(TAG, "stopSdkView");
        mContentView.clearAnimation();
        mParentView.removeView(mContentView);

        for (ComponentPresenter presenter : mComponentPresenterSet) {
            presenter.stopPresenter();
        }
        mAction.unregisterAction();

        // 将播放器View从其父View移出
        ViewGroup parentView = mComponentController.mPlayerView != null ?
                (ViewGroup) mComponentController.mPlayerView.getParent() : null;
        if (parentView != null && parentView.indexOfChild(mComponentController.mPlayerView) != -1) {
            parentView.removeView(mComponentController.mPlayerView);
        }
    }

    @Override
    public void releaseSdkView() {
        super.releaseSdkView();
        MyLog.w(TAG, "releaseSdkView");
    }

    public class Action implements ComponentPresenter.IAction {

        public void registerAction() {
            mComponentController.registerAction(MSG_INPUT_VIEW_SHOWED, this);
            mComponentController.registerAction(MSG_INPUT_VIEW_HIDDEN, this);
        }

        public void unregisterAction() {
            mComponentController.unregisterAction(this);
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
