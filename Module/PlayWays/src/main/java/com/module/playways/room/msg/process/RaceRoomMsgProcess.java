package com.module.playways.room.msg.process;

import com.common.log.MyLog;
import com.module.msg.CustomMsgType;
import com.module.msg.IPushMsgProcess;
import com.module.playways.room.msg.event.raceroom.RBLightEvent;
import com.module.playways.room.msg.event.raceroom.RExitGameEvent;
import com.module.playways.room.msg.event.raceroom.RGetSingChanceEvent;
import com.module.playways.room.msg.event.raceroom.RJoinActionEvent;
import com.module.playways.room.msg.event.raceroom.RJoinNoticeEvent;
import com.module.playways.room.msg.event.raceroom.RRoundOverEvent;
import com.module.playways.room.msg.event.raceroom.RSyncStatusEvent;
import com.module.playways.room.msg.event.raceroom.RWantSingChanceEvent;
import com.zq.live.proto.RaceRoom.ERaceRoomMsgType;
import com.zq.live.proto.RaceRoom.RaceRoomMsg;

import org.greenrobot.eventbus.EventBus;

import java.io.IOException;

public class RaceRoomMsgProcess implements IPushMsgProcess {
    public final String TAG = "ChatRoomMsgProcess";

    @Override
    public void process(int messageType, byte[] data) {
        MyLog.d(TAG, "process" + " messageType=" + messageType + " data=" + data);
        switch (messageType) {
            case CustomMsgType.MSG_TYPE_RACE_ROOM:
                processRoomMsg(data);
                break;
        }

    }

    @Override
    public int[] acceptType() {
        return new int[]{
                CustomMsgType.MSG_TYPE_RACE_ROOM
        };
    }

    // 处理房间消息
    private void processRoomMsg(byte[] data) {
        try {
            RaceRoomMsg msg = RaceRoomMsg.parseFrom(data);

            if (msg == null) {
                MyLog.e(TAG, "processRoomMsg" + " msg == null ");
                return;
            }
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
        } catch (IOException e) {
            MyLog.e(e);
        }

    }
}
