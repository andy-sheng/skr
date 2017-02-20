package com.wali.live.dns;

/**
 * Created by yangli on 16-11-4.
 * <p>
 * 需要域名解析的拉流模块，可以实现该接口
 * 子类实例：@WatchActivity @ReplayActivity
 *
 * @module 域名解析(重连逻辑)
 */
public interface IStreamReconnect extends IDnsStatusListener {
    /***
     * 重连时，更新IP列表
     * 实现该接口时，向IpSelectionHelper请求新的IP列表，并取列表首元素拼接URL
     * 若请求新列表失败，则使用老的列表和URL
     */
    boolean ipSelect();

    /***
     * 当收到引擎重连回调时，调用该接口进行重连
     * 实现该接口时，调用(或延迟调用)doStartStream
     * Note: code可用于传递错误码，用于判断是否需要延迟调用doStartStream
     */
    void startReconnect(int code);
}
