//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package io.rong.imkit.cache;

import io.rong.imkit.RongContext;

public abstract class RongCacheWrap<K, V> extends RongCache<K, V> {
    RongContext mContext;
    boolean mIsSync = false;

    public RongCacheWrap(RongContext context, int maxSize) {
        super(maxSize);
        this.mContext = context;
    }

    public boolean isIsSync() {
        return this.mIsSync;
    }

    public void setIsSync(boolean isSync) {
        this.mIsSync = isSync;
    }

    protected V create(K key) {
        if (key == null) {
            return null;
        } else if (!this.mIsSync) {
            this.executeCacheProvider(key);
            return super.create(key);
        } else {
            return this.obtainValue(key);
        }
    }

    protected RongContext getContext() {
        return this.mContext;
    }

    public void executeCacheProvider(final K key) {
        this.mContext.executorBackground(new Runnable() {
            public void run() {
                RongCacheWrap.this.obtainValue(key);
            }
        });
    }

    public abstract V obtainValue(K var1);
}
