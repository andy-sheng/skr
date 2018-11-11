package com.common.utils;

import com.common.cache.IntelligentCache;

//将 ConfigModule 的实现类的集合存放到缓存 Cache, 可以随时获取
//使用 IntelligentCache.KEY_KEEP 作为 key 的前缀, 可以使储存的数据永久存储在内存中
//否则存储在 LRU 算法的存储空间中 (大于或等于缓存所能允许的最大 size, 则会根据 LRU 算法清除之前的条目)
//前提是 extras 使用的是 IntelligentCache (框架默认使用)
public class CacheUtils {
    IntelligentCache<Object> cache = new IntelligentCache<Object>(100);

    CacheUtils() {

    }

    public void putToLRU(String key, Object value) {
        cache.put(key, value);
    }

    public Object getFromLRU(String key) {
        return cache.get(key);
    }

    public void putToKeep(String key, Object value) {
        cache.put(IntelligentCache.KEY_KEEP + key, value);
    }

    public Object getFromKeep(String key) {
        return cache.get(IntelligentCache.KEY_KEEP + key);
    }

    public Object removeFromKeep(String key) {
        return cache.remove(IntelligentCache.KEY_KEEP + key);
    }

    public boolean containsKeyInKeep(String key) {
        return cache.containsKey(IntelligentCache.KEY_KEEP + key);
    }
}
