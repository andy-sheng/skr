package com.module.playways.grab.room.view.pk;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.common.log.MyLog;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.countdown.CircleCountDownView;
import com.common.view.ex.ExTextView;
import com.component.busilib.view.BitmapTextView;
import com.engine.EngineManager;
import com.engine.arccloud.ArcRecognizeListener;
import com.engine.arccloud.SongInfo;
import com.facebook.drawee.view.SimpleDraweeView;
import com.module.playways.RoomDataUtils;
import com.module.playways.grab.room.GrabRoomData;
import com.module.playways.grab.room.model.GrabRoundInfoModel;
import com.module.playways.grab.room.view.control.SelfSingCardView;
import com.module.playways.others.LyricAndAccMatchManager;
import com.module.playways.room.song.model.SongModel;
import com.module.rank.R;
import com.opensource.svgaplayer.SVGAImageView;
import com.zq.live.proto.Room.EQRoundStatus;
import com.zq.lyrics.LyricsManager;
import com.zq.lyrics.widget.ManyLyricsView;
import com.zq.lyrics.widget.VoiceScaleView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class PKSelfSingCardView extends RelativeLayout {

    public final static String TAG = "PKSelfSingCardView";

    ImageView mIvBg;
    ScrollView mSvLyric;
    TextView mTvLyric;
    ManyLyricsView mManyLyricsView;
    VoiceScaleView mVoiceScaleView;
    SVGAImageView mLeftSingSvga;
    SVGAImageView mRightSingSvga;
    LinearLayout mPkSelfArea;
    SimpleDraweeView mLeftIv;
    ExTextView mLeftName;
    SimpleDraweeView mRightIv;
    ExTextView mRightName;
    ImageView mIvTag;
    CircleCountDownView mCircleCountDownView;
    BitmapTextView mCountDownTv;

    TranslateAnimation mEnterTranslateAnimation; // 飞入的进场动画
    TranslateAnimation mLeaveTranslateAnimation; // 飞出的离场动画

    GrabRoomData mRoomData;
    SongModel mSongModel;
    LyricAndAccMatchManager mLyricAndAccMatchManager = new LyricAndAccMatchManager();
    Disposable mDisposable;
    HandlerTaskTimer mCounDownTask;

    public PKSelfSingCardView(Context context) {
        super(context);
        init();
    }

    public PKSelfSingCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PKSelfSingCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.grab_pk_self_sing_card_layout, this);

        mIvBg = (ImageView) findViewById(R.id.iv_bg);
        mSvLyric = (ScrollView) findViewById(R.id.sv_lyric);
        mTvLyric = (TextView) findViewById(R.id.tv_lyric);
        mManyLyricsView = (ManyLyricsView) findViewById(R.id.many_lyrics_view);
        mVoiceScaleView = (VoiceScaleView) findViewById(R.id.voice_scale_view);
        mLeftSingSvga = (SVGAImageView) findViewById(R.id.left_sing_svga);
        mRightSingSvga = (SVGAImageView) findViewById(R.id.right_sing_svga);
        mPkSelfArea = (LinearLayout) findViewById(R.id.pk_self_area);
        mLeftIv = (SimpleDraweeView) findViewById(R.id.left_iv);
        mLeftName = (ExTextView) findViewById(R.id.left_name);
        mRightIv = (SimpleDraweeView) findViewById(R.id.right_iv);
        mRightName = (ExTextView) findViewById(R.id.right_name);
        mIvTag = (ImageView) findViewById(R.id.iv_tag);
        mCircleCountDownView = (CircleCountDownView) findViewById(R.id.circle_count_down_view);
        mCountDownTv = (BitmapTextView) findViewById(R.id.count_down_tv);

        int offsetX = (U.getDisplayUtils().getScreenWidth() / 2 - U.getDisplayUtils().dip2px(16)) / 2;
        mLeftSingSvga.setTranslationX(-offsetX);
        mRightSingSvga.setTranslationX(offsetX);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    // TODO: 2019/4/22 在这之前应该还有一个动画，然后开始绘制
    public void playLyric() {
        GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        if (infoModel == null) {
            MyLog.d(TAG, "infoModel 是空的");
            return;
        }

        // 入场
        setVisibility(VISIBLE);
        animationGo();

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

        int totalTs = infoModel.getSingTotalMs();
        boolean withAcc = false;
        if (infoModel.isAccRound() && mRoomData != null && mRoomData.isAccEnable()) {
            withAcc = true;
        }
        if (RoomDataUtils.isPKRound(mRoomData)) {
            // pk模式
            withAcc = true;
        }
        if (!withAcc) {
            playWithNoAcc(mSongModel);
            mIvTag.setBackground(U.getDrawable(R.drawable.ycdd_daojishi_qingchang));
            mLyricAndAccMatchManager.stop();
        } else {
            if (RoomDataUtils.isPKRound(mRoomData)) {
                mIvTag.setBackground(U.getDrawable(R.drawable.ycdd_daojishi_pk));
            } else {
                mIvTag.setBackground(U.getDrawable(R.drawable.ycdd_daojishi_banzou));
            }
            SongModel curSong = mSongModel;
            if (infoModel.getStatus() == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.getValue()) {
                if (mSongModel.getPkMusic() != null) {
                    curSong = mSongModel.getPkMusic();
                }
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
            EngineManager.getInstance().setRecognizeListener(new ArcRecognizeListener() {
                @Override
                public void onResult(String result, List<SongInfo> list, SongInfo targetSongInfo, int lineNo) {
                    mLyricAndAccMatchManager.onAcrResult(result, list, targetSongInfo, lineNo);
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
                        MyLog.d(TAG, "accept" + " throwable=" + throwable);
                    }
                });
    }


    private void starCounDown(int totalMs) {
        mCountDownTv.setVisibility(VISIBLE);
        mCircleCountDownView.go(0, totalMs);
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

    private void animationGo() {
        if (mEnterTranslateAnimation == null) {
            mEnterTranslateAnimation = new TranslateAnimation(-U.getDisplayUtils().getScreenWidth(), 0.0F, 0.0F, 0.0F);
            mEnterTranslateAnimation.setDuration(200);
        }
        this.startAnimation(mEnterTranslateAnimation);
    }

    /**
     * 离场动画
     */
    public void hide() {
        if (this != null && this.getVisibility() == VISIBLE) {
            if (mLeaveTranslateAnimation == null) {
                mLeaveTranslateAnimation = new TranslateAnimation(0.0F, U.getDisplayUtils().getScreenWidth(), 0.0F, 0.0F);
                mLeaveTranslateAnimation.setDuration(200);
            }
            mLeaveTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    clearAnimation();
                    setVisibility(GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            this.startAnimation(mLeaveTranslateAnimation);
        } else {
            clearAnimation();
            setVisibility(GONE);
        }
    }

    private void stopCounDown() {
        if (mCounDownTask != null) {
            mCounDownTask.dispose();
        }
    }

    public void setRoomData(GrabRoomData roomData) {
        mRoomData = roomData;
    }

    SelfSingCardView.Listener mListener;

    public void setListener(SelfSingCardView.Listener l) {
        mListener = l;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mEnterTranslateAnimation != null) {
            mEnterTranslateAnimation.setAnimationListener(null);
            mEnterTranslateAnimation.cancel();
        }
        if (mLeaveTranslateAnimation != null) {
            mLeaveTranslateAnimation.setAnimationListener(null);
            mLeaveTranslateAnimation.cancel();
        }
        if (mLeftSingSvga != null) {
            mLeftSingSvga.setCallback(null);
            mRightSingSvga.stopAnimation(true);
        }
        if (mRightSingSvga != null) {
            mRightSingSvga.setCallback(null);
            mRightSingSvga.stopAnimation(true);
        }

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }
}
