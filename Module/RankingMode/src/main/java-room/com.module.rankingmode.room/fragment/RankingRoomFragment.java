package com.module.rankingmode.room.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.graphics.drawable.Animatable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

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
import com.facebook.fresco.animation.drawable.AnimatedDrawable2;
import com.facebook.fresco.animation.drawable.AnimationListener;
import com.facebook.imagepipeline.image.ImageInfo;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.model.OnLineInfoModel;
import com.module.rankingmode.room.comment.CommentView;
import com.module.rankingmode.room.model.RoomData;
import com.module.rankingmode.room.presenter.RankingCorePresenter;
import com.module.rankingmode.room.view.BottomContainerView;
import com.module.rankingmode.room.view.IGameRuleView;
import com.module.rankingmode.room.view.InputContainerView;
import com.module.rankingmode.room.view.TopContainerView;
import com.module.rankingmode.room.view.TurnChangeCardView;
import com.module.rankingmode.song.model.SongModel;
import com.zq.lyrics.LyricsManager;
import com.zq.lyrics.LyricsReader;
import com.zq.lyrics.widget.AbstractLrcView;
import com.zq.lyrics.widget.FloatLyricsView;
import com.zq.lyrics.widget.ManyLyricsView;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;

import static com.zq.lyrics.widget.AbstractLrcView.LRCPLAYERSTATUS_PLAY;

public class RankingRoomFragment extends BaseFragment implements IGameRuleView {

    RoomData mRoomData;

    InputContainerView mInputContainerView;

    BottomContainerView mBottomContainerView;

    CommentView mCommentView;

    TopContainerView mTopContainerView;

    RankingCorePresenter mCorePresenter;

    ExTextView mTestTv;

    ManyLyricsView mManyLyricsView;

    FloatLyricsView mFloatLyricsView;

    Handler mUiHanlder = new Handler();

    Disposable mPrepareLyricTask;

    ScrollView mScrollView;

    TurnChangeCardView mTurnChangeView;

    BaseImageView mReadyGoView;

    SongModel mPlayingSongModel;

    boolean mNeedScroll = true;

    boolean mReadyGoPlaying = false;

    HandlerTaskTimer mSelfSingTaskTimer;

    ObjectAnimator mTurnChangeCardShowAnimator;

    ObjectAnimator mTurnChangeCardHideAnimator;

    Runnable mPendingSelfCountDownRunnable;

    int mPendingRivalCountdownUid = -1;

    @Override
    public int initView() {
        return R.layout.ranking_room_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        // 请保证从下面的view往上面的view开始初始化
        mScrollView = mRootView.findViewById(R.id.scrollview);
        initInputView();
        initBottomView();
        initCommentView();
        initTopView();
        initLyricsView();

        mTurnChangeView = mRootView.findViewById(R.id.turn_change_view);

        mTestTv = mRootView.findViewById(R.id.test_tv);
        mTestTv.setMovementMethod(ScrollingMovementMethod.getInstance());

        RxView.clicks(mRootView.findViewById(R.id.tv_control)).subscribe(o -> {
            mNeedScroll = !mNeedScroll;
        });

        RxView.longClicks(mRootView.findViewById(R.id.tv_control)).subscribe(o -> {
            String filename = U.getAppInfoUtils().getMainDir() + File.separator + mRoomData.getGameId() + ".txt";
            File file = new File(filename);
            BufferedSink bufferedSink = null;
            try {
                Sink sink = Okio.sink(file);
                bufferedSink = Okio.buffer(sink);
                bufferedSink.writeString(mTestTv.getText().toString(), Charset.forName("GBK"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (null != bufferedSink) {
                    bufferedSink.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            U.getToastUtil().showShort("导出文件成功");
        });

        showReadyGoView();

        mCorePresenter = new RankingCorePresenter(this, mRoomData);
        addPresent(mCorePresenter);

        showMsg("gameid 是 " + mRoomData.getGameId() + " userid 是 " + MyUserInfoManager.getInstance().getUid());
    }

    public void playShowTurnCardAnimator() {
        mTurnChangeView.setVisibility(View.VISIBLE);
        if (mTurnChangeCardShowAnimator == null) {
            mTurnChangeCardShowAnimator = ObjectAnimator.ofFloat(mTurnChangeView, "translationX", -U.getDisplayUtils().getScreenWidth(), 0);
            mTurnChangeCardShowAnimator.setDuration(1000);

            mTurnChangeCardShowAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mUiHanlder.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            playHideTurnCardAnimator();
                        }
                    }, 2000);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    onAnimationEnd(animation);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    mTurnChangeView.setVisibility(View.VISIBLE);
                }
            });
        }

        mTurnChangeCardShowAnimator.start();
    }

    public void playHideTurnCardAnimator() {
        mTurnChangeView.setVisibility(View.VISIBLE);
        if (mTurnChangeCardHideAnimator == null) {
            mTurnChangeCardHideAnimator = ObjectAnimator.ofFloat(mTurnChangeView, "translationX", 0, -U.getDisplayUtils().getScreenWidth());
            mTurnChangeCardHideAnimator.setDuration(1000);

            mTurnChangeCardHideAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    mTurnChangeView.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    super.onAnimationCancel(animation);
                    onAnimationEnd(animation);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                }
            });
        }

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
        mCommentView.setRoomData(mRoomData);
    }

    private void initTopView() {
        mTopContainerView = mRootView.findViewById(R.id.top_container_view);
        // 加上状态栏的高度
        int statusBarHeight = U.getStatusBarUtil().getStatusBarHeight(getContext());
        RelativeLayout.LayoutParams topLayoutParams = (RelativeLayout.LayoutParams) mTopContainerView.getLayoutParams();
        topLayoutParams.topMargin = statusBarHeight + topLayoutParams.topMargin;
        mTopContainerView.setListener(new TopContainerView.Listener() {
            @Override
            public void closeBtnClick() {
                getActivity().finish();
            }
        });
    }

    private void initLyricsView() {
        mManyLyricsView = mRootView.findViewById(R.id.many_lyrics_view);
        mManyLyricsView.setLrcStatus(AbstractLrcView.LRCSTATUS_LOADING);
        mFloatLyricsView = mRootView.findViewById(R.id.float_lyrics_view);
        mFloatLyricsView.setLrcStatus(AbstractLrcView.LRCSTATUS_LOADING);
    }

    private void showReadyGoView() {
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
                                    MyLog.d(TAG, "onAnimationReset" + " drawable=" + drawable);
                                    onReadyGoOver();
                                }

                                @Override
                                public void onAnimationRepeat(AnimatedDrawable2 drawable) {
                                    MyLog.d(TAG, "onAnimationRepeat" + " drawable=" + drawable);
                                }

                                @Override
                                public void onAnimationFrame(AnimatedDrawable2 drawable, int frameNumber) {
                                    MyLog.d(TAG, "onAnimationFrame" + " drawable=" + drawable + " frameNumber=" + frameNumber);
                                    // 按序列播放  0 1 2 3 4 5 循环再次到  5 - 0 时说明重复播放了
                                    if (frameNumber < curFrame) {
                                        onReadyGoOver();
                                    }
                                    curFrame = frameNumber;
                                }
                            });
                            animatable.start();
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
        MyLog.d(TAG, "onReadyGoOver");
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
                startRivalCountdown(mPendingRivalCountdownUid);
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
        if (mSelfSingTaskTimer != null) {
            mSelfSingTaskTimer.dispose();
        }
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
        if (mReadyGoPlaying) {
            // 正在播放readyGo动画，保存参数，延迟播放卡片
            mPendingSelfCountDownRunnable = countDownOver;
        } else {
            if (mTurnChangeView.setData(mRoomData)) {
                playShowTurnCardAnimator();
            }
            if (mSelfSingTaskTimer != null) {
                mSelfSingTaskTimer.dispose();
            }

            mSelfSingTaskTimer = HandlerTaskTimer.newBuilder()
                    .delay(1000)
                    .interval(1000)
                    .take(4)
                    .start(new HandlerTaskTimer.ObserverW() {
                        @Override
                        public void onNext(Integer integer) {
                            showMsg("你的演唱要开始了，倒计时" + (4 - integer));
                        }

                        @Override
                        public void onComplete() {
                            super.onComplete();
                            countDownOver.run();
                        }
                    });
        }
    }

    /**
     * 保证在主线程
     */
    @Override
    public void startRivalCountdown(int uid) {
        if (mReadyGoPlaying) {
            // 正在播放readyGo动画，保存参数，延迟播放卡片
            mPendingRivalCountdownUid = uid;
        } else {
            showMsg("用户" + uid + "的演唱开始了");
            if (mTurnChangeView.setData(mRoomData)) {
                playShowTurnCardAnimator();
            }

        }
    }

    @Override
    public void userExit() {

    }

    @Override
    public void gameFinish() {
        showMsg("游戏结束了");

        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), EvaluationFragment.class)
                .setAddToBackStack(true)
                .addDataBeforeAdd(0, mRoomData)
                .build()
        );

        if (mSelfSingTaskTimer != null) {
            mSelfSingTaskTimer.dispose();
        }

        if (mPrepareLyricTask != null && !mPrepareLyricTask.isDisposed()) {
            mPrepareLyricTask.dispose();
        }

        mFloatLyricsView.setVisibility(View.GONE);
        mManyLyricsView.setVisibility(View.GONE);
    }

    @Override
    public void updateUserState(List<OnLineInfoModel> jsonOnLineInfoList) {
        if (jsonOnLineInfoList == null) {
            return;
        }
        for (OnLineInfoModel onLineInfoModel : jsonOnLineInfoList) {
            if (!onLineInfoModel.isIsOnline()) {
                showMsg("用户" + onLineInfoModel.getUserID() + "处于离线状态");
            }
        }
    }

    @Override
    public void playLyric(SongModel songModel, boolean play) {
        showMsg("开始播放歌词 songId=" + songModel.getItemID());
        mPlayingSongModel = songModel;

        if (mPrepareLyricTask != null && !mPrepareLyricTask.isDisposed()) {
            mPrepareLyricTask.dispose();
        }

        File file = SongResUtils.getZRCELyricFileByUrl(songModel.getLyric());

        if (file == null) {
            MyLog.d(TAG, "playLyric is not in local file");

            fetchLyricTask(songModel, play);
        } else {
            MyLog.d(TAG, "playLyric is exist");

            final String fileName = SongResUtils.getFileNameWithMD5(songModel.getLyric());
            parseLyrics(fileName, play);
        }
    }


    private void parseLyrics(String fileName, boolean play) {
        MyLog.d(TAG, "parseLyrics" + " fileName=" + fileName);
        mPrepareLyricTask = LyricsManager.getLyricsManager(getActivity()).loadLyricsObserable(fileName, "沙漠骆驼", fileName.hashCode() + "")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(lyricsReader -> {
                    drawLyric(fileName.hashCode() + "", lyricsReader, play);
                }, throwable -> {
                    MyLog.e(TAG, throwable);
                });
    }


    private void drawLyric(String fileNameHash, LyricsReader lyricsReader, boolean play) {
        MyLog.d(TAG, "drawLyric" + " fileNameHash=" + fileNameHash + " lyricsReader=" + lyricsReader);
        if (lyricsReader != null) {
            lyricsReader.setHash(fileNameHash);

            //自己
            if (mRoomData.getRealRoundInfo().getUserID()
                    == MyUserInfoManager.getInstance().getUid()) {
                mFloatLyricsView.setVisibility(View.GONE);
                mManyLyricsView.setVisibility(View.VISIBLE);
                mManyLyricsView.initLrcData();
                mManyLyricsView.setLyricsReader(lyricsReader);
                if (mManyLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mManyLyricsView.getLrcPlayerStatus() != LRCPLAYERSTATUS_PLAY && play) {
                    MyLog.d(TAG, "onEventMainThread " + "play");
                    mManyLyricsView.play(mPlayingSongModel.getBeginMs());
                }
            } else {
                mManyLyricsView.setVisibility(View.GONE);
                mFloatLyricsView.setVisibility(View.VISIBLE);
                mFloatLyricsView.initLrcData();
                mFloatLyricsView.setLyricsReader(lyricsReader);
                if (mFloatLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mFloatLyricsView.getLrcPlayerStatus() != LRCPLAYERSTATUS_PLAY && play) {
                    MyLog.d(TAG, "onEventMainThread " + "play");
                    mFloatLyricsView.play(mPlayingSongModel.getBeginMs());
                }
            }
        }
    }


    private void fetchLyricTask(SongModel songModel, boolean play) {
        MyLog.d(TAG, "fetchLyricTask" + " songModel=" + songModel);
        mPrepareLyricTask = Observable.create(new ObservableOnSubscribe<File>() {
            @Override
            public void subscribe(ObservableEmitter<File> emitter) {
                File tempFile = new File(SongResUtils.createTempLyricFileName(songModel.getLyric()));

                boolean isSuccess = U.getHttpUtils().downloadFileSync(songModel.getLyric(), tempFile, null);

                File oldName = new File(SongResUtils.createTempLyricFileName(songModel.getLyric()));
                File newName = new File(SongResUtils.createLyricFileName(songModel.getLyric()));

                if (isSuccess) {
                    if (oldName.renameTo(newName)) {
                        System.out.println("已重命名");
                    } else {
                        System.out.println("Error");
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

    @Override
    public void showMsg(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            addText(msg);
        } else {
            addText("收到一个空信息");
        }
    }

    void addText(String te) {
        mUiHanlder.post(() -> {
            mTestTv.append(U.getDateTimeUtils().formatTimeStringForDate(System.currentTimeMillis(), "HH:mm:ss:SSS") + ":" + te + "\n");
            if (mNeedScroll) {
                mScrollView.smoothScrollTo(0, mTestTv.getBottom());
            }
        });
    }
}
