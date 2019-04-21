package com.module.playways.grab.room.view.normal;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.log.MyLog;
import com.common.rx.RxRetryAssist;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.SongResUtils;
import com.common.utils.U;
import com.common.view.countdown.CircleCountDownView;
import com.component.busilib.view.BitmapTextView;
import com.engine.EngineManager;
import com.engine.arccloud.ArcRecognizeListener;
import com.engine.arccloud.SongInfo;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.module.playways.others.LyricAndAccMatchManager;
import com.module.playways.room.song.model.SongModel;
import com.module.rank.R;
import com.zq.live.proto.Room.EWantSingType;
import com.zq.lyrics.widget.ManyLyricsView;
import com.zq.lyrics.widget.VoiceScaleView;

import java.io.File;
import java.io.IOException;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okio.BufferedSource;
import okio.Okio;

/**
 * 你的主场景歌词
 */
public class NormalSelfSingCardView extends RelativeLayout {
    public final static String TAG = "SelfSingCardView2";

    TextView mTvLyric;
    BitmapTextView mCountDownTv;
    ManyLyricsView mManyLyricsView;

    Disposable mDisposable;
    HandlerTaskTimer mCounDownTask;

    GrabRoomData mRoomData;
    SongModel mSongModel;

    ImageView mIvTag;
    ImageView mIvChallengeIcon;

    CircleCountDownView mCircleCountDownView;

    VoiceScaleView mVoiceScaleView;

    LyricAndAccMatchManager mLyricAndAccMatchManager = new LyricAndAccMatchManager();


    public NormalSelfSingCardView(Context context) {
        super(context);
        init();
    }

    public NormalSelfSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public NormalSelfSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_normal_self_sing_card_layout, this);
        mTvLyric = findViewById(R.id.tv_lyric);
        mManyLyricsView = (ManyLyricsView) findViewById(R.id.many_lyrics_view);
        mCircleCountDownView = (CircleCountDownView) findViewById(R.id.circle_count_down_view);
        mCountDownTv = (BitmapTextView) findViewById(R.id.count_down_tv);
        mIvTag = (ImageView) findViewById(R.id.iv_tag);
        mVoiceScaleView = (VoiceScaleView) findViewById(R.id.voice_scale_view);
        mIvChallengeIcon = (ImageView) findViewById(R.id.iv_challenge_icon);
    }

    public void playLyric(GrabRoundInfoModel infoModel, boolean accEnable) {
        if (infoModel == null) {
            MyLog.d(TAG, "infoModel 是空的");
            return;
        }
        if (infoModel.getWantSingType() == EWantSingType.EWST_COMMON_OVER_TIME.getValue()
                || infoModel.getWantSingType() == EWantSingType.EWST_ACCOMPANY_OVER_TIME.getValue()) {
            mIvChallengeIcon.setVisibility(VISIBLE);
        } else {
            mIvChallengeIcon.setVisibility(INVISIBLE);
        }
        mSongModel = infoModel.getMusic();
        mTvLyric.setText("歌词加载中...");
        mTvLyric.setVisibility(VISIBLE);
        mManyLyricsView.setVisibility(GONE);
        mManyLyricsView.initLrcData();
        mVoiceScaleView.setVisibility(View.GONE);
        if (mSongModel == null) {
            MyLog.d(TAG, "songModel 是空的");
            return;
        }
        /**
         * 该轮次的总时间，之前用的是歌曲内的总时间，但是不灵活，现在都放在服务器的轮次信息的 begin 和 end 里
         *
         */
        int totalTs = infoModel.getSingEndMs() - infoModel.getSingBeginMs();
        if (totalTs <= 0) {
            MyLog.d(TAG, "playLyric" + " totalTs时间不合法,做矫正, infoModel=" + infoModel);
            if (infoModel.getWantSingType() == 0) {
                totalTs = 20 * 1000;
            } else if (infoModel.getWantSingType() == 1) {
                totalTs = 30 * 1000;
            } else if (infoModel.getWantSingType() == 2) {
                totalTs = 40 * 1000;
            } else if (infoModel.getWantSingType() == 3) {
                totalTs = 50 * 1000;
            }
        }
        boolean withAcc = false;
        if (infoModel.isAccRound() && accEnable) {
            withAcc = true;
        }
        if (!withAcc) {
            playWithNoAcc(mSongModel);
            mIvTag.setBackground(U.getDrawable(R.drawable.ycdd_daojishi_qingchang));
            mLyricAndAccMatchManager.stop();
        } else {
            mIvTag.setBackground(U.getDrawable(R.drawable.ycdd_daojishi_banzou));
            mLyricAndAccMatchManager.setArgs(mManyLyricsView, mVoiceScaleView,
                    mSongModel.getLyric(),
                    mSongModel.getStandLrcBeginT(), mSongModel.getStandLrcBeginT() + totalTs,
                    mSongModel.getBeginMs(), mSongModel.getBeginMs() + totalTs);
            mLyricAndAccMatchManager.start(new LyricAndAccMatchManager.Listener() {
                @Override
                public void onLyricParseSuccess() {
                    mTvLyric.setVisibility(GONE);
                }

                @Override
                public void onLyricParseFailed() {
                    playWithNoAcc(mSongModel);
                }

                @Override
                public void onLyricEventPost(int lineNum) {
                    mRoomData.setSongLineNum(lineNum);
                }

            });
            EngineManager.getInstance().setRecognizeListener(new ArcRecognizeListener() {
                @Override
                public void onResult(String result, List<SongInfo> list, SongInfo targetSongInfo, int lineNo) {
                    mLyricAndAccMatchManager.onAcrResult(result, list, targetSongInfo, lineNo);
                }
            });
        }
        starCounDown(totalTs);
    }

    private void playWithNoAcc(SongModel songModel) {
        if (songModel == null) {
            return;
        }
        mManyLyricsView.setVisibility(GONE);
        mTvLyric.setVisibility(VISIBLE);
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        File file = SongResUtils.getGrabLyricFileByUrl(songModel.getStandLrc());
        if (file == null || !file.exists()) {
            MyLog.w(TAG, "playLyric is not in local file");
            fetchLyricTask(songModel);
        } else {
            MyLog.w(TAG, "playLyric is exist");
            drawLyric(file);
        }
    }


    private void starCounDown(int totalMs) {
        mCountDownTv.setVisibility(VISIBLE);
        mCircleCountDownView.go(0, totalMs);
        int counDown = totalMs / 1000;
        mCounDownTask = HandlerTaskTimer.newBuilder()
                .interval(1000)
                .take(counDown)
                .start(new HandlerTaskTimer.ObserverW() {
                    @Override
                    public void onNext(Integer integer) {
                        mCountDownTv.setText((counDown - integer) + "");
                    }

                    @Override
                    public void onComplete() {
                        super.onComplete();
                        if (mListener != null) {
                            mListener.onSelfSingOver();
                        }
                        stopCounDown();
//                        mCountDownTv.setVisibility(GONE);
                    }
                });
    }

    private void stopCounDown() {
        if (mCounDownTask != null) {
            mCounDownTask.dispose();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopCounDown();
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        if (mLyricAndAccMatchManager != null) {
            mLyricAndAccMatchManager.stop();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == GONE) {
            stopCounDown();
            if (mManyLyricsView != null) {
                mManyLyricsView.setLyricsReader(null);
            }
            if (mLyricAndAccMatchManager != null) {
                mLyricAndAccMatchManager.stop();
            }
        }
    }

    /**
     * 拉取一唱到底的歌词字符串
     *
     * @param songModel
     */
    private void fetchLyricTask(SongModel songModel) {
        MyLog.w(TAG, "fetchLyricTask" + " songModel=" + songModel);
        mDisposable = Observable.create(new ObservableOnSubscribe<File>() {
            @Override
            public void subscribe(ObservableEmitter<File> emitter) {
                File tempFile = new File(SongResUtils.createStandLyricTempFileName(songModel.getStandLrc()));
                boolean isSuccess = U.getHttpUtils().downloadFileSync(songModel.getStandLrc(), tempFile, null);
                File oldName = new File(SongResUtils.createStandLyricTempFileName(songModel.getStandLrc()));
                File newName = new File(SongResUtils.createStandLyricFileName(songModel.getStandLrc()));
                if (isSuccess) {
                    if (oldName != null && oldName.renameTo(newName)) {
                        MyLog.w(TAG, "已重命名");
                        emitter.onNext(newName);
                        emitter.onComplete();
                    } else {
                        MyLog.w(TAG, "Error");
                        emitter.onError(new Throwable("重命名错误"));
                    }
                } else {
                    emitter.onError(new Throwable("下载失败, 文件地址是" + songModel.getStandLrc()));
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .retryWhen(new RxRetryAssist(5, 1, false))
//                .compose(bindUntilEvent(FragmentEvent.DESTROY))
                .subscribe(file -> {
                    final File fileName = SongResUtils.getGrabLyricFileByUrl(songModel.getStandLrc());
                    drawLyric(fileName);
                }, throwable -> {
                    MyLog.e(TAG, throwable);
                });
    }

    public void destroy() {
        if (mManyLyricsView != null) {
            mManyLyricsView.release();
        }
    }

    /**
     * 画出一唱到底歌词字符串
     *
     * @param file
     */
    private void drawLyric(final File file) {
        MyLog.w(TAG, "file is " + file);
        Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> emitter) {
                if (file != null && file.exists() && file.isFile()) {
                    try (BufferedSource source = Okio.buffer(Okio.source(file))) {
                        emitter.onNext(source.readUtf8());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                emitter.onComplete();
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new Consumer<String>() {
            @Override
            public void accept(String o) {
                mTvLyric.setText(o);
            }
        }, throwable -> MyLog.e(TAG, throwable));
    }

    public void setRoomData(GrabRoomData roomData) {
        mRoomData = roomData;
    }

    SelfSingCardView.Listener mListener;

    public void setListener(SelfSingCardView.Listener l) {
        mListener = l;
    }


}
