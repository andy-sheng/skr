package com.module.playways.room.msg.manager

import com.common.log.MyLog
import com.zq.live.proto.MicRoom.EMicRoomMsgType
import com.zq.live.proto.MicRoom.MicRoomMsg
import org.greenrobot.eventbus.EventBus

/**
 * 处理所有的RaceRoomMsg
 */
object MicRoomMsgManager : BaseMsgManager<EMicRoomMsgType, MicRoomMsg>() {

    /**
     * 处理消息分发
     *
     * @param msg
     */
    override fun processRoomMsg(msg: MicRoomMsg) {
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

    private fun processRoomMsg(messageType: EMicRoomMsgType, msg: MicRoomMsg?) {
        if (msg == null) {
            return
        }
        if (msg != null) {
            MyLog.d(TAG, "processRoomMsg" + " messageType=" + messageType + " 信令 msg.ts=" + msg.timeMs)
        }
        when {
            msg.msgType == EMicRoomMsgType.RMT_JOIN_ACTION -> EventBus.getDefault().post(msg.mJoinActionMsg)
            msg.msgType == EMicRoomMsgType.RMT_JOIN_NOTICE -> EventBus.getDefault().post(msg.mJoinNoticeMsg)
            msg.msgType == EMicRoomMsgType.RMT_EXIT_GAME -> EventBus.getDefault().post(msg.mExitGameMsg)
            msg.msgType == EMicRoomMsgType.RMT_SYNC_STATUS -> EventBus.getDefault().post(msg.syncStatusMsg)
            msg.msgType == EMicRoomMsgType.RMT_ROUND_OVER -> EventBus.getDefault().post(msg.mRoundOverMsg)
            msg.msgType == EMicRoomMsgType.RMT_ADD_MUSIC -> EventBus.getDefault().post(msg.mAddMusicMsg)
            msg.msgType == EMicRoomMsgType.RMT_DEL_MUSIC -> EventBus.getDefault().post(msg.mDelMusicMsg)
            msg.msgType == EMicRoomMsgType.RMT_UP_MUSIC -> EventBus.getDefault().post(msg.mUpMusicMsg)
            msg.msgType == EMicRoomMsgType.RMT_CANCEL_MUSIC -> EventBus.getDefault().post(msg.mCancelMusic)
            msg.msgType == EMicRoomMsgType.RMT_REQ_ADD_MUSIC -> EventBus.getDefault().post(msg.mReqAddMusicMsg)
            msg.msgType == EMicRoomMsgType.RMT_CHO_GIVE_UP -> EventBus.getDefault().post(msg.mchoGiveUpMsg)
            msg.msgType == EMicRoomMsgType.RMT_SPK_INNER_ROUND_OVER -> EventBus.getDefault().post(msg.mspkInnerRoundOverMsg)
            msg.msgType == EMicRoomMsgType.RMT_CHANGE_ROOM_NAME -> EventBus.getDefault().post(msg.mChangeRoomNameMsg)
            msg.msgType == EMicRoomMsgType.RMT_CHANGE_ROOM_LEVEL_LIMIT -> EventBus.getDefault().post(msg.mChangeRoomLevelLimitMsg)
            msg.msgType == EMicRoomMsgType.RMT_KICKOUT_USER -> EventBus.getDefault().post(msg.mKickoutUserMsg)
            msg.msgType == EMicRoomMsgType.RMT_CHANGE_ROOM_OWNER -> EventBus.getDefault().post(msg.mChangeRoomOwnerMsg)
            msg.msgType == EMicRoomMsgType.RMT_MATCH_STATUS -> EventBus.getDefault().post(msg.mMatchStatusMsg)
        }
    }
}
