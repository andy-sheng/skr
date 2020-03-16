package com.module.playways.battle.room.model

import com.common.log.MyLog
import com.module.playways.battle.room.event.BattleRoundStatusChangeEvent
import com.module.playways.room.data.H
import com.module.playways.room.prepare.model.BaseRoundInfoModel
import com.module.playways.room.song.model.SongModel
import com.zq.live.proto.BattleRoom.BRoundInfo
import com.zq.live.proto.BattleRoom.EBRoundStatus
import com.zq.live.proto.BattleRoom.EBUserStatus
import org.greenrobot.eventbus.EventBus


class BattleRoundInfoModel : BaseRoundInfoModel() {

    var hasSendRoundOverInfo = false

    var userID = 0 // 这局的演唱者id
    var musicSeq = 1 // 这局歌曲的序号

    var waitBeginMs = 0 //导唱开始相对时间（相对于createdTimeMs时间） p.s.导唱为等待阶段
    var waitEndMs = 0 //导唱结束相对时间（相对于createdTimeMs时间） p.s.导唱为等待阶段
    var singBeginMs = 0 //演唱开始相对时间（相对于createdTimeMs时间）
    var singEndMs = 0 //演唱结束相对时间（相对于createdTimeMs时间）

    var status = EBRoundStatus.BRS_UNKNOWN.value

    var music: SongModel? = null

    var result: BattleRoundResultModel? = null
    // userStatus 轮次状态
    var userStatus: List<BattleUserStatus>? = null

    var card: BattleCardInfoModel? = null

    override fun getType(): Int {
        return TYPE_BATTLE
    }

    fun updateStatus(notify: Boolean, statusGrab: Int) {
        if (getStatusPriority(status) < getStatusPriority(statusGrab)) {
            val old = status
            status = statusGrab
            if (notify) {
                EventBus.getDefault().post(BattleRoundStatusChangeEvent(this, old))
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

        val roundInfo = round as BattleRoundInfoModel
        if (this.userID == 0) {
            this.userID = roundInfo.userID
        }

        this.setRoundSeq(roundInfo.getRoundSeq())
        this.musicSeq = roundInfo.musicSeq

        this.waitBeginMs = roundInfo.waitBeginMs
        this.waitEndMs = roundInfo.waitEndMs

        this.singBeginMs = roundInfo.singBeginMs
        this.singEndMs = roundInfo.singEndMs

        if (this.music == null) {
            this.music = roundInfo.music
        }

        if (this.result == null) {
            this.result = roundInfo.result
        }


        if (this.card == null) {
            this.card = roundInfo.card
        }
        // 观众席与玩家席更新，以最新的为准

        if (roundInfo.getOverReason() > 0) {
            this.setOverReason(roundInfo.getOverReason())
        }
        updateStatus(notify, roundInfo.status)

        return
    }

    /**
     * 是否全部离线
     * params myTeamTag我的队伍标识
     */
    fun isOpAllOff(myTeamTag: String?): Boolean {
        userStatus?.forEach {
            if (!it.teamTag?.equals(myTeamTag) && it.status == EBUserStatus.EBUS_ONLINE.value) {
                // 不是和我一队的,并且在线
                return false
            }
        }
        return true
    }

    /**
     * 返回帮唱卡的用户id ，被帮唱的人的id
     */
    fun getHelpUserId(): Int {
        return card?.helpCard?.userID ?: 0
    }

    override fun toString(): String {
        return "BattleRoundInfoModel(hasSendRoundOverInfo=$hasSendRoundOverInfo, userID=$userID, musicSeq=$musicSeq, waitBeginMs=$waitBeginMs, waitEndMs=$waitEndMs, singBeginMs=$singBeginMs, singEndMs=$singEndMs, status=$status, music=$music, result=$result, card=$card)"
    }


    //
    companion object {

        fun parseFromRoundInfo(roundInfo: BRoundInfo): BattleRoundInfoModel {
            val roundInfoModel = BattleRoundInfoModel()

            roundInfoModel.userID = roundInfo.userID

            roundInfoModel.setRoundSeq(roundInfo.roundSeq!!)
            roundInfoModel.musicSeq = roundInfo.musicSeq

            roundInfoModel.waitBeginMs = roundInfo.waitBeginMs
            roundInfoModel.waitEndMs = roundInfo.waitEndMs

            roundInfoModel.singBeginMs = roundInfo.singBeginMs
            roundInfoModel.singEndMs = roundInfo.singEndMs

            roundInfoModel.status = roundInfo.status.value

            roundInfoModel.overReason = roundInfo.overReason.value


            var list = ArrayList<BattleUserStatus>()
            roundInfo.userStatusList?.forEach {
                list.add(BattleUserStatus.parseFromPb(it))
            }
            roundInfoModel.userStatus = list

            var songModel = SongModel()
            songModel.parse(roundInfo.music)
            roundInfoModel.music = songModel

            roundInfoModel.result = BattleRoundResultModel.parseFromPb(roundInfo.result)

            roundInfoModel.card = BattleCardInfoModel.parseFromPb(roundInfo.card)
            return roundInfoModel
        }
    }

}
