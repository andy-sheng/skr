package com.wali.live.ipselect;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.utils.network.NetworkUtils;
import com.mi.live.data.account.MyUserInfoManager;
import com.mi.live.data.milink.MiLinkClientAdapter;
import com.mi.live.data.report.ReportProtocol;
import com.mi.live.data.report.keyflow.KeyFlowReportManager;
import com.wali.live.dns.IDnsStatusListener;
import com.wali.live.dns.IStreamUrl;
import com.wali.live.dns.PreDnsManager;
import com.wali.live.utils.AppNetworkUtils;
import com.wali.live.video.manager.ReportManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by yangli on 16-10-27.
 * <p>
 * 域名解析辅助类，实现了IStreamUrl接口，该类具有两个子类：LiveIpSelectionHelper(推流)/WatchIpSelectionHelper(拉流)
 * 该类持有原始URL(没进行域名解析的URL，例如：rtmp://r2.zb.mi.com/live/100067_1478512224)。
 * 接口简介：
 *
 * @func setOriginalStreamUrl 设置原始URL，用于获取域名及拼接URL
 * @func ipSelect 进行IP替换，若当前没有可用IP，会自动进行域名解析拉取IP
 * @func getStreamUrl IP替换之后的URL，若无可用IP，则返回原始URL
 * @func getSelectedIpList 返回设置给引擎的IP列表
 * @func fetchIpSetByHost 进行域名解析，拉取新的IP列表
 * @module 域名解析
 */
public abstract class BaseIpSelectionHelper implements IStreamUrl {
    private final String TAG = getTAG();

    protected int mLocalIpIndex = 0;
    protected int mHttpIpIndex = 0;
    protected final List<String> mLocalIpSet = new ArrayList<>();
    protected final List<String> mHttpIpSet = new ArrayList<>();
    private final List<String> mGuaranteeIpSet = new ArrayList<>();
    private boolean mIsDnsReady = false;

    protected Context mContext;
    protected String mOriginalStreamUrl = ""; // 服务端下发的推/拉流原始地址，包含有域名
    protected String mProtocol = ""; // 协议
    protected String mHost = ""; // 域名
    private long mUserId = MyUserInfoManager.getInstance().getUser().getUid();
    private Subscription mLocalAndHttpSub;
    private Subscription mGuaranteeSub;
    private IDnsStatusListener mDnsStatusListener;
    private boolean mNeedUseCache = true;

    protected String mStreamUrl = "";
    protected String mSelectedIp = "";
    protected List<String> mSelectedIpList;
    protected List<String> mSelectedHttpIpList;
    protected List<String> mSelectedLocalIpList;
    protected boolean mIsStuttering = false; // 当前是否处于卡顿处理
    private boolean mIsNetworkAvail = true;  // 当前网络是否可用
    protected int mFetchGuaranteeIpCnt = 0;  // 拉取保底IP的次数，用于判断是否需要降低码率

    private KeyFlowReportManager mKeyFlowReporter;

    public abstract String getTAG();

    protected abstract String generateUrlForGuaranteeIp(String host, String originalStreamUrl);

    public BaseIpSelectionHelper(
            @NonNull Context context,
            IDnsStatusListener dnsStatusListener,
            KeyFlowReportManager keyFlowReporter) {
        mContext = context;
        mDnsStatusListener = dnsStatusListener;
        mKeyFlowReporter = keyFlowReporter;
    }

    public final String getHost() {
        return mHost;
    }

    public String getStreamHost() { // 供拉流调用，子类可能重载该操作，若要保证拿到host，请调用getHost
        return mHost;
    }

    @Override
    public final String getStreamUrl() {
        return mStreamUrl;
    }

    @Override
    public final String getSelectedIp() {
        return mSelectedIp;
    }

    public final List<String> getSelectedIpList() {
        return mSelectedIpList;
    }

    public final List<String> getSelectedHttpIpList() {
        return mSelectedHttpIpList;
    }

    public final List<String> getSelectedLocalIpList() {
        return mSelectedLocalIpList;
    }

    private final String getProtocol(@NonNull String originalStreamUrl) {
        int pos = originalStreamUrl.indexOf("://");
        if (pos != -1) {
            return originalStreamUrl.substring(0, pos);
        }
        return "";
    }

    protected List<String> generatePortList() {
        return PreDnsManager.INSTANCE.getPortInfoForHost(mHost, mProtocol);
    }

    /**
     * 该操作应在主线程调用
     */
    @Override
    public final boolean ipSelect() {
        if (!NetworkUtils.hasNetwork(mContext)) {
            MyLog.w(TAG, "ipSelect, but network is unavailable, just ignore current call");
            return false;
        }
        PreDnsManager.IpInfo ipInfo = queryNewIpSet();
        if (!ipInfo.isEmpty()) {
            ipInfo.addPortInfoIfNeed(generatePortList());
            mSelectedHttpIpList = ipInfo.httpIpSet;
            mSelectedLocalIpList = ipInfo.localIpSet;
            mSelectedIpList = ipInfo.getIpList();
            mSelectedIp = mSelectedIpList.get(0);
            mStreamUrl = generateUrlForIp(mOriginalStreamUrl, mHost, mSelectedIp);
            if (mKeyFlowReporter != null) {
                mKeyFlowReporter.insertIp(mSelectedIp, queryIpFlag(mSelectedIp), System.currentTimeMillis());
            }
            MyLog.w(TAG, "ipSelect new mStreamUrl=" + mStreamUrl + ", mSelectedIpList=" + mSelectedIpList);
            return true;
        } else {
            MyLog.w(TAG, "ipSelect failed, use previous result, mStreamUrl=" + mStreamUrl + ", mSelectedIpList=" + mSelectedIpList);
        }
        return false;
    }

    @Override
    public final boolean isStuttering() {
        return mIsStuttering;
    }

    public final boolean isDnsReady() {
        return mIsDnsReady;
    }

    @Override
    public final void updateStutterStatus(boolean isStuttering) {
        if (mIsStuttering != isStuttering) {
            MyLog.w(TAG, "updateStutterStatus " + isStuttering);
            mIsStuttering = isStuttering;
            if (mKeyFlowReporter != null) {
                if (mIsStuttering) {
                    mKeyFlowReporter.insertStutterBegin(System.currentTimeMillis());
                } else {
                    mKeyFlowReporter.insertStutterEnd(System.currentTimeMillis());
                }
            }
        }
    }

    @Override
    public final void onNetworkStatus(boolean isAvailable) {
        if (mIsNetworkAvail != isAvailable) { // TODO Yangli 讨论是否插入网络连接状态变化的点
            MyLog.w(TAG, "onNetworkStatus " + isAvailable);
            mIsNetworkAvail = isAvailable;
            if (mIsNetworkAvail) {
                fetchIpSetByHost(mHost, mContext, true);
            } else if (mIsStuttering) {
                // 若网络断开，则停止当前卡顿，同时重置卡顿状态
                updateStutterStatus(false);
            }
        }
    }

    protected void incrementFetchGuaranteeIpCnt() {
        ++mFetchGuaranteeIpCnt;
    }

    protected void onNewStreamUrl(boolean needNotify) {
        mStreamUrl = generateUrlForIp(mOriginalStreamUrl, mHost, null);
        MyLog.w(TAG, "onNewStreamUrl, mStreamUrl=" + mStreamUrl);
        mSelectedIp = "";
        mSelectedIpList = null;
        mSelectedHttpIpList = null;
        mSelectedLocalIpList = null;
        mIsStuttering = false;
        mNeedUseCache = true;
        mFetchGuaranteeIpCnt = 0;
        mHttpIpIndex = 0;
        mLocalIpIndex = 0;
        if (needNotify && mIsDnsReady && mDnsStatusListener != null) { // OriginalStreamUrl发生变化时，若域名未变，补发一个onDnsReady消息
            mDnsStatusListener.onDnsReady();
        }
    }

    private void clearDnsIpCache() {
        mLocalIpIndex = 0;
        mHttpIpIndex = 0;
        mLocalIpSet.clear();
        mHttpIpSet.clear();
    }

    private void clearIpCache() {
        clearDnsIpCache();
        mGuaranteeIpSet.clear();
    }

    /**
     * IP解析方式：1-LocalDNS, 2-DnsPod, 3-网宿HttpDns
     */
    public final int queryIpFlag(String ip) {
        if (mLocalIpSet.contains(ip)) {
            return ReportProtocol.URL_STATUS_DNS;
        } else if (mHttpIpSet.contains(ip)) {
            return ReportProtocol.URL_STATUS_DNS_POD;
        }
        return ReportProtocol.URL_STATUS_HTTP_DNS;
    }

    /**
     * 设置服务端下发的推/拉流原始地址，没有进行过IP替换，包含有域名
     */
    public void setOriginalStreamUrl(String originalStreamUrl) {
        if (TextUtils.isEmpty(originalStreamUrl)) {
            MyLog.e(TAG, "setOriginalStreamUrl, but originalStreamUrl is null");
            return;
        }
        String host = getDomain(originalStreamUrl);
        if (TextUtils.isEmpty(host)) {
            MyLog.e(TAG, "setOriginalStreamUrl, but host is null, originalStreamUrl=" + originalStreamUrl);
            return;
        }
        if (!originalStreamUrl.equals(mOriginalStreamUrl)) {
            MyLog.w(TAG, "setOriginalStreamUrl to " + originalStreamUrl);
            mOriginalStreamUrl = originalStreamUrl;
            mProtocol = getProtocol(mOriginalStreamUrl);
            if (!host.equals(mHost)) { // 若域名发生变化，则更新域名，并且重新进行DNS解析
                MyLog.w(TAG, "set mHost to " + host);
                mHost = host;
                mIsDnsReady = false;
                clearIpCache();
                fetchIpSetByHost(mHost, mContext, true);
                onNewStreamUrl(false);
            } else {
                onNewStreamUrl(true);
            }
        }
    }

    protected final void fetchFromHttpAndLocalIpSet(final @NonNull PreDnsManager.IpInfo ipInfo) {
        MyLog.w(TAG, "fetchFromHttpAndLocalIpSet");
        int httpSize = mHttpIpSet.size(), localSize = mLocalIpSet.size();
        if (mHttpIpIndex < httpSize && mLocalIpIndex < localSize) { // Local和Http都还未用完，各取一个
            MyLog.d(TAG, "queryNewIpSet HttpIpSet and LocalIpSet");
            ipInfo.httpIpSet.add(mHttpIpSet.get(mHttpIpIndex++));
            ipInfo.localIpSet.add(mLocalIpSet.get(mLocalIpIndex++));
        } else if (mHttpIpIndex < httpSize) { // Http未用完，则取两个，若剩余不足两个则只取一个
            MyLog.d(TAG, "queryNewIpSet HttpIpSet");
            ipInfo.httpIpSet.add(mHttpIpSet.get(mHttpIpIndex++));
            if (mHttpIpIndex < httpSize) {
                ipInfo.httpIpSet.add(mHttpIpSet.get(mHttpIpIndex++));
            }
        } else if (mLocalIpIndex < localSize) { // Local未用完，则取两个，若剩余不足两个则只取一个
            MyLog.d(TAG, "queryNewIpSet LocalIpSet");
            ipInfo.localIpSet.add(mLocalIpSet.get(mLocalIpIndex++));
            if (mLocalIpIndex < httpSize) {
                ipInfo.localIpSet.add(mLocalIpSet.get(mLocalIpIndex++));
            }
        }
    }

    protected void fetchFromGuaranteeIpSet(final @NonNull PreDnsManager.IpInfo ipInfo) {
        if (!mGuaranteeIpSet.isEmpty()) { // Local和Http都用完，且保底IP不为空，则返回全部保底IP，并重置Local和Http的访问索引
            MyLog.d(TAG, "queryNewIpSet mGuaranteeIpSet");
            ipInfo.httpIpSet.addAll(mGuaranteeIpSet);
            mGuaranteeIpSet.clear();
            mHttpIpIndex = 0;
            mLocalIpIndex = 0;
        }
    }

    protected void onIpSetRunOut(final @NonNull PreDnsManager.IpInfo ipInfo) {
        MyLog.w(TAG, "onIpSetRunOut");
        if (mHttpIpSet.isEmpty() && mLocalIpSet.isEmpty()) { // 都用完了，若Local和Http集为空，拉取Local和Http
            MyLog.d(TAG, "queryNewIpSet fetchIpSetByHost");
            fetchIpSetByHost(mHost, mContext, false);
        } else { // 拉取保底IP
            MyLog.d(TAG, "queryNewIpSet fetchGuaranteeIpSet");
            fetchGuaranteeIpSet(mHost, mOriginalStreamUrl, mUserId, false);
        }
    }

    private PreDnsManager.IpInfo queryNewIpSet() {
        PreDnsManager.IpInfo ipInfo = new PreDnsManager.IpInfo();
        fetchFromHttpAndLocalIpSet(ipInfo);
        if (!ipInfo.isEmpty()) {
            return ipInfo;
        }
        fetchFromGuaranteeIpSet(ipInfo);
        if (!ipInfo.isEmpty()) {
            return ipInfo;
        }
        onIpSetRunOut(ipInfo);
        return ipInfo;
    }

    public void destroy() {
        if (mLocalAndHttpSub != null && !mLocalAndHttpSub.isUnsubscribed()) {
            mLocalAndHttpSub.unsubscribe();
            mLocalAndHttpSub = null;
        }
        if (mGuaranteeSub != null && !mGuaranteeSub.isUnsubscribed()) {
            mGuaranteeSub.unsubscribe();
            mGuaranteeSub = null;
        }
        mContext = null;
        mDnsStatusListener = null;
        mKeyFlowReporter = null;
    }

    private void onFetchIpSetByHostDone(PreDnsManager.IpInfo ipInfo) {
        clearDnsIpCache();
        mNeedUseCache = false;
        mLocalIpSet.addAll(ipInfo.localIpSet);
        mHttpIpSet.addAll(ipInfo.httpIpSet);
        if (!mIsDnsReady) { // 第一次成功时，通知发送onDnsReady通知
            mIsDnsReady = !ipInfo.isEmpty();
            if (mIsDnsReady && mDnsStatusListener != null) {
                mDnsStatusListener.onDnsReady();
            }
        }
        MyLog.w(TAG, "onFetchIpSetByHostDone localIpSet=" + mLocalIpSet + ", httpIpSet=" + mHttpIpSet);
    }

    /*
     * 通过本地域名解析和Http域名解析获取IP，在IO线程发起请求，在主线程处理请求结果
     */
    protected final void fetchIpSetByHost(final String host, final Context context, boolean forceCloseFormer) {
        if (TextUtils.isEmpty(host) || context == null) {
            MyLog.e(TAG, "fetchIpSetByHost, but host or context is null");
            return;
        }
        MyLog.w(TAG, "fetchIpSetByHost host=" + host + ", forceCloseFormer=" + forceCloseFormer);
        if (mLocalAndHttpSub != null && !mLocalAndHttpSub.isUnsubscribed()) {
            if (!forceCloseFormer) {
                return;
            }
            mLocalAndHttpSub.unsubscribe();
            mLocalAndHttpSub = null;
        }
        if (mNeedUseCache) {
            PreDnsManager.IpInfo ipInfo = PreDnsManager.INSTANCE.getIpInfoForHostFromPool(host);
            if (ipInfo != null && !ipInfo.isEmpty()) {
                onFetchIpSetByHostDone(ipInfo);
                return;
            }
        }
        mLocalAndHttpSub = Observable.create(new Observable.OnSubscribe<PreDnsManager.IpInfo>() {
            @Override
            public void call(Subscriber<? super PreDnsManager.IpInfo> subscriber) {
                try {
                    List<String> httpIpSet = PreDnsManager.getHttpDnsIpSet(host);
                    List<String> localIpSet = PreDnsManager.getLocalDnsIpSet(host);
                    if (httpIpSet.isEmpty() && localIpSet.isEmpty()) { // 拉取结果为空，重试一次
                        if (!subscriber.isUnsubscribed()) { // Note: 网络请求即使在unsubscribe之后线程也会继续执行
                            httpIpSet = PreDnsManager.getHttpDnsIpSet(host);
                        }
                        if (!subscriber.isUnsubscribed()) {
                            localIpSet = PreDnsManager.getLocalDnsIpSet(host);
                        }
                    }
                    localIpSet.removeAll(httpIpSet); // 去重
                    subscriber.onNext(new PreDnsManager.IpInfo(localIpSet, httpIpSet));
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<PreDnsManager.IpInfo>() {
                    @Override
                    public void call(PreDnsManager.IpInfo ipInfo) {
                        PreDnsManager.INSTANCE.addIpSetToPool(host, ipInfo);
                        onFetchIpSetByHostDone(ipInfo);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "fetchIpSetByHost failed, exception=" + throwable);
                    }
                });
    }

    // 在IO线程发起请求，在主线程处理请求结果
    private void fetchGuaranteeIpSet(final String host, final String videoUrl, final long userId, boolean forceCloseFormer) {
        if (TextUtils.isEmpty(host) || TextUtils.isEmpty(videoUrl)) {
            MyLog.e(TAG, "fetchGuaranteeIpSet, but host or videoUrl is null");
            return;
        }
        MyLog.w(TAG, "fetchGuaranteeIpSet host=" + host + ", videoUrl=" + videoUrl + ", userId=" + userId + ", forceCloseFormer=" + forceCloseFormer);
        if (mGuaranteeSub != null && !mGuaranteeSub.isUnsubscribed()) {
            if (!forceCloseFormer) {
                return;
            }
            mGuaranteeSub.unsubscribe();
            mGuaranteeSub = null;
        }
        incrementFetchGuaranteeIpCnt();
        mGuaranteeSub = Observable.just(0)
                .map(new Func1<Integer,  List<String>>() {
                    @Override
                    public  List<String> call(Integer integer) {
                        return getGuaranteeIpSet(host, videoUrl, userId);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<String>>() {
                    @Override
                    public void call(List<String> retList) {
                        mGuaranteeIpSet.clear();
                        mGuaranteeIpSet.addAll(retList);
                        if (mGuaranteeIpSet.isEmpty()) { // 若拉取保底IP返回空，则使用循环使用HttpIpSet和LocalIpSet
                            mHttpIpIndex = 0;
                            mLocalIpIndex = 0;
                        }
                        MyLog.w(TAG, "fetchGuaranteeIpSet done");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        MyLog.e(TAG, "fetchGuaranteeIpSet failed, exception=" + throwable);
                    }
                });
    }

    // 保底IP拉取，拉取到IP列表之后执行跑马再返回
    private List<String> getGuaranteeIpSet(String host, String originalStreamUrl, long userId) {
        MyLog.d(TAG, "getGuaranteeIpSet");
        List<String> ipList = new ArrayList<>();
        if (TextUtils.isEmpty(originalStreamUrl)) {
            return ipList;
        }
        String url = generateUrlForGuaranteeIp(host, originalStreamUrl);
        if (!TextUtils.isEmpty(url)) {
            List<String> ipRsp = ReportManager.getQueryIpRsp(
                    MiLinkClientAdapter.getsInstance().getClientIp(), url, ReportManager.TYPE_NET);
            if (ipRsp != null && !ipRsp.isEmpty()) {
                for (String ipStr : ipRsp) {
                    if (!ipList.contains(ipStr)) { // 去重
                        ipList.add(ipStr);
                    }
                }
            }
            MyLog.w(TAG, "getGuaranteeIpSet url=" + url + ", ipList=" + ipList);
        }
        return AppNetworkUtils.hourseTraceSync(ipList); // 跑个马，拿个相对好的排序
    }

    /**
     * 从url中获取域名：例：http://219.157.114.81/v2.zb.mi.com/live/1222122_1460342569，域名为：v2.zb.mi.com
     */
    private String getDomain(String videoUrl) {
        if (TextUtils.isEmpty(videoUrl)) {
            return "";
        }
        int index = videoUrl.indexOf(".com");
        if (index > 0) {
            int end = index + 4;
            String tempUrl = videoUrl.substring(0, end);
            String[] tempUrlList = tempUrl.split("/");
            int length = tempUrlList.length;
            if (length > 0) {
                String domain = tempUrlList[length - 1];
                if (!TextUtils.isEmpty(domain)) {
                    MyLog.w(TAG, "domain = " + domain);
                    return domain;
                }
            }
        }
        try {
            URI uri = new URI(videoUrl);
            String host = uri.getHost();
            MyLog.d(TAG, "getDomain url=" + videoUrl + ",host=" + host);
            return host;
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return "";
    }
}
