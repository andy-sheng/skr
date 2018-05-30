package com.wali.live.watchsdk.component;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.base.activity.BaseActivity;
import com.base.log.MyLog;
import com.base.utils.Constants;
import com.base.utils.display.DisplayUtils;
import com.mi.live.data.cache.RoomInfoGlobalCache;
import com.thornbirds.component.IParams;
import com.wali.live.common.gift.view.GiftContinueViewGroup;
import com.wali.live.component.BaseSdkView;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;
import com.wali.live.watchsdk.component.presenter.BarrageBtnPresenter;
import com.wali.live.watchsdk.component.presenter.BarrageControlAnimPresenter;
import com.wali.live.watchsdk.component.presenter.BottomButtonPresenter;
import com.wali.live.watchsdk.component.presenter.EnvelopePresenter;
import com.wali.live.watchsdk.component.presenter.ExtraContainerPresenter;
import com.wali.live.watchsdk.component.presenter.FollowGuidePresenter;
import com.wali.live.watchsdk.component.presenter.GameBarragePresenter;
import com.wali.live.watchsdk.component.presenter.GameDownloadPresenter;
import com.wali.live.watchsdk.component.presenter.GameInputPresenter;
import com.wali.live.watchsdk.component.presenter.ImagePagerPresenter;
import com.wali.live.watchsdk.component.presenter.InputAreaPresenter;
import com.wali.live.watchsdk.component.presenter.LiveCommentPresenter;
import com.wali.live.watchsdk.component.presenter.TopAreaPresenter;
import com.wali.live.watchsdk.component.presenter.TouchPresenter;
import com.wali.live.watchsdk.component.presenter.WatchFloatPresenter;
import com.wali.live.watchsdk.component.presenter.WatchPlayerPresenter;
import com.wali.live.watchsdk.component.presenter.WidgetPresenter;
import com.wali.live.watchsdk.component.view.BarrageBtnView;
import com.wali.live.watchsdk.component.view.BarrageControlAnimView;
import com.wali.live.watchsdk.component.view.ExtraContainerView;
import com.wali.live.watchsdk.component.view.FollowGuideView;
import com.wali.live.watchsdk.component.view.GameBarrageView;
import com.wali.live.watchsdk.component.view.GameInputView;
import com.wali.live.watchsdk.component.view.ImagePagerView;
import com.wali.live.watchsdk.component.view.InputAreaView;
import com.wali.live.watchsdk.component.view.LiveCommentView;
import com.wali.live.watchsdk.component.view.TopAreaView;
import com.wali.live.watchsdk.component.view.WatchBottomButton;
import com.wali.live.watchsdk.component.view.WidgetView;
import com.wali.live.watchsdk.component.view.panel.GameDownloadPanel;
import com.wali.live.watchsdk.envelope.SendEnvelopeFragment;
import com.wali.live.watchsdk.watch.presenter.PanelContainerPresenter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import static com.wali.live.component.BaseSdkController.MSG_BACKGROUND_CLICK;
import static com.wali.live.component.BaseSdkController.MSG_DISABLE_MOVE_VIEW;
import static com.wali.live.component.BaseSdkController.MSG_ENABLE_MOVE_VIEW;
import static com.wali.live.component.BaseSdkController.MSG_FOLLOW_COUNT_DOWN;
import static com.wali.live.component.BaseSdkController.MSG_FORCE_ROTATE_SCREEN;
import static com.wali.live.component.BaseSdkController.MSG_HIDE_GAME_INPUT;
import static com.wali.live.component.BaseSdkController.MSG_HIDE_INPUT_VIEW;
import static com.wali.live.component.BaseSdkController.MSG_INPUT_VIEW_HIDDEN;
import static com.wali.live.component.BaseSdkController.MSG_INPUT_VIEW_SHOWED;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_FOLLOW_GUIDE;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_GAME_INPUT;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_SEND_ENVELOPE;
import static com.wali.live.component.BaseSdkController.MSG_VIDEO_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_VIDEO_PORTRAIT;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 游戏直播页面
 */
public class WatchSdkView extends BaseSdkView<View, WatchComponentController> implements View.OnClickListener {

    private final List<View> mHorizontalMoveSet = new ArrayList();
    private final List<View> mVerticalMoveSet = new ArrayList(0);
    private final List<View> mGameHideSet = new ArrayList(0);

    protected final AnimationHelper mAnimationHelper = new AnimationHelper();

    protected TopAreaView mTopAreaView;
    protected LiveCommentView mLiveCommentView;
    protected View mBarrageBtnView;
    protected BarrageControlAnimView mBarrageControlAnimView;

    protected GiftContinueViewGroup mGiftContinueViewGroup;

    // 关注弹窗
    protected FollowGuideView mFollowGuideView;
    protected FollowGuidePresenter mFollowGuidePresenter;

    protected ExtraContainerPresenter mExtraContainerPresenter;

    protected WatchBottomButton mWatchBottomButton;

    protected GameBarragePresenter mGameBarragePresenter;
    protected GameInputPresenter mGameInputPresenter;
    protected GameDownloadPresenter mGameDownloadPresenter;

    protected WidgetPresenter mWidgetPresenter;

    protected ImagePagerView mPagerView;

    protected ImageView mRotateBtn;

    protected boolean mIsGameMode = false;
    protected boolean mIsHuYaLive = false;
    protected boolean mIsLandscape = false;
    protected boolean mIsVideoLandscape = false;

    @Override
    protected String getTAG() {
        return "WatchSdkView";
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.rotate_btn) {
            mController.postEvent(MSG_FORCE_ROTATE_SCREEN);
        }
    }

    public WatchSdkView(
            @NonNull Activity activity,
            @NonNull WatchComponentController controller) {
        super(activity, (ViewGroup) activity.findViewById(android.R.id.content), controller);
    }

    public void setupView(boolean isGameMode, boolean isHuYaLive) {
        mIsGameMode = isGameMode;
        mIsHuYaLive = isHuYaLive;
        setupView();
        if (mIsGameMode) {
            setupGameView();
        }
    }

    private void setupGameView() {
        // 游戏直播横屏弹幕
        if (mGameBarragePresenter == null) {
            GameBarrageView view = new GameBarrageView(mActivity);
            view.setId(R.id.game_barrage_view);
            view.setVisibility(View.GONE);
            mGameBarragePresenter = new GameBarragePresenter(mController);
            registerComponent(view, mGameBarragePresenter);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, DisplayUtils.dip2px(96.77f));
            layoutParams.bottomMargin = DisplayUtils.dip2px(56f);
            layoutParams.rightMargin = DisplayUtils.dip2px(56f);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            addViewAboveAnchor(view, layoutParams, $(R.id.live_comment_view));
        } else {
            mGameBarragePresenter.startPresenter();
        }

        // 游戏直播横屏输入框
        if (mGameInputPresenter == null) {
            GameInputView view = new GameInputView(mActivity);
            view.setId(R.id.game_input_view);
            view.setVisibility(View.GONE);
            mGameInputPresenter = new GameInputPresenter(mController, mController.mMyRoomData, mController.mRoomChatMsgManager);
            registerComponent(view, mGameInputPresenter);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            addViewUnderAnchor(view, layoutParams, $(R.id.bottom_panel_view));
        } else {
            mGameInputPresenter.startPresenter();
        }

        // 游戏直播下载输入框
        if (mGameDownloadPresenter == null) {
            GameDownloadPanel view = new GameDownloadPanel((RelativeLayout) $(R.id.main_act_container));
            mGameDownloadPresenter = new GameDownloadPresenter(mController, mController.mMyRoomData);
            registerComponent(view, mGameDownloadPresenter);
        } else {
            mGameDownloadPresenter.startPresenter();
        }

        if (mGameHideSet.size() == 0) {
            addViewToSet(new int[]{
                    R.id.top_area_view,
                    R.id.bottom_button_view,
                    R.id.game_barrage_view,
                    R.id.game_input_view,
                    R.id.close_btn,
                    R.id.rotate_btn,
                    R.id.widget_view
            }, mGameHideSet);
        }
    }

    private void cancelGameView() {
        if (mGameBarragePresenter != null) {
            mGameBarragePresenter.stopPresenter();
        }
        if (mGameInputPresenter != null) {
            mGameInputPresenter.stopPresenter();
        }
        if (mGameDownloadPresenter != null) {
            mGameDownloadPresenter.stopPresenter();
        }
        mGameHideSet.clear();
    }

    @Override
    public void setupView() {
        mContentView = $(mParentView, R.id.main_act_container);
        mGiftContinueViewGroup = $(R.id.gift_continue_vg);  // 礼物
        // 播放器
        {
            TextureView view = $(R.id.video_view);
            if (view == null) {
                MyLog.e(TAG, "missing R.id.video_view");
                return;
            }
            WatchPlayerPresenter presenter = new WatchPlayerPresenter(mController, mController.mStreamerPresenter);
            registerHybridComponent(presenter, view);
        }
        //顶部view
        {
            mTopAreaView = $(R.id.top_area_view);
            if (mTopAreaView == null) {
                MyLog.e(TAG, "missing R.id.top_area_view");
                return;
            }
            mTopAreaView.isShowFollowBtn(mController.mMyRoomData.isEnableFollow());
            TopAreaPresenter topAreaPresenter = new TopAreaPresenter(mController,
                    mController.mMyRoomData, false);
            registerComponent(mTopAreaView, topAreaPresenter);
        }
        // 弹幕区
        {
            LiveCommentView view = $(R.id.live_comment_view);
            if (view == null) {
                MyLog.e(TAG, "missing R.id.live_comment_view");
                return;
            }
            LiveCommentPresenter presenter = new LiveCommentPresenter(mController);
            registerComponent(view, presenter);
            view.setToken(mController.mRoomChatMsgManager.toString());
            mLiveCommentView = view;
        }
        //弹幕区上面的特权弹幕动画展示
        {
            BarrageControlAnimView view = $(R.id.msg_anim_view);
            BarrageControlAnimPresenter presenter = new BarrageControlAnimPresenter(mController, mController.mMyRoomData);
            registerComponent(view, presenter);
            mBarrageControlAnimView = view;
        }
        // 底部面板
        {
            RelativeLayout relativeLayout = $(R.id.bottom_panel_view);
            if (relativeLayout == null) {
                MyLog.e(TAG, "missing R.id.bottom_panel_view");
                return;
            }
            PanelContainerPresenter presenter = new PanelContainerPresenter(mController,
                    mController.mMyRoomData);
            registerHybridComponent(presenter, relativeLayout);
        }
        // 悬浮面板容器，与底部面板类似，但是不会在显示新Panel时，隐藏之前显示的Panel
        {
            RelativeLayout relativeLayout = $(R.id.float_panel_view);
            if (relativeLayout == null) {
                MyLog.e(TAG, "missing R.id.float_panel_view");
                return;
            }
            WatchFloatPresenter presenter = new WatchFloatPresenter(mController, mController.mMyRoomData);
            registerHybridComponent(presenter, relativeLayout);
            ((BaseComponentSdkActivity) mActivity).addPushProcessor(presenter);
        }
        // 输入框
        {
            InputAreaView view = $(R.id.input_area_view);
            if (view == null) {
                MyLog.e(TAG, "missing R.id.input_area_view");
                return;
            }
            InputAreaPresenter presenter = new InputAreaPresenter(
                    mController, mController.mMyRoomData, mController.mRoomChatMsgManager, true);
            registerComponent(view, presenter);
        }
        //底部输入框
        {
            BarrageBtnView view = $(R.id.barrage_btn_view);
            if (view == null) {
                MyLog.e(TAG, "missing R.id.barrage_btn_view");
                return;
            }
            BarrageBtnPresenter presenter = new BarrageBtnPresenter(mController);
            registerComponent(view, presenter);
            mBarrageBtnView = view;
        }
        // 底部按钮
        {
            RelativeLayout relativeLayout = $(R.id.bottom_button_view);
            if (relativeLayout == null) {
                MyLog.e(TAG, "missing R.id.bottom_button_view");
                return;
            }
            relativeLayout.setVisibility(View.VISIBLE);
            mWatchBottomButton = new WatchBottomButton(relativeLayout, mIsGameMode, mIsHuYaLive);
            BottomButtonPresenter presenter = new BottomButtonPresenter(
                    mController, mController.mMyRoomData);
            registerComponent(mWatchBottomButton, presenter);
        }
        // 抢红包
        {
            RelativeLayout relativeLayout = $(R.id.envelope_view);
            EnvelopePresenter presenter = new EnvelopePresenter(mController, mController.mMyRoomData);
            registerHybridComponent(presenter, relativeLayout);
        }
        // 额外控件的容器
        {
            ExtraContainerView view = $(R.id.extra_container);
            mExtraContainerPresenter = new ExtraContainerPresenter(mController);
            registerComponent(view, mExtraContainerPresenter);
        }
        // 运营位
        if (!Constants.isGooglePlayBuild && !Constants.isIndiaBuild) {
            WidgetView view = $(R.id.widget_view);
            if (view == null) {
                MyLog.e(TAG, "missing R.id.widget_view");
                return;
            }
            mWidgetPresenter = new WidgetPresenter(mController, mController.mMyRoomData, false);
            registerComponent(view, mWidgetPresenter);
            ((BaseComponentSdkActivity) mActivity).addPushProcessor(mWidgetPresenter);
        }

        if (mController.mRoomInfoList != null && mController.mRoomInfoList.size() > 1) {
            mPagerView = new ImagePagerView(mActivity);
            mPagerView.setVerticalList(mController.mRoomInfoList, mController.mRoomInfoPosition);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            addViewAboveAnchor(mPagerView, layoutParams, $(R.id.mask_iv));

            ImagePagerPresenter presenter = new ImagePagerPresenter(mController);
            registerComponent(mPagerView, presenter);
        }

        mRotateBtn = $(R.id.rotate_btn);
        $click(mRotateBtn, this);

        addViewToSet(new int[]{
                R.id.top_area_view,
                R.id.bottom_button_view,
                R.id.live_comment_view,
                R.id.msg_anim_view,
                R.id.gift_animation_player_view,
                R.id.gift_continue_vg,
                R.id.gift_room_effect_view,
                R.id.widget_view,
                R.id.barrage_btn_view,
                R.id.close_btn,
                R.id.rotate_btn,
        }, mHorizontalMoveSet);
        addViewToSet(new int[]{
                R.id.top_area_view,
                R.id.bottom_button_view,
                R.id.live_comment_view,
                R.id.msg_anim_view,
                R.id.gift_animation_player_view,
                R.id.gift_continue_vg,
                R.id.gift_room_effect_view,
                R.id.widget_view,
                R.id.barrage_btn_view,
                R.id.mask_iv,
                R.id.close_btn,
                R.id.rotate_btn,
                R.id.extra_container,
                R.id.float_panel_view
        }, mVerticalMoveSet);

        // 滑动
        {
            View view = $(R.id.touch_view);
            if (view == null) {
                return;
            }
            TouchPresenter presenter = new TouchPresenter(mController, view);
            registerComponent(presenter);
            presenter.setViewSet(mHorizontalMoveSet, mVerticalMoveSet, mIsGameMode);
            // 增加上下滑动的判断
            if (mController.mRoomInfoList != null && mController.mRoomInfoList.size() > 1) {
                // 打开上下滑动的开关
                presenter.setVerticalMoveEnabled(new View[]{$(R.id.last_dv), $(R.id.center_dv), $(R.id.next_dv)},
                        new View[]{$(R.id.video_view)});
            }
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
        registerAction(MSG_SHOW_FOLLOW_GUIDE);
        registerAction(MSG_FOLLOW_COUNT_DOWN);
        registerAction(MSG_SHOW_SEND_ENVELOPE);
        registerAction(MSG_VIDEO_PORTRAIT);
        registerAction(MSG_VIDEO_LANDSCAPE);
    }

    @Override
    public void stopView() {
        super.stopView();
        mAnimationHelper.clearAnimation();
    }

    public void switchToNextRoom() {
        if (mPagerView != null) {
            mPagerView.switchNext(mController.mRoomInfoPosition);
        }
    }

    public void switchToLastRoom() {
        if (mPagerView != null) {
            mPagerView.switchLast(mController.mRoomInfoPosition);
        }
    }

    public void reset() {
        if (mFollowGuidePresenter != null) {
            mFollowGuidePresenter.reset();
        }
        if (mGameDownloadPresenter != null) {
            mGameDownloadPresenter.reset();
        }
        if (mWidgetPresenter != null) {
            mWidgetPresenter.reset();
        }
        mLiveCommentView.reset();
        mBarrageControlAnimView.reset();
        mWatchBottomButton.reset();
        mTopAreaView.reset();
    }

    public void postSwitch(boolean isGameMode) {
        if (mIsGameMode != isGameMode) {
            mIsGameMode = isGameMode;
            if (mIsGameMode) {
                setupGameView();
            } else {
                cancelGameView();
            }
        }
        MyLog.d(TAG, "liveType=" + mController.mMyRoomData.getLiveType() + "@" + mController.mMyRoomData.hashCode());
        mWatchBottomButton.postSwitch(mIsGameMode);
    }

    public void postPrepare() {
        if (mPagerView != null) {
            MyLog.d(TAG, "postPrepare");
            mPagerView.postPrepare();
        }
    }

    public void updateRotateBtn() {
        if (!mIsVideoLandscape && !mIsLandscape) {
            mRotateBtn.setVisibility(View.GONE);
        } else {
            mRotateBtn.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                mIsLandscape = false;
                mAnimationHelper.stopAllAnimator();
                if (mIsGameMode) {
                    mController.postEvent(MSG_ENABLE_MOVE_VIEW);
                    if (mAnimationHelper.mGameHide) { // 横屏转竖屏，恢复被隐藏的View，竖屏转横屏的逻辑在TouchPresenter中处理
                        mAnimationHelper.mGameHide = false;
                        for (View view : mGameHideSet) {
                            if (view != null && view.getVisibility() != View.VISIBLE) {
                                view.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                    mLiveCommentView.setVisibility(View.VISIBLE);
                    mBarrageBtnView.setVisibility(View.VISIBLE);
                    mController.postEvent(MSG_HIDE_GAME_INPUT);
                }
                updateRotateBtn();
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                mIsLandscape = true;
                mAnimationHelper.stopAllAnimator();
                if (mIsGameMode) { // 游戏直播横屏不需左右滑
                    mController.postEvent(MSG_DISABLE_MOVE_VIEW);
                    mLiveCommentView.setVisibility(View.INVISIBLE);
                    mBarrageBtnView.setVisibility(View.INVISIBLE);
                    mController.postEvent(MSG_SHOW_GAME_INPUT);
                }
                updateRotateBtn();
                return true;
            case MSG_INPUT_VIEW_SHOWED:
                if (!mIsGameMode || !mIsLandscape) {
                    mController.postEvent(MSG_DISABLE_MOVE_VIEW);
                }
                if (mGiftContinueViewGroup != null) {
                    mGiftContinueViewGroup.onShowInputView();
                }
                mAnimationHelper.startInputAnimator(true);
                return true;
            case MSG_INPUT_VIEW_HIDDEN:
                if (!mIsGameMode || !mIsLandscape) { // 游戏直播横屏不需左右滑
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
                if (mIsGameMode && mIsLandscape) {
                    mAnimationHelper.startGameAnimator();
                    return true;
                }
                break;
            case MSG_FOLLOW_COUNT_DOWN:
                if (mController.mMyRoomData.isEnableFollow()) {
                    if (mFollowGuidePresenter != null ||
                            TextUtils.isEmpty(RoomInfoGlobalCache.getsInstance().getCurrentRoomId())) {
                        return false;
                    }
                    int countTs = params.getItem(0);
                    mFollowGuidePresenter = new FollowGuidePresenter(mController,
                            mController.mMyRoomData);
                    mFollowGuidePresenter.countDownOut(countTs);
                }
                break;
            case MSG_SHOW_FOLLOW_GUIDE: {
                if (mController.mMyRoomData.isEnableFollow()) {
                    if (mFollowGuidePresenter == null || mFollowGuideView != null
                            || TextUtils.isEmpty(RoomInfoGlobalCache.getsInstance().getCurrentRoomId())) {
                        return false;
                    }
                    mFollowGuideView = new FollowGuideView(mActivity);
                    mFollowGuideView.setVisibility(View.INVISIBLE);
                    registerComponent(mFollowGuideView, mFollowGuidePresenter);

                    RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);

                    ViewGroup rootView = (ViewGroup) mActivity.findViewById(R.id.main_act_container);
                    rootView.addView(mFollowGuideView, layoutParams);

                    mFollowGuideView.post(new Runnable() {
                        @Override
                        public void run() {
                            mFollowGuideView.setMyRoomData(mController.mMyRoomData);
                            mFollowGuideView.onOrientation(mIsLandscape);
                        }
                    });

                    // 出来关注，让关注一起移动
                    mVerticalMoveSet.add(mFollowGuideView);
                }
            }
                break;
            case MSG_SHOW_SEND_ENVELOPE:
                SendEnvelopeFragment.openFragment((BaseActivity) mActivity, mController.mMyRoomData);
                return true;
            case MSG_VIDEO_LANDSCAPE:
                mIsVideoLandscape = true;
                updateRotateBtn();
                break;
            case MSG_VIDEO_PORTRAIT:
                mIsVideoLandscape = false;
                updateRotateBtn();
                break;
            default:
                break;
        }
        return false;
    }

    protected class AnimationHelper extends BaseSdkView.AnimationHelper {

        protected WeakReference<ValueAnimator> mInputAnimatorRef; // 输入框弹起和收起时，隐藏和显示View动画
        protected boolean mInputShow = false;

        private void startInputAnimator(boolean inputShow) {
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
                        if (mIsLandscape && !mIsGameMode) {
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
                        if (!mIsGameMode && mLiveCommentView.getVisibility() != View.VISIBLE) {
                            mLiveCommentView.setVisibility(View.VISIBLE);
                        }
                    }
                }
            });
            mInputAnimatorRef = new WeakReference<>(valueAnimator);
        }

        private WeakReference<ValueAnimator> mGameAnimatorRef; // 游戏直播竖屏时，隐藏显示动画
        private boolean mGameHide = false;

        /**
         * 观看游戏直播横屏时，点击隐藏显示View
         */
        private void startGameAnimator() {
            mGameHide = !mGameHide;
            if (startRefAnimator(mGameAnimatorRef)) {
                return;
            }
            ValueAnimator valueAnimator = startNewAnimator(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    if (mGameHide) {
                        value = 1.0f - value;
                    }
                    for (View view : mGameHideSet) {
                        if (view != null) {
                            view.setAlpha(value);
                        }
                    }
                }
            }, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    if (!mGameHide) {
                        for (View view : mGameHideSet) {
                            if (view != null) {
                                view.setAlpha(0.0f);
                                view.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (mGameHide) {
                        for (View view : mGameHideSet) {
                            if (view != null) {
                                view.setAlpha(1.0f);
                                view.setVisibility(View.GONE);
                            }
                        }
                    }
                }
            });
            mGameAnimatorRef = new WeakReference<>(valueAnimator);
        }

        @Override
        protected void stopAllAnimator() {
            stopRefAnimator(mInputAnimatorRef);
            stopRefAnimator(mGameAnimatorRef);
        }

        @Override
        public void clearAnimation() {
            stopAllAnimator();
            mInputAnimatorRef = null;
            mGameAnimatorRef = null;
        }
    }
}
