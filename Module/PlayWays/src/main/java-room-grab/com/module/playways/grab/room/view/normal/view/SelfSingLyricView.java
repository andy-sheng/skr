package com.module.playways.grab.room.view.normal.view;

import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.common.log.MyLog;
import com.common.utils.SpanUtils;
import com.common.utils.U;
import com.common.view.ExViewStub;
import com.engine.arccloud.AcrRecognizeListener;
import com.engine.arccloud.SongInfo;
import com.module.playways.R;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.component.lyrics.LyricAndAccMatchManager;
import com.module.playways.room.song.model.SongModel;
import com.zq.live.proto.Room.EQRoundStatus;
import com.zq.live.proto.Room.EWantSingType;
import com.component.lyrics.LyricsManager;
import com.component.lyrics.widget.ManyLyricsView;
import com.component.lyrics.widget.VoiceScaleView;
import com.zq.mediaengine.kit.ZqEngineKit;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class SelfSingLyricView extends ExViewStub {
    public final String TAG = "SelfSingLyricView";

    protected ScrollView mSvlyric;
    protected TextView mTvLyric;
    protected ManyLyricsView mManyLyricsView;

    Disposable mDisposable;
    protected GrabRoomData mRoomData;
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
        mSvlyric = mParentView.findViewById(R.id.sv_lyric);
        mTvLyric = mParentView.findViewById(R.id.tv_lyric);
        mManyLyricsView = mParentView.findViewById(R.id.many_lyrics_view);
        mVoiceScaleView = mParentView.findViewById(R.id.voice_scale_view);
        mIvChallengeIcon = mParentView.findViewById(R.id.iv_challenge_icon);
    }

    @Override
    protected int layoutDesc() {
        return R.layout.grab_self_sing_lyric_layout;
    }

    private void initLyric() {
        GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        mSvlyric.setVisibility(View.VISIBLE);
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
                curSong.getBeginMs(), curSong.getBeginMs() + totalTs, curSong.getUploaderName());

        SongModel finalCurSong = curSong;
        mLyricAndAccMatchManager.start(new LyricAndAccMatchManager.Listener() {
            @Override
            public void onLyricParseSuccess() {
                mSvlyric.setVisibility(View.GONE);
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
        mDisposable = LyricsManager.getLyricsManager(U.app())
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
