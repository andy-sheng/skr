package com.wali.live.watchsdk.watch.view.watchgameview;

import android.animation.ValueAnimator;
import android.content.Context;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.WatchComponentController;

import static com.wali.live.watchsdk.channel.data.ChannelDataStore.GAME_WATCH_CHANNEL_FROM_PORTRAIT;

/**
 * Created by liuting on 18-9-10.
 */

public class WatchGameMoreTabView extends WatchGameMoreLiveView implements WatchGameTabView.GameTabChildView{
    protected LinearLayout mTopTabContainer;

    public WatchGameMoreTabView(Context context, WatchComponentController componentController) {
        super(context, componentController);
    }

    @Override
    protected void inflateLayout(Context context) {
        inflate(context, R.layout.watch_game_more_tab_layout, this);
    }

    @Override
    protected void init(Context context, WatchComponentController componentController) {
        super.init(context, componentController);
        mTopTabContainer = (LinearLayout) findViewById(R.id.top_tab_container);
    }

    @Override
    protected void onRecLiveChannelShowTabBarEvent(boolean show, long channelId) {
        showTabBar(show);
    }

    @Override
    protected int getLiveReqFrom() {
        return GAME_WATCH_CHANNEL_FROM_PORTRAIT;
    }

    @Override
    public void select() {

    }

    @Override
    public void unselect() {

    }

    @Override
    public void stopView() {
        destroyView();
    }

    private ValueAnimator mTabBarAnimator;
    private boolean mIsTabBarShowed = true;
    private void showTabBar(boolean show) {
        if (mTopTabContainer == null) {
            return;
        }
        if (mIsTabBarShowed == show) {
            return;
        }
        mIsTabBarShowed = show;

        if (mTabBarAnimator == null) {
            mTabBarAnimator = ValueAnimator.ofInt(0, mTopTabContainer.getHeight());
            mTabBarAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) mTopTabContainer.getLayoutParams();
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    layoutParams.topMargin = - value;
                    mTopTabContainer.setLayoutParams(layoutParams);
                }
            });
            mTabBarAnimator.setRepeatCount(0);
            mTabBarAnimator.setDuration(300);
        }
        if (mTabBarAnimator.isRunning()) {
            mTabBarAnimator.end();
        }
        if (show) {
            mTabBarAnimator.reverse();
        } else {
            mTabBarAnimator.start();
        }
    }
}
