//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.widget;

import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;

import java.util.Map;
import java.util.WeakHashMap;

public abstract class DebouncedOnClickListener implements OnClickListener {
    private static final long DEFAULT_MIN_INTERNAL = 500L;
    private final long mMinInterval;
    private Map<View, Long> mClickMap;

    public DebouncedOnClickListener(long minInterval) {
        this.mMinInterval = minInterval;
        this.mClickMap = new WeakHashMap();
    }

    public DebouncedOnClickListener() {
        this(500L);
    }

    public void onClick(View v) {
        Long lastClickTimestamp = (Long) this.mClickMap.get(v);
        long currentTimestamp = SystemClock.uptimeMillis();
        this.mClickMap.put(v, currentTimestamp);
        if (lastClickTimestamp == null || currentTimestamp - lastClickTimestamp > this.mMinInterval) {
            this.onDebouncedClick(v);
        }

    }

    public abstract void onDebouncedClick(View var1);
}
