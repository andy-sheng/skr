package com.base.ipselect;

import android.text.TextUtils;

import com.base.log.MyLog;
import com.base.utils.network.DnsPodUtils;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by yangli on 16-11-7.
 * <p>
 * Note：该类只负责APP启动和网络变化时，进行域名预解析；IP优选请使用BaseIpSelectionHelper继承体系
 * <p>
 * 服务端下发域名和端口列表，见<class>GetConfigManager</class>
 * 通过push_stream和pull_stream，下发预解析域名列表，解析之后存于mDomainSet中;
 * 通过push_domain和push_domain，下发域名对应的端口信息，解析之后存于mDomainPortMap中，原始数据格式如下：
 * {域名:协议:端口[,端口]...} [;{域名:协议:端口[,端口][,端口]...}]...
 * 例如：r2.zb.mi.com:rtmp:80,1935;r2.in.zb.mi.com:rtmp:80,1935
 * <p>
 * 当拼接URL时，
 * 若IP和端口均存在，则使用IP加端口
 * 若IP存在，端口不存在，则使用IP(可能会加上默认端口)
 * 若IP不存在，端口存在，则使用域名加端口
 * 若IP和端口均不存在，则使用原始URL
 * Note: 存在指的时对应域名的IP和端口，成功下发(或成功拉取)
 *
 * @module 域名预解析
 */
public enum PreDnsManager {

    INSTANCE;

    private static final String TAG = "PreDnsManager";

    public static final String PULL_STREAM_DOMAIN_DEFAULT = "v2.zb.mi.com";
    public static final String PUSH_STREAM_DOMAIN_DEFAULT = "r2.zb.mi.com";

    private final Map<String, IpInfo> mDomainIpMap = new HashMap();
    private final Map<String, DomainInfo> mDomainPortMap = new HashMap();
    private final Set<String> mDomainSet = new LinkedHashSet();

    private String mNetworkId;

    PreDnsManager() {
        mDomainSet.add(PULL_STREAM_DOMAIN_DEFAULT);
        mDomainSet.add(PUSH_STREAM_DOMAIN_DEFAULT);
    }

    private String generateDomainPortKey(String domain, String port) {
        return domain + "_" + port;
    }

    public String getNetworkId() {
        return mNetworkId;
    }

    public void onNetworkConnected(String networkId) {
        if (TextUtils.isEmpty(mNetworkId) || TextUtils.isEmpty(networkId) || !mNetworkId.equals(networkId)) {
            MyLog.w(TAG, "onNetworkConnected networkId=" + networkId);
            mNetworkId = networkId;
        }
    }

    // Note:请从主线程调用该接口
    public IpInfo getIpInfoForHostFromPool(final String host) {
        MyLog.w(TAG, "getIpInfoForHostFromPool host=" + host);
        return mDomainIpMap.get(host);
    }

    // Note:请从主线程调用该接口
    public List<String> getPortInfoForHost(final String host, final String protocol) {
        MyLog.w(TAG, "getPortInfoForHost host=" + host + ", protocol=" + protocol);
        DomainInfo domainInfo = mDomainPortMap.get(generateDomainPortKey(host, protocol));
        return domainInfo != null ? domainInfo.portList : null;
    }

    // Note:请从主线程调用该接口
    public boolean needSetHost(final String host, final String protocol) {
        MyLog.w(TAG, "needSetHost host=" + host + ", protocol=" + protocol);
        DomainInfo domainInfo = mDomainPortMap.get(generateDomainPortKey(host, protocol));
        return domainInfo != null ? domainInfo.needSetDomain : false;
    }

    // Note:请从主线程调用该接口
    public List<String> getIpListForHostFromPool(String host) {
        if (!TextUtils.isEmpty(host)) {
            IpInfo ipInfo = mDomainIpMap.get(host);
            if (ipInfo != null) {
                return ipInfo.getIpList();
            }
        }
        return null;
    }

    // Note:请从主线程调用该接口
    public void addIpSetToPool(final String host, final IpInfo ipInfo) {
        if (TextUtils.isEmpty(host) || ipInfo == null || ipInfo.isEmpty()) {
            return;
        }
        MyLog.w(TAG, "addIpSetToPool host=" + host);
        if (!mDomainSet.contains(host)) {
            mDomainSet.add(host);
        }
        mDomainIpMap.put(host, ipInfo);
    }

    // Local-Dns解析，拉取到IP列表之后执行跑马再返回
    public static List<String> getLocalDnsIpSet(String host) {
        MyLog.w(TAG, "getLocalDnsIpSet domain=" + host);
        List<String> ipList = new ArrayList<>();
        try {
            InetAddress[] addresses = InetAddress.getAllByName(host);
            if (addresses != null && addresses.length > 0) {
                for (InetAddress item : addresses) {
                    String ipStr = item.getHostAddress();
                    if (!ipList.contains(ipStr)) {  // 去重
                        ipList.add(ipStr);
                    }
                }
            }
        } catch (Exception e) {
            MyLog.e("getLocalDnsIpSet failed, exception=" + e);
        }
        MyLog.w(TAG, "getLocalDnsIpSet domain=" + host + ", ipList=" + ipList);
        return ipList;
    }

    // Http-Dns解析，拉取到IP列表之后执行跑马再返回
    public static List<String> getHttpDnsIpSet(String host) {
        MyLog.w(TAG, "getHttpDnsIpSet domain=" + host);
        List<String> ipList = new ArrayList<>();
        try {
            DnsPodUtils dnsPodUtils = new DnsPodUtils();
            String dnsPodStr = dnsPodUtils.getAddressByHostDnsPod(host);
            if (TextUtils.isEmpty(dnsPodStr)) { // chenyong1 失败的话重试一次
                MyLog.w(TAG, "getHttpDnsIpSet failed retry");
                dnsPodStr = dnsPodUtils.getAddressByHostDnsPod(host);
            }
            if (TextUtils.isEmpty(dnsPodStr)) {
                MyLog.e(TAG, "getHttpDnsIpSet failed");
                return ipList;
            }
            dnsPodStr = dnsPodStr.split(",")[0];
            String[] dnsPodSet = dnsPodStr.split(";");
            for (int i = dnsPodSet.length - 1; i >= 0; i--) {
                String ipStr = dnsPodSet[i]; // 去重
                if (!ipList.contains(ipStr)) {
                    ipList.add(0, ipStr);
                }
            }
        } catch (Exception e) {
            MyLog.e("getHttpDnsIpSet failed, exception=" + e);
        }
        MyLog.w(TAG, "getHttpDnsIpSet domain=" + host + ", ipList=" + ipList);
        return ipList;
    }

    public static String parseProtocolFromUrl(String streamUrl) {
        if (TextUtils.isEmpty(streamUrl)) {
            return null;
        }
        int pos = streamUrl.indexOf("://");
        if (pos != -1) {
            return streamUrl.substring(0, pos);
        }
        return null;
    }

    /**
     * 从url中获取域名：例：http://219.157.114.81/v2.zb.mi.com/live/1222122_1460342569，域名为：v2.zb.mi.com
     */
    public static String parseDomainFromUrl(String videoUrl) {
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

    public static class DomainInfo {
        public final boolean needSetDomain;
        public final List<String> portList;

        public DomainInfo(boolean needSetHost, List<String> portList) {
            this.needSetDomain = needSetHost;
            this.portList = portList;
        }
    }

    public static class IpInfo {
        public final List<String> localIpSet;
        public final List<String> httpIpSet;

        public IpInfo() {
            localIpSet = new ArrayList<>();
            httpIpSet = new ArrayList<>();
        }

        public IpInfo(List<String> localIpSet, List<String> httpIpSet) {
            this.localIpSet = localIpSet;
            this.httpIpSet = httpIpSet;
        }

        public boolean isEmpty() {
            return localIpSet.isEmpty() && httpIpSet.isEmpty();
        }

        public void clear() {
            localIpSet.clear();
            httpIpSet.clear();
        }

        public List<String> getIpList() {
            List<String> ipList = new ArrayList<>();
            ipList.addAll(localIpSet);
            ipList.addAll(httpIpSet);
            return ipList;
        }

        private String formatIpV6(String ip) {
            if (ip.contains(":") && ip.indexOf(":") != ip.lastIndexOf(":") && !ip.startsWith("[")) {
                return "[" + ip + "]"; // IpV6需加[]
            }
            return ip;
        }

        private void addPortInfo(List<String> ipList, List<String> portList) {
            List<String> tmpIpList = new ArrayList<>(ipList);
            ipList.clear();
            for (String ip : tmpIpList) {
                ip = formatIpV6(ip);
                if (portList == null || portList.isEmpty() ||
                        (!ip.startsWith("[") && ip.contains(":")/*IpV4*/) || ip.contains("]:")/*IpV6*/) {
                    ipList.add(ip);
                    continue;
                }
                for (String port : portList) {
                    ipList.add(ip + ":" + port);
                }
            }
        }

        public void addPortInfoIfNeed(List<String> portList) {
            MyLog.v(TAG, "addPortInfoIfNeed before localIpSet=" + localIpSet + ", httpIpSet=" + httpIpSet);
            addPortInfo(localIpSet, portList);
            addPortInfo(httpIpSet, portList);
            MyLog.v(TAG, "addPortInfoIfNeed after localIpSet=" + localIpSet + ", httpIpSet=" + httpIpSet);
        }
    }
}
