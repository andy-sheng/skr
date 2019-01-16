package com.module.playways.rank.prepare.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.log.MyLog;
import com.common.player.IPlayerCallback;
import com.common.player.exoplayer.ExoPlayer;
import com.common.utils.SongResUtils;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.dialog.view.TipsDialogView;
import com.engine.EngineEvent;
import com.engine.EngineManager;
import com.engine.Params;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.playways.rank.prepare.model.PrepareData;
import com.module.playways.rank.prepare.view.SendGiftCircleCountDownView;
import com.module.playways.rank.prepare.view.VoiceControlPanelView;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.trello.rxlifecycle2.android.FragmentEvent;
import com.zq.lyrics.LyricsManager;
import com.zq.lyrics.event.LrcEvent;
import com.zq.lyrics.widget.AbstractLrcView;
import com.zq.lyrics.widget.ManyLyricsView;
import com.zq.toast.CommonToastView;
import com.zq.toast.NoImageCommonToastView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.agora.rtc.Constants;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.engine.EngineEvent.TYPE_MUSIC_PLAY_FINISH;
import static com.zq.lyrics.widget.AbstractLrcView.LRCPLAYERSTATUS_PLAY;

public class AuditionFragment extends BaseFragment {
    public static final String TAG = "AuditionFragment";

    static final int MSG_AUTO_LEAVE_CHANNEL = 9;

    static final String AAC_SAVE_PATH = new File(U.getAppInfoUtils().getMainDir(), "audition.aac").getAbsolutePath();

    ExImageView mIvBack;
    ExTextView mTvSongName;

    ManyLyricsView mManyLyricsView;

    ExTextView mTvDown;

    ExTextView mTvUp;
    TextView mTvRecordTip;

    SendGiftCircleCountDownView mPrgressBar;
    ExTextView mTvRecordStop;
    ExImageView mIvRecordStart;

    LinearLayout mLlResing;
    LinearLayout mLlPlay;
    LinearLayout mLlSave;

    ExImageView mIvResing;
    ExImageView mIvPlay;
    ExImageView mIvSave;

    RelativeLayout mRlControlContainer;

    FrameLayout mFlProgressRoot;

    PrepareData mPrepareData;

    SongModel mSongModel;

    VoiceControlPanelView mVoiceControlPanelView;

    FrameLayout mFlProgressContainer;

    private boolean mIsVoiceShow = true;

    private volatile boolean isRecord = false;

    ExoPlayer mExoPlayer;

    Handler mUiHanlder;

    long mStartRecordTs = 0;

    ValueAnimator mRecordAnimator;

    DialogPlus mQuitTipsDialog;

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("mPrepareData", mPrepareData);
    }

    @Override
    public void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mPrepareData = (PrepareData) savedInstanceState.getSerializable("mPrepareData");
    }

    @Override
    public int initView() {
        return R.layout.audition_sence_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        if (!EngineManager.getInstance().isInit()) {
            // 不能每次都初始化,播放伴奏
            EngineManager.getInstance().init("prepare", Params.getFromPref());
            EngineManager.getInstance().joinRoom("" + System.currentTimeMillis(), (int) UserAccountManager.getInstance().getUuidAsLong(), true);
        } else {
            EngineManager.getInstance().resumeAudioMixing();
        }

        mUiHanlder = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == MSG_AUTO_LEAVE_CHANNEL) {
                    // 为了省钱，因为引擎每多在试音房一分钟都是消耗，防止用户挂机
                    U.getFragmentUtils().popFragment(AuditionFragment.this);
                    return;
                }
            }
        };
        mIvBack = (ExImageView) mRootView.findViewById(R.id.iv_back);
        mTvSongName = (ExTextView) mRootView.findViewById(R.id.tv_song_name);

        mTvDown = mRootView.findViewById(R.id.tv_down);
        mTvUp = mRootView.findViewById(R.id.tv_up);
        mVoiceControlPanelView = mRootView.findViewById(R.id.voice_control_view);
        mVoiceControlPanelView.bindData();
        mFlProgressRoot = (FrameLayout) mRootView.findViewById(R.id.fl_progress_root);
        mPrgressBar = mRootView.findViewById(R.id.prgress_bar);
        mTvRecordStop = (ExTextView) mRootView.findViewById(R.id.tv_record_stop);
        mIvRecordStart = (ExImageView) mRootView.findViewById(R.id.iv_record_start);
        mRlControlContainer = (RelativeLayout) mRootView.findViewById(R.id.rl_control_container);
        mFlProgressContainer = (FrameLayout) mRootView.findViewById(R.id.fl_progress_container);
        mLlResing = (LinearLayout) mRootView.findViewById(R.id.ll_resing);
        mIvResing = (ExImageView) mRootView.findViewById(R.id.iv_resing);
        mLlPlay = (LinearLayout) mRootView.findViewById(R.id.ll_play);
        mIvPlay = (ExImageView) mRootView.findViewById(R.id.iv_play);
        mLlSave = (LinearLayout) mRootView.findViewById(R.id.ll_save);
        mIvSave = (ExImageView) mRootView.findViewById(R.id.iv_save);
        mManyLyricsView = mRootView.findViewById(R.id.many_lyrics_view);
        mTvRecordTip = (TextView) mRootView.findViewById(R.id.tv_record_tip);
        mRlControlContainer.setVisibility(View.GONE);


        RxView.clicks(mIvBack).throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    onBackPressed();
                });

//        RxView.clicks(mTvDown).throttleFirst(500, TimeUnit.MILLISECONDS)
//                .subscribe(o -> {
//                    resendAutoLeaveChannelMsg();
//                    showVoicePanelView(false);
//                });

        RxView.clicks(mTvUp).throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    resendAutoLeaveChannelMsg();
                    showVoicePanelView(true);
                });


        RxView.clicks(mIvResing).throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    mPrgressBar.setMax(360);
                    mPrgressBar.setProgress(0);
                    mFlProgressRoot.setVisibility(View.VISIBLE);
                    mVoiceControlPanelView.setVisibility(View.VISIBLE);
                    mRlControlContainer.setVisibility(View.GONE);
                    mIvRecordStart.setVisibility(View.VISIBLE);
                    mTvRecordStop.setVisibility(View.GONE);

                    if (mExoPlayer != null) {
                        mExoPlayer.stop();
                    }

                    startRecord();
                });

        RxView.clicks(mIvPlay).throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    playRecord();
                });

        RxView.clicks(mIvSave).throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(getContext())
                            .setImage(R.drawable.touxiangshezhichenggong_icon)
                            .setText("保存设置成功\n已应用到所有对局")
                            .build());

                    mUiHanlder.postDelayed(() -> {
                        getActivity().finish();
                    }, 2000);
//                    U.getFragmentUtils().popFragment(AuditionFragment.this);
                });

        RxView.clicks(mFlProgressContainer).throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    if (isRecord) {
                        stopRecord();
                    } else {
                        startRecord();
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
            public void onSigleTap(int progress) {
                MyLog.d(TAG, "progress " + progress);
                if (progress > 0) {
                    EngineManager.getInstance().setAudioMixingPosition(progress - mSongModel.getBeginMs());
                    mManyLyricsView.seekto(progress);
                }

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
        mTvSongName.setText("《" + mSongModel.getItemName() + "》");
        playLyrics(mSongModel, false);

        mPrgressBar.setMax(360);
        mPrgressBar.setProgress(0);

        resendAutoLeaveChannelMsg();
    }

    private void startRecord() {
        isRecord = true;

        playLyrics(mSongModel, true);
        playMusic(mSongModel);

        mStartRecordTs = System.currentTimeMillis();
        mIvRecordStart.setVisibility(View.GONE);
        mTvRecordStop.setVisibility(View.VISIBLE);
        mRlControlContainer.setVisibility(View.GONE);
        mTvRecordTip.setText("点击结束试音演唱");
        mIvPlay.setEnabled(true);
        EngineManager.getInstance().startAudioRecording(AAC_SAVE_PATH, Constants.AUDIO_RECORDING_QUALITY_HIGH);

        if (mRecordAnimator != null) {
            mRecordAnimator.cancel();
        }

//        mPrgressBar.setMax(mSongModel.getTotalMs());
        mRecordAnimator = ValueAnimator.ofInt(0, 360);
        mRecordAnimator.setDuration(mSongModel.getTotalMs());
        mRecordAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
//                MyLog.d(TAG, "onAnimationUpdate" + " animation=" + animation);
                int value = (Integer) animation.getAnimatedValue();
                mPrgressBar.setProgress(value);
            }
        });

        mRecordAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mUiHanlder.post(() -> {
                    stopRecord();
                });
            }

            @Override
            public void onAnimationCancel(Animator animation) {
//                onAnimationEnd(animation);
            }
        });

        mRecordAnimator.start();
    }

    private void stopRecord() {
        if (!isRecord) {
            return;
        }

        if (System.currentTimeMillis() - mStartRecordTs < 5000) {
            U.getToastUtil().showSkrCustomShort(new NoImageCommonToastView.Builder(getContext())
                    .setText("太短啦\n再唱几句吧~")
                    .build());
            return;
        }

        isRecord = false;

        EngineManager.getInstance().stopAudioRecording();
        EngineManager.getInstance().stopAudioMixing();

        playLyrics(mSongModel, true);
//        mManyLyricsView.seekto(mSongModel.getBeginMs());
//        mUiHanlder.postDelayed(() -> {
//            mManyLyricsView.pause();
//        }, 100);

        mRecordAnimator.cancel();
        mIvRecordStart.setVisibility(View.VISIBLE);
        mTvRecordStop.setVisibility(View.GONE);
        mRlControlContainer.setVisibility(View.VISIBLE);
        mFlProgressRoot.setVisibility(View.GONE);
        mVoiceControlPanelView.setVisibility(View.GONE);
        mIvPlay.setEnabled(false);

        playRecord();
    }

    /**
     * 播放录音
     */
    private void playRecord() {
        if (mExoPlayer != null) {
            mExoPlayer.stop();
        }
        if (mExoPlayer == null) {
            mExoPlayer = new ExoPlayer();
            mExoPlayer.setCallback(new IPlayerCallback() {
                @Override
                public void onPrepared() {

                }

                @Override
                public void onCompletion() {
                    mIvPlay.setEnabled(true);
                    mManyLyricsView.seekto(mSongModel.getBeginMs());
                    mUiHanlder.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mManyLyricsView.pause();
                        }
                    }, 100);
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

        mExoPlayer.startPlay(AAC_SAVE_PATH);
        mIvPlay.setEnabled(false);
    }

    private void resendAutoLeaveChannelMsg() {
        mUiHanlder.removeMessages(MSG_AUTO_LEAVE_CHANNEL);
        mUiHanlder.sendEmptyMessageDelayed(MSG_AUTO_LEAVE_CHANNEL, 60 * 1000 * 10);
    }

    private void showVoicePanelView(boolean show) {
        mVoiceControlPanelView.clearAnimation();
        mVoiceControlPanelView.setTranslationY(show ? mVoiceControlPanelView.getMeasuredHeight() + U.getDisplayUtils().dip2px(20) : 0);

        mIsVoiceShow = show;
        int startY = show ? mVoiceControlPanelView.getMeasuredHeight() + U.getDisplayUtils().dip2px(20) : 0;
        int endY = show ? 0 : mVoiceControlPanelView.getMeasuredHeight() + U.getDisplayUtils().dip2px(20);

        ValueAnimator creditValueAnimator = ValueAnimator.ofInt(startY, endY);
        creditValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mVoiceControlPanelView.setTranslationY((int) animation.getAnimatedValue());
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

        File midiFile = SongResUtils.getMIDIFileByUrl(songModel.getMidi());
        if (accFile != null) {
            EngineManager.getInstance().startAudioMixing((int) MyUserInfoManager.getInstance().getUid(),accFile.getAbsolutePath(), midiFile.getAbsolutePath(), songModel.getBeginMs(), true, false, 1);
        }
    }

    private void playLyrics(SongModel songModel, boolean play) {
        final String lyricFile = SongResUtils.getFileNameWithMD5(songModel.getLyric());

        if (lyricFile != null) {
            LyricsManager.getLyricsManager(U.app()).loadLyricsObserable(lyricFile, lyricFile.hashCode() + "")
                    .subscribeOn(Schedulers.io())
                    .retry(10)
                    .observeOn(AndroidSchedulers.mainThread())
                    .compose(bindUntilEvent(FragmentEvent.DESTROY))
                    .subscribe(lyricsReader -> {
                        MyLog.d(TAG, "playMusic, start play lyric");
                        mManyLyricsView.resetData();
                        mManyLyricsView.initLrcData();
                        lyricsReader.cut(songModel.getRankLrcBeginT(), songModel.getEndMs());
                        if(isRecord){
                            Set<Integer> set = new HashSet<>();
                            set.add(lyricsReader.getLineInfoIdByStartTs(songModel.getRankLrcBeginT()));
                            mManyLyricsView.setNeedCountDownLine(set);
                        }else {
                            Set<Integer> set = new HashSet<>();
                            mManyLyricsView.setNeedCountDownLine(set);
                        }
                        MyLog.d(TAG, "getRankLrcBeginT : " + songModel.getRankLrcBeginT());
                        mManyLyricsView.setLyricsReader(lyricsReader);
                        if (mManyLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mManyLyricsView.getLrcPlayerStatus() != LRCPLAYERSTATUS_PLAY) {
                            mManyLyricsView.play(songModel.getBeginMs());
                            MyLog.d(TAG, "songModel.getBeginMs() : " + songModel.getBeginMs());
                        }

                        if (!play) {
                            mManyLyricsView.pause();
                        }
                    }, throwable -> MyLog.e(throwable));
        } else {
            MyLog.e(TAG, "没有歌词文件，不应该，进界面前已经下载好了");
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventMainThread(EngineEvent event) {
//        MyLog.d(TAG, "restartLrcEvent type is " + restartLrcEvent.getType());

        if (event.getType() == TYPE_MUSIC_PLAY_FINISH) {
            mUiHanlder.post(() -> {
                stopRecord();
            });
        } else if (event.getType() == EngineEvent.TYPE_USER_AUDIO_VOLUME_INDICATION) {
            List<EngineEvent.UserVolumeInfo> l = event.getObj();
            for (EngineEvent.UserVolumeInfo userVolumeInfo : l) {
                if (userVolumeInfo.getUid() == 0 && userVolumeInfo.getVolume() > 0) {
                    //如果自己在唱歌也延迟关闭
                    resendAutoLeaveChannelMsg();
                }
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LrcEvent.LineEndEvent event) {
        //TODO
        MyLog.d(TAG, "onEvent" + " event=" + event);
        int score = EngineManager.getInstance().getLineScore();
        U.getToastUtil().showShort("score:" + score);
//        score = (int) (Math.random() * 100);
    }

    @Override
    protected boolean onBackPressed() {
        if (mQuitTipsDialog == null) {
            TipsDialogView tipsDialogView = new TipsDialogView.Builder(getContext())
                    .setMessageTip("是否将这次调音设置应用到所有游戏对局中？")
                    .setConfirmTip("保存")
                    .setCancelTip("取消")
                    .setConfirmBtnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mQuitTipsDialog.dismiss(false);
                            // 要保存
                            Params.save2Pref(EngineManager.getInstance().getParams());
                            U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(getContext())
                                    .setImage(R.drawable.touxiangshezhichenggong_icon)
                                    .setText("保存设置成功\n已应用到所有对局")
                                    .build());

                            mUiHanlder.postDelayed(() -> {
                                getActivity().finish();
                            }, 2000);

                        }
                    })
                    .setCancelBtnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mQuitTipsDialog.dismiss(false);
                            U.getFragmentUtils().popFragment(AuditionFragment.this);
                        }
                    })
                    .build();

            mQuitTipsDialog = DialogPlus.newDialog(getContext())
                    .setContentHolder(new ViewHolder(tipsDialogView))
                    .setGravity(Gravity.BOTTOM)
                    .setContentBackgroundResource(R.color.transparent)
                    .setOverlayBackgroundResource(R.color.black_trans_80)
                    .setExpanded(false)
                    .create();
        }
        mQuitTipsDialog.show();
        return true;
    }

    @Override
    public void destroy() {
        super.destroy();

        mManyLyricsView.release();
        if (mExoPlayer != null) {
            mExoPlayer.release();
        }

        if (mRecordAnimator != null) {
            mRecordAnimator.cancel();
        }

        Observable.create(new ObservableOnSubscribe<Object>() {
            @Override
            public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                File recordFile = new File(AAC_SAVE_PATH);
                if (recordFile.exists()) {
                    recordFile.delete();
                }
                EngineManager.getInstance().destroy("prepare");
                emitter.onComplete();
            }
        }).observeOn(Schedulers.io())
                .subscribe();

        mUiHanlder.removeCallbacksAndMessages(null);
    }
}
