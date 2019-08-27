package com.module.playways.race.room.model

import android.util.ArrayMap
import com.common.log.MyLog
import com.module.playways.race.room.event.RacePlaySeatUpdateEvent
import com.module.playways.race.room.event.RaceRoundStatusChangeEvent
import com.module.playways.race.room.event.RaceWantSingChanceEvent
import com.module.playways.room.prepare.model.BaseRoundInfoModel
import com.zq.live.proto.RaceRoom.ERaceRoundStatus
import com.zq.live.proto.RaceRoom.RaceRoundInfo
import org.greenrobot.eventbus.EventBus

class RaceRoundInfoModel : BaseRoundInfoModel() {

    //    protected int overReason; // 结束的原因
    //  protected int roundSeq;// 本局轮次
    var status = ERaceRoundStatus.ERRS_UNKNOWN.value // 轮次状态在擂台赛中使用
    var scores =  ArrayList<RaceScore>()
    var subRoundSeq = 0
    var subRoundInfo = ArrayList<RaceSubRoundInfo>()
    var games = ArrayList<RaceGameInfo>()
    var playUsers = ArrayList<RacePlayerInfoModel>()
    var waitUsers = ArrayList<RacePlayerInfoModel>()
    val gamesChoiceMap = ArrayMap<Int, ArrayList<Int>>()

    override fun getType(): Int {
        return TYPE_RACE
    }

    fun updatePlayUsers(l: List<RacePlayerInfoModel>?) {
        playUsers?.clear()
        l?.let {
            playUsers?.addAll(it)
        }
        EventBus.getDefault().post(RacePlaySeatUpdateEvent(playUsers))
    }

    fun updateWaitUsers(l: List<RacePlayerInfoModel>?) {
        waitUsers?.clear()
        l?.let {
            waitUsers?.addAll(it)
        }
        EventBus.getDefault().post(RacePlaySeatUpdateEvent(waitUsers))
    }

    fun addWantSingChange(choiceID: Int, userID: Int?) {
        var list = gamesChoiceMap[choiceID]
        if (list == null) {
            list = ArrayList()
            gamesChoiceMap[choiceID] = list
        }
        if (!list.contains(userID)) {
            userID?.let {
                list.add(it)
                EventBus.getDefault().post(RaceWantSingChanceEvent(choiceID, it))
            }
        }
    }

    /**
     * 更新状态
     */
    fun updateStatus(notify: Boolean, statusGrab: Int) {
        if (getStatusPriority(status) < getStatusPriority(statusGrab)) {
            val old = status
            status = statusGrab
            if (notify) {
                EventBus.getDefault().post(RaceRoundStatusChangeEvent(this, old))
            }
        }
    }

    override fun tryUpdateRoundInfoModel(round: BaseRoundInfoModel, notify: Boolean) {
        if (round == null) {
            MyLog.e("JsonRoundInfo RoundInfo == null")
            return
        }
        val roundInfo = round as RaceRoundInfoModel
        // 更新双方得票
        // 观众席与玩家席更新，以最新的为准
        run {
            var needUpdate = false
            if (playUsers.size == roundInfo.playUsers.size) {
                var i = 0
                while (i < playUsers.size && i < roundInfo.playUsers.size) {
                    val infoModel1 = playUsers?.get(i)
                    val infoModel2 = roundInfo.playUsers?.get(i)
                    if (infoModel1 != infoModel2) {
                        needUpdate = true
                        break
                    }
                    i++
                }
            } else {
                needUpdate = true
            }
            if (needUpdate) {
                updatePlayUsers(roundInfo?.playUsers)
            }
        }

        run {
            var needUpdate = false
            if (waitUsers.size == roundInfo.waitUsers.size) {
                var i = 0
                while (i < waitUsers.size && i < roundInfo.waitUsers.size) {
                    val infoModel1 = waitUsers?.get(i)
                    val infoModel2 = roundInfo.waitUsers?.get(i)
                    if (infoModel1 != infoModel2) {
                        needUpdate = true
                        break
                    }
                    i++
                }
            } else {
                needUpdate = true
            }
            if (needUpdate) {
                updateWaitUsers(roundInfo?.waitUsers)
            }
        }

        if (roundInfo.overReason > 0) {
            this.overReason = roundInfo.overReason
        }
        updateStatus(notify, roundInfo.status)
        return
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

internal fun parseFromRoundInfoPB(pb: RaceRoundInfo):RaceRoundInfoModel {
    val model = RaceRoundInfoModel()
    model.roundSeq = pb.roundSeq
    model.subRoundSeq = pb.subRoundSeq
    model.status = pb.status.value
    model.overReason = pb.overReason.value
    pb.subRoundInfoList.forEach {
        model.subRoundInfo.add(parseFromSubRoundInfoPB(it))
    }
    pb.scoresList.forEach {
        model.scores.add(parseFromRoundScoreInfoPB(it))
    }
    pb.waitUsersList.forEach {
        model.waitUsers.add(parseFromROnlineInfoPB(it))
    }
    pb.playUsersList.forEach {
        model.playUsers.add(parseFromROnlineInfoPB(it))
    }
    return model
}

