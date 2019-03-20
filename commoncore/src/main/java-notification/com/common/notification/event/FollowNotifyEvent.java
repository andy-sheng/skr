package com.common.notification.event;

import com.common.core.userinfo.model.UserInfoModel;
import com.common.notification.BaseNotiInfo;
import com.zq.live.proto.Notification.FollowMsg;

// 关注提醒
public class FollowNotifyEvent {
    public BaseNotiInfo mBaseNotiInfo;

    public UserInfoModel mUserInfoModel;
    public boolean isFriend;
    public boolean isFollow;

    public FollowNotifyEvent(BaseNotiInfo baseNotiInfo, FollowMsg followMsg){
        this.mBaseNotiInfo = baseNotiInfo;
        this.isFollow = followMsg.getIsFollow();
        this.isFriend = followMsg.getIsFriend();
        this.mUserInfoModel = UserInfoModel.parseFromPB(followMsg.getUser());
    }
}
