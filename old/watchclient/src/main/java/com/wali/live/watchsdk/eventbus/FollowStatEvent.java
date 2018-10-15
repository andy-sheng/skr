package com.wali.live.watchsdk.eventbus;

/**
 * Created by yurui on 2016/12/2.
 */

/**
 * 关注按钮点击统计事件
 */
public class FollowStatEvent {
    private String statKey;

    public FollowStatEvent(String statKey) {
        this.statKey = statKey;
    }

    public String getStatKey() {
        return statKey;
    }
}
