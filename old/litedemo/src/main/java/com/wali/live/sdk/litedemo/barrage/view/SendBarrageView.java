package com.wali.live.sdk.litedemo.barrage.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.wali.live.sdk.litedemo.R;
import com.wali.live.sdk.litedemo.utils.ToastUtils;

/**
 * Created by lan on 17/5/4.
 */
public class SendBarrageView extends RelativeLayout {
    private EditText mBarrageEt;
    private Button mSendBtn;

    private ISendCallback mCallback;

    public SendBarrageView(Context context) {
        super(context);
        init();
    }

    public SendBarrageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SendBarrageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected <V extends View> V $(int id) {
        return (V) findViewById(id);
    }

    private void init() {
        inflate(getContext(), R.layout.send_barrage_view, this);

        mBarrageEt = $(R.id.barrage_et);
        mSendBtn = $(R.id.send_btn);

        mSendBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = mBarrageEt.getText().toString();
                ToastUtils.showToast(message);
                if (!TextUtils.isEmpty(message)) {
                    mBarrageEt.setText("");
                    if (mCallback != null) {
                        mCallback.send(message);
                    }
                }
            }
        });
    }

    public void setCallback(ISendCallback callback) {
        mCallback = callback;
    }

    public interface ISendCallback {
        void send(String message);
    }
}
