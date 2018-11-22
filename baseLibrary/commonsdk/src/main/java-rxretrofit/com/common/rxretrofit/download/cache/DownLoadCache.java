package com.common.rxretrofit.download.cache;

import com.common.rxretrofit.download.DownInfo;
import com.common.utils.U;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DownLoadCache {

    private volatile static DownLoadCache INSTANCE;

    private Map<String, DownInfo> caches = new ConcurrentHashMap<>();

    private DownLoadCache() {

    }

    /**
     * 获取单例
     *
     * @return
     */
    public static DownLoadCache getInstance() {
        if (INSTANCE == null) {
            synchronized (DownLoadCache.class) {
                if (INSTANCE == null) {
                    INSTANCE = new DownLoadCache();
                }
            }
        }
        return INSTANCE;
    }

    public void update(DownInfo downInfo) {
        if (downInfo != null && downInfo.getId() != 0) {
            String md5Key = U.getMD5Utils().MD5_32(downInfo.getUrl());
            caches.put(md5Key, downInfo);
        }
    }

    public void save(DownInfo downInfo) {
        if (downInfo != null && downInfo.getId() != 0) {
            String md5Key = U.getMD5Utils().MD5_32(downInfo.getUrl());
            caches.put(md5Key, downInfo);
        }
    }



    public List<DownInfo> queryAll() {
        List<DownInfo> downInfos = new ArrayList<>();

        if (caches != null && caches.size() > 0) {
            for (DownInfo downInfo : caches.values()) {
                downInfos.add(downInfo);
            }
        }
        return downInfos;
    }
}
