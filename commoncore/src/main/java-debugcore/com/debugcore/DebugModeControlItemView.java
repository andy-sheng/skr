package com.debugcore;

import android.content.Context;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.common.base.BuildConfig;
import com.common.core.R;
import com.common.log.MyLog;
import com.common.utils.U;
import com.doraemon.DoraemonManager;
import com.kyleduo.switchbutton.SwitchButton;

public class DebugModeControlItemView extends RelativeLayout {

    TextView mDescTv;
    SwitchButton mSwtichBtn;

    public DebugModeControlItemView(Context context) {
        super(context);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.debug_mode_control_layout, this);
        mDescTv = (TextView) this.findViewById(R.id.desc_tv);
        mSwtichBtn = (SwitchButton) this.findViewById(R.id.swtich_btn);

        if (BuildConfig.DEBUG ) {
            // 调试模式，悬浮球永远都在，无法关闭
            mSwtichBtn.setChecked(true);
            mSwtichBtn.setEnabled(false);
        } else {
            mSwtichBtn.setEnabled(true);
            if (MyLog.getForceOpenFlag()) {
                mSwtichBtn.setChecked(true);
            } else {
                mSwtichBtn.setChecked(false);
            }
        }

        mSwtichBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    MyLog.setForceOpenFlag(true);
                    // 打开
                    DoraemonManager.showFloatIcon();
                } else {
                    MyLog.setForceOpenFlag(false);
                    DoraemonManager.hideFloatIcon();
                }
            }
        });
    }
}
