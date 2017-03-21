package com.base.ipselect;

import java.util.List;

/**
 * Created by yangli on 16-11-3.
 *
 * @module 域名解析
 */
public interface IStreamUrl {

    /**
     * 获取域名
     */
    String getHost();

    /**
     * 获取当前的推流URL
     */
    String getStreamUrl();

    /**
     * 获取当前的选中的Ip
     */
    String getSelectedIp();

    /**
     * 获取当前的选中的Ip列表
     */
    List<String> getSelectedIpList();

    /**
     * 重连时，更新IP列表
     * 实现该接口时，调用queryNewIpSet请求新的IP列表，并取列表首元素拼接URL
     * 若请求新列表失败，则使用老的列表和URL
     */
    boolean ipSelect();

    /**
     * 生成新的URL
     */
    String generateUrlForIp(String originalStreamUrl, String host, String selectedIp);

    /**
     * 获取当前是否处于卡顿
     */
    boolean isStuttering();

    /**
     * 更新卡顿状态
     */
    void updateStutterStatus(boolean isStuttering);

    /**
     * 更新网络连接状态
     */
    void onNetworkStatus(boolean isAvailable);

}
