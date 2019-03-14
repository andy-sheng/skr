package com.module.playways.rank.prepare.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.permission.SkrAudioPermission;
import com.common.log.MyLog;
import com.common.player.IPlayer;
import com.common.player.IPlayerCallback;
import com.common.player.exoplayer.ExoPlayer;
import com.common.player.mediaplayer.AndroidMediaPlayer;
import com.common.utils.SongResUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.dialog.view.TipsDialogView;
import com.engine.EngineEvent;
import com.engine.EngineManager;
import com.engine.Params;
import com.engine.arccloud.ArcRecognizeListener;
import com.engine.arccloud.RecognizeConfig;
import com.engine.arccloud.SongInfo;
import com.module.playways.rank.prepare.model.PrepareData;
import com.module.playways.rank.prepare.view.VoiceControlPanelView;
import com.module.playways.rank.room.view.RankTopContainerView2;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.zq.lyrics.LyricsManager;
import com.zq.lyrics.LyricsReader;
import com.zq.lyrics.event.LrcEvent;
import com.zq.lyrics.event.LyricEventLauncher;
import com.zq.lyrics.widget.AbstractLrcView;
import com.zq.lyrics.widget.ManyLyricsView;
import com.zq.lyrics.widget.VoiceScaleView;
import com.zq.toast.CommonToastView;
import com.zq.toast.NoImageCommonToastView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.agora.rtc.Constants;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.engine.EngineEvent.TYPE_MUSIC_PLAY_FINISH;
import static com.zq.lyrics.widget.AbstractLrcView.LRCPLAYERSTATUS_PLAY;

public class PlayRecordFragment extends BaseFragment {
    TextView mTvName;
    LinearLayout mBottomContainer;
    RelativeLayout mBackArea;
    RelativeLayout mRlPause;
    RelativeLayout mRlResing;
    ManyLyricsView mManyLyricsView;

    SongModel mSongModel;

    File mFile;

    @Override
    public int initView() {
        return R.layout.play_record_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTvName = (TextView) mRootView.findViewById(R.id.tv_name);
        mBottomContainer = (LinearLayout) mRootView.findViewById(R.id.bottom_container);
        mBackArea = (RelativeLayout) mRootView.findViewById(R.id.back_area);
        mRlPause = (RelativeLayout) mRootView.findViewById(R.id.rl_pause);
        mRlResing = (RelativeLayout) mRootView.findViewById(R.id.rl_resing);
        mManyLyricsView = (ManyLyricsView) mRootView.findViewById(R.id.many_lyrics_view);

//        playLyrics(mSongModel);
    }

    /**
     * 播放录音
     */
    @Override
    public void setData(int type, @Nullable Object data) {
        super.setData(type, data);
        if (type == 0) {
            mSongModel = (SongModel) data;
        } else if (type == 1) {
            mFile = (File) data;
        }
    }

    private void playMusic(SongModel songModel) {

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

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void destroy() {
        super.destroy();
        mManyLyricsView.release();
    }
}
