package com.module.playways.grab.room.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseActivity;
import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.permission.SkrAudioPermission;
import com.common.core.permission.SkrCameraPermission;
import com.common.core.userinfo.UserInfoManager;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.DebugLogView;
import com.common.log.MyLog;
import com.common.rxretrofit.ApiManager;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.AnimateClickListener;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.component.busilib.beauty.JumpBeautyFromKt;
import com.component.busilib.constans.GrabRoomType;
import com.component.busilib.manager.BgMusicManager;
import com.dialog.view.TipsDialogView;
import com.module.RouterConstants;
import com.module.home.IHomeService;
import com.module.playways.R;
import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.activity.GrabRoomActivity;
import com.module.playways.grab.room.bottom.GrabBottomContainerView;
import com.module.playways.grab.room.voicemsg.VoiceRecordTipsView;
import com.module.playways.grab.room.event.GrabSomeOneLightBurstEvent;
import com.module.playways.grab.room.event.GrabSomeOneLightOffEvent;
import com.module.playways.grab.room.event.GrabWantInviteEvent;
import com.module.playways.grab.room.event.LightOffAnimationOverEvent;
import com.module.playways.grab.room.inter.IGrabRoomView;
import com.module.playways.grab.room.invite.fragment.InviteFriendFragment2;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.model.WantSingerInfo;
import com.module.playways.grab.room.presenter.DoubleRoomInvitePresenter;
import com.module.playways.grab.room.presenter.GrabCorePresenter;
import com.module.playways.grab.room.presenter.GrabRedPkgPresenter;
import com.module.playways.grab.room.voicemsg.VoiceRecordUiController;
import com.module.playways.songmanager.SongManagerActivity;
import com.module.playways.songmanager.event.ChangeTagSuccessEvent;
import com.module.playways.grab.room.top.GrabTopContentView;
import com.module.playways.grab.room.top.GrabTopOpView;
import com.module.playways.grab.room.view.GameTipsManager;
import com.module.playways.grab.room.view.GrabChangeRoomTransitionView;
import com.module.playways.grab.room.view.GrabDengBigAnimationView;
import com.module.playways.grab.room.view.GrabGameOverView;
import com.module.playways.grab.room.view.GrabGiveupView;
import com.module.playways.grab.room.view.GrabOpView;
import com.module.playways.grab.room.view.GrabRootView;
import com.module.playways.grab.room.view.GrabScoreTipsView;
import com.module.playways.grab.room.view.GrabVoiceControlPanelView;
import com.module.playways.grab.room.view.IRedPkgCountDownView;
import com.module.playways.grab.room.view.IUpdateFreeGiftCountView;
import com.module.playways.grab.room.view.SongInfoCardView;
import com.module.playways.grab.room.view.TurnInfoCardView;
import com.module.playways.grab.room.view.control.OthersSingCardView;
import com.module.playways.grab.room.view.control.RoundOverCardView;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.module.playways.grab.room.view.control.SingBeginTipsCardView;
import com.module.playways.grab.room.view.video.GrabVideoDisplayView;
import com.module.playways.grab.room.view.video.GrabVideoSelfSingCardView;
import com.module.playways.room.gift.event.BuyGiftEvent;
import com.module.playways.room.gift.event.ShowHalfRechargeFragmentEvent;
import com.module.playways.room.gift.event.UpdateMeiGuiFreeCountEvent;
import com.module.playways.room.gift.view.ContinueSendView;
import com.module.playways.room.gift.view.GiftDisplayView;
import com.module.playways.room.gift.view.GiftPanelView;
import com.module.playways.room.prepare.model.BaseRoundInfoModel;
import com.module.playways.room.prepare.model.OnlineInfoModel;
import com.module.playways.room.room.comment.CommentView;
import com.module.playways.room.room.comment.listener.CommentViewItemListener;
import com.module.playways.room.room.gift.GiftBigAnimationViewGroup;
import com.module.playways.room.room.gift.GiftBigContinuousView;
import com.module.playways.room.room.gift.GiftContinueViewGroup;
import com.module.playways.room.room.gift.GiftOverlayAnimationViewGroup;
import com.module.playways.room.room.view.BottomContainerView;
import com.module.playways.room.room.view.InputContainerView;
import com.module.playways.room.song.model.SongModel;
import com.moudule.playways.beauty.view.BeautyControlPanelView;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.zq.dialog.ConfirmDialog;
import com.zq.dialog.PersonInfoDialog;
import com.zq.live.proto.Common.EMsgRoomMediaType;
import com.zq.live.proto.Room.EQRoundStatus;
import com.zq.person.event.ShowPersonCardEvent;
import com.zq.report.fragment.QuickFeedbackFragment;
import com.zq.toast.CommonToastView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

import static android.view.View.GONE;

public class GrabRoomFragment extends BaseFragment implements IGrabRoomView, IRedPkgCountDownView, IUpdateFreeGiftCountView {

    public final static String TAG = "GrabRoomFragment";

    public final static String TAG_MANAGE_SONG_TIP_VIEW = "ownerShowTimes";
    public final static String TAG_INVITE_TIP_VIEW = "inviteShowTimes";
    public final static String TAG_CHANLLENGE_TIP_VIEW = "showChallengeTime";
    public final static String TAG_GRAB_ROB_TIP_VIEW = "showGrabRobTime";
    public final static String TAG_BURST_TIP_VIEW = "tag_burst_tips";
    public final static String TAG_SELF_SING_TIP_VIEW = "tag_self_sing_tips";
    public final static String TAG_NOACC_SROLL_TIP_VIEW = "tag_noacc_sroll_tips";

//    public static final int MSG_ENSURE_READYGO_OVER = 1;

    public static final int MSG_ENSURE_SONGCARD_OVER = 2;

    public static final int MSG_ENSURE_SING_BEGIN_TIPS_OVER = 3;

    public static final int MSG_ENSURE_ROUND_OVER_PLAY_OVER = 4;

    public static final int MSG_ENSURE_BATTLE_BEGIN_OVER = 5;

//    public static final int MSG_ENSURE_GAME_OVER = 6;

    //自己演唱玩
//    public static final int MSG_SEND_SELF_SING_END = 7;

    GrabRoomData mRoomData;

    GrabRootView mGrabRootView;

    ExImageView mGrabRoomBgFlag;

    InputContainerView mInputContainerView;

    ViewGroup mBottomBgVp;

    GrabBottomContainerView mBottomContainerView;

//    GiftTimerPresenter mGiftTimerPresenter;

//    GrabVoiceControlPanelView mVoiceControlView;

//    RedPkgCountDownView mRedPkgView;

    CommentView mCommentView;

    //    GrabTopContainerView mTopContainerView;// 顶部，抢唱阶段，以及非本人的演唱阶段
    GrabTopOpView mGrabTopOpView;

    GrabTopContentView mGrabTopContentView;

    ExImageView mPracticeFlagIv; // 练习中

    GrabCorePresenter mCorePresenter;

    GrabRedPkgPresenter mGrabRedPkgPresenter;

    DoubleRoomInvitePresenter mDoubleRoomInvitePresenter;

//    DownLoadScoreFilePresenter mDownLoadScoreFilePresenter;

    TurnInfoCardView mTurnInfoCardView; //歌曲次序 以及 对战开始卡片

    SongInfoCardView mSongInfoCardView; // 歌曲信息卡片

    RoundOverCardView mRoundOverCardView; // 轮次结束卡片

    OthersSingCardView mOthersSingCardView; // 他人演唱卡片

    SelfSingCardView mSelfSingCardView; // 自己演唱卡片

    SingBeginTipsCardView mSingBeginTipsCardView;// 演唱提示卡片

    GrabOpView mGrabOpBtn; // 抢 倒计时 灭 等按钮

    GrabGiveupView mGrabGiveupView;

    ExImageView mMiniOwnerMicIv;

    GrabGameOverView mGrabGameOverView;

    GrabDengBigAnimationView mDengBigAnimation;

    GrabChangeRoomTransitionView mGrabChangeRoomTransitionView;

    GrabScoreTipsView mGrabScoreTipsView;

    DialogPlus mQuitTipsDialog;

    PersonInfoDialog mPersonInfoDialog;

    DialogPlus mGameRuleDialog;

    ConfirmDialog mGrabKickDialog;

    GrabVoiceControlPanelView mGrabVoiceControlPanelView;

    GiftPanelView mGiftPanelView;

    GiftContinueViewGroup mGiftContinueViewGroup;

    ContinueSendView mContinueSendView;

    DialogPlus mVoiceControlDialog;

    TipsDialogView mTipsDialogView;

    GrabVideoDisplayView mGrabVideoDisplayView; // 视频展示view

    GrabVideoSelfSingCardView mGrabVideoSelfSingCardView; // 视频模式下 自己演唱时的歌词面板view

    BeautyControlPanelView mBeautyControlPanelView;

    VoiceRecordTipsView mVoiceRecordTipsView;

    VoiceRecordUiController mVoiceRecordUiController;

    List<Animator> mAnimatorList = new ArrayList<>();  //存放所有需要尝试取消的动画

    boolean mIsGameEndAniamtionShow = false; // 标记对战结束动画是否播放

    long mBeginChangeRoomTs;

    ImageView mOwnerBeginGameIv;

    GameTipsManager mGameTipsManager = new GameTipsManager();

    SkrAudioPermission mSkrAudioPermission = new SkrAudioPermission();

    SkrCameraPermission mSkrCameraPermission = new SkrCameraPermission();

    GrabAudioUiController mGrabAudioUiController = new GrabAudioUiController(this);

    GrabVideoUiController mGrabVideoUiController = new GrabVideoUiController(this);

    GrabBaseUiController mGrabBaseUiController = mGrabAudioUiController;

    GrabWidgetAnimationController mGrabWidgetAnimationController = new GrabWidgetAnimationController(this);

    Handler mUiHanlder = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
//                case MSG_ENSURE_READYGO_OVER:
//                    onReadyGoOver();
//                    break;
                case MSG_ENSURE_BATTLE_BEGIN_OVER:
                    onBattleBeginPlayOver();
                    break;
                case MSG_ENSURE_SONGCARD_OVER:
                    onSongInfoCardPlayOver("MSG_ENSURE_SONGCARD_OVER", (PendingPlaySongCardData) msg.obj);
                    break;
                case MSG_ENSURE_SING_BEGIN_TIPS_OVER:
                    onSingBeginTipsPlayOver();
                    break;
                case MSG_ENSURE_ROUND_OVER_PLAY_OVER:
                    onRoundOverPlayOver(msg.arg1 == 1, (BaseRoundInfoModel) msg.obj);
                    break;
//                case MSG_ENSURE_GAME_OVER:
//                    onGrabGameOver("MSG_ENSURE_GAME_OVER");
//                    break;
//                case MSG_SEND_SELF_SING_END:
//                    mCorePresenter.sendRoundOverInfo();
//                    break;
            }
        }
    };

    @Override
    public int initView() {
        return R.layout.grab_room_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        if (System.currentTimeMillis() - mRoomData.getGameStartTs() > 3 * 60 * 1000) {
            Log.w(TAG, "隔了很久从后台返回的，直接finish Activity");
            if (getActivity() != null) {
                getActivity().finish();
            }
            return;
        }

        // 请保证从下面的view往上面的view开始初始化
        mGrabRootView = mRootView.findViewById(R.id.grab_root_view);
//        mGrabRootView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mInputContainerView.hideSoftInput();
//            }
//        });
        mGrabRootView.addOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mInputContainerView.hideSoftInput();
                }
                return false;
            }
        });
        initBgView();
        initInputView();
        initBottomView();
        initCommentView();
        initTopView();
        initTurnChangeView();
        initGiftDisplayView();
        initGrabOpView();
        initSingStageView();
        initChangeRoomTransitionView();
//        initCountDownView();
        initScoreView();
        initGiftPanelView();
        initVideoView();
        if (MyLog.isDebugLogOpen()) {
            ViewStub viewStub = mRootView.findViewById(R.id.debug_log_view_stub);
            DebugLogView debugLogView = new DebugLogView(viewStub);
            debugLogView.tryInflate();
        }

        mCorePresenter = new GrabCorePresenter(this, mRoomData, (BaseActivity) getActivity());
        addPresent(mCorePresenter);
        mGrabRedPkgPresenter = new GrabRedPkgPresenter(this);
        addPresent(mGrabRedPkgPresenter);
        mGrabRedPkgPresenter.checkRedPkg();
        mCorePresenter.setGrabRedPkgPresenter(mGrabRedPkgPresenter);
        mDoubleRoomInvitePresenter = new DoubleRoomInvitePresenter();
        addPresent(mDoubleRoomInvitePresenter);
//        mGiftTimerPresenter = new GiftTimerPresenter(this);
//        addPresent(mGiftTimerPresenter);
//        mGiftTimerPresenter.startTimer();

        if (mRoomData.isVideoRoom()) {
            mGrabBaseUiController = mGrabVideoUiController;
        } else {
            mGrabBaseUiController = mGrabAudioUiController;
        }
        U.getSoundUtils().preLoad(TAG, R.raw.grab_challengelose, R.raw.grab_challengewin,
                R.raw.grab_gameover, R.raw.grab_iwannasing,
                R.raw.grab_nobodywants, R.raw.grab_readygo,
                R.raw.grab_xlight, R.raw.normal_click);

        MyLog.w(TAG, "gameid 是 " + mRoomData.getGameId() + " userid 是 " + MyUserInfoManager.getInstance().getUid());

        mUiHanlder.postDelayed(new Runnable() {
            @Override
            public void run() {
                onBattleBeginPlayOver();
                int openOpBarTimes = U.getPreferenceUtils().getSettingInt("key_open_op_bar_times", 0);
                if (openOpBarTimes < 2) {
                    mGrabWidgetAnimationController.open();
                    openOpBarTimes++;
                    U.getPreferenceUtils().setSettingInt("key_open_op_bar_times", openOpBarTimes);
                } else {
                    mGrabWidgetAnimationController.close();
                }
            }
        }, 500);

        BgMusicManager.getInstance().setRoom(true);
        if (mRoomData.isOwner()) {
            if (!mRoomData.hasGameBegin() && mOwnerBeginGameIv == null) {
                // 是房主并且游戏未开始，增加一个 开始游戏 的按钮
                mOwnerBeginGameIv = new ExImageView(getContext());
                mOwnerBeginGameIv.setImageResource(R.drawable.fz_kaishiyouxi);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                lp.rightMargin = U.getDisplayUtils().dip2px(10);
                lp.addRule(RelativeLayout.ALIGN_TOP, R.id.bottom_bg_vp);
                lp.topMargin = -U.getDisplayUtils().dip2px(55);
                int index = mGrabRootView.indexOfChild(mInputContainerView);
                mGrabRootView.addView(mOwnerBeginGameIv, index, lp);
                mOwnerBeginGameIv.setOnClickListener(new DebounceViewClickListener() {
                    @Override
                    public void clickValid(View v) {
                        mCorePresenter.ownerBeginGame();
                    }
                });
            }

            tryShowInviteTipView();
            tryShowManageSongTipView();

        }
        enterRoomEvent();
    }

    private void enterRoomEvent() {
        if (mRoomData.getRoomType() == GrabRoomType.ROOM_TYPE_COMMON) {
            StatisticsAdapter.recordCountEvent("grab", "normalroom_enter", null);
        } else if (mRoomData.getOwnerId() != MyUserInfoManager.getInstance().getUid()) {
            if (mRoomData.getRoomType() == GrabRoomType.ROOM_TYPE_PUBLIC) {
                StatisticsAdapter.recordCountEvent("grab", "hostroom_enter", null);
            } else if (mRoomData.getRoomType() == GrabRoomType.ROOM_TYPE_FRIEND) {
                StatisticsAdapter.recordCountEvent("grab", "friendroom_enter", null);
            } else if (mRoomData.getRoomType() == GrabRoomType.ROOM_TYPE_SECRET) {
                StatisticsAdapter.recordCountEvent("grab", "privateroom_enter", null);
            }
        }
    }

    private void tryShowInviteTipView() {
        new GameTipsManager.GameTipsView(mGrabRootView, R.drawable.guide_yindao_yaoqinghaoyou)
                .setActivity(getActivity())
                .setMargins(0, U.getDisplayUtils().dip2px(75), U.getDisplayUtils().dip2px(54), 0)
                .addRule(RelativeLayout.ALIGN_PARENT_RIGHT, -1)
                .hasAnimation(true)
                .setShowCount(3)
                .setBaseTranslateY(mGrabWidgetAnimationController.isOpen() ? U.getDisplayUtils().dip2px(32) : 0)
                .setTag(TAG_INVITE_TIP_VIEW)
                .tryShow(mGameTipsManager);
    }

    private void tryShowManageSongTipView() {
        new GameTipsManager.GameTipsView(mGrabRootView, R.drawable.guide_yindao_fangzhukongzhizhongxin)
                .setActivity(getActivity())
                .setMargins(0, 0, U.getDisplayUtils().dip2px(10), U.getDisplayUtils().dip2px(68))
                .addRule(RelativeLayout.ALIGN_PARENT_RIGHT, -1)
                .addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, -1)
                .setIndex(mGrabRootView.indexOfChild(mBottomBgVp) + 1)
                .hasAnimation(true)
                .setShowCount(3)
                .setTag(TAG_MANAGE_SONG_TIP_VIEW)
                .tryShow(mGameTipsManager);

    }

    private void tryShowChallengeTipView() {
        new GameTipsManager.GameTipsView(mGrabRootView, R.drawable.guide_jiabei_tiaozhan)
                .setActivity(getActivity())
                .setMargins(0, U.getDisplayUtils().dip2px(2), U.getDisplayUtils().dip2px(20), 0)
                .addRule(RelativeLayout.ALIGN_PARENT_RIGHT, -1)
                .addRule(RelativeLayout.BELOW, R.id.grab_op_btn)
                .setIndex(mGrabRootView.indexOfChild(mGrabOpBtn) + 1)
                .hasAnimation(true)
                .setShowCount(3)
                .setTag(TAG_CHANLLENGE_TIP_VIEW)
                .tryShow(mGameTipsManager);
    }

    // 抢唱提示
    private void tryShowGrabTipView() {
        new GameTipsManager.GameTipsView(mGrabRootView, R.drawable.guide_qiangchang_yindao)
                .setActivity(getActivity())
                .addRule(RelativeLayout.ALIGN_PARENT_RIGHT, -1)
                .addRule(RelativeLayout.ABOVE, R.id.grab_op_btn)
                .setMargins(0, 0, U.getDisplayUtils().dip2px(10), U.getDisplayUtils().dip2px(2))
                .setIndex(mGrabRootView.indexOfChild(mGrabOpBtn) + 1)
                .hasAnimation(false)
                .setShowCount(2)
                .setTag(TAG_GRAB_ROB_TIP_VIEW)
                .tryShow(mGameTipsManager);
    }

    // 爆灯提示
    private void tryShowBurstTipView() {
        new GameTipsManager.GameTipsView(mGrabRootView, R.drawable.guide_baodeng_yindao)
                .setActivity(getActivity())
                .addRule(RelativeLayout.ALIGN_PARENT_RIGHT, -1)
                .addRule(RelativeLayout.ABOVE, R.id.grab_op_btn)
                .setMargins(0, 0, U.getDisplayUtils().dip2px(50), -U.getDisplayUtils().dip2px(5))
                .setIndex(mGrabRootView.indexOfChild(mGrabOpBtn) + 1)
                .hasAnimation(false)
                .setTag(TAG_BURST_TIP_VIEW)
                .setShowCount(1)
                .tryShow(mGameTipsManager);
    }

    // 歌词提示
    void tryShowGrabSelfSingTipView() {
        new GameTipsManager.GameTipsView(mGrabRootView, R.drawable.guide_daojishi_yindao)
                .setActivity(getActivity())
                .addRule(RelativeLayout.ALIGN_PARENT_LEFT, -1)
                .addRule(RelativeLayout.ALIGN_PARENT_TOP, -1)
                .setMargins(U.getDisplayUtils().dip2px(70), U.getDisplayUtils().dip2px(28), 0, 0)
                .setBaseTranslateY(mGrabWidgetAnimationController.isOpen() ? U.getDisplayUtils().dip2px(32) : 0)
                .hasAnimation(false)
                .setTag(TAG_SELF_SING_TIP_VIEW)
                .setShowCount(1)
                .tryShow(mGameTipsManager);
    }

    // 清唱手势滑动
    void tryShowNoAccSrollTipsView() {

        ObjectAnimator mFingerTipViewAnimator = new ObjectAnimator();
        mFingerTipViewAnimator.setProperty(View.TRANSLATION_Y);
        mFingerTipViewAnimator.setFloatValues(0, -U.getDisplayUtils().dip2px(80));
        mFingerTipViewAnimator.setRepeatCount(2);
        mFingerTipViewAnimator.setDuration(1500);
        mFingerTipViewAnimator.setRepeatMode(ValueAnimator.RESTART);
        mFingerTipViewAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                removeNoAccSrollTipsView();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        new GameTipsManager.GameTipsView(mGrabRootView, R.drawable.grab_sroll_finger_icon)
                .setActivity(getActivity())
                .setSize(U.getDisplayUtils().dip2px(64), U.getDisplayUtils().dip2px(54))
                .addRule(RelativeLayout.ALIGN_PARENT_TOP, -1)
                .addRule(RelativeLayout.CENTER_HORIZONTAL, -1)
                .setMargins(0, U.getDisplayUtils().dip2px(285), 0, 0)
                .hasAnimation(true)
                .setAniamtion(mFingerTipViewAnimator)
                .setTag(TAG_NOACC_SROLL_TIP_VIEW)
                .setShowCount(1)
                .tryShow(mGameTipsManager);
    }

    private void removeNoAccSrollTipsView() {
        mGameTipsManager.dismiss(TAG_NOACC_SROLL_TIP_VIEW, getActivity());
    }

    private void removeGrabSelfSingTipView() {
        mGameTipsManager.dismiss(TAG_SELF_SING_TIP_VIEW, getActivity());
    }

    private void removeBurstTipView() {
        mGameTipsManager.dismiss(TAG_BURST_TIP_VIEW, getActivity());
    }

    private void removeGrabTipView() {
        mGameTipsManager.dismiss(TAG_GRAB_ROB_TIP_VIEW, getActivity());
    }

    private void removeInviteTipView() {
        mGameTipsManager.dismiss(TAG_INVITE_TIP_VIEW, getActivity());
    }

    private void removeManageSongTipView() {
        mGameTipsManager.dismiss(TAG_MANAGE_SONG_TIP_VIEW, getActivity());
    }

    private void removeChallengeTipView() {
        mGameTipsManager.dismiss(TAG_CHANLLENGE_TIP_VIEW, getActivity());
    }

//    private boolean isActivityExit() {
//        Activity activity = getActivity();
//        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
//            return true;
//        }
//        return false;
//    }


    @Override
    public void onStart() {
        super.onStart();
        //mSkrAudioPermission.ensurePermission(null, true);
    }

//    private void initCountDownView() {
//        mRedPkgView = (RedPkgCountDownView) mRootView.findViewById(R.id.red_pkg_view);
//    }

    private void initBgView() {
        mGrabRoomBgFlag = mRootView.findViewById(R.id.grab_room_bg_flag);
        if (mRoomData.getRoomType() == GrabRoomType.ROOM_TYPE_SECRET) {
            mGrabRoomBgFlag.setImageResource(R.drawable.fz_bj_simi);
        } else if (mRoomData.getRoomType() == GrabRoomType.ROOM_TYPE_FRIEND) {
            mGrabRoomBgFlag.setImageResource(R.drawable.fz_bj_haoyou);
        } else if (mRoomData.getRoomType() == GrabRoomType.ROOM_TYPE_COMMON) {
            //mGrabRoomBgFlag.setImageResource(R.drawable.fz_bj_simi);
        } else if (mRoomData.getRoomType() == GrabRoomType.ROOM_TYPE_PUBLIC) {
            mGrabRoomBgFlag.setImageResource(R.drawable.fz_bj_haohua);
        }
    }

    private void initGiftPanelView() {
        mGiftPanelView = (GiftPanelView) mRootView.findViewById(R.id.gift_panel_view);
        mGiftPanelView.setGrabRoomData(mRoomData);
        mContinueSendView = (ContinueSendView) mRootView.findViewById(R.id.continue_send_view);
        mContinueSendView.setBaseRoomData(mRoomData);
        mContinueSendView.setObserver(new ContinueSendView.OnVisibleStateListener() {
            @Override
            public void onVisible(boolean isVisible) {
                mBottomContainerView.setOpVisible(!isVisible);
            }
        });

        mGiftPanelView.setIGetGiftCountDownListener(new GiftDisplayView.IGetGiftCountDownListener() {
            @Override
            public long getCountDownTs() {
//                return mGiftTimerPresenter.getCountDownSecond();
                return 0;
            }
        });
    }

    private void initVideoView() {
        {
            ViewStub viewStub = mRootView.findViewById(R.id.grab_video_display_view_stub);
            mGrabVideoDisplayView = new GrabVideoDisplayView(viewStub, mRoomData);
            mGrabVideoDisplayView.setSelfSingCardListener(new SelfSingCardView.Listener() {
                @Override
                public void onSelfSingOver() {
                    mCorePresenter.sendRoundOverInfo();
                }
            });
            mGrabVideoDisplayView.setListener(new GrabVideoDisplayView.Listener() {
                @Override
                public void clickBeautyBtn() {
                    if (mBeautyControlPanelView != null) {
                        mBeautyControlPanelView.show();
                    }
                }
            });
        }
        mGrabVideoSelfSingCardView = new GrabVideoSelfSingCardView(mRootView, mRoomData);

        {
            ViewStub viewStub = mRootView.findViewById(R.id.grab_beauty_control_panel_view_stub);
            mBeautyControlPanelView = new BeautyControlPanelView(viewStub);
        }
    }

    private void initInputView() {
        mInputContainerView = mRootView.findViewById(R.id.input_container_view);
        mInputContainerView.setRoomData(mRoomData);
    }

    private void initBottomView() {
        {
            ViewStub viewStub = mRootView.findViewById(R.id.grab_voice_record_tip_view_stub);
            mVoiceRecordTipsView = new VoiceRecordTipsView(viewStub);
        }

        mBottomBgVp = mRootView.findViewById(R.id.bottom_bg_vp);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mBottomBgVp.getLayoutParams();
        /**
         * 按比例适配手机
         */
        lp.height = U.getDisplayUtils().getScreenHeight() * 284 / 667;

        mBottomContainerView = (GrabBottomContainerView) mRootView.findViewById(R.id.bottom_container_view);
        mBottomContainerView.setListener(new BottomContainerView.Listener() {
            @Override
            public void showInputBtnClick() {
                if (mPersonInfoDialog != null && mPersonInfoDialog.isShowing()) {
                    mPersonInfoDialog.dismiss();
                }
                mInputContainerView.showSoftInput();
            }

            @Override
            public void clickRoomManagerBtn() {
//                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(GrabRoomFragment.this.getActivity(), OwnerManageFragment.class)
//                        .setAddToBackStack(true)
//                        .setHasAnimation(true)
//                        .setEnterAnim(R.anim.slide_right_in)
//                        .setExitAnim(R.anim.slide_right_out)
//                        .addDataBeforeAdd(0, mRoomData)
//                        .build());
                SongManagerActivity.Companion.open(getActivity(), mRoomData);
                removeManageSongTipView();
            }

            @Override
            public void showGiftPanel() {
                if (mRoomData.getRealRoundInfo() != null) {
                    GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
                    if (now != null) {
                        if (now.isPKRound() && now.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
                            if (now.getsPkRoundInfoModels().size() == 2) {
                                int userId = now.getsPkRoundInfoModels().get(1).getUserID();
                                mGiftPanelView.show(RoomDataUtils.getPlayerInfoById(mRoomData, userId));
                            } else {
                                mGiftPanelView.show(RoomDataUtils.getPlayerInfoById(mRoomData, now.getUserID()));
                            }
                        } else {
                            mGiftPanelView.show(RoomDataUtils.getPlayerInfoById(mRoomData, now.getUserID()));
                        }
                    } else {
                        mGiftPanelView.show(null);
                    }
                } else {
                    mGiftPanelView.show(null);
                }

                mContinueSendView.setVisibility(GONE);
            }
        });
        mBottomContainerView.setRoomData(mRoomData);

        mVoiceRecordUiController = new VoiceRecordUiController(mBottomContainerView.getMVoiceRecordBtn(), mVoiceRecordTipsView);
    }

    private void initCommentView() {
        mCommentView = mRootView.findViewById(R.id.comment_view);
        mCommentView.setListener(new CommentViewItemListener() {
            @Override
            public void clickAvatar(int userId) {
                showPersonInfoView(userId);
            }
        });
        mCommentView.setRoomData(mRoomData);
//        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mCommentView.getLayoutParams();
//        layoutParams.height = U.getDisplayUtils().getPhoneHeight() - U.getDisplayUtils().dip2px(430 + 60);
    }

    private void initChangeRoomTransitionView() {
        mGrabChangeRoomTransitionView = mRootView.findViewById(R.id.change_room_transition_view);
        mGrabChangeRoomTransitionView.setVisibility(GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSkrAudioPermission.onBackFromPermisionManagerMaybe(getActivity());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ShowPersonCardEvent event) {
        showPersonInfoView(event.getUid());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(BuyGiftEvent event) {
        mContinueSendView.startBuy(event.getBaseGift(), event.getReceiver());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabWantInviteEvent event) {
        // 房主想要邀请别人加入游戏
        // 打开邀请面板
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), InviteFriendFragment2.class)
                .setAddToBackStack(true)
                .setHasAnimation(true)
                .addDataBeforeAdd(0, InviteFriendFragment2.FROM_GRAB_ROOM)
                .addDataBeforeAdd(1, mRoomData.getGameId())
                .addDataBeforeAdd(2, mRoomData.isVideoRoom() ? EMsgRoomMediaType.EMR_VIDEO.getValue() : EMsgRoomMediaType.EMR_AUDIO.getValue())
                .addDataBeforeAdd(3, mRoomData.getTagId())
                .build()
        );

        removeInviteTipView();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ChangeTagSuccessEvent event) {
        mRoomData.setSpecialModel(event.getSpecialModel());
        mRoomData.setTagId(event.getSpecialModel().getTagID());
    }

    private void showPersonInfoView(int userID) {
        if (!U.getNetworkUtils().hasNetwork()) {
            U.getToastUtil().showShort("网络异常，请检查网络后重试!");
            return;
        }
        mInputContainerView.hideSoftInput();

        boolean mShowKick;
        if (mRoomData.getRoomType() == GrabRoomType.ROOM_TYPE_COMMON) {
            // 普通房
            mShowKick = true;
        } else {
            if (mRoomData.isOwner()) {
                mShowKick = true;
            } else {
                mShowKick = false;
            }
        }

        mPersonInfoDialog = new PersonInfoDialog.Builder(getActivity(), QuickFeedbackFragment.FROM_GRAB_ROOM, userID, mShowKick, true)
                .setRoomID(mRoomData.getGameId())
                .setInviteDoubleListener(new PersonInfoDialog.InviteDoubleListener() {
                    @Override
                    public void onClickDoubleInvite(UserInfoModel userInfoModel) {
                        if (userInfoModel.isFriend()) {
                            mDoubleRoomInvitePresenter.inviteToDoubleRoom(userInfoModel.getUserId());
                        } else {
                            UserInfoManager.getInstance().checkIsFans((int) MyUserInfoManager.getInstance().getUid(), userInfoModel.getUserId(), new UserInfoManager.ResponseCallBack<Boolean>() {
                                @Override
                                public void onServerSucess(Boolean isFans) {
                                    if (isFans) {
                                        mDoubleRoomInvitePresenter.inviteToDoubleRoom(userInfoModel.getUserId());
                                    } else {
                                        mTipsDialogView = new TipsDialogView.Builder(U.getActivityUtils().getTopActivity())
                                                .setMessageTip("对方不是您的好友或粉丝\n要花2金币邀请ta加入双人唱聊房吗？")
                                                .setConfirmTip("邀请")
                                                .setCancelTip("取消")
                                                .setConfirmBtnClickListener(new AnimateClickListener() {
                                                    @Override
                                                    public void click(View view) {
                                                        mDoubleRoomInvitePresenter.inviteToDoubleRoom(userInfoModel.getUserId());
                                                        mTipsDialogView.dismiss();
                                                    }
                                                })
                                                .setCancelBtnClickListener(new AnimateClickListener() {
                                                    @Override
                                                    public void click(View view) {
                                                        mTipsDialogView.dismiss();
                                                    }
                                                })
                                                .build();
                                        mTipsDialogView.showByDialog();
                                    }
                                }

                                @Override
                                public void onServerFailed() {

                                }
                            });
                        }
                    }
                })
                .setKickListener(new PersonInfoDialog.KickListener() {

                    @Override
                    public void onClickKick(UserInfoModel userInfoModel) {
                        showKickConfirmDialog(userInfoModel);
                    }
                })
                .build();
        mPersonInfoDialog.show();
    }

    @Override
    public void getCashSuccess(float cash) {
        DialogPlus dialogPlus = DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(R.layout.congratulation_get_cash_view_layout))
                .setGravity(Gravity.CENTER)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.black_trans_80)
                .setMargin(0, 0, 0, 0)
                .setExpanded(false)
                .setCancelable(true)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogPlus dialog, @NonNull View view) {
                        dialog.dismiss();
                    }
                })
                .create();

        TextView textView = (TextView) dialogPlus.findViewById(R.id.tv_cash);
        textView.setText(String.valueOf(cash));
        dialogPlus.show();
    }

    DialogPlus mGetRedPkgFailedDialog;

    @Override
    public void showGetRedPkgFailed() {
        TipsDialogView tipsDialogView = new TipsDialogView.Builder(getContext())
                .setMessageTip("注册账号行为异常，红包激活不成功")
                .setOkBtnTip("确定")
                .setOkBtnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mGetRedPkgFailedDialog != null) {
                            mGetRedPkgFailedDialog.dismiss();
                        }
                    }
                })
                .build();

        if (mGetRedPkgFailedDialog == null) {
            mGetRedPkgFailedDialog = DialogPlus.newDialog(getContext())
                    .setContentHolder(new ViewHolder(tipsDialogView))
                    .setGravity(Gravity.BOTTOM)
                    .setContentBackgroundResource(R.color.transparent)
                    .setOverlayBackgroundResource(R.color.black_trans_80)
                    .setExpanded(false)
                    .create();
        }

        mGetRedPkgFailedDialog.show();
    }

    private void initTopView() {
        mGrabTopOpView = mRootView.findViewById(R.id.grab_top_op_view);
        mGrabTopOpView.setRoomData(mRoomData);
        mGrabTopContentView = mRootView.findViewById(R.id.grab_top_content_view);
        mGrabTopContentView.setRoomData(mRoomData);
        mGrabTopOpView.setListener(new GrabTopOpView.Listener() {
            @Override
            public void changeRoom() {
                GrabRoundInfoModel grabRoundInfoModel = mRoomData.getRealRoundInfo();
                if (grabRoundInfoModel != null) {
                    for (WantSingerInfo wantSingerInfo :
                            grabRoundInfoModel.getWantSingInfos()) {
                        if (wantSingerInfo.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                            U.getToastUtil().showShort("演唱时不能切换房间哦～");
                            return;
                        }
                    }
                }

                mBeginChangeRoomTs = System.currentTimeMillis();
                mGrabChangeRoomTransitionView.setVisibility(View.VISIBLE);
                mCorePresenter.changeRoom();
                mGrabGiveupView.hideWithAnimation(false);
            }

            @Override
            public void onClickVoiceAudition() {
                U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                if (mGrabVoiceControlPanelView == null) {
                    mGrabVoiceControlPanelView = new GrabVoiceControlPanelView(getContext());
                    mGrabVoiceControlPanelView.setRoomData(mRoomData);
                }
                mGrabVoiceControlPanelView.bindData();
                if (mVoiceControlDialog == null) {
                    mVoiceControlDialog = DialogPlus.newDialog(getContext())
                            .setContentHolder(new ViewHolder(mGrabVoiceControlPanelView))
                            .setContentBackgroundResource(R.color.transparent)
                            .setOverlayBackgroundResource(R.color.black_trans_50)
                            .setExpanded(false)
                            .setCancelable(true)
                            .setGravity(Gravity.BOTTOM)
                            .create();
                }
                mVoiceControlDialog.show();
            }

            @Override
            public void onClickFeedBack() {
                U.getFragmentUtils().addFragment(
                        FragmentUtils.newAddParamsBuilder(getActivity(), QuickFeedbackFragment.class)
                                .setAddToBackStack(true)
                                .setHasAnimation(true)
                                .addDataBeforeAdd(0, QuickFeedbackFragment.FROM_RANK_ROOM)
                                .addDataBeforeAdd(1, QuickFeedbackFragment.FEED_BACK)
                                .setEnterAnim(R.anim.slide_in_bottom)
                                .setExitAnim(R.anim.slide_out_bottom)
                                .build());
            }

            @Override
            public void closeBtnClick() {
                if (mRoomData.isOwner() && mRoomData.getPlayerInfoList().size() >= 2) {
                    quitGame();
                } else {
                    mCorePresenter.exitRoom("closeBtnClick");
                }
            }

            @Override
            public void onVoiceChange(boolean voiceOpen) {
                mCorePresenter.muteAllRemoteAudioStreams(!voiceOpen, true);
//            if (!voiceOpen) {
//                StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
//                        "game_muteon", null);
//            }
            }

            @Override
            public void onClickGameRule() {
                if (mGameRuleDialog != null) {
                    mGameRuleDialog.dismiss();
                }
                U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
                mGameRuleDialog = DialogPlus.newDialog(getContext())
                        .setContentHolder(new ViewHolder(R.layout.grab_game_rule_view_layout))
                        .setContentBackgroundResource(R.color.transparent)
                        .setOverlayBackgroundResource(R.color.black_trans_50)
                        .setExpanded(false)
                        .setGravity(Gravity.CENTER)
                        .create();
                mGameRuleDialog.show();
            }

            @Override
            public void onClickCamera() {
                if (mRoomData.isVideoRoom()) {
                    GrabRoundInfoModel grabRoundInfoModel = mRoomData.getRealRoundInfo();
                    if (grabRoundInfoModel != null) {
                        for (WantSingerInfo wantSingerInfo :
                                grabRoundInfoModel.getWantSingInfos()) {
                            if (wantSingerInfo.getUserID() == MyUserInfoManager.getInstance().getUid()) {
                                return;
                            }
                        }
                    }
                    // 进入视频预览 判断是否实名验证过
                    mSkrCameraPermission.ensurePermission(new Runnable() {
                        @Override
                        public void run() {
                            ARouter.getInstance()
                                    .build(RouterConstants.ACTIVITY_BEAUTY_PREVIEW)
                                    .withInt("mFrom", JumpBeautyFromKt.FROM_IN_GRAB_ROOM)
                                    .navigation();
                        }
                    }, true);
                } else {
                    U.getToastUtil().showShort("只在视频房间才能开启视频设置");
                }
            }
        });
        mGrabTopContentView.setListener(new GrabTopContentView.Listener() {
            @Override
            public void clickArrow(boolean open) {
                if (open) {
                    mGrabWidgetAnimationController.open();
                } else {
                    mGrabWidgetAnimationController.close();
                }
            }
        });
        mPracticeFlagIv = mRootView.findViewById(R.id.practice_flag_iv);
        // 加上状态栏的高度
        int statusBarHeight = U.getStatusBarUtil().getStatusBarHeight(getContext());

        {
            RelativeLayout.LayoutParams topLayoutParams = (RelativeLayout.LayoutParams) mGrabTopOpView.getLayoutParams();
            topLayoutParams.topMargin = statusBarHeight + topLayoutParams.topMargin;
        }
        {
            RelativeLayout.LayoutParams topLayoutParams = (RelativeLayout.LayoutParams) mGrabTopContentView.getLayoutParams();
            topLayoutParams.topMargin = statusBarHeight + topLayoutParams.topMargin;
        }
    }

    @Override
    public void updateGiftCount(int count, long ts) {
        UpdateMeiGuiFreeCountEvent.sendEvent(count, ts);
    }

    /**
     * 转场动画相关初始化
     */
    private void initTurnChangeView() {
        mTurnInfoCardView = mRootView.findViewById(R.id.turn_info_iv);
        mSongInfoCardView = mRootView.findViewById(R.id.turn_change_song_info_card_view);
        {
            ViewStub viewStub = mRootView.findViewById(R.id.grab_sing_begin_tips_card_stub);
            mSingBeginTipsCardView = new SingBeginTipsCardView(viewStub, mRoomData);
        }
        mRoundOverCardView = new RoundOverCardView(mRootView, mRoomData);
        mGrabGameOverView = mRootView.findViewById(R.id.grab_game_over_view);
    }

    private void initGiftDisplayView() {
        mGiftContinueViewGroup = mRootView.findViewById(R.id.gift_continue_vg);
        mGiftContinueViewGroup.setRoomData(mRoomData);
        GiftOverlayAnimationViewGroup giftOverlayAnimationViewGroup = mRootView.findViewById(R.id.gift_overlay_animation_vg);
        giftOverlayAnimationViewGroup.setRoomData(mRoomData);
        GiftBigAnimationViewGroup giftBigAnimationViewGroup = mRootView.findViewById(R.id.gift_big_animation_vg);
        giftBigAnimationViewGroup.setRoomData(mRoomData);

        GiftBigContinuousView giftBigContinueView = mRootView.findViewById(R.id.gift_big_continue_view);
        giftBigAnimationViewGroup.setGiftBigContinuousView(giftBigContinueView);

        mDengBigAnimation = (GrabDengBigAnimationView) mRootView.findViewById(R.id.deng_big_animation);
    }

    private void initScoreView() {
        mGrabScoreTipsView = mRootView.findViewById(R.id.grab_score_tips_view);
        mGrabScoreTipsView.setRoomData(mRoomData);
    }

    private void initGrabOpView() {
        mGrabOpBtn = mRootView.findViewById(R.id.grab_op_btn);
        mGrabOpBtn.setGrabRoomData(mRoomData);
        mGrabOpBtn.setListener(new GrabOpView.Listener() {
            @Override
            public void clickGrabBtn(int seq, boolean challenge) {
                mSkrAudioPermission.ensurePermission(getActivity(), new Runnable() {
                    @Override
                    public void run() {
                        Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                U.getSoundUtils().play(TAG, R.raw.grab_iwannasing);
                                mCorePresenter.grabThisRound(seq, challenge);
                            }
                        };
                        if (mRoomData.isVideoRoom()) {
                            mSkrCameraPermission.ensurePermission(getActivity(), r, true);
                        } else {
                            r.run();
                        }
                    }
                }, true);
            }

            @Override
            public void clickLightOff() {
                mCorePresenter.lightsOff();
            }

            @Override
            public void grabCountDownOver() {
                mCorePresenter.sendMyGrabOver("grabCountDownOver");
                mGrabGiveupView.hideWithAnimation(false);
            }

            @Override
            public void countDownOver() {

            }

            @Override
            public void clickBurst(int seq) {
                mCorePresenter.lightsBurst();
            }

            @Override
            public void showChallengeTipView() {
                tryShowChallengeTipView();
            }

            @Override
            public void hideChallengeTipView() {
                removeChallengeTipView();
            }

            @Override
            public void showGrabTipView() {
                tryShowGrabTipView();
            }

            @Override
            public void hideGrabTipView() {
                removeGrabTipView();
            }

            @Override
            public void showBurstTipView() {
                tryShowBurstTipView();
            }

            @Override
            public void hideBurstTipView() {
                removeBurstTipView();
            }
        });

        mGrabOpBtn.hide("initGrabOpView");

        mGrabGiveupView = (GrabGiveupView) mRootView.findViewById(R.id.grab_giveup_view);
        mGrabGiveupView.setListener(new GrabGiveupView.Listener() {
            @Override
            public void giveUp(boolean ownerControl) {
                GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
//                if (infoModel != null) {
//                    HashMap map = new HashMap();
//                    map.put("songId2", String.valueOf(infoModel.getMusic().getItemID()));
//                    StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
//                            "give_up_sing", map);
//                }
                mCorePresenter.giveUpSing(ownerControl);
            }
        });
        mGrabGiveupView.hideWithAnimation(false);

        mMiniOwnerMicIv = mRootView.findViewById(R.id.mini_owner_mic_iv);
        mMiniOwnerMicIv.setOnClickListener(new DebounceViewClickListener() {
            boolean mute = true;

            @Override
            public void clickValid(View v) {
                mute = !mute;
                if (mute) {
                    mMiniOwnerMicIv.setImageResource(R.drawable.mini_owner_mute);
                } else {
                    mMiniOwnerMicIv.setImageResource(R.drawable.mini_owner_normal);
                }
                mCorePresenter.miniOwnerMic(mute);
            }
        });
    }

    private void initSingStageView() {
        mSelfSingCardView = new SelfSingCardView(mRootView, mRoomData);
        mSelfSingCardView.setListener(new SelfSingCardView.Listener() {
            @Override
            public void onSelfSingOver() {
                removeNoAccSrollTipsView();
                removeGrabSelfSingTipView();
                mCorePresenter.sendRoundOverInfo();
            }
        });
        mSelfSingCardView.setListener4FreeMic(new SelfSingCardView.Listener() {
            @Override
            public void onSelfSingOver() {
                mCorePresenter.sendMyGrabOver("onSelfSingOver");
            }
        });
        mOthersSingCardView = new OthersSingCardView(mRootView, mRoomData);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabSomeOneLightOffEvent event) {
        // 灭灯
        if (event.getUid() == MyUserInfoManager.getInstance().getUid()) {
            U.getSoundUtils().play(TAG, R.raw.grab_xlight);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ShowHalfRechargeFragmentEvent event) {
        IHomeService channelService = (IHomeService) ARouter.getInstance().build(RouterConstants.SERVICE_HOME).navigation();
        Class<BaseFragment> baseFragmentClass = (Class) channelService.getData(2, null);
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(getActivity(), baseFragmentClass)
                        .setEnterAnim(R.anim.slide_in_bottom)
                        .setExitAnim(R.anim.slide_out_bottom)
                        .setAddToBackStack(true)
                        .setFragmentDataListener(new FragmentDataListener() {
                            @Override
                            public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {
                                //充值成功
                                if (requestCode == 100 && resultCode == 0) {
                                    mGiftPanelView.updateZS();
                                    mGiftPanelView.show(RoomDataUtils.getPlayerInfoById(mRoomData, mRoomData.getRealRoundInfo().getUserID()));
                                }
                            }
                        })
                        .setHasAnimation(true)
                        .build());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabSomeOneLightBurstEvent event) {
        // 爆灯
        if (RoomDataUtils.isMyRound(mRoomData.getRealRoundInfo())) {
            // 当前我是演唱者
            mDengBigAnimation.setTranslationY(U.getDisplayUtils().dip2px(200));
            mDengBigAnimation.playBurstAnimation(event.uid == MyUserInfoManager.getInstance().getUid());
        } else {
            mDengBigAnimation.playBurstAnimation(event.uid == MyUserInfoManager.getInstance().getUid());
        }
    }

    private void removeAllEnsureMsg() {
        mUiHanlder.removeMessages(MSG_ENSURE_SONGCARD_OVER);
        mUiHanlder.removeMessages(MSG_ENSURE_SING_BEGIN_TIPS_OVER);
        mUiHanlder.removeMessages(MSG_ENSURE_ROUND_OVER_PLAY_OVER);
        mUiHanlder.removeMessages(MSG_ENSURE_BATTLE_BEGIN_OVER);
    }

//    private void onReadyGoOver() {
//        MyLog.w(TAG, "onReadyGoOver");
//        mUiHanlder.removeMessages(MSG_ENSURE_READYGO_OVER);
//        mCorePresenter.onOpeningAnimationOver();
//    }

    private void onBattleBeginPlayOver() {
        mUiHanlder.removeMessages(MSG_ENSURE_BATTLE_BEGIN_OVER);
        mTurnInfoCardView.setVisibility(GONE);
        mCorePresenter.onOpeningAnimationOver();
    }


    /**
     * 抢唱阶段开始
     *
     * @param seq       当前轮次的序号
     * @param songModel 要唱的歌信息
     */
    @Override
    public void grabBegin(int seq, SongModel songModel) {
        MyLog.d(TAG, "grabBegin" + " seq=" + seq + " songModel=" + songModel);
        removeAllEnsureMsg();
        if (mOwnerBeginGameIv != null) {
            // 如果房主开始游戏的按钮还在的话，将其移除
            mGrabRootView.removeView(mOwnerBeginGameIv);
        }
        // 播放3秒导唱
        mOthersSingCardView.setVisibility(GONE);
        mSelfSingCardView.setVisibility(GONE);
        mMiniOwnerMicIv.setVisibility(GONE);
        mGrabBaseUiController.grabBegin();
        PendingPlaySongCardData pendingPlaySongCardData = new PendingPlaySongCardData(seq, songModel);
        Message msg = mUiHanlder.obtainMessage(MSG_ENSURE_SONGCARD_OVER);
        msg.obj = pendingPlaySongCardData;
        if (seq == 1) {
            mUiHanlder.sendMessageDelayed(msg, 5000);
            mUiHanlder.postDelayed(new Runnable() {
                @Override
                public void run() {
                    U.getSoundUtils().play(TAG, R.raw.grab_readygo);
                }
            }, 100);
        } else {
            mUiHanlder.sendMessageDelayed(msg, 2000);
        }

        GrabRoundInfoModel grabRoundInfoModel = mRoomData.getRealRoundInfo();
        if (grabRoundInfoModel != null && !grabRoundInfoModel.isParticipant() && grabRoundInfoModel.getEnterStatus() == EQRoundStatus.QRS_INTRO.getValue()) {
            mTurnInfoCardView.setVisibility(GONE);
            onSongInfoCardPlayOver("中途进来", pendingPlaySongCardData);
        } else {
            mTurnInfoCardView.setModeSongSeq(seq == 1, new SVGAListener() {
                @Override
                public void onFinished() {
                    mTurnInfoCardView.setVisibility(GONE);
                    onSongInfoCardPlayOver("动画结束进来", pendingPlaySongCardData);
                }
            });
        }
        mGrabTopContentView.setModeGrab();
    }

    void onSongInfoCardPlayOver(String from, PendingPlaySongCardData pendingPlaySongCardData) {
        MyLog.d(TAG, "onSongInfoCardPlayOver" + " pendingPlaySongCardData=" + pendingPlaySongCardData + " from=" + from);
        mUiHanlder.removeMessages(MSG_ENSURE_SONGCARD_OVER);
        mSingBeginTipsCardView.setVisibility(GONE);
        mGrabGiveupView.hideWithAnimation(false);
        GrabRoundInfoModel grabRoundInfoModel = mRoomData.getRealRoundInfo();
        if (grabRoundInfoModel != null && grabRoundInfoModel.isFreeMicRound()) {
            // 自由麦环节 直接显示卡片了
            mSelfSingCardView.playLyric();
            mGrabOpBtn.hide("onSongInfoCardPlayOver2");
            if (mRoomData.isOwner()) {
                mGrabGiveupView.delayShowGiveUpView(true);
            }
        } else {
            mSongInfoCardView.bindSongModel(mRoomData.getRealRoundSeq(), mRoomData.getGrabConfigModel().getTotalGameRoundSeq(), pendingPlaySongCardData.songModel);
            if (grabRoundInfoModel != null && !grabRoundInfoModel.isParticipant() && grabRoundInfoModel.getEnterStatus() == EQRoundStatus.QRS_INTRO.getValue()) {
                MyLog.d(TAG, "这轮刚进来，不播放导唱过场");
                mGrabOpBtn.hide("onSongInfoCardPlayOver1");
            } else {
                if (mRoomData.isInPlayerList()) {
                    mGrabOpBtn.playCountDown(pendingPlaySongCardData.getSeq(), 4, pendingPlaySongCardData.songModel);
                }
            }
        }
        mCorePresenter.playGuide();
    }

    @Override
    public void singBySelf() {
        removeAllEnsureMsg();
        mCorePresenter.stopGuide();
        GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
        // 第二轮不播这个动画
        mGrabTopContentView.setModeSing();
        mSongInfoCardView.hide();

        mSingBeginTipsCardView.setVisibility(View.VISIBLE);

        mGrabOpBtn.hide("singBySelf");
        mGrabOpBtn.setGrabPreRound(true);

        Message msg = mUiHanlder.obtainMessage(MSG_ENSURE_SING_BEGIN_TIPS_OVER);
        mUiHanlder.sendMessageDelayed(msg, 4000);

        if (now != null && now.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
            // pk的第二轮，没有 vs 的演唱开始提示了
            onSingBeginTipsPlayOver();
        } else {
            singBeginTipsPlay(new Runnable() {
                @Override
                public void run() {
                    onSingBeginTipsPlayOver();
                }
            });
        }

        StatisticsAdapter.recordCountEvent("grab", "game_sing", null);
    }

    @Override
    public void singByOthers() {
        removeAllEnsureMsg();
        mCorePresenter.stopGuide();
        mGrabTopContentView.setModeSing();
        mSongInfoCardView.hide();
        mGrabOpBtn.hide("singByOthers");
        mGrabOpBtn.setGrabPreRound(false);
        mGrabGiveupView.hideWithAnimation(false);
        mSingBeginTipsCardView.setVisibility(View.VISIBLE);

        Message msg = mUiHanlder.obtainMessage(MSG_ENSURE_SING_BEGIN_TIPS_OVER);
        mUiHanlder.sendMessageDelayed(msg, 2600);

        GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
        if (now != null && now.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
            // pk的第二轮，没有 vs 的演唱开始提示了
            if (now != null && now.isParticipant()
                    && mRoomData.isInPlayerList()
                    && !RoomDataUtils.isRoundSinger(now, MyUserInfoManager.getInstance().getUid())) {
                // 不是参与者 不是选手 如果是pk的轮次 pk的参与者 都没有爆灭灯按钮
                mGrabOpBtn.toOtherSingState();
            } else {
                mGrabOpBtn.hide("singByOthers2");
            }
            onSingBeginTipsPlayOver();
        } else {
            singBeginTipsPlay(new Runnable() {
                @Override
                public void run() {
                    GrabRoundInfoModel grabRoundInfoModel = mRoomData.getRealRoundInfo();
                    if (grabRoundInfoModel != null
                            && grabRoundInfoModel.isParticipant()
                            && mRoomData.isInPlayerList()
                            && !RoomDataUtils.isRoundSinger(now, MyUserInfoManager.getInstance().getUid())
                            && !(grabRoundInfoModel.isMiniGameRound() && mRoomData.isOwner())
                    ) {
                        // 参与者 & 游戏列表中 & 不是本轮演唱者 &  不是小游戏中的房主
                        mGrabOpBtn.toOtherSingState();
                    } else {
                        mGrabOpBtn.hide("singByOthers2");
                    }
                    onSingBeginTipsPlayOver();
                }
            });
        }
    }

    private void singBeginTipsPlay(Runnable runnable) {
        GrabRoundInfoModel grabRoundInfoModel = mRoomData.getRealRoundInfo();
        if (grabRoundInfoModel != null) {
            if (!grabRoundInfoModel.isParticipant() && grabRoundInfoModel.isEnterInSingStatus()) {
                MyLog.d(TAG, " 进入时已经时演唱阶段了，则不用播卡片了");
                runnable.run();
            } else {
                mSingBeginTipsCardView.bindData(new SVGAListener() {
                    @Override
                    public void onFinished() {
                        runnable.run();
                    }
                });
            }
        } else {
            MyLog.w(TAG, "singBeginTipsPlay" + " grabRoundInfoModel = null ");
        }
    }

    private void onSingBeginTipsPlayOver() {
        MyLog.d(TAG, "onSingBeginTipsPlayOver");
        mUiHanlder.removeMessages(MSG_ENSURE_SING_BEGIN_TIPS_OVER);
        mSingBeginTipsCardView.setVisibility(GONE);
        mGrabScoreTipsView.reset();
        GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
        if (now != null) {
            if (now.singBySelf()) {
                mGrabGiveupView.delayShowGiveUpView(false);
                mCorePresenter.beginSing();
                mGrabBaseUiController.singBySelf();
            } else {
                if (mRoomData.isOwner() && now.isMiniGameRound() && !RoomDataUtils.isMyRound(mRoomData.getRealRoundInfo())) {
                    mMiniOwnerMicIv.setVisibility(View.VISIBLE);
                    mMiniOwnerMicIv.setImageResource(R.drawable.mini_owner_mute);
                    mGrabGiveupView.delayShowGiveUpView(true);
                } else {
                    mMiniOwnerMicIv.setVisibility(GONE);
                }
                mGrabBaseUiController.singByOthers();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LightOffAnimationOverEvent event) {
        //灭灯动画播放结束
        if (RoomDataUtils.isMyRound(mRoomData.getRealRoundInfo())) {

        } else {
            mOthersSingCardView.tryStartCountDown();
        }
    }

    @Override
    public void roundOver(GrabRoundInfoModel lastInfoModel, boolean playNextSongInfoCard, GrabRoundInfoModel now) {
        removeAllEnsureMsg();
        Message msg = mUiHanlder.obtainMessage(MSG_ENSURE_ROUND_OVER_PLAY_OVER);
        msg.arg1 = playNextSongInfoCard ? 1 : 0;
        msg.obj = now;
        mUiHanlder.sendMessageDelayed(msg, 4000);
        mMiniOwnerMicIv.setVisibility(GONE);
        removeNoAccSrollTipsView();
        removeGrabSelfSingTipView();
        mSongInfoCardView.hide();
        mGrabOpBtn.hide("roundOver");
        mGrabGiveupView.hideWithAnimation(false);
        mGrabBaseUiController.roundOver();
        if (lastInfoModel.isFreeMicRound()) {
            mSelfSingCardView.setVisibility(GONE);
        }
        mRoundOverCardView.bindData(lastInfoModel, new SVGAListener() {
            @Override
            public void onFinished() {
                onRoundOverPlayOver(playNextSongInfoCard, now);
            }
        });
    }

    private void onRoundOverPlayOver(boolean playNextSongInfoCard, BaseRoundInfoModel now) {
        mUiHanlder.removeMessages(MSG_ENSURE_ROUND_OVER_PLAY_OVER);
        mRoundOverCardView.setVisibility(GONE);
        if (playNextSongInfoCard) {
            grabBegin(now.getRoundSeq(), now.getMusic());
        }
    }

//    @Override
//    public void redPkgCountDown(long duration) {
//        mRedPkgView.setVisibility(View.VISIBLE);
//        mRedPkgView.startCountDown(duration);
//        mUiHanlder.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                mRedPkgView.setVisibility(View.GONE);
//            }
//        }, duration);
//    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mRoomData = (GrabRoomData) data;
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        MyLog.d(TAG, "destroy");
        dismissDialog();
        if (mQuitTipsDialog != null && mQuitTipsDialog.isShowing()) {
            mQuitTipsDialog.dismiss(false);
            mQuitTipsDialog = null;
        }
        mGrabAudioUiController.destroy();
        mGrabVideoUiController.destroy();
        mUiHanlder.removeCallbacksAndMessages(null);
        mIsGameEndAniamtionShow = false;
        if (mAnimatorList != null) {
            for (Animator animator : mAnimatorList) {
                if (animator != null) {
                    animator.cancel();
                }
            }
            mAnimatorList.clear();
        }

        if (mContinueSendView != null) {
            mContinueSendView.destroy();
        }

        if (mGiftPanelView != null) {
            mGiftPanelView.destroy();
        }

        mGameTipsManager.destory();

        if (mGrabWidgetAnimationController != null) {
            mGrabWidgetAnimationController.destroy();
        }
        U.getSoundUtils().release(TAG);
        BgMusicManager.getInstance().setRoom(false);
    }

    @Override
    protected boolean onBackPressed() {
        if (mInputContainerView.onBackPressed()) {
            return true;
        }

        if (mGiftPanelView.onBackPressed()) {
            return true;
        }
        if (mBeautyControlPanelView.onBackPressed()) {
            return true;
        }
        quitGame();
        return true;
    }

    @Override
    public void exitInRound() {

    }

    @Override
    public void updateUserState(List<OnlineInfoModel> jsonOnLineInfoList) {

    }

    private void quitGame() {
        dismissDialog();
        if (mQuitTipsDialog == null) {
            String msg = "提前退出会破坏其他玩家的对局体验\n确定退出么？";
            if (mRoomData.isOwner()) {
                msg = "房主退出后房间将解散,是否确认退出?";
            }
            TipsDialogView tipsDialogView = new TipsDialogView.Builder(getContext())
                    .setMessageTip(msg)
                    .setConfirmTip("确定")
                    .setCancelTip("取消")
                    .setConfirmBtnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mQuitTipsDialog != null) {
                                mQuitTipsDialog.dismiss(false);
                            }
                            mCorePresenter.exitRoom("quitGame tipsDialog");
//                            StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
//                                    "game_exit", null);
                        }
                    })
                    .setCancelBtnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mQuitTipsDialog != null) {
                                mQuitTipsDialog.dismiss();
                            }
                        }
                    })
                    .build();

            mQuitTipsDialog = DialogPlus.newDialog(getContext())
                    .setContentHolder(new ViewHolder(tipsDialogView))
                    .setGravity(Gravity.BOTTOM)
                    .setContentBackgroundResource(R.color.transparent)
                    .setOverlayBackgroundResource(R.color.black_trans_80)
                    .setExpanded(false)
                    .create();
        }
        mQuitTipsDialog.show();
    }

    @Override
    public void gameFinish() {
        MyLog.w(TAG, "游戏结束了");
//        mUiHanlder.removeMessages(MSG_ENSURE_GAME_OVER);
//        Message msg = mUiHanlder.obtainMessage(MSG_ENSURE_GAME_OVER);
//        mUiHanlder.sendMessageDelayed(msg, 4000);
        mSelfSingCardView.setVisibility(GONE);
        mOthersSingCardView.hide();
        mTurnInfoCardView.setVisibility(GONE);
        mSingBeginTipsCardView.setVisibility(GONE);
        mRoundOverCardView.setVisibility(GONE);
        mGrabGameOverView.setVisibility(View.VISIBLE);
        mGrabGameOverView.starAnimation(new SVGAListener() {
            @Override
            public void onFinished() {
                mCorePresenter.exitRoom("gameFinish");
                //onGrabGameOver("onFinished");
            }
        });
    }

    @Override
    public void onGetGameResult(boolean success) {
//        if (success) {
        onGrabGameOver("onGetGameResultSuccess");
//        } else {
//            if (getActivity() != null) {
//                getActivity().finish();
//            }
//        }
    }

    @Override
    public void onChangeRoomResult(boolean success, String errMsg) {
        long t = System.currentTimeMillis() - mBeginChangeRoomTs;
        if (t > 1500) {
            mGrabChangeRoomTransitionView.setVisibility(GONE);
            if (!success) {
                U.getToastUtil().showShort(errMsg);
            }
        } else {
            mUiHanlder.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!success) {
                        U.getToastUtil().showShort(errMsg);
                    }
                    mGrabChangeRoomTransitionView.setVisibility(GONE);
                }
            }, 1500 - t);
        }
        if (success) {
            hideAllCardView();
            // 重新决定显示mic按钮
            mBottomContainerView.setRoomData(mRoomData);
        }
    }

    @Override
    public void giveUpSuccess(int seq) {
        mGrabGiveupView.giveUpSuccess();
    }

    @Override
    public void updateScrollBarProgress(int score, int songLineNum) {
        mGrabScoreTipsView.updateScore(score, songLineNum);
    }

    private void onGrabGameOver(String from) {
        MyLog.w(TAG, "onGrabGameOver1 " + from);
//        mUiHanlder.removeMessages(MSG_ENSURE_GAME_OVER);
        Activity activity = getActivity();
        if (activity != null) {
            if (!activity.isDestroyed() && !activity.isFinishing()) {
                MyLog.w(TAG, "onGrabGameOver activity gogogo");
                ARouter.getInstance().build(RouterConstants.ACTIVITY_GRAB_RESULT)
                        .withSerializable("room_data", mRoomData)
                        .navigation();
                for (Activity activity1 : U.getActivityUtils().getActivityList()) {
                    if (activity1 instanceof GrabRoomActivity) {
                        activity1.finish();
                    }
                    if (activity1 instanceof SongManagerActivity) {
                        activity1.finish();
                    }
                }
//                StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
//                        StatConstants.KEY_GAME_FINISH, null);
            } else {
                MyLog.d(TAG, "onGrabGameOver activity hasdestroy");
            }
        } else {
            MyLog.w(TAG, "onGrabGameOver activity==null");
        }
    }

    // 确认踢人弹窗
    private void showKickConfirmDialog(UserInfoModel userInfoModel) {
        MyLog.d(TAG, "showKickConfirmDialog" + " userInfoModel=" + userInfoModel);
        dismissDialog();
        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
        int type;
        if (mRoomData.isOwner()) {
            if (mRoomData.getOwnerKickTimes() > 0) {
                type = ConfirmDialog.TYPE_OWNER_KICK_CONFIRM;
            } else {
                type = ConfirmDialog.TYPE_KICK_CONFIRM;
            }
        } else {
            type = ConfirmDialog.TYPE_KICK_CONFIRM;
        }

        if (mRoomData.isOwner()) {
            // 房主不消耗金币
            mGrabKickDialog = new ConfirmDialog(U.getActivityUtils().getTopActivity(), userInfoModel, type, 0);
        } else {
            mGrabKickDialog = new ConfirmDialog(U.getActivityUtils().getTopActivity(), userInfoModel, type, mRoomData.getGrabConfigModel().getKickUserConsumCoinCnt());
        }
        mGrabKickDialog.setListener(new ConfirmDialog.Listener() {
            @Override
            public void onClickConfirm(UserInfoModel userInfoModel) {
                // 发起踢人请求
                mCorePresenter.reqKickUser(userInfoModel.getUserId());
            }
        });
        mGrabKickDialog.show();
    }

    private void dismissDialog() {
        if (mGameRuleDialog != null) {
            mGameRuleDialog.dismiss(false);
        }
        if (mBottomContainerView != null) {
            mBottomContainerView.dismissPopWindow();
        }
        if (mPersonInfoDialog != null) {
            mPersonInfoDialog.dismiss();
        }
        if (mGrabKickDialog != null) {
            mGrabKickDialog.dismiss(false);
        }
        if (mVoiceControlDialog != null) {
            mVoiceControlDialog.dismiss(false);
        }
        if (mTipsDialogView != null) {
            mTipsDialogView.dismiss(false);
        }
    }

    // 请求踢人弹窗
    @Override
    public void showKickVoteDialog(int userId, int sourceUserId) {
        MyLog.d(TAG, "showKickReqDialog" + " userId=" + userId + " sourceUserId=" + sourceUserId);
        dismissDialog();
        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());

        GrabPlayerInfoModel playerInfoModel = RoomDataUtils.getPlayerInfoById(mRoomData, userId);
        if (playerInfoModel != null) {
            UserInfoModel userInfoModel = playerInfoModel.getUserInfo();
            mGrabKickDialog = new ConfirmDialog(U.getActivityUtils().getTopActivity(), userInfoModel, ConfirmDialog.TYPE_KICK_REQUEST, 5);
            mGrabKickDialog.setListener(new ConfirmDialog.Listener() {
                @Override
                public void onClickConfirm(UserInfoModel userInfoModel) {
                    // 同意踢人
                    mCorePresenter.voteKickUser(true, userId, sourceUserId);
                }
            });
            mGrabKickDialog.show();
        }
    }

    @Override
    public void kickBySomeOne(boolean isOwner) {
        MyLog.d(TAG, "kickBySomeOne" + " isOwner=" + isOwner);
        //onGrabGameOver("kickBySomeOne");
        U.getToastUtil().showSkrCustomLong(new CommonToastView.Builder(U.app())
                .setImage(R.drawable.touxiangshezhishibai_icon)
                .setText(isOwner ? "房主将你请出了房间" : "超过半数玩家请你出房间，要友好文明游戏哦~")
                .build());
        mCorePresenter.exitRoom("kickBySomeOne");
    }

    @Override
    public void dimissKickDialog() {
        if (mGrabKickDialog != null) {
            mGrabKickDialog.dismiss();
        }
    }

    @Override
    public void showPracticeFlag(boolean flag) {
        if (flag) {
            mPracticeFlagIv.setVisibility(View.VISIBLE);
        } else {
            mPracticeFlagIv.setVisibility(GONE);
        }
    }

    @Override
    public void hideInviteTipView() {
        removeInviteTipView();
    }

    @Override
    public void hideManageTipView() {
        removeManageSongTipView();
    }

    public void hideAllCardView() {
        mRoundOverCardView.setVisibility(GONE);
        mOthersSingCardView.setVisibility(GONE);
        mSelfSingCardView.setVisibility(GONE);
        mSingBeginTipsCardView.setVisibility(GONE);
    }

    @Override
    public void beginOuath() {
        ARouter.getInstance().build(RouterConstants.ACTIVITY_WEB)
                .withString("url", ApiManager.getInstance().findRealUrlByChannel("http://app.inframe.mobi/oauth?from=singer"))
                .greenChannel().navigation();
        getActivity().finish();
    }

    @Override
    public void changeRoomMode(boolean isVideo) {
        if (isVideo) {
            mGrabAudioUiController.stopWork();
            mGrabBaseUiController = mGrabVideoUiController;
        } else {
            mGrabVideoUiController.stopWork();
            mGrabBaseUiController = mGrabAudioUiController;
        }
    }

    static class PendingPlaySongCardData {
        int seq;
        SongModel songModel;

        public PendingPlaySongCardData(int seq, SongModel songModel) {
            this.seq = seq;
            this.songModel = songModel;
        }

        public int getSeq() {
            return seq;
        }

        @Override
        public String toString() {
            return "PendingPlaySongCardData{" +
                    "seq=" + seq +
                    ", songModel=" + songModel +
                    '}';
        }
    }

}
