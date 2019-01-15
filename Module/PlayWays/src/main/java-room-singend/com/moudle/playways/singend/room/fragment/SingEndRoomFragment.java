package com.moudle.playways.singend.room.fragment;

import android.animation.Animator;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.common.anim.ExObjectAnimator;
import com.common.base.BaseFragment;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.image.fresco.BaseImageView;
import com.common.image.fresco.FrescoWorker;
import com.common.image.fresco.IFrescoCallBack;
import com.common.image.model.ImageFactory;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.SongResUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.recyclerview.RecyclerOnItemClickListener;
import com.facebook.fresco.animation.drawable.AnimatedDrawable2;
import com.facebook.fresco.animation.drawable.AnimationListener;
import com.facebook.imagepipeline.image.ImageInfo;
import com.module.playways.rank.prepare.model.OnlineInfoModel;
import com.module.playways.rank.room.comment.CommentModel;
import com.module.playways.rank.room.comment.CommentView;
import com.module.playways.rank.room.fragment.EvaluationFragment;
import com.module.playways.rank.room.fragment.RankingRecordFragment;
import com.module.playways.rank.room.model.RecordData;
import com.module.playways.rank.room.model.RoomData;
import com.module.playways.rank.room.view.BottomContainerView;
import com.module.playways.rank.room.view.IGameRuleView;
import com.module.playways.rank.room.view.InputContainerView;
import com.module.playways.rank.room.view.TopContainerView;
import com.module.playways.rank.room.view.TurnChangeCardView;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;
import com.moudle.playways.singend.room.presenter.SingEndCorePresenter;
import com.opensource.svgaplayer.SVGADrawable;
import com.opensource.svgaplayer.SVGAImageView;
import com.opensource.svgaplayer.SVGAParser;
import com.opensource.svgaplayer.SVGAVideoEntity;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;
import com.zq.dialog.PersonInfoDialogView;
import com.zq.lyrics.LyricsManager;
import com.zq.lyrics.LyricsReader;
import com.zq.lyrics.widget.AbstractLrcView;
import com.zq.lyrics.widget.FloatLyricsView;
import com.zq.lyrics.widget.ManyLyricsView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

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

public class SingEndRoomFragment extends BaseFragment implements IGameRuleView {

    static final int ENSURE_RUN = 99;

    RoomData mRoomData;

    InputContainerView mInputContainerView;

    BottomContainerView mBottomContainerView;

    CommentView mCommentView;

    TopContainerView mTopContainerView;

    SVGAImageView mTopVoiceBg;

    SingEndCorePresenter mCorePresenter;

    ManyLyricsView mManyLyricsView;

    FloatLyricsView mFloatLyricsView;

    ExTextView mTvPassedTime;

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
            }
        }
    };

    Disposable mPrepareLyricTask;

    ScrollView mScrollView;

    TurnChangeCardView mTurnChangeView;

    BaseImageView mReadyGoView;

    SongModel mPlayingSongModel;

    boolean mNeedScroll = true;

    boolean mReadyGoPlaying = false;

    ExObjectAnimator mTurnChangeCardShowAnimator;

    ExObjectAnimator mTurnChangeCardHideAnimator;

    Runnable mPendingSelfCountDownRunnable;

    int mPendingRivalCountdownUid = -1;

    HandlerTaskTimer mShowLastedTimeTask;

    @Override
    public int initView() {
        return R.layout.singend_room_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        U.getToastUtil().showShort("一唱到底");
        // 请保证从下面的view往上面的view开始初始化
        mScrollView = mRootView.findViewById(R.id.scrollview);
        initInputView();
        initBottomView();
        initCommentView();
        initTopView();
        initLyricsView();
        initTurnChangeView();

        showReadyGoView();

        mCorePresenter = new SingEndCorePresenter(this, mRoomData);
        addPresent(mCorePresenter);

        MyLog.w(TAG, "gameid 是 " + mRoomData.getGameId() + " userid 是 " + MyUserInfoManager.getInstance().getUid());
    }

    public void playShowTurnCardAnimator(Runnable countDownRunnable) {
        mTopVoiceBg.setVisibility(View.GONE);
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
                if (mRoomData.getRealRoundInfo().getUserID() != MyUserInfoManager.getInstance().getUid()) {
                    mTopVoiceBg.setVisibility(View.VISIBLE);
                }
            }
        });
        mTurnChangeCardHideAnimator.start();
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
    }

    boolean isReport = false;

    private void showPersonInfoView(int userID) {
        PersonInfoDialogView personInfoDialogView = new PersonInfoDialogView(getActivity(), userID);

        DialogPlus.newDialog(getActivity())
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
                            U.getToastUtil().showShort("你点击了关注按钮");
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
                .create().show();
    }

    private void showReportView() {
        // TODO: 2018/12/26  等举报完善再写
    }

    private void initTopView() {
        mTopContainerView = mRootView.findViewById(R.id.top_container_view);
        mTvPassedTime = (ExTextView) mRootView.findViewById(R.id.tv_passed_time);

        // 加上状态栏的高度
        int statusBarHeight = U.getStatusBarUtil().getStatusBarHeight(getContext());
        RelativeLayout.LayoutParams topLayoutParams = (RelativeLayout.LayoutParams) mTopContainerView.getLayoutParams();
        topLayoutParams.topMargin = statusBarHeight + topLayoutParams.topMargin;
        mTopContainerView.setListener(new TopContainerView.Listener() {
            @Override
            public void closeBtnClick() {
                getActivity().finish();

            }

            @Override
            public void onVoiceChange(boolean voiceOpen) {
//                mCorePresenter.muteAllRemoteAudioStreams(!voiceOpen);
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
        mTopVoiceBg = (SVGAImageView) mRootView.findViewById(R.id.top_voice_bg);
        mTurnChangeView = mRootView.findViewById(R.id.turn_change_view);

        //TODO 还要修改
        SVGAParser parser = new SVGAParser(getActivity());
        parser.setFileDownloader(new SVGAParser.FileDownloader() {
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
        try {
            parser.parse(new URL(RoomData.ROOM_STAGE_SVGA), new SVGAParser.ParseCompletion() {
                @Override
                public void onComplete(SVGAVideoEntity videoItem) {
                    SVGADrawable drawable = new SVGADrawable(videoItem);
                    mTopVoiceBg.setImageDrawable(drawable);
                    mTopVoiceBg.startAnimation();
                }

                @Override
                public void onError() {

                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    private void showReadyGoView() {
        // TODO: 2019/1/8 也可以用SVGA替换
        if (mReadyGoView == null) {
            mReadyGoView = new BaseImageView(getActivity());
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(U.getDisplayUtils().dip2px(375), U.getDisplayUtils().dip2px(188));
            lp.topMargin = U.getDisplayUtils().dip2px(143);
            ((RelativeLayout) mRootView).addView(mReadyGoView, lp);
        }
        mReadyGoPlaying = true;
        FrescoWorker.loadImage(mReadyGoView, ImageFactory.newHttpImage(RoomData.READY_GO_WEBP_URL)
                        .setCallBack(new IFrescoCallBack() {
                            @Override
                            public void processWithInfo(ImageInfo info, Animatable animatable) {
                                if (animatable != null && animatable instanceof AnimatedDrawable2) {
                                    ((AnimatedDrawable2) animatable).setAnimationListener(new AnimationListener() {
                                        int curFrame = 0;

                                        @Override
                                        public void onAnimationStart(AnimatedDrawable2 drawable) {
                                            MyLog.d(TAG, "onAnimationStart" + " drawable=" + drawable);
                                        }

                                        @Override
                                        public void onAnimationStop(AnimatedDrawable2 drawable) {
                                            MyLog.d(TAG, "onAnimationStop" + " drawable=" + drawable);
                                            onReadyGoOver();
                                        }

                                        @Override
                                        public void onAnimationReset(AnimatedDrawable2 drawable) {
//                                    MyLog.d(TAG, "onAnimationReset" + " drawable=" + drawable);
                                            onReadyGoOver();
                                        }

                                        @Override
                                        public void onAnimationRepeat(AnimatedDrawable2 drawable) {
//                                    MyLog.d(TAG, "onAnimationRepeat" + " drawable=" + drawable);
                                            onReadyGoOver();
                                        }

                                        @Override
                                        public void onAnimationFrame(AnimatedDrawable2 drawable, int frameNumber) {
//                                    MyLog.d(TAG, "onAnimationFrame" + " drawable=" + drawable + " frameNumber=" + frameNumber);
                                            // 按序列播放  0 1 2 3 4 5 循环再次到  5 - 0 时说明重复播放了
                                            if (frameNumber < curFrame) {
                                                onReadyGoOver();
                                            }
                                            curFrame = frameNumber;
                                        }
                                    });
                                    animatable.start();
                                } else {
                                    onReadyGoOver();
                                }
                            }

                            @Override
                            public void processWithFailure() {
                                MyLog.d(TAG, "processWithFailure");
                                onReadyGoOver();
                            }
                        })
                        .build()
        );
    }

    void onReadyGoOver() {
        MyLog.w(TAG, "onReadyGoOver");
        if (mReadyGoPlaying) {
            mReadyGoPlaying = false;
            // 移除 readyGoView
            if (mReadyGoView != null) {
                ((RelativeLayout) mRootView).removeView(mReadyGoView);
            }
            // 轮到自己演唱了，倒计时因为播放readyGo没播放
            if (mPendingSelfCountDownRunnable != null) {
                startSelfCountdown(mPendingSelfCountDownRunnable);
                mPendingSelfCountDownRunnable = null;
            }
            // 轮到他人唱了，倒计时因为播放readyGo没播放
            if (mPendingRivalCountdownUid != -1) {
                startRivalCountdown(mPendingRivalCountdownUid,"");
                mPendingRivalCountdownUid = -1;
            }
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
        mUiHanlder.removeCallbacksAndMessages(null);
        cancelShowLastedTimeTask();
        mManyLyricsView.release();
        mFloatLyricsView.release();
    }

    @Override
    protected boolean onBackPressed() {
        if (mInputContainerView.onBackPressed()) {
            return true;
        }
        return super.onBackPressed();
    }

    /**
     * 保证在主线程
     */
    @Override
    public void startSelfCountdown(Runnable countDownOver) {
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

    }

    /**
     * 保证在主线程
     */
    @Override
    public void startRivalCountdown(int uid,String avatar) {
        cancelShowLastedTimeTask();
        if (mReadyGoPlaying) {
            // 正在播放readyGo动画，保存参数，延迟播放卡片
            mPendingRivalCountdownUid = uid;
        } else {
            MyLog.w(TAG, "用户" + uid + "的演唱开始了");
            if (mTurnChangeView.setData(mRoomData)) {
                playShowTurnCardAnimator(null);
            }
        }
    }

    @Override
    public void userExit() {

    }

    @Override
    public void showRecordView(RecordData recordData) {
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), RankingRecordFragment.class)
                .setAddToBackStack(true)
                .addDataBeforeAdd(0, recordData)
                .addDataBeforeAdd(1, mRoomData)
                .build()
        );
    }

    @Override
    public void showVoteView() {
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), EvaluationFragment.class)
                .setAddToBackStack(true)
                .addDataBeforeAdd(0, mRoomData)
                .build()
        );
    }

    @Override
    public void gameFinish() {
        MyLog.w(TAG, "游戏结束了");

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
        if (mShowLastedTimeTask != null) {
            mShowLastedTimeTask.dispose();
        }

        long lastedTime = wholeTile / 1000;

        MyLog.d(TAG, "showLastedTime" + " lastedTime=" + lastedTime);

        mShowLastedTimeTask = HandlerTaskTimer.newBuilder()
                .interval(1000)
                .take((int) lastedTime + 1)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        long lastTime = lastedTime + 1 - integer;

                        if (lastTime < 0) {
                            cancelShowLastedTimeTask();
                            mTvPassedTime.setText("");
                            return;
                        }

                        mTvPassedTime.setText(U.getDateTimeUtils().formatTimeStringForDate(lastTime * 1000, "mm:ss"));
                    }
                });

    }

    @Override
    public void hideMainStage() {

    }

    private void cancelShowLastedTimeTask() {
        mTvPassedTime.setText("");
        if (mShowLastedTimeTask != null) {
            mShowLastedTimeTask.dispose();
            mShowLastedTimeTask = null;
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
                .retry(10)
                .observeOn(AndroidSchedulers.mainThread())
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
                mManyLyricsView.setLyricsReader(lyricsReader);
                if (mManyLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mManyLyricsView.getLrcPlayerStatus() != LRCPLAYERSTATUS_PLAY && play) {
                    MyLog.w(TAG, "onEventMainThread " + "play");
                    mManyLyricsView.play(mPlayingSongModel.getBeginMs());
                }
            } else {
                mManyLyricsView.setVisibility(View.GONE);
                mManyLyricsView.resetData();
                mFloatLyricsView.setVisibility(View.VISIBLE);
                mFloatLyricsView.initLrcData();
                mFloatLyricsView.setLyricsReader(lyricsReader);
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
                .subscribe(file -> {
                    final String fileName = SongResUtils.getFileNameWithMD5(songModel.getLyric());
                    parseLyrics(fileName, play);
                }, throwable -> {
                    MyLog.e(TAG, throwable);
                });
    }

}
