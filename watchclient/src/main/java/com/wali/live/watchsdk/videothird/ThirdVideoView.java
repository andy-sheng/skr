package com.wali.live.watchsdk.videothird;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.thornbirds.component.IParams;
import com.wali.live.component.BaseSdkView;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.videothird.presenter.VideoControlPresenter;
import com.wali.live.watchsdk.videothird.view.VideoControlView;

import java.lang.ref.WeakReference;

/**
 * Created by yangli on 2017/8/28.
 */
public class ThirdVideoView extends BaseSdkView<View, ThirdVideoController> {
    private static final String TAG = "VideoDetailView";

    protected final AnimationHelper mAnimationHelper = new AnimationHelper();

    @Override
    protected String getTAG() {
        return "VideoDetailView";
    }

    public ThirdVideoView(
            @NonNull Activity activity,
            @NonNull ThirdVideoController controller) {
        super(activity, (ViewGroup) activity.findViewById(android.R.id.content), controller);
    }

    @Override
    public void setupView() {
        mParentView.setBackgroundColor(Color.BLACK);
        mContentView = mParentView.findViewById(R.id.main_act_container);
    }

    @Override
    public void startView() {
        super.startView();
        if (mParentView.indexOfChild(mContentView) == -1) {
            mParentView.addView(mContentView);
            mAnimationHelper.startShowAnimation();
        }
        // 播放控制View
        {
            VideoControlView view = $(R.id.video_control_view);
            if (view == null) {
                MyLog.e(TAG, "missing missing R.id.video_control_view");
                return;
            }
            VideoControlPresenter presenter = new VideoControlPresenter(mController);
            registerComponent(view, presenter);
        }
        // 添加播放器View
        {
            SurfaceView view = mController.mPlayerView;
            if (view == null) {
                MyLog.e(TAG, "missing mController.mPlayerView");
                return;
            }
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            addViewUnderAnchor(view, layoutParams, null);
        }
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
