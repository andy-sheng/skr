package com.wali.live.livesdk.live.liveshow.view.panel;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.base.log.MyLog;
import com.mi.live.engine.base.GalileoConstants;
import com.wali.live.component.view.IComponentView;
import com.wali.live.component.view.IViewProxy;
import com.wali.live.component.view.panel.BaseBottomPanel;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.liveshow.presenter.adapter.SingleChooser;
import com.wali.live.livesdk.live.liveshow.presenter.adapter.VolumeAdjuster;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.statistics.StatisticsWorker;

/**
 * Created by yangli on 2017/03/07.
 * <p>
 * Generated using create_bottom_panel.py
 *
 * @module 秀场设置面板视图
 */
public class LiveSettingPanel extends BaseBottomPanel<LinearLayout, RelativeLayout>
        implements View.OnClickListener, IComponentView<LiveSettingPanel.IPresenter, LiveSettingPanel.IView> {
    private static final String TAG = "LiveSettingPanel";

    @Nullable
    protected IPresenter mPresenter;

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
                        mPresenter.setVolume(volume);
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
        int id = v.getId();
        if (id == R.id.choose_hifi) {
            v.setSelected(!v.isSelected());
            if (mPresenter != null) {
                mPresenter.enableHifi(v.isSelected());
            }
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

    @Override
    public void setPresenter(@Nullable IPresenter iPresenter) {
        mPresenter = iPresenter;
    }

    public LiveSettingPanel(@NonNull RelativeLayout parentView) {
        super(parentView);
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

        mVolumeAdjuster.setup(mVolumeView, 50);
        mSingleChooser.setup(mReverbView, R.id.original);
    }

    @Override
    public void onOrientation(boolean isLandscape) {
        super.onOrientation(isLandscape);
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mContentView.getLayoutParams();
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        if (mIsLandscape) {
            layoutParams.width = PANEL_WIDTH_LANDSCAPE;
        } else {
            layoutParams.width = RelativeLayout.LayoutParams.MATCH_PARENT;
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

    @Override
    public IView getViewProxy() {
        /**
         * 局部内部类，用于Presenter回调通知该View改变状态
         */
        class ComponentView implements IView {
            @Nullable
            @Override
            public <T extends View> T getRealView() {
                return null;
            }
        }
        return new ComponentView();
    }

    public interface IPresenter {
        /**
         * 设置人声音量
         */
        void setVolume(int volume);

        /**
         * 设置混响
         */
        void setReverb(int reverb);

        /**
         * 开启高保真
         */
        void enableHifi(boolean enable);

        /**
         * 切换前后置摄像头
         */
        void switchCamera();

        /**
         * 当前是否为后置摄像头
         */
        boolean isBackCamera();

        /**
         * 开启镜像
         */
        void enableMirrorImage(boolean enable);

        /**
         * 开启闪光灯
         */
        void enableFlashLight(boolean enable);
    }

    public interface IView extends IViewProxy {
    }
}
