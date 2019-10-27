package com.module.playways.room.msg.dispatcher

import com.common.log.MyLog
import com.module.msg.CustomMsgType
import com.module.msg.IPushMsgProcess
import com.module.playways.room.msg.manager.DoubleRoomMsgManager
import com.module.playways.room.msg.manager.GrabRoomMsgManager
import com.module.playways.room.msg.manager.MicRoomMsgManager
import com.module.playways.room.msg.manager.RaceRoomMsgManager
import com.zq.live.proto.CombineRoom.CombineRoomMsg
import com.zq.live.proto.GrabRoom.RoomMsg
import com.zq.live.proto.MicRoom.MicRoomMsg
import com.zq.live.proto.RaceRoom.RaceRoomMsg

object RoomMsgDispater : IPushMsgProcess {
    const val TAG = "RoomMsgDispater"

    override fun process(messageType: Int, data: ByteArray?) {
        when (messageType) {
            CustomMsgType.MSG_TYPE_ROOM -> {
                val msg = RoomMsg.parseFrom(data)

                if (msg == null) {
                    MyLog.e(TAG, "processRoomMsg" + " msg == null ")
                    return
                }

                GrabRoomMsgManager.getInstance().processRoomMsg(msg)
            }
            CustomMsgType.MSG_TYPE_COMBINE_ROOM -> {
                val msg = CombineRoomMsg.parseFrom(data)

                if (msg == null) {
                    MyLog.e(TAG, "processRoomMsg" + " msg == null ")
                    return
                }

                DoubleRoomMsgManager.getInstance().processRoomMsg(msg)
            }
            CustomMsgType.MSG_TYPE_RACE_ROOM -> {
                val msg = RaceRoomMsg.parseFrom(data)
                if (msg == null) {
                    MyLog.e(TAG, "processRoomMsg" + " msg == null ")
                    return
                }
                RaceRoomMsgManager.processRoomMsg(msg)
            }
            CustomMsgType.MSG_TYPE_MIC_ROOM -> {
                val msg = MicRoomMsg.parseFrom(data)
                if (msg == null) {
                    MyLog.e(TAG, "processRoomMsg" + " msg == null ")
                    return
                }
                MicRoomMsgManager.processRoomMsg(msg)
            }
        }
    }

    override fun acceptType(): IntArray {
        return intArrayOf(
                CustomMsgType.MSG_TYPE_ROOM,
                CustomMsgType.MSG_TYPE_COMBINE_ROOM,
                CustomMsgType.MSG_TYPE_RACE_ROOM,
                CustomMsgType.MSG_TYPE_MIC_ROOM
        )
    }

}