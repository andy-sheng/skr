package com.common.view;

import android.view.View;

public abstract class DebounceViewClickListener implements View.OnClickListener {
    static long sLastClickTs = 0;

    @Override
    public void onClick(View v) {
        /**
         * 防止快速点击 或者 同时按多个按钮的问题
         */
        if (System.currentTimeMillis() - sLastClickTs < 500) {
            return;
        }
        sLastClickTs = System.currentTimeMillis();
        clickValid(v);
    }

    public abstract void clickValid(View v);
}
