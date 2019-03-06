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
import android.widget.FrameLayout;
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
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.dialog.view.TipsDialogView;
import com.engine.EngineEvent;
import com.engine.EngineManager;
import com.engine.Params;
import com.engine.arccloud.ArcRecognizeListener;
import com.engine.arccloud.RecognizeConfig;
import com.engine.arccloud.SongInfo;
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
import com.zq.lyrics.LyricsReader;
import com.zq.lyrics.event.LrcEvent;
import com.zq.lyrics.model.LyricsLineInfo;
import com.zq.lyrics.widget.AbstractLrcView;
import com.zq.lyrics.widget.ManyLyricsView;
import com.zq.toast.CommonToastView;
import com.zq.toast.NoImageCommonToastView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import io.agora.rtc.Constants;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.engine.EngineEvent.TYPE_MUSIC_PLAY_FINISH;
import static com.zq.lyrics.widget.AbstractLrcView.LRCPLAYERSTATUS_PLAY;

public class AuditionFragment extends BaseFragment {
    public static final String TAG = "AuditionFragment";

    static final int MSG_AUTO_LEAVE_CHANNEL = 9;

    static final int MSG_LYRIC_END_EVENT = 10;

    static final boolean RECORD_BY_CALLBACK = false;
    static final String ACC_SAVE_PATH = new File(U.getAppInfoUtils().getMainDir(), "audition.acc").getAbsolutePath();
    static final String PCM_SAVE_PATH = new File(U.getAppInfoUtils().getMainDir(), "audition.pcm").getAbsolutePath();

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

    IPlayer mExoPlayer;

    Handler mUiHanlder;

    long mStartRecordTs = 0;

    ValueAnimator mRecordAnimator;

    DialogPlus mQuitTipsDialog;

    SkrAudioPermission mSkrAudioPermission = new SkrAudioPermission();

    @Override
    public int initView() {
        return R.layout.audition_sence_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        if (!EngineManager.getInstance().isInit()) {
            // 不能每次都初始化,播放伴奏
            Params params = Params.getFromPref();
            params.setScene(Params.Scene.audiotest);
            EngineManager.getInstance().init("prepare", params);
//            boolean isAnchor = MyUserInfoManager.getInstance().getUid() == 1705476;
            boolean isAnchor = true;
            EngineManager.getInstance().joinRoom("csm" + System.currentTimeMillis(), (int) UserAccountManager.getInstance().getUuidAsLong(), isAnchor);
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
                } else if (MSG_LYRIC_END_EVENT == msg.what) {
                    MyLog.d(TAG, "handleMessage" + ", msg.arg1" + msg.arg1 + ", msg.arg2=" + msg.arg2);
                    if (msg.arg2 == 1) {
                        if (msg.arg1 == 0) {
                            EventBus.getDefault().post(new LrcEvent.LineStartEvent(msg.arg1));
                        }
                    } else {
                        EventBus.getDefault().post(new LrcEvent.LineEndEvent(msg.arg1));
                    }
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

        U.getSoundUtils().preLoad(TAG, R.raw.normal_back);

        RxView.clicks(mIvBack).throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    U.getSoundUtils().play(TAG, R.raw.normal_back);
                    onBackPressed();
//                    EngineManager.getInstance().recognizeInManualMode();
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
                    playLyrics(mSongModel, true);
                });

        RxView.clicks(mIvSave).throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    // 要保存
                    Params.save2Pref(EngineManager.getInstance().getParams());
                    U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(getContext())
                            .setImage(R.drawable.touxiangshezhichenggong_icon)
                            .setText("保存设置成功\n已应用到所有对局")
                            .build());

                    mUiHanlder.postDelayed(() -> {
                        if (getActivity() != null) {
                            getActivity().finish();
                        }
                    }, 2000);
//                    U.getFragmentUtils().popFragment(AuditionFragment.this);
                });

        RxView.clicks(mFlProgressContainer).throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(o -> {
                    if (isRecord) {
                        stopRecord();
                    } else {
                        mSkrAudioPermission.ensurePermission(new Runnable() {
                            @Override
                            public void run() {
                                startRecord();
                            }
                        }, true);
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
        if (MyLog.isDebugLogOpen()) {
            postLyricEndEvent(mLyricsReader);
        }

        mStartRecordTs = System.currentTimeMillis();
        mIvRecordStart.setVisibility(View.GONE);
        mTvRecordStop.setVisibility(View.VISIBLE);
        mRlControlContainer.setVisibility(View.GONE);
        mTvRecordTip.setText("点击结束试音演唱");
        mIvPlay.setEnabled(true);
        if (RECORD_BY_CALLBACK) {
            EngineManager.getInstance().startAudioRecording(PCM_SAVE_PATH, Constants.AUDIO_RECORDING_QUALITY_HIGH, true);
        } else {
            EngineManager.getInstance().startAudioRecording(ACC_SAVE_PATH, Constants.AUDIO_RECORDING_QUALITY_HIGH, false);
        }

        if (MyLog.isDebugLogOpen()) {
            EngineManager.getInstance().startRecognize(RecognizeConfig.newBuilder()
                    .setMode(RecognizeConfig.MODE_MANUAL)
                    .setSongName(mSongModel.getItemName())
                    .setArtist(mSongModel.getOwner())
                    .setMResultListener(new ArcRecognizeListener() {
                        @Override
                        public void onResult(String result, List<SongInfo> list, SongInfo targetSongInfo, int lineNo) {
                            int score = 0;
                            if (targetSongInfo != null) {
                                score = (int) (targetSongInfo.getScore() * 100);
                                U.getToastUtil().showShort("acrscore:" + targetSongInfo.getScore());
                                MyLog.d(TAG, "acrscore=" + score);
                            } else {
//                                score = EngineManager.getInstance().getLineScore();
//                                MyLog.d(TAG, "changba score=" + score);
                            }
                        }
                    }).build());
        }


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

        mUiHanlder.removeMessages(MSG_LYRIC_END_EVENT);

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
            mExoPlayer.reset();
        }
        if (mExoPlayer == null) {
            if (RECORD_BY_CALLBACK) {
                mExoPlayer = new AndroidMediaPlayer();
            } else {
                mExoPlayer = new ExoPlayer();
            }

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
        if (RECORD_BY_CALLBACK) {
            mExoPlayer.startPlayPcm(PCM_SAVE_PATH, 2, 44100, 44100 * 2);
        } else {
            mExoPlayer.startPlay(ACC_SAVE_PATH);
        }

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
        super.setData(type, data);
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
            if (RECORD_BY_CALLBACK) {
                EngineManager.getInstance().startAudioMixing((int) MyUserInfoManager.getInstance().getUid(), accFile.getAbsolutePath(), midiFile.getAbsolutePath(), songModel.getBeginMs(), false, false, 1);
            } else {
                EngineManager.getInstance().startAudioMixing((int) MyUserInfoManager.getInstance().getUid(), accFile.getAbsolutePath(), midiFile.getAbsolutePath(), songModel.getBeginMs(), true, false, 1);
            }
        }
    }

    LyricsReader mLyricsReader;

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
                        lyricsReader.cut(songModel.getRankLrcBeginT(), songModel.getRankLrcEndT());
                        if (isRecord) {
                            Set<Integer> set = new HashSet<>();
                            set.add(lyricsReader.getLineInfoIdByStartTs(songModel.getRankLrcBeginT()));
                            mManyLyricsView.setNeedCountDownLine(set);
                        } else {
                            Set<Integer> set = new HashSet<>();
                            mManyLyricsView.setNeedCountDownLine(set);
                        }
                        MyLog.d(TAG, "getRankLrcBeginT : " + songModel.getRankLrcBeginT());
                        mManyLyricsView.setLyricsReader(lyricsReader);
                        mLyricsReader = lyricsReader;
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

    private void postLyricEndEvent(LyricsReader lyricsReader) {
        if (lyricsReader == null) {
            return;
        }
        Map<Integer, LyricsLineInfo> lyricsLineInfos = lyricsReader.getLrcLineInfos();
        Iterator<Map.Entry<Integer, LyricsLineInfo>> it = lyricsLineInfos.entrySet().iterator();
        mUiHanlder.removeMessages(MSG_LYRIC_END_EVENT);
        while (it.hasNext()) {
            Map.Entry<Integer, LyricsLineInfo> entry = it.next();
            Message msg = mUiHanlder.obtainMessage(MSG_LYRIC_END_EVENT);
            msg.arg1 = entry.getKey();

            if (entry.getKey() == 0) {
                //暂定   1为开始，0为结束
                Message message = mUiHanlder.obtainMessage(MSG_LYRIC_END_EVENT);
                message.arg1 = entry.getKey();
                message.arg2 = 1;
                mUiHanlder.sendMessageDelayed(message, entry.getValue().getStartTime() - mSongModel.getBeginMs());
            }

            if (entry.getValue().getEndTime() > mSongModel.getEndMs()) {
                mUiHanlder.sendMessageDelayed(msg, mSongModel.getEndMs() - mSongModel.getBeginMs());
            } else {
                mUiHanlder.sendMessageDelayed(msg, entry.getValue().getEndTime() - mSongModel.getBeginMs());
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mSkrAudioPermission.onBackFromPermisionManagerMaybe();
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
                MyLog.d(TAG, "onEventMainThread" + " userVolumeInfo=" + userVolumeInfo);
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
        if (MyLog.isDebugLogOpen()) {
            EngineManager.getInstance().recognizeInManualMode(event.getLineNum());
            int score = EngineManager.getInstance().getLineScore();
            U.getToastUtil().showShort("changba score:" + score);
            MyLog.d(TAG, "changba score:" + score);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LrcEvent.LineStartEvent event) {
        Params params = EngineManager.getInstance().getParams();
        if (params != null) {
            params.setLrcHasStart(true);
        }
    }

    @Override
    protected boolean onBackPressed() {
        if (mVoiceControlPanelView.isChange()) {
            if (mQuitTipsDialog == null) {
                TipsDialogView tipsDialogView = new TipsDialogView.Builder(getContext())
                        .setMessageTip("直接返回你的设置变动\n将不会被保存哦～")
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
                                    Activity activity = getActivity();
                                    if (activity != null) {
                                        activity.finish();
                                    }
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
        } else {
            if (getActivity() != null) {
                getActivity().finish();
            }
            return true;
        }
    }

    @Override
    public void destroy() {
        super.destroy();

        mUiHanlder.removeMessages(MSG_LYRIC_END_EVENT);
        mManyLyricsView.release();
        if (mExoPlayer != null) {
            mExoPlayer.release();
        }

        if (mRecordAnimator != null) {
            mRecordAnimator.cancel();
        }
        EngineManager.getInstance().destroy("prepare");
        File recordFile = new File(PCM_SAVE_PATH);
        if (recordFile != null && recordFile.exists()) {
            recordFile.delete();
        }
        mUiHanlder.removeCallbacksAndMessages(null);

        U.getSoundUtils().release(TAG);
    }
}
