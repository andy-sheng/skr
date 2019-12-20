package com.module.playways.party.room.model

import com.common.log.MyLog
import com.module.playways.BaseRoomData
import com.module.playways.party.room.event.PartyRoundStatusChangeEvent
import com.module.playways.room.prepare.model.BaseRoundInfoModel
import com.zq.live.proto.PartyRoom.EPRoundStatus
import com.zq.live.proto.PartyRoom.PRoundInfo
import org.greenrobot.eventbus.EventBus


class PartyRoundInfoModel : BaseRoundInfoModel() {
    var status = EPRoundStatus.PRS_UNKNOWN.value
    var beginMs: Int = 0 //开始相对时间（相对于createdTimeMs时间）
    var endMs: Int = 0 //结束相对时间（相对于createdTimeMs时间）
    var sceneInfo: PartyGameInfoModel? = null

    //！！！这个不是服务器返回的，是中途进来的时候从roomData里面设置的
    var elapsedTimeMs: Int = 0

    var roundStartTimeMs = System.currentTimeMillis()

    var accLoadingOk = false

    override fun getType(): Int {
        return TYPE_PARTY
    }

    fun updateStatus(notify: Boolean, statusGrab: Int) {
        if (getStatusPriority(status) < getStatusPriority(statusGrab)) {
            val old = status
            status = statusGrab
            if (notify) {
                EventBus.getDefault().post(PartyRoundStatusChangeEvent(this, old))
            }
        }
    }

    /**
     * 重排一下状态机的优先级
     *
     * @param status
     * @return
     */
    internal fun getStatusPriority(status: Int): Int {
        return status
    }

    //    /**
//     * 一唱到底使用
//     */
    override fun tryUpdateRoundInfoModel(round: BaseRoundInfoModel?, notify: Boolean) {
        if (round == null) {
            MyLog.e("JsonRoundInfo RoundInfo == null")
            return
        }
        val roundInfo = round as PartyRoundInfoModel
        this.setRoundSeq(roundInfo.getRoundSeq())
        this.beginMs = roundInfo.beginMs
        this.endMs = roundInfo.endMs
//        if (this.music == null) {
//            this.music = roundInfo.music
//        }
        // 观众席与玩家席更新，以最新的为准

        if (roundInfo.getOverReason() > 0) {
            this.setOverReason(roundInfo.getOverReason())
        }
        if (roundInfo.sceneInfo != null) {
            this.sceneInfo = roundInfo.sceneInfo
        }
        updateStatus(notify, roundInfo.status)
        return
    }

    //
//
    override fun toString(): String {
        return "PartyRoundInfoModel{" +
                "roundSeq=" + roundSeq +
                ", status=" + status +
//                ", songModel=" + (if (music == null) "" else music!!.toSimpleString()) +
//                ", singBeginMs=" + singBeginMs +
                ", sceneInfo=" + sceneInfo +
//                ", overReason=" + overReason +
                ", roundStartTimeMs=" + roundStartTimeMs +
                '}'.toString()
    }

    //
    companion object {

        fun parseFromRoundInfo(roundInfo: PRoundInfo): PartyRoundInfoModel {
            val roundInfoModel = PartyRoundInfoModel()
            roundInfoModel.setRoundSeq(roundInfo.roundSeq!!)
            roundInfoModel.beginMs = roundInfo.beginMs
            roundInfoModel.endMs = roundInfo.endMs
            if (roundInfo.hasSceneInfo()) {
                roundInfoModel.sceneInfo = PartyGameInfoModel.parseFromItemInfo(roundInfo.sceneInfo)
            }
            roundInfoModel.status = roundInfo.status.value
            roundInfoModel.roundStartTimeMs = roundInfo.roundStartTimeMs
            return roundInfoModel
        }
    }

}
