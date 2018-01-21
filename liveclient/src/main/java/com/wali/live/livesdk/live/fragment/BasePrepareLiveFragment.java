package com.wali.live.livesdk.live.fragment;

import android.app.Activity;
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

import com.base.fragment.BaseEventBusFragment;
import com.base.fragment.FragmentDataListener;
import com.base.fragment.FragmentListener;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.global.GlobalData;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.preference.PreferenceUtils;
import com.base.utils.network.Network;
import com.base.utils.toast.ToastUtils;
import com.mi.live.data.account.UserAccountManager;
import com.mi.live.data.event.LiveRoomManagerEvent;
import com.mi.live.data.manager.LiveRoomCharacterManager;
import com.mi.live.data.manager.model.LiveRoomManagerModel;
import com.mi.live.data.milink.event.MiLinkEvent;
import com.mi.live.data.preference.PreferenceKeys;
import com.mi.live.data.query.model.MessageRule;
import com.mi.live.data.room.model.RoomBaseDataModel;
import com.wali.live.common.barrage.manager.LiveRoomChatMsgManager;
import com.wali.live.event.UserActionEvent;
import com.wali.live.livesdk.R;
import com.wali.live.livesdk.live.presenter.RoomPreparePresenter;
import com.wali.live.livesdk.live.presenter.view.IRoomPrepareView;
import com.wali.live.proto.LiveCommonProto;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import static android.view.View.VISIBLE;

/**
 * Created by zyh on 2017/2/8.
 */

public abstract class BasePrepareLiveFragment extends BaseEventBusFragment implements View.OnClickListener, FragmentDataListener, FragmentListener, IRoomPrepareView {
    public static final int REQUEST_CODE = GlobalData.getRequestCode();

    public static final String EXTRA_SNS_TYPE = "extra_sns_type";
    public static final String EXTRA_LIVE_TYPE = "extra_live_type";
    public static final String EXTRA_ADD_HISTORY = "extra_add_history";
    public static final String EXTRA_LIVE_TITLE = "extra_live_title";
    public static final String EXTRA_LIVE_COVER_URL = "extra_live_cover_url";
    public static final String EXTRA_LIVE_TAG_INFO = "extra_live_tag_info";
    public static final String EXTRA_LIVE_QUALITY = "extra_game_live_quality";
    public static final String EXTRA_GAME_LIVE_MUTE = "extra_game_live_mute";

    public static final int LOW_CLARITY = 0;
    public static final int MEDIUM_CLARITY = 1;
    public static final int HIGH_CLARITY = 2;

    // 直播话题get的方式：从TopicRecommendActivity获取topic为1；当前页面自定义为0
    public final static int TOPIC_FROM_TMATY = 1;
    public final static int TOPIC_FROM_CUSTOM = 0;
    @NonNull
    protected RoomBaseDataModel mMyRoomData;
    protected boolean mIsAddHistory = true;
    protected TitleTextWatcher mTitleTextWatcher;

    protected TextView mBeginBtn;
    protected ImageView mCloseBtn;

    protected EditText mLiveTitleEt;

    protected RoomPreparePresenter mRoomPreparePresenter;
    protected LiveRoomChatMsgManager mRoomChatMsgManager;
    protected ImageView mShareSelectedIv;
    protected View mShareContainer;

    private View mDailyTaskSl;
    private ViewGroup mDailyTaskArea;
    private LiveCommonProto.NewWidgetUnit mWidgetUnit;

    protected ViewGroup mAdminArea;
    protected TextView mAdminCount;

    private ViewGroup mControlTitleArea;
    private TextView mChangeTitleTv;
    private TextView mClearTitleTv;
    protected ViewGroup mTopContainer;
    protected ViewGroup mTitleContainer;
    protected ViewGroup mMiddleContainer;

    @Override
    public int getRequestCode() {
        return REQUEST_CODE;
    }

    @CallSuper
    @Override
    public void onClick(View v) {
        KeyboardUtils.hideKeyboardImmediately(getActivity());
        int i = v.getId();
        if (i == R.id.begin_btn) {
            if (!Network.hasNetwork((GlobalData.app()))) { // 网络判断
                ToastUtils.showToast(GlobalData.app(), R.string.network_unavailable);
                return;
            }
            performBeginClick();
        } else if (i == R.id.close_btn) {
            KeyboardUtils.hideKeyboard(getActivity());
            getActivity().finish();
        } else if (i == R.id.share_container) {
            mShareSelectedIv.setSelected(!mShareSelectedIv.isSelected());
        } else if (i == R.id.daily_task_area) {
            UserActionEvent.post(UserActionEvent.EVENT_TYPE_CLICK_ATTACHMENT,
                    mWidgetUnit.getLinkUrl(), mWidgetUnit.getUrlNeedParam(), mWidgetUnit.getOpenType(),
                    UserAccountManager.getInstance().getUuidAsLong());
        } else if (i == R.id.admin_area) {
            openAdminFragment();
        } else if (i == R.id.change_title_tv) {
            mRoomPreparePresenter.changeTitle();
        } else if (i == R.id.clear_title_tv) {
            mLiveTitleEt.setText("");
        } else if (i == R.id.main_fragment_container) {
            hideBottomPanel(true);
        }
    }

    protected abstract void updateCover();

    protected abstract int getLayoutResId();

    protected abstract void openLive();

    protected abstract void performBeginClick();

    public void setMyRoomData(@NonNull RoomBaseDataModel myRoomData) {
        mMyRoomData = myRoomData;
        tryHideShareBtnView();
    }

    private void tryHideShareBtnView() {
        if (mMyRoomData != null && mShareContainer != null) {
            if (!mMyRoomData.getEnableShare()) {
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

    protected void getDailyTaskFromServer() {
        if (mRoomPreparePresenter != null) {
            mRoomPreparePresenter.loadManager();
            mRoomPreparePresenter.loadTitle();
            mRoomPreparePresenter.loadDailyTask(mMyRoomData.getLiveType());
        }
    }

    private void openAdminFragment() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(RoomAdminFragment.KEY_ROOM_SEND_MSG_CONFIG, new MessageRule());
        bundle.putLong(RoomAdminFragment.KEY_ROOM_ANCHOR_ID, UserAccountManager.getInstance().getUuidAsLong());
        bundle.putBoolean(RoomAdminFragment.KEY_ONLY_SHOW_ADMIN_MANAGER_PAGE, true);
        FragmentNaviUtils.addFragment(getActivity(), R.id.main_act_container, RoomAdminFragment.class, bundle, true, true, true);
    }

    protected void showBottomPanel(boolean useAnimation) {
        mTopContainer.setVisibility(View.GONE);
        mTitleContainer.setVisibility(View.GONE);
        mMiddleContainer.setVisibility(View.GONE);
        if (mMyRoomData.getEnableShare()) {
            mShareContainer.setVisibility(View.GONE);
        }
        mRootView.setOnClickListener(this);
    }

    protected void hideBottomPanel(boolean useAnimation) {
        mTopContainer.setVisibility(View.VISIBLE);
        mTitleContainer.setVisibility(View.VISIBLE);
        mMiddleContainer.setVisibility(View.VISIBLE);
        if (mMyRoomData.getEnableShare()) {
            mShareContainer.setVisibility(View.VISIBLE);
        }
        mRootView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                KeyboardUtils.hideKeyboardImmediately(getActivity());
            }
        });
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
        $click(mBeginBtn, this);

        mCloseBtn = $(R.id.close_btn);
        $click(mCloseBtn, this);

        mShareContainer = $(R.id.share_container);
        mShareSelectedIv = $(R.id.share_friends_iv);
        $click(mShareContainer, this);
        tryHideShareBtnView();

        mDailyTaskSl = $(R.id.daily_task_sl);
        mDailyTaskArea = $(R.id.daily_task_area);
        $click(mDailyTaskArea, this);

        mAdminArea = $(R.id.admin_area);
        mAdminCount = $(R.id.admin_count);
        $click(mAdminArea, this);

        mControlTitleArea = $(R.id.control_title_area);
        mChangeTitleTv = $(R.id.change_title_tv);
        $click(mChangeTitleTv, this);
        mClearTitleTv = $(R.id.clear_title_tv);
        $click(mClearTitleTv, this);

        mTopContainer = $(R.id.top_container);
        mTitleContainer = $(R.id.title_container);
        mMiddleContainer = $(R.id.middle_container);
    }

    protected void initTitleView() {
        mLiveTitleEt = $(R.id.live_title_et);
        mTitleTextWatcher = new TitleTextWatcher(mLiveTitleEt);
        mLiveTitleEt.addTextChangedListener(mTitleTextWatcher);
    }

    @Override
    protected void bindView() {
        MyLog.w(TAG, "bindView");
        initContentView();
        initTitleView();
        initPresenters();
        getDailyTaskFromServer();
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

    protected void putCommonData(Bundle bundle) {
        // 产品要求支持多个分享
        bundle.putBoolean(EXTRA_SNS_TYPE, isShareSelected());
        bundle.putString(EXTRA_LIVE_TITLE, mLiveTitleEt.getText().toString().trim());
    }

    protected void initPresenters() {
    }

    @Override
    public void setManagerCount(int count) {
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
    public void setDailyTaskUnit(LiveCommonProto.NewWidgetUnit unit) {
        if (unit != null && unit.hasLinkUrl()) {
            mDailyTaskSl.setVisibility(VISIBLE);
            mDailyTaskArea.setVisibility(VISIBLE);
            mWidgetUnit = unit;
        } else {
            mDailyTaskSl.setVisibility(View.GONE);
            mDailyTaskArea.setVisibility(View.GONE);
        }
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
        if (mShareContainer.getVisibility() == VISIBLE) {
            return mShareSelectedIv.isSelected();
        }
        return false;
    }

    protected void recordShareSelectState() {
        if (mShareContainer.getVisibility() == VISIBLE) {
            PreferenceUtils.setSettingBoolean(GlobalData.app(), PreferenceKeys.PRE_SHARE_SELECTED_STATE, mShareSelectedIv.isSelected());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(MiLinkEvent.StatusLogined event) {
        if (event != null) {
            getDailyTaskFromServer();
            updateCover();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LiveRoomManagerEvent event) {
        List<LiveRoomManagerModel> managerModels = LiveRoomCharacterManager.getInstance().getRoomManagers();
        int managerCount = managerModels.size();
        long top1Id = mRoomPreparePresenter.getTop1Id();
        if (top1Id != 0) {
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
        }
        setManagerCount(managerCount);
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
    }
}

