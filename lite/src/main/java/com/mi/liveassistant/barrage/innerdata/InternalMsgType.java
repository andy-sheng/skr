package com.mi.liveassistant.barrage.innerdata;

import com.mi.liveassistant.barrage.model.BarrageMsgType;

/**
 * Created by wuxiaoshan on 17-5-16.
 */
public class InternalMsgType {
    //房间系统消息
    public static final int MSG_TYPE_ROOM_SYS_MSG = BarrageMsgType.B_MSG_TYPE_ROOM_SYS_MSG;
    //加入房间消息
    public static final int MSG_TYPE_JOIN = BarrageMsgType.B_MSG_TYPE_JOIN;
    //离开房间消息
    public static final int MSG_TYPE_LEAVE = BarrageMsgType.B_MSG_TYPE_LEAVE;
    //房间观众变化消息
    public static final int MSG_TYPE_VIEWER_CHANGE = BarrageMsgType.B_MSG_TYPE_VIEWER_CHANGE;
    //排行榜成为TOP1变动消息(不展示)
    public static final int MSG_TYPE_TOP_GET = BarrageMsgType.B_MSG_TYPE_TOP_GET;
    //排行榜失去TOP1变动消息(不展示)
    public static final int MSG_TYPE_TOP_LOSE = BarrageMsgType.B_MSG_TYPE_TOP_LOSE;
    //直播结束
    public static final int MSG_TYPE_LIVE_END = BarrageMsgType.B_MSG_TYPE_LIVE_END;
    //踢人
    public static final int MSG_TYPE_KICK_VIEWER = BarrageMsgType.B_MSG_TYPE_KICK_VIEWER;

    /**礼物消息，开始**/
    //礼物消息
    public static final int MSG_TYPE_GIFT = BarrageMsgType.B_MSG_TYPE_GIFT;
    //付费弹幕
    public static final int MSG_TYPE_PAY_BARRAGE = BarrageMsgType.B_MSG_TYPE_PAY_BARRAGE;
    //红包
    public static final int MSG_TYPE_RED_ENVELOPE = BarrageMsgType.B_MSG_TYPE_RED_ENVELOPE;
    //背景礼物推送
    public static final int MSG_TYPE_ROOM_BACKGROUND_GIFT =  BarrageMsgType.B_MSG_TYPE_ROOM_BACKGROUND_GIFT;
    //点亮礼物消息
    public static final int MSG_TYPE_LIGHT_UP_GIFT = BarrageMsgType.B_MSG_TYPE_LIGHT_UP_GIFT;
    //全局消息推送，大金龙
    public static final int MSG_TYPE_GLABAL_MSG = BarrageMsgType.B_MSG_TYPE_GLABAL_MSG;
    /**礼物消息，结束**/
}
