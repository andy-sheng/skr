package com.module.playways.room.msg.dispatcher

import com.common.log.MyLog
import com.module.msg.CustomMsgType
import com.module.msg.IPushMsgProcess
import com.module.playways.room.msg.manager.*
import com.zq.live.proto.CombineRoom.CombineRoomMsg
import com.zq.live.proto.GrabRoom.RoomMsg
import com.zq.live.proto.MicRoom.MicRoomMsg
import com.zq.live.proto.PartyRoom.PartyRoomMsg
import com.zq.live.proto.RaceRoom.RaceRoomMsg
import com.zq.live.proto.RelayRoom.RelayRoomMsg

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
            CustomMsgType.MSG_TYPE_RELAY_ROOM -> {
                val msg = RelayRoomMsg.parseFrom(data)
                if (msg == null) {
                    MyLog.e(TAG, "processRoomMsg" + " msg == null ")
                    return
                }
                RelayRoomMsgManager.processRoomMsg(msg)
            }
            CustomMsgType.MSG_TYPE_PARTY_ROOM -> {
                val msg = PartyRoomMsg.parseFrom(data)
                if (msg == null) {
                    MyLog.e(TAG, "processRoomMsg" + " msg == null ")
                    return
                }
                PartyRoomMsgManager.processRoomMsg(msg)
            }
        }
    }

    override fun acceptType(): IntArray {
        return intArrayOf(
                CustomMsgType.MSG_TYPE_ROOM,
                CustomMsgType.MSG_TYPE_COMBINE_ROOM,
                CustomMsgType.MSG_TYPE_RACE_ROOM,
                CustomMsgType.MSG_TYPE_MIC_ROOM,
                CustomMsgType.MSG_TYPE_RELAY_ROOM,
                CustomMsgType.MSG_TYPE_PARTY_ROOM
        )
    }

}