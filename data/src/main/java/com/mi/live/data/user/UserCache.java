package com.mi.live.data.user;

import android.util.LruCache;

import com.mi.live.data.user.User;

/**
 * Created by lan on 15/12/28.
 * 目前没有数据库的操作，之后添加
 */
public class UserCache {
    private static final int MAX_CAPACITY = 100;

    private static LruCache<Long, User> sLruCache = new LruCache(MAX_CAPACITY);

    public static User getUser(long userId) {
        return sLruCache.get(userId);
    }

    public static boolean addUser(User user) {
        if (user == null) {
            return false;
        }
        sLruCache.put(user.getUid(), user);
        return true;
    }
}
