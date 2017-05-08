package com.mi.liveassistant.barrage.data;

import com.mi.liveassistant.barrage.model.BarrageMsgType;

/**
 * 消息类型
 *
 * Created by wuxiaoshan on 17-5-3.
 */
public class MessageType {
    //文本消息
    public static final int MSG_TYPE_TEXT = BarrageMsgType.B_MSG_TYPE_TEXT;
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
    /*需要上层处理的系统消息，开始*/
    //房间禁言消息
    public static final int MSG_TYPE_FORBIDDEN = BarrageMsgType.B_MSG_TYPE_FORBIDDEN;
    //房间取消禁言消息
    public static final int MSG_TYPE_CANCEL_FORBIDDEN = BarrageMsgType.B_MSG_TYPE_CANCEL_FORBIDDEN;
    //取消管理员
    public static final int MSG_TYPE_CANCEL_MANAGER = BarrageMsgType.B_MSG_TYPE_CANCEL_MANAGER;
    //设置管理员
    public static final int MSG_TYPE_SET_MANAGER = BarrageMsgType.B_MSG_TYPE_SET_MANAGER;
    //消息频率控制等消息
    public static final int MSG_TYPE_FREQUENCY_CONTROL = BarrageMsgType.B_MSG_TYPE_FREQUENCY_CONTROL;
    //踢人
    public static final int MSG_TYPE_KICK_VIEWER = BarrageMsgType.B_MSG_TYPE_KICK_VIEWER;
    //直播结束
    public static final int MSG_TYPE_LIVE_END = BarrageMsgType.B_MSG_TYPE_LIVE_END;
    //主播离开
    public static final int MSG_TYPE_ANCHOR_LEAVE = BarrageMsgType.B_MSG_TYPE_ANCHOR_LEAVE;
    //主播回来
    public static final int MSG_TYPE_ANCHOR_JOIN = BarrageMsgType.B_MSG_TYPE_ANCHOR_JOIN;
    /*需要上层处理的系统消息，结束*/
    //礼物消息
    public static final int MSG_TYPE_GIFT = BarrageMsgType.B_MSG_TYPE_GIFT;



}
