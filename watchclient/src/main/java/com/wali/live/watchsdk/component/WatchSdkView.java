package com.wali.live.watchsdk.component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.support.annotation.FloatRange;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.common.gift.view.GiftContinueViewGroup;
import com.wali.live.component.BaseSdkView;
import com.wali.live.component.ComponentController;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.component.presenter.BottomButtonPresenter;
import com.wali.live.watchsdk.component.presenter.GameBarragePresenter;
import com.wali.live.watchsdk.component.presenter.GameInputPresenter;
import com.wali.live.watchsdk.component.presenter.InputAreaPresenter;
import com.wali.live.watchsdk.component.presenter.LiveCommentPresenter;
import com.wali.live.watchsdk.component.presenter.PanelContainerPresenter;
import com.wali.live.watchsdk.component.presenter.TouchPresenter;
import com.wali.live.watchsdk.component.view.GameBarrageView;
import com.wali.live.watchsdk.component.view.GameInputView;
import com.wali.live.watchsdk.component.view.InputAreaView;
import com.wali.live.watchsdk.component.view.LiveCommentView;
import com.wali.live.watchsdk.component.view.WatchBottomButton;
import com.wali.live.watchsdk.component.view.WatchPanelContainer;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 游戏直播页面
 */
public class WatchSdkView extends BaseSdkView<WatchComponentController> {
    private static final String TAG = "WatchSdkView";

    @NonNull
    protected final Action mAction = new Action();

    @NonNull
    protected RoomBaseDataModel mMyRoomData;
    @NonNull
    protected LiveRoomChatMsgManager mRoomChatMsgManager;

    @Nullable
    protected GiftContinueViewGroup mGiftContinueViewGroup;
    @Nullable
    protected View mWatchTopInfoSingleView;
    @Nullable
    protected View mLiveCommentView;

    protected boolean mIsGameMode = false;
    protected boolean mIsHideAll = false;
    protected boolean mIsLandscape = false;

    public WatchSdkView(
            @NonNull Activity activity,
            @NonNull WatchComponentController componentController,
            @NonNull RoomBaseDataModel myRoomData,
            @NonNull LiveRoomChatMsgManager roomChatMsgManager) {
        super(activity, componentController);
        mMyRoomData = myRoomData;
        mRoomChatMsgManager = roomChatMsgManager;
    }

    @Override
    public void releaseSdkView() {
        super.releaseSdkView();
        mAction.clearAnimation();
    }

    private final <T extends View> void addView(
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
        if (mIsGameMode) {
            // 游戏直播横屏输入框
            {
                GameInputView view = new GameInputView(mActivity);
                view.setId(R.id.game_input_view);
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
                view.setId(R.id.game_barrage_view);
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
        setupSdkView();
    }

    @Override
    public void setupSdkView() {
        mGiftContinueViewGroup = $(R.id.gift_continue_vg); // 礼物
        mWatchTopInfoSingleView = $(R.id.watch_top_info_view); // 顶部view

        // 弹幕区
        {
            LiveCommentView view = $(R.id.live_comment_view);
            if (view == null) {
                return;
            }
            LiveCommentPresenter presenter = new LiveCommentPresenter(mComponentController);
            addComponentView(view, presenter);
            view.setToken(mRoomChatMsgManager.toString());

            mLiveCommentView = view;
        }

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

        // 滑动
        {
            View view = $(R.id.touch_view);
            if (view == null) {
                return;
            }
            TouchPresenter presenter = new TouchPresenter(mComponentController, view);
            addComponentView(presenter);

            presenter.addHorizontalView($(R.id.watch_top_info_view));
            presenter.addHorizontalView($(R.id.bottom_button_view));
            presenter.addHorizontalView($(R.id.live_comment_view));
            presenter.addHorizontalView($(R.id.gift_animation_player_view));
            presenter.addHorizontalView($(R.id.gift_continue_vg));
            presenter.addHorizontalView($(R.id.gift_room_effect_view));
            presenter.addVerticalView($(R.id.close_btn));
        }

        mAction.clearViewSet();
        if (mIsGameMode) {
            mAction.addGameHideView(
                    R.id.watch_top_info_view,
                    R.id.bottom_button_view,
                    R.id.game_barrage_view,
                    R.id.game_input_view,
                    R.id.close_btn);
        }
        mAction.registerAction();
    }

    public class Action implements ComponentPresenter.IAction {

        private <T> T deRef(WeakReference<?> reference) {
            return reference != null ? (T) reference.get() : null;
        }

        private void setAlpha(View view, @FloatRange(from = 0.0f, to = 1.0f) float alpha) {
            if (view != null) {
                view.setAlpha(alpha);
            }
        }

        private void setVisibility(View view, int visibility) {
            if (view != null) {
                view.setVisibility(visibility);
            }
        }

        private WeakReference<ValueAnimator> mInputAnimatorRef; // 输入框弹起时，隐藏
        private boolean mInputShow = false;
        /**
         * 输入框显示时，隐藏弹幕区和头部区
         * 弹幕区只在横屏下才需要显示和隐藏，直接修改visibility，在显示动画开始时显示，在消失动画结束时消失。
         */
        private void startInputAnimator(boolean inputShow) {
            if (mInputShow == inputShow) {
                return;
            }
            mInputShow = inputShow;
            ValueAnimator valueAnimator = deRef(mInputAnimatorRef);
            if (valueAnimator != null) {
                if (!valueAnimator.isStarted() && !valueAnimator.isRunning()) {
                    valueAnimator.start();
                }
                return;
            }
            valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            valueAnimator.setDuration(300);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    if (mInputShow) {
                        value = 1.0f - value;
                    }
                    setAlpha(mWatchTopInfoSingleView, value);
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (mInputShow) {
                        if (mIsLandscape && !mIsGameMode) {
                            setVisibility(mLiveCommentView, View.GONE);
                        }
                    } else {
                        setVisibility(mWatchTopInfoSingleView, View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mInputShow) {
                        setVisibility(mWatchTopInfoSingleView, View.GONE);
                    } else {
                        setAlpha(mWatchTopInfoSingleView, 1.0f);
                        if (mIsLandscape && !mIsGameMode) {
                            setVisibility(mLiveCommentView, View.VISIBLE);
                        }
                    }
                }
            });
            valueAnimator.start();
            mInputAnimatorRef = new WeakReference<>(valueAnimator);
        }

        private WeakReference<ValueAnimator> mGameAnimatorRef; // 游戏直播竖屏时，隐藏显示动画
        private boolean mGameShow = true;
        private final List<View> mGameViewSet = new ArrayList<>(0);

        public void clearViewSet() {
            mGameViewSet.clear();
        }

        public void addGameHideView(@IdRes int... idList) {
            if (idList != null && idList.length > 0) {
                for (int id : idList) {
                    mGameViewSet.add($(id));
                }
            }
        }

        /**
         * 观看游戏直播横屏时，点击隐藏显示View
         */
        private void startGameAnimator() {
            mGameShow = !mGameShow;
            ValueAnimator valueAnimator = deRef(mGameAnimatorRef);
            if (valueAnimator != null) {
                if (!valueAnimator.isStarted() && !valueAnimator.isRunning()) {
                    valueAnimator.start();
                }
                return;
            }
            valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
            valueAnimator.setDuration(300);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    if (!mGameShow) {
                        value = 1.0f - value;
                    }
                    for (View view : mGameViewSet) {
                        if (view != null) {
                            setAlpha(view, value);
                        }
                    }
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (mGameShow) {
                        for (View view : mGameViewSet) {
                            if (view != null) {
                                setAlpha(view, 0.0f);
                                setVisibility(view, View.VISIBLE);
                            }
                        }
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!mGameShow) {
                        for (View view : mGameViewSet) {
                            if (view != null) {
                                setAlpha(view, 1.0f);
                                setVisibility(view, View.GONE);
                            }
                        }
                    }
                }
            });
            valueAnimator.start();
            mGameAnimatorRef = new WeakReference<>(valueAnimator);
        }

        private void stopAllAnimator() {
            ValueAnimator valueAnimator = deRef(mInputAnimatorRef);
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            valueAnimator = deRef(mGameAnimatorRef);
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
        }

        public void clearAnimation() {
            stopAllAnimator();
            mInputAnimatorRef = null;
            mGameAnimatorRef = null;
        }

        public void registerAction() {
            mComponentController.registerAction(WatchComponentController.MSG_ON_ORIENT_PORTRAIT, this);
            mComponentController.registerAction(WatchComponentController.MSG_ON_ORIENT_LANDSCAPE, this);
            mComponentController.registerAction(WatchComponentController.MSG_INPUT_VIEW_SHOWED, this);
            mComponentController.registerAction(WatchComponentController.MSG_INPUT_VIEW_HIDDEN, this);
            mComponentController.registerAction(WatchComponentController.MSG_BACKGROUND_CLICK, this);
        }

        @Override
        public boolean onAction(int source, @Nullable ComponentPresenter.Params params) {
            switch (source) {
                case ComponentController.MSG_ON_ORIENT_PORTRAIT:
                    mIsLandscape = false;
                    stopAllAnimator();
                    if (mIsGameMode) {
                        mComponentController.onEvent(ComponentController.MSG_ENABLE_MOVE_VIEW);
                        setVisibility(mLiveCommentView, View.VISIBLE);
                    }
                    return true;
                case ComponentController.MSG_ON_ORIENT_LANDSCAPE:
                    mIsLandscape = true;
                    stopAllAnimator();
                    if (mIsGameMode) { // 游戏直播横屏不需左右滑
                        mComponentController.onEvent(ComponentController.MSG_DISABLE_MOVE_VIEW);
                        setVisibility(mLiveCommentView, View.INVISIBLE);
                    }
                    return true;
                case WatchComponentController.MSG_INPUT_VIEW_SHOWED:
                    if (!mIsGameMode || !mIsLandscape) {
                        mComponentController.onEvent(ComponentController.MSG_DISABLE_MOVE_VIEW);
                    }
                    if (mGiftContinueViewGroup != null) {
                        mGiftContinueViewGroup.onShowInputView();
                    }
                    startInputAnimator(true);
                    return true;
                case WatchComponentController.MSG_INPUT_VIEW_HIDDEN:
                    if (!mIsGameMode || !mIsLandscape) { // 游戏直播横屏不需左右滑
                        mComponentController.onEvent(ComponentController.MSG_ENABLE_MOVE_VIEW);
                    }
                    if (mGiftContinueViewGroup != null) {
                        mGiftContinueViewGroup.onHideInputView();
                    }
                    startInputAnimator(false);
                    return true;
                case WatchComponentController.MSG_BACKGROUND_CLICK:
                    if (mComponentController.onEvent(ComponentController.MSG_HIDE_INPUT_VIEW)) {
                        return true;
                    }
                    if (mIsGameMode && mIsLandscape) {
                        startGameAnimator();
                        return true;
                    }
                    break;
                default:
                    break;
            }
            return false;
        }
    }
}
