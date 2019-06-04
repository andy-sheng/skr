package com.module.playways.grab.room.view.normal.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.utils.U;
import com.engine.arccloud.ArcRecognizeListener;
import com.engine.arccloud.SongInfo;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.view.CharmsView;
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

/**
 * 自己唱的歌词板(正常和pk都可以用)
 */
public class SelfSingLyricView extends RelativeLayout {

    public final static String TAG = "SelfSingLyricView";

    CharmsView mCharmsView;
    TextView mTvLyric;
    ManyLyricsView mManyLyricsView;
    VoiceScaleView mVoiceScaleView;
    ImageView mIvChallengeIcon;

    Disposable mDisposable;
    GrabRoomData mRoomData;
    SongModel mSongModel;

    LyricAndAccMatchManager mLyricAndAccMatchManager = new LyricAndAccMatchManager();

    public SelfSingLyricView(Context context) {
        super(context);
        init();
    }

    public SelfSingLyricView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SelfSingLyricView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_self_sing_lyric_layout, this);

        mCharmsView = findViewById(R.id.charms_view);
        mTvLyric = findViewById(R.id.tv_lyric);
        mManyLyricsView = findViewById(R.id.many_lyrics_view);
        mVoiceScaleView = findViewById(R.id.voice_scale_view);
        mIvChallengeIcon = findViewById(R.id.iv_challenge_icon);
    }

    public void initLyric() {
        mCharmsView.bindData((int)MyUserInfoManager.getInstance().getUid());
        if (mRoomData == null) {
            MyLog.w(TAG, "playLyric mRoomData = null");
            return;
        }
        GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        if (infoModel == null) {
            MyLog.d(TAG, "infoModel 是空的");
            return;
        }

        if (infoModel.getWantSingType() == EWantSingType.EWST_COMMON_OVER_TIME.getValue()
                || infoModel.getWantSingType() == EWantSingType.EWST_ACCOMPANY_OVER_TIME.getValue()) {
            mIvChallengeIcon.setVisibility(VISIBLE);
        } else {
            mIvChallengeIcon.setVisibility(GONE);
        }
        mSongModel = infoModel.getMusic();
        mTvLyric.setText("歌词加载中...");
        mTvLyric.setVisibility(VISIBLE);
        mManyLyricsView.setVisibility(GONE);
        mManyLyricsView.initLrcData();
        mVoiceScaleView.setVisibility(View.GONE);
    }

    public void playWithAcc(GrabRoundInfoModel infoModel, int totalTs) {
        if (infoModel == null) {
            MyLog.w(TAG, "playWithAcc" + " infoModel = null totalTs=" + totalTs);
            return;
        }
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
                mTvLyric.setVisibility(GONE);
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
        mManyLyricsView.setVisibility(GONE);
        mTvLyric.setVisibility(VISIBLE);
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
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
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

    public void setRoomData(GrabRoomData roomData) {
        mRoomData = roomData;
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
