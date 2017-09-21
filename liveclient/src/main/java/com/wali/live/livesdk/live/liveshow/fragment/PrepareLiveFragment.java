package com.wali.live.livesdk.live.liveshow.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.dialog.MyAlertDialog;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.permission.PermissionUtils;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.thornbirds.component.ComponentController;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.component.data.StreamerPresenter;
import com.wali.live.livesdk.live.fragment.BasePrepareLiveFragment;
import com.wali.live.livesdk.live.image.ClipImageActivity;
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

/**
 * Created by yangli on 2017/3/7.
 *
 * @module 秀场直播准备页
 */
public class PrepareLiveFragment extends BasePrepareLiveFragment {
    private static final String TAG = "PrepareShowLiveFragment";
    private StreamerPresenter mStreamerPresenter;
    private ComponentController mController;

    private int mQualityIndex = MEDIUM_CLARITY;
    private CharSequence[] mQualityArray;

    private MagicParamPresenter mMagicParamPresenter;

    private ImageView mTurnOverIv;
    private TextView mClarityTv;
    private SelectCoverView mCoverView;
    private ImageView mSoundEffectIv;
    private ImageView mMagicIv;
    private View mBottomContainer;

    private LiveSettingPanel mLiveSettingPanel;
    private LiveMagicPanel mLiveMagicPanel;

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
        } else if (id == R.id.clarity_tv) {
            showQualityDialog();
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
        mClarityTv = $(R.id.clarity_tv);
        mClarityTv.setVisibility(View.GONE);
        mCoverView = $(R.id.cover_layout);
        mCoverView.setFragment(this);
        mBottomContainer = $(R.id.bottom_container);
        mSoundEffectIv = $(R.id.sound_effect_iv);
        mMagicIv = $(R.id.magic_iv);

        $click(mTurnOverIv, this);
        $click(mClarityTv, this);
        $click(mCoverView, this);
        $click(mSoundEffectIv, this);
        $click(mMagicIv, this);
    }

    private void showQualityDialog() {
        KeyboardUtils.hideKeyboard(getActivity());
        if (mQualityArray == null) {
            CharSequence[] qualityArray = getResources().getTextArray(R.array.quality_arrays);
            mQualityArray = new CharSequence[]{qualityArray[0], qualityArray[1]};
        }
        MyAlertDialog.Builder builder = new MyAlertDialog.Builder(getContext());
        builder.setItems(mQualityArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mQualityIndex = which;
                mClarityTv.setText(mQualityArray[mQualityIndex]);
            }
        });
        builder.show();
    }

    private void showEffectPanel(boolean useAnimation) {
        super.showBottomPanel(useAnimation);
        mBottomContainer.setVisibility(View.GONE);
        mCoverView.setVisibility(View.GONE);
        if (mLiveSettingPanel == null && mStreamerPresenter != null) {
            mLiveSettingPanel = new LiveSettingPanel((RelativeLayout) mRootView, mStreamerPresenter, mController);
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
            presenter.setView(mLiveMagicPanel.getViewProxy());
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
        mMagicParamPresenter = new MagicParamPresenter(mController, getActivity());
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
    protected void performBeginClick() {
        if (!PermissionUtils.checkCamera(getContext())) {
            PermissionUtils.requestPermissionDialog(getActivity(), PermissionUtils.PermissionType.CAMERA);
            return;
        }
        if (!PermissionUtils.checkRecordAudio(getContext())) {
            PermissionUtils.requestPermissionDialog(getActivity(), PermissionUtils.PermissionType.RECORD_AUDIO);
            return;
        }
        if (!AccountAuthManager.triggerActionNeedAccount(getActivity())) {
            return;
        }
        openLive();
    }

    @Override
    protected void openLive() {
        recordShareSelectState();
        if (mDataListener != null) {
            Bundle bundle = new Bundle();
            putCommonData(bundle);
            bundle.putInt(EXTRA_LIVE_QUALITY, mQualityIndex);
            bundle.putInt(EXTRA_LIVE_TYPE, LiveManager.TYPE_LIVE_PUBLIC);
            bundle.putBoolean(EXTRA_ADD_HISTORY, mIsAddHistory);
            mDataListener.onFragmentResult(mRequestCode, Activity.RESULT_OK, bundle);
        }
        finish();
    }

    @Override
    public void setManagerCount(int count) {
        if (count >= 0) {
            mAdminCount.setText(count == 0 ?
                    GlobalData.app().getString(R.string.has_add_manager_count_zero) :
                    GlobalData.app().getString(R.string.has_add_manager_count, count));
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
    public void onDestroy() {
        super.onDestroy();
        mMagicParamPresenter.destroy();
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

    public void setComponentController(ComponentController controller) {
        mController = controller;
    }

    public static void openFragment(
            FragmentActivity fragmentActivity,
            int requestCode,
            FragmentDataListener listener,
            ComponentController controller,
            StreamerPresenter streamerPresenter,
            RoomBaseDataModel roomBaseDataModel) {
        PrepareLiveFragment fragment = (PrepareLiveFragment) FragmentNaviUtils.addFragment(fragmentActivity, R.id.main_act_container,
                PrepareLiveFragment.class, null, true, false, true);
        fragment.setComponentController(controller);
        fragment.setStreamerPresenter(streamerPresenter);
        fragment.setMyRoomData(roomBaseDataModel);
        if (listener != null) {
            fragment.initDataResult(requestCode, listener);
        }
    }
}
