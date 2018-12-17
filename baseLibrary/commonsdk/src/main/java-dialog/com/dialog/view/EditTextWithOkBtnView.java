package com.dialog.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.base.R;
import com.common.utils.U;
import com.common.view.ex.ExTextView;
import com.common.view.ex.NoLeakEditText;

public class EditTextWithOkBtnView extends RelativeLayout {

    NoLeakEditText mContentEt;
    ExTextView mOkBtn;

    public EditTextWithOkBtnView(Context context) {
        super(context);
        init();
    }

    public EditTextWithOkBtnView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EditTextWithOkBtnView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.template_edittext1, this);
        mContentEt = (NoLeakEditText) this.findViewById(R.id.content_et);
        mOkBtn = (ExTextView) this.findViewById(R.id.ok_btn);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mContentEt.requestFocus();
        U.getKeyBoardUtils().showSoftInputKeyBoard(getContext(), mContentEt);
    }

    public NoLeakEditText getContentEt() {
        return mContentEt;
    }

    public ExTextView getOkBtn() {
        return mOkBtn;
    }
}
