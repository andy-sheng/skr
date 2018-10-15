package com.wali.live.watchsdk.feedback;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.base.activity.BaseActivity;
import com.base.event.KeyboardEvent;
import com.base.fragment.RxFragment;
import com.base.fragment.utils.FragmentNaviUtils;
import com.base.keyboard.KeyboardUtils;
import com.base.log.MyLog;
import com.base.utils.display.DisplayUtils;
import com.base.utils.toast.ToastUtils;
import com.wali.live.common.view.PlaceHolderView;
import com.wali.live.watchsdk.R;
import com.wali.live.watchsdk.channel.holder.LiveLinearLayoutManager;
import com.wali.live.watchsdk.feedback.adapter.ReportReasonAdapter;
import com.wali.live.watchsdk.feedback.contact.ReportContact;
import com.wali.live.watchsdk.feedback.listener.OnItemListener;
import com.wali.live.watchsdk.feedback.presenter.ReportPresenter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.Arrays;

import static com.mi.milink.sdk.base.Global.getSystemService;


/**
 * Created by yangjiawei on 2017/4/14.
 * <p>
 * 新的举报fragment,取消了举报类型二级分类
 */

public class ReportFragment extends RxFragment implements View.OnClickListener {
    private static final String TAG = ReportFragment.class.getSimpleName();

    private static final int REPORT_TYPE_ID_START = 200; // +  距reason源数据 offset  = 上传的id
    public static final String EXT_ANCHOR = "anchor"; //举报主播
    public static final String LOCATION_ROOM = "room";  //举报时所处的位置  房间内
    public static final String LOCATION_FEEDS_COMMENT = "feeds_comment"; // feeds评论
    private static final String EXTRA_REPORT_TYPE = "reportType";
    public static final int TYPE_NORMAL = 0;    //正常列表
    public static final int TYPE_OTHER = 1;    //点击"其他"时的输入框
    private static final String EXTRA_UID = "uid";
    private static final String EXTRA_ROOM_ID = "roomId";
    private static final String EXTRA_URL = "url";
    private static final String EXTRA_REPORT_POSITION = "reportPosition";
    private static final String EXTRA_COMMENT_PROOF = "commentProof";
    private static final int REPORT_REASON_LIST = R.array.report_reason_detail;

    //data
    private String mReportType;
    private int mSelectedNum = -1;   // 选择的原因index
    private int mCurContentType = TYPE_NORMAL;
    private long mUuid;
    private String mRoomId;
    private String mLiveUrl;
    private String mReportPosition; // 从哪里点的举报
    private String mCommentProof; // feeds评论点举报 的证据
    private boolean mIsShortMode; // content是否缩短，避免被键盘盖住
    private String[] mReportReasons;

    //ui
    private TextView mTitle;
    private Button mBottomButton;
    private View mBack;
    private View mCardRoot;
    private View dismissArea;
    private RelativeLayout mTitleBar;
    private RelativeLayout mContentView;
    private RecyclerView mRecyclerView;             //fragment主列表
    private ReportReasonAdapter mAdapter;
    private PlaceHolderView mPlaceHolderView;       //点击"其他"，当键盘弹出时，用于调整输入框的高度
    private EditText mEditText;

    //presenter
    private ReportPresenter mPresenter;
    @Override
    public int getRequestCode() {
        return 0;
    }

    @Override
    protected View createView(LayoutInflater inflater, ViewGroup container) {
        mRootView = inflater.inflate(R.layout.frag_new_report, container, false);
        EventBus.getDefault().register(this);
        return mRootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //隐藏可能显示的输入法
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(mRecyclerView.getWindowToken(), 0);
    }

    @Override
    protected void bindView() {
        Bundle bundle = getArguments();
        if (bundle == null) {
            return;
        }
        mUuid = bundle.getLong(EXTRA_UID);
        mRoomId = bundle.getString(EXTRA_ROOM_ID);
        mLiveUrl = bundle.getString(EXTRA_URL);
        mReportPosition = bundle.getString(EXTRA_REPORT_POSITION);
        mReportType = bundle.getString(EXTRA_REPORT_TYPE);
        if ((LOCATION_FEEDS_COMMENT).equals(mReportPosition)) {
            mCommentProof = bundle.getString(EXTRA_COMMENT_PROOF);
        }
        mContentView = (RelativeLayout) mRootView.findViewById(R.id.content);
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.report_detail_recycler);
        mTitle = (TextView) mRootView.findViewById(R.id.title_tv);
        mCardRoot = mRootView.findViewById(R.id.rl_root);
        dismissArea = mRootView.findViewById(R.id.dismiss_area);
        dismissArea.setOnClickListener(this);
        mBottomButton = (Button) mRootView.findViewById(R.id.button);
        mBottomButton.setOnClickListener(this);
        mTitleBar = (RelativeLayout) mRootView.findViewById(R.id.title_bar);
        mBack = mRootView.findViewById(R.id.back_btn);
        mEditText = (EditText) mRootView.findViewById(R.id.editText);
        mEditText.setHint(getContext().getResources().getString(R.string.report_edittext_hint));
        mEditText.setFilters(new InputFilter[]{new MyLengthFilter(200, getContext())});
        mBack.setOnClickListener(this);
        mPlaceHolderView = (PlaceHolderView) mRootView.findViewById(R.id.holder_view);
        startOutAnimation();
        showEnterAnimation();
        bindContentView();
        setCurrentContent(TYPE_NORMAL);

        initPresenter();
    }

    @Override
    protected boolean needForceActivityOrientation() {
        return false;
    }

    private void initPresenter() {
        mPresenter = new ReportPresenter(new ReportContact.IView() {
            @Override
            public void reportFeedBack(boolean ret) {
                if (ret) {
                    ToastUtils.showToast(getContext(), R.string.report_success);
                    KeyboardUtils.hideKeyboard(getActivity());
                    finish();
                } else {
                    ToastUtils.showToast(getContext(), R.string.report_fail);
                }
            }
        });
    }

    private void bindContentView() {
        mReportReasons = getResources().getStringArray(REPORT_REASON_LIST);
        mAdapter = new ReportReasonAdapter();
        mAdapter.setListener(new OnItemListener() {
            @Override
            public void onClick(int pos) {
                mSelectedNum = pos;
                sendReport();
            }
        });
        mAdapter.setReasonList(Arrays.asList(mReportReasons));
        //固定，无法滑动
        LinearLayoutManager linearLayoutManager = new LiveLinearLayoutManager(getContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.back_btn) {
            setCurrentContent(TYPE_NORMAL);
            KeyboardUtils.hideKeyboard(getActivity());

        } else if (i == R.id.button) {
            if (mCurContentType == TYPE_NORMAL) {
                finish();
            } else {
                sendReport();
            }

        } else if (i == R.id.dismiss_area) {
            finish();
        }
    }

    private void setCurrentContent(int i) {
        mCurContentType = i;
        if (i == TYPE_NORMAL) {
            mBottomButton.setText(getResources().getString(R.string.cancel));
            mSelectedNum = -1;
            mBack.setVisibility(View.INVISIBLE);
            mTitleBar.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mEditText.setVisibility(View.GONE);
        } else {
            mBottomButton.setText(getResources().getString(R.string.report_send));
            mBack.setVisibility(View.VISIBLE);
            mTitleBar.setVisibility(View.VISIBLE);
            mTitle.setText(getResources().getString(R.string.report_input_content));
            mRecyclerView.setVisibility(View.GONE);
            mEditText.setVisibility(View.VISIBLE);
        }
    }

    private String getOtherReason() {
        String content = "";
        content = mEditText.getText().toString().trim();
        MyLog.w(TAG, "content = " + content);
        return (!TextUtils.isEmpty(content) && content.length() >= 200) ? content.substring(0, 200) : content;
    }

    public void sendReport() {
        mPresenter.sendReport(mUuid, getReportTypeId(), mRoomId, mLiveUrl, mReportPosition, mCommentProof, mCurContentType, getOtherReason());
    }

    private int getReportTypeId() {
        //其他
        if (mSelectedNum == mReportReasons.length - 1) {
            return 0;
        } else {
            return mSelectedNum + 1 + REPORT_TYPE_ID_START;
        }
    }

    private void finish() {
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_bottom_out);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                //动画结束时finish页面.
                if (!isDetached()) {
                    FragmentNaviUtils.popFragment(getActivity());
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mCardRoot.startAnimation(animation);
    }

    @Override
    public boolean onBackPressed() {
        if (mCurContentType == TYPE_OTHER) {
            setCurrentContent(TYPE_NORMAL);
            return true;
        } else {
            if (!isDetached()) {
                FragmentNaviUtils.popFragment(getActivity());
                return true;
            }
            return super.onBackPressed();
        }
    }

    private void showEnterAnimation() {
        Animation animation = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_bottom_in);
        animation.setDuration(500);
        mCardRoot.startAnimation(animation);
    }

    private void startOutAnimation() {
        AlphaAnimation animation = new AlphaAnimation(0.0f, 1.0f);//从全透到不透明
        animation.setDuration(200);
        mRootView.startAnimation(animation);
    }

    private void endOutAnimation() {
        AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);//从不透明到全透明
        animation.setDuration(200);
        mRootView.startAnimation(animation);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(KeyboardEvent event) {
        MyLog.d(TAG, "KeyboardEvent eventType = " + event.eventType);
        switch (event.eventType) {
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_VISIBLE: {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mPlaceHolderView.getLayoutParams();
                float height = DisplayUtils.getScreenHeight() * 0.44f;  // 设置位置刚好在键盘上面一点
                params.height = (int) height;
                mPlaceHolderView.setLayoutParams(params);
                break;
            }
            case KeyboardEvent.EVENT_TYPE_KEYBOARD_HIDDEN: {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) mPlaceHolderView.getLayoutParams();
                params.height = 0;
                mPlaceHolderView.setLayoutParams(params);
                if (mIsShortMode) {
                    mIsShortMode = false;
                }
                break;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        mPresenter.destroy();
    }

    class MyLengthFilter implements InputFilter {

        private final int mMax;
        private Context context;

        public MyLengthFilter(int max, Context context) {
            mMax = max;
            this.context = context;
        }

        public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
                                   int dstart, int dend) {
            int keep = mMax - (dest.length() - (dend - dstart));
            if (keep <= 0) {
                ToastUtils.showToast(context, R.string.report_edittext_hint);
                return "";
            } else if (keep >= end - start) {
                return null; // keep original
            } else {
                keep += start;
                if (Character.isHighSurrogate(source.charAt(keep - 1))) {
                    --keep;
                    if (keep == start) {
                        return "";
                    }
                }
                return source.subSequence(start, keep);
            }
        }
    }

    public static void openFragment(BaseActivity baseActivity, long uuid, String roomId, String liveUrl, String reportPosition, String reportType) {
        openFragment(baseActivity, uuid, roomId, liveUrl, reportPosition, reportType, null);
    }

    public static void openFragment(BaseActivity baseActivity, long uuid, String roomId, String liveUrl, String reportPosition, String reportType, String commentJson) {
        MyLog.w(TAG, "openFragment uid: " + uuid + " roomId: " + roomId + "liveUrl : " + liveUrl + " reportType : " + reportType + " reportPosition : " + reportPosition + " comment json : " + commentJson);
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_UID, uuid);
        bundle.putString(EXTRA_ROOM_ID, roomId);
        bundle.putString(EXTRA_URL, liveUrl);
        bundle.putString(EXTRA_REPORT_POSITION, reportPosition);
        bundle.putString(EXTRA_REPORT_TYPE, reportType);
        if (!TextUtils.isEmpty(commentJson)) {
            bundle.putString(EXTRA_COMMENT_PROOF, commentJson);
        }
        int[] anim = {R.anim.slide_bottom_in, R.anim.slide_bottom_out, R.anim.slide_bottom_in, R.anim.slide_bottom_out};
        FragmentNaviUtils.addFragment(baseActivity, R.id.main_act_container, ReportFragment.class, bundle, true, false, anim, true);
    }

}
