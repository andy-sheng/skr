package com.module.playways.relay.room.model
import com.common.log.MyLog
import com.module.playways.relay.room.event.RelayRoundStatusChangeEvent
import com.module.playways.room.prepare.model.BaseRoundInfoModel
import com.module.playways.room.song.model.SongModel
import com.zq.live.proto.RelayRoom.ERRoundStatus
import com.zq.live.proto.RelayRoom.RRoundInfo
import org.greenrobot.eventbus.EventBus
import java.util.*


class RelayRoundInfoModel : BaseRoundInfoModel() {

    var accLoadingOk = false
    /* 一唱到底使用 */
    var status = ERRoundStatus.RRS_UNKNOWN.value

    var music: SongModel? = null//本轮次要唱的歌儿的详细信息
    var singBeginMs: Int = 0 // 轮次开始时间 相对时间
    var singEndMs: Int = 0 // 轮次结束时间
    var originId:Int = 0
    /**
     * 判断是各种演唱阶段
     *
     * @return
     */
    val isSingStatus: Boolean
        get() = (status == ERRoundStatus.RRS_SING.value)

    /**
     * 该轮次的总时间，之前用的是歌曲内的总时间，但是不灵活，现在都放在服务器的轮次信息的 begin 和 end 里
     *
     */
    /**
     * pk第一轮和第二轮的演唱时间 和 歌曲截取的部位不一样
     */
    val singTotalMs: Int
        get() {
                var totalTs = singEndMs - singBeginMs
            if (totalTs <= 0) {
                    totalTs = 4* 60 * 1000
            }
            return totalTs
        }

    override fun getType(): Int {
        return BaseRoundInfoModel.TYPE_RELAY
    }

    fun updateStatus(notify: Boolean, statusGrab: Int) {
        if (getStatusPriority(status) < getStatusPriority(statusGrab)) {
            val old = status
            status = statusGrab
            if (notify) {
                EventBus.getDefault().post(RelayRoundStatusChangeEvent(this, old))
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

    /**
     * 一唱到底使用
     */
    override fun tryUpdateRoundInfoModel(round: BaseRoundInfoModel?, notify: Boolean) {
        if (round == null) {
            MyLog.e("JsonRoundInfo RoundInfo == null")
            return
        }
        val roundInfo = round as RelayRoundInfoModel
        this.setRoundSeq(roundInfo.getRoundSeq())
        this.singBeginMs = roundInfo.singBeginMs
        this.singEndMs = roundInfo.singEndMs
        if (this.music == null) {
            this.music = roundInfo.music
        }
        // 观众席与玩家席更新，以最新的为准

        if (roundInfo.getOverReason() > 0) {
            this.setOverReason(roundInfo.getOverReason())
        }
        updateStatus(notify, roundInfo.status)
        return
    }


    override fun toString(): String {
        return "RelayRoundInfoModel{" +
                "roundSeq=" + roundSeq +
                ", status=" + status +
                ", songModel=" + (if (music == null) "" else music!!.toSimpleString()) +
                ", singBeginMs=" + singBeginMs +
                ", singEndMs=" + singEndMs +
                ", overReason=" + overReason +
                '}'.toString()
    }

    companion object {

        fun parseFromRoundInfo(roundInfo: RRoundInfo): RelayRoundInfoModel {
            val roundInfoModel = RelayRoundInfoModel()
            roundInfoModel.setRoundSeq(roundInfo.roundSeq!!)
            roundInfoModel.singBeginMs = roundInfo.singBeginMs
            roundInfoModel.singEndMs = roundInfo.singEndMs
            // 轮次状态
            roundInfoModel.status = roundInfo.status.value

            roundInfoModel.setOverReason(roundInfo.overReason.value)
//            roundInfoModel.resultType = roundInfo.resultType.value

            val songModel = SongModel()
            songModel.parse(roundInfo.music)
            roundInfoModel.music = songModel

            return roundInfoModel
        }
    }

}
