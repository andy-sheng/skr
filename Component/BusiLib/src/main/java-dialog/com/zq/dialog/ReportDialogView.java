package com.zq.dialog;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.RelativeLayout;

import com.common.view.ex.ExTextView;
import com.component.busilib.R;

public class ReportDialogView extends RelativeLayout {

    CheckBox mCheckbox1;
    CheckBox mCheckbox2;
    CheckBox mCheckbox3;
    CheckBox mCheckbox4;
    CheckBox mCheckbox5;
    ExTextView mSubmitTv;

    public ReportDialogView(Context context) {
        super(context);
        init();
    }

    public ReportDialogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ReportDialogView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.report_dialog_view, this);

        mCheckbox1 = (CheckBox) findViewById(R.id.checkbox1);
        mCheckbox2 = (CheckBox) findViewById(R.id.checkbox2);
        mCheckbox3 = (CheckBox) findViewById(R.id.checkbox3);
        mCheckbox4 = (CheckBox) findViewById(R.id.checkbox4);
        mCheckbox5 = (CheckBox) findViewById(R.id.checkbox5);
        mSubmitTv = (ExTextView) findViewById(R.id.submit_tv);
    }

}
