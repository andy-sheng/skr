package com.module.playways.grab.room.fragment;

import android.animation.Animator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoManager;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.HttpUtils;
import com.common.utils.U;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.dialog.view.TipsDialogView;
import com.module.playways.RoomData;
import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.event.SomeOneLightOffEvent;
import com.module.playways.grab.room.inter.IGrabView;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.playways.grab.room.presenter.GrabCorePresenter;
import com.module.playways.grab.room.top.GrabTopContainerView;
import com.module.playways.grab.room.view.GrabGameOverView;
import com.module.playways.grab.room.view.GrabOpView;
import com.module.playways.grab.room.view.OthersSingCardView;
import com.module.playways.grab.room.view.RoundOverCardView;
import com.module.playways.grab.room.view.SelfSingCardView;
import com.module.playways.grab.room.view.SingBeginTipsCardView;
import com.module.playways.grab.room.view.SongInfoCardView;
import com.module.playways.grab.room.view.TurnInfoCardView;
import com.module.playways.rank.prepare.model.OnlineInfoModel;
import com.module.playways.rank.prepare.model.RoundInfoModel;
import com.module.playways.rank.room.comment.CommentModel;
import com.module.playways.rank.room.comment.CommentView;
import com.module.playways.rank.room.gift.GiftBigAnimationViewGroup;
import com.module.playways.rank.room.gift.GiftContinueViewGroup;
import com.module.playways.rank.room.presenter.DownLoadScoreFilePresenter;
import com.module.playways.rank.room.view.BottomContainerView;
import com.module.playways.rank.room.view.InputContainerView;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGAParser;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.zq.dialog.PersonInfoDialogView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class GrabRoomFragment extends BaseFragment implements IGrabView {

    public final static String TAG = "RankingRoomFragment";

    public static final int MSG_ENSURE_READYGO_OVER = 1;

    public static final int MSG_ENSURE_SONGCARD_OVER = 2;

    public static final int MSG_ENSURE_SING_BEGIN_TIPS_OVER = 3;

    public static final int MSG_ENSURE_ROUND_OVER_PLAY_OVER = 4;

    public static final int MSG_ENSURE_BATTLE_BEGIN_OVER = 5;

    public static final int MSG_ENSURE_GAME_OVER = 6;

    RoomData mRoomData;

    RelativeLayout mRankingContainer;

    InputContainerView mInputContainerView;

    BottomContainerView mBottomContainerView;

    CommentView mCommentView;

    GrabTopContainerView mTopContainerView;

    GrabCorePresenter mCorePresenter;

    DownLoadScoreFilePresenter mDownLoadScoreFilePresenter;

    DialogPlus mQuitTipsDialog;

    TurnInfoCardView mTurnInfoCardView; //歌曲次序 以及 对战开始卡片

    SongInfoCardView mSongInfoCardView; // 歌曲信息卡片

    SingBeginTipsCardView mSingBeginTipsCardView; // 提示xxx演唱开始的卡片

    RoundOverCardView mRoundOverCardView; // 轮次结束的卡片

    GrabOpView mGrabOpBtn; // 抢 倒计时 灭 等按钮

    OthersSingCardView mOthersSingCardView;

    SelfSingCardView mSelfSingCardView;

    GrabGameOverView mGrabGameOverView;

    DialogPlus mDialogPlus;

    boolean mNeedScroll = true;

    int mUFOMode = 0; //UFO飞碟模式 1即入场 2即循环 3即离场 4动画结束

    SVGAParser mSVGAParser;

    List<Animator> mAnimatorList = new ArrayList<>();  //存放所有需要尝试取消的动画

    boolean isGameEndAniamtionShow = false; // 标记对战结束动画是否播放

    Handler mUiHanlder = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_ENSURE_READYGO_OVER:
                    onReadyGoOver();
                    break;
                case MSG_ENSURE_BATTLE_BEGIN_OVER:
                    onBattleBeginPlayOver();
                    break;
                case MSG_ENSURE_SONGCARD_OVER:
                    onSongInfoCardPlayOver((PendingPlaySongCardData) msg.obj);
                    break;
                case MSG_ENSURE_SING_BEGIN_TIPS_OVER:
                    onSingBeginTipsPlayOver(msg.arg1);
                    break;
                case MSG_ENSURE_ROUND_OVER_PLAY_OVER:
                    onRoundOverPlayOver(msg.arg1 == 1, (RoundInfoModel) msg.obj);
                    break;
                case MSG_ENSURE_GAME_OVER:
                    onGrabGameOver();
                    break;
            }
        }
    };

    /**
     * 可以在此恢复数据
     *
     * @param savedInstanceState
     */
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        mRoomData = (RoomData) savedInstanceState.getSerializable("roomData");
    }

    /**
     * 当系统认为你的fragment存在被销毁的可能时，onSaveInstanceState 就会被调用
     * 不包括用户主动退出fragment导致其被销毁，比如按BACK键后fragment被主动销毁
     *
     * @param outState
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("roomData", mRoomData);
    }

    @Override
    public int initView() {
        return R.layout.grab_room_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        // 请保证从下面的view往上面的view开始初始化
        mRankingContainer = mRootView.findViewById(R.id.ranking_container);
        initInputView();
        initBottomView();
        initCommentView();
        initTopView();
        initTurnChangeView();
        initGiftDisplayView();
        initGrabOpView();
        initSingStageView();

        mCorePresenter = new GrabCorePresenter(this, mRoomData);
        addPresent(mCorePresenter);

        mDownLoadScoreFilePresenter = new DownLoadScoreFilePresenter(new HttpUtils.OnDownloadProgress() {
            @Override
            public void onDownloaded(long downloaded, long totalLength) {

            }

            @Override
            public void onCompleted(String localPath) {
                MyLog.d(TAG, "机器人打分文件下载就绪");
            }

            @Override
            public void onCanceled() {

            }

            @Override
            public void onFailed() {

            }
        }, mRoomData.getPlayerInfoList());

        addPresent(mDownLoadScoreFilePresenter);
        mDownLoadScoreFilePresenter.prepareRes();

        U.getSoundUtils().preLoad(TAG, R.raw.stage_readygo, R.raw.general_countdown);

        MyLog.w(TAG, "gameid 是 " + mRoomData.getGameId() + " userid 是 " + MyUserInfoManager.getInstance().getUid());

        mUiHanlder.postDelayed(new Runnable() {
            @Override
            public void run() {
                // TODO: 2019/1/22 现在的动画，对战开始和第一首是连着一起的
                onBattleBeginPlayOver();
            }
        }, 100);

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void initInputView() {
        mInputContainerView = mRootView.findViewById(R.id.input_container_view);
        mInputContainerView.setRoomData(mRoomData);
    }

    private void initBottomView() {
        mBottomContainerView = (BottomContainerView) mRootView.findViewById(R.id.bottom_container_view);
        mBottomContainerView.setListener(new BottomContainerView.Listener() {
            @Override
            public void showInputBtnClick() {
                if (mDialogPlus != null && mDialogPlus.isShowing()) {
                    mDialogPlus.dismiss();
                }
                mInputContainerView.showSoftInput();
            }
        });
        mBottomContainerView.setRoomData(mRoomData);
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

    boolean isReport = false;

    private void showPersonInfoView(int userID) {
        if (!U.getNetworkUtils().hasNetwork()) {
            U.getToastUtil().showShort("网络异常，请检查网络后重试!");
            return;
        }
        mInputContainerView.hideSoftInput();
        PersonInfoDialogView personInfoDialogView = new PersonInfoDialogView(getContext(), userID);

        mDialogPlus = DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(personInfoDialogView))
                .setGravity(Gravity.BOTTOM)
                .setContentBackgroundResource(R.color.transparent)
                .setOverlayBackgroundResource(R.color.transparent)
                .setExpanded(false)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogPlus dialog, @NonNull View view) {
                        if (view.getId() == R.id.report) {
                            // 举报
                            dialog.dismiss();
                            isReport = true;
                            U.getToastUtil().showShort("你点击了举报按钮");
                        } else if (view.getId() == R.id.follow_tv) {
                            // 关注
                            if (personInfoDialogView.getUserInfoModel().isFollow() || personInfoDialogView.getUserInfoModel().isFriend()) {
                                UserInfoManager.getInstance().mateRelation(personInfoDialogView.getUserInfoModel().getUserId(),
                                        UserInfoManager.RA_UNBUILD, personInfoDialogView.getUserInfoModel().isFriend());
                            } else {
                                UserInfoManager.getInstance().mateRelation(personInfoDialogView.getUserInfoModel().getUserId(),
                                        UserInfoManager.RA_BUILD, personInfoDialogView.getUserInfoModel().isFriend());
                            }

                        }
                    }
                })
                .setOnDismissListener(new OnDismissListener() {
                    @Override
                    public void onDismiss(@NonNull DialogPlus dialog) {
                        if (isReport) {
                            showReportView();
                        }
                        isReport = false;
                    }
                })
                .create();
        mDialogPlus.show();
    }

    private void showReportView() {
        // TODO: 2018/12/26  等举报完善再写
    }

    private void initTopView() {
        mTopContainerView = mRootView.findViewById(R.id.top_container_view);
        mTopContainerView.setRoomData(mRoomData);

        // 加上状态栏的高度
        int statusBarHeight = U.getStatusBarUtil().getStatusBarHeight(getContext());
        RelativeLayout.LayoutParams topLayoutParams = (RelativeLayout.LayoutParams) mTopContainerView.getLayoutParams();
        topLayoutParams.topMargin = statusBarHeight + topLayoutParams.topMargin;
        mTopContainerView.setListener(new GrabTopContainerView.Listener() {
            @Override
            public void closeBtnClick() {
                quitGame();
            }

            @Override
            public void onVoiceChange(boolean voiceOpen) {
//                mCorePresenter.muteAllRemoteAudioStreams(!voiceOpen);
            }
        });
    }

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
    }

    private void initGrabOpView() {
        mGrabOpBtn = mRootView.findViewById(R.id.grab_op_btn);
        mGrabOpBtn.setListener(new GrabOpView.Listener() {
            @Override
            public void clickGrabBtn() {
                mCorePresenter.grabThisRound();
            }

            @Override
            public void clickLightOff() {
                mCorePresenter.lightsOff();
            }

            @Override
            public void countDownOver() {

            }
        });

        mGrabOpBtn.hide();
    }

    private void initSingStageView() {
        mOthersSingCardView = mRootView.findViewById(R.id.other_sing_card_view);
        mSelfSingCardView = mRootView.findViewById(R.id.self_sing_card_view);
    }

    private SVGAParser getSVGAParser() {
        if (mSVGAParser == null) {
            mSVGAParser = new SVGAParser(getActivity());
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
    public void onEvent(SomeOneLightOffEvent event) {
        //自己灭了别人的灯成功了
        if (event.uid == MyUserInfoManager.getInstance().getUid()) {
            mGrabOpBtn.hide();
        }
    }

    private void onReadyGoOver() {
        MyLog.w(TAG, "onReadyGoOver");
        mUiHanlder.removeMessages(MSG_ENSURE_READYGO_OVER);
        mCorePresenter.onOpeningAnimationOver();
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
        // 播放3秒导唱

        mTopContainerView.setSeqIndex(seq, mRoomData.getRoundInfoModelList().size());
        PendingPlaySongCardData pendingPlaySongCardData = new PendingPlaySongCardData(seq, songModel);
        Message msg = mUiHanlder.obtainMessage(MSG_ENSURE_SONGCARD_OVER);
        msg.obj = pendingPlaySongCardData;
        mUiHanlder.removeMessages(MSG_ENSURE_SONGCARD_OVER);
        mUiHanlder.sendMessageDelayed(msg, 4000);

        mTurnInfoCardView.setModeSongSeq(seq == 1, new SVGAListener() {
            @Override
            public void onFinished() {
                mTurnInfoCardView.setVisibility(View.GONE);
                onSongInfoCardPlayOver(pendingPlaySongCardData);
            }
        });
        mTopContainerView.setModeGrab();
    }

    void onSongInfoCardPlayOver(PendingPlaySongCardData pendingPlaySongCardData) {
        mUiHanlder.removeMessages(MSG_ENSURE_SONGCARD_OVER);
        mSingBeginTipsCardView.setVisibility(View.GONE);
        mSongInfoCardView.bindSongModel(pendingPlaySongCardData.songModel);
//        mGrabOpBtn.setVisibility(View.VISIBLE);
        mGrabOpBtn.playCountDown(4, pendingPlaySongCardData.songModel.getStandIntroEndT() - pendingPlaySongCardData.songModel.getStandIntroBeginT());
        mCorePresenter.playGuide();
    }

    @Override
    public void singBySelf() {
        mCorePresenter.stopGuide();
        mTopContainerView.setModeSing((int) MyUserInfoManager.getInstance().getUid());
        mTopContainerView.setSeqIndex(RoomDataUtils.getSeqOfRoundInfo(mRoomData.getRealRoundInfo()), mRoomData.getRoundInfoModelList().size());
        mSongInfoCardView.hide();
        mSingBeginTipsCardView.setVisibility(View.VISIBLE);
        mGrabOpBtn.hide();
        mUiHanlder.removeMessages(MSG_ENSURE_SING_BEGIN_TIPS_OVER);
        Message msg = mUiHanlder.obtainMessage(MSG_ENSURE_SING_BEGIN_TIPS_OVER);
        msg.arg1 = (int) MyUserInfoManager.getInstance().getUid();
        mUiHanlder.sendMessageDelayed(msg, 4000);

        singBeginTipsPlay((int) MyUserInfoManager.getInstance().getUid(), new Runnable() {
            @Override
            public void run() {
                onSingBeginTipsPlayOver(MyUserInfoManager.getInstance().getUid());
            }
        });
    }

    @Override
    public void singByOthers(long uid) {
        mCorePresenter.stopGuide();
        mTopContainerView.setModeSing(uid);
        mTopContainerView.setSeqIndex(RoomDataUtils.getSeqOfRoundInfo(mRoomData.getRealRoundInfo()), mRoomData.getRoundInfoModelList().size());
        mSongInfoCardView.hide();
        mGrabOpBtn.toSingState();
        mSingBeginTipsCardView.setVisibility(View.VISIBLE);

        mUiHanlder.removeMessages(MSG_ENSURE_SING_BEGIN_TIPS_OVER);
        Message msg = mUiHanlder.obtainMessage(MSG_ENSURE_SING_BEGIN_TIPS_OVER);
        msg.arg1 = (int) uid;
        mUiHanlder.sendMessageDelayed(msg, 4000);

        singBeginTipsPlay((int) uid, new Runnable() {
            @Override
            public void run() {
                onSingBeginTipsPlayOver(uid);
            }
        });
    }

    private void singBeginTipsPlay(int uid, Runnable runnable) {
        mSingBeginTipsCardView.bindData(mRoomData.getUserInfo(uid), new SVGAListener() {
            @Override
            public void onFinished() {
                runnable.run();
            }
        });
    }

    private void onSingBeginTipsPlayOver(long uid) {
        MyLog.d(TAG, "onSingBeginTipsPlayOver" + " uid=" + uid);
        mUiHanlder.removeMessages(MSG_ENSURE_SING_BEGIN_TIPS_OVER);
        mSingBeginTipsCardView.setVisibility(View.GONE);
        if (uid == MyUserInfoManager.getInstance().getUid()) {
            mCorePresenter.beginSing();
            // 显示歌词
            mSelfSingCardView.setVisibility(View.VISIBLE);
            mSelfSingCardView.playLyric(mRoomData.getRealRoundInfo().getSongModel(), true);
        } else {
            // 显示收音机
            mOthersSingCardView.setVisibility(View.VISIBLE);
            mOthersSingCardView.bindData(mRoomData.getUserInfo((int) uid).getAvatar(), mRoomData.getRealRoundInfo().getSongModel());
        }
    }

    @Override
    public void roundOver(int reason, int resultType, boolean playNextSongInfoCard, RoundInfoModel now) {
        mUiHanlder.removeMessages(MSG_ENSURE_ROUND_OVER_PLAY_OVER);
        Message msg = mUiHanlder.obtainMessage(MSG_ENSURE_ROUND_OVER_PLAY_OVER);
        msg.arg1 = playNextSongInfoCard ? 1 : 0;
        msg.obj = now;
        mUiHanlder.sendMessageDelayed(msg, 4000);
        mSelfSingCardView.hide();
        mOthersSingCardView.hide();
        mSongInfoCardView.hide();
        mGrabOpBtn.hide();
        mRoundOverCardView.bindData(reason, resultType, new SVGAListener() {
            @Override
            public void onFinished() {
                onRoundOverPlayOver(playNextSongInfoCard, now);
            }
        });
    }

    private void onRoundOverPlayOver(boolean playNextSongInfoCard, RoundInfoModel now) {
        mUiHanlder.removeMessages(MSG_ENSURE_ROUND_OVER_PLAY_OVER);
        mRoundOverCardView.setVisibility(View.GONE);
        if (playNextSongInfoCard) {
            grabBegin(now.getRoundSeq(), now.getSongModel());
        }
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        if (type == 0) {
            mRoomData = (RoomData) data;
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        MyLog.d(TAG, "destroy");
        if (mDialogPlus != null && mDialogPlus.isShowing()) {
            mDialogPlus.dismiss();
            mDialogPlus = null;
        }
        mUiHanlder.removeCallbacksAndMessages(null);

        isGameEndAniamtionShow = false;

        if (mAnimatorList != null) {
            for (Animator animator : mAnimatorList) {
                if (animator != null) {
                    animator.cancel();
                }
            }
            mAnimatorList.clear();
        }

        U.getSoundUtils().release(TAG);
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
                    .setMessageTip("提前退出会破坏其他玩家的对局体验，确定退出么？")
                    .setConfirmTip("取消")
                    .setCancelTip("确定")
                    .setConfirmBtnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mQuitTipsDialog.dismiss(false);
                        }
                    })
                    .setCancelBtnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mQuitTipsDialog.dismiss(false);
                            getActivity().finish();
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
        Message msg = mUiHanlder.obtainMessage(MSG_ENSURE_GAME_OVER);
        mUiHanlder.sendMessageDelayed(msg, 4000);

        mSelfSingCardView.hide();
        mOthersSingCardView.hide();
        mTurnInfoCardView.setVisibility(View.GONE);
        mSingBeginTipsCardView.setVisibility(View.GONE);
        mRoundOverCardView.setVisibility(View.GONE);
        mGrabGameOverView.setVisibility(View.VISIBLE);
        mGrabGameOverView.starAnimation(new SVGAListener() {
            @Override
            public void onFinished() {
                onGrabGameOver();
            }
        });
    }

    private void onGrabGameOver() {
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), GrabResultFragment.class)
                .setAddToBackStack(true)
                .setHasAnimation(true)
                .addDataBeforeAdd(0, mRoomData)
                .build());

        U.getFragmentUtils().popFragment(FragmentUtils.newPopParamsBuilder()
                .setPopFragment(GrabRoomFragment.this)
                .setPopAbove(false)
                .setHasAnimation(true)
                .build());
    }

    static class PendingPlaySongCardData {
        int seq;
        SongModel songModel;

        public PendingPlaySongCardData(int seq, SongModel songModel) {
            this.seq = seq;
            this.songModel = songModel;
        }
    }

}
