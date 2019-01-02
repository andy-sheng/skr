package com.module.playways.rank.prepare.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.common.base.BaseFragment;
import com.common.log.MyLog;
import com.common.utils.SongResUtils;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.engine.EngineManager;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.playways.rank.prepare.model.PrepareData;
import com.module.playways.rank.prepare.view.VoiceControlPanelView;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.zq.lyrics.LyricsManager;
import com.zq.lyrics.event.LrcEvent;
import com.zq.lyrics.widget.AbstractLrcView;
import com.zq.lyrics.widget.ManyLyricsView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.zq.lyrics.widget.AbstractLrcView.LRCPLAYERSTATUS_PLAY;

public class AuditionFragment extends BaseFragment {
    public static final String TAG = "AuditionFragment";

    ManyLyricsView mManyLyricsView;

    ExTextView mTvDown;

    ExTextView mTvUp;

    PrepareData mPrepareData;

    SongModel mSongModel;

    VoiceControlPanelView mVoiceControlPanelView;

    private boolean mIsVoiceShow = true;

    @Override
    public int initView() {
        return R.layout.audition_sence_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {

        mTvDown = mRootView.findViewById(R.id.tv_down);
        mTvUp = mRootView.findViewById(R.id.tv_up);

        mVoiceControlPanelView = mRootView.findViewById(R.id.voice_control_view);

        RxView.clicks(mTvDown).throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    showVoicePanelView(false);
        });

        RxView.clicks(mTvUp).throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    showVoicePanelView(true);
                });

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

        mSongModel = mPrepareData.getSongModel();

        playMusic(mSongModel);
        playLyrics(mSongModel);
    }

    private void showVoicePanelView(boolean show){
        mVoiceControlPanelView.clearAnimation();
        mVoiceControlPanelView.setTranslationY(show ? mVoiceControlPanelView.getMeasuredHeight() + U.getDisplayUtils().dip2px(20) : 0);

        mIsVoiceShow = show;
        int startY = show ? mVoiceControlPanelView.getMeasuredHeight() + U.getDisplayUtils().dip2px(20) : 0;
        int endY = show ? 0 : mVoiceControlPanelView.getMeasuredHeight() + U.getDisplayUtils().dip2px(20);

        ValueAnimator creditValueAnimator = ValueAnimator.ofInt(startY, endY);
        creditValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mVoiceControlPanelView.setTranslationY((int)animation.getAnimatedValue());
            }
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.setDuration(300);
        animatorSet.play(creditValueAnimator);
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimationEnd(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mTvUp.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });
        animatorSet.start();
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
        playLyrics(mSongModel);
    }

    @Override
    public void destroy() {
        super.destroy();
        EngineManager.getInstance().stopAudioMixing();
        mManyLyricsView.release();
    }
}
