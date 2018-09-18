package com.wali.live.watchsdk.watch.view.watchgameview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchComponentController;

import static com.wali.live.watchsdk.channel.data.ChannelDataStore.GAME_WATCH_CHANNEL_FROM_LANDSCAPE;

/**
 * Created by liuting on 18-9-13.
 */

public class WatchGameFullScreenMoreView extends WatchGameMoreLiveView {
    private int mWidth = getResources().getDimensionPixelSize(R.dimen.view_dimen_873);
    private ValueAnimator mShowAndHideAnimator;
    private boolean isVisibleToUser;

    public WatchGameFullScreenMoreView(Context context, WatchComponentController componentController) {
        super(context, componentController);
    }

    @Override
    protected void inflateLayout(Context context) {
        inflate(context, R.layout.watch_game_fullscreen_more_layout, this);
    }

    @Override
    protected void init(Context context, WatchComponentController componentController) {
        super.init(context, componentController);
        setBackgroundColor(getResources().getColor(R.color.color_black_trans_70));
    }

    @Override
    protected int getLiveReqFrom() {
        return GAME_WATCH_CHANNEL_FROM_LANDSCAPE;
    }

    @Override
    protected void onViewOrientChange(boolean isLandscape) {
        hideSelfWithoutAnimation();
    }

    @Override
    protected void onViewFullScreenMoreLiveClick() {
        showSelfWithAnimatiom();
    }
    @Override
    protected void onViewVideoTouchViewClick() {
        hideSelfWithAnimation();
    }

    public void addSelfToWatchLayoutAndShow(ViewGroup parentView) {
        if (getParent() == null) {
            RelativeLayout.LayoutParams layoutParams = new LayoutParams(mWidth, LayoutParams.MATCH_PARENT);
            layoutParams.addRule(ALIGN_PARENT_RIGHT);
            layoutParams.rightMargin = mWidth; // 初始化add到父布局时先不显示
            setLayoutParams(layoutParams);
            parentView.addView(this);
        }
        showSelfWithAnimatiom(); // 动画显示
    }

    private void showSelfWithAnimatiom() {
        if (isVisibleToUser) {
            return;
        }

        isVisibleToUser = true;
        initShowAndHideAnimatorIfNeed();
        mShowAndHideAnimator.start();
    }

    private void hideSelfWithAnimation() {
        if (!isVisibleToUser) {
            return;
        }

        isVisibleToUser = false;
        initShowAndHideAnimatorIfNeed();
        mShowAndHideAnimator.reverse();
    }

    private void hideSelfWithoutAnimation() {
        if (!isVisibleToUser) {
            return;
        }

        isVisibleToUser = false;
        RelativeLayout.LayoutParams layoutParams = (LayoutParams) getLayoutParams();
        layoutParams.rightMargin = -mWidth;
        setLayoutParams(layoutParams);
    }

    private void initShowAndHideAnimatorIfNeed() {
        if (mShowAndHideAnimator == null) {
            mShowAndHideAnimator = ValueAnimator.ofInt(mWidth, 0);
            mShowAndHideAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = -(int) animation.getAnimatedValue();
                    RelativeLayout.LayoutParams layoutParams = (LayoutParams) getLayoutParams();
                    layoutParams.rightMargin = value;
                    setLayoutParams(layoutParams);
                }
            });
            mShowAndHideAnimator.setRepeatCount(0);
            mShowAndHideAnimator.setDuration(400);
        }
    }

    public void stopView() {
        destroyView();
    }

}
