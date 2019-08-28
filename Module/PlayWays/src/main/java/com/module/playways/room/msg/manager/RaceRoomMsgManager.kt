package com.module.playways.room.msg.manager

import com.common.log.MyLog
import com.module.playways.room.msg.filter.PushMsgFilter
import com.module.playways.room.msg.process.IPushChatRoomMsgProcess
import com.zq.live.proto.RaceRoom.ERaceRoomMsgType
import com.zq.live.proto.RaceRoom.RaceRoomMsg
import com.zq.live.proto.Room.ERoomMsgType
import com.zq.live.proto.Room.RoomMsg

import java.util.HashSet

/**
 * 处理所有的RaceRoomMsg
 */
object RaceRoomMsgManager : BaseMsgManager<ERaceRoomMsgType, RaceRoomMsg>() {

    /**
     * 处理消息分发
     *
     * @param msg
     */
    override fun processRoomMsg(msg: RaceRoomMsg) {
        var canGo = true  //是否放行的flag
        for (filter in mPushMsgFilterList) {
            canGo = filter.doFilter(msg)
            if (!canGo) {
                MyLog.d("RaceRoomMsgManager", "processRoomMsg " + msg + "被拦截")
                return
            }
        }

        val processors = mProcessorMap[msg.msgType]
        if (processors != null) {
            for (process in processors) {
                process.processRoomMsg(msg.msgType, msg)
            }
        }
    }

}
