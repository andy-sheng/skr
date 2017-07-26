package com.wali.live.watchsdk.component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.base.utils.Constants;
import com.base.utils.display.DisplayUtils;
import com.wali.live.common.gift.view.GiftContinueViewGroup;
import com.wali.live.component.BaseSdkView;
import com.wali.live.component.ComponentController;
import com.wali.live.component.presenter.ComponentPresenter;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;
import com.wali.live.watchsdk.component.presenter.BottomButtonPresenter;
import com.wali.live.watchsdk.component.presenter.FollowGuidePresenter;
import com.wali.live.watchsdk.component.presenter.GameBarragePresenter;
import com.wali.live.watchsdk.component.presenter.GameDownloadPresenter;
import com.wali.live.watchsdk.component.presenter.GameInputPresenter;
import com.wali.live.watchsdk.component.presenter.ImagePagerPresenter;
import com.wali.live.watchsdk.component.presenter.InputAreaPresenter;
import com.wali.live.watchsdk.component.presenter.LiveCommentPresenter;
import com.wali.live.watchsdk.component.presenter.TouchPresenter;
import com.wali.live.watchsdk.component.presenter.WidgetPresenter;
import com.wali.live.watchsdk.component.view.FollowGuideView;
import com.wali.live.watchsdk.component.view.GameBarrageView;
import com.wali.live.watchsdk.component.view.GameInputView;
import com.wali.live.watchsdk.component.view.ImagePagerView;
import com.wali.live.watchsdk.component.view.InputAreaView;
import com.wali.live.watchsdk.component.view.LiveCommentView;
import com.wali.live.watchsdk.component.view.WatchBottomButton;
import com.wali.live.watchsdk.component.view.WidgetView;
import com.wali.live.watchsdk.component.view.panel.GameDownloadPanel;
import com.wali.live.watchsdk.watch.presenter.PanelContainerPresenter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 游戏直播页面
 */
public class WatchSdkView extends BaseSdkView<WatchComponentController> {

    @NonNull
    protected final Action mAction = new Action();

    private final List<View> mHorizontalMoveSet = new ArrayList();
    private final List<View> mVerticalMoveSet = new ArrayList(0);
    private final List<View> mGameHideSet = new ArrayList(0);

    @Nullable
    protected View mTopInfoView;
    @Nullable
    protected LiveCommentView mLiveCommentView;
    @Nullable
    protected GiftContinueViewGroup mGiftContinueViewGroup;

    // 关注弹窗
    protected FollowGuideView mFollowGuideView;
    protected FollowGuidePresenter mFollowGuidePresenter;

    protected WatchBottomButton mWatchBottomButton;

    protected GameDownloadPresenter mGameDownloadPresenter;

    protected ImagePagerView mPagerView;

    protected boolean mIsGameMode = false;
    protected boolean mIsLandscape = false;

    @Override
    protected String getTAG() {
        return "WatchSdkView";
    }

    public WatchSdkView(
            @NonNull Activity activity,
            @NonNull WatchComponentController componentController) {
        super(activity, componentController);
    }

    @Override
    public void releaseSdkView() {
        super.releaseSdkView();
        mAction.clearAnimation();
    }

    public void setupSdkView(boolean isGameMode) {
        mIsGameMode = isGameMode;
        if (mIsGameMode) {
            // 游戏直播横屏输入框
            {
                GameInputView view = new GameInputView(mActivity);
                view.setId(R.id.game_input_view);
                view.setVisibility(View.GONE);
                GameInputPresenter presenter = new GameInputPresenter(mComponentController, mComponentController.mMyRoomData);
                addComponentView(view, presenter);

                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                addViewAboveAnchor(view, layoutParams, $(R.id.input_area_view));
            }
            // 游戏直播横屏弹幕
            {
                GameBarrageView view = new GameBarrageView(mActivity);
                view.setId(R.id.game_barrage_view);
                view.setVisibility(View.GONE);
                GameBarragePresenter presenter = new GameBarragePresenter(mComponentController);
                addComponentView(view, presenter);

                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtils.dip2px(96.77f));
                layoutParams.bottomMargin = DisplayUtils.dip2px(56f);
                layoutParams.rightMargin = DisplayUtils.dip2px(56f);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                addViewAboveAnchor(view, layoutParams, $(R.id.comment_rv));
            }

            setupGameSdkView();
        }
        setupSdkView();
    }

    private void setupGameSdkView() {
        if (mGameDownloadPresenter == null) {
            GameDownloadPanel view = new GameDownloadPanel((RelativeLayout) $(R.id.main_act_container));
            mGameDownloadPresenter = new GameDownloadPresenter(mComponentController, mComponentController.mMyRoomData);
            addComponentView(view, mGameDownloadPresenter);
        }
    }

    @Override
    public void setupSdkView() {
        mGiftContinueViewGroup = $(R.id.gift_continue_vg);  // 礼物
        mTopInfoView = $(R.id.watch_top_info_view);         // 顶部view

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

        // 底部面板
        {
            RelativeLayout relativeLayout = $(R.id.bottom_panel_view);
            if (relativeLayout == null) {
                MyLog.e(TAG, "missing R.id.bottom_panel_view");
                return;
            }
            PanelContainerPresenter presenter = new PanelContainerPresenter(mComponentController, mComponentController.mMyRoomData);
            presenter.setComponentView(relativeLayout);
            addComponentView(presenter);
        }

        // 输入框
        {
            InputAreaView view = $(R.id.input_area_view);
            if (view == null) {
                return;
            }
            InputAreaPresenter presenter = new InputAreaPresenter(mComponentController, mComponentController.mMyRoomData);
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
            mWatchBottomButton = new WatchBottomButton(relativeLayout, mIsGameMode,
                    mComponentController.mMyRoomData.getEnableShare());
            BottomButtonPresenter presenter =
                    new BottomButtonPresenter(mComponentController, mComponentController.mMyRoomData);
            addComponentView(mWatchBottomButton, presenter);
        }

        if (!Constants.isGooglePlayBuild && !Constants.isIndiaBuild) {
            // 运营位
            {
                WidgetView view = $(R.id.widget_view);
                if (view == null) {
                    MyLog.e(TAG, "missing R.id.widget_view");
                    return;
                }
                WidgetPresenter presenter = new WidgetPresenter(mComponentController, mComponentController.mMyRoomData, false);
                addComponentView(view, presenter);
                ((BaseComponentSdkActivity) mActivity).addPushProcessor(presenter);
            }
        }

        if (mComponentController.mRoomInfoList != null && mComponentController.mRoomInfoList.size() > 1) {
            {
                mPagerView = new ImagePagerView(mActivity);
                mPagerView.setVerticalList(mComponentController.mRoomInfoList, mComponentController.mRoomInfoPosition);

                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                addViewAboveAnchor(mPagerView, layoutParams, $(R.id.mask_iv));

                ImagePagerPresenter presenter = new ImagePagerPresenter(mComponentController);
                addComponentView(mPagerView, presenter);
            }
        }

        addViewToSet(new int[]{
                R.id.watch_top_info_view,
                R.id.bottom_button_view,
                R.id.live_comment_view,
                R.id.gift_animation_player_view,
                R.id.gift_continue_vg,
                R.id.gift_room_effect_view,
                R.id.widget_view,
        }, mHorizontalMoveSet);

        addViewToSet(new int[]{
                R.id.watch_top_info_view,
                R.id.bottom_button_view,
                R.id.live_comment_view,
                R.id.gift_animation_player_view,
                R.id.gift_continue_vg,
                R.id.gift_room_effect_view,
                R.id.widget_view,
                R.id.mask_iv,
                R.id.rotate_btn,
                R.id.close_btn,
        }, mVerticalMoveSet);

        if (mIsGameMode) {
            addViewToSet(new int[]{
                    R.id.watch_top_info_view,
                    R.id.bottom_button_view,
                    R.id.game_barrage_view,
                    R.id.game_input_view,
                    R.id.close_btn,
                    R.id.widget_view
            }, mGameHideSet);
        }

        // 滑动
        {
            View view = $(R.id.touch_view);
            if (view == null) {
                return;
            }
            TouchPresenter presenter = new TouchPresenter(mComponentController, view);
            addComponentView(presenter);
            presenter.setViewSet(mHorizontalMoveSet, mVerticalMoveSet, mIsGameMode);

            // 增加上线滑动的判断
            if (mComponentController.mRoomInfoList != null && mComponentController.mRoomInfoList.size() > 1) {
                // 打开上下滑动的开关
                presenter.setVerticalMoveEnabled(new View[]{$(R.id.last_dv), $(R.id.center_dv), $(R.id.next_dv)}, new View[]{$(R.id.video_view)});
            }
        }
        mAction.registerAction(); // 最后注册该Action，任何事件mAction都最后收到
    }

    public void switchToNextRoom() {
        if (mPagerView != null) {
            mPagerView.switchNext(mComponentController.mRoomInfoPosition);
        }
    }

    public void switchToLastRoom() {
        if (mPagerView != null) {
            mPagerView.switchLast(mComponentController.mRoomInfoPosition);
        }
    }

    public void reset() {
        if (mFollowGuidePresenter != null) {
            mFollowGuidePresenter.reset();
        }
        if (mGameDownloadPresenter != null) {
            mGameDownloadPresenter.reset();
        }

        mLiveCommentView.reset();
        mWatchBottomButton.reset();
    }

    public void postSwitch(boolean isGameMode) {
        mIsGameMode = isGameMode;
        if (mIsGameMode) {
            setupGameSdkView();
        }

        mWatchBottomButton.postSwitch(mIsGameMode);
    }

    public void postPrepare() {
        if (mPagerView != null) {
            MyLog.d(TAG, "postPrepare");
            mPagerView.postPrepare();
        }
    }

    public class Action extends BaseSdkView.Action {
        @Override
        public void registerAction() {
            super.registerAction();
            mComponentController.registerAction(WatchComponentController.MSG_SHOW_FOLLOW_GUIDE, this);
            mComponentController.registerAction(WatchComponentController.MSG_FOLLOW_COUNT_DOWN, this);
            mComponentController.registerAction(WatchComponentController.MSG_PAGE_DOWN, this);
            mComponentController.registerAction(WatchComponentController.MSG_PAGE_UP, this);
        }

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
                        if (mIsLandscape && !mIsGameMode) {
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
        private boolean mGameHide = false;

        /**
         * 观看游戏直播横屏时，点击隐藏显示View
         */
        private void startGameAnimator() {
            mGameHide = !mGameHide;
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
                    if (mGameHide) {
                        value = 1.0f - value;
                    }
                    for (View view : mGameHideSet) {
                        if (view != null) {
                            setAlpha(view, value);
                        }
                    }
                }
            });
            valueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (!mGameHide) {
                        for (View view : mGameHideSet) {
                            if (view != null) {
                                setAlpha(view, 0.0f);
                                setVisibility(view, View.VISIBLE);
                            }
                        }
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mGameHide) {
                        for (View view : mGameHideSet) {
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

        @Override
        protected void stopAllAnimator() {
            ValueAnimator valueAnimator = deRef(mInputAnimatorRef);
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
            valueAnimator = deRef(mGameAnimatorRef);
            if (valueAnimator != null) {
                valueAnimator.cancel();
            }
        }

        @Override
        public void clearAnimation() {
            stopAllAnimator();
            mInputAnimatorRef = null;
            mGameAnimatorRef = null;
        }

        @Override
        public boolean onAction(int source, @Nullable ComponentPresenter.Params params) {
            switch (source) {
                case ComponentController.MSG_ON_ORIENT_PORTRAIT:
                    mIsLandscape = false;
                    stopAllAnimator();
                    if (mIsGameMode) {
                        mComponentController.onEvent(ComponentController.MSG_ENABLE_MOVE_VIEW);
                        if (mGameHide) { // 横屏转竖屏，恢复被隐藏的View，竖屏转横屏的逻辑在TouchPresenter中处理
                            mGameHide = false;
                            for (View view : mGameHideSet) {
                                if (view != null && view.getVisibility() != View.VISIBLE) {
                                    view.setVisibility(View.VISIBLE);
                                }
                            }
                        }
                        setVisibility(mLiveCommentView, View.VISIBLE);
                        mComponentController.onEvent(ComponentController.MSG_HIDE_GAME_INPUT);
                    }
                    return true;
                case ComponentController.MSG_ON_ORIENT_LANDSCAPE:
                    mIsLandscape = true;
                    stopAllAnimator();
                    if (mIsGameMode) { // 游戏直播横屏不需左右滑
                        mComponentController.onEvent(ComponentController.MSG_DISABLE_MOVE_VIEW);
                        setVisibility(mLiveCommentView, View.INVISIBLE);
                        mComponentController.onEvent(ComponentController.MSG_SHOW_GAME_INPUT);
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
                case WatchComponentController.MSG_FOLLOW_COUNT_DOWN:
                    if (mFollowGuidePresenter != null) {
                        return false;
                    }
                    int countTs = params.getItem(0);
                    mFollowGuidePresenter = new FollowGuidePresenter(mComponentController,
                            mComponentController.mMyRoomData);
                    mFollowGuidePresenter.countDownOut(countTs);
                    break;
                case WatchComponentController.MSG_SHOW_FOLLOW_GUIDE:
                    if (mFollowGuidePresenter == null || mFollowGuideView != null) {
                        return false;
                    }
                    mFollowGuideView = new FollowGuideView(mActivity);
                    mFollowGuideView.setVisibility(View.INVISIBLE);
                    mFollowGuidePresenter.setComponentView(mFollowGuideView.getViewProxy());
                    mFollowGuideView.setPresenter(mFollowGuidePresenter);
                    addComponentView(mFollowGuideView, mFollowGuidePresenter);

                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

                    ViewGroup rootView = (ViewGroup) mActivity.findViewById(R.id.main_act_container);
                    rootView.addView(mFollowGuideView, layoutParams);

                    mFollowGuideView.post(new Runnable() {
                        @Override
                        public void run() {
                            mFollowGuideView.setMyRoomData(mComponentController.mMyRoomData);
                            mFollowGuideView.onOrientation(mIsLandscape);
                        }
                    });

                    // 出来关注，让关注一起移动
                    mVerticalMoveSet.add(mFollowGuideView);
                    break;
                default:
                    break;
            }
            return false;
        }
    }
}
