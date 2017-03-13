package com.wali.live.livesdk.live.liveshow.fragment;

import android.support.annotation.LayoutRes;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.base.dialog.MyAlertDialog;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.preference.PreferenceUtils;
import com.base.utils.language.LocaleUtil;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.api.RoomTagRequest;
import com.wali.live.livesdk.live.fragment.BasePrepareLiveFragment;
import com.wali.live.livesdk.live.liveshow.data.MagicParamPresenter;
import com.wali.live.livesdk.live.view.BeautyView;
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

    public static final int REQUEST_RECIPIENT_SELECT = 1000;

    private MyAlertDialog.Builder builder;

    private ImageView mTurnOverIv;
    protected RelativeLayout mCoverArea;
    private RelativeLayout mAddTopicContainer;
    private BeautyView mBeautyView;

    private final void $click(View view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
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
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (id == R.id.turn_over) {
            StatisticsWorker.getsInstance().sendCommand(
                    StatisticsWorker.AC_APP, StatisticsKey.KEY_PRE_LIVE_CAMERA, 1);
            // TODO 切换前后置相机
        } else if (id == R.id.cover_layout) {
            // TODO 跳转到添加封面页
        } else if (id == R.id.add_topic_container) {
            // TODO 跳转到添加话题页
            // 将新的Fragment(TopicRecommendFragment)加(add)到栈中,并隐藏PrepareLiveFragment
//            TopicRecommendFragment.openFragment((BaseAppActivity) getActivity(), new TopicRecommendFragment.ITopicDataChangeListener() {
//                @Override
//                public void onTopicDataChanged(String topic) {
//                    MyLog.d(TAG, "onTopicDataChanged " + topic);
//                    if (!TextUtils.isEmpty(topic)) {
//                        mTitleTextWatcher.setTopicDefaultWay(TOPIC_FROM_TMATY);
//                        mLiveTitleEt.setText(topic);
//                    } else {
//                        if (TextUtils.isEmpty(mLiveTitleEt.getText())) {
//                            mLiveTitleEt.setHint(R.string.prepare_live_edittext_hint);
//                        }
//                    }
//                }
//
//                @Override
//                public void onTopicFragmentFinished() {
//                    MyLog.d(TAG, "onTopicFragmentFinished");
//                    mRootView.setVisibility(View.VISIBLE);
//                    mAddTopicContainer.setClickable(false);
//                }
//
//                @Override
//                public void onTopicFragmentDestoryed() {
//                    mAddTopicContainer.setClickable(true);
//                }
//            });
//            KeyboardUtils.hideKeyboard(getActivity());
//            mRootView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void initContentView() {
        super.initContentView();
        mTurnOverIv = $(R.id.turn_over);
        mCoverArea = $(R.id.cover_layout);
        mAddTopicContainer = $(R.id.add_topic_container);
        mBeautyView = $(R.id.beauty_view);
        mBeautyView.setBeautyCallBack(new BeautyView.BeautyCallBack() {
            @Override
            public void showMultiBeautyAnim() {
                //隐藏相机和地理位置
                mLocationTv.setVisibility(View.INVISIBLE);
                mTurnOverIv.setVisibility(View.INVISIBLE);
            }

            @Override
            public void hideMultiBeautyAnim() {
                //显示相机和地理位置
                mLocationTv.setVisibility(View.VISIBLE);
                mTurnOverIv.setVisibility(View.VISIBLE);
            }
        });
        $click(mTurnOverIv, this);
        $click(mCoverArea, this);
        $click(mAddTopicContainer, this);
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
//         fragment.setMagicParamPresenter(magicParamPresenter);
        if (listener != null) {
            fragment.initDataResult(requestCode, listener);
        }
    }
}
