package com.wali.live.common.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.live.module.common.R;

/**
 * Created by linjinbin on 16/2/23.
 */
public class ErrorView extends RelativeLayout {
    TextView mErrorTipTv;
    TextView mRetryTv;

    public ErrorView(Context context) {
        super(context);
        init(context, null);
    }

    public ErrorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ErrorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.error_view, this);

        mErrorTipTv = (TextView) findViewById(R.id.error_tips_tv);
        mRetryTv = (TextView) findViewById(R.id.error_retry);
    }


    public void setRetryOnClickListener(OnClickListener clickListener) {
        mRetryTv.setOnClickListener(clickListener);
    }

    public void setErrorTips(String errorTips) {
        mErrorTipTv.setText(errorTips);
    }

    public TextView getRetryTv() {
        return mRetryTv;
    }
}
