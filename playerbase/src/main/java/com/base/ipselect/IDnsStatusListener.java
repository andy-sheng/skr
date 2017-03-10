package com.base.ipselect;

public interface IDnsStatusListener {
    /**
     * Dns状态回调
     * IpSelectionHelper第一次解析到Http和本地IP时，发送onDnsReady
     * 监听器收到该回调，表示可以向IpSelectionHelper请求IP地址
     */
    void onDnsReady();
}