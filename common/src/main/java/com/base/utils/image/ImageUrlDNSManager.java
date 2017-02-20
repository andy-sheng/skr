package com.base.utils.image;

import android.net.Uri;
import android.text.TextUtils;

import com.base.utils.network.NetworkUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * 图片域名预解析
 *
 * @author xionanping
 */
public class ImageUrlDNSManager {

    private final static String TAG = "ImageUrlDNSManager";

    private static Map<String, List<String>> sIpMaps = new HashMap<>();

    private static Map<String, Integer> sHostResolveIngMap = new HashMap<>();

    public static String getAvailableUrl(String originUrl) {
        String host = Uri.parse(originUrl).getHost();
        List<String> ips = null;
        synchronized (sIpMaps) {
            if (sIpMaps.isEmpty() || !sIpMaps.containsKey(host)) {
                fetchIpByDnsPodAndLocalDns(host);
                return originUrl;
            }
            ips = sIpMaps.get(host);
        }
        if (ips == null || ips.size() == 0) {
            return originUrl;
        }
        return originUrl.replace(host, ips.get(0));
    }


    public static String getNextAvailableUrl(String originUrl, int index) {
        String host = Uri.parse(originUrl).getHost();
        List<String> ips = null;
        synchronized (sIpMaps) {
            if (sIpMaps.isEmpty() || !sIpMaps.containsKey(host)) {
                fetchIpByDnsPodAndLocalDns(host);
                return originUrl;
            }
            ips = sIpMaps.get(host);
        }
        if (ips == null || ips.size() == 0 || ips.size() <= index) {
            return null;
        }
        return originUrl.replace(host, ips.get(index));
    }


    public static void fetchIpByDnsPodAndLocalDns(final String domain) {
        if (!TextUtils.isEmpty(domain) && !sHostResolveIngMap.containsKey(domain)) {
            sHostResolveIngMap.put(domain, 1);
            Observable.just("").observeOn(Schedulers.io()).subscribe(new Subscriber<String>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(String s) {
                    List<String> ips = NetworkUtils.getAddressByHost(domain);
                    if (ips != null && ips.size() > 0) {
                        synchronized (sIpMaps) {
                            sIpMaps.put(domain, ips);
                        }
                    }
                    sHostResolveIngMap.remove(domain);
                }
            });
        }
    }

    public static void clearBackUpIp() {
        synchronized (sIpMaps) {
            sIpMaps.clear();
        }
    }


    public static void reFetchIpByNetWorkChange() {
        if (sIpMaps.isEmpty()) {
            return;
        }
        final Map<String, List<String>> ipMapsClone = new HashMap(sIpMaps);

        clearBackUpIp();

        Observable.just("").observeOn(Schedulers.io()).subscribe(new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                for (String domain : ipMapsClone.keySet()) {
                    List<String> ips = NetworkUtils.getAddressByHost(domain);
                    if (ips != null && ips.size() > 0) {
                        synchronized (sIpMaps) {
                            sIpMaps.put(domain, ips);
                        }
                    }
                }
            }
        });
    }
}
