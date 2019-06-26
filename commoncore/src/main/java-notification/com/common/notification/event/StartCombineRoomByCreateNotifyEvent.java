package com.common.notification.event;

import com.common.core.userinfo.model.LocalCombineRoomConfig;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Notification.CombineRoomEnterMsg;

import java.util.List;
import java.util.Map;

/**
 * 通过在唱聊房内邀请之后对方同意之后收到的push
 */
public class StartCombineRoomByCreateNotifyEvent extends BaseEnterRoomEvent{
    public StartCombineRoomByCreateNotifyEvent(BaseNotiInfo basePushInfo, CombineRoomEnterMsg combineRoomEnterMsg) {
        super(basePushInfo, combineRoomEnterMsg);
    }
}
