package com.module.playways.room.msg.manager

import com.common.log.MyLog
import com.zq.live.proto.PartyRoom.EPartyRoomMsgType
import com.zq.live.proto.PartyRoom.PartyRoomMsg
import org.greenrobot.eventbus.EventBus

/**
 * 处理所有的RaceRoomMsg
 */
object PartyRoomMsgManager : BaseMsgManager<EPartyRoomMsgType, PartyRoomMsg>() {

    /**
     * 处理消息分发
     *
     * @param msg
     */
    override fun processRoomMsg(msg: PartyRoomMsg) {
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

    private fun processRoomMsg(messageType: EPartyRoomMsgType, msg: PartyRoomMsg?) {
        if (msg == null) {
            return
        }
        if (msg != null) {
            MyLog.d(TAG, "processRoomMsg" + " messageType=" + messageType + " 信令 msg.ts=" + msg.timeMs)
        }
        when {
            msg.msgType == EPartyRoomMsgType.PRT_JOIN_NOTICE -> EventBus.getDefault().post(msg.pJoinNoticeMsg)
            msg.msgType == EPartyRoomMsgType.PRT_FIX_ROOM_NOTICE -> EventBus.getDefault().post(msg.pFixRoomNoticeMsg)
            msg.msgType == EPartyRoomMsgType.PRT_SET_ROOM_ADMIN -> EventBus.getDefault().post(msg.pSetRoomAdminMsg)
            msg.msgType == EPartyRoomMsgType.PRT_SET_ALL_MEMBER_MIC -> EventBus.getDefault().post(msg.pSetAllMemberMicMsg)
            msg.msgType == EPartyRoomMsgType.PRT_SET_USER_MIC -> EventBus.getDefault().post(msg.pSetUserMicMsg)
            msg.msgType == EPartyRoomMsgType.PRT_SET_SEAT_STATUS -> EventBus.getDefault().post(msg.pSetSeatStatusMsg)
            msg.msgType == EPartyRoomMsgType.PRT_APPLY_FOR_GUEST -> EventBus.getDefault().post(msg.pApplyForGuest)
            msg.msgType == EPartyRoomMsgType.PRT_GET_SEAT -> EventBus.getDefault().post(msg.pGetSeatMsg)
            msg.msgType == EPartyRoomMsgType.PRT_BACK_SEAT -> EventBus.getDefault().post(msg.pBackSeatMsg)
//            msg.msgType == EPartyRoomMsgType.PRT_INVITE_USER -> {
//                var e = PartyRoomInviteEvent()
//                e.roomID = msg.pInviteUserMsg.roomID
//                e.userInfoModel = UserInfoModel.parseFromPB(msg.pInviteUserMsg.user)
//                EventBus.getDefault().post(e)
//            }
            msg.msgType == EPartyRoomMsgType.PRT_CHANGE_SEAT -> EventBus.getDefault().post(msg.pChangeSeatMsg)
            msg.msgType == EPartyRoomMsgType.PRT_KICK_OUT_USER -> EventBus.getDefault().post(msg.pKickoutUserMsg)
            msg.msgType == EPartyRoomMsgType.PRT_NEXT_ROUND -> EventBus.getDefault().post(msg.pNextRoundMsg)
            msg.msgType == EPartyRoomMsgType.PRT_EXIT_GAME -> EventBus.getDefault().post(msg.ppExitGameMsg)
            msg.msgType == EPartyRoomMsgType.PRT_SYNC -> EventBus.getDefault().post(msg.pSyncMsg)
            msg.msgType == EPartyRoomMsgType.PRT_DYNAMIC_EMOJI -> EventBus.getDefault().post(msg.pDynamicEmojiMsg)
            msg.msgType == EPartyRoomMsgType.PRT_GAME_OVER -> EventBus.getDefault().post(msg.pGameOverMsg)
            msg.msgType == EPartyRoomMsgType.PRT_CHANGE_ROOM_TOPIC -> EventBus.getDefault().post(msg.pChangeRoomTopicMsg)
            msg.msgType == EPartyRoomMsgType.PRT_CHANGE_ROOM_ENTER_PERMISSION -> EventBus.getDefault().post(msg.pChangeRoomEnterPermissionMsg)
            msg.msgType == EPartyRoomMsgType.PRT_UPDATE_POPULARITY -> EventBus.getDefault().post(msg.pUpdatePopularityMsg)
            msg.msgType == EPartyRoomMsgType.PRT_CLUB_GAME_STOP -> EventBus.getDefault().post(msg.pClubGameStopMsg)
            msg.msgType == EPartyRoomMsgType.PRT_CLUB_BECOME_HOST -> EventBus.getDefault().post(msg.pClubBecomeHostMsg)
            msg.msgType == EPartyRoomMsgType.PRT_CLUB_CHANGE_HOST -> EventBus.getDefault().post(msg.pClubChangeHostMsg)
            msg.msgType == EPartyRoomMsgType.PRT_BEGIN_VOTE -> EventBus.getDefault().post(msg.pBeginVote)
            msg.msgType == EPartyRoomMsgType.PRT_RSP_VOTE -> EventBus.getDefault().post(msg.pResponseVote)
            msg.msgType == EPartyRoomMsgType.PRT_RESULT_VOTE -> EventBus.getDefault().post(msg.pResultVote)

            msg.msgType == EPartyRoomMsgType.PRT_BEGIN_QUICK_ANSWER -> EventBus.getDefault().post(msg.pBeginQuickAnswer)
            msg.msgType == EPartyRoomMsgType.PRT_RSP_QUICK_ANSWER -> EventBus.getDefault().post(msg.pResponseQuickAnswer)
            msg.msgType == EPartyRoomMsgType.PRT_RESULT_QUICK_ANSWER -> EventBus.getDefault().post(msg.pResultQuickAnswer)
        }
    }
}
