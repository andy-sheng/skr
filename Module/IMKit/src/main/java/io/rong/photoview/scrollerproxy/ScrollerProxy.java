//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.photoview.scrollerproxy;

import android.content.Context;
import android.os.Build.VERSION;

import io.rong.photoview.scrollerproxy.GingerScroller;
import io.rong.photoview.scrollerproxy.IcsScroller;
import io.rong.photoview.scrollerproxy.PreGingerScroller;

public abstract class ScrollerProxy {
    public ScrollerProxy() {
    }

    public static io.rong.photoview.scrollerproxy.ScrollerProxy getScroller(Context context) {
        if (VERSION.SDK_INT < 9) {
            return new PreGingerScroller(context);
        } else {
            return (io.rong.photoview.scrollerproxy.ScrollerProxy) (VERSION.SDK_INT < 14 ? new GingerScroller(context) : new IcsScroller(context));
        }
    }

    public abstract boolean computeScrollOffset();

    public abstract void fling(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10);

    public abstract void forceFinished(boolean var1);

    public abstract boolean isFinished();

    public abstract int getCurrX();

    public abstract int getCurrY();
}
