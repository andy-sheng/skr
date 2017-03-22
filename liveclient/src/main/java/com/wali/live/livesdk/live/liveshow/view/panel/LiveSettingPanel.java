package com.wali.live.livesdk.live.liveshow.view.panel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.mi.live.engine.base.GalileoConstants;
import com.wali.live.component.ComponentController;
import com.wali.live.component.presenter.ComponentPresenter.IComponentController;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.component.data.StreamerPresenter;
import com.wali.live.livesdk.live.liveshow.presenter.adapter.SingleChooser;
import com.wali.live.livesdk.live.liveshow.presenter.adapter.VolumeAdjuster;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.statistics.StatisticsWorker;

/**
 * Created by yangli on 2017/03/07.
 *
 * @module 秀场设置面板视图
 */
public class LiveSettingPanel extends BaseBottomPanel<LinearLayout, RelativeLayout>
        implements View.OnClickListener {
    private static final String TAG = "LiveSettingPanel";

    @Nullable
    protected StreamerPresenter mPresenter;
    @NonNull
    protected IComponentController mComponentController;

    private ViewGroup mVolumeView;
    private ViewGroup mReverbView;

    private View mChooseHifi;
    private View mMirrorImageBtn;
    private View mFlashLightBtn;

    private final VolumeAdjuster mVolumeAdjuster = new VolumeAdjuster(
            new VolumeAdjuster.IAdjusterListener() {
                @Override
                public void onMinimizeBtn(boolean isSelected) {
                    StatisticsWorker.getsInstance().sendCommand(
                            StatisticsWorker.AC_APP, StatisticsKey.KEY_LIVING_VOICE_MUTE, 1);
                }

                @Override
                public void onMaximizeBtn() {
                    StatisticsWorker.getsInstance().sendCommand(
                            StatisticsWorker.AC_APP, StatisticsKey.KEY_LIVING_VOICE_HIGHEST, 1);
                }

                @Override
                public void onChangeVolume(int volume) {
                    // 人声调节打点
                    StatisticsWorker.getsInstance().sendCommand(
                            StatisticsWorker.AC_APP, StatisticsKey.KEY_LIVING_VOICE_ADJUST, 1);
                    if (mPresenter != null) {
                        mPresenter.setVoiceVolume(volume);
                    }
                }
            });

    private final SingleChooser mSingleChooser = new SingleChooser(
            new SingleChooser.IChooserListener() {
                @Override
                public void onItemSelected(View view) {
                    if (mPresenter == null) {
                        return;
                    }
                    int i = view.getId();
                    if (i == R.id.original) {
                        mPresenter.setReverb(GalileoConstants.TYPE_ORIGINAL);
                        StatisticsWorker.getsInstance().sendCommand(
                                StatisticsWorker.AC_APP, StatisticsKey.KEY_LIVING_REVERBERATION_ORIGIN, 1);
                    } else if (i == R.id.recording_studio) {
                        mPresenter.setReverb(GalileoConstants.TYPE_RECORDING_STUDIO);
                        StatisticsWorker.getsInstance().sendCommand(
                                StatisticsWorker.AC_APP, StatisticsKey.KEY_LIVING_REVERBERATION_RECORD_STUDIO, 1);
                    } else if (i == R.id.ktv) {
                        mPresenter.setReverb(GalileoConstants.TYPE_KTV);
                        StatisticsWorker.getsInstance().sendCommand(
                                StatisticsWorker.AC_APP, StatisticsKey.KEY_LIVING_REVERBERATION_KTV, 1);
                    } else if (i == R.id.concert) {
                        mPresenter.setReverb(GalileoConstants.TYPE_CONCERT);
                        StatisticsWorker.getsInstance().sendCommand(
                                StatisticsWorker.AC_APP, StatisticsKey.KEY_LIVING_REVERBERATION_CONCERT, 1);
                    }
                }
            });

    protected final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public void onClick(View v) {
        if (mPresenter == null) {
            return;
        }
        int id = v.getId();
        if (id == R.id.choose_hifi) {
            v.setSelected(!v.isSelected());
            mPresenter.enableHifi(v.isSelected());
        } else if (id == R.id.switch_camera) {
            updateSwitchCamera();
        } else if (id == R.id.mirror_image) {
            updateMirrorImage(!v.isSelected());
        } else if (id == R.id.flash_light) {
            updateFlashLight(!v.isSelected());
        }
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.setting_control_panel;
    }

    public LiveSettingPanel(
            @NonNull RelativeLayout parentView,
            @Nullable StreamerPresenter presenter, IComponentController componentController) {
        super(parentView);
        mPresenter = presenter;
        mComponentController = componentController;
    }

    @Override
    protected void inflateContentView() {
        super.inflateContentView();

        mVolumeView = $(R.id.volume_view);
        mReverbView = $(R.id.reverb_view);

        mChooseHifi = $(R.id.choose_hifi);
        mMirrorImageBtn = $(R.id.mirror_image);
        mFlashLightBtn = $(R.id.flash_light);

        $click(mChooseHifi, this);
        $click(mMirrorImageBtn, this);
        $click(mFlashLightBtn, this);
        $click(R.id.switch_camera, this);

        mContentView.setSoundEffectsEnabled(false);
        $click(mContentView, this);

        mVolumeAdjuster.setup(mVolumeView, 50);
        mSingleChooser.setup(mReverbView, R.id.original);

        if (mPresenter != null) {
            mMirrorImageBtn.setSelected(mPresenter.isMirrorImage());
            mFlashLightBtn.setSelected(mPresenter.isFlashLight());
            mFlashLightBtn.setEnabled(mPresenter.isBackCamera());
            mChooseHifi.setSelected(mPresenter.isHifi());
            mVolumeAdjuster.setVolume(mPresenter.getVoiceVolume());
            switch (mPresenter.getReverb()) {
                case GalileoConstants.TYPE_RECORDING_STUDIO:
                    mSingleChooser.setSelection(R.id.recording_studio);
                    break;
                case GalileoConstants.TYPE_KTV:
                    mSingleChooser.setSelection(R.id.ktv);
                    break;
                case GalileoConstants.TYPE_CONCERT:
                    mSingleChooser.setSelection(R.id.concert);
                    break;
                case GalileoConstants.TYPE_ORIGINAL: // fall through
                default:
                    mSingleChooser.setSelection(R.id.original);
                    break;
            }
        }
    }

    private void updateSwitchCamera() {
        MyLog.w(TAG, "updateSwitchCamera");
        StatisticsWorker.getsInstance().sendCommand(
                StatisticsWorker.AC_APP, StatisticsKey.KEY_LIVING_CAMERA, 1);
        mPresenter.switchCamera();
        boolean isBackCamera = mPresenter.isBackCamera();
        mFlashLightBtn.setEnabled(isBackCamera);
        if (isBackCamera) {
            updateFlashLight(mFlashLightBtn.isSelected());
        }
        mComponentController.onEvent(ComponentController.MSG_HIDE_BOTTOM_PANEL);
    }

    private void updateMirrorImage(boolean enable) {
        MyLog.w(TAG, "updateMirrorImage " + enable);
        StatisticsWorker.getsInstance().sendCommand(
                StatisticsWorker.AC_APP, StatisticsKey.KEY_LIVING_SELF_MIRROR, 1);
        mMirrorImageBtn.setSelected(enable);
        mPresenter.enableMirrorImage(enable);
    }

    public void updateFlashLight(boolean enable) {
        MyLog.w(TAG, "updateFlashLight " + enable);
        StatisticsWorker.getsInstance().sendCommand(
                StatisticsWorker.AC_APP, StatisticsKey.KEY_LIVING_PHOTO_FLASH, 1);
        mFlashLightBtn.setSelected(enable);
        mPresenter.enableFlashLight(enable);
    }
}
