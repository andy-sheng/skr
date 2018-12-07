package com.module.rankingmode.prepare.fragment;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.utils.FragmentUtils;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.rankingmode.R;
import com.module.rankingmode.fragment.AuditionFragment;
import com.module.rankingmode.prepare.event.MatchStatusChangeEvent;
import com.module.rankingmode.prepare.presenter.MatchPresenter;
import com.module.rankingmode.prepare.view.MatchStartView;
import com.module.rankingmode.prepare.view.MatchSucessView;
import com.module.rankingmode.prepare.view.MatchingView;
import com.module.rankingmode.prepare.view.VoiceControlPanelView;
import com.module.rankingmode.prepare.view.VoiceLineView;
import com.module.rankingmode.view.AudioControlPanelView;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.zq.lyrics.inter.IlyricController;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

/**
 * 匹配界面
 */
public class MatchingFragment extends BaseFragment {

    public final static String TAG = "MatchingFragment";

    public static final String KEY_SONG_NAME = "key_song_name"; //歌曲名
    public static final String KEY_SONG_TIME = "key_song_time"; //歌曲时长

    RelativeLayout mMainActContainer;

    CommonTitleBar mTitleBar;
    RelativeLayout mMatchContent; // 匹配中间的容器

    ExTextView mToneTuningTv;   //试音调音
    ExTextView mMatchStatusTv;

    ExTextView mPredictTimeTv;  //预计等待时间
    ExTextView mWaitTimeTv;     //已经等待时间
    ExTextView mPrepareTipsTv;  //准备提示信息

    VoiceLineView mVoiceLineView;
    MediaRecorder mMediaRecorder;

    MatchStartView startMatchView;
    MatchingView matchingView;
    MatchSucessView matchSucessView;

    MatchPresenter matchPresenter;

    String songName;
    String songTime;

    HandlerTaskTimer mHandlerTaskTimer;

    @Override
    public int initView() {
        return R.layout.matching_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);

        mTitleBar = (CommonTitleBar)mRootView.findViewById(R.id.title_bar);
        mMatchContent = (RelativeLayout) mRootView.findViewById(R.id.match_content);

        mToneTuningTv = (ExTextView) mRootView.findViewById(R.id.tone_tuning_tv);
        mMatchStatusTv = (ExTextView) mRootView.findViewById(R.id.match_status_tv);

        mPredictTimeTv = (ExTextView) mRootView.findViewById(R.id.predict_time_tv);
        mWaitTimeTv = (ExTextView) mRootView.findViewById(R.id.wait_time_tv);

        mPrepareTipsTv = (ExTextView) mRootView.findViewById(R.id.prepare_tips_tv);

        mVoiceLineView = (VoiceLineView) mRootView.findViewById(R.id.voice_line_view);

        if (startMatchView == null) {
            startMatchView = new MatchStartView(getContext());
        }
        mMatchStatusTv.setTag(MatchStatusChangeEvent.MATCH_STATUS_START);
        mMatchContent.addView(startMatchView);

        matchPresenter = new MatchPresenter();

        Bundle bundle = getArguments();
        if (bundle != null) {
            songName = bundle.getString(KEY_SONG_NAME);
            songTime = bundle.getString(KEY_SONG_TIME);
        }


        RxView.clicks(mMatchStatusTv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        switch ((int) mMatchStatusTv.getTag()) {
                            case MatchStatusChangeEvent.MATCH_STATUS_START:
                                matchPresenter.startMatch();
                                break;
                            case MatchStatusChangeEvent.MATCH_STATUS_MATCHING:
                                matchPresenter.cancelMatch();
                                break;
                            case MatchStatusChangeEvent.MATCH_STATUS_MATCH_SUCESS:
                                break;
                            default:
                                break;
                        }
                    }
                });

        RxView.clicks(mToneTuningTv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        // todo 试唱调音
                        VoiceControlPanelView view = new VoiceControlPanelView(getContext());

                        DialogPlus.newDialog(getContext())
                                .setExpanded(true, U.getDisplayUtils().getScreenHeight() / 2)
                                .setContentHolder(new ViewHolder(view))
                                .setGravity(Gravity.BOTTOM)
                                .setCancelable(true)
                                .create().show();

                    }
                });

        initMediaRecoder();
    }

    // todo 后面会用我们自己的去采集声音拿到音高
    private void initMediaRecoder() {
        if (mMediaRecorder == null)
            mMediaRecorder = new MediaRecorder();

        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        File file = new File(Environment.getExternalStorageDirectory().getPath(), "hello.log");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mMediaRecorder.setOutputFile(file.getAbsolutePath());
        mMediaRecorder.setMaxDuration(1000 * 60 * 10);
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaRecorder.start();

        mHandlerTaskTimer = HandlerTaskTimer.newBuilder().interval(50)
                .start(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Integer integer) {
                        if (mMediaRecorder == null) return;
                        double ratio = (double) mMediaRecorder.getMaxAmplitude() / 100;
                        double db = 0;// 分贝
                        //默认的最大音量是100,可以修改，但其实默认的，在测试过程中就有不错的表现
                        //你可以传自定义的数字进去，但需要在一定的范围内，比如0-200，就需要在xml文件中配置maxVolume
                        //同时，也可以配置灵敏度sensibility
                        if (ratio > 1)
                            db = 20 * Math.log10(ratio);
                        //只要有一个线程，不断调用这个方法，就可以使波形变化
                        //主要，这个方法必须在ui线程中调用
                        mVoiceLineView.setVolume((int) (db));
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    @Override
    public void destroy() {
        super.destroy();
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        if (mHandlerTaskTimer != null) {
            mHandlerTaskTimer.dispose();
        }
        if (mMatchContent != null) {
            mMatchContent.removeAllViews();
        }
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MatchStatusChangeEvent event) {
        if (event != null) {
            if (event.status == MatchStatusChangeEvent.MATCH_STATUS_MATCHING) {
                mMatchContent.removeAllViews();
                if (matchingView == null) {
                    matchingView = new MatchingView(getContext());
                }
                mMatchContent.addView(matchingView);
                mMatchStatusTv.setTag(event.status);
                mMatchStatusTv.setText("取消匹配");
                mTitleBar.getCenterTextView().setText("匹配中");
                mTitleBar.getCenterSubTextView().setText("一大波skrer在来的路上啦...");
                setMatchStatus(MatchStatusChangeEvent.MATCH_STATUS_MATCHING);
            } else if (event.status == MatchStatusChangeEvent.MATCH_STATUS_MATCH_SUCESS) {
                mMatchContent.removeAllViews();
                if (matchSucessView == null) {
                    matchSucessView = new MatchSucessView(getContext());
                }
                mMatchContent.addView(matchSucessView);
                mMatchStatusTv.setTag(event.status);
                mMatchStatusTv.setText("准备");
                setMatchStatus(MatchStatusChangeEvent.MATCH_STATUS_MATCH_SUCESS);
            } else if (event.status == MatchStatusChangeEvent.MATCH_STATUS_START) {
                mMatchContent.removeAllViews();
                mMatchContent.addView(startMatchView);
                mMatchStatusTv.setTag(event.status);
                mMatchStatusTv.setText("开始匹配");
                mTitleBar.getCenterTextView().setText("匹配中");
                mTitleBar.getCenterSubTextView().setText("一大波skrer在来的路上啦...");
                setMatchStatus(MatchStatusChangeEvent.MATCH_STATUS_START);
            }


        }
    }

    private void setMatchStatus(int matchStatus) {
        mToneTuningTv.setVisibility(matchStatus == MatchStatusChangeEvent.MATCH_STATUS_START ? View.VISIBLE : View.GONE);
        mVoiceLineView.setVisibility(matchStatus == MatchStatusChangeEvent.MATCH_STATUS_START ? View.VISIBLE : View.GONE);
        mWaitTimeTv.setVisibility(matchStatus == MatchStatusChangeEvent.MATCH_STATUS_MATCHING ? View.VISIBLE : View.GONE);
        mPredictTimeTv.setVisibility(matchStatus == MatchStatusChangeEvent.MATCH_STATUS_MATCHING ? View.VISIBLE : View.GONE);
        mPrepareTipsTv.setVisibility(matchStatus == MatchStatusChangeEvent.MATCH_STATUS_MATCH_SUCESS ? View.VISIBLE : View.GONE);
    }

}
