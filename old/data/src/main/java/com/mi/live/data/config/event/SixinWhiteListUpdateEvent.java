package com.mi.live.data.config.event;

/**
 * Created by chengsimin on 16/9/12.
 */

/**
 * 私信白名单更新了，接收到这个时间顺带更新对话列表
 */
public class SixinWhiteListUpdateEvent {

    public String newWhiteList;

    public SixinWhiteListUpdateEvent(String newWhiteList) {
        this.newWhiteList = newWhiteList;
    }
}
