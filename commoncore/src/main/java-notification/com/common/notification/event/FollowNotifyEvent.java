package com.common.notification.event;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.notification.BaseNotiInfo;
import com.component.live.proto.Notification.FollowMsg;

// 关注提醒
public class FollowNotifyEvent {
    public BaseNotiInfo mBaseNotiInfo;

    public UserInfoModel mUserInfoModel;

    public FollowNotifyEvent() {
    }

    public FollowNotifyEvent(BaseNotiInfo baseNotiInfo, FollowMsg followMsg){
        this.mBaseNotiInfo = baseNotiInfo;
        this.mUserInfoModel = UserInfoModel.parseFromPB(followMsg.getUser());
        this.mUserInfoModel.setFollow(followMsg.getIsFollow());
        this.mUserInfoModel.setFriend(followMsg.getIsFriend());
    }
}
