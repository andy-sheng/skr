package com.module.msg.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.common.base.BaseActivity;
import com.common.view.titlebar.CommonTitleBar;
import com.jakewharton.rxbinding2.view.RxView;

import java.util.concurrent.TimeUnit;

import io.reactivex.functions.Consumer;
import io.rong.imkit.R;

/**
 * 单聊界面
 */
public class ConversationActivity extends BaseActivity {

    CommonTitleBar mTitleBar;

//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.conversation_activity);
//
//        initData();
//    }
//
//    public void initData() {
//        mTitleBar = findViewById(R.id.titlebar);
//
//        String title = getIntent().getData().getQueryParameter("title");
//        mTitleBar.getCenterTextView().setText(title);
//
//        RxView.clicks(mTitleBar.getLeftTextView())
//                .throttleFirst(500, TimeUnit.MILLISECONDS)
//                .subscribe(new Consumer<Object>() {
//                    @Override
//                    public void accept(Object o) {
//                        finish();
//                    }
//                });
//    }

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.conversation_activity;
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
        mTitleBar = findViewById(R.id.titlebar);

        String title = getIntent().getData().getQueryParameter("title");
        mTitleBar.getCenterTextView().setText(title);

        RxView.clicks(mTitleBar.getLeftTextView())
                .throttleFirst(500, TimeUnit.MILLISECONDS)
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) {
                        finish();
                    }
                });
    }

    @Override
    public boolean useEventBus() {
        return false;
    }
}
