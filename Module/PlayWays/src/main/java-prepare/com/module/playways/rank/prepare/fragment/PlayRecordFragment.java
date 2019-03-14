package com.module.playways.rank.prepare.fragment;


import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.log.MyLog;
import com.common.player.IPlayer;
import com.common.player.IPlayerCallback;
import com.common.player.exoplayer.ExoPlayer;
import com.common.player.mediaplayer.AndroidMediaPlayer;
import com.common.utils.ActivityUtils;
import com.common.utils.SongResUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.zq.lyrics.LyricsManager;
import com.zq.lyrics.LyricsReader;
import com.zq.lyrics.widget.AbstractLrcView;
import com.zq.lyrics.widget.ManyLyricsView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.zq.lyrics.widget.AbstractLrcView.LRCPLAYERSTATUS_PLAY;

public class PlayRecordFragment extends BaseFragment {
    TextView mTvName;
    LinearLayout mBottomContainer;
    RelativeLayout mBackArea;
    RelativeLayout mOptArea;
    ExTextView mOptTv;
    RelativeLayout mResetArea;
    ManyLyricsView mManyLyricsView;

    SongModel mSongModel;

    Handler mUiHanlder;

    IPlayer mPlayer;

    boolean mIsPlay = false;

    @Override
    public int initView() {
        return R.layout.play_record_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTvName = (TextView) mRootView.findViewById(R.id.tv_name);
        mBottomContainer = (LinearLayout) mRootView.findViewById(R.id.bottom_container);
        mBackArea = (RelativeLayout) mRootView.findViewById(R.id.back_area);
        mOptArea = (RelativeLayout) mRootView.findViewById(R.id.opt_area);
        mOptTv = (ExTextView) mRootView.findViewById(R.id.opt_tv);
        mResetArea = (RelativeLayout) mRootView.findViewById(R.id.reset_area);
        mManyLyricsView = (ManyLyricsView) mRootView.findViewById(R.id.many_lyrics_view);
        mTvName.setText("《" + mSongModel.getItemName() + "》");
        mUiHanlder = new Handler();

        playLyrics(mSongModel);
        playRecord();

        mBackArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                // 返回选歌页面
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });

        mResetArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mFragmentDataListener != null) {
                    mFragmentDataListener.onFragmentResult(0, 0, null, null);
                }
                U.getFragmentUtils().popFragment(PlayRecordFragment.this);
            }
        });

        mOptArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mIsPlay) {
                    // 暂停
                    if (mPlayer != null) {
                        mPlayer.pause();
                        mIsPlay = false;
                    }
                    mManyLyricsView.pause();
                    mOptTv.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.audition_bofang), null, null);
                    mOptTv.setText("播放");
                } else {
                    // 播放
                    mManyLyricsView.resume();
                    mPlayer.resume();
                    mIsPlay = true;
                    mOptTv.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.audition_zanting), null, null);
                    mOptTv.setText("暂停");
                }
            }
        });
    }

    /**
     * 播放录音
     */
    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mSongModel = (SongModel) data;
        }
    }

    LyricsReader mLyricsReader;

    private void playLyrics(SongModel songModel) {
        final String lyricFile = SongResUtils.getFileNameWithMD5(songModel.getLyric());

        if (lyricFile != null) {
            LyricsManager.getLyricsManager(U.app())
                    .loadLyricsObserable(lyricFile, lyricFile.hashCode() + "")
                    .subscribeOn(Schedulers.io())
                    .retry(10)
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(bindUntilEvent(FragmentEvent.DESTROY))
                    .subscribe(lyricsReader -> {
                        MyLog.d(TAG, "playMusic, start play lyric");
                        mManyLyricsView.resetData();
                        mManyLyricsView.initLrcData();
                        lyricsReader.cut(songModel.getRankLrcBeginT(), songModel.getRankLrcEndT());
                        MyLog.d(TAG, "getRankLrcBeginT : " + songModel.getRankLrcBeginT());
                        mManyLyricsView.setLyricsReader(lyricsReader);
                        mLyricsReader = lyricsReader;
                        if (mManyLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mManyLyricsView.getLrcPlayerStatus() != LRCPLAYERSTATUS_PLAY) {
                            mManyLyricsView.play(songModel.getBeginMs());
                            MyLog.d(TAG, "songModel.getBeginMs() : " + songModel.getBeginMs());
                        }
                    }, throwable -> MyLog.e(throwable));
        } else {
            MyLog.e(TAG, "没有歌词文件，不应该，进界面前已经下载好了");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ActivityUtils.ForeOrBackgroundChange event) {
        MyLog.w(TAG, event.foreground ? "切换到前台" : "切换到后台");
        if (!event.foreground && mIsPlay) {
            // 暂停
            if (mPlayer != null) {
                mPlayer.pause();
                mIsPlay = false;
            }
            mManyLyricsView.pause();
            mOptTv.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.audition_bofang), null, null);
            mOptTv.setText("播放");
        }
    }


    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void destroy() {
        super.destroy();
        mManyLyricsView.release();
        if (mPlayer != null) {
            mPlayer.release();
        }
        mUiHanlder.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    /**
     * 播放录音
     */
    private void playRecord() {
        if (mPlayer != null) {
            mPlayer.reset();
        }
        if (mPlayer == null) {
            if (AuditionFragment.RECORD_BY_CALLBACK) {
                mPlayer = new AndroidMediaPlayer();
            } else {
                mPlayer = new ExoPlayer();
            }

            mPlayer.setCallback(new IPlayerCallback() {
                @Override
                public void onPrepared() {

                }

                @Override
                public void onCompletion() {
                    mManyLyricsView.seekto(mSongModel.getBeginMs());
                    mUiHanlder.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mManyLyricsView.pause();
                            mPlayer.pause();
                        }
                    }, 30);

                    mIsPlay = false;
                    mPlayer.seekTo(0);
                    mOptTv.setCompoundDrawablesWithIntrinsicBounds(null, getResources().getDrawable(R.drawable.audition_bofang), null, null);
                    mOptTv.setText("播放");
                }

                @Override
                public void onSeekComplete() {

                }

                @Override
                public void onVideoSizeChanged(int width, int height) {

                }

                @Override
                public void onError(int what, int extra) {

                }

                @Override
                public void onInfo(int what, int extra) {

                }
            });
        }

        mIsPlay = true;
        if (AuditionFragment.RECORD_BY_CALLBACK) {
            mPlayer.startPlayPcm(AuditionFragment.PCM_SAVE_PATH, 2, 44100, 44100 * 2);
        } else {
            mPlayer.startPlay(AuditionFragment.ACC_SAVE_PATH);
        }

    }
}
