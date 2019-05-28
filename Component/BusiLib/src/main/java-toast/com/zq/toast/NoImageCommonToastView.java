package com.zq.toast;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.component.busilib.R;

public class NoImageCommonToastView extends RelativeLayout {

    ImageView mImage;
    TextView mText;

    private NoImageCommonToastView(Context context) {
        super(context);
        init();
    }

    private NoImageCommonToastView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private NoImageCommonToastView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.no_iamge_common_toast_view, this);

        mText = (TextView) findViewById(R.id.text);
    }

    public static final class Builder {
        NoImageCommonToastView mCommonToastView;
        Context mContext;

        public Builder(Context context) {
            mContext = context;
            mCommonToastView = new NoImageCommonToastView(context);
        }

        public Builder setText(String text) {
            mCommonToastView.mText.setText(text);
            return this;
        }

        public NoImageCommonToastView build() {
            return mCommonToastView;
        }
    }
}
