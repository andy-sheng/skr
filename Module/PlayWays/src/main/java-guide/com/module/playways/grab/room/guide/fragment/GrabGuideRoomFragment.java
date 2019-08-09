package com.module.playways.grab.room.guide.fragment;

import android.animation.Animator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.alibaba.android.arouter.launcher.ARouter;
import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.permission.SkrAudioPermission;
import com.common.log.MyLog;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.component.busilib.constans.GrabRoomType;
import com.component.busilib.manager.BgMusicManager;
import com.dialog.view.TipsDialogView;
import com.module.RouterConstants;
import com.module.playways.R;
import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.event.GrabSomeOneLightBurstEvent;
import com.module.playways.grab.room.event.GrabSomeOneLightOffEvent;
import com.module.playways.grab.room.event.LightOffAnimationOverEvent;
import com.module.playways.grab.room.guide.presenter.GrabGuidePresenter;
import com.module.playways.grab.room.guide.IGrabGuideView;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.top.GrabTopContainerView;
import com.module.playways.grab.room.top.GrabTopOpView;
import com.module.playways.grab.room.view.GrabDengBigAnimationView;
import com.module.playways.grab.room.view.GrabGameOverView;
import com.module.playways.grab.room.view.GrabGiveupView;
import com.module.playways.grab.room.view.GrabOpView;
import com.module.playways.grab.room.view.GrabScoreTipsView;
import com.module.playways.grab.room.view.SongInfoCardView;
import com.module.playways.grab.room.view.TurnInfoCardView;
import com.module.playways.grab.room.view.control.OthersSingCardView;
import com.module.playways.grab.room.view.control.RoundOverCardView;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.module.playways.grab.room.view.control.SingBeginTipsCardView;
import com.module.playways.room.prepare.model.BaseRoundInfoModel;
import com.module.playways.room.prepare.model.OnlineInfoModel;
import com.module.playways.room.room.comment.CommentView;
import com.module.playways.room.room.comment.listener.CommentItemListener;
import com.module.playways.room.room.gift.GiftOverlayAnimationViewGroup;
import com.module.playways.room.room.gift.GiftContinueViewGroup;
import com.module.playways.room.room.view.BottomContainerView;
import com.module.playways.room.room.view.InputContainerView;
import com.module.playways.room.song.model.SongModel;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.zq.live.proto.Room.EQRoundStatus;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class GrabGuideRoomFragment extends BaseFragment implements IGrabGuideView {

    public final String TAG = "GrabGuideFragment";

    public static final int MSG_ENSURE_SONGCARD_OVER = 2;

    public static final int MSG_ENSURE_SING_BEGIN_TIPS_OVER = 3;

    public static final int MSG_ENSURE_ROUND_OVER_PLAY_OVER = 4;

    public static final int MSG_ENSURE_BATTLE_BEGIN_OVER = 5;

    GrabRoomData mRoomData;

    RelativeLayout mRankingContainer;

    ExImageView mGrabRoomBgFlag;

    InputContainerView mInputContainerView;

    ViewGroup mBottomBgVp;

    BottomContainerView mBottomContainerView;

    CommentView mCommentView;

    GrabTopContainerView mTopContainerView;// 顶部，抢唱阶段，以及非本人的演唱阶段

    ExImageView mPracticeFlagIv; // 练习中

    GrabGuidePresenter mCorePresenter;

    TurnInfoCardView mTurnInfoCardView; //歌曲次序 以及 对战开始卡片

    SongInfoCardView mSongInfoCardView; // 歌曲信息卡片


    RoundOverCardView mRoundOverCardView;
    OthersSingCardView mOthersSingCardView;
    SelfSingCardView mSelfSingCardView;
    SingBeginTipsCardView mSingBeginTipsCardView;

    GrabOpView mGrabOpBtn; // 抢 倒计时 灭 等按钮

    GrabGiveupView mGrabGiveupView;

    GrabGameOverView mGrabGameOverView;

    GrabDengBigAnimationView mDengBigAnimation;

    GrabScoreTipsView mGrabScoreTipsView;

    DialogPlus mQuitTipsDialog;

    List<Animator> mAnimatorList = new ArrayList<>();  //存放所有需要尝试取消的动画

    boolean mIsGameEndAniamtionShow = false; // 标记对战结束动画是否播放

    ImageView mGrabBtnTipIv;
    ImageView mGrabSelfSingTipIv;
    ImageView mGrabLightTipIv;

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
        return R.layout.grab_guide_room_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
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
        initScoreView();

        mCorePresenter = new GrabGuidePresenter(this, mRoomData);
        addPresent(mCorePresenter);

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
    }


    @Override
    public void onStart() {
        super.onStart();
        mSkrAudioPermission.ensurePermission(null, true);
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
                mInputContainerView.showSoftInput();
            }

            @Override
            public void clickRoomManagerBtn() {
            }

            @Override
            public void showGiftPanel() {
            }
        });
        mBottomContainerView.setRoomData(mRoomData);
    }

    private void initCommentView() {
        mCommentView = mRootView.findViewById(R.id.comment_view);
        mCommentView.setListener(new CommentItemListener() {
            @Override
            public void clickAvatar(int userId) {
            }

            @Override
            public void clickAgreeKick(int userId, boolean isAgree) {

            }
        });
        mCommentView.setRoomData(mRoomData);
//        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mCommentView.getLayoutParams();
//        layoutParams.height = U.getDisplayUtils().getPhoneHeight() - U.getDisplayUtils().dip2px(430 + 60);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSkrAudioPermission.onBackFromPermisionManagerMaybe(getActivity());
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
            }

            @Override
            public void addFirend() {

            }

            @Override
            public void onClickVoiceAudition() {
                U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
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
        }

        @Override
        public void onClickGameRule() {
            U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
        }

        @Override
        public void onClickVoiceVoiceAudition() {
            U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
        }

        @Override
        public void onClickSkipGuide() {
            quitGame();
        }

        @Override
        public void onClickFeedBack() {

        }
    };

    /**
     * 转场动画相关初始化
     */
    private void initTurnChangeView() {
        mTurnInfoCardView = mRootView.findViewById(R.id.turn_info_iv);
        mSongInfoCardView = mRootView.findViewById(R.id.turn_change_song_info_card_view);
        mSingBeginTipsCardView = new SingBeginTipsCardView(mRootView, mRoomData);

        mRoundOverCardView = new RoundOverCardView(mRootView, mRoomData);

        mGrabGameOverView = mRootView.findViewById(R.id.grab_game_over_view);
    }

    private void initGiftDisplayView() {
        GiftContinueViewGroup giftContinueViewGroup = mRootView.findViewById(R.id.gift_continue_vg);
        giftContinueViewGroup.setRoomData(mRoomData);
        GiftOverlayAnimationViewGroup giftBigAnimationViewGroup = mRootView.findViewById(R.id.gift_big_animation_vg);
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
                mCorePresenter.grabThisRound((int) MyUserInfoManager.getInstance().getUid(), seq, challenge);
                removeGrabBtnTipView();
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
                GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
                if (now != null) {
                    if (now.getRoundSeq() == 2) {
                        mCorePresenter.grabThisRound(mRoomData.getGrabGuideInfoModel().getbRoundUserID(), now.getRoundSeq(), false);
                    }
                    if (now.getRoundSeq() == 1) {
                        tryShowGrabTipView();
                    }
                }
            }

            @Override
            public void clickBurst(int seq) {
                mCorePresenter.lightsBurst();
                removeGrabLightTipView();
            }

            @Override
            public void showChallengeTipView() {
            }

            @Override
            public void hideChallengeTipView() {
            }

            @Override
            public void showGrabTipView() {

            }

            @Override
            public void hideGrabTipView() {

            }

            @Override
            public void showBurstTipView() {

            }

            @Override
            public void hideBurstTipView() {

            }
        });

        mGrabOpBtn.hide("initGrabOpView");

        mGrabGiveupView = (GrabGiveupView) mRootView.findViewById(R.id.grab_giveup_view);
        mGrabGiveupView.setListener(new GrabGiveupView.Listener() {
            @Override
            public void giveUp(boolean b) {
                GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
//                if (infoModel != null) {
//                    HashMap map = new HashMap();
//                    map.put("songId2", String.valueOf(infoModel.getMusic().getItemID()));
//                    StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
//                            "give_up_sing", map);
//                }
                mCorePresenter.giveUpSing();
            }
        });
        mGrabGiveupView.hideWithAnimation(false);
    }

    private void tryShowGrabTipView() {
        if (mGrabBtnTipIv == null) {
            mGrabBtnTipIv = new ImageView(getContext());
            mGrabBtnTipIv.setImageResource(R.drawable.xinshou_huange);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mGrabBtnTipIv.setLayoutParams(layoutParams);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

            int[] location = new int[2];
            mGrabOpBtn.getGrabBtn().getLocationInWindow(location);
            layoutParams.rightMargin = U.getDisplayUtils().dip2px(14);
            layoutParams.topMargin = location[1] + mGrabOpBtn.getGrabBtn().getHeight();
            ((ViewGroup) mRankingContainer).addView(mGrabBtnTipIv);
            mGrabBtnTipIv.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    mCorePresenter.changeSong();
                }
            });
        }
    }

    private void tryShowGrabLightTipView() {
        if (mGrabLightTipIv == null) {
            mGrabLightTipIv = new ImageView(getContext());
            mGrabLightTipIv.setImageResource(R.drawable.xinshou_baodeng);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            int[] location = new int[2];
            mGrabOpBtn.getBurstBtn().getLocationInWindow(location);
            layoutParams.rightMargin = U.getDisplayUtils().dip2px(14);
            layoutParams.topMargin = location[1] - U.getDisplayUtils().dip2px(70);
            mGrabLightTipIv.setLayoutParams(layoutParams);
            ((ViewGroup) mRankingContainer).addView(mGrabLightTipIv);
        }
    }

    private void removeGrabLightTipView() {
        if (mGrabLightTipIv != null) {
            mRankingContainer.removeView(mGrabLightTipIv);
            mGrabLightTipIv = null;
        }
    }

    private void tryShowGrabSelfSingTipView() {
        if (mGrabSelfSingTipIv == null) {
            mGrabSelfSingTipIv = new ImageView(getContext());
            mGrabSelfSingTipIv.setImageResource(R.drawable.xinshou_yanchang);
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            layoutParams.leftMargin = U.getDisplayUtils().dip2px(65);
            layoutParams.topMargin = U.getDisplayUtils().dip2px(65);
            mGrabSelfSingTipIv.setLayoutParams(layoutParams);
            ((ViewGroup) mRankingContainer).addView(mGrabSelfSingTipIv);
        }
    }

    private void removeGrabSelfSingTipView() {
        if (mGrabSelfSingTipIv != null) {
            mRankingContainer.removeView(mGrabSelfSingTipIv);
            mGrabSelfSingTipIv = null;
        }
    }


    private void removeGrabBtnTipView() {
        if (mGrabBtnTipIv != null) {
            mRankingContainer.removeView(mGrabBtnTipIv);
            mGrabBtnTipIv = null;
        }
    }

    private void initSingStageView() {
        mSelfSingCardView = new SelfSingCardView(mRootView, mRoomData);
        mSelfSingCardView.setListener(new SelfSingCardView.Listener() {
            @Override
            public void onSelfSingOver() {
                mCorePresenter.sendRoundOverInfo();
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
        // 播放3秒导唱
        mTopContainerView.setVisibility(View.VISIBLE);
        mOthersSingCardView.setVisibility(View.GONE);
        mSelfSingCardView.setVisibility(View.GONE);
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
        if (grabRoundInfoModel != null && !grabRoundInfoModel.isParticipant() && grabRoundInfoModel.getEnterStatus() == EQRoundStatus.QRS_INTRO.getValue()) {
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
        if (grabRoundInfoModel != null && !grabRoundInfoModel.isParticipant() && grabRoundInfoModel.getEnterStatus() == EQRoundStatus.QRS_INTRO.getValue()) {
            MyLog.d(TAG, "这轮刚进来，不播放导唱过场");
            mGrabOpBtn.hide("onSongInfoCardPlayOver1");
        } else {
            if (mRoomData.isInPlayerList()) {
                mGrabOpBtn.playCountDown(pendingPlaySongCardData.getSeq(), 4, pendingPlaySongCardData.songModel);
            }
        }
        mCorePresenter.playGuide();
    }


    @Override
    public void changeSong() {
        GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
        if (now != null) {
            mSongInfoCardView.bindSongModel(now.getRoundSeq(), mRoomData.getGrabConfigModel().getTotalGameRoundSeq(), now.getMusic());
        }
        mCorePresenter.playGuide();
    }

    @Override
    public void singBySelf() {
        removeAllEnsureMsg();
        mCorePresenter.stopGuide();
        GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
        // 第二轮不播这个动画
        mTopContainerView.setModeSing();
        mTopContainerView.setSeqIndex(RoomDataUtils.getSeqOfRoundInfo(mRoomData.getRealRoundInfo()), mRoomData.getGrabConfigModel().getTotalGameRoundSeq());
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

    }

    @Override
    public void singByOthers() {
        removeAllEnsureMsg();
        mTopContainerView.setVisibility(View.VISIBLE);
        mCorePresenter.stopGuide();
        mTopContainerView.setModeSing();
        mTopContainerView.setSeqIndex(RoomDataUtils.getSeqOfRoundInfo(mRoomData.getRealRoundInfo()), mRoomData.getGrabConfigModel().getTotalGameRoundSeq());
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
                MyLog.d(TAG, "中途进来的，不是本局参与者，隐藏按钮");
            }
            onSingBeginTipsPlayOver();
        } else {
            singBeginTipsPlay(new Runnable() {
                @Override
                public void run() {
                    GrabRoundInfoModel grabRoundInfoModel = mRoomData.getRealRoundInfo();
                    if (grabRoundInfoModel != null && grabRoundInfoModel.isParticipant()
                            && mRoomData.isInPlayerList()
                            && !RoomDataUtils.isRoundSinger(now, MyUserInfoManager.getInstance().getUid())) {
                        mGrabOpBtn.toOtherSingState();
                    } else {
                        mGrabOpBtn.hide("singByOthers2");
                        MyLog.d(TAG, "中途进来的，不是本局参与者，隐藏按钮");
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
        mSingBeginTipsCardView.setVisibility(View.GONE);
        mGrabScoreTipsView.reset();
        GrabRoundInfoModel now = mRoomData.getRealRoundInfo();
        if (now != null) {
            if (now.singBySelf()) {
                mGrabGiveupView.delayShowGiveUpView(false);
                mCorePresenter.beginSing();
                // 自己唱
                mSelfSingCardView.setVisibility(View.VISIBLE);
                mOthersSingCardView.setVisibility(View.GONE);
                mSelfSingCardView.playLyric();

                if (mRoomData.getRoomType() == GrabRoomType.ROOM_TYPE_GUIDE) {
                    tryShowGrabSelfSingTipView();
                }
            } else {
                // 别人唱
                mSelfSingCardView.setVisibility(View.GONE);
                mOthersSingCardView.setVisibility(View.VISIBLE);
                mOthersSingCardView.bindData();
                if (mRoomData.getRoomType() == GrabRoomType.ROOM_TYPE_GUIDE) {
                    tryShowGrabLightTipView();
                }
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
        removeGrabLightTipView();
        removeGrabSelfSingTipView();
        removeAllEnsureMsg();
        Message msg = mUiHanlder.obtainMessage(MSG_ENSURE_ROUND_OVER_PLAY_OVER);
        msg.arg1 = playNextSongInfoCard ? 1 : 0;
        msg.obj = now;
        mUiHanlder.sendMessageDelayed(msg, 4000);
        mSelfSingCardView.setVisibility(View.GONE);
        mTopContainerView.setVisibility(View.VISIBLE);
        mOthersSingCardView.hide();
        mSongInfoCardView.hide();
        mGrabOpBtn.hide("roundOver");
        mGrabGiveupView.hideWithAnimation(false);
        mRoundOverCardView.bindData(lastInfoModel, new SVGAListener() {
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

        U.getSoundUtils().release(TAG);
        BgMusicManager.getInstance().setRoom(false);
    }

    @Override
    public boolean onBackPressed() {
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
            String msg = "还未完成新手引导\n确定退出么？";
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
        onGrabGameOver("onGetGameResultSuccess");
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
        ARouter.getInstance().build(RouterConstants.ACTIVITY_GRAB_GUIDE_RESULT)
                .withSerializable("room_data", mRoomData)
                .navigation();

    }

    private void dismissDialog() {
        if (mBottomContainerView != null) {
            mBottomContainerView.dismissPopWindow();
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
