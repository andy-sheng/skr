package com.wali.live.common.smiley;

import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.widget.EditText;


/**
 * Created by anping on 16-3-22.
 */
public class SmileyInputFilter implements InputFilter {

    public int charSize = 0;
    public EditText editText;

    public SmileyInputFilter(EditText editText, int charSize) {
        this.charSize = charSize;
        this.editText = editText;
    }

    @Override
    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

        if (charSize <= 0 || editText == null) {
            return null;
        }

        if(start ==  0 && end == 0 ){ // 在删除不做判断
            return null;
        }

        int size = countChatSize(editText.getText());

        if (size >= charSize) { //输入框的数据已经到了最大
            return "";
        }

        int sourceLength = countChatSize(source);

        if(charSize - size < sourceLength){
            return source.subSequence(0, charSize - size>source.length()?source.length():(charSize -size));
        }

        return null;
    }


    private int countChatSize(CharSequence charSequence){
        SpannableString data= new SpannableString(charSequence);
        int length = data.length();

        if(length>=2) {
            SmileyParser.SmileySpan[] smileySpans = data.getSpans(0, data.length(), SmileyParser.SmileySpan.class);
            if (smileySpans != null && smileySpans.length > 0) {
                int spanLength = 0;
                for (SmileyParser.SmileySpan smileySpan : smileySpans) {
                    spanLength = data.getSpanEnd(smileySpan) - data.getSpanStart(smileySpan);
                    length = length - spanLength + 1;
                    data.removeSpan(smileySpan);
                }
            }
        }

        for(int i=0;i<data.length();i++){
            if((data.charAt(i) >= 0x4e00)&&(data.charAt(i) <= 0x9fbb)) {
                length=length+1;
            }
        }
        return length;
    }
}
