package com.common.notification.event;

import com.common.core.userinfo.model.LocalCombineRoomConfig;
import com.common.core.userinfo.model.UserInfoModel;
import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Common.AgoraTokenInfo;
import com.zq.live.proto.Notification.CombineRoomEnterMsg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CRStartByMatchPushEvent extends BaseEnterRoomEvent {
    public CRStartByMatchPushEvent(BaseNotiInfo basePushInfo, CombineRoomEnterMsg combineRoomEnterMsg) {
        super(basePushInfo, combineRoomEnterMsg);
    }
}
