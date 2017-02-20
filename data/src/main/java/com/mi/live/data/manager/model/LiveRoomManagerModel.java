package com.mi.live.data.manager.model;

import com.wali.live.proto.UserProto;

/**
 * Created by yurui on 2016/12/1.
 */

public class LiveRoomManagerModel {
    public long uuid;
    public long avatar;                    //头像时间戳
    public int level;                      //等级
    public int certificationType = 0;      //认证类型
    public boolean isInRoom = false;       //是否在线
    public boolean redName ;               //是否红名


    public LiveRoomManagerModel(long uuid) {
        this.uuid = uuid;
    }

    public LiveRoomManagerModel(UserProto.PersonalInfo personalInfo) {
        uuid = personalInfo.getZuid();
        avatar = personalInfo.getAvatar();
        level = personalInfo.getLevel();
        certificationType = personalInfo.getCertificationType();
        isInRoom = false;
        redName = personalInfo.getIsRedname();
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof LiveRoomManagerModel) {
            if (uuid == ((LiveRoomManagerModel) o).uuid) {
                return true;
            }
        }
        return false;
    }
}
