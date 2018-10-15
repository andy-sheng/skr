
package com.base.view;

import android.graphics.Typeface;
import android.text.TextPaint;

import com.base.global.GlobalData;
import com.base.utils.CommonUtils;


public class MLTextPaint extends TextPaint {
    private MLTextView mTv;

    public MLTextPaint(MLTextView textView, TextPaint tp) {
        super(tp);
        mTv = textView;

    }

    @Override
    public void setFakeBoldText(boolean isBold) {
        super.setFakeBoldText(isBold);
        if (isBold) {
            if (CommonUtils.isMIUIRom()) {
                mTv.setTypeface(Typeface.DEFAULT_BOLD);
                return;
            }
        }
    }

}
