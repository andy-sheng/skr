package com.module.playways.rank.room.fragment;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.anim.ExObjectAnimator;
import com.common.base.BaseFragment;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.userinfo.UserInfoManager;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.HttpUtils;
import com.common.utils.SongResUtils;
import com.common.utils.U;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.dialog.view.TipsDialogView;
import com.module.playways.rank.prepare.model.OnlineInfoModel;
import com.module.playways.rank.room.comment.CommentModel;
import com.module.playways.rank.room.comment.CommentView;
import com.module.playways.rank.room.gift.GiftBigAnimationViewGroup;
import com.module.playways.rank.room.gift.GiftContinueViewGroup;
import com.module.playways.rank.room.model.RecordData;
import com.module.playways.RoomData;
import com.module.playways.rank.room.presenter.DownLoadScoreFilePresenter;
import com.module.playways.rank.room.presenter.RankCorePresenter;
import com.module.playways.rank.room.view.BottomContainerView;
import com.module.playways.rank.room.view.IGameRuleView;
import com.module.playways.rank.room.view.InputContainerView;
import com.module.playways.rank.room.view.TopContainerView;
import com.module.playways.rank.room.view.TurnChangeCardView;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGACallback;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.zq.dialog.PersonInfoDialogView;
import com.zq.lyrics.LyricsManager;
import com.zq.lyrics.LyricsReader;
import com.zq.lyrics.widget.AbstractLrcView;
import com.zq.lyrics.widget.FloatLyricsView;
import com.zq.lyrics.widget.ManyLyricsView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static com.zq.lyrics.widget.AbstractLrcView.LRCPLAYERSTATUS_PLAY;

public class RankRoomFragment extends BaseFragment implements IGameRuleView {

    public final static String TAG = "RankingRoomFragment";

    static final int ENSURE_RUN = 99;

    static final int SHOW_RIVAL_LYRIC = 10;

    RoomData mRoomData;

    RelativeLayout mRankingContainer;

    InputContainerView mInputContainerView;

    BottomContainerView mBottomContainerView;

    CommentView mCommentView;

    TopContainerView mTopContainerView;

    SVGAImageView mReadyGoBg;

    SVGAImageView mStagePeopleBg;

    SVGAImageView mStageUfoBg;

    ImageView mEndRoundHint;

    ImageView mEndGameIv;

    RankCorePresenter mCorePresenter;

    DownLoadScoreFilePresenter mDownLoadScoreFilePresenter;

    ManyLyricsView mManyLyricsView;

    FloatLyricsView mFloatLyricsView;

    DialogPlus mQuitTipsDialog;

    Handler mUiHanlder = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == ENSURE_RUN) {
                Runnable runnable = (Runnable) msg.obj;
                if (runnable != null) {
                    runnable.run();
                    mPendingSelfCountDownRunnable = null;
                }
                onReadyGoOver();
            } else if (SHOW_RIVAL_LYRIC == msg.what) {
                mFloatLyricsView.setVisibility(View.VISIBLE);
            }
        }
    };

    Disposable mPrepareLyricTask;

    TurnChangeCardView mTurnChangeView;

    DialogPlus mDialogPlus;

    SongModel mPlayingSongModel;

    boolean mNeedScroll = true;

    boolean mReadyGoPlaying = false;

    ExObjectAnimator mTurnChangeCardShowAnimator;

    ExObjectAnimator mTurnChangeCardHideAnimator;

    Runnable mPendingSelfCountDownRunnable;

    PendingRivalData mPendingRivalCountdown;

    int mUFOMode = 0; //UFO飞碟模式 1即入场 2即循环 3即离场 4动画结束

    SVGAParser mSVGAParser;

    AnimatorSet mGameEndAnimation;

    List<Animator> mAnimatorList = new ArrayList<>();  //存放所有需要尝试取消的动画

    boolean isGameEndAniamtionShow = false; // 标记对战结束动画是否播放

    @Override
    public int initView() {
        return R.layout.rank_room_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        // 请保证从下面的view往上面的view开始初始化
        mRankingContainer = mRootView.findViewById(R.id.ranking_container);
        initInputView();
        initBottomView();
        initCommentView();
        initTopView();
        initLyricsView();
        initTurnChangeView();
        initGiftDisplayView();

        showReadyGoView();

        mCorePresenter = new RankCorePresenter(this, mRoomData);
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
    }

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

    public void playShowTurnCardAnimator(Runnable countDownRunnable) {
        mTurnChangeView.setVisibility(View.VISIBLE);
        if (mTurnChangeCardShowAnimator == null) {
            mTurnChangeCardShowAnimator = ExObjectAnimator.ofFloat(mTurnChangeView, "translationX", -U.getDisplayUtils().getScreenWidth(), 0.08f * U.getDisplayUtils().getScreenWidth(), 0);
            mTurnChangeCardShowAnimator.setDuration(750);
        }
        // 这里有坑！！！一直一定要保证 countDownRunnable 每次都要改变，能准确拿到
        mTurnChangeCardShowAnimator.setListener(new ExObjectAnimator.Listener() {
            @Override
            public void onAnimationStart(Animator animator) {
                mTurnChangeView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mUiHanlder.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        playHideTurnCardAnimator(countDownRunnable);
                    }
                }, 1500);
            }
        });
        mTurnChangeCardShowAnimator.start();
    }

    public void playHideTurnCardAnimator(Runnable countDownRunnable) {
        mTurnChangeView.setVisibility(View.VISIBLE);
        if (mTurnChangeCardHideAnimator == null) {
            mTurnChangeCardHideAnimator = ExObjectAnimator.ofFloat(mTurnChangeView, "translationX", 0, -0.08f * U.getDisplayUtils().getScreenWidth(), U.getDisplayUtils().getScreenWidth());
            mTurnChangeCardHideAnimator.setDuration(750);
        }
        mTurnChangeCardHideAnimator.setListener(new ExObjectAnimator.Listener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mTurnChangeView.setVisibility(View.GONE);
                if (countDownRunnable != null) {
                    countDownRunnable.run();
                    mUiHanlder.removeMessages(ENSURE_RUN);
                }
                // TODO: 2018/12/29 先加一个保护 
                if (mRoomData.getRealRoundInfo() != null) {
                    if (mRoomData.getRealRoundInfo().getUserID() != MyUserInfoManager.getInstance().getUid()) {
                        playShowMainStageAnimator();
                    }
                }
            }
        });
        mTurnChangeCardHideAnimator.start();
    }

    // 播放主舞台动画,入场、循环的离场
    private void playShowMainStageAnimator() {
        MyLog.d(TAG, "playShowMainStageAnimator");
        mFloatLyricsView.setVisibility(View.VISIBLE);
        // 舞台人的动画
        if (mStagePeopleBg != null) {
            mStagePeopleBg.setVisibility(View.VISIBLE);
            try {
                getSVGAParser().parse(new URL(RoomData.ROOM_STAGE_SVGA), new SVGAParser.ParseCompletion() {
                    @Override
                    public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                        SVGADrawable drawable = new SVGADrawable(videoItem);
                        mStagePeopleBg.setImageDrawable(drawable);
                        mStagePeopleBg.startAnimation();
                    }

                    @Override
                    public void onError() {

                    }
                });
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }

        //飞碟动画
        if (mStageUfoBg == null) {
            return;
        }
        mStageUfoBg.setVisibility(View.VISIBLE);
        getSVGAParser().parse("ufo_enter.svga", new SVGAParser.ParseCompletion() {
            @Override
            public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                MyLog.d(TAG, "playUFOStageAnimator");
                mUFOMode = 1;
                // 飞碟入场
                SVGADrawable drawable = new SVGADrawable(videoItem);
                mStageUfoBg.stopAnimation(true);
                mStageUfoBg.setLoops(1); // 播一次
                mStageUfoBg.setImageDrawable(drawable);
                mStageUfoBg.startAnimation();
                // 舞台入场,淡入
                ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(mStagePeopleBg, View.ALPHA, 0f, 1f);
                objectAnimator.setDuration(1000);
                objectAnimator.start();
                mAnimatorList.add(objectAnimator);
            }

            @Override
            public void onError() {

            }
        });


        mStageUfoBg.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mUFOMode == 1) {
                    if (mStageUfoBg.isAnimating()) {
                        mStageUfoBg.stopAnimation(true);
                    }
                    getSVGAParser().parse("ufo_process.svga", new SVGAParser.ParseCompletion() {
                        @Override
                        public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                            mUFOMode = 2;
                            SVGADrawable drawable = new SVGADrawable(videoItem);
                            if (mStageUfoBg != null) {
                                mStageUfoBg.setLoops(0);// 循环播放
                                mStageUfoBg.setImageDrawable(drawable);
                                mStageUfoBg.startAnimation();
                            }
                        }

                        @Override
                        public void onError() {

                        }
                    });
                } else if (mUFOMode == 3) {
                    if (mStageUfoBg.isAnimating()) {
                        mStageUfoBg.stopAnimation(true);
                    }
                    getSVGAParser().parse("ufo_leave.svga", new SVGAParser.ParseCompletion() {
                        @Override
                        public void onComplete(@NotNull SVGAVideoEntity videoItem) {
                            mUFOMode = 4;
                            // 主舞台消失动画
                            SVGADrawable drawable = new SVGADrawable(videoItem);
                            if (mStageUfoBg != null) {
                                mStageUfoBg.setLoops(1); // 播一次
                                mStageUfoBg.setImageDrawable(drawable);
                                mStageUfoBg.startAnimation();
                            }
                            // end小卡片，做一个满满消失的动画
                            ObjectAnimator objectAnimatorEnd = ObjectAnimator.ofFloat(mEndRoundHint, View.ALPHA, 1f, 0f);
                            objectAnimatorEnd.setDuration(1000);
                            objectAnimatorEnd.start();
                            mAnimatorList.add(objectAnimatorEnd);
                            // 舞台退出，淡出
                            ObjectAnimator objectAnimatorStage = ObjectAnimator.ofFloat(mStagePeopleBg, View.ALPHA, 1f, 0f);
                            objectAnimatorStage.setDuration(1000);
                            objectAnimatorStage.start();
                            mAnimatorList.add(objectAnimatorStage);
                        }

                        @Override
                        public void onError() {

                        }
                    });
                } else if (mUFOMode == 4) {
                    if (mStageUfoBg != null) {
                        mStageUfoBg.stopAnimation(true);
                    }
                }
            }

            @Override
            public void onRepeat() {
                if (mUFOMode == 1) {
                    if (mStageUfoBg != null && mStageUfoBg.isAnimating()) {
                        mStageUfoBg.stopAnimation(false);
                    }
                } else if (mUFOMode == 3) {
                    if (mStageUfoBg != null && mStageUfoBg.isAnimating()) {
                        mStageUfoBg.stopAnimation(false);
                    }
                } else if (mUFOMode == 4) {
                    if (mStageUfoBg != null && mStageUfoBg.isAnimating()) {
                        mStageUfoBg.stopAnimation(false);
                    }
                }
            }

            @Override
            public void onStep(int frame, double percentage) {

            }
        });
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
        mTopContainerView.setListener(new TopContainerView.Listener() {
            @Override
            public void closeBtnClick() {
                quitGame();
            }

            @Override
            public void onVoiceChange(boolean voiceOpen) {
                mCorePresenter.muteAllRemoteAudioStreams(!voiceOpen);
            }
        });
    }

    private void initLyricsView() {
        mManyLyricsView = mRootView.findViewById(R.id.many_lyrics_view);
        mManyLyricsView.setLrcStatus(AbstractLrcView.LRCSTATUS_LOADING);
        mFloatLyricsView = mRootView.findViewById(R.id.float_lyrics_view);
        mFloatLyricsView.setLrcStatus(AbstractLrcView.LRCSTATUS_LOADING);
    }

    private void initTurnChangeView() {
        mReadyGoBg = (SVGAImageView) mRootView.findViewById(R.id.ready_go_bg);

        mStagePeopleBg = (SVGAImageView) mRootView.findViewById(R.id.stage_people_bg);
        mStageUfoBg = (SVGAImageView) mRootView.findViewById(R.id.stage_ufo_bg);

        mEndRoundHint = (ImageView) mRootView.findViewById(R.id.end_round_hint);

        mTurnChangeView = mRootView.findViewById(R.id.turn_change_view);

        mEndGameIv = (ImageView) mRootView.findViewById(R.id.end_game_iv);
    }

    private void initGiftDisplayView() {
        GiftContinueViewGroup giftContinueViewGroup = mRootView.findViewById(R.id.gift_continue_vg);
        giftContinueViewGroup.setRoomData(mRoomData);
        GiftBigAnimationViewGroup giftBigAnimationViewGroup = mRootView.findViewById(R.id.gift_big_animation_vg);
        giftBigAnimationViewGroup.setRoomData(mRoomData);
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

    private void showReadyGoView() {
        mReadyGoBg.setVisibility(View.VISIBLE);
        mReadyGoPlaying = true;

        try {
            getSVGAParser().parse(new URL(RoomData.READY_GO_SVGA_URL), new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(SVGAVideoEntity videoItem) {
                    SVGADrawable drawable = new SVGADrawable(videoItem);
                    mReadyGoBg.stopAnimation(true);
                    mReadyGoBg.setImageDrawable(drawable);
                    mReadyGoBg.startAnimation();
                    U.getSoundUtils().play(TAG, R.raw.stage_readygo);
                }

                @Override
                public void onError() {
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        mReadyGoBg.setCallback(new SVGACallback() {
            @Override
            public void onPause() {

            }

            @Override
            public void onFinished() {
                if (mReadyGoBg.isAnimating()) {
                    mReadyGoBg.stopAnimation();
                    onReadyGoOver();
                }
            }

            @Override
            public void onRepeat() {
                if (mReadyGoBg.isAnimating()) {
                    mReadyGoBg.stopAnimation();
                    onReadyGoOver();
                }
            }

            @Override
            public void onStep(int i, double v) {

            }
        });
    }

    private void onReadyGoOver() {
        MyLog.w(TAG, "onReadyGoOver");
        if (mReadyGoPlaying) {
            mReadyGoPlaying = false;
            // 移除 readyGoView
            // 轮到自己演唱了，倒计时因为播放readyGo没播放
            if (mPendingSelfCountDownRunnable != null) {
                startSelfCountdown(mPendingSelfCountDownRunnable);
                mPendingSelfCountDownRunnable = null;
            }
            // 轮到他人唱了，倒计时因为播放readyGo没播放
            if (mPendingRivalCountdown != null) {
                startRivalCountdown(mPendingRivalCountdown.uid, mPendingRivalCountdown.avatar);
                mPendingRivalCountdown = null;
            }
            mRankingContainer.removeView(mReadyGoBg);
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
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
        destroyAnimation();
        if (mDialogPlus != null && mDialogPlus.isShowing()) {
            mDialogPlus.dismiss();
            mDialogPlus = null;
        }
        mUiHanlder.removeCallbacksAndMessages(null);
        mManyLyricsView.release();
        mFloatLyricsView.release();

        isGameEndAniamtionShow = false;
        if (mGameEndAnimation != null) {
            mGameEndAnimation.cancel();
        }

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

    /**
     * 保证在主线程
     */
    @Override
    public void startSelfCountdown(Runnable countDownOver) {
        mTopContainerView.loadAvatar(AvatarUtils.newParamsBuilder(MyUserInfoManager.getInstance().getAvatar())
                .build());
        mManyLyricsView.setVisibility(View.GONE);
        mFloatLyricsView.setVisibility(View.GONE);
        // 加保护，确保当前主舞台一定被移除
        if (mStagePeopleBg.isAnimating()) {
            mStagePeopleBg.stopAnimation(false);
        }
        if (mStageUfoBg.isAnimating()) {
            mStageUfoBg.stopAnimation();
        }
        mStagePeopleBg.setVisibility(View.GONE);
        mStageUfoBg.setVisibility(View.GONE);

        // 确保演唱逻辑一定要执行
        Message msg = mUiHanlder.obtainMessage(ENSURE_RUN);
        msg.what = ENSURE_RUN;
        msg.obj = countDownOver;

        if (mReadyGoPlaying) {
            // 正在播放readyGo动画，保存参数，延迟播放卡片
            mPendingSelfCountDownRunnable = countDownOver;
            // 目前 readyGo动画3秒 + 卡片动画4秒，按理7秒后一定执行，这里容错，允许延迟1秒
            mUiHanlder.removeMessages(ENSURE_RUN);
            mUiHanlder.sendMessageDelayed(msg, 8000);
        } else {
            if (mTurnChangeView.setData(mRoomData)) {
                playShowTurnCardAnimator(countDownOver);
                mUiHanlder.removeMessages(ENSURE_RUN);
                mUiHanlder.sendMessageDelayed(msg, 5000);
            } else {
                countDownOver.run();
            }
        }

        mUiHanlder.removeMessages(SHOW_RIVAL_LYRIC);
    }

    /**
     * 保证在主线程
     */
    @Override
    public void startRivalCountdown(int uid, String avatar) {
        mTopContainerView.loadAvatar(AvatarUtils.newParamsBuilder(avatar).build());
        mManyLyricsView.setVisibility(View.GONE);
        mFloatLyricsView.setVisibility(View.GONE);
        // 加保护，确保当前主舞台一定被移除
        if (mStagePeopleBg.isAnimating()) {
            mStagePeopleBg.stopAnimation(false);
        }
        if (mStageUfoBg.isAnimating()) {
            mStageUfoBg.stopAnimation();
        }
        mStagePeopleBg.setVisibility(View.GONE);
        mStageUfoBg.setVisibility(View.GONE);

//        mTopContainerView.cancelShowLastedTimeTask();
        if (mReadyGoPlaying) {
            // 正在播放readyGo动画，保存参数，延迟播放卡片
            mPendingRivalCountdown = new PendingRivalData(uid, avatar);
        } else {
            MyLog.w(TAG, "用户" + uid + "的演唱开始了");
            if (mTurnChangeView.setData(mRoomData)) {
                playShowTurnCardAnimator(null);
            }
        }

        mUiHanlder.removeMessages(SHOW_RIVAL_LYRIC);
        Message showLyricMsg = new Message();
        showLyricMsg.what = SHOW_RIVAL_LYRIC;
        mUiHanlder.sendMessageDelayed(showLyricMsg, 3000);
    }

    @Override
    public void userExit() {

    }

    @Override
    public void showRecordView(RecordData recordData) {
        MyLog.d(TAG, "showRecordView" + " recordData=" + recordData);
        if (mDialogPlus != null && mDialogPlus.isShowing()) {
            mDialogPlus.dismiss();
        }

        startGameEndAniamtion();
        mUiHanlder.postDelayed(new Runnable() {
            @Override
            public void run() {
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), RankRecordFragment.class)
                        .setAddToBackStack(true)
                        .addDataBeforeAdd(0, recordData)
                        .addDataBeforeAdd(1, mRoomData)
                        .build()
                );
            }
        }, 3000);
    }

    @Override
    public void showVoteView() {
        MyLog.d(TAG, "showVoteView");
        if (mDialogPlus != null && mDialogPlus.isShowing()) {
            mDialogPlus.dismiss();
        }

        startGameEndAniamtion();
        mUiHanlder.postDelayed(new Runnable() {
            @Override
            public void run() {
                U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), EvaluationFragment.class)
                        .setAddToBackStack(true)
                        .addDataBeforeAdd(0, mRoomData)
                        .build()
                );
            }
        }, 3000);
    }

    private void startGameEndAniamtion() {
        if (isGameEndAniamtionShow) {
            return;
        }
        isGameEndAniamtionShow = true;

        destroyAnimation();
        // 对战结束动画
        mEndGameIv.setVisibility(View.VISIBLE);
        if (mGameEndAnimation == null) {
            ObjectAnimator a1 = ObjectAnimator.ofFloat(mEndGameIv, View.SCALE_X, 0.3f, 1f);
            ObjectAnimator a2 = ObjectAnimator.ofFloat(mEndGameIv, View.SCALE_Y, 0.3f, 1f);
            mGameEndAnimation = new AnimatorSet();
            mGameEndAnimation.setDuration(750);
            mGameEndAnimation.playTogether(a1, a2);
        }
        mGameEndAnimation.start();

    }

    private void destroyAnimation() {
        if (mStagePeopleBg != null) {
            mStagePeopleBg.stopAnimation(true);
            mStagePeopleBg.setVisibility(View.GONE);
            mRankingContainer.removeView(mStagePeopleBg);
            mStagePeopleBg = null;
        }

        if (mStageUfoBg != null) {
            mStageUfoBg.stopAnimation(true);
            mStageUfoBg.setVisibility(View.GONE);
            mRankingContainer.removeView(mStageUfoBg);
            mStageUfoBg = null;
        }

        if (mReadyGoBg != null) {
            mReadyGoBg.stopAnimation(true);
            mReadyGoBg.setVisibility(View.GONE);
            mRankingContainer.removeView(mReadyGoBg);
            mReadyGoBg = null;
        }
    }


    @Override
    public void gameFinish() {
        MyLog.w(TAG, "游戏结束了");
        mTopContainerView.cancelShowLastedTimeTask();
        if (mPrepareLyricTask != null && !mPrepareLyricTask.isDisposed()) {
            mPrepareLyricTask.dispose();
        }
        mFloatLyricsView.setVisibility(View.GONE);
        mFloatLyricsView.release();
        mManyLyricsView.setVisibility(View.GONE);
        mManyLyricsView.release();
    }

    @Override
    public void updateUserState(List<OnlineInfoModel> jsonOnLineInfoList) {
        if (jsonOnLineInfoList == null) {
            return;
        }
        for (OnlineInfoModel onLineInfoModel : jsonOnLineInfoList) {
            if (!onLineInfoModel.isIsOnline()) {
                MyLog.w(TAG, "用户" + onLineInfoModel.getUserID() + "处于离线状态");
            }
        }
    }

    @Override
    public void updateScrollBarProgress(int volume) {
        mTopContainerView.setScoreProgress(volume);
    }

    @Override
    public void showLeftTime(long wholeTile) {
        MyLog.d(TAG, "showLastedTime" + " wholeTile=" + wholeTile);
        mTopContainerView.startPlayLeftTime(wholeTile);
    }

    @Override
    public void hideMainStage() {
        MyLog.d(TAG, "hideMainStage");
        // 显示end小卡片
        mEndRoundHint.setVisibility(View.VISIBLE);
        mUiHanlder.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 模式改为3，自动播放主舞台退出的svga动画
                mUFOMode = 3;
                mFloatLyricsView.setVisibility(View.GONE);
                mManyLyricsView.setVisibility(View.GONE);
            }
        }, 800);

        if (mRoomData.getRealRoundInfo().getRoundSeq() == 3) {
            // 最后一轮
            mUiHanlder.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startGameEndAniamtion();
                }
            }, 2000);
        }
    }

    @Override
    public void playLyric(SongModel songModel, boolean play) {
        MyLog.w(TAG, "开始播放歌词 songId=" + songModel.getItemID());
        if (songModel == null) {
            MyLog.d(TAG, "songModel 是空的");
            return;
        }
        mPlayingSongModel = songModel;

        if (mPrepareLyricTask != null && !mPrepareLyricTask.isDisposed()) {
            mPrepareLyricTask.dispose();
        }

        File file = SongResUtils.getZRCELyricFileByUrl(songModel.getLyric());

        if (file == null) {
            MyLog.w(TAG, "playLyric is not in local file");
            fetchLyricTask(songModel, play);
        } else {
            MyLog.w(TAG, "playLyric is exist");
            final String fileName = SongResUtils.getFileNameWithMD5(songModel.getLyric());
            parseLyrics(fileName, play);
        }
    }


    private void parseLyrics(String fileName, boolean play) {
        MyLog.w(TAG, "parseLyrics" + " fileName=" + fileName);
        mPrepareLyricTask = LyricsManager.getLyricsManager(getActivity()).loadLyricsObserable(fileName, fileName.hashCode() + "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(10)
                .compose(bindUntilEvent(FragmentEvent.DESTROY))
                .subscribe(lyricsReader -> {
                    drawLyric(fileName.hashCode() + "", lyricsReader, play);
                }, throwable -> {
                    MyLog.e(TAG, throwable);
                });
    }


    private void drawLyric(String fileNameHash, LyricsReader lyricsReader, boolean play) {
        MyLog.w(TAG, "drawLyric" + " fileNameHash=" + fileNameHash + " lyricsReader=" + lyricsReader);
        if (lyricsReader != null) {
            lyricsReader.setHash(fileNameHash);

            //自己
            if (mRoomData.getRealRoundInfo().getUserID()
                    == MyUserInfoManager.getInstance().getUid()) {
                mFloatLyricsView.setVisibility(View.GONE);
                mFloatLyricsView.resetData();
                mManyLyricsView.setVisibility(View.VISIBLE);
                mManyLyricsView.initLrcData();
                lyricsReader.cut(mPlayingSongModel.getRankLrcBeginT(), mPlayingSongModel.getEndMs());
                mManyLyricsView.setLyricsReader(lyricsReader);

                Set<Integer> set = new HashSet<>();
                set.add(lyricsReader.getLineInfoIdByStartTs(mPlayingSongModel.getRankLrcBeginT()));
                mManyLyricsView.setNeedCountDownLine(set);

                if (!play) {
                    mManyLyricsView.seekto(mPlayingSongModel.getBeginMs());
                    mManyLyricsView.pause();
                }
                if (mManyLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mManyLyricsView.getLrcPlayerStatus() != LRCPLAYERSTATUS_PLAY && play) {
                    MyLog.w(TAG, "onEventMainThread " + "play");
                    mManyLyricsView.play(mPlayingSongModel.getBeginMs());
                }
            } else {
                mManyLyricsView.setVisibility(View.GONE);
                mManyLyricsView.resetData();
//                mFloatLyricsView.setVisibility(View.VISIBLE);
                mFloatLyricsView.initLrcData();
                lyricsReader.cut(mPlayingSongModel.getRankLrcBeginT(), mPlayingSongModel.getEndMs());
                mFloatLyricsView.setLyricsReader(lyricsReader);
                if (!play) {
                    mFloatLyricsView.seekto(mPlayingSongModel.getBeginMs());
                    mFloatLyricsView.pause();
                }
                if (mFloatLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mFloatLyricsView.getLrcPlayerStatus() != LRCPLAYERSTATUS_PLAY && play) {
                    MyLog.w(TAG, "onEventMainThread " + "play");
                    mFloatLyricsView.play(mPlayingSongModel.getBeginMs());
                }
            }
        }
    }


    private void fetchLyricTask(SongModel songModel, boolean play) {
        MyLog.w(TAG, "fetchLyricTask" + " songModel=" + songModel);
        mPrepareLyricTask = Observable.create(new ObservableOnSubscribe<File>() {
            @Override
            public void subscribe(ObservableEmitter<File> emitter) {
                File tempFile = new File(SongResUtils.createTempLyricFileName(songModel.getLyric()));

                boolean isSuccess = U.getHttpUtils().downloadFileSync(songModel.getLyric(), tempFile, null);

                File oldName = new File(SongResUtils.createTempLyricFileName(songModel.getLyric()));
                File newName = new File(SongResUtils.createLyricFileName(songModel.getLyric()));

                if (isSuccess) {
                    if (oldName.renameTo(newName)) {
                        MyLog.w(TAG, "已重命名");
                    } else {
                        MyLog.w(TAG, "Error");
                        emitter.onError(new Throwable("重命名错误"));
                    }
                } else {
                    emitter.onError(new Throwable("下载失败"));
                }

                emitter.onNext(newName);
                emitter.onComplete();
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retry(1000)
                .compose(bindUntilEvent(FragmentEvent.DESTROY))
                .subscribe(file -> {
                    final String fileName = SongResUtils.getFileNameWithMD5(songModel.getLyric());
                    parseLyrics(fileName, play);
                }, throwable -> {
                    MyLog.e(TAG, throwable);
                });
    }

    static class PendingRivalData {
        int uid;
        String avatar;

        public PendingRivalData(int uid, String avatar) {
            this.uid = uid;
            this.avatar = avatar;
        }
    }
}
