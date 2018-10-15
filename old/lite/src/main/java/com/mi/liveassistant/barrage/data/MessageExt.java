package com.mi.liveassistant.barrage.data;

import com.mi.liveassistant.barrage.model.BarrageMsg;
import com.mi.liveassistant.barrage.model.MessageRuleModel;
import com.mi.liveassistant.barrage.model.ViewerModel;
import com.mi.liveassistant.data.model.Viewer;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息扩展数据
 *
 * Created by wuxiaoshan on 17-5-3.
 */
public class MessageExt {

    /**
     * 对应的消息类型：MSG_TYPE_FORBIDDEN、MSG_TYPE_CANCEL_FORBIDDEN
     */
    public static class ForbiddenMessageExt extends MessageExt{
        public long forbiddenUserId;
        public int operatorType;
        public String banNickname;

        public ForbiddenMessageExt(BarrageMsg.ForbiddenMsgExt forbiddenMsgExt){
            forbiddenUserId = forbiddenMsgExt.forbiddenUserId;
            operatorType = forbiddenMsgExt.operatorType;
            banNickname = forbiddenMsgExt.banNickname;
        }
    }

    /**
     * 对应的消息类型：MSG_TYPE_VIEWER_CHANGE
     */
    public static class ViewChangeMessageExt extends MessageExt{
        public int viewerCount;

        public List<Viewer> viewerList = new ArrayList<>();

        public ViewChangeMessageExt(BarrageMsg.ViewerChangeMsgExt viewerChangeMsgExt) {
            viewerCount = viewerChangeMsgExt.viewerCount;
            if (viewerChangeMsgExt.viewerList != null && viewerChangeMsgExt.viewerList.size() > 0) {
                for (int i = 0; i < viewerChangeMsgExt.viewerList.size(); i++) {
                    viewerList.add(new Viewer(viewerChangeMsgExt.viewerList.get(i)));
                }
            }
        }
    }

    /**
     * 对应的消息类型：MSG_TYPE_LIVE_END
     *
     * 这种消息，客户端只是接收方， 不会发出
     */
    public static class LiveEndMessageExt extends MessageExt{
        public int viewerCount;

        public LiveEndMessageExt(BarrageMsg.LiveEndMsgExt liveEndMsgExt){
            viewerCount = liveEndMsgExt.viewerCount;
        }
    }

    /**
     *对应的消息类型：MSG_TYPE_KICK_VIEWER
     */
    public static class KickMessageExt extends MessageExt{

        public static final int OPERATION_TYPE_THIS_ROOM = 0;
        public static final int OPERATION_TYPE_BLOCK = 1;

        public long zuid; // 主播id
        public String liveid; // 直播id
        public long operatorId; //操作人id
        public int operatorType; //操作人类型: 0:主播, 1:管理员, 2:榜一
        public long kickedId; //被踢用户id
        public int operationType; //操作类型: 0:本场拉黑，1:永久拉黑
        public String kickedNickname;

        public KickMessageExt(BarrageMsg.KickMessageExt kickMessageExt){
            zuid = kickMessageExt.getZuid();
            liveid = kickMessageExt.getLiveid();
            operatorId = kickMessageExt.getOperatorId();
            operatorType = kickMessageExt.getOperatorType();
            kickedId = kickMessageExt.getKickedId();
            operationType = kickMessageExt.getOperationType();
            kickedNickname = kickMessageExt.getKickedNickname();
        }
    }

    /**
     * 对应的消息类型：B_MSG_TYPE_GIFT、MSG_TYPE_PAY_BARRAGE、MSG_TYPE_RED_ENVELOPE
     *              、MSG_TYPE_ROOM_BACKGROUND_GIFT、MSG_TYPE_LIGHT_UP_GIFT、MSG_TYPE_GLABAL_MSG
     */
    public static class GiftMessageExt extends MessageExt{
        public int giftId;// 礼物id
        public String giftName;// 礼物名称
        public int giftCount;// 礼物个数
        public int zhuboAsset;// 主播当前的收益资产
        public long zhuboAssetTs;// 主播当前资产时间戳

        public GiftMessageExt(BarrageMsg.GiftMsgExt giftMsgExt){
            giftId = giftMsgExt.giftId;
            giftName = giftMsgExt.giftName;
            giftCount = giftMsgExt.giftCount;
            zhuboAsset = giftMsgExt.zhuboAsset;
            zhuboAssetTs = giftMsgExt.zhuboAssetTs;

        }
    }

    /**
     * 对应的消息类型：MSG_TYPE_FREQUENCY_CONTROL
     */
    public static class FrequencyControlMessageExt extends MessageExt{
        //不能重复，默认false，不设置或者​false：可以重复发言，true：不能重复发言
        public boolean unrepeatable;
        //发言频率周期,单位s,不设置或者0代表没有限制
        public int speakPeriod;

        public FrequencyControlMessageExt(MessageRuleModel msgRule){
            if(msgRule == null){
                return;
            }
            unrepeatable = msgRule.isUnrepeatable();
            speakPeriod = msgRule.getSpeakPeriod();
        }

    }

    /**
     * 对应的消息类型：MSG_TYPE_JOIN
     *
     * 这种消息，客户端只是接收方， 不会发出
     */
    public static class JoinRoomMessageExt extends MessageExt {
        public int viewerCount;

        public List<Viewer> viewerList = new ArrayList<>();

        public JoinRoomMessageExt(BarrageMsg.JoinRoomMsgExt joinRoomMsgExt){
            viewerCount = joinRoomMsgExt.viewerCount;
            if(joinRoomMsgExt.viewerList != null){
                for(ViewerModel viewerModel:joinRoomMsgExt.viewerList){
                    viewerList.add(new Viewer(viewerModel));
                }
            }
        }

    }

    /**
     * 这种消息，客户端只是接收方， 不会发出
     *
     * 对应的消息类型：MSG_TYPE_LEAVE
     */

    public static class LeaveRoomMessageExt extends MessageExt {
        public int viewerCount;

        public List<Viewer> viewerList = new ArrayList<>();

        public LeaveRoomMessageExt(BarrageMsg.LeaveRoomMsgExt leaveRoomMsgExt){
            viewerCount = leaveRoomMsgExt.viewerCount;
            if(leaveRoomMsgExt.viewerList != null){
                for(ViewerModel viewerModel:leaveRoomMsgExt.viewerList){
                    viewerList.add(new Viewer(viewerModel));
                }
            }
        }

    }

}
