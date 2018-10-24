package com.wali.live.modulewatch.event;

import com.wali.live.modulewatch.model.roominfo.RoomBaseDataModel;

/**
 * Created by chengsimin on 16/4/1.
 */
public class RoomDataChangeEvent {
    public static final int TYPE_CHANGE_TICKET = 1;
    public static final int TYPE_CHANGE_VIEWER_COUNT = 2;
    public static final int TYPE_CHANGE_VIEWERS = 3;
    public static final int TYPE_CHANGE_AVATAR = 4;
    public static final int TYPE_CHANGE_USER_INFO_COMPLETE = 5;
    public static final int TYPE_CHANGE_USER_MANAGER = 6;
    public static final int TYPE_CHANGE_GAME_INFO = 7;
    public static final int TYPE_CHANGE_THIRD_PARTY_INFO = 8;

    public int type;
    public RoomBaseDataModel source;

    public RoomDataChangeEvent(RoomBaseDataModel source, int type) {
        this.source = source;
        this.type = type;
    }

    public Object obj1;

    public RoomDataChangeEvent(RoomBaseDataModel source, int type, Object obj1) {
        this.source = source;
        this.type = type;
        this.obj1 = obj1;
    }
}
