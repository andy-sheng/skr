package com.module.playways.others;

import android.os.Handler;
import android.os.Message;
import android.view.View;

import com.common.log.MyLog;
import com.common.utils.U;
import com.engine.EngineEvent;
import com.engine.EngineManager;
import com.zq.lyrics.LyricsManager;
import com.zq.lyrics.LyricsReader;
import com.zq.lyrics.event.LyricEventLauncher;
import com.zq.lyrics.widget.AbstractLrcView;
import com.zq.lyrics.widget.ManyLyricsView;
import com.zq.lyrics.widget.VoiceScaleView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashSet;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.zq.lyrics.widget.AbstractLrcView.LRCPLAYERSTATUS_PLAY;

/**
 * 这个类的职责是负责
 * 歌词与伴奏 以及 歌词结束时间 以及歌词条运动动画 的完整匹配
 * 因为 调音间 排位赛 一adb唱到底伴奏模式 很多地方都要有类似的校准
 * 特别是 加入 agora token 验证后，播放音乐就有延迟了
 */
public class LyricAndAccMatchManager {
    public final static String TAG = "LyricAndAccMatchManager";
    static final int MSG_ENSURE_LAUNCHER = 1;
    static final int LAUNCHER_DELAY = 5000;
    ManyLyricsView mManyLyricsView; // 歌词显示用的view
    VoiceScaleView mVoiceScaleView; // 歌词长度滚动view
    String mLyricUrl; // 伴奏url
    int mLyricBeginTs, mLyricEndTs; // 歌词开始与结束时间 相对于完整的歌
    int mAccBeginTs, mAccEndTs; // 伴奏开始与结束时间 相对于完整的歌

    Disposable mDisposable;
    Listener mListener;

    LyricEventLauncher mLyricEventLauncher = new LyricEventLauncher();
    LyricsReader mLyricsReader;
    // 按理 歌词 和 伴奏 都ok了 才抛出歌词end事件，但事件的时间戳要做矫正
    boolean mAccLoadOk = false;
    boolean mLrcLoadOk = false;
    boolean mHasLauncher = false;

    Handler mUiHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == MSG_ENSURE_LAUNCHER) {
                MyLog.d(TAG, "handleMessage acc 加载超时，不等了，直接发事件");
                launchLyricEvent(LAUNCHER_DELAY);
            }
        }
    };

    public void setArgs(ManyLyricsView manyLyricsView,
                        VoiceScaleView voiceScaleView,
                        String lyricUrl,
                        int lyricBeginTs,
                        int lyricEndTs,
                        int accBeginTs,
                        int accEndTs
    ) {
        mManyLyricsView = manyLyricsView;
        mVoiceScaleView = voiceScaleView;
        mLyricUrl = lyricUrl;
        mLyricBeginTs = lyricBeginTs;
        mLyricEndTs = lyricEndTs;
        mAccBeginTs = accBeginTs;
        mAccEndTs = accEndTs;
        mAccLoadOk = false;
        mLrcLoadOk = false;
        mHasLauncher = false;
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public void start(Listener l) {
        mListener = l;
        parseLyric();
    }

    public void stop() {
        EventBus.getDefault().unregister(this);
        mUiHandler.removeCallbacksAndMessages(null);
        mLyricEventLauncher.destroy();
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mListener = null;
    }


    private void parseLyric() {
        if (mDisposable != null) {
            mDisposable.dispose();
        }
        mDisposable = LyricsManager.getLyricsManager(U.app())
                .fetchAndLoadLyrics(mLyricUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<LyricsReader>() {
                    @Override
                    public void accept(LyricsReader lyricsReader) throws Exception {
                        MyLog.w(TAG, "onEventMainThread " + "play");
                        if (mManyLyricsView != null) {
                            mManyLyricsView.setVisibility(View.VISIBLE);
                            mManyLyricsView.initLrcData();
                        }
                        lyricsReader.cut(mLyricBeginTs, mLyricEndTs);
                        mManyLyricsView.setLyricsReader(lyricsReader);
                        Set<Integer> set = new HashSet<>();
                        set.add(lyricsReader.getLineInfoIdByStartTs(mLyricBeginTs));
                        mManyLyricsView.setNeedCountDownLine(set);
                        if (mManyLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC
                                && mManyLyricsView.getLrcPlayerStatus() != LRCPLAYERSTATUS_PLAY) {
//                            mManyLyricsView.play(mAccBeginTs);
                            mManyLyricsView.seekto(mAccBeginTs);
                            mManyLyricsView.pause();
                            mLyricsReader = lyricsReader;
                            if (mAccLoadOk) {
                                launchLyricEvent(EngineManager.getInstance().getAudioMixingCurrentPosition());
                            } else {
                                mUiHandler.sendEmptyMessageDelayed(MSG_ENSURE_LAUNCHER, LAUNCHER_DELAY);
                            }
                            mLrcLoadOk = true;
                            // 这里是假设 伴奏 和 歌词一起初始化完毕的， 实际两者会有偏差优化下
//                            int lineNum = mLyricEventLauncher.postLyricEvent(lyricsReader, lrcBeginTs - GrabRoomData.ACC_OFFSET_BY_LYRIC, lrcBeginTs + totalMs - GrabRoomData.ACC_OFFSET_BY_LYRIC, null);
//                            mRoomData.setSongLineNum(lineNum);
                        }
                        if (mListener != null) {
                            mListener.onLyricParseSuccess();
                        }
                    }
                }, throwable -> {
                    MyLog.e(TAG, throwable);
                    MyLog.d(TAG, "歌词下载失败，采用不滚动方式播放歌词");
                    if (mListener != null) {
                        mListener.onLyricParseFailed();
                    }
                });
    }


    //发射歌词事件
    void launchLyricEvent(int accPlayTs) {
        MyLog.d(TAG, "launchLyricEvent accPlayTs=" + accPlayTs + "mAccLoadOk=" + mAccLoadOk + " mLryLoadOk=" + mLrcLoadOk);
        if (mLyricsReader == null) {
            return;
        }
        if (mHasLauncher) {
            MyLog.d(TAG, "launchLyricEvent 事件已经发射过了，取消这次");
        }
        mHasLauncher = true;
        mUiHandler.removeMessages(MSG_ENSURE_LAUNCHER);
        if (mManyLyricsView != null) {
            mManyLyricsView.play(mAccBeginTs + accPlayTs);
        }

        int lineNum = mLyricEventLauncher.postLyricEvent(mLyricsReader, mAccBeginTs + accPlayTs, mAccEndTs, null);
        if (mListener != null) {
            mListener.onLyricEventPost(lineNum);
        }
        if (mVoiceScaleView != null && mManyLyricsView.getVisibility()==View.VISIBLE) {
            mVoiceScaleView.setVisibility(View.VISIBLE);
            mVoiceScaleView.startWithData(mLyricsReader.getLyricsLineInfoList(), mAccBeginTs + accPlayTs);
        }

    }

    /**
     * 会偶现播伴奏失败，即没有这个调整事件
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EngineEvent event) {
        if (event.getType() == EngineEvent.TYPE_MUSIC_PLAY_TIME_FLY_LISTENER) {
            EngineEvent.MixMusicTimeInfo in = event.getObj();
            MyLog.d(TAG, "伴奏 ts=" + in.getCurrent());
            if (in != null && in.getCurrent() > 0) {
                if (!mAccLoadOk) {
                    if (mLrcLoadOk) {
                        launchLyricEvent(in.getCurrent());
                    }
                }
                mAccLoadOk = true;
                if (mManyLyricsView.getVisibility() == View.VISIBLE) {
                    long ts1 = mManyLyricsView.getCurPlayingTime() + mManyLyricsView.getPlayerSpendTime();
                    long ts2 = in.getCurrent() + mAccBeginTs;
                    if (Math.abs(ts1 - ts2) > 500) {
                        MyLog.d(TAG, "伴奏与歌词的时间戳差距较大时,矫正一下,歌词ts=" + ts1 + " 伴奏ts=" + ts2);
                        mManyLyricsView.seekto((int) ts2);
                    }
                }
            }
        }
    }

    public void setListener(Listener l) {
        mListener = l;
    }

    public interface Listener {
        void onLyricParseSuccess();

        void onLyricParseFailed();

        void onLyricEventPost(int lineNum);
    }
}
