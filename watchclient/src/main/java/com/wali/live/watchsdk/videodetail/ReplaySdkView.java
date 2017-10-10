package com.wali.live.watchsdk.videodetail;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.RelativeLayout;

import com.base.activity.BaseActivity;
import com.base.log.MyLog;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.LiveManager;
import com.thornbirds.component.IParams;
import com.wali.live.common.barrage.manager.BarrageMessageManager;
import com.wali.live.common.flybarrage.view.FlyBarrageViewGroup;
import com.wali.live.common.gift.view.GiftAnimationView;
import com.wali.live.common.gift.view.GiftContinueViewGroup;
import com.wali.live.common.gift.view.GiftRoomEffectView;
import com.wali.live.component.BaseSdkView;
import com.wali.live.proto.LiveMessageProto;
import com.wali.live.utils.ReplayBarrageMessageManager;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;
import com.wali.live.watchsdk.component.presenter.LiveCommentPresenter;
import com.wali.live.watchsdk.component.presenter.TopAreaPresenter;
import com.wali.live.watchsdk.component.presenter.TouchPresenter;
import com.wali.live.watchsdk.component.view.LiveCommentView;
import com.wali.live.watchsdk.component.view.TopAreaView;
import com.wali.live.watchsdk.videodetail.view.DetailPlayerView;
import com.wali.live.watchsdk.watch.presenter.push.GiftPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomStatusPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomTextMsgPresenter;
import com.wali.live.watchsdk.watch.presenter.push.RoomViewerPresenter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.wali.live.component.BaseSdkController.MSG_BACKGROUND_CLICK;
import static com.wali.live.component.BaseSdkController.MSG_DISABLE_MOVE_VIEW;
import static com.wali.live.component.BaseSdkController.MSG_ENABLE_MOVE_VIEW;
import static com.wali.live.component.BaseSdkController.MSG_FORCE_ROTATE_SCREEN;
import static com.wali.live.component.BaseSdkController.MSG_HIDE_GAME_INPUT;
import static com.wali.live.component.BaseSdkController.MSG_HIDE_INPUT_VIEW;
import static com.wali.live.component.BaseSdkController.MSG_INPUT_VIEW_HIDDEN;
import static com.wali.live.component.BaseSdkController.MSG_INPUT_VIEW_SHOWED;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.component.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.component.BaseSdkController.MSG_SHOW_GAME_INPUT;
import static com.wali.live.component.BaseSdkController.MSG_SWITCH_TO_DETAIL_MODE;

/**
 * Created by yangli on 2017/6/19.
 *
 * @module 详情播放全屏
 */
public class ReplaySdkView extends BaseSdkView<View, VideoDetailController>
        implements View.OnClickListener {

    private final List<View> mHorizontalMoveSet = new ArrayList();
    private final List<View> mVerticalMoveSet = new ArrayList(0);
    private final List<View> mGameHideSet = new ArrayList(0);

    protected final AnimationHelper mAnimationHelper = new AnimationHelper();

    protected GiftPresenter mGiftPresenter;
    protected RoomTextMsgPresenter mRoomTextMsgPresenter;
    protected RoomViewerPresenter mRoomViewerPresenter;
    protected RoomStatusPresenter mRoomStatusPresenter;

    @Nullable
    protected TopAreaView mTopAreaView;
    @Nullable
    protected View mLiveCommentView;
    @Nullable
    protected GiftContinueViewGroup mGiftContinueViewGroup;

    protected GiftAnimationView mGiftAnimationView;
    protected GiftRoomEffectView mGiftRoomEffectView;
    protected FlyBarrageViewGroup mFlyBarrageViewGroup;
    protected View mCloseBtn;
    protected View mRotateBtn;

    protected boolean mIsGameMode = false;
    protected boolean mIsLandscape = false;

    protected Timer mTimer; // 每秒拉取弹幕的timer
    protected long mVideoStartTime;

    @Override
    protected String getTAG() {
        return "ReplaySdkView";
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.close_btn) {
            mController.postEvent(MSG_SWITCH_TO_DETAIL_MODE);
        } else if (i == R.id.rotate_btn) {
            mController.postEvent(MSG_FORCE_ROTATE_SCREEN);
        }
    }

    public ReplaySdkView(
            @NonNull Activity activity,
            @NonNull VideoDetailController controller) {
        super(activity, (ViewGroup) activity.findViewById(android.R.id.content), controller);
        mIsGameMode = (controller.mMyRoomData.getLiveType() == LiveManager.TYPE_LIVE_GAME);
    }

    @Override
    public void setupView() {
        MyLog.w(TAG, "setupView");
        mContentView = LayoutInflater.from(mParentView.getContext()).inflate(R.layout.video_replay_layout,
                mParentView, false);
        mGiftPresenter = new GiftPresenter(mController.mRoomChatMsgManager, false);
        mRoomTextMsgPresenter = new RoomTextMsgPresenter(mController.mRoomChatMsgManager);
        mRoomViewerPresenter = new RoomViewerPresenter(mController.mRoomChatMsgManager);
        mRoomStatusPresenter = new RoomStatusPresenter(mController.mRoomChatMsgManager);

        // 顶部面板
        {
            mTopAreaView = $(R.id.top_area_view);
            if (mTopAreaView == null) {
                MyLog.e(TAG, "missing R.id.top_area_view");
                return;
            }
            TopAreaPresenter topAreaPresenter = new TopAreaPresenter(mController,
                    mController.mMyRoomData, false);
            registerComponent(mTopAreaView, topAreaPresenter);
        }
        // 大礼物动画
        {
            GiftAnimationView view = $(R.id.gift_animation_player_view);
            if (view == null) {
                MyLog.e(TAG, "missing R.id.gift_animation_player_view");
                return;
            }
            mGiftAnimationView = view;
            mGiftAnimationView.onActivityCreate();
        }
        // 房间特效动画
        {
            GiftRoomEffectView view = $(R.id.gift_room_effect_view);
            if (view == null) {
                MyLog.e(TAG, "missing R.id.gift_room_effect_view");
                return;
            }
            mGiftRoomEffectView = view;
            mGiftRoomEffectView.onActivityCreate();
        }
        // 礼物连送动画
        {
            GiftContinueViewGroup view = $(R.id.gift_continue_vg);
            if (view == null) {
                MyLog.e(TAG, "missing R.id.gift_continue_vg");
                return;
            }
            mGiftContinueViewGroup = view;
            mGiftContinueViewGroup.onActivityCreate();
        }
        // 飘屏弹幕
        {
            FlyBarrageViewGroup view = $(R.id.fly_barrage_viewgroup);
            if (view == null) {
                MyLog.e(TAG, "missing R.id.fly_barrage_viewgroup");
                return;
            }
            mFlyBarrageViewGroup = $(R.id.fly_barrage_viewgroup);
            mFlyBarrageViewGroup.onActivityCreate();
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

        mCloseBtn = $(R.id.close_btn);
        mRotateBtn = $(R.id.rotate_btn);
        $click(mRotateBtn, this);
        $click(mCloseBtn, this);
        orientCloseBtn(false);

        mVerticalMoveSet.add($(R.id.close_btn));
        addViewToSet(new int[]{
                R.id.top_area_view,
                R.id.bottom_button_view,
                R.id.live_comment_view,
                R.id.gift_animation_player_view,
                R.id.gift_continue_vg,
                R.id.gift_room_effect_view,
                R.id.widget_view,
                R.id.close_btn,
                R.id.rotate_btn,
        }, mHorizontalMoveSet, mVerticalMoveSet);
        if (mIsGameMode) {
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
        // 滑动
        {
            View view = $(R.id.touch_view);
            if (view == null) {
                return;
            }
            TouchPresenter presenter = new TouchPresenter(mController, view);
            registerComponent(presenter);
            presenter.setViewSet(mHorizontalMoveSet, mVerticalMoveSet, mIsGameMode);
        }
    }

    public void startView(long videoStartTime) {
        mVideoStartTime = videoStartTime;
        startView();
    }

    @Override
    public void startView() {
        super.startView();
        if (mParentView.indexOfChild(mContentView) == -1) {
            mParentView.addView(mContentView);
            mAnimationHelper.startShowAnimation();
        }
        ReplayBarrageMessageManager.getInstance().init(mController.mRoomChatMsgManager.toString()); //回放弹幕管理
        startGetBarrageTimer();

        BaseComponentSdkActivity sdkActivity = (BaseComponentSdkActivity) mActivity;
        sdkActivity.addPushProcessor(mGiftPresenter);
        sdkActivity.addPushProcessor(mRoomTextMsgPresenter);
        sdkActivity.addPushProcessor(mRoomViewerPresenter);
        sdkActivity.addPushProcessor(mRoomStatusPresenter);

        // 添加播放器View
        DetailPlayerView view = mController.mPlayerView;
        if (view == null) {
            MyLog.e(TAG, "missing mController.mPlayerView");
            return;
        }
        view.switchToReplayMode();
        mController.mPlayerPresenter.setIsDetailMode(false);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        addViewUnderAnchor(view, layoutParams, $(R.id.top_area_view));

        registerAction(MSG_ON_ORIENT_PORTRAIT);
        registerAction(MSG_ON_ORIENT_LANDSCAPE);
        registerAction(MSG_INPUT_VIEW_SHOWED);
        registerAction(MSG_INPUT_VIEW_HIDDEN);
        registerAction(MSG_BACKGROUND_CLICK);
    }

    @Override
    public void stopView() {
        super.stopView();
        mAnimationHelper.clearAnimation();
        mParentView.removeView(mContentView);
        ReplayBarrageMessageManager.getInstance().destory();
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        BaseComponentSdkActivity sdkActivity = (BaseComponentSdkActivity) mActivity;
        sdkActivity.removePushProcessor(mGiftPresenter);
        sdkActivity.removePushProcessor(mRoomTextMsgPresenter);
        sdkActivity.removePushProcessor(mRoomViewerPresenter);
        sdkActivity.removePushProcessor(mRoomStatusPresenter);

        // 将播放器View从其父View移出
        ViewGroup parentView = mController.mPlayerView != null ?
                (ViewGroup) mController.mPlayerView.getParent() : null;
        if (parentView != null) {
            parentView.removeView(mController.mPlayerView);
        }
    }

    @Override
    public void release() {
        super.release();
        mGiftPresenter.destroy();
        mRoomTextMsgPresenter.destroy();
        mRoomViewerPresenter.destroy();
        mRoomStatusPresenter.destroy();

        mGiftAnimationView.onActivityDestroy();
        mGiftRoomEffectView.onActivityDestroy();
        mGiftContinueViewGroup.onActivityDestroy();
        mFlyBarrageViewGroup.onActivityDestroy();
    }

    private void startGetBarrageTimer() {
        if (mTimer == null) {
            syncSystemMessage();
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    MyLog.d(TAG, "TickTimerTask mVideoStartTime=" + mVideoStartTime);
                    if (mController.mPlayerPresenter == null) {
                        return;
                    }
                    ReplayBarrageMessageManager.getInstance().getBarrageMessageByReplayTime(
                            mController.mMyRoomData.getRoomId(),
                            mVideoStartTime + mController.mStreamerPresenter.getCurrentPosition(),
                            false);
                }
            }, 0, 1000);
        }
    }

    private void syncSystemMessage() {
        final String roomId = mController.mMyRoomData.getRoomId();
        if (!TextUtils.isEmpty(roomId)) {
            long myId = UserAccountManager.getInstance().getUuidAsLong();
            LiveMessageProto.SyncSysMsgRequest syncSysMsgRequest =
                    LiveMessageProto.SyncSysMsgRequest.newBuilder()
                            .setCid(System.currentTimeMillis())
                            .setFromUser(myId)
                            .setRoomId(roomId)
                            .build();
            BarrageMessageManager.getInstance().sendSyncSystemMessage(syncSysMsgRequest);
        }
    }

    private void orientCloseBtn(boolean isLandscape) {
        if (mCloseBtn == null) {
            return;
        }
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams)
                mCloseBtn.getLayoutParams();
        if (!isLandscape && BaseActivity.isProfileMode()) {
            layoutParams.topMargin = layoutParams.rightMargin + BaseActivity.getStatusBarHeight();
        } else {
            layoutParams.topMargin = layoutParams.rightMargin;
        }
        mCloseBtn.setLayoutParams(layoutParams);
    }

    @Override
    public boolean onEvent(int event, IParams params) {
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                mIsLandscape = false;
                mAnimationHelper.stopAllAnimator();
                orientCloseBtn(false);
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
                    mController.postEvent(MSG_HIDE_GAME_INPUT);
                }
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                mIsLandscape = true;
                mAnimationHelper.stopAllAnimator();
                orientCloseBtn(true);
                if (mIsGameMode) { // 游戏直播横屏不需左右滑
                    mController.postEvent(MSG_DISABLE_MOVE_VIEW);
                    mLiveCommentView.setVisibility(View.INVISIBLE);
                    mController.postEvent(MSG_SHOW_GAME_INPUT);
                }
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
                        if (mIsLandscape && !mIsGameMode) {
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
            stopRefAnimator(mInputAnimatorRef);
            stopRefAnimator(mGameAnimatorRef);
            stopRefAnimation(mShowAnimationRef);
        }

        @Override
        public void clearAnimation() {
            stopAllAnimator();
            mInputAnimatorRef = null;
            mGameAnimatorRef = null;
        }

    }
}
