package com.wali.live.livesdk.live.livegame.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.projection.MediaProjectionManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.dialog.MyAlertDialog;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.permission.PermissionUtils;
import com.base.preference.PreferenceUtils;
import com.base.utils.toast.ToastUtils;
import com.base.utils.version.VersionManager;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.event.LiveRoomManagerEvent;
import com.mi.live.data.manager.LiveRoomCharacterManager;
import com.mi.live.data.manager.model.LiveRoomManagerModel;
import com.mi.live.data.milink.event.MiLinkEvent;
import com.mi.live.data.query.model.MessageRule;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.event.UserActionEvent;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.LiveSdkActivity;
import com.wali.live.livesdk.live.api.RoomTagRequest;
import com.wali.live.livesdk.live.eventbus.LiveEventClass;
import com.wali.live.livesdk.live.fragment.BasePrepareLiveFragment;
import com.wali.live.livesdk.live.fragment.RoomAdminFragment;
import com.wali.live.livesdk.live.livegame.view.panel.GameSettingPanel;
import com.wali.live.livesdk.live.presenter.RoomPreparePresenter;
import com.wali.live.livesdk.live.presenter.viewmodel.TitleViewModel;
import com.wali.live.livesdk.live.viewmodel.RoomTag;
import com.wali.live.proto.LiveCommonProto;
import com.wali.live.watchsdk.auth.AccountAuthManager;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import static android.view.View.VISIBLE;

/**
 * Created by yangli on 2017/3/7.
 *
 * @module 游戏直播准备页
 */
public class PrepareLiveFragment extends BasePrepareLiveFragment {
    private static final String TAG = "PrepareGameLiveFragment";

    public static final String EXTRA_GAME_LIVE_QUALITY = "extra_game_live_quality";
    public static final String EXTRA_GAME_LIVE_MUTE = "extra_game_live_mute";

    public static final int LOW_CLARITY = 0;
    public static final int MEDIUM_CLARITY = 1;
    public static final int HIGH_CLARITY = 2;

    private int mQualityIndex = MEDIUM_CLARITY;
    private CharSequence[] mQualityArray;

    private TextView mClarityTv;

    private TextView mMuteTv;
    private TextView mUnMuteTv;

    private boolean mIsMute = false;

    private ViewGroup mControlTitleArea;
    private TextView mChangeTitleTv;
    private TextView mClearTitleTv;

    private ViewGroup mBlockArea;
    private GameSettingPanel mGameSettingPanel;

    private ViewGroup mAdminArea;
    private TextView mAdminCount;

    private View mDailyTaskSl;
    private ViewGroup mDailyTaskArea;
    private LiveCommonProto.NewWidgetUnit mWidgetUnit;

    private ViewGroup mTopContainer;
    private ViewGroup mTitleContainer;
    private ViewGroup mMiddleContainer;

    private RoomPreparePresenter mRoomPreparePresenter;

    @Override
    protected String getTAG() {
        return getClass().getSimpleName() + "#" + this.hashCode();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.prepare_game_live_layout;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void bindView() {
        super.bindView();
        initOtherViews();

        asyncProcess();
    }

    @Override
    protected void initPresenters() {
        super.initPresenters();
        mRoomPreparePresenter = new RoomPreparePresenter(this, TitleViewModel.SOURCE_GAME);
    }

    private void asyncProcess() {
        mRoomPreparePresenter.loadManager();
        mRoomPreparePresenter.loadTitle();
        mRoomPreparePresenter.loadDailyTask();
    }

    private void initOtherViews() {
        if (mQualityArray == null) {
            mQualityArray = getResources().getTextArray(R.array.quality_arrays);
        }
        mQualityIndex = PreferenceUtils.getSettingInt(GlobalData.app(), PreferenceUtils.PREF_KEY_LIVE_GAME_CLARITY, MEDIUM_CLARITY);

        mClarityTv = $(R.id.clarity_tv);
        mClarityTv.setText(mQualityArray[mQualityIndex]);
        mClarityTv.setOnClickListener(this);

        mBlockArea = $(R.id.block_area);
        mBlockArea.setOnClickListener(this);

        mAdminArea = $(R.id.admin_area);
        mAdminCount = $(R.id.admin_count);
        mAdminArea.setOnClickListener(this);

        mDailyTaskSl = $(R.id.daily_task_sl);

        mDailyTaskArea = $(R.id.daily_task_area);
        mDailyTaskArea.setOnClickListener(this);

        mMuteTv = $(R.id.mute_yes_tv);
        mMuteTv.setOnClickListener(this);
        mUnMuteTv = $(R.id.mute_no_tv);
        mUnMuteTv.setOnClickListener(this);

        mControlTitleArea = $(R.id.control_title_area);

        mChangeTitleTv = $(R.id.change_title_tv);
        mChangeTitleTv.setOnClickListener(this);

        mClearTitleTv = $(R.id.clear_title_tv);
        mClearTitleTv.setOnClickListener(this);

        mTopContainer = $(R.id.top_container);
        mTitleContainer = $(R.id.title_container);
        mMiddleContainer = $(R.id.middle_container);

        tryInitSettingPanel();
    }

    public void setRoomChatMsgManager(@NonNull LiveRoomChatMsgManager roomChatMsgManager) {
        super.setRoomChatMsgManager(roomChatMsgManager);
        tryInitSettingPanel();
    }

    private void tryInitSettingPanel() {
        if (mGameSettingPanel == null && mRootView != null && mRoomChatMsgManager != null) {
            mGameSettingPanel = new GameSettingPanel((RelativeLayout) mRootView, mRoomChatMsgManager);
        }
    }

    @Override
    protected void onBeginBtnClick() {
        PermissionUtils.requestPermissionDialog(getActivity(), PermissionUtils.PermissionType.READ_PHONE_STATE, new PermissionUtils.IPermissionCallback() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void okProcess() {
                if (!AccountAuthManager.triggerActionNeedAccount(getActivity())) {
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
                                    Uri.parse("package:" + getActivity().getPackageName()));
                            startActivityForResult(intent, 10);
                        } else {
                            PermissionUtils.requestPermissionDialog(getActivity(), PermissionUtils.PermissionType.SYSTEM_ALERT_WINDOW);
                        }
                    }
                } else {
                    ToastUtils.showToast(R.string.third_party_system_version_error);
                }
            }
        });
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
        }
        PreferenceUtils.setSettingInt(GlobalData.app(), PreferenceUtils.PREF_KEY_LIVE_GAME_CLARITY, mQualityIndex);
        openGameLive();
    }

    @Override
    public void onClick(View v) {
        if (isFastDoubleClick() || getActivity() == null) {
            return;
        }
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.clarity_tv) {
            showQualityDialog();
        } else if (id == R.id.block_area) {
            showSettingPanel(true);
        } else if (id == R.id.daily_task_area) {
            showDailyTask();
        } else if (id == R.id.mute_no_tv) {
            updateMuteStatus(false);
        } else if (id == R.id.mute_yes_tv) {
            updateMuteStatus(true);
        } else if (id == R.id.admin_area) {
            openAdminFragment();
        } else if (id == R.id.change_title_tv) {
            changeTitle();
        } else if (id == R.id.clear_title_tv) {
            clearTitle();
        } else if (id == R.id.main_fragment_container) {
            hideSettingPanel(true);
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
                mClarityTv.setText(mQualityArray[mQualityIndex]);
            }
        });
        builder.show();
    }

    private void showSettingPanel(boolean useAnimation) {
        mTopContainer.setVisibility(View.GONE);
        mTitleContainer.setVisibility(View.GONE);
        mMiddleContainer.setVisibility(View.GONE);
        mBeginBtn.setVisibility(View.GONE);
        if (mMyRoomData.getEnableShare()) {
            mShareContainer.setVisibility(View.GONE);
        }
        mGameSettingPanel.showSelf(useAnimation, false);
        mRootView.setOnClickListener(this);
    }

    private void hideSettingPanel(boolean useAnimation) {
        mTopContainer.setVisibility(VISIBLE);
        mTitleContainer.setVisibility(VISIBLE);
        mMiddleContainer.setVisibility(VISIBLE);
        mBeginBtn.setVisibility(VISIBLE);
        if (mMyRoomData.getEnableShare()) {
            mShareContainer.setVisibility(View.VISIBLE);
        }
        mGameSettingPanel.hideSelf(useAnimation);
        mRootView.setOnClickListener(null);
    }

    private void updateMuteStatus(boolean isMute) {
        if (mIsMute != isMute) {
            mIsMute = isMute;
            if (mIsMute) {
                mUnMuteTv.setTextColor(getResources().getColor(R.color.color_white_trans_40));
                mMuteTv.setTextColor(getResources().getColor(R.color.color_white_trans_90));
            } else {
                mUnMuteTv.setTextColor(getResources().getColor(R.color.color_white_trans_90));
                mMuteTv.setTextColor(getResources().getColor(R.color.color_white_trans_40));
            }
        }
    }

    private void showDailyTask() {
        UserActionEvent.post(UserActionEvent.EVENT_TYPE_CLICK_ATTACHMENT,
                mWidgetUnit.getLinkUrl(), mWidgetUnit.getUrlNeedParam(), mWidgetUnit.getOpenType(), UserAccountManager.getInstance().getUuidAsLong());
    }

    private void openAdminFragment() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(RoomAdminFragment.KEY_ROOM_SEND_MSG_CONFIG, new MessageRule());
        bundle.putLong(RoomAdminFragment.KEY_ROOM_ANCHOR_ID, UserAccountManager.getInstance().getUuidAsLong());
        bundle.putBoolean(RoomAdminFragment.KEY_ONLY_SHOW_ADMIN_MANAGER_PAGE, true);
        FragmentNaviUtils.addFragment(getActivity(), R.id.main_act_container, RoomAdminFragment.class, bundle, true, true, true);
    }

    @Override
    public void fillTitle(String title) {
        mLiveTitleEt.setText(title);
    }

    @Override
    public void updateControlTitleArea(boolean isShow) {
        if (isShow) {
            if (mControlTitleArea.getVisibility() != VISIBLE) {
                mControlTitleArea.setVisibility(VISIBLE);
            }
        } else {
            if (mControlTitleArea.getVisibility() == VISIBLE) {
                mControlTitleArea.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void setDailyTaskUnit(final LiveCommonProto.NewWidgetUnit unit) {
        if (unit != null && unit.hasLinkUrl()) {
            mDailyTaskSl.setVisibility(VISIBLE);
            mDailyTaskArea.setVisibility(VISIBLE);
            mWidgetUnit = unit;
        } else {
            mDailyTaskSl.setVisibility(View.GONE);
            mDailyTaskArea.setVisibility(View.GONE);
        }
    }

    private void changeTitle() {
        mRoomPreparePresenter.changeTitle();
    }

    private void clearTitle() {
        mLiveTitleEt.setText("");
    }

    protected void openGameLive() {
        if (mDataListener != null) {
            Bundle bundle = new Bundle();
            putCommonData(bundle);
            bundle.putInt(EXTRA_GAME_LIVE_QUALITY, mQualityIndex);
            bundle.putInt(EXTRA_LIVE_TYPE, LiveManager.TYPE_LIVE_GAME);
            bundle.putBoolean(EXTRA_GAME_LIVE_MUTE, mIsMute);
            mDataListener.onFragmentResult(mRequestCode, Activity.RESULT_OK, bundle);
        }
        finish();
    }

    @Override
    protected void adjustTitleEtPosByCover(boolean isTitleEtFocus, int coverState) {
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
            MyLog.w(TAG, "HidePrepareGameLiveEvent");
            super.onBeginBtnClick();
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void onEvent(MiLinkEvent.StatusLogined event) {
        if (event != null) {
            asyncProcess();
        }
    }

    @Override
    public boolean onBackPressed() {
        if (mGameSettingPanel.isShow()) {
            hideSettingPanel(true);
            return true;
        }
        getActivity().finish();
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void hideTag() {
        // 游戏直播,不需要hideTag
    }

    public static void openFragment(
            BaseComponentSdkActivity activity, int requestCode, FragmentDataListener listener,
            RoomBaseDataModel roomBaseDataModel, LiveRoomChatMsgManager roomChatMsgManager) {
        PrepareLiveFragment fragment = (PrepareLiveFragment) FragmentNaviUtils.addFragment(activity, R.id.main_act_container,
                PrepareLiveFragment.class, null, true, false, true);
        fragment.setMyRoomData(roomBaseDataModel);
        fragment.setRoomChatMsgManager(roomChatMsgManager);
        if (listener != null) {
            fragment.initDataResult(requestCode, listener);
        }
    }

    @Override
    public void setManagerCount(int count) {
        mAdminCount.setText(getString(R.string.has_add_manager_count, count));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LiveRoomManagerEvent event) {
        List<LiveRoomManagerModel> managerModels = LiveRoomCharacterManager.getInstance().getRoomManagers();
        int managerCount = managerModels.size();
        long top1Id = mRoomPreparePresenter.getTop1Id();
        boolean isTop1Manager = false;
        for (LiveRoomManagerModel managerModel : managerModels) {
            if (top1Id == managerModel.uuid) {
                isTop1Manager = true;
                break;
            }
        }
        if (!isTop1Manager) {
            managerCount++;
        }
        setManagerCount(managerCount);
    }
}
