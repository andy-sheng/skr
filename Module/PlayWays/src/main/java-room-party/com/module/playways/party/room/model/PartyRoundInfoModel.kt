package com.module.playways.party.room.model

import com.common.log.MyLog
import com.component.busilib.model.EffectModel
import com.module.playways.relay.room.event.RelayRoundStatusChangeEvent
import com.module.playways.room.prepare.model.BaseRoundInfoModel
import com.module.playways.room.song.model.SongModel
import com.zq.live.proto.Common.BackgroundShowInfo
import com.zq.live.proto.PartyRoom.PRoundInfo
import com.zq.live.proto.RelayRoom.ERRoundStatus
import com.zq.live.proto.RelayRoom.RRoundInfo
import org.greenrobot.eventbus.EventBus
import java.util.*
import kotlin.collections.ArrayList


class PartyRoundInfoModel : BaseRoundInfoModel() {

    var status = 0
    var beginMs: Int = 0 //开始相对时间（相对于createdTimeMs时间）
    var endMs: Int = 0 //结束相对时间（相对于createdTimeMs时间）
    var itemInfo: PartyGameInfoModel? = null
    var hasNextItem = false

    override fun getType(): Int {
        return TYPE_PARTY
    }

    //
//    fun updateStatus(notify: Boolean, statusGrab: Int) {
//        if (getStatusPriority(status) < getStatusPriority(statusGrab)) {
//            val old = status
//            status = statusGrab
//            if (notify) {
//                EventBus.getDefault().post(RelayRoundStatusChangeEvent(this, old))
//            }
//        }
//    }
//
//    /**
//     * 重排一下状态机的优先级
//     *
//     * @param status
//     * @return
//     */
//    internal fun getStatusPriority(status: Int): Int {
//        return status
//    }
//
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
        if (roundInfo.itemInfo != null) {
            this.itemInfo = roundInfo.itemInfo
        }
//        updateStatus(notify, roundInfo.status)
        return
    }
//
//
    override fun toString(): String {
        return "PartyRoundInfoModel{" +
                "roundSeq=" + roundSeq +
//                ", status=" + status +
//                ", songModel=" + (if (music == null) "" else music!!.toSimpleString()) +
//                ", singBeginMs=" + singBeginMs +
                ", itemInfo=" + itemInfo +
                ", hasNextItem=" + hasNextItem +
//                ", overReason=" + overReason +
                '}'.toString()
    }
//
    companion object {

        fun parseFromRoundInfo(roundInfo: PRoundInfo): PartyRoundInfoModel {
            val roundInfoModel = PartyRoundInfoModel()
            roundInfoModel.setRoundSeq(roundInfo.roundSeq!!)
            roundInfoModel.beginMs = roundInfo.beginMs
            roundInfoModel.endMs = roundInfo.endMs
            roundInfoModel.itemInfo = PartyGameInfoModel.parseFromItemInfo(roundInfo.itemInfo)
            roundInfoModel.hasNextItem = roundInfo.hasNextItem
            return roundInfoModel
        }
    }

}
