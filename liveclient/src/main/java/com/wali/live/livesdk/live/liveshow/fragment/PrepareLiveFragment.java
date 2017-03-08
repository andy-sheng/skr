package com.wali.live.livesdk.live.liveshow.fragment;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.dialog.MyAlertDialog;
import com.base.fragment.BaseFragment;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.CommonUtils;
import com.base.utils.language.LocaleUtil;
import com.wali.live.common.action.VideoAction;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.api.RoomTagRequest;
import com.wali.live.livesdk.live.fragment.BasePrepareLiveFragment;
import com.wali.live.livesdk.live.liveshow.data.MagicParamPresenter;
import com.wali.live.livesdk.live.viewmodel.RoomTag;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.statistics.StatisticsWorker;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;

/**
 * Created by yangli on 2017/3/7.
 *
 * @module 秀场直播准备页
 */
public class PrepareLiveFragment extends BasePrepareLiveFragment {
    private static final String TAG = "PrepareShowLiveFragment";

    // 口令直播间的密码
    public static final String EXTRA_LIVE_PASSWORD = "extra_live_password";
    public static final String EXTRA_LIVE_TICKET_ID = "extra_live_ticket_id";
    public static final String EXTRA_LIVE_TICKET_PRICE = "extra_live_ticket_price";
    public static final String EXTRA_LIVE_GLANCE_ENABLE = "extra_live_glance_enable";

    public static final int REQUEST_RECIPIENT_SELECT = 1000;

    private MyAlertDialog.Builder builder;

    private RelativeLayout mSelectLayout;
    private TextView mSelect;
    private RelativeLayout mAddTopicContainer;
    private ImageView mTurnOverIv;


    @Override
    public String getTAG() {
        return getClass().getSimpleName() + "#" + this.hashCode();
    }

    public void hideCloseBtn() {
        mCloseBtn.setVisibility(View.INVISIBLE);
    }

    @Override
    @LayoutRes
    protected int getLayoutResId() {
        return R.layout.prepare_live_layout;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void initData() {
    }

    @Override
    protected void bindView() {
        super.bindView();
        initData();
    }

    @Override
    protected void initContentView() {
        super.initContentView();

        mTurnOverIv = (ImageView) mRootView.findViewById(R.id.turn_over);
        mTurnOverIv.setTag(VideoAction.ACTION_VIDEO_CAMERA_SWITCH);
        mTurnOverIv.setOnClickListener(this);

        mAddTopicContainer = (RelativeLayout) mRootView.findViewById(R.id.add_topic_container);
        mAddTopicContainer.setTag(VideoAction.ACTION_PREPARE_ADD_TOPIC);
        mAddTopicContainer.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        int action = 0;
        try {
            if (v.getTag() != null) {
                action = Integer.valueOf(String.valueOf(v.getTag()));
            }
        } catch (NumberFormatException e) {
            MyLog.e(TAG, e);
            return;
        }

        if (CommonUtils.isFastDoubleClick()) {
            return;
        }

        switch (action) {
            case VideoAction.ACTION_PREPARE_ADD_TOPIC:
                /*
                * 将新的Fragment(TopicRecommendFragment)加(add)到栈中,并隐藏PrepareLiveFragment
                * */
//                TopicRecommendFragment.openFragment((BaseAppActivity) getActivity(), new TopicRecommendFragment.ITopicDataChangeListener() {
//                    @Override
//                    public void onTopicDataChanged(String topic) {
//                        MyLog.d(TAG, "onTopicDataChanged " + topic);
//                        if (!TextUtils.isEmpty(topic)) {
//                            mTitleTextWatcher.setTopicDefaultWay(TOPIC_FROM_TMATY);
//                            mLiveTitleEt.setText(topic);
//                        } else {
//                            if (TextUtils.isEmpty(mLiveTitleEt.getText())) {
//                                mLiveTitleEt.setHint(R.string.prepare_live_edittext_hint);
//                            }
//                        }
//                    }
//
//                    @Override
//                    public void onTopicFragmentFinished() {
//                        MyLog.d(TAG, "onTopicFragmentFinished");
//                        mRootView.setVisibility(View.VISIBLE);
//                        mAddTopicContainer.setClickable(false);
//                    }
//
//                    @Override
//                    public void onTopicFragmentDestoryed() {
//                        mAddTopicContainer.setClickable(true);
//                    }
//                });
                KeyboardUtils.hideKeyboard(getActivity());
                mRootView.setVisibility(View.GONE);
                break;
            case VideoAction.ACTION_VIDEO_CAMERA_SWITCH:
                StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP, StatisticsKey.KEY_PRE_LIVE_CAMERA, 1);
//                EventController.onActionCamera(EventClass.CameraEvent.EVENT_TYPE_SWITCH);
                break;
            default:
                break;
        }
    }

    @Override
    protected void getTagFromServer() {
        mBeginBtn.setEnabled(true);
        mRoomTagPresenter.start(RoomTagRequest.TAG_TYPE_NORMAL);
    }

    protected void prepareTagFromServer() {
        mRoomTagPresenter.prepare(RoomTagRequest.TAG_TYPE_NORMAL);
    }

    @Override
    public boolean onBackPressed() {
        super.onBackPressed();
        getActivity().finish();
        return true;
    }

    @Override
    protected void adjustTitleEtPosByCover(boolean isTitleEtFocus, int coverState) {
        if (!isTitleEtFocus) {
            return;
        }
        // 应产品需求去掉margin
        // RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mLiveTitleEt.getLayoutParams();
        // params.rightMargin = params.leftMargin = DisplayUtils.dip2px(58f);
        // mLiveTitleEt.setLayoutParams(params);


//        mLiveTitleEt.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
//        mLiveTitleEt.setHint("");
    }

    @Override
    protected void initTagName() {
        if (!LocaleUtil.isChineseLocal()) {
            hideTag();
            return;
        }
        String jsonString = PreferenceUtils.getSettingString(
                GlobalData.app(), PreferenceUtils.PREF_KEY_LIVE_NORMAL_TAG, "");
        if (!TextUtils.isEmpty(jsonString)) {
            try {
                mRoomTag = new RoomTag(jsonString);
            } catch (Exception e) {
            }
        }
        if (mRoomTag != null) {
            mTagNameTv.setText(getString(R.string.quanzi_name, mRoomTag.getTagName()));
            mTagNameTv.setSelected(true);
            mBeginBtn.setEnabled(true);
        } else {
            mBeginBtn.setEnabled(false);
        }
        prepareTagFromServer();
    }

    @Override
    protected void updateTagName() {
        if (mRoomTag != null) {
            mTagNameTv.setText(getString(R.string.quanzi_name, mRoomTag.getTagName()));
            mTagNameTv.setSelected(true);
        }
    }

    @Override
    public void hideTag() {
        super.hideTag();
        mBeginBtn.setEnabled(true);
    }

    @Override
    protected void openLive() {
        if (mRoomTag != null) {
            PreferenceUtils.setSettingString(
                    GlobalData.app(), PreferenceUtils.PREF_KEY_LIVE_NORMAL_TAG, mRoomTag.toJsonString());
        }
        openPublicLive();
    }

    public static void openFragment(
            BaseComponentSdkActivity fragmentActivity,
            int requestCode,
            FragmentDataListener listener,
            MagicParamPresenter magicParamPresenter) {
        PrepareLiveFragment fragment = (PrepareLiveFragment) FragmentNaviUtils.addFragment(fragmentActivity, R.id.main_act_container,
                PrepareLiveFragment.class, null, true, false, true);
        // fragment.setMagicParamPresenter(magicParamPresenter);
        if (listener != null) {
            fragment.initDataResult(requestCode, listener);
        }
    }
}
