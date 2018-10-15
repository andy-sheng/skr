package com.mi.live.data.push.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @module com.wali.live.message.data
 * <p>
 * Created by MK on 16/2/23.
 * 弹幕消息类型
 * @module 弹幕
 */
public class BarrageMsgType {
    // TODO: 加了新类型后要在本文件最后加到数组和Set里
    // TODO: 新增类型时注意一下BaseComponentActivity#processPushMsgList(List<BarrageMsg>)中对非本房间消息的过滤

    public static final int B_MSG_TYPE_RADIO_PUSH = 130;
    public static final int B_MSG_TYPE_SET_MANAGER = 200;
    public static final int B_MSG_TYPE_CANCEL_MANAGER = 201;
    public static final int B_MSG_TYPE_ROOM_FOUCES_ANCHOR = 202;//关注主播消息
    public static final int B_MSG_TYPE_OUTDOOR_FOUCES = 203;// 房间外关注消息(系统消息)
    public static final int B_MSG_TYPE_CANCLE_FOUCES = 204; //取消关注消息
    public static final int B_MSG_TYPE_FREQUENCY_CONTROL = 205; //消息频率控制等消息
    public static final int B_MSG_TYPE_KICK_VIEWER = 206; // 踢人push消息（只push给被踢人）

    //常量取值和服务器保持一致
    public static final int B_MSG_TYPE_GIFT = 302; //房间礼物消息
    public static final int B_MSG_TYPE_TEXT = 303; //房间文本消息
    public static final int B_MSG_TYPE_FORBIDDEN = 304; //房间禁言消息
    public static final int B_MSG_TYPE_LIKE = 305; //房间点赞消息
    public static final int B_MSG_TYPE_PAY_BARRAGE = 306; //付费弹幕
    public static final int B_MSG_TYPE_CANCEL_FORBIDDEN = 307; //房间取消禁言消息
    public static final int B_MSG_TYPE_SHARE = 308; //房间分享消息
    public static final int B_MSG_TYPE_TOP_GET = 309;// 排行榜成为TOP1变动消息(不展示)
    public static final int B_MSG_TYPE_TOP_LOSE = 310;// 排行榜失去TOP1变动消息(不展示)
    public static final int B_MSG_TYPE_VIEWER_CHANGE = 311; //房间观众变化消息
    public static final int B_MSG_TYPE_KICK_VIEWER_BARRAGE = 312;   //踢人弹幕消息

    public static final int B_MSG_TYPE_JOIN = 320; //加入房间消息
    public static final int B_MSG_TYPE_LEAVE = 321; //离开房间消息
    public static final int B_MSG_TYPE_ANCHOR_LEAVE = 322; //主播离开房间
    public static final int B_MSG_TYPE_ANCHOR_JOIN = 323; //主播回到房间

    public static final int B_MSG_TYPE_RECORD_SHARE = 325;  //录屏幕分享消息

    public static final int B_MSG_TYPE_LIVE_INFO_CHANGE = 327; //房间信息改动（针对是否让观众连麦的状态）
    public static final int B_MSG_TYPE_LIVE_START = 330; //开始直播消息
    public static final int B_MSG_TYPE_LIVE_END = 331; //结束直播消息

    public static final int B_MSG_TYPE_LINE_MIC_BEGIN = 335; // 连麦开始消息
    public static final int B_MSG_TYPE_LINE_MIC_END = 336; // 连麦结束消息
    public static final int B_MSG_TYPE_LINE_VIEWER_BACK = 337; // 连麦嘉宾回来消息
    public static final int B_MSG_TYPE_LINE_VIEWER_LEAVE = 338; // 连麦嘉宾离开消息
    public static final int B_MSG_TYPE_LIGHT_UP_GIFT = 339;//点亮礼物消息
    public static final int B_MSG_TYPE_ROOM_BACKGROUND_GIFT = 340;//背景礼物推送
    public static final int B_MSG_TYPE_TAP_TP_SELL = 341;//主播开启商城功能推送
    public static final int B_MSG_TYPE_SELL = 342;//购物成功消息

    public static final int B_MSG_TYPE_ANIM = 344; // 房间动画效果消息，如进入房间消息，等级升级消息
    public static final int B_MSG_TYPE_ADD_SHOP = 345;//上下架 购买成功
    public static final int B_MSG_TYPE_ATTACHMENT_COUNTER = 346;//运营位计数
    public static final int B_MSG_TYPE_ATTACHMENT = 347;             //运营位控制消息弹幕

    public static final int B_MSG_TYPE_RED_ENVELOPE = 350;           // 红包礼物消息
    public static final int B_MSG_TYPE_MISSION_COMPLETED = 351;      //活动任务完成
    public static final int B_MSG_TYPE_ANCHOR_CALL_OF_FOLLOW = 352;  // 主播召唤观众关注push  cmd:zhibo.push.roommsg
    public static final int B_MSG_TYPE_ANCHOR_RANK_CHANGED = 353;    // 主播排名发生变动  cmd:zhibo.push.roommsg

    public static final int B_MSG_TYPE_NEW_PK_START = 356;           //pk开始
    public static final int B_MSG_TYPE_NEW_PK_END = 357;             //pk结束
    public static final int B_MSG_TYPE_NEW_PK_SYSTEM = 354;          //pk系统消息(邀请pk 拒绝pk 接受pk 取消pk邀请)
    public static final int B_MSG_TYPE_NEW_PK_SCORE = 355;           //pk分数变化
    public static final int B_MSG_TYPE_QUESTION = 358;               //冲顶大会问题push消息
    public static final int B_MSG_TYPE_ANSWER = 359;                 //冲顶大会答案push消息

    public static final int B_MSG_TYPE_GLOBAL_SYS_MSG = 400;//全局系统消息
    public static final int B_MSG_TYPE_ROOM_SYS_MSG = 401;//房间系统消息
    public static final int B_MSG_TYPE_LIVE_OWNER_MSG = 402;//主播通知消息
    public static final int B_MSG_TYPE_COMMEN_SYS_MSG = 403;//通用系统消息(系统消息)

    public static final int B_MSG_TYPE_LEVEL_UPGRADE_SYS_MSG = 406; //等级升级提示
    public static final int B_MSG_TYPE_RED_NAME_STATUES = 407; //红名信息状态变化

    public static final int B_MSG_TYPE_VIP_LEVEL_CHANGED = 409;//VIP用户等级变化，只发给自己
    public static final int B_MSG_TYPE_GLABAL_MSG = 500;//全局消息推送，大金龙
    public static final int B_MSG_TYPE_ANNOUNCEMENT = 502;//主播公告更改

    public static final int B_MSG_TYPE_RADIO_ROOMMSG = 801;//电台房间消息

    public static final int B_MSG_TYPE_OPEN_TURN_TABLE = 802;//主播开启大转盘

    public static final int B_MSG_TYPE_DISCOUNT_GIFT_EVENT = 901;//半折礼物活动时间改变

    // TODO 记得在这加
    public static int[] types = new int[]{
            B_MSG_TYPE_SET_MANAGER, B_MSG_TYPE_CANCEL_MANAGER, B_MSG_TYPE_ROOM_FOUCES_ANCHOR,
            B_MSG_TYPE_OUTDOOR_FOUCES, B_MSG_TYPE_CANCLE_FOUCES, B_MSG_TYPE_FREQUENCY_CONTROL,
            B_MSG_TYPE_KICK_VIEWER, B_MSG_TYPE_GIFT, B_MSG_TYPE_TEXT, B_MSG_TYPE_FORBIDDEN,
            B_MSG_TYPE_LIKE, B_MSG_TYPE_PAY_BARRAGE, B_MSG_TYPE_CANCEL_FORBIDDEN, B_MSG_TYPE_SHARE,
            B_MSG_TYPE_TOP_GET, B_MSG_TYPE_TOP_LOSE, B_MSG_TYPE_VIEWER_CHANGE,
            B_MSG_TYPE_KICK_VIEWER_BARRAGE, B_MSG_TYPE_JOIN, B_MSG_TYPE_LEAVE,
            B_MSG_TYPE_ANCHOR_LEAVE, B_MSG_TYPE_ANCHOR_JOIN, B_MSG_TYPE_RECORD_SHARE,
            B_MSG_TYPE_LIVE_START, B_MSG_TYPE_LIVE_END,
//            B_MSG_TYPE_PK_BEGIN, B_MSG_TYPE_PK_END,
            B_MSG_TYPE_LINE_MIC_BEGIN, B_MSG_TYPE_LINE_MIC_END, B_MSG_TYPE_LINE_VIEWER_BACK,
            B_MSG_TYPE_LINE_VIEWER_LEAVE, B_MSG_TYPE_ANIM, B_MSG_TYPE_LIGHT_UP_GIFT,
            B_MSG_TYPE_ROOM_BACKGROUND_GIFT, B_MSG_TYPE_TAP_TP_SELL, B_MSG_TYPE_SELL,
            B_MSG_TYPE_ATTACHMENT, B_MSG_TYPE_RED_ENVELOPE, B_MSG_TYPE_GLOBAL_SYS_MSG,
            B_MSG_TYPE_ROOM_SYS_MSG, B_MSG_TYPE_LIVE_OWNER_MSG, B_MSG_TYPE_ADD_SHOP, B_MSG_TYPE_ANNOUNCEMENT,
            B_MSG_TYPE_COMMEN_SYS_MSG, B_MSG_TYPE_GLABAL_MSG, B_MSG_TYPE_LEVEL_UPGRADE_SYS_MSG,
            B_MSG_TYPE_RED_NAME_STATUES, B_MSG_TYPE_ATTACHMENT_COUNTER, B_MSG_TYPE_VIP_LEVEL_CHANGED,
            B_MSG_TYPE_MISSION_COMPLETED, B_MSG_TYPE_ANCHOR_CALL_OF_FOLLOW, B_MSG_TYPE_ANCHOR_RANK_CHANGED,
            B_MSG_TYPE_NEW_PK_START, B_MSG_TYPE_NEW_PK_END, B_MSG_TYPE_NEW_PK_SYSTEM, B_MSG_TYPE_NEW_PK_SCORE,
            B_MSG_TYPE_QUESTION, B_MSG_TYPE_ANSWER, B_MSG_TYPE_RADIO_ROOMMSG, B_MSG_TYPE_OPEN_TURN_TABLE,
            B_MSG_TYPE_DISCOUNT_GIFT_EVENT
    };

    // 不关心房间号的barrage
    private static Set<Integer> sDoNotCareRoomIdType;

    public static boolean doNotCareRoomId(int barrageType) {
        return sDoNotCareRoomIdType.contains(barrageType);
    }

    static {
        Arrays.sort(types);
        sDoNotCareRoomIdType = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                B_MSG_TYPE_GLOBAL_SYS_MSG,
                B_MSG_TYPE_LEVEL_UPGRADE_SYS_MSG,
                B_MSG_TYPE_COMMEN_SYS_MSG,
                B_MSG_TYPE_VIP_LEVEL_CHANGED,
                B_MSG_TYPE_MISSION_COMPLETED,
                B_MSG_TYPE_NEW_PK_START,
                B_MSG_TYPE_NEW_PK_END,
                B_MSG_TYPE_NEW_PK_SYSTEM,
                B_MSG_TYPE_NEW_PK_SCORE,
                B_MSG_TYPE_RADIO_PUSH
        )));
    }
}
