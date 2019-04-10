package com.module.playways.grab.room.view;

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
import com.common.view.ex.ExTextView;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.others.LyricAndAccMatchManager;
import com.module.playways.room.song.model.SongModel;
import com.module.rank.R;
import com.zq.lyrics.widget.ManyLyricsView;
import com.zq.lyrics.widget.VoiceScaleView;

import java.io.File;
import java.io.IOException;

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
public class SelfSingCardView2 extends RelativeLayout {
    public final static String TAG = "SelfSingCardView2";

    TextView mTvLyric;
    ExTextView mCountDownTv;
    ManyLyricsView mManyLyricsView;

    Disposable mDisposable;
    HandlerTaskTimer mCounDownTask;

    GrabRoomData mRoomData;
    SongModel mSongModel;

    ImageView mIvTag;

    CircleCountDownView mCircleCountDownView;

    VoiceScaleView mVoiceScaleView;

    LyricAndAccMatchManager mLyricAndAccMatchManager = new LyricAndAccMatchManager();


    public SelfSingCardView2(Context context) {
        super(context);
        init();
    }

    public SelfSingCardView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SelfSingCardView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_self_sing_card_layout_two, this);
        mTvLyric = findViewById(R.id.tv_lyric);
        mManyLyricsView = (ManyLyricsView) findViewById(R.id.many_lyrics_view);
        mCircleCountDownView = (CircleCountDownView) findViewById(R.id.circle_count_down_view);
        mCountDownTv = (ExTextView) findViewById(R.id.count_down_tv);
        mIvTag = (ImageView) findViewById(R.id.iv_tag);
        mVoiceScaleView = (VoiceScaleView) findViewById(R.id.voice_scale_view);
    }

    public void playLyric(GrabRoundInfoModel infoModel, boolean hasAcc) {
        if (infoModel == null) {
            MyLog.d(TAG, "infoModel 是空的");
            return;
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
        if (!hasAcc) {
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
        mCircleCountDownView.go(totalMs);
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

    Listener mListener;

    public void setListener(Listener l) {
        mListener = l;
    }

    public interface Listener {
        void onSelfSingOver();
    }

}
