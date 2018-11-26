//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class RCMessageFrameLayout extends FrameLayout {
    private Drawable mOldDrawable;

    public RCMessageFrameLayout(Context context) {
        super(context);
    }

    public RCMessageFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RCMessageFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setBackgroundResource(int resid) {
        super.setBackgroundResource(resid);
        this.mOldDrawable = this.getBackground();
        this.setBackgroundDrawable((Drawable) null);
        this.setPadding(0, 0, 0, 0);
    }

    public Drawable getBackgroundDrawable() {
        return this.mOldDrawable;
    }
}
