package com.module.playways.room.msg.manager

import com.common.log.MyLog
import com.zq.live.proto.RelayRoom.ERelayRoomMsgType
import com.zq.live.proto.RelayRoom.RelayRoomMsg
import org.greenrobot.eventbus.EventBus

/**
 * 处理所有的RaceRoomMsg
 */
object PartyRoomMsgManager : BaseMsgManager<ERelayRoomMsgType, RelayRoomMsg>() {

    /**
     * 处理消息分发
     *
     * @param msg
     */
    override fun processRoomMsg(msg: RelayRoomMsg) {
        var canGo = true  //是否放行的flag
        for (filter in mPushMsgFilterList) {
            canGo = filter.doFilter(msg)
            if (!canGo) {
                MyLog.d("MicRoomMsgManager", "processRoomMsg " + msg + "被拦截")
                return
            }
        }
        processRoomMsg(msg.msgType, msg)
    }

    private fun processRoomMsg(messageType: ERelayRoomMsgType, msg: RelayRoomMsg?) {
        if (msg == null) {
            return
        }
        if (msg != null) {
            MyLog.d(TAG, "processRoomMsg" + " messageType=" + messageType + " 信令 msg.ts=" + msg.timeMs)
        }
        when {
            msg.msgType == ERelayRoomMsgType.RRT_USER_ENTER -> EventBus.getDefault().post(msg.rUserEnterMsg)
            msg.msgType == ERelayRoomMsgType.RRT_GAME_OVER -> EventBus.getDefault().post(msg.rGameOverMsg)
            msg.msgType == ERelayRoomMsgType.RRT_NEXT_ROUND -> EventBus.getDefault().post(msg.rNextRoundMsg)
            msg.msgType == ERelayRoomMsgType.RRT_UNLOCK -> EventBus.getDefault().post(msg.rUnlockMsg)
            msg.msgType == ERelayRoomMsgType.RRT_SYNC -> EventBus.getDefault().post(msg.rSyncMsg)
            msg.msgType == ERelayRoomMsgType.RRT_REQ_ADD_MUSIC -> EventBus.getDefault().post(msg.rReqAddMusicMsg)
            msg.msgType == ERelayRoomMsgType.RRT_ADD_MUSIC -> EventBus.getDefault().post(msg.rAddMusicMsg)
            msg.msgType == ERelayRoomMsgType.RRT_DEL_MUSIC -> EventBus.getDefault().post(msg.rDelMusicMsg)
            msg.msgType == ERelayRoomMsgType.RRT_UP_MUSIC -> EventBus.getDefault().post(msg.rUpMusicMsg)
            msg.msgType == ERelayRoomMsgType.RRT_MUTE -> EventBus.getDefault().post(msg.rMuteMsg)
        }
    }
}
