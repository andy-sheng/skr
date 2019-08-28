package com.module.playways.room.msg.process.pushprocess;

import com.module.playways.room.msg.event.raceroom.RBLightEvent;
import com.module.playways.room.msg.event.raceroom.RExitGameEvent;
import com.module.playways.room.msg.event.raceroom.RGetSingChanceEvent;
import com.module.playways.room.msg.event.raceroom.RJoinActionEvent;
import com.module.playways.room.msg.event.raceroom.RJoinNoticeEvent;
import com.module.playways.room.msg.event.raceroom.RRoundOverEvent;
import com.module.playways.room.msg.event.raceroom.RSyncStatusEvent;
import com.module.playways.room.msg.event.raceroom.RWantSingChanceEvent;
import com.module.playways.room.msg.process.IPushChatRoomMsgProcess;
import com.zq.live.proto.RaceRoom.ERaceRoomMsgType;
import com.zq.live.proto.RaceRoom.RaceRoomMsg;

import org.greenrobot.eventbus.EventBus;

public class RaceRoomMsgProcess implements IPushChatRoomMsgProcess<ERaceRoomMsgType, RaceRoomMsg> {
    public final String TAG = "DoubleRoomGameMsgProcess";

    @Override
    public void processRoomMsg(ERaceRoomMsgType messageType, RaceRoomMsg msg) {
        if (msg.getMsgType() == ERaceRoomMsgType.RRM_JOIN_ACTION) {
            EventBus.getDefault().post(new RJoinActionEvent(msg.getRJoinActionMsg()));
        } else if (msg.getMsgType() == ERaceRoomMsgType.RRM_JOIN_NOTICE) {
            EventBus.getDefault().post(new RJoinNoticeEvent(msg.getRJoinNoticeMsg()));
        } else if (msg.getMsgType() == ERaceRoomMsgType.RRM_EXIT_GAME) {
            EventBus.getDefault().post(new RExitGameEvent(msg.getRExitGameMsg()));
        } else if (msg.getMsgType() == ERaceRoomMsgType.RRM_B_LIGHT) {
            EventBus.getDefault().post(new RBLightEvent(msg.getRBLightMsg()));
        } else if (msg.getMsgType() == ERaceRoomMsgType.RRM_WANT_SING) {
            EventBus.getDefault().post(new RWantSingChanceEvent(msg.getRWantSingChanceMsg()));
        } else if (msg.getMsgType() == ERaceRoomMsgType.RRM_GET_SING) {
            EventBus.getDefault().post(new RGetSingChanceEvent(msg.getRGetSingChanceMsg()));
        } else if (msg.getMsgType() == ERaceRoomMsgType.RRM_SYNC_STATUS) {
            EventBus.getDefault().post(new RSyncStatusEvent(msg.getRSyncStatusMsg()));
        } else if (msg.getMsgType() == ERaceRoomMsgType.RRM_ROUND_OVER) {
            EventBus.getDefault().post(new RRoundOverEvent(msg.getRRoundOverMsg()));
        }
    }

    @Override
    public ERaceRoomMsgType[] acceptType() {
        return new ERaceRoomMsgType[]{
                ERaceRoomMsgType.RRM_JOIN_ACTION
                , ERaceRoomMsgType.RRM_JOIN_NOTICE
                , ERaceRoomMsgType.RRM_EXIT_GAME
                , ERaceRoomMsgType.RRM_B_LIGHT
                , ERaceRoomMsgType.RRM_WANT_SING
                , ERaceRoomMsgType.RRM_GET_SING
                , ERaceRoomMsgType.RRM_SYNC_STATUS
                , ERaceRoomMsgType.RRM_ROUND_OVER
        };
    }

}
