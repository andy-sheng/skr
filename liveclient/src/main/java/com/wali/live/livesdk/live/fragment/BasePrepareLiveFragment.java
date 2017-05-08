package com.wali.live.livesdk.live.fragment;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.base.activity.RxActivity;
import com.base.dialog.MyAlertDialog;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.FragmentListener;
import com.base.fragment.MyRxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.network.Network;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.api.LiveManager;
import com.mi.live.data.preference.PreferenceKeys;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.api.RoomTagRequest;
import com.wali.live.livesdk.live.presenter.IRoomTagView;
import com.wali.live.livesdk.live.presenter.RoomTagPresenter;
import com.wali.live.livesdk.live.presenter.view.IRoomPrepareView;
import com.wali.live.livesdk.live.viewmodel.RoomTag;

import java.util.List;

import rx.Observable;

/**
 * Created by zyh on 2017/2/8.
 */

public abstract class BasePrepareLiveFragment extends MyRxFragment implements View.OnClickListener, FragmentDataListener, IRoomTagView, FragmentListener, IRoomPrepareView {
    public static final int REQUEST_CODE = GlobalData.getRequestCode();

    public static final String EXTRA_SNS_TYPE = "extra_sns_type";
    public static final String EXTRA_LIVE_TYPE = "extra_live_type";
    public static final String EXTRA_ADD_HISTORY = "extra_add_history";
    public static final String EXTRA_INVITEE_LIST = "extra_invitee_list";
    public static final String EXTRA_LIVE_TITLE = "extra_live_title";
    public static final String EXTRA_LIVE_COVER_URL = "extra_live_cover_url";
    public static final String EXTRA_LIVE_TAG_INFO = "extra_live_tag_info";

    // 直播话题get的方式：从TopicRecommendActivity获取topic为1；当前页面自定义为0
    public final static int TOPIC_FROM_TMATY = 1;
    public final static int TOPIC_FROM_CUSTOM = 0;

    // 分享相关
    public static final int WEI_XIN = 1;
    public static final int MOMENT = 1 << 1;
    public static final int QQ = 1 << 2;
    public static final int QZONE = 1 << 3;
    public static final int WEIBO = 1 << 4;
    public static final int MILIAO = 1 << 5;
    public static final int MILIAO_FEEDS = 1 << 6;

    @NonNull
    protected RoomBaseDataModel mMyRoomData;
    protected boolean mIsAddHistory = true;
    protected TitleTextWatcher mTitleTextWatcher;

    protected TextView mBeginBtn;
    protected TextView mTagNameTv;
    protected ViewGroup mTagNameContainer;
    protected ImageView mCloseBtn;

    protected EditText mLiveTitleEt;

    protected RoomTag mRoomTag;
    protected RoomTagPresenter mRoomTagPresenter;
    protected int mTagIndex = -1;

    protected LiveRoomChatMsgManager mRoomChatMsgManager;
    protected ImageView mShareSelectedIv;
    protected View mShareContainer;

    public void setMyRoomData(@NonNull RoomBaseDataModel myRoomData) {
        mMyRoomData = myRoomData;
        tryHideShareBtnView();
    }

    private void tryHideShareBtnView() {
        if (mMyRoomData != null && mShareContainer != null) {
            if (!mMyRoomData.getShareType()) {
                mShareContainer.setVisibility(View.GONE);
            } else {
                boolean shareSelectedState = PreferenceUtils.getSettingBoolean(GlobalData.app(), PreferenceKeys.PRE_SHARE_SELECTED_STATE, true);
                mShareSelectedIv.setSelected(shareSelectedState);
            }
        }
    }

    public void setRoomChatMsgManager(@NonNull LiveRoomChatMsgManager roomChatMsgManager) {
        mRoomChatMsgManager = roomChatMsgManager;
    }

    @CallSuper
    @Override
    public void onClick(View v) {
        if (isFastDoubleClick() || getActivity() == null) {
            return;
        }
        KeyboardUtils.hideKeyboardImmediately(getActivity());
        int i = v.getId();
        if (i == R.id.begin_btn) {
            onBeginBtnClick();
        } else if (i == R.id.close_btn) {
            onCloseBtnClick();
        } else if (i == R.id.tag_name_container) {
            onTagNameBtnClick();
        } else if (i == R.id.share_container) {
            onShareBtnClick();
        }
    }

    protected abstract int getLayoutResId();

    protected abstract void adjustTitleEtPosByCover(boolean isTitleEtFocus, int coverState);

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    protected void onBeginBtnClick() {
        //网络判断
        if (!Network.hasNetwork((GlobalData.app()))) {
            ToastUtils.showToast(GlobalData.app(), R.string.network_unavailable);
            return;
        }
        openLive();
        recordShareSelectState();
//        mShareBtnView.hideShareToast();
    }

    private void onCloseBtnClick() {
        KeyboardUtils.hideKeyboard(getActivity());
        getActivity().finish();
    }

    private void onTagNameBtnClick() {
        getTagFromServer();
    }

    private void onShareBtnClick() {
        MyLog.w(TAG, "onShareBtnClick");
        mShareSelectedIv.setSelected(!mShareSelectedIv.isSelected());
        MyLog.w(TAG, "mShareSelectedIv.isSelected()=" + mShareSelectedIv.isSelected());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        MyLog.w(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        MyLog.w(TAG, "createView");
        return inflater.inflate(getLayoutResId(), container, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mTagNameContainer != null) {
            mRoomTagPresenter.stop();
        }
    }

    protected void initContentView() {
        MyLog.w(TAG, "initContentView");
        mRootView.setOnTouchListener(null);
        mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyboardUtils.hideKeyboard(getActivity());
            }
        });

        mBeginBtn = $(R.id.begin_btn);
        mBeginBtn.setOnClickListener(this);

        mTagNameContainer = $(R.id.tag_name_container);
        if (mTagNameContainer != null) {
            mTagNameContainer.setOnClickListener(this);
        }

        mCloseBtn = $(R.id.close_btn);
        mCloseBtn.setOnClickListener(this);

        mTagNameTv = $(R.id.tag_name_tv);

        mShareContainer = $(R.id.share_container);
        mShareSelectedIv = $(R.id.share_friends_iv);
        mShareContainer.setOnClickListener(this);

        tryHideShareBtnView();
    }

    protected void initTitleView() {
        mLiveTitleEt = (EditText) mRootView.findViewById(R.id.live_title_et);
        mLiveTitleEt.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                BasePrepareLiveFragment.this.adjustTitleEtPosByCover(hasFocus, View.GONE);
            }
        });
        mTitleTextWatcher = new TitleTextWatcher(mLiveTitleEt);
        mLiveTitleEt.addTextChangedListener(mTitleTextWatcher);
    }

    @Override
    protected void bindView() {
        MyLog.w(TAG, "bindView");
        initContentView();
        initTitleView();
        initPresenters();
        if (mTagNameContainer != null) {
            initTagName();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        MyLog.w(TAG, "onDestroy");
        super.onDestroy();
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    protected void finish() {
        MyLog.w(TAG, "finish");
        FragmentNaviUtils.popFragmentFromStack(getActivity());
    }

    protected abstract void openLive();

    protected abstract void getTagFromServer();

    protected abstract void initTagName();

    protected void openPublicLive() {
        if (mDataListener != null) {
            Bundle bundle = new Bundle();
            putCommonData(bundle);
            bundle.putInt(EXTRA_LIVE_TYPE, LiveManager.TYPE_LIVE_PUBLIC);
            bundle.putBoolean(EXTRA_ADD_HISTORY, mIsAddHistory);
            mDataListener.onFragmentResult(mRequestCode, Activity.RESULT_OK, bundle);
        }
        finish();
    }

    protected void putCommonData(Bundle bundle) {
        // 产品要求支持多个分享
        bundle.putBoolean(EXTRA_SNS_TYPE, isShareSelected());
        bundle.putString(EXTRA_LIVE_TITLE, mLiveTitleEt.getText().toString().trim());
        // 添加标签
        if (mRoomTag != null) {
            bundle.putSerializable(EXTRA_LIVE_TAG_INFO, mRoomTag);
        }
    }

    protected void initPresenters() {
        if (mTagNameContainer != null) {
            mRoomTagPresenter = new RoomTagPresenter((RxActivity) getActivity(), this);
        }
    }

    @Override
    public void hideTag() {
        mTagNameContainer.setVisibility(View.GONE);
    }

    @Override
    public void showTagList(final List<RoomTag> roomTags, int type) {
        mTagNameContainer.setVisibility(View.VISIBLE);

        MyAlertDialog.Builder builder = new MyAlertDialog.Builder(getActivity());
        String[] tagArray = new String[roomTags.size()];
        String[] tagIcons = new String[roomTags.size()];
        for (int i = 0; i < roomTags.size(); i++) {
            tagArray[i] = roomTags.get(i).getTagName();
            tagIcons[i] = roomTags.get(i).getIconUrl();
            if (mRoomTag != null && mRoomTag.getTagName().equals(tagArray[i])) {
                mTagIndex = i;
            }
        }
        switch (type) {
            case RoomTagRequest.TAG_TYPE_GAME:
                builder.setTitle(R.string.game_live_tag_title);
                break;
            default:
                builder.setTitle(R.string.normal_live_tag_title);
                mTagIndex = mTagIndex == -1 ? tagArray.length - 1 : mTagIndex;
                mRoomTag = roomTags.get(mTagIndex);
                break;
        }
        updateTagName();

        builder.setSingleChoiceItems(tagArray, mTagIndex, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MyLog.d(TAG, "click item=" + which + ";tagInfo=" + mRoomTag);
                mTagIndex = which;
                mRoomTag = roomTags.get(which);
                updateTagName();
                dialog.dismiss();
            }
        }).show();
    }

    protected abstract void updateTagName();

    @Override
    public void setManagerCount(int count) {
    }

    @Override
    public void fillTitle(String title) {
    }

    @Override
    public void updateControlTitleArea(boolean isShow) {
    }

    @Override
    public <T> Observable.Transformer<T, T> bindLifecycle() {
        return bindUntilEvent();
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        return null;
    }

    @Override
    public void onFragmentResult(int requestCode, int resultCode, Bundle bundle) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        MyLog.w(TAG, "onActivityResult requestCode : " + requestCode);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            default:
                break;
        }
    }

    protected boolean isShareSelected() {
        if (mShareContainer.getVisibility() == View.VISIBLE) {
            return mShareSelectedIv.isSelected();
        }
        return false;
    }

    protected void recordShareSelectState() {
        if (mShareContainer.getVisibility() == View.VISIBLE) {
            PreferenceUtils.setSettingBoolean(GlobalData.app(), PreferenceKeys.PRE_SHARE_SELECTED_STATE, mShareSelectedIv.isSelected());
        }
    }

    protected static class TitleTextWatcher implements TextWatcher {
        private String originStr; // 原始字符串
        private int afterIndex;   // 光标位置
        private int delL, delR;   // 删除字符串的边界
        private String addStr;    // 添加的字符串 输入一个# 补上#  删除操作时候为空

        private boolean enableWatch = true;
        private int defaultWay = TOPIC_FROM_CUSTOM;
        private final int lenMax = 28;

        private EditText editText;

        public TitleTextWatcher(@NonNull EditText editText) {
            this.editText = editText;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (enableWatch) {
                delL = -1;
                delR = -1;
                originStr = s.toString();
                if (count > 0) {
                    // 说明从start开始有count个字符被删除，检测是否有#被删除
                    String mid = s.toString().substring(start, start + count);
                    if (mid.contains("#")) {
                        int midC = 0, // #的个数
                                L = -1;
                        // 如果有# 需要成对删除#
                        for (int i = 0; i < mid.length(); i++) {
                            if (mid.charAt(i) == '#') {
                                midC++;
                            }
                        }
                        for (int i = 0; i < start; i++) {
                            if (originStr.charAt(i) == '#') {
                                if (L == -1) {
                                    L = i;
                                } else {
                                    L = -1;
                                }
                            }
                        }
                        if (L != -1) {
                            delL = L;
                            delR = start + count;
                            midC--;
                        } else {
                            delL = start;
                            delR = start + count;
                        }
                        midC = midC % 2;
                        if (midC > 0) {
                            // 右侧成对删除#
                            for (int i = start + count; i < originStr.length(); i++) {
                                if (originStr.charAt(i) == '#') {
                                    delR = i + 1;
                                    break;
                                }
                            }
                        }
                    } else {
                        delL = start;
                        delR = start + count;
                    }
                } else {
                    delL = start;
                    delR = start + count;
                }
            }
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (enableWatch) {
                addStr = s.toString().substring(start, start + count);
                if (defaultWay == TOPIC_FROM_CUSTOM) {
                    if (addStr.contains("#")) {
                        addStr = addStr.replace("#", "");
                    }
                }
                defaultWay = TOPIC_FROM_CUSTOM;
            }
            if (TextUtils.isEmpty(editText.getText())) {
                editText.setHint(GlobalData.app().getString(R.string.live_title_hint));
            } else {
                editText.setHint("");
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (enableWatch) {
                String result;
                if (addStr.contains("#")) {
                    // 加入井号优先级高 直接补在队尾
                    result = originStr + addStr;
                    afterIndex = originStr.length() + addStr.length();
                } else {
                    result = originStr.substring(0, delL) + addStr + originStr.substring(delR, originStr.length());
                    if (addStr.equals("")) {
                        // 删除操作
                        afterIndex = delL;
                    } else {
                        // 添加操作
                        afterIndex = delL + addStr.length();
                    }
                }
                if (result.length() > lenMax) {
                    formatInputString(originStr, delL); // delL现在等于 start
                    ToastUtils.showToast(editText.getResources().getString(R.string.max_topic_count, lenMax));
                } else {
                    formatInputString(result, afterIndex);
                }
            }
        }

        public void formatInputString(String text, int strIndex) {
            enableWatch = false;
            SpannableStringBuilder str = new SpannableStringBuilder(text);
            int len = text.length();
            int l = -1;
            for (int i = 0; i < len; i++) {
                if (text.charAt(i) == '#') {
                    if (l == -1) {
                        l = i;
                    } else {
                        str.setSpan(new ForegroundColorSpan(editText.getResources().getColor(R.color.color_e5aa1e)),
                                l, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        l = -1;
                    }
                }
            }
            editText.setText(str);
            editText.setSelection(strIndex);
            enableWatch = true;
        }

        public void setTopicDefaultWay(int defaultWay) {
            this.defaultWay = defaultWay;
        }
    }
}

