package com.wali.live.watchsdk.contest.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wali.live.watchsdk.R;

/**
 * Created by zyh on 2018/1/12.
 *
 * @module 冲顶大会的输入区view, 因为BaseInputArea有飘萍以及管理员弹幕的逻辑，
 * 这里只需要一个简单的输入区域
 */
public class ContestInputView extends LinearLayout implements View.OnClickListener {
    private final String TAG = "ContestInputView";

    private boolean mIsInputMode = false;

    private EditText mInputEt;
    private TextView mSendBtn;

    private InputListener mInputListener;

    public boolean isInputMode() {
        return mIsInputMode;
    }

    public ContestInputView(Context context) {
        this(context, null);
    }

    public ContestInputView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContestInputView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        initView();
    }

    private void initView() {
        inflate(getContext(), R.layout.contest_input_view, this);

        mInputEt = $(R.id.input_et);
        mInputEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String content = mInputEt.getText().toString().trim();
                mSendBtn.setEnabled(!TextUtils.isEmpty(content));
            }
        });

        mSendBtn = $(R.id.send_btn);
        $click(mSendBtn, this);

        // 吃掉点击事件
        setOnClickListener(this);
        setSoundEffectsEnabled(false);
    }

    public void setInputListener(InputListener listener) {
        mInputListener = listener;
    }

    public void showInputView() {
        if (mIsInputMode) {
            return;
        }
        mIsInputMode = true;
        setVisibility(View.VISIBLE);

        mInputEt.requestFocus();
    }

    public void hideInputView() {
        if (!mIsInputMode) {
            return;
        }
        mIsInputMode = false;
        setVisibility(View.INVISIBLE);
    }

    private void sendBarrage() {
        String content = mInputEt.getText().toString().trim();

        if (!TextUtils.isEmpty(content)) {
            mInputEt.setText("");
            if (mInputListener != null) {
                mInputListener.sendBarrage(content);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.send_btn) {
            sendBarrage();
        }
    }

    private final <V extends View> V $(@IdRes int resId) {
        return (V) findViewById(resId);
    }

    private final <V extends View> void $click(V view, View.OnClickListener listener) {
        if (view != null) {
            view.setOnClickListener(listener);
        }
    }

    public interface InputListener {
        void sendBarrage(String msg);
    }
}
