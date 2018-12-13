package com.module.home.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.common.utils.U;
import com.common.view.ex.ExButton;
import com.common.view.ex.ExTextView;
import com.module.home.R;

public class PermissionTipsView extends RelativeLayout {

    ExTextView mTipsTv;
    ExButton mGoSettion;

    public PermissionTipsView(Context context) {
        super(context);
        init();
    }

    public PermissionTipsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.permission_tips_view_layout, this);
        mTipsTv = (ExTextView) this.findViewById(R.id.tips_tv);
        mGoSettion = (ExButton) this.findViewById(R.id.go_setting);
        mTipsTv.setText("请开启存储读写，录音等权限，保证应用正常使用");
        mGoSettion.setText("去设置");
        mGoSettion.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                U.getPermissionUtils().goToPermissionManager((Activity) getContext());
            }
        });
    }
}
