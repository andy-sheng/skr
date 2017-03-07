package com.wali.live.livesdk.live.liveshow.fragment;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
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
import com.mi.live.engine.base.GalileoConstants;
import com.wali.live.common.action.VideoAction;
import com.wali.live.common.statistics.StatisticsAlmightyWorker;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.api.RoomTagRequest;
import com.wali.live.livesdk.live.fragment.BasePrepareLiveFragment;
import com.wali.live.livesdk.live.viewmodel.RoomTag;
import com.wali.live.statistics.StatisticsKey;
import com.wali.live.statistics.StatisticsWorker;
import com.wali.live.watchsdk.base.BaseComponentSdkActivity;

import butterknife.Bind;
import butterknife.OnClick;

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

    /**
     * 美颜设置相关
     */
    private IconConfigPresenter mPresenter = new IconConfigPresenter();
    private boolean mIsSupportBeauty = mPresenter.isSupportBeauty(); // 美颜
    private boolean mIsMultiLevel = mPresenter.isSupportMultiLevelBeauty();

    private int mSingleLevel = GalileoConstants.BEAUTY_LEVEL_HIGHEST; // 单级美颜默认级别
    private static final int sMultiLength = 4;
    private int[] mBeautyLevelStrengthIndex = new int[]{
            GalileoConstants.BEAUTY_LEVEL_OFF,
            GalileoConstants.BEAUTY_LEVEL_LOW,
            GalileoConstants.BEAUTY_LEVEL_MIDDLE,
            GalileoConstants.BEAUTY_LEVEL_HIGHEST
    };
    private boolean mShowBeautyContainer = false;
    private int mBeautyLevel = GalileoConstants.BEAUTY_LEVEL_HIGHEST; // 默认高美颜
    private int mBeautySupportCode = StreamerUtils.FACE_BEAUTY_DEFAULT;

    @Bind(R.id.beauty_btn)
    protected ImageView mBeautyIv;

    @Bind(R.id.beauty_level_container)
    protected ViewGroup mBeautyLevelContainer;

    private View mLastSelectView; // 标记上次选中的view

    @OnClick({R.id.close_tv, R.id.low_tv, R.id.middle_tv, R.id.high_tv})
    void onClickBeautyLevel(View v) {
        if (mLastSelectView != null) {
            if (mLastSelectView.equals(v)) {
                return;
            }
            mLastSelectView.setSelected(false);
        }
        mLastSelectView = v;
        mLastSelectView.setSelected(true);
        int level = GalileoConstants.BEAUTY_LEVEL_OFF;
        switch (v.getId()) {
            case R.id.close_tv:
                StatisticsWorker.getsInstance().sendCommand(
                        StatisticsWorker.AC_APP, StatisticsKey.KEY_PRE_LIVE_BEAUTY_CLOSE, 1);
                level = mBeautyLevelStrengthIndex[0];
                break;
            case R.id.low_tv:
                level = mBeautyLevelStrengthIndex[1];
                StatisticsWorker.getsInstance().sendCommand(
                        StatisticsWorker.AC_APP, StatisticsKey.KEY_PRE_LIVE_BEAUTY_LOW, 1);
                break;
            case R.id.middle_tv:
                level = mBeautyLevelStrengthIndex[2];
                StatisticsWorker.getsInstance().sendCommand(
                        StatisticsWorker.AC_APP, StatisticsKey.KEY_PRE_LIVE_BEAUTY_MIDDLE, 1);
                break;
            case R.id.high_tv:
                level = mBeautyLevelStrengthIndex[3];
                StatisticsWorker.getsInstance().sendCommand(
                        StatisticsWorker.AC_APP, StatisticsKey.KEY_PRE_LIVE_BEAUTY_HIGH, 1);
                break;
            default:
                break;
        }
        if (mBeautyLevel != level) {
            mBeautyLevel = level;
            updateBeautyIv(mBeautyLevel);
        }
        showBeautyLevelContainer(false);
    }

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
        int[] beautyLevelStrengthIndex = StreamerUtils.getMultiBeautyParams();
        if (mIsSupportBeauty && beautyLevelStrengthIndex != null && beautyLevelStrengthIndex.length > 0) {
            if (mIsMultiLevel) { // 支持美颜
                if (beautyLevelStrengthIndex.length == mBeautyLevelStrengthIndex.length) { // 多级美颜
                    mBeautyLevelStrengthIndex = beautyLevelStrengthIndex;
                }
            } else {
                mSingleLevel = beautyLevelStrengthIndex[0]; // 单级美颜
            }
        }
    }

    private void initBeautyView() {
        mBeautyIv.setTag(VideoAction.ACTION_VIDEO_BEAUTY_SWITCH);
        mBeautyIv.setOnClickListener(this);
        mBeautySupportCode = StreamerUtils.getFaceBeautySupportCode();
        int beautyLevel = PreferenceUtils.getSettingInt(
                getActivity(), PreferenceUtils.PREF_KEY_FACE_BEAUTY_LEVEL, GalileoConstants.BEAUTY_LEVEL_HIGHEST);
        switch (mBeautySupportCode) {
            case StreamerUtils.FACE_BEAUTY_DEFAULT:
                mBeautyLevel = mSingleLevel; // 单级美颜默认为BEAUTY_LEVEL_HIGH
                beautyLevel = mBeautyLevel;
                break;
            case StreamerUtils.FACE_BEAUTY_MULTI_LEVEL:
                mBeautyLevel = beautyLevel = (beautyLevel == GalileoConstants.BEAUTY_LEVEL_OFF) ? mBeautyLevelStrengthIndex[mBeautyLevelStrengthIndex.length - 1] : beautyLevel;
                break;
            case StreamerUtils.FACE_BEAUTY_DISABLED:
                mBeautyIv.setVisibility(View.GONE);
                mBeautyLevel = beautyLevel = GalileoConstants.BEAUTY_LEVEL_OFF;
                break;
            default:
                break;
        }
        int selectMultiBeautyIndex = 0;
        for (; selectMultiBeautyIndex < sMultiLength; ++selectMultiBeautyIndex) {
            if (mBeautyLevelStrengthIndex[selectMultiBeautyIndex] == beautyLevel) {
                break;
            }
        }
        switch (selectMultiBeautyIndex) {
            case 0:
                mLastSelectView = mBeautyLevelContainer.findViewById(R.id.close_tv);
                mLastSelectView.setSelected(true);
                break;
            case 1:
                mLastSelectView = mBeautyLevelContainer.findViewById(R.id.low_tv);
                mLastSelectView.setSelected(true);
                break;
            case 2:
                mLastSelectView = mBeautyLevelContainer.findViewById(R.id.middle_tv);
                mLastSelectView.setSelected(true);
                break;
            case 3:
                mLastSelectView = mBeautyLevelContainer.findViewById(R.id.high_tv);
                mLastSelectView.setSelected(true);
                break;
            default:
                break;
        }
        MyLog.w(TAG, "beautyLevel=" + beautyLevel + "mBeautySupportCode=" + mBeautySupportCode);
        updateBeautyIv(beautyLevel);
    }

    @Override
    protected void bindView() {
        super.bindView();
        initData();
        initBeautyView();
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
                TopicRecommendFragment.openFragment((BaseAppActivity) getActivity(), new TopicRecommendFragment.ITopicDataChangeListener() {
                    @Override
                    public void onTopicDataChanged(String topic) {
                        MyLog.d(TAG, "onTopicDataChanged " + topic);
                        if (!TextUtils.isEmpty(topic)) {
                            mTitleTextWatcher.setTopicDefaultWay(TOPIC_FROM_TMATY);
                            mLiveTitleEt.setText(topic);
                        } else {
                            if (TextUtils.isEmpty(mLiveTitleEt.getText())) {
                                mLiveTitleEt.setHint(R.string.prepare_live_edittext_hint);
                            }
                        }
                    }

                    @Override
                    public void onTopicFragmentFinished() {
                        MyLog.d(TAG, "onTopicFragmentFinished");
                        mRootView.setVisibility(View.VISIBLE);
                        mAddTopicContainer.setClickable(false);
                    }

                    @Override
                    public void onTopicFragmentDestoryed() {
                        mAddTopicContainer.setClickable(true);
                    }
                });
                KeyboardUtils.hideKeyboard(getActivity());
                mRootView.setVisibility(View.GONE);
                break;
            case VideoAction.ACTION_VIDEO_BEAUTY_SWITCH:
                StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP, StatisticsKey.KEY_PRE_LIVE_BEAUTY, 1);
                showBeautyLevelContainer(!mShowBeautyContainer);
                break;
            case VideoAction.ACTION_VIDEO_CAMERA_SWITCH:
                StatisticsWorker.getsInstance().sendCommand(StatisticsWorker.AC_APP, StatisticsKey.KEY_PRE_LIVE_CAMERA, 1);
                EventController.onActionCamera(EventClass.CameraEvent.EVENT_TYPE_SWITCH);
                break;
            default:
                break;
        }
    }

    private void updateBeautyIv(int beautyLevel) {
        MyLog.w(TAG, "beautyLevel=" + beautyLevel);
        mBeautyIv.setSelected(beautyLevel != GalileoConstants.BEAUTY_LEVEL_OFF);
        EventController.onActionBeautyLevelChange(beautyLevel);
    }

    private void showBeautyLevelContainer(boolean showBeautyContainer) {
        if (mShowBeautyContainer == showBeautyContainer) {
            return;
        }
        MyLog.d(TAG, "mBeautyLevelContainer isShow=" + showBeautyContainer + ", supportCode=" + mBeautySupportCode);
        mShowBeautyContainer = showBeautyContainer;
        switch (mBeautySupportCode) {
            case StreamerUtils.FACE_BEAUTY_MULTI_LEVEL:
                if (showBeautyContainer) {
                    if (mBeautyLevelContainer.getVisibility() != View.VISIBLE) {
                        mBeautyLevelContainer.setVisibility(View.VISIBLE);
                    }
                    showBeautyAnim();
                } else {
                    hideBeautyAnim();
                }
                break;
            case StreamerUtils.FACE_BEAUTY_DEFAULT:
                updateBeautyIv(showBeautyContainer ? mBeautyLevel : GalileoConstants.BEAUTY_LEVEL_OFF);
                break;
            default:
                break;
        }
    }

    private ValueAnimator mShowAnimation;
    private ValueAnimator mHideAnimation;

    private void showBeautyAnim() {
        if (mHideAnimation != null && mHideAnimation.isRunning()) {
            mHideAnimation.cancel();
        }
        if (mShowAnimation == null) {
            mShowAnimation = ValueAnimator.ofFloat(0.0f, 1.0f);
            mShowAnimation.setDuration(300);
            mShowAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            mShowAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    mBeautyLevelContainer.setAlpha(value);
                    mLocation.setAlpha(1 - value);
                    mTurnOverIv.setAlpha(1 - value);
                }
            });
        }
        if (!mShowAnimation.isRunning()) {
            mShowAnimation.start();
        }
        mBeautyLevelContainer.setEnabled(true);
        mLocation.setEnabled(false);
        mTurnOverIv.setEnabled(false);
    }

    private void hideBeautyAnim() {
        if (mShowAnimation != null && mShowAnimation.isRunning()) {
            mShowAnimation.cancel();
        }
        if (mHideAnimation == null) {
            mHideAnimation = ValueAnimator.ofFloat(1.0f, 0.0f);
            mHideAnimation.setDuration(300);
            mHideAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
            mHideAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float value = (float) animation.getAnimatedValue();
                    mBeautyLevelContainer.setAlpha(value);
                    mLocation.setAlpha(1 - value);
                    mTurnOverIv.setAlpha(1 - value);
                }
            });
        }
        if (!mHideAnimation.isRunning()) {
            mHideAnimation.start();
        }
        mBeautyLevelContainer.setEnabled(false);
        mLocation.setEnabled(true);
        mTurnOverIv.setEnabled(true);
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
            StatisticsAlmightyWorker.getsInstance().recordDelay(StatisticsKey.AC_APP,
                    StatisticsKey.KEY, String.format(StatisticsKey.KEY_LIVE_START_QUANZI,
                            mRoomTag.getTagId(), mRoomTag.getTagName()),
                    StatisticsKey.TIMES, "1");
        }
        openPublicLive();
    }

    public static void openFragment(
            BaseComponentSdkActivity fragmentActivity,
            int requestCode,
            FragmentDataListener listener) {
        BaseFragment fragment = FragmentNaviUtils.addFragment(fragmentActivity, R.id.main_act_container,
                PrepareLiveFragment.class, null, true, false, true);
        if (listener != null) {
            fragment.initDataResult(requestCode, listener);
        }
    }
}
