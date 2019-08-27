package com.module.playways.race.room.view;

import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.widget.ScrollView;
import android.widget.TextView;

import com.common.log.MyLog;
import com.common.utils.SpanUtils;
import com.common.view.ExViewStub;
import com.component.lyrics.LyricAndAccMatchManager;
import com.component.lyrics.LyricsManager;
import com.component.lyrics.LyricsReader;
import com.component.lyrics.widget.ManyLyricsView;
import com.component.lyrics.widget.VoiceScaleView;
import com.engine.arccloud.AcrRecognizeListener;
import com.engine.arccloud.SongInfo;
import com.module.playways.R;
import com.module.playways.grab.room.view.SingCountDownView2;
import com.module.playways.race.room.RaceRoomData;
import com.module.playways.race.room.model.RaceRoundInfoModel;
import com.module.playways.room.song.model.SongModel;
import com.zq.live.proto.Room.EQRoundStatus;
import com.zq.mediaengine.kit.ZqEngineKit;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class RaceSelfSingLyricView extends ExViewStub {
    public final String TAG = "SelfSingLyricView";

    protected ScrollView mSvlyric;
    protected TextView mTvLyric;
    protected ManyLyricsView mManyLyricsView;

    Disposable mDisposable;
    protected RaceRoomData mRoomData;
    SongModel mSongModel;
    VoiceScaleView mVoiceScaleView;
    SingCountDownView2 mSingCountDownView2;

    LyricAndAccMatchManager mLyricAndAccMatchManager = new LyricAndAccMatchManager();

    public RaceSelfSingLyricView(ViewStub viewStub, RaceRoomData roomData) {
        super(viewStub);
        mRoomData = roomData;
    }

    @Override
    protected void init(View parentView) {
        mSvlyric = mParentView.findViewById(R.id.sv_lyric);
        mTvLyric = mParentView.findViewById(R.id.tv_lyric);
        mManyLyricsView = mParentView.findViewById(R.id.many_lyrics_view);
        mVoiceScaleView = mParentView.findViewById(R.id.voice_scale_view);
        mSingCountDownView2 = mParentView.findViewById(R.id.sing_count_down_view);
    }

    @Override
    protected int layoutDesc() {
        return R.layout.race_self_sing_lyric_layout;
    }

    private void initLyric() {
        RaceRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        mSvlyric.setVisibility(View.VISIBLE);
        mManyLyricsView.setVisibility(View.GONE);
        mManyLyricsView.initLrcData();

        if (mVoiceScaleView != null) {
            mVoiceScaleView.setVisibility(View.GONE);
        }
    }

    public void startFly() {
//        if (infoModel.isAccRound() && mRoomData != null && mRoomData.isAccEnable()) {
//            withAcc = true;
//        }
//        if (!withAcc) {
//            mSelfSingLyricView.playWithNoAcc(infoModel.getMusic());
//        } else {
//            mSelfSingLyricView.playWithAcc(infoModel, totalTs);
//        }
    }

    public void playWithAcc(RaceRoundInfoModel infoModel, int totalTs) {
        if (infoModel == null) {
            MyLog.w(TAG, "playWithAcc" + " infoModel = null totalTs=" + totalTs);
            return;
        }
        tryInflate();
        initLyric();
        mSongModel = infoModel.getMusic();
        if (mSongModel == null) {
            MyLog.w(TAG, "playWithAcc" + " mSongModel = null totalTs=" + totalTs);
            return;
        }
        SongModel curSong = mSongModel;
        if (infoModel.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
            if (mSongModel.getPkMusic() != null) {
                curSong = mSongModel.getPkMusic();
            }
        }
        if (curSong == null) {
            MyLog.w(TAG, "playWithAcc" + " curSong = null totalTs=" + totalTs);
            return;
        }
        LyricAndAccMatchManager.ConfigParams configParams = new LyricAndAccMatchManager.ConfigParams();
        configParams.setManyLyricsView(mManyLyricsView);
        configParams.setVoiceScaleView(mVoiceScaleView);
        configParams.setLyricUrl(curSong.getLyric());
        configParams.setLyricBeginTs(curSong.getStandLrcBeginT());
        configParams.setLyricEndTs(curSong.getStandLrcBeginT() + totalTs);
        configParams.setAccBeginTs(curSong.getBeginMs());
        configParams.setAccEndTs(curSong.getBeginMs() + totalTs);
        configParams.setAuthorName(curSong.getUploaderName());
        mLyricAndAccMatchManager.setArgs(configParams);
        SongModel finalCurSong = curSong;
        mLyricAndAccMatchManager.start(new LyricAndAccMatchManager.Listener() {

            @Override
            public void onLyricParseSuccess(LyricsReader reader) {
                mSvlyric.setVisibility(View.GONE);
            }

            @Override
            public void onLyricParseFailed() {
                playWithNoAcc(finalCurSong);
            }

            @Override
            public void onLyricEventPost(int lineNum) {
//                mRoomData.setSongLineNum(lineNum);
            }

        });
        ZqEngineKit.getInstance().setRecognizeListener(new AcrRecognizeListener() {
            @Override
            public void onResult(String result, List<SongInfo> list, SongInfo targetSongInfo, int lineNo) {
                mLyricAndAccMatchManager.onAcrResult(result, list, targetSongInfo, lineNo);
            }
        });
    }

    public void playWithNoAcc(SongModel songModel) {
        if (songModel == null) {
            return;
        }
        tryInflate();
        initLyric();
        mManyLyricsView.setVisibility(View.GONE);
        mSvlyric.setVisibility(View.VISIBLE);
        mSvlyric.scrollTo(0, 0);
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        mDisposable = LyricsManager.INSTANCE
                .loadGrabPlainLyric(songModel.getStandLrc())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        SpannableStringBuilder ssb = createLyricSpan(s, songModel);
                        if (ssb == null) {
                            mTvLyric.setText(s);
                        } else {
                            mTvLyric.setText(ssb);
                        }

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        MyLog.e(TAG, "accept" + " throwable=" + throwable);
                    }
                });
        mLyricAndAccMatchManager.stop();
    }

    protected SpannableStringBuilder createLyricSpan(String lyric, SongModel songModel) {
        if (songModel != null && !TextUtils.isEmpty(songModel.getUploaderName())) {
            SpannableStringBuilder ssb = new SpanUtils()
                    .append(lyric)
                    .append("\n")
                    .append("上传者:" + songModel.getUploaderName()).setFontSize(12, true)
                    .create();
            return ssb;
        }
        return null;
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        super.onViewDetachedFromWindow(v);
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        if (mLyricAndAccMatchManager != null) {
            mLyricAndAccMatchManager.stop();
        }
    }

    public void destroy() {
        if (mManyLyricsView != null) {
            mManyLyricsView.release();
        }
    }

    public void reset() {
        if (mManyLyricsView != null) {
            mManyLyricsView.setLyricsReader(null);
        }
        if (mLyricAndAccMatchManager != null) {
            mLyricAndAccMatchManager.stop();
        }
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (visibility == View.GONE) {
            reset();
        }
    }
}
