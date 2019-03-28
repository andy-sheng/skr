package com.module.playways.grab.room.fragment;

import android.animation.Animator;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
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
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExImageView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
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
import com.module.playways.grab.room.view.IRedPkgCountDownView;
import com.module.playways.grab.room.view.OthersSingCardView;
import com.module.playways.grab.room.view.RedPkgCountDownView;
import com.module.playways.grab.room.view.RoundOverCardView;
import com.module.playways.grab.room.view.SelfSingCardView2;
import com.module.playways.grab.room.view.SingBeginTipsCardView;
import com.module.playways.grab.room.view.SongInfoCardView;
import com.module.playways.grab.room.view.TurnInfoCardView;
import com.module.playways.rank.prepare.model.OnlineInfoModel;
import com.module.playways.rank.prepare.model.BaseRoundInfoModel;
import com.module.playways.rank.prepare.view.VoiceControlPanelView;
import com.module.playways.rank.room.comment.CommentModel;
import com.module.playways.rank.room.comment.CommentView;
import com.module.playways.rank.room.gift.GiftBigAnimationViewGroup;
import com.module.playways.rank.room.gift.GiftContinueViewGroup;
import com.module.playways.rank.room.view.BottomContainerView;
import com.module.playways.rank.room.view.InputContainerView;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGAParser;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.zq.dialog.ConfirmDialog;
import com.zq.dialog.PersonInfoDialog;
import com.zq.toast.CommonToastView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GrabRoomFragment extends BaseFragment implements IGrabView, IRedPkgCountDownView {

    public final static String TAG = "GrabRoomFragment";

//    public static final int MSG_ENSURE_READYGO_OVER = 1;

    public static final int MSG_ENSURE_SONGCARD_OVER = 2;

    public static final int MSG_ENSURE_SING_BEGIN_TIPS_OVER = 3;

    public static final int MSG_ENSURE_ROUND_OVER_PLAY_OVER = 4;

    public static final int MSG_ENSURE_BATTLE_BEGIN_OVER = 5;

    public static final int MSG_ENSURE_GAME_OVER = 6;

    //自己演唱玩
//    public static final int MSG_SEND_SELF_SING_END = 7;

    GrabRoomData mRoomData;

    RelativeLayout mRankingContainer;

    InputContainerView mInputContainerView;

    BottomContainerView mBottomContainerView;

    View mVoiceControlBg;

    VoiceControlPanelView mVoiceControlView;

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

    DialogPlus mGameRoleDialog;

    ConfirmDialog mGrabKickDialog;

    ExImageView mIvRoomManage;

    SVGAParser mSVGAParser;

    List<Animator> mAnimatorList = new ArrayList<>();  //存放所有需要尝试取消的动画

    boolean mIsGameEndAniamtionShow = false; // 标记对战结束动画是否播放

    long mBeginChangeRoomTs;

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
                case MSG_ENSURE_GAME_OVER:
                    onGrabGameOver("MSG_ENSURE_GAME_OVER");
                    break;
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
        initManageView();
        mCorePresenter = new GrabCorePresenter(this, mRoomData);
        addPresent(mCorePresenter);
        mGrabRedPkgPresenter = new GrabRedPkgPresenter(this);
        addPresent(mGrabRedPkgPresenter);
        mGrabRedPkgPresenter.checkRedPkg();
//        mDownLoadScoreFilePresenter = new DownLoadScoreFilePresenter(new HttpUtils.OnDownloadProgress() {
//            @Override
//            public void onDownloaded(long downloaded, long totalLength) {
//
//            }
//
//            @Override
//            public void onCompleted(String localPath) {
//                MyLog.d(TAG, "机器人打分文件下载就绪");
//            }
//
//            @Override
//            public void onCanceled() {
//
//            }
//
//            @Override
//            public void onFailed() {
//
//            }
//        }, mRoomData.getPlayerInfoList());
//
//        addPresent(mDownLoadScoreFilePresenter);
//        mDownLoadScoreFilePresenter.prepareRes();

        U.getSoundUtils().preLoad(TAG, R.raw.grab_challengelose, R.raw.grab_challengewin,
                R.raw.grab_gameover, R.raw.grab_iwannasing,
                R.raw.grab_nobodywants, R.raw.grab_olight, R.raw.grab_olight_lowervolume,
                R.raw.grab_readygo, R.raw.grab_xlight, R.raw.grab_lightup, R.raw.normal_click);

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

    private void initCountDownView() {
        mRedPkgView = (RedPkgCountDownView) mRootView.findViewById(R.id.red_pkg_view);
    }

    private void initInputView() {
        mInputContainerView = mRootView.findViewById(R.id.input_container_view);
        mInputContainerView.setRoomData(mRoomData);
    }

    private void initBottomView() {
        mVoiceControlBg = (View) mRootView.findViewById(R.id.voice_control_bg);
        mVoiceControlView = (VoiceControlPanelView) mRootView.findViewById(R.id.voice_control_view);
        mBottomContainerView = (BottomContainerView) mRootView.findViewById(R.id.bottom_container_view);
        mBottomContainerView.setListener(new BottomContainerView.Listener() {
            @Override
            public void showInputBtnClick() {
                if (mPersonInfoDialog != null && mPersonInfoDialog.isShowing()) {
                    mPersonInfoDialog.dismiss();
                }
                mInputContainerView.showSoftInput();
            }
        });
        mBottomContainerView.setRoomData(mRoomData);

        mVoiceControlBg.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mVoiceControlView.setVisibility(View.GONE);
                mVoiceControlBg.setVisibility(View.GONE);
            }
        });
    }

    private void initCommentView() {
        mCommentView = mRootView.findViewById(R.id.comment_view);
        mCommentView.setListener(new RecyclerOnItemClickListener() {
            @Override
            public void onItemClicked(View view, int position, Object model) {
                if (model instanceof CommentModel) {
                    int userID = ((CommentModel) model).getUserId();
                    showPersonInfoView(userID);
                }
            }
        });
        mCommentView.setRoomData(mRoomData);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mCommentView.getLayoutParams();
        layoutParams.height = U.getDisplayUtils().getPhoneHeight() - U.getDisplayUtils().dip2px(430 + 60);
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
        if (event.getUid() != MyUserInfoManager.getInstance().getUid()) {
            showPersonInfoView(event.getUid());
        }
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

        mTopContainerView.setListener(mListener);
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
        });

        mPracticeFlagIv = mRootView.findViewById(R.id.practice_flag_iv);
    }

    GrabTopContainerView.Listener mListener = new GrabTopContainerView.Listener() {
        @Override
        public void closeBtnClick() {
            mCorePresenter.exitRoom();
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
            if (mGameRoleDialog != null) {
                mGameRoleDialog.dismiss();
            }
            U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
            mGameRoleDialog = DialogPlus.newDialog(getContext())
                    .setContentHolder(new ViewHolder(R.layout.grab_game_role_view_layout))
                    .setContentBackgroundResource(R.color.transparent)
                    .setOverlayBackgroundResource(R.color.black_trans_50)
                    .setExpanded(false)
                    .setGravity(Gravity.CENTER)
                    .create();
            mGameRoleDialog.show();
        }

        @Override
        public void onClickVoiceVoiceAudition() {
            U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
            mVoiceControlBg.setVisibility(View.VISIBLE);
            mVoiceControlView.setVisibility(View.VISIBLE);
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

    private void initManageView() {
        mIvRoomManage = (ExImageView) mRootView.findViewById(R.id.iv_room_manage);
        if (mRoomData.isOwner()) {
            mIvRoomManage.setVisibility(View.VISIBLE);
            mIvRoomManage.setOnClickListener(new DebounceViewClickListener() {
                @Override
                public void clickValid(View v) {
                    U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(GrabRoomFragment.this.getActivity(), GrabSongManageFragment.class)
                            .setAddToBackStack(true)
                            .setHasAnimation(true)
                            .addDataBeforeAdd(0, mRoomData)
                            .build());
                }
            });
        }
    }

    private void initGrabOpView() {
        mGrabOpBtn = mRootView.findViewById(R.id.grab_op_btn);
        mGrabOpBtn.setGrabRoomData(mRoomData);
        mGrabOpBtn.setListener(new GrabOpView.Listener() {
            @Override
            public void clickGrabBtn(int seq) {
                U.getSoundUtils().play(TAG, R.raw.grab_iwannasing);
                mCorePresenter.grabThisRound(seq);
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
        });

        mGrabOpBtn.hide();

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

    private SVGAParser getSVGAParser() {
        if (mSVGAParser == null) {
            mSVGAParser = new SVGAParser(U.app());
            mSVGAParser.setFileDownloader(new SVGAParser.FileDownloader() {
                @Override
                public void resume(final URL url, final Function1<? super InputStream, Unit> complete, final Function1<? super Exception, Unit> failure) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            OkHttpClient client = new OkHttpClient();
                            Request request = new Request.Builder().url(url).get().build();
                            try {
                                Response response = client.newCall(request).execute();
                                complete.invoke(response.body().byteStream());
                            } catch (IOException e) {
                                e.printStackTrace();
                                failure.invoke(e);
                            }
                        }
                    }).start();
                }
            });
        }
        return mSVGAParser;
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
            U.getSoundUtils().play(TAG, R.raw.grab_olight_lowervolume);
            mDengBigAnimation.setTranslationY(U.getDisplayUtils().dip2px(200));
            mDengBigAnimation.playBurstAnimation();
        } else {
            U.getSoundUtils().play(TAG, R.raw.grab_olight);
            mDengBigAnimation.playBurstAnimation();
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
        // 播放3秒导唱
        mTopContainerView.setVisibility(View.VISIBLE);
        mOthersSingCardView.setVisibility(View.GONE);
        mTopContainerView.setSeqIndex(seq, mRoomData.getGrabConfigModel().getTotalGameRoundSeq());
        PendingPlaySongCardData pendingPlaySongCardData = new PendingPlaySongCardData(seq, songModel);
        Message msg = mUiHanlder.obtainMessage(MSG_ENSURE_SONGCARD_OVER);
        msg.obj = pendingPlaySongCardData;
        if (seq == 1) {
            mUiHanlder.sendMessageDelayed(msg, 4000);
            mUiHanlder.postDelayed(new Runnable() {
                @Override
                public void run() {
                    U.getSoundUtils().play(TAG, R.raw.grab_readygo);
                }
            }, 100);
        } else {
            mUiHanlder.sendMessageDelayed(msg, 1200);
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
            mGrabOpBtn.hide();
        } else {
            mGrabOpBtn.playCountDown(pendingPlaySongCardData.getSeq(), 4
                    , pendingPlaySongCardData.songModel.getStandIntroEndT() - pendingPlaySongCardData.songModel.getStandIntroBeginT());
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
        mGrabOpBtn.hide();
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
        mGrabOpBtn.hide();
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
                if (grabRoundInfoModel != null && grabRoundInfoModel.isParticipant()) {
                    mGrabOpBtn.toOtherSingState();
                } else {
                    mGrabOpBtn.hide();
                    MyLog.d(TAG, "中途进来的，不是本局参与者，隐藏按钮");
                }
                onSingBeginTipsPlayOver(uid);
            }
        });
    }

    private void singBeginTipsPlay(int uid, Runnable runnable) {
        GrabRoundInfoModel grabRoundInfoModel = mRoomData.getRealRoundInfo();
        if (grabRoundInfoModel != null && !grabRoundInfoModel.isParticipant() && grabRoundInfoModel.getEnterStatus() == GrabRoundInfoModel.STATUS_SING) {
            MyLog.d(TAG, " 进入时已经时演唱阶段了，则不用播卡片了");
            runnable.run();
        } else {
            mSingBeginTipsCardView.bindData(mRoomData.getUserInfo(uid), grabRoundInfoModel.getMusic(), new SVGAListener() {
                @Override
                public void onFinished() {
                    runnable.run();
                }
            });
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
                mSelfSingCardView.playLyric(infoModel.getMusic(), mRoomData.isAccEnable());
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
        mGrabOpBtn.hide();
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
            mQuitTipsDialog.dismiss();
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
        if (mQuitTipsDialog == null) {
            TipsDialogView tipsDialogView = new TipsDialogView.Builder(getContext())
                    .setMessageTip("提前退出会破坏其他玩家的对局体验\n确定退出么？")
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
                                mQuitTipsDialog.dismiss(false);
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
        mUiHanlder.removeMessages(MSG_ENSURE_GAME_OVER);
//        mUiHanlder.removeMessages(MSG_SEND_SELF_SING_END);
        Message msg = mUiHanlder.obtainMessage(MSG_ENSURE_GAME_OVER);
        mUiHanlder.sendMessageDelayed(msg, 4000);

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
                onGrabGameOver("onFinished");
            }
        });
    }

    @Override
    public void onGetGameResult(boolean success) {
        if (success) {
            onGrabGameOver("onGetGameResultSuccess");
        } else {
            if (getActivity() != null) {
                getActivity().finish();
            }
        }
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
        mGrabGiveupView.passSuccess();
    }

    @Override
    public void updateScrollBarProgress(int score, int songLineNum) {
        mGrabScoreTipsView.updateScore(score, songLineNum);
    }

    private void onGrabGameOver(String from) {
        MyLog.d(TAG, "onGrabGameOver " + from);
        ARouter.getInstance().build(RouterConstants.ACTIVITY_GRAB_RESULT)
                .withSerializable("room_data", mRoomData)
                .navigation();
        // 延迟一点finish，以防漏底
        mUiHanlder.postDelayed(new Runnable() {
            @Override
            public void run() {
                Activity activity = getActivity();
                if (activity != null) {
                    activity.finish();
                }
                StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_GRAB),
                        StatConstants.KEY_GAME_FINISH, null);
            }
        }, 500);
    }

    // 确认踢人弹窗
    private void showKickConfirmDialog(UserInfoModel userInfoModel) {
        MyLog.d(TAG, "showConfirmDialog" + " userInfoModel=" + userInfoModel);
        dismissDialog();
        U.getKeyBoardUtils().hideSoftInputKeyBoard(getActivity());
        mGrabKickDialog = new ConfirmDialog(getActivity(), userInfoModel, ConfirmDialog.TYPE_KICK_CONFIRM, mRoomData.getGrabConfigModel().getKickUserConsumCoinCnt());
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
        if (mGameRoleDialog != null) {
            mGameRoleDialog.dismiss();
        }
        if (mPersonInfoDialog != null) {
            mPersonInfoDialog.dismiss();
        }
        mVoiceControlView.setVisibility(View.GONE);
        if (mGrabKickDialog != null) {
            mGrabKickDialog.dismiss();
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
            mGrabKickDialog = new ConfirmDialog(getActivity(), userInfoModel, ConfirmDialog.TYPE_KICK_REQUEST, 5);
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
    public void kickBySomeOne() {
        MyLog.d(TAG, "kickBySomeOne");
        if (getActivity() != null) {
            getActivity().finish();
        }

        U.getToastUtil().showSkrCustomLong(new CommonToastView.Builder(U.app())
                .setImage(R.drawable.touxiangshezhishibai_icon)
                .setText("超过半数玩家请你出房间，要友好文明游戏哦~")
                .build());
    }

    @Override
    public void kickSomeOne() {
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
