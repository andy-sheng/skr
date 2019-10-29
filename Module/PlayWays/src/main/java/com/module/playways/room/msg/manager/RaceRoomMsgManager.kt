package com.module.playways.room.msg.manager

import com.common.log.MyLog
import com.module.playways.room.msg.event.raceroom.*
import com.zq.live.proto.RaceRoom.ERaceRoomMsgType
import com.zq.live.proto.RaceRoom.RaceRoomMsg
import org.greenrobot.eventbus.EventBus

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
        processRoomMsg(msg.msgType, msg)
    }

    fun processRoomMsg(messageType: ERaceRoomMsgType, msg: RaceRoomMsg?) {
        if (msg != null) {
            MyLog.d(TAG, "processRoomMsg" + " messageType=" + messageType + " 信令 msg.ts=" + msg.timeMs)
        }
        if (msg!!.msgType == ERaceRoomMsgType.RRM_JOIN_ACTION) {
            EventBus.getDefault().post(RJoinActionEvent(msg.rJoinActionMsg))
        } else if (msg.msgType == ERaceRoomMsgType.RRM_JOIN_NOTICE) {
            EventBus.getDefault().post(RJoinNoticeEvent(msg.rJoinNoticeMsg))
        } else if (msg.msgType == ERaceRoomMsgType.RRM_EXIT_GAME) {
            EventBus.getDefault().post(RExitGameEvent(msg.rExitGameMsg))
        } else if (msg.msgType == ERaceRoomMsgType.RRM_B_LIGHT) {
            EventBus.getDefault().post(RBLightEvent(msg.rbLightMsg))
        } else if (msg.msgType == ERaceRoomMsgType.RRM_WANT_SING) {
            EventBus.getDefault().post(RWantSingChanceEvent(msg.rWantSingChanceMsg))
        } else if (msg.msgType == ERaceRoomMsgType.RRM_GET_SING) {
            EventBus.getDefault().post(RGetSingChanceEvent(msg.rGetSingChanceMsg))
        } else if (msg.msgType == ERaceRoomMsgType.RRM_SYNC_STATUS) {
            EventBus.getDefault().post(RSyncStatusEvent(msg.rSyncStatusMsg))
        } else if (msg.msgType == ERaceRoomMsgType.RRM_ROUND_OVER) {
            EventBus.getDefault().post(RRoundOverEvent(msg.rRoundOverMsg))
        }
    }
}
