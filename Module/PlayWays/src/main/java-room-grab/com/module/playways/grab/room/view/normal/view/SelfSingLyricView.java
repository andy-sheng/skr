package com.module.playways.grab.room.view.normal.view;

import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.TextView;

import com.common.log.MyLog;
import com.common.utils.U;
import com.engine.arccloud.ArcRecognizeListener;
import com.engine.arccloud.SongInfo;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.view.ExViewStub;
import com.module.playways.others.LyricAndAccMatchManager;
import com.module.playways.room.song.model.SongModel;
import com.zq.live.proto.Room.EQRoundStatus;
import com.zq.live.proto.Room.EWantSingType;
import com.zq.lyrics.LyricsManager;
import com.zq.lyrics.widget.ManyLyricsView;
import com.zq.lyrics.widget.VoiceScaleView;
import com.zq.mediaengine.kit.ZqEngineKit;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class SelfSingLyricView extends ExViewStub {
    public final static String TAG = "SelfSingLyricView";

    TextView mTvLyric;
    ManyLyricsView mManyLyricsView;

    Disposable mDisposable;
    GrabRoomData mRoomData;
    SongModel mSongModel;
    VoiceScaleView mVoiceScaleView;

    ImageView mIvChallengeIcon;

    LyricAndAccMatchManager mLyricAndAccMatchManager = new LyricAndAccMatchManager();

    public SelfSingLyricView(ViewStub viewStub, GrabRoomData roomData) {
        super(viewStub);
        mRoomData = roomData;
    }

    @Override
    protected void init(View parentView) {
        mTvLyric = mParentView.findViewById(R.id.tv_lyric);
        mManyLyricsView = mParentView.findViewById(R.id.many_lyrics_view);
        mVoiceScaleView = mParentView.findViewById(R.id.voice_scale_view);
        mIvChallengeIcon = mParentView.findViewById(R.id.iv_challenge_icon);
    }

    private void initLyric() {
        GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        mTvLyric.setVisibility(View.VISIBLE);
        mManyLyricsView.setVisibility(View.GONE);
        mManyLyricsView.initLrcData();

        if (mIvChallengeIcon != null) {
            if (infoModel != null &&
                    (infoModel.getWantSingType() == EWantSingType.EWST_COMMON_OVER_TIME.getValue() || infoModel.getWantSingType() == EWantSingType.EWST_ACCOMPANY_OVER_TIME.getValue())) {
                mIvChallengeIcon.setVisibility(View.VISIBLE);
            } else {
                mIvChallengeIcon.setVisibility(View.GONE);
            }
        }
        if (mVoiceScaleView != null) {
            mVoiceScaleView.setVisibility(View.GONE);
        }
    }

    public void playWithAcc(GrabRoundInfoModel infoModel, int totalTs) {
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
        mLyricAndAccMatchManager.setArgs(mManyLyricsView, mVoiceScaleView,
                curSong.getLyric(),
                curSong.getStandLrcBeginT(), curSong.getStandLrcBeginT() + totalTs,
                curSong.getBeginMs(), curSong.getBeginMs() + totalTs);

        SongModel finalCurSong = curSong;
        mLyricAndAccMatchManager.start(new LyricAndAccMatchManager.Listener() {
            @Override
            public void onLyricParseSuccess() {
                mTvLyric.setVisibility(View.GONE);
            }

            @Override
            public void onLyricParseFailed() {
                playWithNoAcc(finalCurSong);
            }

            @Override
            public void onLyricEventPost(int lineNum) {
                mRoomData.setSongLineNum(lineNum);
            }

        });
        ZqEngineKit.getInstance().setRecognizeListener(new ArcRecognizeListener() {
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
        mTvLyric.setVisibility(View.VISIBLE);
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
        mDisposable = LyricsManager.getLyricsManager(U.app())
                .loadGrabPlainLyric(songModel.getStandLrc())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        mTvLyric.setText(s);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        MyLog.e(TAG, "accept" + " throwable=" + throwable);
                    }
                });
        mLyricAndAccMatchManager.stop();
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

}
