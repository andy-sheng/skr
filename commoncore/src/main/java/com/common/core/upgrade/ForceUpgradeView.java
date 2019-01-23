package com.common.core.upgrade;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.core.R;
import com.common.view.ex.ExTextView;

public class ForceUpgradeView extends RelativeLayout {
    ExTextView mInfoTv;
    ExTextView mUpdateBtn;
    ExTextView mInstallBtn;

    Listener mListener;

    public ForceUpgradeView(Context context) {
        super(context);
        init();
    }

    public ForceUpgradeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ForceUpgradeView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


    private void init() {
        inflate(getContext(), R.layout.force_upgrade_view_layout, this);
        mInfoTv = (ExTextView) this.findViewById(R.id.info_tv);
        mUpdateBtn = (ExTextView) this.findViewById(R.id.update_btn);
        mUpdateBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onUpdateBtnClick();
                }
            }
        });
        mInstallBtn = (ExTextView)this.findViewById(R.id.install_btn);
        mInstallBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onInstallBtnClick();
                }
            }
        });
    }

    void setListener(Listener l){
        mListener = l;
    }

    public void updateProgress(int arg1) {
        if (mInfoTv != null) {
            mInfoTv.setText(arg1+"进度");
        }
    }

    public interface Listener{
        void onUpdateBtnClick();
        void onInstallBtnClick();
    }
}
