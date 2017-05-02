package com.mi.liveassistant.room.user;

import android.text.TextUtils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mi.liveassistant.data.User;
import com.mi.liveassistant.proto.LiveCommonProto;
import com.mi.liveassistant.proto.UserProto;
import com.mi.liveassistant.room.user.request.HomepageRequest;

public class UserInfoManager {
    private static final String TAG = UserInfoManager.class.getSimpleName();

    /**
     * 获取一个用户的信息
     */
    public static User getUserByUuid(long uuid) {
        User user = new User();
        user.setUid(uuid);
        UserProto.GetHomepageRsp rsp = new HomepageRequest(uuid).syncRsp();
        if (rsp != null && rsp.getRetCode() == 0) {
            user.parse(rsp.getPersonalInfo());
            user.parse(rsp.getPersonalData());
            try {
                user.parse(LiveCommonProto.UserRoomInfo.parseFrom(rsp.getRoomInfo()));
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
            if (rsp.getRankTopThreeListList() != null) {
                user.setRankTopThreeList(rsp.getRankTopThreeListList());
            }
            if (!TextUtils.isEmpty(rsp.getViewUrl())) {
                user.setViewUrl(rsp.getViewUrl());
            }
            if (!TextUtils.isEmpty(rsp.getRoomId())) {
                user.setRoomId(rsp.getRoomId());
            }
        }
        return user;
    }
}