package com.wali.live.livesdk.live.liveshow.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.base.fragment.FragmentDataListener;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.permission.PermissionUtils;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.component.data.StreamerPresenter;
import com.wali.live.livesdk.live.fragment.BasePrepareLiveFragment;
import com.wali.live.livesdk.live.image.ClipImageActivity;
import com.wali.live.livesdk.live.liveshow.LiveComponentController;
import com.wali.live.livesdk.live.liveshow.data.MagicParamPresenter;
import com.wali.live.livesdk.live.liveshow.presenter.panel.LiveMagicPresenter;
import com.wali.live.livesdk.live.liveshow.view.panel.LiveMagicPanel;
import com.wali.live.livesdk.live.liveshow.view.panel.LiveSettingPanel;
import com.wali.live.livesdk.live.manager.PrepareLiveCoverManager;
import com.wali.live.livesdk.live.presenter.RoomPreparePresenter;
import com.wali.live.livesdk.live.presenter.viewmodel.TitleViewModel;
import com.wali.live.livesdk.live.view.SelectCoverView;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.statistics.StatisticsWorker;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;

/**
 * Created by yangli on 2017/3/7.
 *
 * @module 秀场直播准备页
 */
public class PrepareLiveFragment extends BasePrepareLiveFragment {
    private static final String TAG = "PrepareShowLiveFragment";
    private StreamerPresenter mStreamerPresenter;
    private LiveComponentController mLiveComponentController;
    private ImageView mTurnOverIv;
    private SelectCoverView mCoverView;
    private ImageView mSoundEffectIv;
    private ImageView mMagicIv;
    private View mBottomContainer;

    private LiveSettingPanel mLiveSettingPanel;
    private LiveMagicPanel mLiveMagicPanel;

    private final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    @Override
    public String getTAG() {
        return getClass().getSimpleName() + "#" + this.hashCode();
    }

    @Override
    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.prepare_live_layout;
    }

    @Override
    public void onClick(View v) {
        if (isFastDoubleClick() || getActivity() == null) {
            return;
        }
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.turn_over) {
            StatisticsWorker.getsInstance().sendCommand(
                    StatisticsWorker.AC_APP, StatisticsKey.KEY_PRE_LIVE_CAMERA, 1);
            mStreamerPresenter.switchCamera();
        } else if (id == R.id.sound_effect_iv) {
            showEffectPanel(true);
        } else if (id == R.id.magic_iv) {
            showMagicPanel(true);
        }
    }

    @Override
    protected void initContentView() {
        super.initContentView();
        mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyboardUtils.hideKeyboardImmediately(getActivity());
            }
        });
        mTurnOverIv = $(R.id.turn_over);
        mCoverView = $(R.id.cover_layout);
        mCoverView.setFragment(this);
        mBottomContainer = $(R.id.bottom_container);
        mSoundEffectIv = $(R.id.sound_effect_iv);
        mMagicIv = $(R.id.magic_iv);

        $click(mTurnOverIv, this);
        $click(mCoverView, this);
        $click(mSoundEffectIv, this);
        $click(mMagicIv, this);
    }

    private void showEffectPanel(boolean useAnimation) {
        super.showBottomPanel(useAnimation);
        mBottomContainer.setVisibility(View.GONE);
        mCoverView.setVisibility(View.GONE);
        if (mLiveSettingPanel == null && mStreamerPresenter != null) {
            mLiveSettingPanel = new LiveSettingPanel((RelativeLayout) mRootView, mStreamerPresenter, mLiveComponentController);
            mLiveSettingPanel.setHideCameraContainer(true);
        }
        mLiveSettingPanel.showSelf(useAnimation, false);
    }

    private void showMagicPanel(boolean useAnimation) {
        super.showBottomPanel(useAnimation);
        mBottomContainer.setVisibility(View.GONE);
        mCoverView.setVisibility(View.GONE);

        if (mLiveMagicPanel == null && mStreamerPresenter != null) {
            mLiveMagicPanel = new LiveMagicPanel((RelativeLayout) mRootView, mStreamerPresenter);
            LiveMagicPresenter presenter = new LiveMagicPresenter();
            presenter.setComponentView(mLiveMagicPanel.getViewProxy());
            mLiveMagicPanel.setPresenter(presenter);
        }
        mLiveMagicPanel.showSelf(useAnimation, false);
    }

    @Override
    public void hideBottomPanel(boolean useAnimation) {
        if (mLiveSettingPanel != null) {
            mLiveSettingPanel.hideSelf(useAnimation);
        }
        if (mLiveMagicPanel != null) {
            mLiveMagicPanel.hideSelf(useAnimation);
        }

        mCoverView.setVisibility(View.VISIBLE);
        mBottomContainer.setVisibility(View.VISIBLE);
        super.hideBottomPanel(useAnimation);
    }

    @Override
    protected void initPresenters() {
        super.initPresenters();
        mRoomPreparePresenter = new RoomPreparePresenter(this, TitleViewModel.SOURCE_NORMAL);
        new MagicParamPresenter(mLiveComponentController, getActivity());
    }

    @Override
    public boolean onBackPressed() {
        if ((mLiveMagicPanel != null && mLiveMagicPanel.isShow())
                || (mLiveSettingPanel != null && mLiveSettingPanel.isShow())) {
            hideBottomPanel(true);
            return true;
        }
        getActivity().finish();
        return true;
    }

    @Override
    protected void putCommonData(Bundle bundle) {
        super.putCommonData(bundle);
        bundle.putString(EXTRA_LIVE_COVER_URL, mCoverView.getCoverUrl());
    }

    @Override
    protected void openLive() {
        if (PermissionUtils.checkCamera(getContext())) {
            if (PermissionUtils.checkRecordAudio(getContext())) {
                if (!AccountAuthManager.triggerActionNeedAccount(getActivity())) {
                    return;
                }
                openPublicLive();
            } else {
                PermissionUtils.requestPermissionDialog(getActivity(), PermissionUtils.PermissionType.RECORD_AUDIO);
            }
        } else {
            PermissionUtils.requestPermissionDialog(getActivity(), PermissionUtils.PermissionType.CAMERA);
        }
    }

    @Override
    public void setManagerCount(int count) {
        if (count >= 0) {
            mAdminCount.setText(getString(R.string.has_add_manager_count, count));
            mAdminArea.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        MyLog.w(TAG, "onActivityResult requestCode : " + requestCode);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case PrepareLiveCoverManager.REQUEST_CODE_TAKE_PHOTO:
            case ClipImageActivity.REQUEST_CODE_CROP:
                if (mCoverView != null)
                    mCoverView.onActivityResult(requestCode, resultCode, data);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mCoverView != null) {
            mCoverView.onDestroy();
        }
    }

    @Override
    protected void updateCover() {
        if (TextUtils.isEmpty(mCoverView.getCoverUrl())) {
            mCoverView.setCoverByAvatar();
        }
    }

    public void setStreamerPresenter(StreamerPresenter streamerPresenter) {
        mStreamerPresenter = streamerPresenter;
    }

    public void setLiveComponentController(LiveComponentController liveComponentController) {
        mLiveComponentController = liveComponentController;
    }

    public static void openFragment(
            BaseComponentSdkActivity fragmentActivity,
            int requestCode,
            FragmentDataListener listener,
            LiveComponentController liveComponentController,
            StreamerPresenter streamerPresenter, RoomBaseDataModel roomBaseDataModel) {
        PrepareLiveFragment fragment = (PrepareLiveFragment) FragmentNaviUtils.addFragment(fragmentActivity, R.id.main_act_container,
                PrepareLiveFragment.class, null, true, false, true);
        fragment.setLiveComponentController(liveComponentController);
        fragment.setStreamerPresenter(streamerPresenter);
        fragment.setMyRoomData(roomBaseDataModel);
        if (listener != null) {
            fragment.initDataResult(requestCode, listener);
        }
    }
}
