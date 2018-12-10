package com.module.rankingmode.prepare.fragment;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.core.account.UserAccountManager;
import com.common.log.MyLog;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.titlebar.CommonTitleBar;
import com.engine.EngineEvent;
import com.engine.EngineManager;
import com.engine.Params;
import com.jakewharton.rxbinding2.view.RxView;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.event.MatchStatusChangeEvent;
import com.module.rankingmode.prepare.presenter.MatchPresenter;
import com.module.rankingmode.prepare.sence.controller.MatchSenceContainer;
import com.module.rankingmode.prepare.view.VoiceControlPanelView;
import com.module.rankingmode.prepare.view.VoiceLineView;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnDismissListener;
import com.orhanobut.dialogplus.ViewHolder;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.concurrent.TimeUnit;

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
    MatchSenceContainer mMatchContent; // 匹配中间的容器

    ExTextView mToneTuningTv;   //试音调音
    ExTextView mMatchStatusTv;

    VoiceLineView mVoiceLineView;

    MatchPresenter matchPresenter;

    String songName;
    String songTime;

    HandlerTaskTimer mHandlerTaskTimer;

    Handler mHandler = new Handler();

    @Override
    public int initView() {
        return R.layout.matching_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);

        mTitleBar = (CommonTitleBar) mRootView.findViewById(R.id.title_bar);
        mMatchContent = (MatchSenceContainer) mRootView.findViewById(R.id.match_content);

        mToneTuningTv = (ExTextView) mRootView.findViewById(R.id.tone_tuning_tv);
        mMatchStatusTv = (ExTextView) mRootView.findViewById(R.id.match_status_tv);

        mVoiceLineView = (VoiceLineView) mRootView.findViewById(R.id.voice_line_view);
        mMatchStatusTv.setTag(MatchStatusChangeEvent.MATCH_STATUS_START);

        matchPresenter = new MatchPresenter();

        Bundle bundle = getArguments();
        if (bundle != null) {
            songName = bundle.getString(KEY_SONG_NAME);
            songTime = bundle.getString(KEY_SONG_TIME);
        }

        mMatchContent.setOnStateChangeListener(onSenceStateChangeListener);
        mMatchContent.setCommonTitleBar(mTitleBar);

        mMatchContent.toNextSence(null);

        RxView.clicks(mMatchStatusTv)
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        if (mMatchContent.getCurrentMatchState() == MatchSenceContainer.MatchSenceState.PrepareSongRes) {
                            mMatchContent.toAssignSence(MatchSenceContainer.MatchSenceState.Matching, null);

                        } else if (mMatchContent.getCurrentMatchState() == MatchSenceContainer.MatchSenceState.Matching) {
                            mMatchContent.popSence();
                        } else if (mMatchContent.getCurrentMatchState() == MatchSenceContainer.MatchSenceState.MatchingSucess){
                            //这里就是开始准备了
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
                                .setExpanded(true, U.getDisplayUtils().dip2px(233))
                                .setContentBackgroundResource(R.drawable.voice_control_panel_bg)
                                .setContentHolder(new ViewHolder(view))
                                .setGravity(Gravity.BOTTOM)
                                .setCancelable(true)
                                .setOnDismissListener(new OnDismissListener() {
                                    @Override
                                    public void onDismiss(@NonNull DialogPlus dialog) {
                                        if(mMatchContent.getCurrentMatchState() == MatchSenceContainer.MatchSenceState.Audition){
                                            mMatchContent.popSence();
                                        }
                                    }
                                })
                                .create().show();
                        mMatchContent.toNextSence(null);

                    }
                });

        initMediaEngine();

        mMatchStatusTv.setText("加载歌曲中");
        mToneTuningTv.setText("加载歌曲中");
        mMatchStatusTv.setEnabled(false);
        mToneTuningTv.setEnabled(false);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mMatchStatusTv.setText("开始匹配");
                mToneTuningTv.setText("试唱调音");
                mMatchStatusTv.setEnabled(true);
                mToneTuningTv.setEnabled(true);
            }
        }, 3000);
    }

    private MatchSenceContainer.OnSenceStateChangeListener onSenceStateChangeListener = new MatchSenceContainer.OnSenceStateChangeListener() {
        @Override
        public void onChange(MatchSenceContainer.MatchSenceState matchSenceState) {
            MyLog.d(TAG, "matchSenceState " + matchSenceState);
            if (matchSenceState == MatchSenceContainer.MatchSenceState.PrepareSongRes) {
                mMatchStatusTv.setText("开始匹配");
                mToneTuningTv.setVisibility(View.VISIBLE);
                mMatchStatusTv.setVisibility(View.VISIBLE);
            } else if (matchSenceState == MatchSenceContainer.MatchSenceState.Audition) {
                mMatchStatusTv.setVisibility(View.GONE);
            } else if (matchSenceState == MatchSenceContainer.MatchSenceState.Matching) {
                mToneTuningTv.setVisibility(View.GONE);
                mMatchStatusTv.setText("取消匹配");
            } else if (matchSenceState == MatchSenceContainer.MatchSenceState.MatchingSucess) {
                mToneTuningTv.setVisibility(View.GONE);
                mMatchStatusTv.setText("准备");
            }
        }
    };

    // todo 后面会用我们自己的去采集声音拿到音高
    private void initMediaEngine() {
        if (!EngineManager.getInstance().isInit()) {
            // 不能每次都初始化,播放伴奏
            EngineManager.getInstance().init(Params.newBuilder(Params.CHANNEL_TYPE_COMMUNICATION)
                    .setEnableVideo(false)
                    .build());
            EngineManager.getInstance().joinRoom("" + System.currentTimeMillis(), (int) UserAccountManager.getInstance().getUuidAsLong(), true);
            EngineManager.getInstance().startAudioMixing("/assets/test.mp3", true, false, -1);
            EngineManager.getInstance().pauseAudioMixing();

            String c = U.getDateTimeUtils().formatVideoTime(0);
            String d = U.getDateTimeUtils().formatVideoTime(EngineManager.getInstance().getAudioMixingDuration());
            String info = String.format(getString(R.string.song_time_info), c, d);
            mTitleBar.getCenterSubTextView().setText(info);
        }
    }

    @Override
    protected boolean onBackPressed() {
        if (mMatchContent.interceptBackPressed()) {
            if (mMatchContent.getSenceSize() == 0) {
                return super.onBackPressed();
            }
            return true;
        }

        return super.onBackPressed();
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mHandlerTaskTimer != null) {
            mHandlerTaskTimer.dispose();
        }
        if (mMatchContent != null) {
            mMatchContent.removeAllViews();
        }
        // 退出了匹配页面，销毁引擎
        EngineManager.getInstance().destroy();
    }

    @Override
    public boolean useEventBus() {
        return true;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(MatchStatusChangeEvent event) {

    }

    private void setMatchStatus(int matchStatus) {
        mToneTuningTv.setVisibility(matchStatus == MatchStatusChangeEvent.MATCH_STATUS_START ? View.VISIBLE : View.GONE);
        mVoiceLineView.setVisibility(matchStatus == MatchStatusChangeEvent.MATCH_STATUS_START ? View.VISIBLE : View.GONE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EngineEvent engineEvent) {
        switch (engineEvent.getType()) {
            case EngineEvent.TYPE_MUSIC_PLAY_START:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    mTitleBar.getCenterSubTextView().setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.matching_prepare_titlebar_center_dot_red, 0, 0, 0);
                } else {
                    mTitleBar.getCenterSubTextView().setCompoundDrawablesWithIntrinsicBounds(R.drawable.matching_prepare_titlebar_center_dot_red, 0, 0, 0);
                }
                break;
            case EngineEvent.TYPE_MUSIC_PLAY_STOP:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    mTitleBar.getCenterSubTextView().setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.matching_prepare_titlebar_center_dot_yellow, 0, 0, 0);
                } else {
                    mTitleBar.getCenterSubTextView().setCompoundDrawablesWithIntrinsicBounds(R.drawable.matching_prepare_titlebar_center_dot_yellow, 0, 0, 0);
                }
                break;

            case EngineEvent.TYPE_MUSIC_PLAY_TIME_FLY_LISTENER:
                EngineEvent.MixMusicTimeInfo musicTimeInfo = (EngineEvent.MixMusicTimeInfo) engineEvent.getObj();
                String c = U.getDateTimeUtils().formatVideoTime(musicTimeInfo.getCurrent());
                String d = U.getDateTimeUtils().formatVideoTime(musicTimeInfo.getDuration());
                String info = String.format(getString(R.string.song_time_info), c, d);
                mTitleBar.getCenterSubTextView().setText(info);
                break;
            case EngineEvent.TYPE_USER_AUDIO_VOLUME_INDICATION:
                List<EngineEvent.UserVolumeInfo> userVolumeInfoList = (List<EngineEvent.UserVolumeInfo>) engineEvent.getObj();
                for (EngineEvent.UserVolumeInfo userVolumeInfo : userVolumeInfoList) {
                    mVoiceLineView.setVolume(userVolumeInfo.getVolume());
                }
                break;
        }
    }
}
