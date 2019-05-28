package com.dialog.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.common.base.R;
import com.common.view.ex.ExTextView;

public class ProgressDialogView extends RelativeLayout {


    ProgressBar mProgressBar;


    public ProgressDialogView(Context context) {
        super(context);
        init();
    }

    public ProgressDialogView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProgressDialogView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        inflate(getContext(), R.layout.template_progress1_dialog, this);
        mProgressBar = (ProgressBar) this.findViewById(R.id.progressBar);
    }

}
