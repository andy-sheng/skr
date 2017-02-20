package com.wali.live.common.smiley;

import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;

import com.base.global.GlobalData;


/**
 * Created by anping on 16-6-27.
 */
public class SmileyTranslateFilter implements InputFilter {

    private float textSize;


    public SmileyTranslateFilter(float textSize) {
        this.textSize = textSize;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        String sourceAsString   = String.valueOf(source);
        if(TextUtils.isEmpty(sourceAsString) || sourceAsString.length() < 2 || !sourceAsString.contains("【")  || !sourceAsString.contains("】")){
                return null;
        }

        SpannableString spannableString = new SpannableString(source);
        SmileyParser.SmileySpan[] spans = spannableString.getSpans(0, spannableString.length(), SmileyParser.SmileySpan.class);
        if (spans == null || spans.length == 0) {
            CharSequence data = SmileyParser.getInstance().addSmileySpans(GlobalData.app(),
                    source,
                    textSize, true, false, true);
            return data;
        }
        return null;
    }
}
