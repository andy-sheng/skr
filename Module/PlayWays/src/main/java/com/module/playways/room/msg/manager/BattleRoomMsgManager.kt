package com.module.playways.room.msg.manager

import com.common.log.MyLog
import com.zq.live.proto.BattleRoom.BattleRoomMsg
import com.zq.live.proto.BattleRoom.EBattleRoomMsgType
import org.greenrobot.eventbus.EventBus

/**
 * 处理所有的RaceRoomMsg
 */
object BattleRoomMsgManager : BaseMsgManager<EBattleRoomMsgType, BattleRoomMsg>() {

    /**
     * 处理消息分发
     *
     * @param msg
     */
    override fun processRoomMsg(msg: BattleRoomMsg) {
        var canGo = true  //是否放行的flag
        for (filter in mPushMsgFilterList) {
            canGo = filter.doFilter(msg)
            if (!canGo) {
                MyLog.d("BattleRoomMsgManager", "processRoomMsg " + msg + "被拦截")
                return
            }
        }
        processRoomMsg(msg.msgType, msg)
    }

    private fun processRoomMsg(messageType: EBattleRoomMsgType, msg: BattleRoomMsg?) {
        if (msg == null) {
            return
        }
        if (msg != null) {
            MyLog.d("BattleRoomMsgManager", "processRoomMsg" + " messageType=" + messageType + " 信令 msg.ts=" + msg.timeMs)
        }
        when {
            msg.msgType == EBattleRoomMsgType.BRT_USER_ENTER -> {
                EventBus.getDefault().post(msg.bUserEnterMsg)
                EventBus.getDefault().postSticky(msg.bUserEnterMsg)
            }
            msg.msgType == EBattleRoomMsgType.BRT_GAME_OVER -> EventBus.getDefault().post(msg.bGameOverMsg)
            msg.msgType == EBattleRoomMsgType.BRT_NEXT_ROUND -> EventBus.getDefault().post(msg.bNextRoundMsg)
            msg.msgType == EBattleRoomMsgType.BRT_SING_ROUND -> EventBus.getDefault().post(msg.bSingRoundMsg)
            msg.msgType == EBattleRoomMsgType.BRT_SYNC -> EventBus.getDefault().post(msg.bSyncMsg)
        }
    }
}
