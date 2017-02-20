package com.wali.live.dns;

/**
 * Created by yangli on 16-10-27.
 *
 * 需要域名解析的推流模块，可以实现该接口
 * 子类实例：@LiveActivity
 *
 * @module 域名解析(推流)
 */
public interface ILiveReconnect extends IStreamReconnect {

    /***
     * 强制开始推流
     * 实现该接口时，先调用ipSelect，然后将请求的IP列表及拼接好的URL设置给引擎，启动重连
     */
    void doStartStream();

    /***
     * 开始推流
     * 实现该接口时，判断IpSelectionHelper是否就绪:
     * 若是，则调用doStartStream；否则，延迟(5s)调用doStartStream
     */
    void startStream();

    /***
     * 停止推流
     * 实现该接口时，直接调用引擎的接口，停止推流
     */
    void stopStream();

}