package com.module.playways.rank.room.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.core.account.UserAccountManager;
import com.common.core.avatar.AvatarUtils;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.permission.SkrAudioPermission;
import com.common.core.userinfo.UserInfoManager;
import com.common.image.fresco.BaseImageView;
import com.common.log.MyLog;
import com.common.statistics.StatConstants;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.FragmentUtils;
import com.common.utils.HttpUtils;
import com.common.utils.SongResUtils;
import com.common.utils.U;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.component.busilib.manager.BgMusicManager;
import com.dialog.view.TipsDialogView;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.listener.SVGAListener;
import com.module.playways.rank.others.LyricAndAccMatchManager;
import com.module.playways.rank.prepare.model.OnlineInfoModel;
import com.module.playways.rank.room.RankRoomData;
import com.module.playways.rank.room.comment.CommentModel;
import com.module.playways.rank.room.comment.CommentView;
import com.module.playways.rank.room.event.RankToVoiceTransformDataEvent;
import com.module.playways.rank.room.gift.GiftBigAnimationViewGroup;
import com.module.playways.rank.room.gift.GiftContinueViewGroup;
import com.module.playways.rank.room.model.RankRoundInfoModel;
import com.module.playways.rank.room.presenter.DownLoadScoreFilePresenter;
import com.module.playways.rank.room.presenter.RankCorePresenter;
import com.module.playways.rank.room.view.ArcProgressBar;
import com.module.playways.rank.room.view.BottomContainerView;
import com.module.playways.rank.room.view.IGameRuleView;
import com.module.playways.rank.room.view.InputContainerView;
import com.module.playways.rank.room.view.RankOpView;
import com.module.playways.rank.room.view.RankTopContainerView2;
import com.module.playways.rank.room.view.RankTopContainerView1;
import com.module.playways.rank.room.view.TurnChangeCardView;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.zq.dialog.PersonInfoDialog;
import com.zq.lyrics.LyricsManager;
import com.zq.lyrics.LyricsReader;
import com.zq.lyrics.event.LyricEventLauncher;
import com.zq.lyrics.widget.AbstractLrcView;
import com.zq.lyrics.widget.ManyLyricsView;
import com.zq.lyrics.widget.VoiceScaleView;

import org.greenrobot.eventbus.EventBus;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
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

    RankRoomData mRoomData;

    RelativeLayout mRankingContainer;

    InputContainerView mInputContainerView;

    BottomContainerView mBottomContainerView;

    CommentView mCommentView;

    RankTopContainerView2 mRankTopContainerView;

    SVGAImageView mStageView;      //主舞台动画，webp形式
    BaseImageView mSingAvatarView; //主舞台中心，歌唱者头像
    ArcProgressBar mCountDownProcess; //主舞台中心，倒计时

    ImageView mEndGameIv;

    RankCorePresenter mCorePresenter;

    DownLoadScoreFilePresenter mDownLoadScoreFilePresenter;

    ManyLyricsView mManyLyricsView;

    DialogPlus mQuitTipsDialog;

    SkrAudioPermission mSkrAudioPermission = new SkrAudioPermission();

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
                onFirstSongGo();
            } else if (SHOW_RIVAL_LYRIC == msg.what) {

            }
        }
    };

    TurnChangeCardView mTurnChangeView;

    PersonInfoDialog mPersonInfoDialog;

    Runnable mPendingSelfCountDownRunnable;

    PendingRivalData mPendingRivalCountdown;

    RankOpView mRankOpView;

    SVGAParser mSVGAParser;

    AnimatorSet mGameEndAnimation;

    VoiceScaleView mVoiceScaleView;

    List<Animator> mAnimatorList = new ArrayList<>();  //存放所有需要尝试取消的动画

    boolean isGameEndAniamtionShow = false; // 标记对战结束动画是否播放

    LyricAndAccMatchManager mLyricAndAccMatchManager = new LyricAndAccMatchManager();

    @Override
    public int initView() {
        return R.layout.rank_room_fragment_layout;
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
        initMainStage();
        initLyricsView();
        initTurnChangeView();
        initGiftDisplayView();
        initOpView();

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
        U.getSoundUtils().preLoad(TAG, R.raw.rank_readygo, R.raw.rank_gameover);
        BgMusicManager.getInstance().setRoom(true);
        MyLog.w(TAG, "gameid 是 " + mRoomData.getGameId() + " userid 是 " + MyUserInfoManager.getInstance().getUid());

        onFirstSongGo();
    }

    private void initMainStage() {
        mStageView = (SVGAImageView) mRootView.findViewById(R.id.stage_view);
        mSingAvatarView = (BaseImageView) mRootView.findViewById(R.id.sing_avatar_view);
        mCountDownProcess = (ArcProgressBar) mRootView.findViewById(R.id.count_down_process);
    }

    @Override
    public void onStart() {
        super.onStart();
        mSkrAudioPermission.ensurePermission(null, true);
    }

    @Override
    public void onResume() {
        super.onResume();
        mSkrAudioPermission.onBackFromPermisionManagerMaybe();
    }

    @Override
    public void hideMainStage() {
        MyLog.d(TAG, "hideMainStage");
        mUiHanlder.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 模式改为3，自动播放主舞台退出的svga动画
//                mUFOMode = 3;
                playhideMainStageAnimator();
                mManyLyricsView.setVisibility(View.GONE);
            }
        }, 800);

        if (mRoomData.getRealRoundInfo().getRoundSeq() == 3) {
            // 最后一轮
            mUiHanlder.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startGameEndAniamtion(false);
                }
            }, 2000);
        }
    }

    private void playhideMainStageAnimator() {
        MyLog.d(TAG, "playhideMainStageAnimator");
        //        // 舞台退出，淡出
        ObjectAnimator objectAnimatorStage = ObjectAnimator.ofFloat(mStageView, View.ALPHA, 1f, 0f);
        objectAnimatorStage.setDuration(1000);
        mAnimatorList.add(objectAnimatorStage);
        objectAnimatorStage.start();

        // 头像退出，淡出
        ObjectAnimator objectAnimatorAvatar = ObjectAnimator.ofFloat(mSingAvatarView, View.ALPHA, 1f, 0f);
        objectAnimatorAvatar.setDuration(1000);
        mAnimatorList.add(objectAnimatorAvatar);
        objectAnimatorAvatar.start();

        // 头像退出，淡出
        ObjectAnimator objectAnimatorTime = ObjectAnimator.ofFloat(mCountDownProcess, View.ALPHA, 1f, 0f);
        objectAnimatorTime.setDuration(1000);
        mAnimatorList.add(objectAnimatorTime);
        objectAnimatorTime.start();

        objectAnimatorStage.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mStageView.stopAnimation(true);
                mStageView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                onAnimationEnd(animator);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        objectAnimatorAvatar.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mSingAvatarView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                onAnimationEnd(animator);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        objectAnimatorTime.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                mCountDownProcess.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                onAnimationEnd(animator);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

//    private void hideWebpStage() {
//        MyLog.d(TAG, "hideWebpStage");
//        // 舞台退出，淡出
//        ObjectAnimator objectAnimatorStage = ObjectAnimator.ofFloat(mStageView, View.ALPHA, 1f, 0f);
//        objectAnimatorStage.setDuration(1000);
//        mAnimatorList.add(objectAnimatorStage);
//        objectAnimatorStage.start();
//
//        // 头像退出，淡出
//        ObjectAnimator objectAnimatorAvatar = ObjectAnimator.ofFloat(mSingAvatarView, View.ALPHA, 1f, 0f);
//        objectAnimatorAvatar.setDuration(1000);
//        mAnimatorList.add(objectAnimatorAvatar);
//        objectAnimatorAvatar.start();
//
//        // 头像退出，淡出
//        ObjectAnimator objectAnimatorTime = ObjectAnimator.ofFloat(mCountDownProcess, View.ALPHA, 1f, 0f);
//        objectAnimatorTime.setDuration(1000);
//        mAnimatorList.add(objectAnimatorTime);
//        objectAnimatorTime.start();
//
//        objectAnimatorStage.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animator) {
//                mStageView.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animator) {
//                onAnimationEnd(animator);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animator) {
//
//            }
//        });
//
//        objectAnimatorAvatar.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animator) {
//                mSingAvatarView.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animator) {
//                onAnimationEnd(animator);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animator) {
//
//            }
//        });
//
//        objectAnimatorTime.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animator) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animator) {
//                mCountDownProcess.setVisibility(View.GONE);
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animator) {
//                onAnimationEnd(animator);
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animator) {
//
//            }
//        });
//    }

    // 播放主舞台动画,入场、循环的离场
    private void playShowMainStageAnimator(int userId) {
        MyLog.d(TAG, "playShowMainStageAnimator");
        mStageView.setVisibility(View.VISIBLE);
        String avatar = mRoomData.getUserInfo(userId).getAvatar();

        mStageView.setLoops(0);
        SVGAParser parser = new SVGAParser(U.app());
        try {
            parser.parse("rank_stage_voice.svga", new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(@NotNull SVGAVideoEntity svgaVideoEntity) {
                    SVGADrawable drawable = new SVGADrawable(svgaVideoEntity);
                    mStageView.setImageDrawable(drawable);
                    mStageView.startAnimation();
                    mSingAvatarView.setVisibility(View.VISIBLE);
                    mCountDownProcess.setVisibility(View.VISIBLE);
                    mCountDownProcess.restart();
                    AvatarUtils.loadAvatarByUrl(mSingAvatarView,
                            AvatarUtils.newParamsBuilder(avatar)
                                    .setCircle(true)
                                    .build());
                    ObjectAnimator objectAnimatorStage = ObjectAnimator.ofFloat(mStageView, View.ALPHA, 0f, 1f);
                    objectAnimatorStage.setDuration(1000);
                    objectAnimatorStage.start();
                    mAnimatorList.add(objectAnimatorStage);

                    ObjectAnimator objectAnimatorAvatar = ObjectAnimator.ofFloat(mSingAvatarView, View.ALPHA, 0f, 1f);
                    objectAnimatorAvatar.setDuration(1000);
                    objectAnimatorAvatar.start();
                    mAnimatorList.add(objectAnimatorAvatar);

                    ObjectAnimator objectAnimatorTime = ObjectAnimator.ofFloat(mCountDownProcess, View.ALPHA, 0f, 1f);
                    objectAnimatorTime.setDuration(1000);
                    objectAnimatorTime.start();
                    mAnimatorList.add(objectAnimatorTime);
                }

                @Override
                public void onError() {

                }
            });
        } catch (Exception e) {
            System.out.print(true);
        }

        RxView.clicks(mSingAvatarView)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        showPersonInfoView(userId);
                    }
                });

    }

//    private void playWebpMainStage() {
//        mStageView.setVisibility(View.VISIBLE);
//        int userId = mRoomData.getRealRoundInfo().getUserID();
//        String avatar = mRoomData.getUserInfo(userId).getAvatar();
//        FrescoWorker.loadImage(mStageView, ImageFactory.newHttpImage(RoomData.PK_MAIN_STAGE_WEBP)
//                .setCallBack(new IFrescoCallBack() {
//                    @Override
//                    public void processWithInfo(ImageInfo info, Animatable animatable) {
//                        if (animatable != null && animatable instanceof AnimatedDrawable2) {
//                            ((AnimatedDrawable2) animatable).setAnimationListener(new AnimationListener() {
//
//                                @Override
//                                public void onAnimationStart(AnimatedDrawable2 drawable) {
//                                    MyLog.d(TAG, "onAnimationStart" + " drawable=" + drawable);
//                                    mSingAvatarView.setVisibility(View.VISIBLE);
//                                    mCountDownProcess.setVisibility(View.VISIBLE);
//                                    // TODO: 2019/2/20  测试UI
//                                    mCountDownProcess.setProgress(75);
//                                    AvatarUtils.loadAvatarByUrl(mSingAvatarView,
//                                            AvatarUtils.newParamsBuilder(avatar)
//                                                    .setCircle(true)
//                                                    .build());
//                                    ObjectAnimator objectAnimatorStage = ObjectAnimator.ofFloat(mStageView, View.ALPHA, 0f, 1f);
//                                    objectAnimatorStage.setDuration(1000);
//                                    objectAnimatorStage.start();
//                                    mAnimatorList.add(objectAnimatorStage);
//
//                                    ObjectAnimator objectAnimatorAvatar = ObjectAnimator.ofFloat(mSingAvatarView, View.ALPHA, 0f, 1f);
//                                    objectAnimatorAvatar.setDuration(1000);
//                                    objectAnimatorAvatar.start();
//                                    mAnimatorList.add(objectAnimatorAvatar);
//
//                                    ObjectAnimator objectAnimatorTime = ObjectAnimator.ofFloat(mCountDownProcess, View.ALPHA, 0f, 1f);
//                                    objectAnimatorTime.setDuration(1000);
//                                    objectAnimatorTime.start();
//                                    mAnimatorList.add(objectAnimatorTime);
//                                }
//
//                                @Override
//                                public void onAnimationStop(AnimatedDrawable2 drawable) {
//                                    MyLog.d(TAG, "onAnimationStop" + " drawable=" + drawable);
//                                }
//
//                                @Override
//                                public void onAnimationReset(AnimatedDrawable2 drawable) {
//                                }
//
//                                @Override
//                                public void onAnimationRepeat(AnimatedDrawable2 drawable) {
//
//                                }
//
//                                @Override
//                                public void onAnimationFrame(AnimatedDrawable2 drawable, int frameNumber) {
//                                }
//                            });
//                            animatable.start();
//                        }
//                    }
//
//                    @Override
//                    public void processWithFailure() {
//                        MyLog.d(TAG, "processWithFailure");
//                    }
//                })
//                .build()
//        );
//    }

    private void initInputView() {
        mInputContainerView = mRootView.findViewById(R.id.input_container_view);
        mInputContainerView.setRoomData(mRoomData);
    }

    private void initBottomView() {
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
        layoutParams.height = U.getDisplayUtils().getPhoneHeight() - U.getDisplayUtils().dip2px(370 + 60);

    }


    private void showPersonInfoView(int userID) {
        if (!U.getNetworkUtils().hasNetwork()) {
            U.getToastUtil().showShort("网络异常，请检查网络后重试!");
            return;
        }
        mInputContainerView.hideSoftInput();

        mPersonInfoDialog = new PersonInfoDialog(getActivity(), userID, true, false);
        mPersonInfoDialog.show();
    }

    private void initTopView() {
        mRankTopContainerView = mRootView.findViewById(R.id.rank_top_view);
        mRankTopContainerView.setRoomData(mRoomData);

        mRankTopContainerView.setListener(new RankTopContainerView1.Listener() {
            @Override
            public void closeBtnClick() {
                quitGame();
            }

            @Override
            public void onVoiceChange(boolean voiceOpen) {
                mCorePresenter.muteAllRemoteAudioStreams(!voiceOpen, true);
            }
        });

    }

    private void initLyricsView() {
        mManyLyricsView = mRootView.findViewById(R.id.many_lyrics_view);
        mManyLyricsView.setLrcStatus(AbstractLrcView.LRCSTATUS_LOADING);
        mVoiceScaleView = mRootView.findViewById(R.id.voice_scale_view);
    }

    private void initTurnChangeView() {
        mTurnChangeView = mRootView.findViewById(R.id.turn_change_view);
        mEndGameIv = (ImageView) mRootView.findViewById(R.id.end_game_iv);
    }

    private void initGiftDisplayView() {
        GiftContinueViewGroup giftContinueViewGroup = mRootView.findViewById(R.id.gift_continue_vg);
        giftContinueViewGroup.setRoomData(mRoomData);
        GiftBigAnimationViewGroup giftBigAnimationViewGroup = mRootView.findViewById(R.id.gift_big_animation_vg);
        giftBigAnimationViewGroup.setRoomData(mRoomData);
    }

    private void initOpView() {
        mRankOpView = mRootView.findViewById(R.id.rank_op_view);
        mRankOpView.setRoomData(mRoomData);
        mRankOpView.setOpListener(new RankOpView.OpListener() {
            @Override
            public void clickBurst(int seq) {
                mCorePresenter.sendBurst(seq);
            }

            @Override
            public void clickLightOff(int seq) {
                mCorePresenter.sendLightOff(seq);
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

    private void onFirstSongGo() {
        MyLog.w(TAG, "onFirstSongGo");
        // 轮到自己演唱了，倒计时因为播放readyGo没播放
        if (mPendingSelfCountDownRunnable != null) {
            startSelfCountdown(null, mPendingSelfCountDownRunnable);
            mPendingSelfCountDownRunnable = null;
        }
        // 轮到他人唱了，倒计时因为播放readyGo没播放
        if (mPendingRivalCountdown != null) {
            startRivalCountdown(null, mPendingRivalCountdown.uid, mPendingRivalCountdown.avatar);
            mPendingRivalCountdown = null;
        }
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mRoomData = (RankRoomData) data;
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        MyLog.d(TAG, "destroy");
        destroyAnimation();
        if (mPersonInfoDialog != null && mPersonInfoDialog.isShowing()) {
            mPersonInfoDialog.dismiss();
            mPersonInfoDialog = null;
        }
        mUiHanlder.removeCallbacksAndMessages(null);
        if (mManyLyricsView != null) {
            mManyLyricsView.release();
        }
//        mFloatLyricsView.release();

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

    private void quitGame() {
        if (mQuitTipsDialog == null) {
            TipsDialogView tipsDialogView = new TipsDialogView.Builder(getContext())
                    .setMessageTip("提前退出会破坏其他玩家的对局体验\n确定退出么？")
                    .setConfirmTip("确定")
                    .setCancelTip("取消")
                    .setConfirmBtnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mQuitTipsDialog.dismiss(false);
                            if (getActivity() != null) {
                                getActivity().finish();
                            }
                            StatisticsAdapter.recordCountEvent(UserAccountManager.getInstance().getGategory(StatConstants.CATEGORY_RANK), "game_exit", null);
                        }
                    })
                    .setCancelBtnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mQuitTipsDialog.dismiss(false);
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
        mBottomContainerView.dismissPopWindow();
        mQuitTipsDialog.show();
    }

    /**
     * 保证在主线程
     */
    @Override
    public void startSelfCountdown(RankRoundInfoModel lastRoundInfoModel, Runnable countDownOver) {
        MyLog.d(TAG, "startSelfCountdown" + " countDownOver=" + countDownOver);
        mRankTopContainerView.roundOver(lastRoundInfoModel);
        mRankOpView.setVisibility(View.GONE);
        mManyLyricsView.setVisibility(View.GONE);

        mLyricAndAccMatchManager.stop();
        // 加保护，确保当前主舞台一定被移除
        mStageView.setVisibility(View.GONE);
        mSingAvatarView.setVisibility(View.GONE);
        mCountDownProcess.setVisibility(View.GONE);

        // 确保演唱逻辑一定要执行
        Message msg = mUiHanlder.obtainMessage(ENSURE_RUN);
        msg.what = ENSURE_RUN;
        msg.obj = countDownOver;
        mUiHanlder.removeMessages(ENSURE_RUN);
        mUiHanlder.sendMessageDelayed(msg, 5000);

        int seq = 0;
        if (mRoomData.getRealRoundInfo() != null) {
            seq = mRoomData.getRealRoundInfo().getRoundSeq();
        }
        if (seq == 1) {
            mUiHanlder.postDelayed(new Runnable() {
                @Override
                public void run() {
                    U.getSoundUtils().play(TAG, R.raw.rank_readygo);
                }
            }, 500);
        }

        mTurnChangeView.setData(mRoomData, new SVGAListener() {
            @Override
            public void onFinished() {
                if (countDownOver != null) {
                    mUiHanlder.removeMessages(ENSURE_RUN);
                    countDownOver.run();
                }
            }
        });
        mUiHanlder.removeMessages(SHOW_RIVAL_LYRIC);
    }

    @Override
    public void onOtherStartSing(SongModel songModel) {
        mRankOpView.playCountDown(mRoomData.getRealRoundSeq(), true);
        mCountDownProcess.startCountDown(0, songModel.getTotalMs());
        if (mManyLyricsView != null) {
            mManyLyricsView.setVisibility(View.GONE);
        }
    }

    /**
     * 保证在主线程
     */
    @Override
    public void startRivalCountdown(RankRoundInfoModel lastRoundInfoModel, int uid, String avatar) {
        MyLog.d(TAG, "startRivalCountdown" + " uid=" + uid + " avatar=" + avatar);
        mCountDownProcess.restart();
        mRankTopContainerView.roundOver(lastRoundInfoModel);
        mRankOpView.setVisibility(View.VISIBLE);
        mRankOpView.playCountDown(mRoomData.getRealRoundSeq(), false);
        mVoiceScaleView.setVisibility(View.GONE);
        mManyLyricsView.setVisibility(View.GONE);
        mLyricAndAccMatchManager.stop();
        // 加保护，确保当前主舞台一定被移除
        mStageView.setVisibility(View.GONE);
        mSingAvatarView.setVisibility(View.GONE);
        mCountDownProcess.setVisibility(View.GONE);
//        mRankOpView.playCountDown(mRoomData.getRealRoundSeq());

//        mTopContainerView.cancelShowLastedTimeTask();
        MyLog.w(TAG, "用户" + uid + "的演唱开始了");
        if (mRoomData == null) {
            MyLog.w(TAG, "mRoomData为null");
            return;
        }

        int seq = mRoomData.getRealRoundInfo().getRoundSeq();
        if (seq == 1) {
            mUiHanlder.postDelayed(new Runnable() {
                @Override
                public void run() {
                    U.getSoundUtils().play(TAG, R.raw.rank_readygo);
                }
            }, 500);
        }
        mTurnChangeView.setData(mRoomData, new SVGAListener() {
            @Override
            public void onFinished() {
                playShowMainStageAnimator(uid);
            }
        });

        mUiHanlder.removeMessages(SHOW_RIVAL_LYRIC);
        Message showLyricMsg = new Message();
        showLyricMsg.what = SHOW_RIVAL_LYRIC;
        mUiHanlder.sendMessageDelayed(showLyricMsg, 3000);
    }

    @Override
    public void userExit() {

    }

    @Override
    public void finishActivity() {
        if (getActivity() != null) {
            getActivity().finish();
        }
    }

    private void startGameEndAniamtion(boolean isGameOver) {
        // 提前加载音效
        U.getSoundUtils().preLoad(RankLevelChange2Fragment.TAG, R.raw.rank_win, R.raw.rank_lose);
        if (isGameEndAniamtionShow) {
            // 动画已经在播放
            if (isGameOver) {
                // 游戏结束了
                showRankLevelChange();
            }
            return;
        }
        isGameEndAniamtionShow = true;

        destroyAnimation();
        U.getSoundUtils().play(TAG, R.raw.rank_gameover);
        // 对战结束动画
        mEndGameIv.setVisibility(View.VISIBLE);
        if (mGameEndAnimation == null) {
            ObjectAnimator a1 = ObjectAnimator.ofFloat(mEndGameIv, View.SCALE_X, 0.3f, 1f);
            ObjectAnimator a2 = ObjectAnimator.ofFloat(mEndGameIv, View.SCALE_Y, 0.3f, 1f);
            mGameEndAnimation = new AnimatorSet();
            mGameEndAnimation.setDuration(750);
            mGameEndAnimation.playTogether(a1, a2);
        }
        mGameEndAnimation.removeAllListeners();
        mGameEndAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mUiHanlder.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (mPersonInfoDialog != null && mPersonInfoDialog.isShowing()) {
                            mPersonInfoDialog.dismiss();
                        }

                        if (isGameOver) {
                            showRankLevelChange();
                            return;
                        }
                    }
                }, 2250);
            }
        });
        mGameEndAnimation.start();
    }

    private void showRankLevelChange() {
        RankToVoiceTransformDataEvent event = new RankToVoiceTransformDataEvent();
        event.mCommentModelList = mCommentView.getComments();
        EventBus.getDefault().removeStickyEvent(RankToVoiceTransformDataEvent.class);
        EventBus.getDefault().postSticky(event);
        // 先播放段位动画
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), RankLevelChange2Fragment.class)
                .setAddToBackStack(true)
                .setHasAnimation(false)
                .addDataBeforeAdd(1, mRoomData)
                .build());
    }

    private void destroyAnimation() {
        MyLog.d(TAG, "destroyAnimation");

        if (mStageView != null) {
            mStageView.setCallback(null);
            mStageView.stopAnimation(true);
            mStageView.setVisibility(View.GONE);
        }

        if (mSingAvatarView != null && mSingAvatarView.getVisibility() == View.VISIBLE) {
            mSingAvatarView.setVisibility(View.GONE);
        }
    }


    @Override
    public void gameFinish() {
        MyLog.w(TAG, "游戏结束了");
        mLyricAndAccMatchManager.stop();
        mRankOpView.setVisibility(View.GONE);
        mManyLyricsView.setVisibility(View.GONE);
        mManyLyricsView.release();
        mVoiceScaleView.setVisibility(View.GONE);
        mRankTopContainerView.onGameFinish();
        startGameEndAniamtion(true);
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
    public void updateScrollBarProgress(int score, int curTotalScore, int lineNum) {
        mRankTopContainerView.setScoreProgress(score, curTotalScore, lineNum);
    }

    @Override
    public void showLeftTime(long wholeTile) {
        MyLog.d(TAG, "showLastedTime" + " wholeTile=" + wholeTile);
    }

    @Override
    public void playLyric(SongModel songModel) {
        if (songModel == null) {
            MyLog.d(TAG, "songModel 是空的");
            return;
        }
        MyLog.w(TAG, "开始播放歌词 songId=" + songModel.getItemID());

        mLyricAndAccMatchManager.setArgs(mManyLyricsView,mVoiceScaleView,
                songModel.getLyric(),songModel.getRankLrcBeginT(),songModel.getRankLrcEndT(),
                songModel.getBeginMs(),songModel.getEndMs());
        mLyricAndAccMatchManager.start(new LyricAndAccMatchManager.Listener() {
            @Override
            public void onLyricParseSuccess() {

            }

            @Override
            public void onLyricParseFailed() {

            }

            @Override
            public void onLyricEventPost(int eventNum) {
                mRoomData.setSongLineNum(eventNum);
                mCorePresenter.sendTotalScoreToOthers(eventNum);
            }
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
