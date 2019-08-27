package com.module.playways.race.room.model

import com.common.log.MyLog
import com.module.playways.grab.room.event.GrabPlaySeatUpdateEvent
import com.module.playways.grab.room.event.GrabRoundStatusChangeEvent
import com.module.playways.grab.room.model.GrabPlayerInfoModel
import com.module.playways.grab.room.model.GrabRoundInfoModel
import com.module.playways.race.room.event.RacePlaySeatUpdateEvent
import com.module.playways.race.room.event.RaceRoundStatusChangeEvent
import com.module.playways.room.prepare.model.BaseRoundInfoModel
import com.module.playways.room.song.model.SongModel
import com.zq.live.proto.RaceRoom.ERaceRoundStatus
import com.zq.live.proto.Room.EQRoundStatus
import com.zq.live.proto.Room.EWantSingType
import org.greenrobot.eventbus.EventBus

class RaceRoundInfoModel : BaseRoundInfoModel() {

    //    protected int overReason; // 结束的原因
    //  protected int roundSeq;// 本局轮次
    var status = ERaceRoundStatus.ERRS_UNKNOWN.value // 轮次状态在擂台赛中使用
    var scores: ArrayList<RaceScore>? = null
    var subRoundSeq = 0
    var subRoundInfo: ArrayList<RaceSubRoundInfo>? = null
    var games: ArrayList<RaceGameInfo>? = null
    var playUsers: ArrayList<RacePlayerInfoModel>? = null
    var waitUsers: ArrayList<RacePlayerInfoModel>? = null

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
            if (playUsers?.size == roundInfo.playUsers?.size) {
                var i = 0
                while (i < (playUsers?.size ?: 0) && i < (roundInfo.playUsers?.size ?: 0)) {
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
            if (waitUsers?.size == roundInfo.waitUsers?.size) {
                var i = 0
                while (i < (waitUsers?.size ?: 0) && i < (roundInfo.waitUsers?.size ?: 0)) {
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

