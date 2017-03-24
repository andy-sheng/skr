package com.wali.live.livesdk.live.livegame;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.wali.live.common.gift.view.GiftContinueViewGroup;
import com.wali.live.component.BaseSdkView;
import com.wali.live.component.ComponentController;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.component.presenter.OperatingPresenter;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.livegame.presenter.BottomButtonPresenter;
import com.wali.live.livesdk.live.livegame.presenter.PanelContainerPresenter;
import com.wali.live.livesdk.live.livegame.view.LiveBottomButton;
import com.wali.live.watchsdk.component.presenter.InputAreaPresenter;
import com.wali.live.watchsdk.component.presenter.LiveCommentPresenter;
import com.wali.live.watchsdk.component.view.InputAreaView;
import com.wali.live.watchsdk.component.view.LiveCommentView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 游戏直播页面
 */
public class LiveSdkView extends BaseSdkView<LiveComponentController> {
    private static final String TAG = "LiveSdkView";

    @NonNull
    protected final Action mAction = new Action();

    private final List<View> mHorizontalMoveSet = new ArrayList<>();

    @Nullable
    protected View mTopInfoView;
    @Nullable
    protected View mLiveCommentView;
    @Nullable
    protected GiftContinueViewGroup mGiftContinueViewGroup;

    protected boolean mIsLandscape = false;

    public LiveSdkView(
            @NonNull Activity activity,
            @NonNull LiveComponentController componentController) {
        super(activity, componentController);
    }

    @Override
    public void setupSdkView() {
        mGiftContinueViewGroup = $(R.id.gift_continue_vg); // 礼物
        mTopInfoView = $(R.id.live_top_info_view); // 顶部view

        // 弹幕区
        {
            LiveCommentView view = $(R.id.live_comment_view);
            if (view == null) {
                return;
            }
            LiveCommentPresenter presenter = new LiveCommentPresenter(mComponentController);
            addComponentView(view, presenter);
            view.setToken(mComponentController.mRoomChatMsgManager.toString());

            mLiveCommentView = view;
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

        // 底部面板
        {
            RelativeLayout relativeLayout = $(R.id.bottom_panel_view);
            if (relativeLayout == null) {
                MyLog.e(TAG, "missing R.id.bottom_panel_view");
                return;
            }
            PanelContainerPresenter presenter = new PanelContainerPresenter(
                    mComponentController, mComponentController.mRoomChatMsgManager);
            presenter.setComponentView(relativeLayout);
            addComponentView(presenter);
        }

        // 底部按钮
        {
            RelativeLayout relativeLayout = $(R.id.bottom_button_view);
            if (relativeLayout == null) {
                MyLog.e(TAG, "missing R.id.bottom_button_view");
                return;
            }
            relativeLayout.setVisibility(View.VISIBLE);
            LiveBottomButton view = new LiveBottomButton(relativeLayout);
            BottomButtonPresenter presenter =
                    new BottomButtonPresenter(mComponentController, mComponentController.mGameLivePresenter);
            addComponentView(view, presenter);
        }

        // 运营位
        {
            RelativeLayout relativeLayout = $(R.id.operating_view);
            if (relativeLayout == null) {
                MyLog.e(TAG, "missing R.id.operating_view");
                return;
            }
            OperatingPresenter presenter = new OperatingPresenter(mComponentController);
            addComponentView(presenter);
        }

        addViewToSet(new int[]{
                R.id.live_top_info_view,
                R.id.bottom_button_view,
                R.id.live_comment_view,
                R.id.gift_animation_player_view,
                R.id.gift_continue_vg,
                R.id.gift_room_effect_view,
                R.id.operating_view
        }, mHorizontalMoveSet);

        // 滑动
        {
//            View view = $(R.id.touch_view);
//            if (view == null) {
//                return;
//            }
//            TouchPresenter presenter = new TouchPresenter(mComponentController, view);
//            addComponentView(presenter);
//            presenter.setViewSet(mHorizontalMoveSet);
        }

        mAction.registerAction(); // 最后注册该Action，任何事件mAction都最后收到
        mComponentController.onEvent(LiveComponentController.MSG_SHOW_BARRAGE_SWITCH);
    }

    public class Action extends BaseSdkView.Action {

        @Override
        protected void startInputAnimator(boolean inputShow) {
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
                    setAlpha(mTopInfoView, value);
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (mInputShow) {
                        if (mIsLandscape) {
                            setVisibility(mLiveCommentView, View.GONE);
                        }
                    } else {
                        setVisibility(mTopInfoView, View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mInputShow) {
                        setVisibility(mTopInfoView, View.GONE);
                    } else {
                        setAlpha(mTopInfoView, 1.0f);
                        if (mIsLandscape) {
                            setVisibility(mLiveCommentView, View.VISIBLE);
                        }
                    }
                }
            });
            valueAnimator.start();
            mInputAnimatorRef = new WeakReference<>(valueAnimator);
        }

        @Override
        protected void stopAllAnimator() {
            ValueAnimator valueAnimator = deRef(mInputAnimatorRef);
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
        }

        @Override
        public void clearAnimation() {
            stopAllAnimator();
            mInputAnimatorRef = null;
        }

        @Override
        public boolean onAction(int source, @Nullable ComponentPresenter.Params params) {
            switch (source) {
                case LiveComponentController.MSG_ON_ORIENT_PORTRAIT:
                    mIsLandscape = false;
                    stopAllAnimator();
                    return true;
                case LiveComponentController.MSG_ON_ORIENT_LANDSCAPE:
                    mIsLandscape = true;
                    stopAllAnimator();
                    return true;
                case LiveComponentController.MSG_INPUT_VIEW_SHOWED:
                    if (!mIsLandscape) {
                        mComponentController.onEvent(ComponentController.MSG_DISABLE_MOVE_VIEW);
                    }
                    if (mGiftContinueViewGroup != null) {
                        mGiftContinueViewGroup.onShowInputView();
                    }
                    return true;
                case LiveComponentController.MSG_INPUT_VIEW_HIDDEN:
                    if (!mIsLandscape) { // 游戏直播横屏不需左右滑
                        mComponentController.onEvent(ComponentController.MSG_ENABLE_MOVE_VIEW);
                    }
                    if (mGiftContinueViewGroup != null) {
                        mGiftContinueViewGroup.onHideInputView();
                    }
                    return true;
                case LiveComponentController.MSG_BACKGROUND_CLICK:
                    if (mComponentController.onEvent(ComponentController.MSG_HIDE_INPUT_VIEW)) {
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
