package com.module.home.setting.fragment;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.common.base.BaseFragment;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.titlebar.CommonTitleBar;
import com.component.busilib.manager.BgMusicManager;
import com.kyleduo.switchbutton.SwitchButton;
import com.module.home.R;

import static com.common.utils.SoundUtils.PREF_KEY_GAME_VOLUME_SWITCH;
import static com.component.busilib.manager.BgMusicManager.PREF_KEY_PIPEI_VOLUME;
import static com.component.busilib.manager.BgMusicManager.PREF_KEY_PIPEI_VOLUME_SWITCH;

public class VolumeFragment extends BaseFragment {

    RelativeLayout mMainActContainer;
    CommonTitleBar mTitlebar;
    RelativeLayout mPipeiArea;
    SwitchButton mPipeiSb;
    SeekBar mPipeiVoiceSeekbar;
    SwitchButton mGameSb;

    @Override
    public int initView() {
        return R.layout.volume_setting_fragment_layout;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mMainActContainer = (RelativeLayout) getRootView().findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) getRootView().findViewById(R.id.titlebar);
        mPipeiArea = (RelativeLayout) getRootView().findViewById(R.id.pipei_area);
        mPipeiSb = (SwitchButton) getRootView().findViewById(R.id.pipei_sb);
        mPipeiVoiceSeekbar = (SeekBar) getRootView().findViewById(R.id.pipei_voice_seekbar);
        mGameSb = (SwitchButton) getRootView().findViewById(R.id.game_sb);

        mPipeiSb.setChecked(U.getPreferenceUtils().getSettingBoolean(PREF_KEY_PIPEI_VOLUME_SWITCH, true));
        mGameSb.setChecked(U.getPreferenceUtils().getSettingBoolean(PREF_KEY_GAME_VOLUME_SWITCH, true));
        mPipeiVoiceSeekbar.setProgress(U.getPreferenceUtils().getSettingInt(PREF_KEY_PIPEI_VOLUME, 100));

        mPipeiSb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                U.getPreferenceUtils().setSettingBoolean(PREF_KEY_PIPEI_VOLUME_SWITCH, isChecked);
                BgMusicManager.getInstance().setPlay(isChecked);
            }
        });

        mPipeiVoiceSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int process, boolean b) {
                U.getPreferenceUtils().setSettingInt(PREF_KEY_PIPEI_VOLUME, process);
                BgMusicManager.getInstance().setMaxVolume(process / 100f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mGameSb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                U.getPreferenceUtils().setSettingBoolean(PREF_KEY_GAME_VOLUME_SWITCH, isChecked);
                U.getSoundUtils().setPlay(isChecked);
                if (isChecked) {
//                    NotificationManager mNotificationManager = (NotificationManager) U.app().getSystemService(Context.NOTIFICATION_SERVICE);
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
//                            && !mNotificationManager.isNotificationPolicyAccessGranted()) {
//                        U.getToastUtil().showShort("如果手机处于免打扰状态，可能听不到音效");
//                        return;
//                    }
                    AudioManager mAudioManager = (AudioManager) U.app().getSystemService(Context.AUDIO_SERVICE);
                    int v = mAudioManager.getStreamVolume(AudioManager.STREAM_RING);
                    int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_RING);
                    if (v < max / 2) {
                        U.getToastUtil().showShort("按键音效音量太小，可能听不到音效");
                        return;
                    }
                }
            }
        });

        mTitlebar.getLeftTextView().setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                //U.getSoundUtils().play(TAG, R.raw.normal_back, 500);
                U.getFragmentUtils().popFragment(FragmentUtils.newPopParamsBuilder()
                        .setPopFragment(VolumeFragment.this)
                        .setPopAbove(true)
                        .setHasAnimation(true)
                        .build());
            }
        });

        U.getSoundUtils().preLoad(getTAG(), R.raw.normal_back);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        U.getSoundUtils().release(getTAG());
    }
}
