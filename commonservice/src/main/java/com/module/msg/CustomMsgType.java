package com.module.msg;

public interface CustomMsgType {
    int MSG_TYPE_TEXT = 1;
    int MSG_TYPE_ENTER = 2;
    int MSG_TYPE_QUIT = 3;

    int MSG_TYPE_ROOM = 4;  // RoomMsg 消息类型

    int MSG_TYPE_NOTIFICATION = 5; // NotificationMsg 消息类型

    int MSG_TYPE_COMBINE_ROOM = 6; // CombineRoomMsg 消息类型

    int MSG_TYPE_RACE_ROOM = 7;

    int MSG_TYPE_BROADCAST = 8; // BroadcastRoomMsg 消息类型

    int MSG_TYPE_MIC_ROOM = 9;

    int MSG_TYPE_RELAY_ROOM = 10;

    int MSG_TYPE_PARTY_ROOM = 11;

    int MSG_TYPE_BATTLE_ROOM = 12;
}
