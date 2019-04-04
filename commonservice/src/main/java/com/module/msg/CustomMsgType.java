package com.module.msg;

public interface CustomMsgType {
    int MSG_TYPE_TEXT = 1;
    int MSG_TYPE_ENTER = 2;
    int MSG_TYPE_QUIT = 3;

    int MSG_TYPE_ROOM = 4;  // RoomMsg 消息类型

    int MSG_TYPE_NOTIFICATION = 5; // NotificationMsg 消息类型
}
