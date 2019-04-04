package com.didichuxing.doraemonkit.kit.logInfo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.common.utils.U;
import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.didichuxing.doraemonkit.DoraemonKit;
import com.didichuxing.doraemonkit.R;
import com.didichuxing.doraemonkit.config.LogInfoConfig;
import com.didichuxing.doraemonkit.ui.base.BaseFragment;
import com.didichuxing.doraemonkit.ui.base.FloatPageManager;
import com.didichuxing.doraemonkit.ui.base.PageIntent;
import com.didichuxing.doraemonkit.ui.setting.SettingItem;
import com.didichuxing.doraemonkit.ui.setting.SettingItemAdapter;
import com.didichuxing.doraemonkit.ui.widget.titlebar.HomeTitleBar;

/**
 * Created by wanglikun on 2018/10/9.
 */

public class LogInfoSettingFragment extends BaseFragment {
    private static final String TAG = "LogInfoSettingFragment";
    private RecyclerView mSettingList;
    private SettingItemAdapter mSettingItemAdapter;

    NoLeakEditText mIdInputEt;
    ExTextView mPullLogBtn;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        HomeTitleBar titleBar = findViewById(R.id.title_bar);
        titleBar.setListener(new HomeTitleBar.OnTitleBarClickListener() {
            @Override
            public void onRightClick() {
                finish();
            }
        });
        mSettingList = findViewById(R.id.setting_list);
        mSettingList.setLayoutManager(new LinearLayoutManager(getContext()));
        mSettingItemAdapter = new SettingItemAdapter(getContext());
        mSettingItemAdapter.append(new SettingItem(R.string.dk_kit_log_info, LogInfoConfig.isLogInfoOpen(getContext())));
        mSettingItemAdapter.setOnSettingItemSwitchListener(new SettingItemAdapter.OnSettingItemSwitchListener() {
            @Override
            public void onSettingItemSwitch(View view, SettingItem data, boolean on) {
                if (data.desc == R.string.dk_kit_log_info) {
                    if (on) {
                        PageIntent intent = new PageIntent(LogInfoFloatPage.class);
                        intent.mode = PageIntent.MODE_SINGLE_INSTANCE;
                        FloatPageManager.getInstance().add(intent);
                    } else {
                        FloatPageManager.getInstance().removeAll(LogInfoFloatPage.class);
                    }
                    LogInfoConfig.setLogInfoOpen(getContext(), on);
                }
            }
        });
        mSettingList.setAdapter(mSettingItemAdapter);

        mIdInputEt = (NoLeakEditText)this.findViewById(R.id.id_input_et);
        mPullLogBtn = (ExTextView)this.findViewById(R.id.pull_log_btn);
        mPullLogBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                DoraemonKit.getExtraInfoProvider().pullLog(mIdInputEt.getText().toString());
            }
        });
    }

    @Override
    protected int onRequestLayout() {
        return R.layout.dk_fragment_log_info_setting;
    }
}