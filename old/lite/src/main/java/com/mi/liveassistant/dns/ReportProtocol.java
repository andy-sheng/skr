package com.mi.liveassistant.dns;

/**
 * @module MiLink打点
 * Created by yangli on 16-7-6.
 */
public class ReportProtocol {
    /**
     * 推流地址获取方式
     */
    public static final int URL_STATUS_DNS = 1; // 域名解析

    public static final int URL_STATUS_DNS_POD = 2; // DNSPod解析

    public static final int URL_STATUS_HTTP_DNS = 3; // 网宿HTTP DNS

    public static final int URL_STATUS_HTTP_ERR = 4; // 出错
}
