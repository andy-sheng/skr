package com.wali.live.livesdk.live.component.data;

import com.wali.live.dns.IDnsStatusListener;

/**
 * Created by yangli on 2017/3/15.
 */
public interface IIpSelection extends IDnsStatusListener {

    /**
     * 重连时，更新IP列表
     * 实现该接口时，向IpSelectionHelper请求新的IP列表，并取列表首元素拼接URL
     * 若请求新列表失败，则使用老的列表和URL
     */
    boolean ipSelect();

    /**
     * Dns解析是否已经顺利完成
     */
    boolean isDnsReady();
}
