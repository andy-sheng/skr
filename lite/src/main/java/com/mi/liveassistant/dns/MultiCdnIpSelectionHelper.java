package com.mi.liveassistant.dns;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LruCache;
import android.support.v4.util.Pair;
import android.text.TextUtils;

import com.mi.liveassistant.common.log.MyLog;
import com.mi.liveassistant.common.network.NetworkUtils;
import com.mi.liveassistant.proto.LiveCommonProto;
import com.xiaomi.broadcaster.dataStruct.RtmpServerInfo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by yangli on 17-1-4.
 *
 * @module 域名解析(多CND)(推流)
 */
public class MultiCdnIpSelectionHelper {
    private static final String TAG = "MultiCdnIpSelectionHelper";

    protected Context mContext;
    private IDnsStatusListener mDnsStatusListener;
    protected boolean mIsStuttering = false; // 当前是否处于卡顿处理
    private boolean mIsNetworkAvail = true;  // 当前网络是否可用

    private Subscription mLocalAndHttpSub;
    private String mNetworkId = PreDnsManager.INSTANCE.getNetworkId();
    private boolean mIsDnsReady = false;

    private List<LiveCommonProto.UpStreamUrl> mOriginalStreamUrlList;
    private final List<CdnItem> mCdnItemList = new LinkedList<>();
    private final LruCache<String, Integer> mIpFlagMap = new LruCache<>(32);

    private List<LiveCommonProto.UpStreamUrl> mSavedOriginalStreamUrlList;
    private final List<LiveCommonProto.UpStreamUrl> mSavedOriginalUdpStreamUrlList = new ArrayList<>();
    private boolean mIsUseUdp = false;   // 当前是否是UDP推流
    private boolean mIsUdpAvail = false; // UDP推流URL是否能成功推流

    public static boolean isEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }

    public final boolean isStuttering() {
        return mIsStuttering;
    }

    public final boolean isDnsReady() {
        return mIsDnsReady;
    }

    private boolean isIpPortEmpty() {
        for (CdnItem cdnItem : mCdnItemList) {
            if (!cdnItem.isIpPortEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean isIpPortFull() {
        for (CdnItem cdnItem : mCdnItemList) {
            if (cdnItem.isIpPortEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void clearAllIpPort() {
        for (CdnItem cdnItem : mCdnItemList) {
            cdnItem.clearIpPortList();
        }
    }

    public final String getOriginalStreamUrl() {
        StringBuilder stringBuilder =  new StringBuilder("");
        if (!isEmpty(mSavedOriginalUdpStreamUrlList)) {
            for (LiveCommonProto.UpStreamUrl upStreamUrl : mSavedOriginalUdpStreamUrlList) {
                if (stringBuilder.length() == 0) {
                    stringBuilder.append(upStreamUrl.getUrl());
                } else {
                    stringBuilder.append(",").append(upStreamUrl.getUrl());
                }
            }
        }
        if (!isEmpty(mSavedOriginalStreamUrlList)) {
            for (LiveCommonProto.UpStreamUrl upStreamUrl : mSavedOriginalStreamUrlList) {
                if (stringBuilder.length() == 0) {
                    stringBuilder.append(upStreamUrl.getUrl());
                } else {
                    stringBuilder.append(",").append(upStreamUrl.getUrl());
                }
            }
        }
        return stringBuilder.toString();
    }

    public final @NonNull RtmpServerInfo[] getRtmpServerInfos() {
        RtmpServerInfo[] rtmpServerInfos = new RtmpServerInfo[mCdnItemList.size()];
        int i = 0;
        for (MultiCdnIpSelectionHelper.CdnItem cdnItem : mCdnItemList) {
            rtmpServerInfos[i++] = cdnItem.toRtmpServerInfo();
        }
        return rtmpServerInfos;
    }

    /**
     * IP解析方式：1-LocalDNS(LocalIp), 2-DnsPod(HttpIp), 3-网宿HttpDns(GuaranteeIp)
     */
    public final int queryIpFlag(String ip) {
        Integer flag = mIpFlagMap.get(ip);
        return flag != null ? flag : PreDnsManager.URL_STATUS_DNS_ERR;
    }

    public MultiCdnIpSelectionHelper(
            @NonNull Context context,
            IDnsStatusListener dnsStatusListener) {
        mContext = context;
        mDnsStatusListener = dnsStatusListener;
    }

    public void onPushStreamSuccess() {
        if (mIsUseUdp && !mIsUdpAvail) { // UDP推流成功，则设置mIsUdpAvail为true
            MyLog.w(TAG, "onPushStreamSuccess udp is available");
            mIsUdpAvail = true;
        }
    }

    public final void updateStutterStatus(boolean isStuttering) {
        if (mIsStuttering != isStuttering) {
            MyLog.w(TAG, "updateStutterStatus " + isStuttering);
            mIsStuttering = isStuttering;
            if (mIsStuttering && mIsUseUdp && !mIsUdpAvail) { // 使用UDP卡顿时，若未推流成功过，则尝试改用TCP推流
                setOriginalStreamUrl(mSavedOriginalStreamUrlList);
            }
        }
    }

    public final void onNetworkStatus(boolean isAvailable, @Nullable String networkId) {
        if (mIsNetworkAvail != isAvailable) { // TODO Yangli 讨论是否插入网络连接状态变化的点
            MyLog.w(TAG, "onNetworkStatus " + isAvailable);
            mIsNetworkAvail = isAvailable;
            if (mIsNetworkAvail) {
                // 网络真正变化时才重新拉取IP
                if (TextUtils.isEmpty(mNetworkId) || TextUtils.isEmpty(networkId) || !mNetworkId.equals(networkId)) {
                    MyLog.w(TAG, "onNetworkStatus mNetworkId" + networkId);
                    mNetworkId = networkId;
                    clearAllIpPort();
                    fetchIpSetForHost(mCdnItemList);
                }
            } else if (mIsStuttering) {
                // 若网络断开，则停止当前卡顿，同时重置卡顿状态
                updateStutterStatus(false);
            }
        }
    }

    /**
     * 该操作应在主线程调用
     */
    public final boolean ipSelect() {
        MyLog.w(TAG, "ipSelect");
        if (!isIpPortFull()) {
            fetchIpSetForHost(mCdnItemList);
            return true;
        }
        return false;
    }

    private void setOriginalStreamUrl(List<LiveCommonProto.UpStreamUrl> originalStreamUrlList) {
        if (isEmpty(originalStreamUrlList)) {
            MyLog.e(TAG, "setOriginalStreamUrl, but originalStreamUrlList is null");
            return;
        }
        mOriginalStreamUrlList = originalStreamUrlList;
        mIsUseUdp = mOriginalStreamUrlList == mSavedOriginalUdpStreamUrlList;
        MyLog.w(TAG, "setOriginalStreamUrl, mIsUseUdp=" + mIsUseUdp);
        mCdnItemList.clear();
        for (LiveCommonProto.UpStreamUrl upStreamUrl : mOriginalStreamUrlList) {
            String streamUrl = upStreamUrl.getUrl();
            String host = PreDnsManager.parseDomainFromUrl(streamUrl);
            String protocol = PreDnsManager.parseProtocolFromUrl(streamUrl);
            if (TextUtils.isEmpty(host) || TextUtils.isEmpty(protocol)) {
                MyLog.w(TAG, "setOriginalStreamUrl, parse host and protocol failed, continue");
                continue;
            }
            CdnItem cdnItem = new CdnItem(streamUrl, host, protocol, upStreamUrl.getWeight());
            PreDnsManager.IpInfo ipInfo = PreDnsManager.INSTANCE.getIpInfoForHostFromPool(host);
            if (ipInfo != null && !ipInfo.isEmpty()) {
                ipInfo.saveIpFlagToMap(mIpFlagMap);
                ipInfo.addPortInfoIfNeed(PreDnsManager.INSTANCE.getPortInfoForHost(host, protocol));
                cdnItem.addIpPortList(ipInfo);
            }
            mCdnItemList.add(cdnItem);
        }
        fetchIpSetForHost(mCdnItemList);
    }

    public void setOriginalStreamUrl(List<LiveCommonProto.UpStreamUrl> originalStreamUrlList, String originalUdpStreamUrl) {
        MyLog.w(TAG, "setOriginalStreamUrl originalStreamUrlList=" + originalStreamUrlList + ", originalUdpStreamUrl=" + originalUdpStreamUrl);
        mSavedOriginalStreamUrlList = originalStreamUrlList;
        mSavedOriginalUdpStreamUrlList.clear();
        if (!TextUtils.isEmpty(originalUdpStreamUrl)) {
            LiveCommonProto.UpStreamUrl upStreamUrl = LiveCommonProto.UpStreamUrl.newBuilder()
                    .setUrl(originalUdpStreamUrl).setWeight(100)
                    .build();
            mSavedOriginalUdpStreamUrlList.add(upStreamUrl);
        }
        if (!isEmpty(mSavedOriginalUdpStreamUrlList)) {
            setOriginalStreamUrl(mSavedOriginalUdpStreamUrlList);
        } else {
            setOriginalStreamUrl(mSavedOriginalStreamUrlList);
        }
    }

    private void onFetchIpSetByHostDone() {
        MyLog.w(TAG, "onFetchIpSetByHostDone mCdnItemList=" + mCdnItemList);
        if (!mIsDnsReady) { // 第一次成功时，通知发送onDnsReady通知
            mIsDnsReady = !isIpPortEmpty();
            if (mIsDnsReady && mDnsStatusListener != null) {
                mDnsStatusListener.onDnsReady();
            }
        }
    }

    /*
     * 通过本地域名解析和Http域名解析获取IP，在IO线程发起请求，在主线程处理请求结果
     */
    protected final void fetchIpSetForHost(final List<CdnItem> cndItemList) {
        if (isEmpty(cndItemList)) {
            MyLog.e(TAG, "fetchIpSetByHost, but cndItemList is null");
            return;
        }
        if (mLocalAndHttpSub != null && !mLocalAndHttpSub.isUnsubscribed()) {
            mLocalAndHttpSub.unsubscribe();
            mLocalAndHttpSub = null;
        }
        if (mContext == null || !NetworkUtils.hasNetwork(mContext)) {
            MyLog.w(TAG, "fetchIpSetForHost, but context is null or network is unavailable, just ignore current call");
            return;
        }
        mLocalAndHttpSub = Observable.from(cndItemList)
                .filter(new Func1<CdnItem, Boolean>() {
                    @Override
                    public Boolean call(CdnItem cdnItem) {
                        return cdnItem == null || cdnItem.isIpPortEmpty();
                    }
                }).map(new Func1<CdnItem, Pair<CdnItem, PreDnsManager.IpInfo>>() {
                    @Override
                    public Pair<CdnItem, PreDnsManager.IpInfo> call(CdnItem cdnItem) {
                        List<String> httpIpSet = PreDnsManager.getHttpDnsIpSet(cdnItem.host);
                        List<String> localIpSet = PreDnsManager.getLocalDnsIpSet(cdnItem.host);
                        if (httpIpSet.isEmpty() && localIpSet.isEmpty()) { // 拉取结果为空，重试一次
                            httpIpSet = PreDnsManager.getHttpDnsIpSet(cdnItem.host);
                            localIpSet = PreDnsManager.getLocalDnsIpSet(cdnItem.host);
                        }
                        localIpSet.removeAll(httpIpSet); // 去重
                        return Pair.create(cdnItem, new PreDnsManager.IpInfo(localIpSet, httpIpSet));
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Pair<CdnItem, PreDnsManager.IpInfo>>() {
                    @Override
                    public void onCompleted() {
                        onFetchIpSetByHostDone();
                    }

                    @Override
                    public void onError(Throwable e) {
                        MyLog.e(TAG, "fetchIpSetForHost failed, exception=" + e);
                        onFetchIpSetByHostDone();
                    }

                    @Override
                    public void onNext(Pair<CdnItem, PreDnsManager.IpInfo> cdnItemIpInfoPair) {
                        CdnItem cdnItem = cdnItemIpInfoPair.first;
                        PreDnsManager.IpInfo ipInfo = cdnItemIpInfoPair.second;
                        if (ipInfo != null && !ipInfo.isEmpty()) {
                            PreDnsManager.INSTANCE.addIpSetToPool(cdnItem.host, ipInfo);
                            ipInfo.saveIpFlagToMap(mIpFlagMap);
                            ipInfo.addPortInfoIfNeed(PreDnsManager.INSTANCE.getPortInfoForHost(cdnItem.host, cdnItem.protocol));
                            cdnItem.addIpPortList(ipInfo);
                        }
                    }
                });
    }

    public void destroy() {
        if (mLocalAndHttpSub != null && !mLocalAndHttpSub.isUnsubscribed()) {
            mLocalAndHttpSub.unsubscribe();
            mLocalAndHttpSub = null;
        }
        mContext = null;
        mDnsStatusListener = null;
    }

    public static class CdnItem {
        private String url;
        private String host;
        private String protocol;
        private final List<String> ipPortList = new ArrayList<>();
        private int weight;

        public CdnItem(
                @NonNull String url,
                @NonNull String host,
                @NonNull String protocol,
                int weight) {
            this.url = url;
            this.host = host;
            this.protocol = protocol;
            this.weight = weight;
        }

        private String[] getIpPortArray() {
            String[] ipPortArray = new String[ipPortList.size()];
            int i = 0;
            for (String ipPort : ipPortList) {
                ipPortArray[i++] = ipPort;
            }
            return ipPortArray;
        }

        public RtmpServerInfo toRtmpServerInfo() {
            RtmpServerInfo rtmpServerInfo = new RtmpServerInfo();
            rtmpServerInfo.rtmpUrl = url;
            rtmpServerInfo.weight = weight;
            rtmpServerInfo.ipPortList = getIpPortArray();
            return rtmpServerInfo;
        }

        public void clearIpPortList() {
            ipPortList.clear();
        }

        public void addIpPortList(PreDnsManager.IpInfo ipInfo) {
            ipPortList.clear();
            if (ipInfo != null) {
                ipPortList.addAll(ipInfo.httpIpSet);
                ipPortList.addAll(ipInfo.localIpSet);
            }
        }

        public boolean isIpPortEmpty() {
            return ipPortList.isEmpty();
        }

        @Override
        public String toString() {
            return "url=" + url + ", ipPortList=" + ipPortList + ", weight=" + weight;
        }
    }
}
