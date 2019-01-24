package com.common.core.upgrade;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.core.R;
import com.common.view.ex.ExTextView;

public class TipsInstallView extends RelativeLayout {
    ExTextView mInstallBtn;

    Listener mListener;

    public TipsInstallView(Context context) {
        super(context);
        init();
    }

    public TipsInstallView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public TipsInstallView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        inflate(getContext(), R.layout.tips_install_view_layout, this);
        mInstallBtn = (ExTextView) this.findViewById(R.id.install_btn);
        mInstallBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onInstallBtnClick();
                }
            }
        });
    }

    void setListener(Listener l) {
        mListener = l;
    }


    public interface Listener {
        void onInstallBtnClick();
    }
}
