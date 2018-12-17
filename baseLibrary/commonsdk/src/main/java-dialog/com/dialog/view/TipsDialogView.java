package com.dialog.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.common.base.R;
import com.common.view.ex.ExTextView;

public class TipsDialogView extends RelativeLayout {

    ExTextView mMessageTv;
    ExTextView mOkBtn;

    public TipsDialogView(Context context) {
        super(context);
        init();
    }

    public TipsDialogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TipsDialogView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.template_tips1_dialog, this);
        mMessageTv = (ExTextView) this.findViewById(R.id.message_tv);
        mOkBtn = (ExTextView) this.findViewById(R.id.ok_btn);
    }
}
