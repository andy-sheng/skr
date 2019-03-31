package com.module.playways.rank.prepare.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.base.BaseFragment;
import com.common.base.FragmentDataListener;
import com.common.clipboard.ClipboardUtils;
import com.common.core.account.UserAccountManager;
import com.common.core.myinfo.MyUserInfoManager;
import com.common.core.permission.SkrAudioPermission;
import com.common.engine.ScoreConfig;
import com.common.log.MyLog;
import com.common.utils.ActivityUtils;
import com.common.utils.FragmentUtils;
import com.common.utils.SongResUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.engine.EngineEvent;
import com.engine.EngineManager;
import com.engine.Params;
import com.engine.arccloud.ArcRecognizeListener;
import com.engine.arccloud.RecognizeConfig;
import com.engine.arccloud.SongInfo;
import com.engine.score.Score2Callback;
import com.module.playways.rank.others.LyricAndAccMatchManager;
import com.module.playways.rank.prepare.model.PrepareData;
import com.module.playways.rank.prepare.view.VoiceControlPanelView;
import com.module.playways.rank.room.view.RankTopContainerView2;
import com.module.playways.rank.song.model.SongModel;
import com.module.rank.R;
import com.zq.lyrics.LyricsReader;
import com.zq.lyrics.event.LrcEvent;
import com.zq.lyrics.widget.ManyLyricsView;
import com.zq.lyrics.widget.VoiceScaleView;
import com.zq.toast.NoImageCommonToastView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.agora.rtc.Constants;

import static com.engine.EngineEvent.TYPE_MUSIC_PLAY_FINISH;
import static com.zq.lyrics.widget.AbstractLrcView.LRCPLAYERSTATUS_PLAY;

public class AuditionFragment extends BaseFragment {
    public static final String TAG = "AuditionFragment";

    static final int MSG_AUTO_LEAVE_CHANNEL = 9;

    static final int MSG_SHOW_SCORE_EVENT = 32;

    static final boolean RECORD_BY_CALLBACK = false;
    static final String ACC_SAVE_PATH = new File(U.getAppInfoUtils().getMainDir(), "audition.acc").getAbsolutePath();
    static final String PCM_SAVE_PATH = new File(U.getAppInfoUtils().getMainDir(), "audition.pcm").getAbsolutePath();

    RankTopContainerView2 mRankTopView;
    LinearLayout mBottomContainer;
    RelativeLayout mBackArea;
    RelativeLayout mAuditionArea;
    ImageView mTiaoyinIv;
    ExTextView mTiaoyinTv;
    RelativeLayout mResArea;
    RelativeLayout mCompleArea;
    ExTextView mTvSongName;
    ManyLyricsView mManyLyricsView;
    VoiceControlPanelView mVoiceControlView;
    VoiceScaleView mVoiceScaleView;
    TextView mLogView;
    LyricsReader mLyricsReader;
    PrepareData mPrepareData;

    SongModel mSongModel;

    private boolean mIsVoiceShow = true;

    private volatile boolean isRecord = false;

    private int mLastLineNum = -1;

    private int mTotalLineNum = -1;

    LyricAndAccMatchManager mLyricAndAccMatchManager = new LyricAndAccMatchManager();

    Handler mUiHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_AUTO_LEAVE_CHANNEL:
                    // 为了省钱，因为引擎每多在试音房一分钟都是消耗，防止用户挂机
                    U.getFragmentUtils().popFragment(AuditionFragment.this);
                    break;
                default:
                    int lineNo = (msg.what - MSG_SHOW_SCORE_EVENT) / 100;
                    MyLog.d(TAG, "handleMessage" + " lineNo=" + lineNo);
                    if (lineNo > mLastLineNum) {
                        if (ScoreConfig.isMelpEnable()) {
                            int melp1score = EngineManager.getInstance().getLineScore1();
                            if (MyLog.isDebugLogOpen()) {
                                addLogText("第" + lineNo + "行,melp1得分:" + melp1score);
                            }
                            MyLog.d(TAG, "handleMessage acr超时 本地获取得分:" + melp1score);
                            processScore(melp1score, lineNo);
                        }

                        if (ScoreConfig.isMelp2Enable()) {
                            EngineManager.getInstance().getLineScore2(lineNo, new Score2Callback() {
                                @Override
                                public void onGetScore(int lineNum, int melp2score) {
                                    if (MyLog.isDebugLogOpen()) {
                                        addLogText("第" + lineNo + "行 melp2得分:" + melp2score);
                                    }
                                    processScore(melp2score, lineNo);
                                }
                            });
                        }

                        if (MyLog.isDebugLogOpen()) {
                            addLogText("第" + lineNo + "行,acr请求未及时返回");
                        }
                    }
                    break;
            }
        }
    };

    long mStartRecordTs = 0;

//    DialogPlus mQuitTipsDialog;

    SkrAudioPermission mSkrAudioPermission = new SkrAudioPermission();

    List<Integer> mCbScoreList = new ArrayList<>();

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
            EngineManager.getInstance().joinRoom("csm" + System.currentTimeMillis(), (int) UserAccountManager.getInstance().getUuidAsLong(), isAnchor, null);
        } else {
            EngineManager.getInstance().resumeAudioMixing();
        }

        mRankTopView = (RankTopContainerView2) mRootView.findViewById(R.id.rank_top_view);
        mBottomContainer = (LinearLayout) mRootView.findViewById(R.id.bottom_container);
        mBackArea = (RelativeLayout) mRootView.findViewById(R.id.back_area);
        mAuditionArea = (RelativeLayout) mRootView.findViewById(R.id.audition_area);
        mTiaoyinIv = (ImageView) mRootView.findViewById(R.id.tiaoyin_iv);
        mTiaoyinTv = (ExTextView) mRootView.findViewById(R.id.tiaoyin_tv);
        mResArea = (RelativeLayout) mRootView.findViewById(R.id.res_area);
        mCompleArea = (RelativeLayout) mRootView.findViewById(R.id.comple_area);
        mTvSongName = (ExTextView) mRootView.findViewById(R.id.tv_song_name);
        mManyLyricsView = (ManyLyricsView) mRootView.findViewById(R.id.many_lyrics_view);
        mVoiceControlView = (VoiceControlPanelView) mRootView.findViewById(R.id.voice_control_view);
        mVoiceScaleView = (VoiceScaleView) mRootView.findViewById(R.id.voice_scale_view);
        mLogView = mRootView.findViewById(R.id.log_view);
        View mLogViewScrollContainer = mRootView.findViewById(R.id.log_view_scroll_container);
        if (MyLog.isDebugLogOpen()) {
            mLogViewScrollContainer.setVisibility(View.VISIBLE);
        } else {
            mLogViewScrollContainer.setVisibility(View.GONE);
        }
        U.getSoundUtils().preLoad(TAG, R.raw.normal_back);

        mBackArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
//                U.getSoundUtils().play(TAG, R.raw.normal_back);
                onBackPressed();
            }
        });

        mAuditionArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                resendAutoLeaveChannelMsg();
                showVoicePanelView(!mIsVoiceShow);
            }
        });

        mResArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                startRecord();
            }
        });

        mCompleArea.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                stopRecord();
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
        resendAutoLeaveChannelMsg();
        startRecord();
    }

    private void startRecord() {
        EngineManager.getInstance().stopAudioRecording();
        EngineManager.getInstance().stopAudioMixing();
        mSkrAudioPermission.ensurePermission(new Runnable() {
            @Override
            public void run() {
                startRecord1();
            }
        }, true);

    }

    private void startRecord1() {
        reset();
        if (mLyricsReader != null) {
            mVoiceScaleView.startWithData(mLyricsReader.getLyricsLineInfoList(), mSongModel.getBeginMs());
        }

        mLyricAndAccMatchManager.setArgs(mManyLyricsView, mVoiceScaleView, mSongModel.getLyric(),
                mSongModel.getRankLrcBeginT(), mSongModel.getRankLrcEndT(),
                mSongModel.getBeginMs(), mSongModel.getEndMs());
        mLyricAndAccMatchManager.start(new LyricAndAccMatchManager.Listener() {
            @Override
            public void onLyricParseSuccess() {

            }

            @Override
            public void onLyricParseFailed() {

            }

            @Override
            public void onLyricEventPost(int lineNum) {
                mTotalLineNum = lineNum;
                mStartRecordTs = System.currentTimeMillis();
                mCbScoreList.clear();
                if (RECORD_BY_CALLBACK) {
                    EngineManager.getInstance().startAudioRecording(PCM_SAVE_PATH, Constants.AUDIO_RECORDING_QUALITY_HIGH, true);
                } else {
                    EngineManager.getInstance().startAudioRecording(ACC_SAVE_PATH, Constants.AUDIO_RECORDING_QUALITY_HIGH, false);
                }

            }
        });
//        playLyrics(mSongModel, true, false);
        playMusic(mSongModel);
        mStartRecordTs = System.currentTimeMillis();

        EngineManager.getInstance().startRecognize(RecognizeConfig.newBuilder()
                .setMode(RecognizeConfig.MODE_MANUAL)
                .setSongName(mSongModel.getItemName())
                .setArtist(mSongModel.getOwner())
                .setMResultListener(new ArcRecognizeListener() {
                    @Override
                    public void onResult(String result, List<SongInfo> list, SongInfo targetSongInfo, int lineNo) {
                        mUiHandler.removeMessages(MSG_SHOW_SCORE_EVENT + lineNo * 100);
                        if (lineNo > mLastLineNum) {
                            int acrScore = -1;
                            if (targetSongInfo != null) {
                                acrScore = (int) (targetSongInfo.getScore() * 100);
                                if (MyLog.isDebugLogOpen()) {
                                    addLogText("第" + lineNo + "行 acr得分:" + acrScore);
                                }
                            } else {
                                if (MyLog.isDebugLogOpen()) {
                                    addLogText("第" + lineNo + "行 acr得分:未识别");
                                }
                            }

                            if (ScoreConfig.isMelpEnable()) {
                                int melp1Score = EngineManager.getInstance().getLineScore1();
                                if (MyLog.isDebugLogOpen()) {
                                    addLogText("第" + lineNo + "行 melp1得分:" + melp1Score);
                                }
                                if (melp1Score > acrScore) {
                                    processScore(melp1Score, lineNo);
                                } else {
                                    processScore(acrScore, lineNo);
                                }
                            } else {
                                processScore(acrScore, lineNo);
                            }

                            if (ScoreConfig.isMelp2Enable()) {
                                int finalAcrScore = acrScore;
                                EngineManager.getInstance().getLineScore2(lineNo, new Score2Callback() {
                                    @Override
                                    public void onGetScore(int lineNum, int melp2score) {
                                        if (MyLog.isDebugLogOpen()) {
                                            addLogText("第" + lineNo + "行 melp2得分:" + melp2score);
                                        }
                                        if (melp2score > finalAcrScore) {
                                            processScore(melp2score, lineNo);
                                        } else {
                                            processScore(finalAcrScore, lineNo);
                                        }
                                    }
                                });
                            }
                        }
                    }
                }).build());
    }

    private void reset() {
        isRecord = true;
        mRankTopView.reset();
        mLastLineNum = -1;
        mAuditionArea.setVisibility(View.VISIBLE);
        showVoicePanelView(true);
        mVoiceScaleView.setVisibility(View.VISIBLE);
        mLogView.setText("");
    }

    private void addLogText(String txt) {
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                String str = mLogView.getText() + txt + "\n";
                mLogView.setText(str);
            }
        });
    }

    private void stopRecord() {
        if (!isRecord) {
            return;
        }

        if (System.currentTimeMillis() - mStartRecordTs < 5000) {
            U.getToastUtil().showSkrCustomShort(new NoImageCommonToastView.Builder(U.app())
                    .setText("太短啦\n再唱几句吧~")
                    .build());
            return;
        }
        mLyricAndAccMatchManager.stop();
        mVoiceScaleView.setVisibility(View.GONE);
        isRecord = false;
        EngineManager.getInstance().stopAudioRecording();
        EngineManager.getInstance().stopAudioMixing();

        // TODO: 2019/3/14 原来会自动播放，现在该为跳到PlayRecordFragment中去
//        playLyrics(mSongModel, true, false);
//
//        playRecord();

        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), PlayRecordFragment.class)
                .setAddToBackStack(true)
                .setHasAnimation(true)
                .addDataBeforeAdd(0, mSongModel)
                .setFragmentDataListener(new FragmentDataListener() {
                    @Override
                    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle, Object obj) {
                        if (requestCode == 0 && resultCode == 0) {
                            startRecord();
                        }
                    }
                })
                .build());

        if (MyLog.isDebugLogOpen()) {
            jisuanScore();
        }
    }

    private void resendAutoLeaveChannelMsg() {
        mUiHandler.removeMessages(MSG_AUTO_LEAVE_CHANNEL);
        mUiHandler.sendEmptyMessageDelayed(MSG_AUTO_LEAVE_CHANNEL, 60 * 1000 * 10);
    }

    private void showVoicePanelView(boolean show) {
        if (mIsVoiceShow == show) {
            return;
        }
        mVoiceControlView.clearAnimation();
        mVoiceControlView.setTranslationY(show ? mVoiceControlView.getMeasuredHeight() + U.getDisplayUtils().dip2px(20) : 0);

        mIsVoiceShow = show;
        if (mIsVoiceShow) {
            mTiaoyinIv.setImageResource(R.drawable.audition_tiaoyin_anxia);
            mTiaoyinTv.setTextColor(Color.parseColor("#99EF5E85"));
        } else {
            mTiaoyinIv.setImageResource(R.drawable.audition_tiaoyin);
            mTiaoyinTv.setTextColor(Color.parseColor("#99B2B6D6"));
        }
        int startY = show ? mVoiceControlView.getMeasuredHeight() + U.getDisplayUtils().dip2px(20) : 0;
        int endY = show ? 0 : mVoiceControlView.getMeasuredHeight() + U.getDisplayUtils().dip2px(20);

        ValueAnimator creditValueAnimator = ValueAnimator.ofInt(startY, endY);
        creditValueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mVoiceControlView.setTranslationY((int) animation.getAnimatedValue());
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

//    private void playLyrics(SongModel songModel, boolean play, boolean isFirst) {
//        final String lyricFile = SongResUtils.getFileNameWithMD5(songModel.getLyric());
//
//        if (lyricFile != null) {
//            LyricsManager.getLyricsManager(U.app())
//                    .loadLyricsObserable(lyricFile, lyricFile.hashCode() + "")
//                    .subscribeOn(Schedulers.io())
//                    .retry(10)
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .compose(bindUntilEvent(FragmentEvent.DESTROY))
//                    .subscribe(lyricsReader -> {
//                        MyLog.d(TAG, "playMusic, start play lyric");
//                        mManyLyricsView.resetData();
//                        mManyLyricsView.initLrcData();
//                        lyricsReader.cut(songModel.getRankLrcBeginT(), songModel.getRankLrcEndT());
//                        if (isRecord) {
//                            Set<Integer> set = new HashSet<>();
//                            set.add(lyricsReader.getLineInfoIdByStartTs(songModel.getRankLrcBeginT()));
//                            mManyLyricsView.setNeedCountDownLine(set);
//                        } else {
//                            Set<Integer> set = new HashSet<>();
//                            mManyLyricsView.setNeedCountDownLine(set);
//                        }
//                        MyLog.d(TAG, "getRankLrcBeginT : " + songModel.getRankLrcBeginT());
//                        mManyLyricsView.setLyricsReader(lyricsReader);
//                        mLyricsReader = lyricsReader;
//                        if (mManyLyricsView.getLrcStatus() == AbstractLrcView.LRCSTATUS_LRC && mManyLyricsView.getLrcPlayerStatus() != LRCPLAYERSTATUS_PLAY) {
//                            mManyLyricsView.play(songModel.getBeginMs());
//                            MyLog.d(TAG, "songModel.getBeginMs() : " + songModel.getBeginMs());
//                        }
//
//                        if (!play) {
//                            mManyLyricsView.pause();
//                        }
//
//                        if (isFirst) {
//                            startRecord();
//                        }
//                    }, throwable -> MyLog.e(throwable));
//        } else {
//            MyLog.e(TAG, "没有歌词文件，不应该，进界面前已经下载好了");
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();
        mSkrAudioPermission.onBackFromPermisionManagerMaybe();
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEventMainThread(EngineEvent event) {
//        MyLog.d(TAG, "restartLrcEvent type is " + restartLrcEvent.getType());
        if (event.getType() == TYPE_MUSIC_PLAY_FINISH) {
            mUiHandler.post(() -> {
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
    public void onEvent(LrcEvent.LineLineEndEvent event) {
        if (ScoreConfig.isAcrEnable()) {
            EngineManager.getInstance().recognizeInManualMode(event.lineNum);
        } else {
            if (ScoreConfig.isMelpEnable()) {
                int score = EngineManager.getInstance().getLineScore1();
                if (MyLog.isDebugLogOpen()) {
                    addLogText("第" + event.lineNum + "行 melp得分:" + score);
                }
                processScore(score, event.lineNum);
            }
            if (ScoreConfig.isMelp2Enable()) {
                EngineManager.getInstance().getLineScore2(event.lineNum, new Score2Callback() {
                    @Override
                    public void onGetScore(int lineNum, int score) {
                        if (MyLog.isDebugLogOpen()) {
                            addLogText("第" + lineNum + "行 melp2得分:" + score);
                        }
                        processScore(score, event.lineNum);
                    }
                });
            }
        }
        Message msg = mUiHandler.obtainMessage(MSG_SHOW_SCORE_EVENT + event.lineNum * 100);
        mUiHandler.sendMessageDelayed(msg, 1000);
    }

    void jisuanScore() {
        if (mCbScoreList.isEmpty()) {
            return;
        }
        int total = 0;
        for (int i = 0; i < mCbScoreList.size(); i++) {
            total += mCbScoreList.get(i);
        }
        int pj = total / mCbScoreList.size();
        int fc = 0;
        for (int i = 0; i < mCbScoreList.size(); i++) {
            int t = mCbScoreList.get(i) - pj;
            fc += (t * t);
        }
        if (MyLog.isDebugLogOpen()) {
            ClipboardUtils.setCopy(mLogView.getText().toString());
            U.getToastUtil().showShort("平均分:" + pj + " 方差:" + fc + "得分记录已在剪贴板中");
        }
        MyLog.d(TAG, "平均分:" + pj + " 方差:" + fc);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LrcEvent.LyricStartEvent event) {
        Params params = EngineManager.getInstance().getParams();
        if (params != null) {
            params.setLrcHasStart(true);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(ActivityUtils.ForeOrBackgroundChange event) {
        MyLog.w(TAG, event.foreground ? "切换到前台" : "切换到后台");
        if (!event.foreground) {
            EngineManager.getInstance().stopAudioRecording();
            EngineManager.getInstance().stopAudioMixing();
        } else {
            reset();
            mVoiceScaleView.setVisibility(View.GONE);
            mManyLyricsView.pause();
        }
    }

    @Override
    protected boolean onBackPressed() {
        Params.save2Pref(EngineManager.getInstance().getParams());
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
//            if (mQuitTipsDialog == null) {
//                TipsDialogView tipsDialogView = new TipsDialogView.Builder(getContext())
//                        .setMessageTip("直接返回你的设置变动\n将不会被保存哦～")
//                        .setConfirmTip("保存")
//                        .setCancelTip("取消")
//                        .setConfirmBtnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                mQuitTipsDialog.dismiss(false);
//                                // 要保存
//                                Params.save2Pref(EngineManager.getInstance().getParams());
//                                U.getToastUtil().showSkrCustomShort(new CommonToastView.Builder(U.app())
//                                        .setImage(R.drawable.touxiangshezhichenggong_icon)
//                                        .setText("保存设置成功\n已应用到所有对局")
//                                        .build());
//
//                                mUiHandler.postDelayed(() -> {
//                                    Activity activity = getActivity();
//                                    if (activity != null) {
//                                        activity.finish();
//                                    }
//                                }, 2000);
//
//                            }
//                        })
//                        .setCancelBtnClickListener(new View.OnClickListener() {
//                            @Override
//                            public void onClick(View v) {
//                                mQuitTipsDialog.dismiss(false);
//                                U.getFragmentUtils().popFragment(AuditionFragment.this);
//                            }
//                        })
//                        .build();
//
//                mQuitTipsDialog = DialogPlus.newDialog(getContext())
//                        .setContentHolder(new ViewHolder(tipsDialogView))
//                        .setGravity(Gravity.BOTTOM)
//                        .setContentBackgroundResource(R.color.transparent)
//                        .setOverlayBackgroundResource(R.color.black_trans_80)
//                        .setExpanded(false)
//                        .create();
//            }
//            mQuitTipsDialog.show();
        return true;
    }

    private void processScore(int score, int line) {
        if (line <= mLastLineNum) {
            return;
        }
        if (score < 0) {
            return;
        }
        mCbScoreList.add(score);
        mLastLineNum = line;
        mUiHandler.post(new Runnable() {
            @Override
            public void run() {
                mRankTopView.setScoreProgress(score, 0, mTotalLineNum);
            }
        });
    }

    @Override
    public void destroy() {
        super.destroy();
        mLyricAndAccMatchManager.stop();
        mManyLyricsView.release();
        EngineManager.getInstance().destroy("prepare");
        File recordFile = new File(PCM_SAVE_PATH);
        if (recordFile != null && recordFile.exists()) {
            recordFile.delete();
        }
        mUiHandler.removeCallbacksAndMessages(null);
        U.getSoundUtils().release(TAG);
    }
}
