package com.wali.live.watchsdk.videodetail;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.thornbirds.component.IParams;
import com.wali.live.component.BaseSdkView;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.view.InputAreaView;
import com.wali.live.watchsdk.videodetail.presenter.CommentInputPresenter;
import com.wali.live.watchsdk.videodetail.presenter.DetailBottomPresenter;
import com.wali.live.watchsdk.videodetail.presenter.DetailInfoPresenter;
import com.wali.live.watchsdk.videodetail.presenter.DetailTabPresenter;
import com.wali.live.watchsdk.videodetail.view.DetailBottomView;
import com.wali.live.watchsdk.videodetail.view.DetailInfoView;
import com.wali.live.watchsdk.videodetail.view.DetailPlayerView;
import com.wali.live.watchsdk.videodetail.view.DetailTabView;

import java.lang.ref.WeakReference;

import static com.wali.live.component.BaseSdkController.MSG_HIDE_INPUT_VIEW;
import static com.wali.live.component.BaseSdkController.MSG_INPUT_VIEW_HIDDEN;
import static com.wali.live.component.BaseSdkController.MSG_INPUT_VIEW_SHOWED;

/**
 * Created by yangli on 2017/5/26.
 *
 * @module 详情播放半屏
 */
public class VideoDetailView extends BaseSdkView<View, VideoDetailController> {
    private static final String TAG = "VideoDetailView";

    protected View mTouchView;

    protected final AnimationHelper mAnimationHelper = new AnimationHelper();

    @Override
    protected String getTAG() {
        return "VideoDetailView";
    }

    public VideoDetailView(
            @NonNull Activity activity,
            @NonNull VideoDetailController controller) {
        super(activity, (ViewGroup) activity.findViewById(android.R.id.content), controller);
    }

    @Override
    public void setupView() {
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
            DetailInfoPresenter presenter = new DetailInfoPresenter(mController,
                    mController.mMyRoomData);
            registerComponent(view, presenter);
        }
        // TAB区域
        {
            DetailTabView view = new DetailTabView(mContentView);
            DetailTabPresenter presenter = new DetailTabPresenter(mController,
                    mController.mMyRoomData);
            registerComponent(view, presenter);
        }
        // 底部按钮
        {
            View contentView = $(R.id.bottom_button_view);
            if (contentView == null) {
                MyLog.e(TAG, "missing R.id.bottom_button_view");
                return;
            }
            boolean enableShare = (mController.mMyRoomData == null) ? false : mController.mMyRoomData.getEnableShare();
            DetailBottomView view = new DetailBottomView(contentView, enableShare);
            DetailBottomPresenter presenter = new DetailBottomPresenter(mController,
                    mController.mMyRoomData);
            registerComponent(view, presenter);
        }
        // 输入框
        {
            InputAreaView view = $(R.id.input_area_view);
            if (view == null) {
                MyLog.e(TAG, "missing R.id.input_area_view");
                return;
            }
            CommentInputPresenter presenter = new CommentInputPresenter(mController);
            registerComponent(view, presenter);
        }
        // 触摸
        {
            mTouchView = $(R.id.touch_view);
            mTouchView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        mController.postEvent(MSG_HIDE_INPUT_VIEW);
                    }
                    return false;
                }
            });
        }
    }

    @Override
    public void startView() {
        super.startView();
        if (mParentView.indexOfChild(mContentView) == -1) {
            mParentView.addView(mContentView);
            mAnimationHelper.startShowAnimation();
        }

        // 添加播放器View
        DetailPlayerView view = mController.mPlayerView;
        if (view == null) {
            MyLog.e(TAG, "missing mController.mPlayerView");
            return;
        }
//        view.switchToFullScreen(false);
//        view.showOrHideFullScreenBtn(true);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.height = (int) mActivity.getResources().getDimension(R.dimen.view_dimen_608);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.status_bar);
        addViewUnderAnchor(view, layoutParams, null);

        registerAction(MSG_INPUT_VIEW_SHOWED);
        registerAction(MSG_INPUT_VIEW_HIDDEN);
    }

    @Override
    public void stopView() {
        super.stopView();
        mAnimationHelper.clearAnimation();
        mParentView.removeView(mContentView);

        // 将播放器View从其父View移出
        ViewGroup parentView = mController.mPlayerView != null ?
                (ViewGroup) mController.mPlayerView.getParent() : null;
        if (parentView != null && parentView.indexOfChild(mController.mPlayerView) != -1) {
            parentView.removeView(mController.mPlayerView);
        }
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        switch (event) {
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

    public class AnimationHelper extends BaseSdkView.AnimationHelper {

        private WeakReference<Animation> mShowAnimationRef; // 出现动画

        private void startShowAnimation() {
            Animation animation = deRef(mShowAnimationRef);
            if (animation == null) {
                animation = new AlphaAnimation(0, 1);
                animation.setDuration(400);
                mShowAnimationRef = new WeakReference<>(animation);
            }
            mContentView.startAnimation(animation);
        }

        @Override
        protected void stopAllAnimator() {
            stopRefAnimation(mShowAnimationRef);
        }

        @Override
        public void clearAnimation() {
            stopAllAnimator();
            mShowAnimationRef = null;
        }
    }
}
