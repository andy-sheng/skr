package com.common.http;

import android.net.Uri;
import android.text.TextUtils;

import com.common.utils.U;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;

/**
 * 域名预解析
 *
 * @author chengsimin
 */
public class Url2IpManager {
    public final static String TAG = "Url2IpManager";

    private Map<String, List<String>> mIpMaps = new ConcurrentHashMap<>();

    private HashSet<String> mFetchingHostMap = new HashSet<>();

    /**
     * 如果有ip就返回ip，如果没有就返回null
     * 不会返回原始url
     *
     * @param originUrl
     * @param index
     * @return
     */
    public String getNextAvailableUrl(String originUrl, int index) {
        Uri uri = Uri.parse(originUrl);
        if (uri == null) {
            return null;
        }
        String host = uri.getHost();
        if (!mIpMaps.containsKey(host)) {
            fetchIpByDnsPodAndLocalDns(host);
            // 什么都没有得话，重试3次就得了，避免死循环。
            return null;
        } else {
            // 如果有ip列表了
            List<String> ips = mIpMaps.get(host);
            if (ips == null || ips.size() <= index) {
                return null;
            }
            return originUrl.replace(host, ips.get(index));
        }

    }


    public void fetchIpByDnsPodAndLocalDns(final String domain) {
        if (!TextUtils.isEmpty(domain) && !mFetchingHostMap.contains(domain)) {
            mFetchingHostMap.add(domain);

            Observable.create(new ObservableOnSubscribe<Object>() {
                @Override
                public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                    List<String> ips = U.getHttpUtils().getAddressByHost(domain);
                    if (ips != null && ips.size() > 0) {
                        mIpMaps.put(domain, ips);
                    }
                    mFetchingHostMap.remove(domain);
                    emitter.onComplete();
                }
            })
                    .subscribeOn(Schedulers.io())
                    .subscribe();
        }
    }

    public void clearBackUpIp() {
        mIpMaps.clear();
        mFetchingHostMap.clear();
    }

}
