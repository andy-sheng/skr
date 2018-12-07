package com.module.rankingmode.fragment;

import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.ex.ExImageView;
import com.common.view.ex.ExTextView;
import com.module.rankingmode.R;
import com.module.rankingmode.event.MatchStatusChangeEvent;
import com.module.rankingmode.view.VoiceLineView;
import com.trello.rxlifecycle2.android.FragmentEvent;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 匹配界面
 */
public class MatchingFragment extends BaseFragment {

    public final static String TAG = "MatchingFragment";

    public static final String KEY_SONG_NAME = "key_song_name"; //歌曲名
    public static final String KEY_SONG_TIME = "key_song_time"; //歌曲时长

    RelativeLayout mMainActContainer;
    ExImageView mBackIv;
    ExTextView mSongNameTv;
    ExTextView mSongHintTv;
    RelativeLayout mStartMatchArea;
    ExImageView mSongImageTv;
    ExTextView mToneTuningTv;
    RelativeLayout mMatchingArea;
    RelativeLayout mMatchSucessArea;
    ExTextView mCountDownHint;
    ExTextView mMatchStatusTv;

    VoiceLineView mVoiceLineView;
    MediaRecorder mMediaRecorder;
    boolean isAlive = false;

    String songName;
    String songTime;

    private final static int UPLOAD_VOICE_VOLUME = 1;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == UPLOAD_VOICE_VOLUME){
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
        }
    };

    @Override
    public int initView() {
        return R.layout.matching_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mBackIv = (ExImageView) mRootView.findViewById(R.id.back_iv);
        mSongNameTv = (ExTextView) mRootView.findViewById(R.id.song_name_tv);
        mSongHintTv = (ExTextView) mRootView.findViewById(R.id.song_hint_tv);

        mStartMatchArea = (RelativeLayout) mRootView.findViewById(R.id.start_match_area);
        mMatchingArea = (RelativeLayout) mRootView.findViewById(R.id.matching_area);
        mMatchSucessArea = (RelativeLayout) mRootView.findViewById(R.id.match_sucess_area);

        mSongImageTv = (ExImageView) mRootView.findViewById(R.id.song_image_tv);
        mToneTuningTv = (ExTextView) mRootView.findViewById(R.id.tone_tuning_tv);

        mCountDownHint = (ExTextView) mRootView.findViewById(R.id.count_down_hint);

        mMatchStatusTv = (ExTextView) mRootView.findViewById(R.id.match_status_tv);
        mMatchStatusTv.setTag(MatchStatusChangeEvent.MATCH_STATUS_START);

        mVoiceLineView = (VoiceLineView) mRootView.findViewById(R.id.voicLine);

        isAlive = true;

        Bundle bundle = getArguments();
        if (bundle != null) {
            songName = bundle.getString(KEY_SONG_NAME);
            songTime = bundle.getString(KEY_SONG_TIME);
            mSongNameTv.setText(songName);
            mSongHintTv.setText(songTime);
        }

        mMatchStatusTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (U.getCommonUtils().isFastDoubleClick()) {
                    return;
                }

                switch ((int) mMatchStatusTv.getTag()) {
                    case MatchStatusChangeEvent.MATCH_STATUS_START:
                        break;
                    case MatchStatusChangeEvent.MATCH_STATUS_MATCHING:
                        break;
                    case MatchStatusChangeEvent.MATCH_STATUS_MATCH_SUCESS:
                        break;
                    default:
                        break;
                }
            }
        });

        mToneTuningTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (U.getCommonUtils().isFastDoubleClick()) {
                    return;
                }
                // todo 试唱调音
                U.getFragmentUtils().addFragment(FragmentUtils
                        .newParamsBuilder(getActivity(), AuditionFragment.class)
                        .setAddToBackStack(true)
                        .setHasAnimation(true)
                        .build());
            }
        });

        initMediaRecoder();
    }

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

        Observable.interval(0, 1, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .compose(this.<Long>bindUntilEvent(FragmentEvent.DESTROY))
                .subscribe(new Observer<Long>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Long aLong) {
                        if(isAlive){
                            handler.sendEmptyMessage(UPLOAD_VOICE_VOLUME);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(e);
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    @Override
    public void destroy() {
        super.destroy();
        isAlive = false;
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
        if (handler != null) {
            handler.removeMessages(UPLOAD_VOICE_VOLUME);
            handler = null;
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
                mStartMatchArea.setVisibility(View.GONE);
                mMatchSucessArea.setVisibility(View.GONE);
                mMatchingArea.setVisibility(View.VISIBLE);
                mMatchStatusTv.setTag(event.status);
                mMatchStatusTv.setText("匹配中");
            } else if (event.status == MatchStatusChangeEvent.MATCH_STATUS_MATCH_SUCESS) {
                mStartMatchArea.setVisibility(View.GONE);
                mMatchingArea.setVisibility(View.GONE);
                mMatchSucessArea.setVisibility(View.VISIBLE);
                mMatchStatusTv.setTag(event.status);
                mMatchStatusTv.setText("准备");
            } else if (event.status == MatchStatusChangeEvent.MATCH_STATUS_START) {
                mStartMatchArea.setVisibility(View.VISIBLE);
                mMatchSucessArea.setVisibility(View.GONE);
                mMatchingArea.setVisibility(View.GONE);
                mMatchStatusTv.setTag(event.status);
                mMatchStatusTv.setTag("开始匹配");
            }
        }
    }
}
