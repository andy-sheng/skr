package com.module.playways.room.msg.manager

import com.common.log.MyLog
import com.module.playways.room.msg.event.raceroom.*
import com.zq.live.proto.MicRoom.EMicRoomMsgType
import com.zq.live.proto.MicRoom.MicRoomMsg
import com.zq.live.proto.RaceRoom.ERaceRoomMsgType
import com.zq.live.proto.RaceRoom.RaceRoomMsg
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
                MyLog.d("RaceRoomMsgManager", "processRoomMsg " + msg + "被拦截")
                return
            }
        }
        processRoomMsg(msg.msgType, msg)
    }

    fun processRoomMsg(messageType: EMicRoomMsgType, msg: MicRoomMsg?) {
        if (msg == null) {
            return
        }
        if (msg != null) {
            MyLog.d(TAG, "processRoomMsg" + " messageType=" + messageType + " 信令 msg.ts=" + msg.timeMs)
        }
        if (msg.msgType == EMicRoomMsgType.RMT_JOIN_ACTION) {
            EventBus.getDefault().post(msg.mJoinActionMsg)
        } else if (msg.msgType == EMicRoomMsgType.RMT_JOIN_NOTICE) {
            EventBus.getDefault().post(msg.mJoinNoticeMsg)
        } else if (msg.msgType == EMicRoomMsgType.RMT_EXIT_GAME) {
            EventBus.getDefault().post(msg.mExitGameMsg)
        } else if (msg.msgType == EMicRoomMsgType.RMT_SYNC_STATUS) {
            EventBus.getDefault().post(msg.syncStatusMsg)
        } else if (msg.msgType == EMicRoomMsgType.RMT_ROUND_OVER) {
            EventBus.getDefault().post(msg.mRoundOverMsg)
        } else if (msg.msgType == EMicRoomMsgType.RMT_ADD_MUSIC) {
            EventBus.getDefault().post(msg.mAddMusicMsg)
        } else if (msg.msgType == EMicRoomMsgType.RMT_DEL_MUSIC) {
            EventBus.getDefault().post(msg.mDelMusicMsg)
        } else if (msg.msgType == EMicRoomMsgType.RMT_UP_MUSIC) {
            EventBus.getDefault().post(msg.mUpMusicMsg)
        } else if (msg.msgType == EMicRoomMsgType.RMT_CANCEL_MUSIC) {
            EventBus.getDefault().post(msg.mCancelMusic)
        } else if (msg.msgType == EMicRoomMsgType.RMT_REQ_ADD_MUSIC) {
            EventBus.getDefault().post(msg.mReqAddMusicMsg)
        } else if (msg.msgType == EMicRoomMsgType.RMT_CHO_GIVE_UP) {
            EventBus.getDefault().post(msg.mchoGiveUpMsg)
        } else if (msg.msgType == EMicRoomMsgType.RMT_SPK_INNER_ROUND_OVER) {
            EventBus.getDefault().post(msg.mspkInnerRoundOverMsg)
        } else if (msg.msgType == EMicRoomMsgType.RMT_CHANGE_ROOM_NAME) {
            EventBus.getDefault().post(msg.mChangeRoomNameMsg)
        } else if (msg.msgType == EMicRoomMsgType.RMT_CHANGE_ROOM_LEVEL_LIMIT) {
            EventBus.getDefault().post(msg.mChangeRoomLevelLimitMsg)
        } else if (msg.msgType == EMicRoomMsgType.RMT_KICKOUT_USER) {
            EventBus.getDefault().post(msg.mKickoutUserMsg)
        } else if (msg.msgType == EMicRoomMsgType.RMT_CHANGE_ROOM_OWNER) {
            EventBus.getDefault().post(msg.mChangeRoomOwnerMsg)
        } else if (msg.msgType == EMicRoomMsgType.RMT_MATCH_STATUS) {
            EventBus.getDefault().post(msg.mMatchStatusMsg)
        }
    }
}
