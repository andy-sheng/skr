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
import com.wali.live.watchsdk.component.view.GameBarrageView;
import com.wali.live.watchsdk.component.view.GameInputView;
import com.wali.live.watchsdk.component.view.InputAreaView;
import com.wali.live.watchsdk.component.view.LiveCommentView;
import com.wali.live.watchsdk.component.view.WatchBottomButton;
import com.wali.live.watchsdk.component.view.WatchPanelContainer;

import java.lang.ref.WeakReference;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 游戏直播页面
 */
public class WatchSdkView extends BaseSdkView<WatchComponentController> {
    private static final String TAG = "WatchSdkView";

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

        mComponentController.registerAction(WatchComponentController.MSG_ON_ORIENT_PORTRAIT, mAction);
        mComponentController.registerAction(WatchComponentController.MSG_ON_ORIENT_LANDSCAPE, mAction);
        mComponentController.registerAction(WatchComponentController.MSG_INPUT_VIEW_SHOWED, mAction);
        mComponentController.registerAction(WatchComponentController.MSG_INPUT_VIEW_HIDDEN, mAction);
    }

    public class Action implements ComponentPresenter.IAction {

        private WeakReference<ValueAnimator> mInputAnimatorRef; // 输入框弹起时，隐藏
        private boolean mInputShow = false;

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

        /**
         * 输入框显示时，隐藏弹幕区和头部区
         * 弹幕区只在横屏下才需要显示和隐藏，直接修改visibility，在显示动画开始时显示，在消失动画结束时消失。
         */
        private void startInputAnimator(boolean inputShow) {
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

        public void clearAnimation() {
            ValueAnimator valueAnimator = deRef(mInputAnimatorRef);
            if (valueAnimator != null) {
                valueAnimator.cancel();
                mInputAnimatorRef = null;
            }
        }

        @Override
        public boolean onAction(int source, @Nullable ComponentPresenter.Params params) {
            switch (source) {
                case ComponentController.MSG_ON_ORIENT_PORTRAIT:
                    mIsLandscape = false;
                    return true;
                case ComponentController.MSG_ON_ORIENT_LANDSCAPE:
                    mIsLandscape = true;
                    return true;
                case WatchComponentController.MSG_INPUT_VIEW_SHOWED:
                    if (mGiftContinueViewGroup != null) {
                        mGiftContinueViewGroup.onShowInputView();
                    }
                    startInputAnimator(true);
                    return true;
                case WatchComponentController.MSG_INPUT_VIEW_HIDDEN:
                    if (mGiftContinueViewGroup != null) {
                        mGiftContinueViewGroup.onHideInputView();
                    }
                    startInputAnimator(false);
                    return true;
                default:
                    break;
            }
            return false;
        }
    }
}
