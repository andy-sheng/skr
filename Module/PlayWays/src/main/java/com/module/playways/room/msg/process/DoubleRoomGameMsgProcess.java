package com.module.playways.room.msg.process;

import com.common.log.MyLog;
import com.module.playways.room.msg.BasePushInfo;
import com.zq.live.proto.CombineRoom.CombineRoomMsg;
import com.zq.live.proto.CombineRoom.ECombineRoomMsgType;

public class DoubleRoomGameMsgProcess implements IPushChatRoomMsgProcess<ECombineRoomMsgType, CombineRoomMsg> {
    public final static String TAG = "DoubleRoomGameMsgProcess";

    @Override
    public ECombineRoomMsgType[] acceptType() {
        return new ECombineRoomMsgType[]{
                ECombineRoomMsgType.DRM_PICK
        };
    }

    @Override
    public void processRoomMsg(ECombineRoomMsgType messageType, CombineRoomMsg msg) {
        MyLog.d(TAG, "processRoomMsg" + " messageType=" + messageType.getValue());
        BasePushInfo basePushInfo = BasePushInfo.parse(msg);
        MyLog.d(TAG, "processRoomMsg" + " timeMs=" + basePushInfo.getTimeMs());

        if (msg.getMsgType() == ECombineRoomMsgType.DRM_PICK) {
//            processJoinActionMsg(basePushInfo, msg.getJoinActionMsg());
        }
    }

    public void processJoinActionMsg() {

    }
}
