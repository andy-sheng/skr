package com.module.rankingmode.prepare.fragment;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.widget.RelativeLayout;

import com.common.base.BaseFragment;
import com.common.core.account.UserAccountManager;
import com.common.utils.HandlerTaskTimer;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.engine.EngineEvent;
import com.engine.EngineManager;
import com.engine.Params;
import com.module.rankingmode.R;
import com.module.rankingmode.prepare.model.PrepareData;
import com.module.rankingmode.prepare.sence.controller.MatchSenceContainer;
import com.module.rankingmode.prepare.view.VoiceLineView;
import com.module.rankingmode.song.model.SongModel;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

/**
 * 匹配界面
 */
public class MatchingFragment extends BaseFragment {

    public final static String TAG = "MatchingFragment";

    private PrepareData mPrepareData = new PrepareData();

    RelativeLayout mMainActContainer;

    CommonTitleBar mTitleBar;
    MatchSenceContainer mMatchContent; // 匹配中间的容器

    VoiceLineView mVoiceLineView;

    HandlerTaskTimer mHandlerTaskTimer;

    @Override
    public int initView() {
        return R.layout.matching_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);

        mTitleBar = (CommonTitleBar) mRootView.findViewById(R.id.title_bar);
        mMatchContent = (MatchSenceContainer) mRootView.findViewById(R.id.match_content);

        mVoiceLineView = (VoiceLineView) mRootView.findViewById(R.id.voice_line_view);

        mMatchContent.setCommonTitleBar(mTitleBar);
        mMatchContent.toNextSence(mPrepareData);

        initMediaEngine();
    }

    @Override
    public void setData(int type, @Nullable Object data) {
        if(0 == type){
            mPrepareData.setSongModel((SongModel) data);
        }
    }

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
