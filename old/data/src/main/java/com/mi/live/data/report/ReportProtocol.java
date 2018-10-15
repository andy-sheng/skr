package com.mi.live.data.report;

/**
 * @module MiLink打点
 * Created by yangli on 16-7-6.
 */
public class ReportProtocol {

    public static final int MIN_REPORT_STREAM_FAILURE_INTERVAL = 1000;

    /** 公用关键字 */
    public static final String KEY_USER_ID = "user_id";

    public static final String KEY_ANCHOR_ID = "anchor_id";

    public static final String KEY_STREAM_URL = "stream_url";

    public static final String KEY_STREAM_DOMAIN = "stream_domain";

    public static final String KEY_STREAM_IP = "stream_ip";

    public static final String KEY_STATUS = "status";

    public static final String KEY_ERR_CODE = "err_code";

    public static final String KEY_ERR_MSG = "err_msg";

    public static final String KEY_DELAY = "delay";

    public static final String KEY_TIME = "time";

    public static final String KEY_TYPE = "type";

    public static final String KEY_RATE = "rate";

    /** 推拉流错误码 */
    public static final int STREAM_SUCCESS = 0;

    public static final int STREAM_FAILED = -1;

    public static final String STREAM_ERR_MSG_SUCCESS = "succ";

    public static final String STREAM_ERR_MSG_FAILED = "err";

    /** 推流地址获取方式 */
    public static final int URL_STATUS_DNS = 1; // 域名解析

    public static final int URL_STATUS_DNS_POD = 2; // DNSPod解析

    public static final int URL_STATUS_HTTP_DNS = 3; // 网宿HTTP DNS

    public static final int URL_STATUS_HTTP_ERR = 4; // 出错
}
