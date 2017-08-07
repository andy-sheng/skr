package com.wali.live.livesdk.live.livegame;

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
import com.thornbirds.component.IParams;
import com.wali.live.common.gift.view.GiftContinueViewGroup;
import com.wali.live.component.BaseSdkView;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.livegame.presenter.BottomButtonPresenter;
import com.wali.live.livesdk.live.livegame.presenter.PanelContainerPresenter;
import com.wali.live.livesdk.live.livegame.view.LiveBottomButton;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;
import com.wali.live.watchsdk.component.presenter.EnvelopePresenter;
import com.wali.live.watchsdk.component.presenter.InputAreaPresenter;
import com.wali.live.watchsdk.component.presenter.LiveCommentPresenter;
import com.wali.live.watchsdk.component.presenter.WidgetPresenter;
import com.wali.live.watchsdk.component.view.InputAreaView;
import com.wali.live.watchsdk.component.view.LiveCommentView;
import com.wali.live.watchsdk.component.view.WidgetView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.wali.live.component.BaseSdkController.MSG_BACKGROUND_CLICK;
import static com.wali.live.component.BaseSdkController.MSG_DISABLE_MOVE_VIEW;
import static com.wali.live.component.BaseSdkController.MSG_ENABLE_MOVE_VIEW;
import static com.wali.live.component.BaseSdkController.MSG_HIDE_INPUT_VIEW;
import static com.wali.live.component.BaseSdkController.MSG_INPUT_VIEW_HIDDEN;
import static com.wali.live.component.BaseSdkController.MSG_INPUT_VIEW_SHOWED;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_BARRAGE_SWITCH;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 游戏直播页面
 */
public class LiveSdkView extends BaseSdkView<View, LiveComponentController> {

    private final List<View> mHorizontalMoveSet = new ArrayList<>();

    protected final AnimationHelper mAnimationHelper = new AnimationHelper();

    @Nullable
    protected View mTopInfoView;
    @Nullable
    protected View mLiveCommentView;
    @Nullable
    protected GiftContinueViewGroup mGiftContinueViewGroup;

    protected boolean mIsLandscape = false;

    @Override
    protected String getTAG() {
        return "LiveSdkView";
    }

    public LiveSdkView(@NonNull Activity activity, @NonNull LiveComponentController controller) {
        super(activity, (ViewGroup) activity.findViewById(android.R.id.content), controller);
    }

    @Override
    public void setupView() {
        mContentView = $(mParentView, R.id.main_act_container);
        mGiftContinueViewGroup = $(R.id.gift_continue_vg); // 礼物
        mTopInfoView = $(R.id.live_top_info_view); // 顶部view
        // 弹幕区
        {
            LiveCommentView view = $(R.id.live_comment_view);
            if (view == null) {
                return;
            }
            LiveCommentPresenter presenter = new LiveCommentPresenter(mController);
            registerComponent(view, presenter);
            view.setToken(mController.mRoomChatMsgManager.toString());

            mLiveCommentView = view;
        }
        // 输入框
        {
            InputAreaView view = $(R.id.input_area_view);
            if (view == null) {
                return;
            }
            InputAreaPresenter presenter = new InputAreaPresenter(
                    mController, mController.mMyRoomData, false);
            registerComponent(view, presenter);
        }
        // 底部面板
        {
            RelativeLayout relativeLayout = $(R.id.bottom_panel_view);
            if (relativeLayout == null) {
                MyLog.e(TAG, "missing R.id.bottom_panel_view");
                return;
            }
            PanelContainerPresenter presenter = new PanelContainerPresenter(
                    mController, mController.mRoomChatMsgManager,
                    mController.mMyRoomData);
            registerHybridComponent(presenter, relativeLayout);
        }
        // 底部按钮
        {
            RelativeLayout relativeLayout = $(R.id.bottom_button_view);
            if (relativeLayout == null) {
                MyLog.e(TAG, "missing R.id.bottom_button_view");
                return;
            }
            relativeLayout.setVisibility(View.VISIBLE);
            LiveBottomButton view = new LiveBottomButton(relativeLayout,
                    mController.mMyRoomData.getEnableShare());
            BottomButtonPresenter presenter = new BottomButtonPresenter(mController,
                    mController.mMyRoomData, mController.mGameLivePresenter);
            registerComponent(view, presenter);
        }
        // 抢红包
        {
            RelativeLayout relativeLayout = $(com.wali.live.watchsdk.R.id.envelope_view);
            EnvelopePresenter presenter = new EnvelopePresenter(mController, mController.mMyRoomData);
            registerHybridComponent(presenter, relativeLayout);
        }
        // 运营位
        {
            WidgetView view = $(R.id.widget_view);
            if (view == null) {
                MyLog.e(TAG, "missing R.id.widget_view");
                return;
            }
            WidgetPresenter presenter = new WidgetPresenter(mController, mController.mMyRoomData, true);
            registerComponent(view, presenter);
            ((BaseComponentSdkActivity) mActivity).addPushProcessor(presenter);
        }

        addViewToSet(new int[]{
                R.id.live_top_info_view,
                R.id.bottom_button_view,
                R.id.live_comment_view,
                R.id.gift_animation_player_view,
                R.id.gift_continue_vg,
                R.id.gift_room_effect_view,
                R.id.widget_view
        }, mHorizontalMoveSet);
        // 滑动
        {
//            View view = $(R.id.touch_view);
//            if (view == null) {
//                return;
//            }
//            TouchPresenter presenter = new TouchPresenter(mController, view);
//            registerComponent(presenter);
//            presenter.setViewSet(mHorizontalMoveSet);
        }
    }

    @Override
    public void startView() {
        super.startView();
        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_INPUT_VIEW_SHOWED);
        registerAction(MSG_INPUT_VIEW_HIDDEN);
        registerAction(MSG_BACKGROUND_CLICK);
        mController.postEvent(MSG_SHOW_BARRAGE_SWITCH);
    }

    @Override
    public void stopView() {
        super.stopView();
        mAnimationHelper.clearAnimation();
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                mIsLandscape = false;
                mController.mGameLivePresenter.onOrientation(false);
                mAnimationHelper.stopAllAnimator();
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                mIsLandscape = true;
                mController.mGameLivePresenter.onOrientation(true);
                mAnimationHelper.stopAllAnimator();
                return true;
            case MSG_INPUT_VIEW_SHOWED:
                if (!mIsLandscape) {
                    mController.postEvent(MSG_DISABLE_MOVE_VIEW);
                }
                if (mGiftContinueViewGroup != null) {
                    mGiftContinueViewGroup.onShowInputView();
                }
                return true;
            case MSG_INPUT_VIEW_HIDDEN:
                if (!mIsLandscape) { // 游戏直播横屏不需左右滑
                    mController.postEvent(MSG_ENABLE_MOVE_VIEW);
                }
                if (mGiftContinueViewGroup != null) {
                    mGiftContinueViewGroup.onHideInputView();
                }
                return true;
            case MSG_BACKGROUND_CLICK:
                if (mController.postEvent(MSG_HIDE_INPUT_VIEW)) {
                    return true;
                }
                break;
            default:
                break;
        }
        return false;
    }

    public class AnimationHelper extends BaseSdkView.AnimationHelper {

        protected WeakReference<ValueAnimator> mInputAnimatorRef; // 输入框弹起和收起时，隐藏和显示View动画
        protected boolean mInputShow = false;

        protected void startInputAnimator(boolean inputShow) {
            if (mInputShow == inputShow) {
                return;
            }
            mInputShow = inputShow;
            if (startRefAnimator(mInputAnimatorRef)) {
                return;
            }
            ValueAnimator valueAnimator = startNewAnimator(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    if (mInputShow) {
                        value = 1.0f - value;
                    }
                    mTopInfoView.setAlpha(value);
                }
            }, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (mInputShow) {
                        if (mIsLandscape) {
                            mLiveCommentView.setVisibility(View.GONE);
                        }
                    } else {
                        mTopInfoView.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mInputShow) {
                        mTopInfoView.setVisibility(View.GONE);
                    } else {
                        mTopInfoView.setAlpha(1.0f);
                        if (mIsLandscape) {
                            mLiveCommentView.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
            mInputAnimatorRef = new WeakReference<>(valueAnimator);
        }

        @Override
        protected void stopAllAnimator() {
            stopRefAnimator(mInputAnimatorRef);
        }

        @Override
        public void clearAnimation() {
            stopAllAnimator();
            mInputAnimatorRef = null;
        }
    }
}
