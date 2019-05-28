package com.didichuxing.doraemonkit.kit.logInfo;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.common.rxretrofit.ApiManager;
import com.common.utils.U;
import com.didichuxing.doraemonkit.R;
import com.didichuxing.doraemonkit.ui.base.BaseFloatPage;
import com.didichuxing.doraemonkit.ui.loginfo.LogItemAdapter;
import com.didichuxing.doraemonkit.ui.widget.titlebar.TitleBar;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by wanglikun on 2018/10/9.
 */

public class LogInfoFloatPage extends BaseFloatPage implements LogInfoManager.OnLogCatchListener {
    private static final String TAG = "LogInfoFloatPage";

    private static final int MAX_LOG_LINE_NUM = 200;

    private RecyclerView mLogList;
    private LogItemAdapter mLogItemAdapter;
    private EditText mLogFilter;
    private RadioGroup mRadioGroup;
    private TitleBar mTitleBar;
    private List<LogInfoItem> mLogInfoItems = new ArrayList<>();

    private WindowManager mWindowManager;
    private TextView mLogHint;
    private View mLogPage;

    private HashSet<String> mTagSet = new HashSet<>();
    private int mLevel = Log.VERBOSE;

    @Override
    protected void onCreate(Context context) {
        super.onCreate(context);
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LogInfoManager.getInstance().registerListener(this);
        LogInfoManager.getInstance().start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LogInfoManager.getInstance().stop();
        LogInfoManager.getInstance().removeListener();
        mLogInfoItems.clear();
        mLogInfoItems = null;
    }

    @Override
    protected View onCreateView(Context context, ViewGroup view) {
        return LayoutInflater.from(context).inflate(R.layout.dk_float_log_info, null);
    }

    @Override
    protected void onViewCreated(View view) {
        super.onViewCreated(view);
        initView();
    }

    public void initView() {
        mLogHint = findViewById(R.id.log_hint);
        mLogPage = findViewById(R.id.log_page);
        mLogList = findViewById(R.id.log_list);
        mLogList.setLayoutManager(new LinearLayoutManager(getContext()));
        mLogItemAdapter = new LogItemAdapter(getContext());
        mLogList.setAdapter(mLogItemAdapter);
        mLogFilter = findViewById(R.id.log_filter);
        mLogFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(mLogFilter.getText())) {
                    CharSequence filter = mLogFilter.getText();
                    List<LogInfoItem> infoItems = new ArrayList<>();
                    for (LogInfoItem item : mLogInfoItems) {
                        if (item.orginalLog.contains(filter)) {
                            infoItems.add(item);
                        }
                    }
                    mLogItemAdapter.clear();
                    mLogItemAdapter.setData(infoItems);
                } else {
                    mLogItemAdapter.clear();
                    mLogItemAdapter.setData(mLogInfoItems);
                }
            }
        });
        mTitleBar = findViewById(R.id.title_bar);
        mTitleBar.setOnTitleBarClickListener(new TitleBar.OnTitleBarClickListener() {
            @Override
            public void onLeftClick() {
                hidePage();
            }

            @Override
            public void onRightClick() {

            }
        });
        mLogHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPage();
            }
        });
        mRadioGroup = findViewById(R.id.radio_group);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.verbose) {
                    mLevel = Log.VERBOSE;
                } else if (checkedId == R.id.debug) {
                    mLevel = Log.DEBUG;
                } else if (checkedId == R.id.info) {
                    mLevel = Log.INFO;
                } else if (checkedId == R.id.warn) {
                    mLevel = Log.WARN;
                } else if (checkedId == R.id.error) {
                    mLevel = Log.ERROR;
                }
                List<LogInfoItem> infoItems = getListItem();
                mLogItemAdapter.clear();
                mLogItemAdapter.setData(infoItems);
            }
        });
        mRadioGroup.check(R.id.verbose);

        LinearLayout tagContainer = findViewById(R.id.tag_container);

        {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText("服务器Api");
            final String[] arrs = new String[]{
                    ApiManager.TAG
            };
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        addTagSets(arrs);
                    } else {
                        removeTagSets(arrs);
                    }
                    List<LogInfoItem> infoItems = getListItem();
                    mLogItemAdapter.clear();
                    mLogItemAdapter.setData(infoItems);
                }
            });
            tagContainer.addView(checkBox);
        }

        {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText("融云相关");
            final String[] arrs = new String[]{
                    "RC:",
                    "Rong"
            };
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        addTagSets(arrs);
                    } else {
                        removeTagSets(arrs);
                    }
                    List<LogInfoItem> infoItems = getListItem();
                    mLogItemAdapter.clear();
                    mLogItemAdapter.setData(infoItems);
                }
            });
            tagContainer.addView(checkBox);
        }

        {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText("播放器");
            final String[] arrs = new String[]{
                    "Player"
            };
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        addTagSets(arrs);
                    } else {
                        removeTagSets(arrs);
                    }
                    List<LogInfoItem> infoItems = getListItem();
                    mLogItemAdapter.clear();
                    mLogItemAdapter.setData(infoItems);
                }
            });
            tagContainer.addView(checkBox);
        }
    }

    void addTagSets(String[] arrs) {
        StringBuilder sb = new StringBuilder();
        for (String tag : arrs) {
            mTagSet.add(tag);
            sb.append(tag).append(" ");
        }
        U.getToastUtil().showShort("过滤" + sb.toString());
    }

    void removeTagSets(String[] arrs) {
        for (String tag : arrs) {
            mTagSet.remove(tag);
        }
    }

    List<LogInfoItem> getListItem() {
        List<LogInfoItem> infoItems = new ArrayList<>();
        for (LogInfoItem infoItem : mLogInfoItems) {
            if (accept(infoItem)) {
                infoItems.add(infoItem);
            }
        }
        return infoItems;
    }

    boolean accept(LogInfoItem infoItem) {
        if (infoItem.level >= mLevel) {
            boolean goOn = mTagSet.isEmpty();
            for (String tag : mTagSet) {
                if (infoItem.orginalLog.contains(tag)) {
                    goOn = true;
                    break;
                }
            }
            if (!goOn) {
                return false;
            }
            if (!TextUtils.isEmpty(mLogFilter.getText())) {
                CharSequence filter = mLogFilter.getText();
                if (infoItem.orginalLog.contains(filter)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return true;
            }
        }
        return false;
    }

    boolean accept2(LogInfoItem infoItem) {
        if (infoItem.level >= mLevel) {
            if (infoItem.tag != null && infoItem.tag.equals("ApiManager")) {
                if (infoItem.meseage == null) {
                    return false;
                }
                if (infoItem.meseage.contains("http")) {
                    return true;
                }
                if (infoItem.meseage.contains("traceId")) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onLayoutParamsCreated(WindowManager.LayoutParams params) {
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
    }

    @Override
    public void onLogCatch(LogInfoItem infoItem) {
        if (mLogList == null || mLogItemAdapter == null) {
            return;
        }
        if (accept2(infoItem)) {
            mLogInfoItems.add(infoItem);
            if (mLogInfoItems.size() >= MAX_LOG_LINE_NUM) {
                mLogInfoItems.remove(0);
            }
            mLogItemAdapter.append(infoItem);
            if (mLogItemAdapter.getItemCount() >= MAX_LOG_LINE_NUM) {
                mLogItemAdapter.remove(0);
            }
        }
    }

    private void hidePage() {
        final WindowManager.LayoutParams layoutParams = getLayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.gravity = Gravity.START | Gravity.TOP;
        mLogHint.setVisibility(View.VISIBLE);
        mLogPage.setVisibility(View.GONE);
        mWindowManager.updateViewLayout(getRootView(), layoutParams);
    }

    private void showPage() {
        final WindowManager.LayoutParams layoutParams = getLayoutParams();
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.gravity = Gravity.START | Gravity.TOP;
        mLogHint.setVisibility(View.GONE);
        mLogPage.setVisibility(View.VISIBLE);
        mWindowManager.updateViewLayout(getRootView(), layoutParams);
    }

    @Override
    protected boolean onBackPressed() {
        if (U.getKeyBoardUtils().isSoftKeyboardShowing(U.getActivityUtils().getTopActivity())) {
            U.getKeyBoardUtils().hideSoftInputKeyBoard(U.getActivityUtils().getTopActivity());
            return true;
        }
        hidePage();
        return true;
    }

    @Override
    public void onEnterForeground() {
        super.onEnterForeground();
        getRootView().setVisibility(View.VISIBLE);
    }

    @Override
    public void onEnterBackground() {
        super.onEnterBackground();
        getRootView().setVisibility(View.GONE);
    }
}