package com.module.playways.rank.prepare.sence;

import android.os.Bundle;
import android.support.annotation.Nullable;
import com.common.base.BaseFragment;
import com.common.log.MyLog;
import com.common.utils.SongResUtils;
import com.common.utils.U;
import com.engine.EngineManager;
import com.module.playways.rank.prepare.model.PrepareData;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.zq.lyrics.LyricsManager;
import com.zq.lyrics.event.LrcEvent;
import com.zq.lyrics.widget.AbstractLrcView;
import com.zq.lyrics.widget.ManyLyricsView;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.File;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import static com.zq.lyrics.widget.AbstractLrcView.LRCPLAYERSTATUS_PLAY;

public class AuditionFragment extends BaseFragment {
    public static final String TAG = "AuditionFragment";

    ManyLyricsView mManyLyricsView;

    PrepareData mPrepareData;

    SongModel songModel;

    @Override
    public int initView() {
        return R.layout.audition_sence_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        mManyLyricsView = mRootView.findViewById(R.id.many_lyrics_view);
        mManyLyricsView.setOnLrcClickListener(new ManyLyricsView.OnLrcClickListener() {
            @Override
            public void onLrcPlayClicked(int progress) {
                MyLog.d(TAG, "onLrcPlayClicked");
                EngineManager.getInstance().setAudioMixingPosition(progress);
                mManyLyricsView.seekto(progress);
            }
        });

        mManyLyricsView.setOnLyricViewTapListener(new ManyLyricsView.OnLyricViewTapListener() {
            @Override
            public void onDoubleTap() {
                if (EngineManager.getInstance().getParams().isMixMusicPlaying()) {
                    EngineManager.getInstance().pauseAudioMixing();
                }

                mManyLyricsView.pause();
            }

            @Override
            public void onSigleTap() {
                if (!EngineManager.getInstance().getParams().isMixMusicPlaying()) {
                    EngineManager.getInstance().resumeAudioMixing();
                }

                if (mManyLyricsView.getLrcPlayerStatus() != LRCPLAYERSTATUS_PLAY) {
                    MyLog.d(TAG, "LRC onSigleTap " + mManyLyricsView.getLrcStatus());
                    mManyLyricsView.resume();
                }
            }
        });

        songModel = mPrepareData.getSongModel();

        playMusic(songModel);
        playLyrics(songModel);
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        if (type == 0) {
            mPrepareData = (PrepareData) data;
        }
    }

    private void playMusic(SongModel songModel) {
        //从bundle里面拿音乐相关数据，然后开始试唱
        String fileName = SongResUtils.getFileNameWithMD5(songModel.getLyric());
        MyLog.d(TAG, "playMusic " + " fileName=" + fileName + " song name is " + songModel.getItemName());

        File accFile = SongResUtils.getAccFileByUrl(songModel.getAcc());

        if (accFile != null) {
            EngineManager.getInstance().startAudioMixing(accFile.getAbsolutePath(), true, false, -1);
        }
    }

    private void playLyrics(SongModel songModel) {
        final String lyricFile = SongResUtils.getFileNameWithMD5(songModel.getLyric());

        if (lyricFile != null) {
            LyricsManager.getLyricsManager(U.app()).loadLyricsObserable(lyricFile, lyricFile.hashCode() + "")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(bindUntilEvent(FragmentEvent.DESTROY))
                    .subscribe(lyricsReader -> {
                        MyLog.d(TAG, "playMusic, start play lyric");
                        mManyLyricsView.resetData();
                        mManyLyricsView.initLrcData();
                        mManyLyricsView.setLyricsReader(lyricsReader);
                        if (mManyLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mManyLyricsView.getLrcPlayerStatus() != LRCPLAYERSTATUS_PLAY) {
                            mManyLyricsView.play(0);
                        }
                    }, throwable -> MyLog.e(throwable));
        } else {
            MyLog.e(TAG, "没有歌词文件，不应该，进界面前已经下载好了");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LrcEvent.RestartLrcEvent restartLrcEvent) {
        playLyrics(songModel);
    }

    @Override
    public void destroy() {
        EngineManager.getInstance().stopAudioMixing();
        EventBus.getDefault().unregister(this);
        mManyLyricsView.release();
    }
}
