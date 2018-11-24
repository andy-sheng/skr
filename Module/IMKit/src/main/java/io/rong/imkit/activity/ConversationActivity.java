package io.rong.imkit.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.common.base.BaseActivity;
import com.common.view.titlebar.CommonTitleBar;

import io.rong.imkit.R;

/**
 * 单聊界面
 */
public class ConversationActivity extends BaseActivity {

    CommonTitleBar titleBar;

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.conversation_activity;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        titleBar = findViewById(R.id.titlebar);

        String targetId = getIntent().getData().getQueryParameter("targetId");
        titleBar.getCenterTextView().setText(targetId);
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
