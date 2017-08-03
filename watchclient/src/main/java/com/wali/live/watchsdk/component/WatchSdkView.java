package com.wali.live.watchsdk.component;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.base.activity.BaseActivity;
import com.base.log.MyLog;
import com.base.utils.Constants;
import com.base.utils.display.DisplayUtils;
import com.mi.live.data.cache.RoomInfoGlobalCache;
import com.thornbirds.component.IParams;
import com.wali.live.common.gift.view.GiftContinueViewGroup;
import com.wali.live.componentwrapper.BaseSdkView;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;
import com.wali.live.watchsdk.component.presenter.BarrageBtnPresenter;
import com.wali.live.watchsdk.component.presenter.BottomButtonPresenter;
import com.wali.live.watchsdk.component.presenter.EnvelopePresenter;
import com.wali.live.watchsdk.component.presenter.FollowGuidePresenter;
import com.wali.live.watchsdk.component.presenter.GameBarragePresenter;
import com.wali.live.watchsdk.component.presenter.GameDownloadPresenter;
import com.wali.live.watchsdk.component.presenter.GameInputPresenter;
import com.wali.live.watchsdk.component.presenter.ImagePagerPresenter;
import com.wali.live.watchsdk.component.presenter.InputAreaPresenter;
import com.wali.live.watchsdk.component.presenter.LiveCommentPresenter;
import com.wali.live.watchsdk.component.presenter.TouchPresenter;
import com.wali.live.watchsdk.component.presenter.WidgetPresenter;
import com.wali.live.watchsdk.component.view.BarrageBtnView;
import com.wali.live.watchsdk.component.view.FollowGuideView;
import com.wali.live.watchsdk.component.view.GameBarrageView;
import com.wali.live.watchsdk.component.view.GameInputView;
import com.wali.live.watchsdk.component.view.ImagePagerView;
import com.wali.live.watchsdk.component.view.InputAreaView;
import com.wali.live.watchsdk.component.view.LiveCommentView;
import com.wali.live.watchsdk.component.view.WatchBottomButton;
import com.wali.live.watchsdk.component.view.WidgetView;
import com.wali.live.watchsdk.component.view.panel.GameDownloadPanel;
import com.wali.live.watchsdk.envelope.SendEnvelopeFragment;
import com.wali.live.watchsdk.watch.presenter.PanelContainerPresenter;

import java.util.ArrayList;
import java.util.List;

import static com.wali.live.componentwrapper.BaseSdkController.MSG_BACKGROUND_CLICK;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_DISABLE_MOVE_VIEW;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_ENABLE_MOVE_VIEW;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_FOLLOW_COUNT_DOWN;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_HIDE_GAME_INPUT;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_HIDE_INPUT_VIEW;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_INPUT_VIEW_HIDDEN;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_INPUT_VIEW_SHOWED;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_ON_ORIENT_LANDSCAPE;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_ON_ORIENT_PORTRAIT;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_PAGE_DOWN;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_PAGE_UP;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_SHOW_FOLLOW_GUIDE;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_SHOW_GAME_INPUT;
import static com.wali.live.componentwrapper.BaseSdkController.MSG_SHOW_SEND_ENVELOPE;

/**
 * Created by yangli on 2017/2/18.
 *
 * @module 游戏直播页面
 */
public class WatchSdkView extends BaseSdkView<View, WatchComponentController> {

    private final List<View> mHorizontalMoveSet = new ArrayList();
    private final List<View> mVerticalMoveSet = new ArrayList(0);
    private final List<View> mGameHideSet = new ArrayList(0);

    protected View mTopInfoView;
    @Nullable
    protected LiveCommentView mLiveCommentView;
    protected View mBarrageBtnView;

    protected GiftContinueViewGroup mGiftContinueViewGroup;

    // 关注弹窗
    protected FollowGuideView mFollowGuideView;
    protected FollowGuidePresenter mFollowGuidePresenter;

    protected WatchBottomButton mWatchBottomButton;

    protected GameBarragePresenter mGameBarragePresenter;
    protected GameInputPresenter mGameInputPresenter;
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
            @NonNull WatchComponentController controller) {
        super(activity, (ViewGroup) activity.findViewById(android.R.id.content), controller);
    }

    public void setupView(boolean isGameMode) {
        mIsGameMode = isGameMode;
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
            addViewAboveAnchor(view, layoutParams, $(R.id.comment_rv));
        } else {
            mGameBarragePresenter.startPresenter();
        }

        // 游戏直播横屏输入框
        if (mGameInputPresenter == null) {
            GameInputView view = new GameInputView(mActivity);
            view.setId(R.id.game_input_view);
            view.setVisibility(View.GONE);
            mGameInputPresenter = new GameInputPresenter(mController, mController.mMyRoomData);
            registerComponent(view, mGameInputPresenter);

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
            addViewAboveAnchor(view, layoutParams, $(R.id.input_area_view));
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
                    R.id.watch_top_info_view,
                    R.id.bottom_button_view,
                    R.id.game_barrage_view,
                    R.id.game_input_view,
                    R.id.close_btn,
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
        mTopInfoView = $(R.id.watch_top_info_view);         // 顶部view

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

        // 底部面板
        {
            RelativeLayout relativeLayout = $(R.id.bottom_panel_view);
            if (relativeLayout == null) {
                MyLog.e(TAG, "missing R.id.bottom_panel_view");
                return;
            }
            PanelContainerPresenter presenter = new PanelContainerPresenter(mController,
                    mController.mMyRoomData);
            presenter.setComponentView(relativeLayout);
            registerComponent(presenter);
        }

        // 输入框
        {
            InputAreaView view = $(R.id.input_area_view);
            if (view == null) {
                return;
            }
            InputAreaPresenter presenter = new InputAreaPresenter(
                    mController, mController.mMyRoomData, true);
            registerComponent(view, presenter);
        }

        //底部输入框
        {
            BarrageBtnView view = $(R.id.barrage_btn_view);
            if (view == null) {
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
            mWatchBottomButton = new WatchBottomButton(relativeLayout, mIsGameMode,
                    mController.mMyRoomData.getEnableShare());
            BottomButtonPresenter presenter = new BottomButtonPresenter(
                    mController, mController.mMyRoomData);
            registerComponent(mWatchBottomButton, presenter);
        }

        // 抢红包
        {
            RelativeLayout relativeLayout = $(R.id.envelope_view);
            EnvelopePresenter presenter = new EnvelopePresenter(mController, mController.mMyRoomData);
            presenter.setComponentView(relativeLayout);
            registerComponent(presenter);
        }

        if (!Constants.isGooglePlayBuild && !Constants.isIndiaBuild) {
            // 运营位
            {
                WidgetView view = $(R.id.widget_view);
                if (view == null) {
                    MyLog.e(TAG, "missing R.id.widget_view");
                    return;
                }
                WidgetPresenter presenter = new WidgetPresenter(mController, mController.mMyRoomData, false);
                registerComponent(view, presenter);
                ((BaseComponentSdkActivity) mActivity).addPushProcessor(presenter);
            }
        }

        if (mController.mRoomInfoList != null && mController.mRoomInfoList.size() > 1) {
            {
                mPagerView = new ImagePagerView(mActivity);
                mPagerView.setVerticalList(mController.mRoomInfoList, mController.mRoomInfoPosition);

                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                addViewAboveAnchor(mPagerView, layoutParams, $(R.id.mask_iv));

                ImagePagerPresenter presenter = new ImagePagerPresenter(mController);
                registerComponent(mPagerView, presenter);
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
                R.id.barrage_btn_view
        }, mHorizontalMoveSet);

        addViewToSet(new int[]{
                R.id.watch_top_info_view,
                R.id.bottom_button_view,
                R.id.live_comment_view,
                R.id.gift_animation_player_view,
                R.id.gift_continue_vg,
                R.id.gift_room_effect_view,
                R.id.widget_view,
                R.id.barrage_btn_view,
                R.id.mask_iv,
                R.id.rotate_btn,
                R.id.close_btn,
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
            // 增加上线滑动的判断
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
        registerAction(MSG_SHOW_FOLLOW_GUIDE);
        registerAction(MSG_FOLLOW_COUNT_DOWN);
        registerAction(MSG_PAGE_DOWN);
        registerAction(MSG_PAGE_UP);
        registerAction(MSG_SHOW_SEND_ENVELOPE);
    }

    @Override
    public void release() {
        super.release();
//        clearAnimation();
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

        mLiveCommentView.reset();
        mWatchBottomButton.reset();
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

    @Override
    public boolean onEvent(int event, IParams params) {
        switch (event) {
            case MSG_ON_ORIENT_PORTRAIT:
                mIsLandscape = false;
//                stopAllAnimator();
                if (mIsGameMode) {
                    mController.postEvent(MSG_ENABLE_MOVE_VIEW);
//                    if (mGameHide) { // 横屏转竖屏，恢复被隐藏的View，竖屏转横屏的逻辑在TouchPresenter中处理
//                        mGameHide = false;
//                        for (View view : mGameHideSet) {
//                            if (view != null && view.getVisibility() != View.VISIBLE) {
//                                view.setVisibility(View.VISIBLE);
//                            }
//                        }
//                    }
//                    setVisibility(mLiveCommentView, View.VISIBLE);
//                    setVisibility(mBarrageBtnView, View.VISIBLE);
                    mController.postEvent(MSG_HIDE_GAME_INPUT);
                }
                return true;
            case MSG_ON_ORIENT_LANDSCAPE:
                mIsLandscape = true;
//                stopAllAnimator();
                if (mIsGameMode) { // 游戏直播横屏不需左右滑
                    mController.postEvent(MSG_DISABLE_MOVE_VIEW);
//                    setVisibility(mLiveCommentView, View.INVISIBLE);
//                    setVisibility(mBarrageBtnView, View.INVISIBLE);
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
//                startInputAnimator(true);
                return true;
            case MSG_INPUT_VIEW_HIDDEN:
                if (!mIsGameMode || !mIsLandscape) { // 游戏直播横屏不需左右滑
                    mController.postEvent(MSG_ENABLE_MOVE_VIEW);
                }
                if (mGiftContinueViewGroup != null) {
                    mGiftContinueViewGroup.onHideInputView();
                }
//                startInputAnimator(false);
                return true;
            case MSG_BACKGROUND_CLICK:
                if (mController.postEvent(MSG_HIDE_INPUT_VIEW)) {
                    return true;
                }
                if (mIsGameMode && mIsLandscape) {
//                    startGameAnimator();
                    return true;
                }
                break;
            case MSG_FOLLOW_COUNT_DOWN:
                if (mFollowGuidePresenter != null ||
                        TextUtils.isEmpty(RoomInfoGlobalCache.getsInstance().getCurrentRoomId())) {
                    return false;
                }
                int countTs = params.getItem(0);
                mFollowGuidePresenter = new FollowGuidePresenter(mController,
                        mController.mMyRoomData);
                mFollowGuidePresenter.countDownOut(countTs);
                break;
            case MSG_SHOW_FOLLOW_GUIDE: {
                if (mFollowGuidePresenter == null || mFollowGuideView != null
                        || TextUtils.isEmpty(RoomInfoGlobalCache.getsInstance().getCurrentRoomId())) {
                    return false;
                }
                mFollowGuideView = new FollowGuideView(mActivity);
                mFollowGuideView.setVisibility(View.INVISIBLE);
                mFollowGuidePresenter.setComponentView(mFollowGuideView.getViewProxy());
                mFollowGuideView.setPresenter(mFollowGuidePresenter);
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
            break;
            case MSG_SHOW_SEND_ENVELOPE:
                SendEnvelopeFragment.openFragment((BaseActivity) mActivity, mController.mMyRoomData);
                break;
            default:
                break;
        }
        return false;
    }
}
