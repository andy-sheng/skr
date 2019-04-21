package com.module.playways.grab.room.fragment;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.permission.SkrAudioPermission;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.log.MyLog;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.component.busilib.constans.GrabRoomType;
import com.component.busilib.manager.BgMusicManager;
import com.dialog.view.TipsDialogView;
import com.module.RouterConstants;
import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.event.GrabWantInviteEvent;
import com.module.playways.grab.room.event.LightOffAnimationOverEvent;
import com.module.playways.grab.room.event.ShowPersonCardEvent;
import com.module.playways.grab.room.event.GrabSomeOneLightBurstEvent;
import com.module.playways.grab.room.event.GrabSomeOneLightOffEvent;
import com.module.playways.grab.room.inter.IGrabView;
import com.module.playways.grab.room.invite.InviteFriendFragment;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.playways.grab.room.model.GrabPlayerInfoModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.model.WantSingerInfo;
import com.module.playways.grab.room.presenter.GrabCorePresenter;
import com.module.playways.grab.room.presenter.GrabRedPkgPresenter;
import com.module.playways.grab.room.songmanager.GrabSongManageFragment;
import com.module.playways.grab.room.top.GrabTopContainerView;
import com.module.playways.grab.room.top.GrabTopView;
import com.module.playways.grab.room.view.GrabChangeRoomTransitionView;
import com.module.playways.grab.room.view.GrabDengBigAnimationView;
import com.module.playways.grab.room.view.GrabGameOverView;
import com.module.playways.grab.room.view.GrabOpView;

import com.module.playways.grab.room.view.GrabGiveupView;
import com.module.playways.grab.room.view.GrabScoreTipsView;
import com.module.playways.grab.room.view.GrabVoiceControlPanelView;
import com.module.playways.grab.room.view.IRedPkgCountDownView;
import com.module.playways.grab.room.view.OthersSingCardView;
import com.module.playways.grab.room.view.RedPkgCountDownView;
import com.module.playways.grab.room.view.RoundOverCardView;
import com.module.playways.grab.room.view.SelfSingCardView2;
import com.module.playways.grab.room.view.SingBeginTipsCardView;
import com.module.playways.grab.room.view.SongInfoCardView;
import com.module.playways.grab.room.view.TurnInfoCardView;
import com.module.playways.room.prepare.model.OnlineInfoModel;
import com.module.playways.room.prepare.model.BaseRoundInfoModel;
import com.module.playways.room.room.comment.listener.CommentItemListener;
import com.module.playways.room.room.comment.CommentView;
import com.module.playways.room.room.gift.GiftBigAnimationViewGroup;
import com.module.playways.room.room.gift.GiftContinueViewGroup;
import com.module.playways.room.room.view.BottomContainerView;
import com.module.playways.room.room.view.InputContainerView;
import com.module.playways.room.song.model.SongModel;
import com.module.rank.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.zq.dialog.ConfirmDialog;
import com.zq.dialog.PersonInfoDialog;
import com.zq.toast.CommonToastView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class GrabRoomFragment extends BaseFragment implements IGrabView, IRedPkgCountDownView {

    public final static String TAG = "GrabRoomFragment";

    public final static String KEY_OWNER_SHOW_TIMES = "ownerShowTimes";

//    public static final int MSG_ENSURE_READYGO_OVER = 1;

    public static final int MSG_ENSURE_SONGCARD_OVER = 2;

    public static final int MSG_ENSURE_SING_BEGIN_TIPS_OVER = 3;

    public static final int MSG_ENSURE_ROUND_OVER_PLAY_OVER = 4;

    public static final int MSG_ENSURE_BATTLE_BEGIN_OVER = 5;

//    public static final int MSG_ENSURE_GAME_OVER = 6;

    //自己演唱玩
//    public static final int MSG_SEND_SELF_SING_END = 7;

    GrabRoomData mRoomData;

    RelativeLayout mRankingContainer;

    ExImageView mGrabRoomBgFlag;

    InputContainerView mInputContainerView;

    ViewGroup mBottomBgVp;
    BottomContainerView mBottomContainerView;

//    GrabVoiceControlPanelView mVoiceControlView;

    RedPkgCountDownView mRedPkgView;

    CommentView mCommentView;

    GrabTopContainerView mTopContainerView;// 顶部，抢唱阶段，以及非本人的演唱阶段

    ExImageView mPracticeFlagIv; // 练习中

    GrabCorePresenter mCorePresenter;

    GrabRedPkgPresenter mGrabRedPkgPresenter;

//    DownLoadScoreFilePresenter mDownLoadScoreFilePresenter;

    TurnInfoCardView mTurnInfoCardView; //歌曲次序 以及 对战开始卡片

    SongInfoCardView mSongInfoCardView; // 歌曲信息卡片

    SingBeginTipsCardView mSingBeginTipsCardView; // 提示xxx演唱开始的卡片

    RoundOverCardView mRoundOverCardView; // 轮次结束的卡片

    GrabOpView mGrabOpBtn; // 抢 倒计时 灭 等按钮

    GrabGiveupView mGrabGiveupView;

    OthersSingCardView mOthersSingCardView;

    SelfSingCardView2 mSelfSingCardView;

    GrabGameOverView mGrabGameOverView;

    GrabDengBigAnimationView mDengBigAnimation;

    GrabChangeRoomTransitionView mGrabChangeRoomTransitionView;

    GrabScoreTipsView mGrabScoreTipsView;

    DialogPlus mQuitTipsDialog;

    PersonInfoDialog mPersonInfoDialog;

    DialogPlus mGameRuleDialog;

    ConfirmDialog mGrabKickDialog;

    GrabVoiceControlPanelView mGrabVoiceControlPanelView;

    DialogPlus mVoiceControlDialog;

    List<Animator> mAnimatorList = new ArrayList<>();  //存放所有需要尝试取消的动画

    boolean mIsGameEndAniamtionShow = false; // 标记对战结束动画是否播放

    long mBeginChangeRoomTs;

    ImageView mOwnerBeginGameIv;

    ImageView mIvInviteTip;
    ImageView mIvManageSongTipView;
    ImageView mIvChanllengeTipView;

    int mShowOwnerTipTimes = 0;

    SkrAudioPermission mSkrAudioPermission = new SkrAudioPermission();

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
                    onSingBeginTipsPlayOver(msg.arg1);
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
        mRankingContainer = mRootView.findViewById(R.id.ranking_container);
        mRankingContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mInputContainerView.hideSoftInput();
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
        initCountDownView();
        initScoreView();

        mShowOwnerTipTimes = U.getPreferenceUtils().getSettingInt(KEY_OWNER_SHOW_TIMES, 0);

        mCorePresenter = new GrabCorePresenter(this, mRoomData);
        addPresent(mCorePresenter);
        mGrabRedPkgPresenter = new GrabRedPkgPresenter(this);
        addPresent(mGrabRedPkgPresenter);
        mGrabRedPkgPresenter.checkRedPkg();
        mCorePresenter.setGrabRedPkgPresenter(mGrabRedPkgPresenter);

        U.getSoundUtils().preLoad(TAG, R.raw.grab_challengelose, R.raw.grab_challengewin,
                R.raw.grab_gameover, R.raw.grab_iwannasing,
                R.raw.grab_nobodywants, R.raw.grab_readygo,
                R.raw.grab_xlight, R.raw.normal_click);

        MyLog.w(TAG, "gameid 是 " + mRoomData.getGameId() + " userid 是 " + MyUserInfoManager.getInstance().getUid());

        mUiHanlder.postDelayed(new Runnable() {
            @Override
            public void run() {
                onBattleBeginPlayOver();
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
                int index = mRankingContainer.indexOfChild(mInputContainerView);
                mRankingContainer.addView(mOwnerBeginGameIv, index, lp);
                mOwnerBeginGameIv.setOnClickListener(new DebounceViewClickListener() {
                    @Override
                    public void clickValid(View v) {
                        mCorePresenter.ownerBeginGame();
                    }
                });
            }

            if (mShowOwnerTipTimes < 3) {
                tryShowInviteTipView();
                tryShowManageSongTipView();
                tipViewAnimate(mIvInviteTip, mIvManageSongTipView);
            }
        }
    }

    private void tryShowInviteTipView() {
        if (mIvInviteTip == null) {
            mIvInviteTip = new ImageView(getContext());
            mIvInviteTip.setBackground(U.getDrawable(R.drawable.fz_yaoqing_tishi));
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(U.getDisplayUtils().dip2px(142), U.getDisplayUtils().dip2px(74));
            mIvInviteTip.setLayoutParams(layoutParams);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams.setMargins(0, U.getDisplayUtils().dip2px(127), U.getDisplayUtils().dip2px(13), 0);
            ((ViewGroup) mRankingContainer).addView(mIvInviteTip);
            U.getPreferenceUtils().setSettingInt(KEY_OWNER_SHOW_TIMES, ++mShowOwnerTipTimes);
        }
    }

    private void tryShowManageSongTipView() {
        if (mIvManageSongTipView == null) {
            mIvManageSongTipView = new ImageView(getContext());
            mIvManageSongTipView.setBackground(U.getDrawable(R.drawable.fz_kongzhi_tishi));
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(U.getDisplayUtils().dip2px(142), U.getDisplayUtils().dip2px(74));
            mIvManageSongTipView.setLayoutParams(layoutParams);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.setMargins(0, 0, U.getDisplayUtils().dip2px(13), U.getDisplayUtils().dip2px(78));
            int index = mRankingContainer.indexOfChild(mInputContainerView);
            mRankingContainer.addView(mIvManageSongTipView, index, layoutParams);
        }
    }

    private void tryShowChallengeTipView() {
        if (mIvChanllengeTipView == null) {
            mIvChanllengeTipView = new ImageView(getContext());
            mIvChanllengeTipView.setBackground(U.getDrawable(R.drawable.fz_tiaozhan_tishi));
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(U.getDisplayUtils().dip2px(142), U.getDisplayUtils().dip2px(74));
            mIvChanllengeTipView.setLayoutParams(layoutParams);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            layoutParams.addRule(RelativeLayout.BELOW, R.id.grab_op_btn);
            layoutParams.setMargins(0, U.getDisplayUtils().dip2px(2), U.getDisplayUtils().dip2px(10), 0);
            int index = mRankingContainer.indexOfChild(mInputContainerView);
            mRankingContainer.addView(mIvChanllengeTipView, index, layoutParams);
            startChallengeTipViewAnimator(mIvChanllengeTipView);
        }
    }

    private void removeInviteTipView() {
        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
            if (mIvInviteTip != null) {
                ((ViewGroup) mRankingContainer).removeView(mIvInviteTip);
                mIvInviteTip = null;
            }
        }
    }

    private void removeManageSongTipView() {
        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
            if (mIvManageSongTipView != null) {
                ((ViewGroup) mRankingContainer).removeView(mIvManageSongTipView);
                mIvManageSongTipView = null;
            }
        }
    }

    private void removeChallengeTipView() {
        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
            if (mIvChanllengeTipView != null) {
                ((ViewGroup) mRankingContainer).removeView(mIvChanllengeTipView);
                mIvChanllengeTipView = null;
                mChallengeTipViewAnimator.cancel();
            }
        }
    }

    ValueAnimator mTipViewAnimator;

    ValueAnimator mChallengeTipViewAnimator;

    private void tipViewAnimate(View... viewList) {
        if (mTipViewAnimator != null) {
            mTipViewAnimator.removeAllUpdateListeners();
            mTipViewAnimator.cancel();
        }
        mTipViewAnimator = ValueAnimator.ofInt(0, 20, 0);
        mTipViewAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mTipViewAnimator.setDuration(2500);
        mTipViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                boolean hasSetableView = false;
                for (View view : viewList) {
                    if (view != null && view.getParent() != null) {
                        hasSetableView = true;
                        view.setTranslationY((int) animation.getAnimatedValue());
                    }
                }

                if (!hasSetableView) {
                    mTipViewAnimator.cancel();
                }
            }
        });

        mTipViewAnimator.start();
    }

    private void startChallengeTipViewAnimator(View view) {
        if (mChallengeTipViewAnimator != null) {
            mChallengeTipViewAnimator.removeAllUpdateListeners();
            mChallengeTipViewAnimator.cancel();
        }
        mChallengeTipViewAnimator = ValueAnimator.ofInt(0, 20, 0);
        mChallengeTipViewAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mChallengeTipViewAnimator.setDuration(2500);
        mChallengeTipViewAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                boolean hasSetableView = false;

                if (view.getParent() != null) {
                    hasSetableView = true;
                    view.setTranslationY((int) animation.getAnimatedValue());
                }

                if (!hasSetableView) {
                    mChallengeTipViewAnimator.cancel();
                }
            }
        });

        mChallengeTipViewAnimator.start();
    }

    @Override
    public void onStart() {
        super.onStart();
        mSkrAudioPermission.ensurePermission(null, true);
    }

    private void initCountDownView() {
        mRedPkgView = (RedPkgCountDownView) mRootView.findViewById(R.id.red_pkg_view);
    }

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

    private void initInputView() {
        mInputContainerView = mRootView.findViewById(R.id.input_container_view);
        mInputContainerView.setRoomData(mRoomData);
    }

    private void initBottomView() {
        mBottomBgVp = mRootView.findViewById(R.id.bottom_bg_vp);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mBottomBgVp.getLayoutParams();
        /**
         * 按比例适配手机
         */
        lp.height = U.getDisplayUtils().getScreenHeight() * 284 / 667;

        mBottomContainerView = (BottomContainerView) mRootView.findViewById(R.id.bottom_container_view);
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
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(GrabRoomFragment.this.getActivity(), GrabSongManageFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(false)
                        .addDataBeforeAdd(0, mRoomData)
                        .build());
                removeManageSongTipView();
            }


        });
        mBottomContainerView.setRoomData(mRoomData);
    }

    private void initCommentView() {
        mCommentView = mRootView.findViewById(R.id.comment_view);
        mCommentView.setListener(new CommentItemListener() {
            @Override
            public void clickAvatar(int userId) {
                showPersonInfoView(userId);
            }

            @Override
            public void clickAgreeKick(int userId, boolean isAgree) {

            }
        });
        mCommentView.setRoomData(mRoomData);
//        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mCommentView.getLayoutParams();
//        layoutParams.height = U.getDisplayUtils().getPhoneHeight() - U.getDisplayUtils().dip2px(430 + 60);
    }

    private void initChangeRoomTransitionView() {
        mGrabChangeRoomTransitionView = mRootView.findViewById(R.id.change_room_transition_view);
        mGrabChangeRoomTransitionView.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSkrAudioPermission.onBackFromPermisionManagerMaybe();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ShowPersonCardEvent event) {
        showPersonInfoView(event.getUid());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabWantInviteEvent event) {
        // 房主想要邀请别人加入游戏
        // 打开邀请面板
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), InviteFriendFragment.class)
                .setAddToBackStack(true)
                .setHasAnimation(true)
                .addDataBeforeAdd(0, mRoomData)
                .setEnterAnim(R.anim.slide_in_bottom)
                .setExitAnim(R.anim.slide_out_bottom)
                .build()
        );

        removeInviteTipView();
    }

    private void showPersonInfoView(int userID) {
        if (!U.getNetworkUtils().hasNetwork()) {
            U.getToastUtil().showShort("网络异常，请检查网络后重试!");
            return;
        }
        mInputContainerView.hideSoftInput();

        if (mRoomData.getRoomType() == GrabRoomType.ROOM_TYPE_COMMON) {
            // 普通房
            mPersonInfoDialog = new PersonInfoDialog(getActivity(), userID, true, true);
        } else {
            if (mRoomData.isOwner()) {
                mPersonInfoDialog = new PersonInfoDialog(getActivity(), userID, true, true);
            } else {
                mPersonInfoDialog = new PersonInfoDialog(getActivity(), userID, true, false);
            }
        }

        mPersonInfoDialog.setListener(new PersonInfoDialog.KickListener() {

            @Override
            public void onClickKick(UserInfoModel userInfoModel) {
                showKickConfirmDialog(userInfoModel);
            }
        });
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

    private void initTopView() {
        // 加上状态栏的高度
        int statusBarHeight = U.getStatusBarUtil().getStatusBarHeight(getContext());

        mTopContainerView = mRootView.findViewById(R.id.top_container_view);
        mTopContainerView.setRoomData(mRoomData);

        {
            RelativeLayout.LayoutParams topLayoutParams = (RelativeLayout.LayoutParams) mTopContainerView.getLayoutParams();
            topLayoutParams.topMargin = statusBarHeight + topLayoutParams.topMargin;
        }

        mTopContainerView.setListener(mTopListener);
        mTopContainerView.getGrabTopView().setListener(new GrabTopView.Listener() {
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
            public void addFirend() {

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
        });

        mPracticeFlagIv = mRootView.findViewById(R.id.practice_flag_iv);
    }

    GrabTopContainerView.Listener mTopListener = new GrabTopContainerView.Listener() {
        @Override
        public void closeBtnClick() {
            if (mRoomData.isOwner() && mRoomData.getPlayerInfoList().size() >= 2) {
                quitGame();
            } else {
                mCorePresenter.exitRoom();
            }
        }

        @Override
        public void onVoiceChange(boolean voiceOpen) {
            mCorePresenter.muteAllRemoteAudioStreams(!voiceOpen, true);
            if (!voiceOpen) {
                StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
                        "game_muteon", null);
            }
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
        public void onClickVoiceVoiceAudition() {
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
    };

    /**
     * 转场动画相关初始化
     */
    private void initTurnChangeView() {
        mTurnInfoCardView = mRootView.findViewById(R.id.turn_info_iv);
        mSongInfoCardView = mRootView.findViewById(R.id.turn_change_song_info_card_view);
        mSingBeginTipsCardView = mRootView.findViewById(R.id.turn_change_sing_beign_tips_card_view);
        mRoundOverCardView = mRootView.findViewById(R.id.turn_change_round_over_card_view);
        mGrabGameOverView = (GrabGameOverView) mRootView.findViewById(R.id.grab_game_over_view);
    }

    private void initGiftDisplayView() {
        GiftContinueViewGroup giftContinueViewGroup = mRootView.findViewById(R.id.gift_continue_vg);
        giftContinueViewGroup.setRoomData(mRoomData);
        GiftBigAnimationViewGroup giftBigAnimationViewGroup = mRootView.findViewById(R.id.gift_big_animation_vg);
        giftBigAnimationViewGroup.setRoomData(mRoomData);

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
                U.getSoundUtils().play(TAG, R.raw.grab_iwannasing);
                mCorePresenter.grabThisRound(seq, challenge);
            }

            @Override
            public void clickLightOff() {
                mCorePresenter.lightsOff();
            }

            @Override
            public void grabCountDownOver() {
                mCorePresenter.sendMyGrabOver();
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
        });

        mGrabOpBtn.hide("initGrabOpView");

        mGrabGiveupView = (GrabGiveupView) mRootView.findViewById(R.id.grab_pass_view);
        mGrabGiveupView.setListener(new GrabGiveupView.Listener() {
            @Override
            public void giveUp() {
                GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
                if (infoModel != null) {
                    HashMap map = new HashMap();
                    map.put("songId2", String.valueOf(infoModel.getMusic().getItemID()));
                    StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
                            "give_up_sing", map);
                }
                mCorePresenter.giveUpSing();
            }
        });
        mGrabGiveupView.hideWithAnimation(false);
    }

    private void initSingStageView() {
        mOthersSingCardView = mRootView.findViewById(R.id.other_sing_card_view);
        mOthersSingCardView.setRoomData(mRoomData);
        mSelfSingCardView = mRootView.findViewById(R.id.self_sing_card_view);
        mSelfSingCardView.setRoomData(mRoomData);
        mSelfSingCardView.setListener(new SelfSingCardView2.Listener() {
            @Override
            public void onSelfSingOver() {
                mCorePresenter.sendRoundOverInfo();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabSomeOneLightOffEvent event) {
        // 灭灯
        if (event.getUid() == MyUserInfoManager.getInstance().getUid()) {
            U.getSoundUtils().play(TAG, R.raw.grab_xlight);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(GrabSomeOneLightBurstEvent event) {
        // 爆灯

        if (RoomDataUtils.isMyRound(mRoomData.getRealRoundInfo())) {
            // 当前我是演唱者
            mDengBigAnimation.setTranslationY(U.getDisplayUtils().dip2px(200));
            mDengBigAnimation.playBurstAnimation(true);
        } else {
            mDengBigAnimation.playBurstAnimation(false);
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
        mTurnInfoCardView.setVisibility(View.GONE);
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
            mRankingContainer.removeView(mOwnerBeginGameIv);
        }
        // 播放3秒导唱
        mTopContainerView.setVisibility(View.VISIBLE);
        mOthersSingCardView.setVisibility(View.GONE);
        mTopContainerView.setSeqIndex(seq, mRoomData.getGrabConfigModel().getTotalGameRoundSeq());
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
        if (grabRoundInfoModel != null && !grabRoundInfoModel.isParticipant() && grabRoundInfoModel.getEnterStatus() == GrabRoundInfoModel.STATUS_GRAB) {
            mTurnInfoCardView.setVisibility(View.GONE);
            onSongInfoCardPlayOver("中途进来", pendingPlaySongCardData);
        } else {
            mTurnInfoCardView.setModeSongSeq(seq == 1, new SVGAListener() {
                @Override
                public void onFinished() {
                    mTurnInfoCardView.setVisibility(View.GONE);
                    onSongInfoCardPlayOver("动画结束进来", pendingPlaySongCardData);
                }
            });
        }
        mTopContainerView.setModeGrab();
    }

    void onSongInfoCardPlayOver(String from, PendingPlaySongCardData pendingPlaySongCardData) {
        MyLog.d(TAG, "onSongInfoCardPlayOver" + " pendingPlaySongCardData=" + pendingPlaySongCardData + " from=" + from);
        mUiHanlder.removeMessages(MSG_ENSURE_SONGCARD_OVER);
        mSingBeginTipsCardView.setVisibility(View.GONE);
        mSongInfoCardView.bindSongModel(mRoomData.getRealRoundSeq(), mRoomData.getGrabConfigModel().getTotalGameRoundSeq(), pendingPlaySongCardData.songModel);

        mGrabGiveupView.hideWithAnimation(false);
        GrabRoundInfoModel grabRoundInfoModel = mRoomData.getRealRoundInfo();
        if (grabRoundInfoModel != null && !grabRoundInfoModel.isParticipant() && grabRoundInfoModel.getEnterStatus() == GrabRoundInfoModel.STATUS_GRAB) {
            MyLog.d(TAG, "这轮刚进来，不播放导唱过场");
            mGrabOpBtn.hide("onSongInfoCardPlayOver1");
        } else {
            if (mRoomData.isInPlayerList()) {
                mGrabOpBtn.playCountDown(pendingPlaySongCardData.getSeq(), 4,pendingPlaySongCardData.songModel);
            }
        }
        mCorePresenter.playGuide();
    }

    @Override
    public void singBySelf() {
        removeAllEnsureMsg();
        mCorePresenter.stopGuide();
        mTopContainerView.setModeSing((int) MyUserInfoManager.getInstance().getUid());
        mTopContainerView.setSeqIndex(RoomDataUtils.getSeqOfRoundInfo(mRoomData.getRealRoundInfo()), mRoomData.getGrabConfigModel().getTotalGameRoundSeq());
        mSongInfoCardView.hide();
        mSingBeginTipsCardView.setVisibility(View.VISIBLE);
        mGrabOpBtn.hide("singBySelf");
        mGrabOpBtn.setGrabPreRound(true);
        Message msg = mUiHanlder.obtainMessage(MSG_ENSURE_SING_BEGIN_TIPS_OVER);
        msg.arg1 = (int) MyUserInfoManager.getInstance().getUid();
        mUiHanlder.sendMessageDelayed(msg, 4000);

//        mUiHanlder.removeMessages(MSG_SEND_SELF_SING_END);
//        mUiHanlder.sendMessageDelayed(mUiHanlder.obtainMessage(MSG_SEND_SELF_SING_END), mRoomData.getRealRoundInfo().getMusic().getTotalMs());

        singBeginTipsPlay((int) MyUserInfoManager.getInstance().getUid(), new Runnable() {
            @Override
            public void run() {
                onSingBeginTipsPlayOver(MyUserInfoManager.getInstance().getUid());
            }
        });
    }

    @Override
    public void singByOthers(long uid) {
        removeAllEnsureMsg();
        mTopContainerView.setVisibility(View.VISIBLE);
        mCorePresenter.stopGuide();
        mTopContainerView.setModeSing(uid);
        mTopContainerView.setSeqIndex(RoomDataUtils.getSeqOfRoundInfo(mRoomData.getRealRoundInfo()), mRoomData.getGrabConfigModel().getTotalGameRoundSeq());
        mSongInfoCardView.hide();
        mGrabOpBtn.hide("singByOthers");
        mGrabOpBtn.setGrabPreRound(false);
        mGrabGiveupView.hideWithAnimation(false);
        mSingBeginTipsCardView.setVisibility(View.VISIBLE);

        Message msg = mUiHanlder.obtainMessage(MSG_ENSURE_SING_BEGIN_TIPS_OVER);
        msg.arg1 = (int) uid;
        mUiHanlder.sendMessageDelayed(msg, 2600);

        singBeginTipsPlay((int) uid, new Runnable() {
            @Override
            public void run() {
                GrabRoundInfoModel grabRoundInfoModel = mRoomData.getRealRoundInfo();
                if (grabRoundInfoModel != null && grabRoundInfoModel.isParticipant() && mRoomData.isInPlayerList()) {
                    mGrabOpBtn.toOtherSingState();
                } else {
                    mGrabOpBtn.hide("singByOthers2");
                    MyLog.d(TAG, "中途进来的，不是本局参与者，隐藏按钮");
                }
                onSingBeginTipsPlayOver(uid);
            }
        });
    }

    private void singBeginTipsPlay(int uid, Runnable runnable) {
        GrabRoundInfoModel grabRoundInfoModel = mRoomData.getRealRoundInfo();
        if (grabRoundInfoModel != null) {
            if (!grabRoundInfoModel.isParticipant() && grabRoundInfoModel.getEnterStatus() == GrabRoundInfoModel.STATUS_SING) {
                MyLog.d(TAG, " 进入时已经时演唱阶段了，则不用播卡片了");
                runnable.run();
            } else {
                mSingBeginTipsCardView.bindData(mRoomData.getUserInfo(uid), grabRoundInfoModel.getMusic(), new SVGAListener() {
                    @Override
                    public void onFinished() {
                        runnable.run();
                    }
                }, grabRoundInfoModel.isChallengeRound());
            }
        } else {
            MyLog.w(TAG, "singBeginTipsPlay" + " grabRoundInfoModel = null ");
        }
    }

    private void onSingBeginTipsPlayOver(long uid) {
        MyLog.d(TAG, "onSingBeginTipsPlayOver" + " uid=" + uid);
        mUiHanlder.removeMessages(MSG_ENSURE_SING_BEGIN_TIPS_OVER);
        mSingBeginTipsCardView.setVisibility(View.GONE);
        mGrabScoreTipsView.reset();
        if (uid == MyUserInfoManager.getInstance().getUid()) {
            mGrabGiveupView.delayShowPassView();
            mCorePresenter.beginSing();
            // 显示歌词
            mSelfSingCardView.setVisibility(View.VISIBLE);
            mOthersSingCardView.setVisibility(View.GONE);
            GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
            if (infoModel != null) {
                mSelfSingCardView.playLyric(infoModel, mRoomData.isAccEnable());
            }
        } else {
            // 显示收音机
            mSelfSingCardView.setVisibility(View.GONE);
            mOthersSingCardView.setVisibility(View.VISIBLE);
            UserInfoModel userInfoModel = mRoomData.getUserInfo((int) uid);
            mOthersSingCardView.bindData(userInfoModel);
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
    public void roundOver(int songId, int reason, int resultType, boolean playNextSongInfoCard, BaseRoundInfoModel now) {
        removeAllEnsureMsg();
        Message msg = mUiHanlder.obtainMessage(MSG_ENSURE_ROUND_OVER_PLAY_OVER);
        msg.arg1 = playNextSongInfoCard ? 1 : 0;
        msg.obj = now;
        mUiHanlder.sendMessageDelayed(msg, 2400);
        mSelfSingCardView.setVisibility(View.GONE);
        mTopContainerView.setVisibility(View.VISIBLE);
        mOthersSingCardView.hide();
        mSongInfoCardView.hide();
        mGrabOpBtn.hide("roundOver");
        mGrabGiveupView.hideWithAnimation(false);
        mRoundOverCardView.bindData(songId, reason, resultType, new SVGAListener() {
            @Override
            public void onFinished() {
                onRoundOverPlayOver(playNextSongInfoCard, now);
            }
        });
    }

    private void onRoundOverPlayOver(boolean playNextSongInfoCard, BaseRoundInfoModel now) {
        mUiHanlder.removeMessages(MSG_ENSURE_ROUND_OVER_PLAY_OVER);
        mRoundOverCardView.setVisibility(View.GONE);
        if (playNextSongInfoCard) {
            grabBegin(now.getRoundSeq(), now.getMusic());
        }
    }

    @Override
    public void redPkgCountDown(long duration) {
        mRedPkgView.setVisibility(View.VISIBLE);
        mRedPkgView.startCountDown(duration);
        mUiHanlder.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRedPkgView.setVisibility(View.GONE);
            }
        }, duration);
    }

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
        if (mSelfSingCardView != null) {
            mSelfSingCardView.destroy();
        }
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

        if (mTipViewAnimator != null) {
            mTipViewAnimator.cancel();
        }

        if (mChallengeTipViewAnimator != null) {
            mChallengeTipViewAnimator.cancel();
        }

        U.getSoundUtils().release(TAG);
        BgMusicManager.getInstance().setRoom(false);
    }

    @Override
    protected boolean onBackPressed() {
        if (mInputContainerView.onBackPressed()) {
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
                            mCorePresenter.exitRoom();
                            StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
                                    "game_exit", null);
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
        mSelfSingCardView.setVisibility(View.GONE);
        mOthersSingCardView.hide();
        mTurnInfoCardView.setVisibility(View.GONE);
        mSingBeginTipsCardView.setVisibility(View.GONE);
        mRoundOverCardView.setVisibility(View.GONE);
        mGrabGameOverView.setVisibility(View.VISIBLE);
        mTopContainerView.onGameFinish();
        mGrabGameOverView.starAnimation(new SVGAListener() {
            @Override
            public void onFinished() {
                mCorePresenter.exitRoom();
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
            mGrabChangeRoomTransitionView.setVisibility(View.GONE);
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
                    mGrabChangeRoomTransitionView.setVisibility(View.GONE);
                }
            }, 1500 - t);
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
        MyLog.d(TAG, "onGrabGameOver " + from);
//        mUiHanlder.removeMessages(MSG_ENSURE_GAME_OVER);

        if (getActivity() != null) {
            getActivity().finish();
        } else {
            MyLog.d(TAG, "onGrabGameOver activity==null");
        }
        StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
                StatConstants.KEY_GAME_FINISH, null);

        ARouter.getInstance().build(RouterConstants.ACTIVITY_GRAB_RESULT)
                .withSerializable("room_data", mRoomData)
                .navigation();
    }

    // 确认踢人弹窗
    private void showKickConfirmDialog(UserInfoModel userInfoModel) {
        MyLog.d(TAG, "showKickConfirmDialog" + " userInfoModel=" + userInfoModel);
        dismissDialog();
        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
        int type;
        if (mRoomData.isOwner()) {
            type = ConfirmDialog.TYPE_OWNER_KICK_CONFIRM;
        } else {
            type = ConfirmDialog.TYPE_KICK_CONFIRM;
        }
        mGrabKickDialog = new ConfirmDialog(U.getActivityUtils().getTopActivity(), userInfoModel, type, mRoomData.getGrabConfigModel().getKickUserConsumCoinCnt());
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
        mCorePresenter.exitRoom();
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
            mPracticeFlagIv.setVisibility(View.GONE);
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
