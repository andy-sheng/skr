package com.module.msg.activity;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.common.view.titlebar.CommonTitleBar;

import io.rong.imkit.R;

/**
 * 单聊界面
 */
public class ConversationActivity extends FragmentActivity {

    CommonTitleBar titleBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.conversation_activity);

        initData();
    }

    public void initData() {
        titleBar = findViewById(R.id.titlebar);

        String targetId = getIntent().getData().getQueryParameter("title");
        titleBar.getCenterTextView().setText(targetId);
    }
}
