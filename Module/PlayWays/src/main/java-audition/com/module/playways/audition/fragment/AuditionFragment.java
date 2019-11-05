package com.module.playways.audition.fragment;

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
import com.common.log.MyLog;
import com.common.statistics.StatisticsAdapter;
import com.common.utils.ActivityUtils;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.component.voice.control.VoiceControlPanelView;
import com.engine.EngineEvent;
import com.engine.Params;
import com.engine.arccloud.AcrRecognizeListener;
import com.engine.arccloud.RecognizeConfig;
import com.engine.arccloud.SongInfo;
import com.module.playways.R;
import com.component.lyrics.LyricAndAccMatchManager;
import com.module.playways.room.prepare.model.PrepareData;
import com.module.playways.room.room.view.RankTopContainerView2;
import com.module.playways.room.song.model.SongModel;
import com.component.lyrics.LyricsReader;
import com.component.lyrics.utils.SongResUtils;
import com.component.lyrics.widget.ManyLyricsView;
import com.component.lyrics.widget.VoiceScaleView;
import com.zq.mediaengine.kit.ZqEngineKit;
import com.component.toast.NoImageCommonToastView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.engine.EngineEvent.TYPE_MUSIC_PLAY_FINISH;
import static com.component.lyrics.widget.AbstractLrcView.LRCPLAYERSTATUS_PLAY;

public class AuditionFragment extends BaseFragment {
    public static final String TAG = "AuditionFragment";

    static final int MSG_AUTO_LEAVE_CHANNEL = 9;
    static final String AAC_SAVE_PATH = new File(U.getAppInfoUtils().getMainDir(), "audition.mp4").getAbsolutePath();

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
    String mRecordPath;

    private boolean mIsVoiceShow = true;

    private volatile boolean isRecord = false;

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
        if (!ZqEngineKit.getInstance().isInit()) {
            // 不能每次都初始化,播放伴奏
            Params params = Params.getFromPref();
            params.setScene(Params.Scene.audiotest);
            ZqEngineKit.getInstance().init("prepare", params);
//            boolean isAnchor = MyUserInfoManager.getInstance().getUid() == 1705476;
            boolean isAnchor = true;
            ZqEngineKit.getInstance().joinRoom("csm" + System.currentTimeMillis(),
                    (int) UserAccountManager.INSTANCE.getUuidAsLong(), isAnchor, null);
        } else {
            ZqEngineKit.getInstance().resumeAudioMixing();
        }

        mRankTopView = (RankTopContainerView2) getRootView().findViewById(R.id.rank_top_view);
        mBottomContainer = (LinearLayout) getRootView().findViewById(R.id.bottom_container);
        mBackArea = (RelativeLayout) getRootView().findViewById(R.id.back_area);
        mAuditionArea = (RelativeLayout) getRootView().findViewById(R.id.audition_area);
        mTiaoyinIv = (ImageView) getRootView().findViewById(R.id.tiaoyin_iv);
        mTiaoyinTv = (ExTextView) getRootView().findViewById(R.id.tiaoyin_tv);
        mResArea = (RelativeLayout) getRootView().findViewById(R.id.res_area);
        mCompleArea = (RelativeLayout) getRootView().findViewById(R.id.comple_area);
        mTvSongName = (ExTextView) getRootView().findViewById(R.id.tv_song_name);
        mManyLyricsView = (ManyLyricsView) getRootView().findViewById(R.id.many_lyrics_view);
        mVoiceControlView = (VoiceControlPanelView) getRootView().findViewById(R.id.voice_control_view);
        mVoiceScaleView = (VoiceScaleView) getRootView().findViewById(R.id.voice_scale_view);
        mLogView = getRootView().findViewById(R.id.log_view);
        View mLogViewScrollContainer = getRootView().findViewById(R.id.log_view_scroll_container);
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
                if (ZqEngineKit.getInstance().getParams().isMixMusicPlaying()) {
                    ZqEngineKit.getInstance().pauseAudioMixing();
                }
                mManyLyricsView.pause();
            }

            @Override
            public void onSigleTap(int progress) {
                MyLog.d(TAG, "progress " + progress);
                if (progress > 0) {
                    ZqEngineKit.getInstance().setAudioMixingPosition(progress - mSongModel.getBeginMs());
                    mManyLyricsView.seekTo(progress);
                }

                if (!ZqEngineKit.getInstance().getParams().isMixMusicPlaying()) {
                    ZqEngineKit.getInstance().resumeAudioMixing();
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

        StatisticsAdapter.recordCountEvent("rank", "practice", null);
    }

    private void startRecord() {
        ZqEngineKit.getInstance().stopAudioRecording();
        ZqEngineKit.getInstance().stopAudioMixing();
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

        LyricAndAccMatchManager.ConfigParams configParams = new LyricAndAccMatchManager.ConfigParams();
        configParams.setManyLyricsView(mManyLyricsView);
        configParams.setVoiceScaleView(mVoiceScaleView);
        configParams.setLyricUrl(mSongModel.getLyric());
        configParams.setLyricBeginTs( mSongModel.getRankLrcBeginT());
        configParams.setLyricEndTs( mSongModel.getRankLrcEndT());
        configParams.setAccBeginTs(mSongModel.getBeginMs());
        configParams.setAccEndTs(mSongModel.getEndMs());
        configParams.setAuthorName(mSongModel.getUploaderName());
        mLyricAndAccMatchManager.setArgs(configParams);
        mLyricAndAccMatchManager.start(new LyricAndAccMatchManager.Listener() {

            @Override
            public void onLyricParseSuccess(LyricsReader reader) {

            }

            @Override
            public void onLyricParseFailed() {

            }

            @Override
            public void onLyricEventPost(int lineNum) {
                mTotalLineNum = lineNum;
                mStartRecordTs = System.currentTimeMillis();
                mCbScoreList.clear();
                if (ZqEngineKit.RECORD_FOR_DEBUG) {
                    mRecordPath = U.getAppInfoUtils().getMainDir().getAbsolutePath() + "/" + mSongModel.getItemName() + "_" + mSongModel.getOwner() + ".wav";
                    ZqEngineKit.getInstance().startAudioRecording(mRecordPath, true);
                } else {
                    mRecordPath = AAC_SAVE_PATH;
                    ZqEngineKit.getInstance().startAudioRecording(mRecordPath, false);
                }
            }
        });
//        playLyrics(mSongModel, true, false);
        playMusic(mSongModel);
        mStartRecordTs = System.currentTimeMillis();

        ZqEngineKit.getInstance().startRecognize(RecognizeConfig.newBuilder()
                .setMode(RecognizeConfig.MODE_MANUAL)
                .setSongName(mSongModel.getItemName())
                .setArtist(mSongModel.getOwner())
                .setMResultListener(new AcrRecognizeListener() {
                    @Override
                    public void onResult(String result, List<SongInfo> list, SongInfo targetSongInfo, int lineNo) {
                        mLyricAndAccMatchManager.onAcrResult(result, list, targetSongInfo, lineNo);
                    }
                }).build());
    }

    private void reset() {
        isRecord = true;
        mRankTopView.reset();
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
        ZqEngineKit.getInstance().stopAudioRecording();
        ZqEngineKit.getInstance().stopAudioMixing();

        // TODO: 2019/3/14 原来会自动播放，现在该为跳到PlayRecordFragment中去
//        playLyrics(mSongModel, true, false);
//
//        playRecord();

        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(getActivity(), PlayRecordFragment.class)
                .setAddToBackStack(true)
                .setHasAnimation(true)
                .addDataBeforeAdd(0, mSongModel)
                .addDataBeforeAdd(1, mRecordPath)
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
            if (ZqEngineKit.RECORD_FOR_DEBUG) {
                ZqEngineKit.getInstance().startAudioMixing((int) MyUserInfoManager.INSTANCE.getUid(), accFile.getAbsolutePath(), midiFile.getAbsolutePath(), songModel.getBeginMs(), false, false, 1);
            } else {
                ZqEngineKit.getInstance().startAudioMixing((int) MyUserInfoManager.INSTANCE.getUid(), accFile.getAbsolutePath(), midiFile.getAbsolutePath(), songModel.getBeginMs(), true, false, 1);
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
        mSkrAudioPermission.onBackFromPermisionManagerMaybe(getActivity());
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
                //MyLog.d(TAG, "onEventMainThread" + " userVolumeInfo=" + userVolumeInfo);
                if (userVolumeInfo.getUid() == 0 && userVolumeInfo.getVolume() > 0) {
                    //如果自己在唱歌也延迟关闭
                    resendAutoLeaveChannelMsg();
                }
            }
        }
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
    public void onEvent(ActivityUtils.ForeOrBackgroundChange event) {
        MyLog.w(TAG, event.foreground ? "切换到前台" : "切换到后台");
        if (!event.foreground) {
            ZqEngineKit.getInstance().stopAudioRecording();
            ZqEngineKit.getInstance().stopAudioMixing();
        } else {
            reset();
            mVoiceScaleView.setVisibility(View.GONE);
            mManyLyricsView.pause();
        }
    }

    @Override
    public boolean onBackPressed() {
        Params.save2Pref(ZqEngineKit.getInstance().getParams());
        Activity activity = getActivity();
        if (activity != null) {
            activity.finish();
        }
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LyricAndAccMatchManager.ScoreResultEvent event) {
        int line = event.getLine();
        int acrScore = event.getAcrScore();
        int melpScore = event.getMelpScore();
        String from = event.getFrom();
        if (MyLog.isDebugLogOpen()) {
            StringBuilder sb = new StringBuilder();
            sb.append("第").append(line).append("行,");
            sb.append(" melpScore=").append(melpScore);
            sb.append(" acrScore=").append(acrScore);
            addLogText(sb.toString());
        }
        if (melpScore > acrScore) {
            processScore(from, melpScore, line);
        } else {
            processScore(from, acrScore, line);
        }
    }

    private void processScore(String from, int score, int line) {
        MyLog.d(TAG, "processScore" + " from=" + from + " score=" + score + " line=" + line);
        mCbScoreList.add(score);
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
        ZqEngineKit.getInstance().destroy("prepare");
        mUiHandler.removeCallbacksAndMessages(null);
        U.getSoundUtils().release(TAG);
    }
}
