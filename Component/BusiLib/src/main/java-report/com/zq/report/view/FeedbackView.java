package com.zq.report.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.common.view.DebounceViewClickListener;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;
import com.component.busilib.R;
import com.dialog.view.StrokeTextView;

public class FeedbackView extends RelativeLayout {

    public static int FEEDBACK_ERRO = 1;  // 反馈问题
    public static int FEEDBACK_SUGGEST = 2; // 功能建议

    NoLeakEditText mFeedbackContent;
    ExTextView mContentTextSize;
    RadioGroup mButtonArea;
    RadioButton mErrorBack;
    RadioButton mFeedBack;
    StrokeTextView mSubmitTv;

    int mBefore;  // 记录之前的位置
    int mType = FEEDBACK_ERRO;

    Listener mListener;

    public FeedbackView(Context context) {
        super(context);
        init();
    }

    public FeedbackView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FeedbackView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    private void init() {
        inflate(getContext(), R.layout.feedback_view_layout, this);
        mFeedbackContent = (NoLeakEditText) findViewById(R.id.feedback_content);
        mContentTextSize = (ExTextView) findViewById(R.id.content_text_size);
        mButtonArea = (RadioGroup) findViewById(R.id.button_area);
        mErrorBack = (RadioButton) findViewById(R.id.error_back);
        mFeedBack = (RadioButton) findViewById(R.id.feed_back);
        mSubmitTv = (StrokeTextView) findViewById(R.id.submit_tv);


        mButtonArea.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.error_back) {
                    mType = FEEDBACK_ERRO;
                } else if (checkedId == R.id.feed_back) {
                    mType = FEEDBACK_SUGGEST;
                }
            }
        });


        mFeedbackContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mBefore = i;
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                int length = editable.length();
                mContentTextSize.setText("" + length + "/200");
                int selectionEnd = mFeedbackContent.getSelectionEnd();
                if (length > 200) {
                    editable.delete(mBefore, selectionEnd);
                    mFeedbackContent.setText(editable.toString());
                    int selection = editable.length();
                    mFeedbackContent.setSelection(selection);
                }
            }
        });

        mSubmitTv.setOnClickListener(new DebounceViewClickListener() {
            @Override
            public void clickValid(View v) {
                if (mListener != null) {
                    mListener.onClickSubmit(mType, mFeedbackContent.getText().toString().trim());
                }
            }
        });

    }

    public interface Listener {
        void onClickSubmit(int type, String content);
    }
}
