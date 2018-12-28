package com.zq.toast;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.component.busilib.R;

public class CommonToastView extends RelativeLayout {

    ImageView mImage;
    TextView mText;

    private CommonToastView(Context context) {
        super(context);
        init();
    }

    private CommonToastView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private CommonToastView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.common_toast_view, this);

        mImage = (ImageView) findViewById(R.id.image);
        mText = (TextView) findViewById(R.id.text);
    }

    public static final class Builder {
        CommonToastView mCommonToastView;
        Context mContext;

        public Builder(Context context) {
            mContext = context;
            mCommonToastView = new CommonToastView(context);
        }

        public Builder setImage(int resId) {
            mCommonToastView.mImage.setImageDrawable(ContextCompat.getDrawable(mContext, resId));
            return this;
        }

        public Builder setText(String text) {
            mCommonToastView.mText.setText(text);
            return this;
        }

        public CommonToastView build() {
            return mCommonToastView;
        }
    }
}
