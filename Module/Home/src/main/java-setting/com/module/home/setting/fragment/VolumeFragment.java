package com.module.home.setting.fragment;

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.common.base.BaseFragment;
import com.common.log.MyLog;
import com.common.utils.FragmentUtils;
import com.common.utils.U;
import com.common.view.titlebar.CommonTitleBar;
import com.component.busilib.manager.BgMusicManager;
import com.jakewharton.rxbinding2.view.RxView;
import com.kyleduo.switchbutton.SwitchButton;
import com.module.home.R;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;

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
        mMainActContainer = (RelativeLayout) mRootView.findViewById(R.id.main_act_container);
        mTitlebar = (CommonTitleBar) mRootView.findViewById(R.id.titlebar);
        mPipeiArea = (RelativeLayout) mRootView.findViewById(R.id.pipei_area);
        mPipeiSb = (SwitchButton) mRootView.findViewById(R.id.pipei_sb);
        mPipeiVoiceSeekbar = (SeekBar) mRootView.findViewById(R.id.pipei_voice_seekbar);
        mGameSb = (SwitchButton) mRootView.findViewById(R.id.game_sb);

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
                    NotificationManager mNotificationManager = (NotificationManager) U.app().getSystemService(Context.NOTIFICATION_SERVICE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                            && !mNotificationManager.isNotificationPolicyAccessGranted()) {
                        U.getToastUtil().showShort("手机处于免打扰状态，可能听不到音效");
                        return;
                    }
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

        RxView.clicks(mTitlebar.getLeftTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        //U.getSoundUtils().play(TAG, R.raw.normal_back, 500);
                        U.getFragmentUtils().popFragment(FragmentUtils.newPopParamsBuilder()
                                .setPopFragment(VolumeFragment.this)
                                .setPopAbove(true)
                                .setHasAnimation(true)
                                .build());
                    }
                });

        U.getSoundUtils().preLoad(TAG, R.raw.normal_back);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }

    @Override
    public void destroy() {
        super.destroy();
        U.getSoundUtils().release(TAG);
    }
}
