package com.wali.live.livesdk.live.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.base.dialog.MyAlertDialog;
import com.base.fragment.BaseFragment;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.permission.PermissionUtils;
import com.base.preference.PreferenceUtils;
import com.base.utils.CommonUtils;
import com.base.utils.toast.ToastUtils;
import com.base.utils.version.VersionManager;
import com.mi.live.data.api.LiveManager;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.LiveSdkActivity;
import com.wali.live.livesdk.live.api.RoomTagRequest;
import com.wali.live.livesdk.live.eventbus.LiveEventClass;
import com.wali.live.livesdk.live.viewmodel.RoomTag;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * Created by yangli on 16-11-28.
 *
 * @module 游戏直播准备页
 */
public class PrepareGameLiveFragment extends BasePrepareLiveFragment {
    public static final String EXTRA_GAME_LIVE_QUALITY = "extra_game_live_quality";

    public static final int LOW_CLARITY = 0;
    public static final int MEDIUM_CLARITY = 1;
    public static final int HIGH_CLARITY = 2;

    private ViewGroup mGameClarityContainer;
    private TextView mGameClarityTv;
    private int mQualityIndex = MEDIUM_CLARITY;
    private CharSequence[] mQualityArray;
    private ViewGroup mQualityCnCotainer;
    private ViewGroup mQualityEnConatainer;
    private TextView[] mQualityBtns;
    private TextView mQualityTv;

    @Override
    protected String getTAG() {
        return getClass().getSimpleName() + "#" + this.hashCode();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int i = v.getId();
        if (i == R.id.game_clarity_container) {
            showQualityDialog();
        } else if (i == R.id.standard_definition || i == R.id.high_definition || i == R.id.super_definition) {
            selectQuality(v);
        } else {
        }
    }

    private void selectQuality(View v) {
        int index = (int) v.getTag();
        if (mQualityIndex != index) {
            mQualityBtns[mQualityIndex].setSelected(false);
            mQualityIndex = index;
            mQualityBtns[mQualityIndex].setSelected(true);
        }
    }

    private void showQualityDialog() {
        KeyboardUtils.hideKeyboard(getActivity());
        if (mQualityArray == null) {
            mQualityArray = getResources().getTextArray(R.array.quality_arrays);
        }
        MyAlertDialog.Builder builder = new MyAlertDialog.Builder(getContext());
        builder.setItems(mQualityArray, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mQualityIndex = which;
                mGameClarityTv.setText(mQualityArray[mQualityIndex]);
            }
        });
        builder.show();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.prepare_game_live_layout;
    }

    @Override
    protected void bindView() {
        super.bindView();
        initOtherViews();
    }

    private void initOtherViews() {
        mQualityCnCotainer = $(R.id.quality_cn_container);
        mQualityEnConatainer = $(R.id.quality_en_container);
        if (mQualityArray == null) {
            mQualityArray = getResources().getTextArray(R.array.quality_arrays);
        }
        mQualityIndex = PreferenceUtils.getSettingInt(GlobalData.app(), PreferenceUtils.PREF_KEY_LIVE_GAME_CLARITY, MEDIUM_CLARITY);
        if (!CommonUtils.isLocalChina()) {
            mQualityEnConatainer.setVisibility(View.VISIBLE);
            mQualityCnCotainer.setVisibility(View.GONE);
            // 游戏画质view
            mQualityTv = $(R.id.quality_tv);
            mQualityTv.setOnClickListener(null);
            mQualityTv.setText(getString(R.string.game_quality_title_en));

            mQualityBtns = new TextView[3];
            mQualityBtns[0] = $(R.id.standard_definition);
            mQualityBtns[0].setTag(LOW_CLARITY);
            mQualityBtns[0].setText(mQualityArray[0]);
            mQualityBtns[0].setOnClickListener(this);

            mQualityBtns[1] = $(R.id.high_definition);
            mQualityBtns[1].setTag(MEDIUM_CLARITY);
            mQualityBtns[1].setText(mQualityArray[1]);
            mQualityBtns[1].setOnClickListener(this);

            mQualityBtns[2] = $(R.id.super_definition);
            mQualityBtns[2].setTag(HIGH_CLARITY);
            mQualityBtns[2].setText(mQualityArray[2]);
            mQualityBtns[2].setOnClickListener(this);

            mQualityBtns[mQualityIndex].setSelected(true);
        } else {
            mQualityCnCotainer.setVisibility(View.VISIBLE);
            mQualityEnConatainer.setVisibility(View.GONE);
            mGameClarityContainer = $(R.id.game_clarity_container);
            mGameClarityContainer.setOnClickListener(this);
            mGameClarityTv = $(R.id.game_clarity_tv);
            mGameClarityTv.setText(mQualityArray[mQualityIndex]);
            mGameClarityTv.setSelected(true);
        }

    }

    @Override
    protected void onBeginBtnClick() {
        if (!AccountAuthManager.triggerActionNeedAccount(getActivity())) {
            return;
        }
        if (CommonUtils.isLocalChina() && mRoomTag == null) {
            ToastUtils.showToast(R.string.game_choose_tag_tip);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (PermissionUtils.checkSystemAlertWindow(getContext())) {
                if (PermissionUtils.checkRecordAudio(getContext())) {
                    getActivity().startActivityForResult(
                            ((MediaProjectionManager) GlobalData.app()
                                    .getSystemService(Context.MEDIA_PROJECTION_SERVICE)).createScreenCaptureIntent(),
                            LiveSdkActivity.REQUEST_MEDIA_PROJECTION);
                } else {
                    PermissionUtils.requestPermissionDialog(getActivity(), PermissionUtils.PermissionType.RECORD_AUDIO);
                }
            } else {
                if (VersionManager.getCurrentSdkVersion() >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getActivity())) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + "com.mi.liveassistant"));
                    startActivityForResult(intent, 10);
                } else {
                    PermissionUtils.requestPermissionDialog(getActivity(), PermissionUtils.PermissionType.SYSTEM_ALERT_WINDOW);
                }
            }
        } else {
            ToastUtils.showToast(R.string.third_party_system_version_error);
        }
    }

    @Override
    protected void initTagName() {
        String jsonString = PreferenceUtils.getSettingString(GlobalData.app(), PreferenceUtils.PREF_KEY_LIVE_GAME_TAG, "");
        if (!TextUtils.isEmpty(jsonString)) {
            try {
                mRoomTag = new RoomTag(jsonString);
            } catch (Exception e) {
            }
            if (mRoomTag != null) {
                mTagNameTv.setText(mRoomTag.getTagName());
                mTagNameTv.setSelected(true);
            }
        }
        prepareTagFromServer();
    }

    @Override
    protected void openLive() {
        if (mRoomTag != null) {
            PreferenceUtils.setSettingString(GlobalData.app(), PreferenceUtils.PREF_KEY_LIVE_GAME_TAG, mRoomTag.toJsonString());
            PreferenceUtils.setSettingInt(GlobalData.app(), PreferenceUtils.PREF_KEY_LIVE_GAME_CLARITY, mQualityIndex);
        }
        openGameLive();
    }

    protected void openGameLive() {
        if (mDataListener != null) {
            Bundle bundle = new Bundle();
            putCommonData(bundle);
            bundle.putInt(EXTRA_GAME_LIVE_QUALITY, mQualityIndex);
            bundle.putInt(EXTRA_LIVE_TYPE, LiveManager.TYPE_LIVE_GAME);
            mDataListener.onFragmentResult(mRequestCode, Activity.RESULT_OK, bundle);
        }
        finish();
    }

    @Override
    protected void adjustTitleEtPosByCover(boolean isTitleEtFocus, int coverState) {
        if (!isTitleEtFocus) {
            return;
        }
        mLiveTitleEt.setHint("");
    }

    @Override
    protected void getTagFromServer() {
        KeyboardUtils.hideKeyboard(getActivity());
        mRoomTagPresenter.start(RoomTagRequest.TAG_TYPE_GAME);
    }

    protected void prepareTagFromServer() {
        mRoomTagPresenter.prepare(RoomTagRequest.TAG_TYPE_GAME);
    }

    @Override
    protected void updateTagName() {
        if (mRoomTag != null) {
            mTagNameTv.setText(mRoomTag.getTagName());
            mTagNameTv.setSelected(true);
            if (!TextUtils.isEmpty(mRoomTag.getIconUrl())) {
                EventBus.getDefault().post(new LiveEventClass.LiveCoverEvent(mRoomTag.getIconUrl()));
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(LiveEventClass.HidePrepareGameLiveEvent event) {
        if (event != null) {
            MyLog.d(TAG, "HidePrepareGameLiveEvent");
            super.onBeginBtnClick();
        }
    }

    public static void openFragment(BaseComponentSdkActivity activity, int requestCode, FragmentDataListener listener) {
        BaseFragment fragment = FragmentNaviUtils.addFragment(activity, R.id.main_act_container,
                PrepareGameLiveFragment.class, null, true, false, true);
        if (listener != null) {
            fragment.initDataResult(requestCode, listener);
        }
    }

    @Override
    public boolean onBackPressed() {
        super.onBackPressed();
        getActivity().finish();
        return true;
    }

    @Override
    public void hideTag() {
        //游戏直播,不需要hideTag
    }
}
