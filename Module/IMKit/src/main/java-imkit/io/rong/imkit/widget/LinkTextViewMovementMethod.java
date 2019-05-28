//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget;

import android.text.Layout;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.text.method.Touch;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.TextView;

import io.rong.imkit.widget.ILinkClickListener;

public class LinkTextViewMovementMethod extends LinkMovementMethod {
    private long mLastActionDownTime;
    private ILinkClickListener mListener;

    public LinkTextViewMovementMethod(ILinkClickListener listener) {
        this.mListener = listener;
    }

    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
        int action = event.getAction();
        if (action != 1 && action != 0) {
            return Touch.onTouchEvent(widget, buffer, event);
        } else {
            int x = (int) event.getX();
            int y = (int) event.getY();
            x -= widget.getTotalPaddingLeft();
            y -= widget.getTotalPaddingTop();
            x += widget.getScrollX();
            y += widget.getScrollY();
            Layout layout = widget.getLayout();
            int line = layout.getLineForVertical(y);
            int off = layout.getOffsetForHorizontal(line, (float) x);
            ClickableSpan[] link = (ClickableSpan[]) buffer.getSpans(off, off, ClickableSpan.class);
            if (link.length != 0) {
                if (action == 1) {
                    long actionUpTime = System.currentTimeMillis();
                    if (actionUpTime - this.mLastActionDownTime > (long) ViewConfiguration.getLongPressTimeout()) {
                        return true;
                    }

                    String url = null;
                    if (link[0] instanceof URLSpan) {
                        url = ((URLSpan) link[0]).getURL();
                    }

                    if (this.mListener != null && this.mListener.onLinkClick(url)) {
                        return true;
                    }

                    link[0].onClick(widget);
                } else if (action == 0) {
                    this.mLastActionDownTime = System.currentTimeMillis();
                }

                return true;
            } else {
                Touch.onTouchEvent(widget, buffer, event);
                return false;
            }
        }
    }
}
