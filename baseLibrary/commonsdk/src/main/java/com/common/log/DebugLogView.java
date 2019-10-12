package com.common.log;

import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewStub;
import android.widget.TextView;

import com.common.base.R;
import com.common.view.DebounceViewClickListener;
import com.common.view.ExViewStub;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class DebugLogView extends ExViewStub {

    TextView mLogView;
    TextView mPlayBtn;
    TextView mClearBtn;
    TextView mColorBtn;
    boolean playing = true;

    public DebugLogView(ViewStub viewStub) {
        super(viewStub);
    }

    @Override
    protected void init(View parentView) {
        mLogView = parentView.findViewById(R.id.log_tv);
        mPlayBtn = parentView.findViewById(R.id.play_btn);
        mPlayBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                playing = !playing;
                if (playing) {
                    mPlayBtn.setText("暂停");
                } else {
                    mPlayBtn.setText("继续");
                }
            }
        });
        mClearBtn = parentView.findViewById(R.id.clear_btn);
        mClearBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                mLogView.setText("");
            }
        });
        mColorBtn = parentView.findViewById(R.id.color_btn);
        mColorBtn.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mColorBtn.getText().equals("白色")) {
                    mColorBtn.setText("红色");
                    mColorBtn.setTextColor(Color.RED);
                    mLogView.setTextColor(Color.RED);
                } else if (mColorBtn.getText().equals("红色")) {
                    mColorBtn.setText("白色");
                    mColorBtn.setTextColor(Color.WHITE);
                    mLogView.setTextColor(Color.WHITE);
                }
            }
        });
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @Override
    protected int layoutDesc() {
        return R.layout.debug_log_view_stub_layout;
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        super.onViewDetachedFromWindow(v);
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(LogEvent event) {
        if (!playing) {
            return;
        }
        String old = mLogView.getText().toString();
        if (old.length() > 1000) {
            old = old.substring(old.length() - 1000, old.length());
        }
        mLogView.setText(old + "\n" + event.getLog());
    }

    /**
     * 这样的日志会打在屏幕上
     */
    public static void println(String TAG, String line) {
        MyLog.d(TAG, line);
        if (MyLog.isDebugLogOpen()) {
            EventBus.getDefault().post(new DebugLogView.LogEvent(TAG, line));
        }
    }

    public static class LogEvent {
        String tag;
        String line;

        public LogEvent(String tag, String line) {
            this.tag = tag;
            this.line = line;
        }

        public String getLog() {
            String log = line;
            if (!TextUtils.isEmpty(tag)) {
                log = tag + ":" + log;
            }
            return log;
        }
    }
}
