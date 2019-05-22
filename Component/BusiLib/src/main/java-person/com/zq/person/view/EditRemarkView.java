package com.zq.person.view;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.view.DebounceViewClickListener;
import com.common.view.ex.NoLeakEditText;
import com.component.busilib.R;
import com.dialog.view.StrokeTextView;

public class EditRemarkView extends RelativeLayout {

    String mRemarkName;
    Listener mListener;

    NoLeakEditText mRemarkNameEdt;
    StrokeTextView mCancelTv;
    StrokeTextView mSaveTv;

    public EditRemarkView(Context context, String remarkName) {
        super(context);
        this.mRemarkName = remarkName;
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.remark_edit_view_layout, this);

        mRemarkNameEdt = (NoLeakEditText) findViewById(R.id.remark_name_edt);
        mCancelTv = (StrokeTextView) findViewById(R.id.cancel_tv);
        mSaveTv = (StrokeTextView) findViewById(R.id.save_tv);

        if (!TextUtils.isEmpty(mRemarkName)) {
            mRemarkNameEdt.setHint("" + mRemarkName);
        }

        mCancelTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onClickCancel();
                }
            }
        });

        mSaveTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                String mRemarkName = mRemarkNameEdt.getText().toString().trim();
                if (mListener != null) {
                    mListener.onClickSave(mRemarkName);
                }
            }
        });

        mRemarkNameEdt.postDelayed(new Runnable() {
            @Override
            public void run() {
                mRemarkNameEdt.requestFocus();
            }
        }, 500);

    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void onClickCancel();

        void onClickSave(String remarkName);
    }

}
