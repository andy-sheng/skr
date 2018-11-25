//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import io.rong.imkit.emoticon.AndroidEmoji;

public class SpannableTextView extends TextView {
  public SpannableTextView(Context context) {
    super(context);
  }

  public SpannableTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  public SpannableTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  public void setText(CharSequence text, BufferType type) {
    if (text == null) {
      text = "";
    }

    SpannableStringBuilder spannable = new SpannableStringBuilder((CharSequence)text);
    AndroidEmoji.ensure(spannable);
    super.setText(spannable, type);
  }

  public void setText(String text) {
    if (TextUtils.isEmpty(text)) {
      this.setText("", BufferType.SPANNABLE);
    } else {
      this.setText(text, BufferType.SPANNABLE);
    }

  }
}
