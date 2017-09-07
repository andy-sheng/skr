package com.wali.live.livesdk.live.liveshow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.base.activity.BaseActivity;
import com.base.log.MyLog;
import com.thornbirds.component.IParams;
import com.wali.live.common.gift.view.GiftContinueViewGroup;
import com.wali.live.component.BaseSdkView;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.liveshow.presenter.BottomButtonPresenter;
import com.wali.live.livesdk.live.liveshow.presenter.FloatContainerPresenter;
import com.wali.live.livesdk.live.liveshow.presenter.LiveDisplayPresenter;
import com.wali.live.livesdk.live.liveshow.presenter.PanelContainerPresenter;
import com.wali.live.livesdk.live.liveshow.presenter.button.MagicControlBtnPresenter;
import com.wali.live.livesdk.live.liveshow.presenter.button.PlusControlBtnPresenter;
import com.wali.live.livesdk.live.liveshow.view.LiveBottomButton;
import com.wali.live.livesdk.live.liveshow.view.LiveDisplayView;
import com.wali.live.livesdk.live.liveshow.view.button.MagicControlBtnView;
import com.wali.live.livesdk.live.liveshow.view.button.PlusControlBtnView;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;
import com.wali.live.watchsdk.component.presenter.EnvelopePresenter;
import com.wali.live.watchsdk.component.presenter.InputAreaPresenter;
import com.wali.live.watchsdk.component.presenter.LiveCommentPresenter;
import com.wali.live.watchsdk.component.presenter.TopAreaPresenter;
import com.wali.live.watchsdk.component.presenter.WidgetPresenter;
import com.wali.live.watchsdk.component.view.InputAreaView;
import com.wali.live.watchsdk.component.view.LiveCommentView;
import com.wali.live.watchsdk.component.view.TopAreaView;
import com.wali.live.watchsdk.component.view.WidgetView;
import com.wali.live.watchsdk.envelope.SendEnvelopeFragment;

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
import static com.wali.live.component.BaseSdkController.MSG_SHOW_SEND_ENVELOPE;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 秀场直播页面
 */
public class ShowLiveSdkView extends BaseSdkView<View, ShowLiveController> {

    private final List<View> mHorizontalMoveSet = new ArrayList<>();

    protected final AnimationHelper mAnimationHelper = new AnimationHelper();

    @Nullable
    protected TopAreaView mTopAreaView;
    @Nullable
    protected View mLiveCommentView;
    @Nullable
    protected GiftContinueViewGroup mGiftContinueViewGroup;
    @Nullable
    protected RelativeLayout mFloatContainer;

    protected boolean mIsLandscape = false;

    @Override
    protected String getTAG() {
        return "GameLiveSdkView";
    }

    public ShowLiveSdkView(
            @NonNull Activity activity,
            @NonNull ShowLiveController controller) {
        super(activity, (ViewGroup) activity.findViewById(android.R.id.content), controller);
        mContentView = $(mParentView, R.id.main_act_container);
        addMissingView();
    }

    private void addMissingView() {
        // 画面
        LiveDisplayView view = new LiveDisplayView(mContentView.getContext());
        LiveDisplayPresenter presenter = new LiveDisplayPresenter(mController);
        registerComponent(view, presenter);
        // add view to activity
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addViewUnderAnchor(view, layoutParams, $(R.id.top_area_view));

        // FloatContainer，放在BottomButtonView的下方，目前用来放氛围面板：FloatAtmospherePanel
        mFloatContainer = new RelativeLayout(mContentView.getContext());
        layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addViewUnderAnchor(mFloatContainer, layoutParams, $(R.id.bottom_button_view));
    }

    @Override
    public void setupView() {
        mGiftContinueViewGroup = $(R.id.gift_continue_vg); // 礼物

        //顶部view
        {
            mTopAreaView = $(R.id.top_area_view);
            if (mTopAreaView == null) {
                return;
            }
            mTopAreaView.setVisibility(View.VISIBLE);
            TopAreaPresenter presenter = new TopAreaPresenter(mController,
                    mController.mMyRoomData, true);
            registerComponent(mTopAreaView, presenter);
        }

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
                    mController, mController.mStreamerPresenter,
                    mController.mMyRoomData);
            registerHybridComponent(presenter, relativeLayout);
        }
        // 音效面板
        {
            FloatContainerPresenter presenter = new FloatContainerPresenter(
                    mController, mController.mStreamerPresenter);
            registerHybridComponent(presenter, mFloatContainer);
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
            BottomButtonPresenter presenter = new BottomButtonPresenter(
                    mController, mController.mMyRoomData);
            registerComponent(view, presenter);
            // 直播加按钮
            {
                PlusControlBtnView btnView = $(R.id.plus_btn);
                if (btnView == null) {
                    MyLog.e(TAG, "missing R.id.plus_btn");
                    return;
                }
                PlusControlBtnPresenter btnPresenter = new PlusControlBtnPresenter(
                        mController, mActivity.getApplicationContext());
                registerComponent(btnView, btnPresenter);
            }
            // 美妆按钮
            {
                MagicControlBtnView btnView = $(R.id.magic_btn);
                if (btnView == null) {
                    MyLog.e(TAG, "missing R.id.magic_btn");
                    return;
                }
                MagicControlBtnPresenter btnPresenter =
                        new MagicControlBtnPresenter(mController);
                registerComponent(btnView, btnPresenter);
            }
        }
        // 抢红包
        {
            RelativeLayout relativeLayout = $(com.wali.live.watchsdk.R.id.envelope_view);
            if (relativeLayout == null) {
                MyLog.e(TAG, "missing R.id.envelope_view");
                return;
            }
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
                R.id.top_area_view,
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
        registerAction(MSG_SHOW_SEND_ENVELOPE);
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
                mAnimationHelper.stopAllAnimator();
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                mIsLandscape = true;
                mAnimationHelper.stopAllAnimator();
                return true;
            case MSG_INPUT_VIEW_SHOWED:
                if (!mIsLandscape) {
                    mController.postEvent(MSG_DISABLE_MOVE_VIEW);
                }
                if (mGiftContinueViewGroup != null) {
                    mGiftContinueViewGroup.onShowInputView();
                }
                mAnimationHelper.startInputAnimator(true);
                return true;
            case MSG_INPUT_VIEW_HIDDEN:
                if (!mIsLandscape) { // 游戏直播横屏不需左右滑
                    mController.postEvent(MSG_ENABLE_MOVE_VIEW);
                }
                if (mGiftContinueViewGroup != null) {
                    mGiftContinueViewGroup.onHideInputView();
                }
                mAnimationHelper.startInputAnimator(false);
                return true;
            case MSG_BACKGROUND_CLICK:
                if (mController.postEvent(MSG_HIDE_INPUT_VIEW)) {
                    return true;
                }
                break;
            case MSG_SHOW_SEND_ENVELOPE:
                SendEnvelopeFragment.openFragment((BaseActivity) mActivity,
                        mController.mMyRoomData);
                return true;
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
                    mTopAreaView.setAlpha(value);
                }
            }, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (mInputShow) {
                        if (mIsLandscape) {
                            mLiveCommentView.setVisibility(View.GONE);
                        }
                    } else {
                        mTopAreaView.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mInputShow) {
                        mTopAreaView.setVisibility(View.GONE);
                    } else {
                        mTopAreaView.setAlpha(1.0f);
                        if (mLiveCommentView.getVisibility() != View.VISIBLE) {
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
