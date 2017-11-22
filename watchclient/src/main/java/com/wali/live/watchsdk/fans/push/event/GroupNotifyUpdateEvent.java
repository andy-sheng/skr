package com.wali.live.watchsdk.fans.push.event;

import com.wali.live.watchsdk.fans.model.notification.GroupNotifyBaseModel;

import java.util.ArrayList;
import java.util.List;

/**
 * 关心群通知的接这个eventbus时间，会在群消息有更新时发出这个时间。里面的群消息按照时间戳从大到小排列，get(0)得到的是最新一条群通知消息。
 * Created by chengsimin on 16/10/28.
 */
public class GroupNotifyUpdateEvent {
    public GroupNotifyUpdateEvent() {
    }

    public boolean empty = false;
    public List<GroupNotifyBaseModel> allGroupNotifyList = new ArrayList<>();
    public List<GroupNotifyBaseModel> unDealGroupNotifyList = new ArrayList<>();
    public List<GroupNotifyBaseModel> unReadGroupNotifyList = new ArrayList<>();
}
