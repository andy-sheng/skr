package com.module.playways.race.room.model

import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.module.playways.race.room.event.*
import com.module.playways.room.prepare.model.BaseRoundInfoModel
import com.module.playways.room.song.model.SongModel
import com.zq.live.proto.RaceRoom.ERUserRole
import com.zq.live.proto.RaceRoom.ERWantSingType
import com.zq.live.proto.RaceRoom.ERaceRoundStatus
import com.zq.live.proto.RaceRoom.RaceRoundInfo
import org.greenrobot.eventbus.EventBus

class RaceRoundInfoModel : BaseRoundInfoModel() {

    //    protected int overReason; // 结束的原因
    //  protected int roundSeq;// 本局轮次
    var status = ERaceRoundStatus.ERRS_UNKNOWN.value // 轮次状态在擂台赛中使用
    var scores = ArrayList<RaceScore>()
    var subRoundSeq = 0 // 子轮次为1 代表第一轮A演唱 2 为第二轮B演唱
    var subRoundInfo = ArrayList<RaceSubRoundInfo>() //子轮次信息
    var games = ArrayList<RaceGamePlayInfo>() // choice 可选择的歌曲
    var playUsers = ArrayList<RacePlayerInfoModel>() // 选手
    var waitUsers = ArrayList<RacePlayerInfoModel>() // 观众
    var introBeginMs = 0 //竞选开始相对时间（相对于createTimeMs时间）
    var introEndMs = 0 // 竞选结束相对时间（相对于createTimeMs时间）
    var wantSingInfos = ArrayList<RaceWantSingInfo>() // 想唱信息列表

    // 以下不是服务器返回的
    var isParticipant = true// 我是不是这局的参与者，能不能抢唱，投票
    var elapsedTimeMs: Int = 0//这个轮次当前状态已经经过的时间，一般用于中途加入者使用,相对于子轮次开始的相对时间
    var enterStatus: Int = ERaceRoundStatus.ERRS_UNKNOWN.value//你进入房间当前轮次处于的状态


    override fun getType(): Int {
        return TYPE_RACE
    }

    /**
     * 有人进入房间
     */
    fun joinUser(racePlayerInfoModel: RacePlayerInfoModel) {
        if (racePlayerInfoModel.role == ERUserRole.ERUR_PLAY_USER.value) {
            if (!playUsers?.contains(racePlayerInfoModel)) {
                playUsers?.add(racePlayerInfoModel)
                EventBus.getDefault().post(RacePlaySeatUpdateEvent(playUsers))
            }
        } else if (racePlayerInfoModel.role == ERUserRole.ERUR_WAIT_USER.value) {
            if (!waitUsers?.contains(racePlayerInfoModel)) {
                waitUsers?.add(racePlayerInfoModel)
                EventBus.getDefault().post(RaceWaitSeatUpdateEvent(playUsers))
            }
        }
    }

    /**
     * 有人离开房间
     */
    fun exitUser(userId: Int) {
        kotlin.run {
            var i = 0
            for (p in playUsers) {
                if (p.userInfo.userId == userId) {
                    playUsers.removeAt(i)
                    EventBus.getDefault().post(RacePlaySeatUpdateEvent(playUsers))
                    return
                }
                i++
            }
        }
        kotlin.run {
            var i = 0
            for (p in waitUsers) {
                if (p.userInfo.userId == userId) {
                    waitUsers.removeAt(i)
                    EventBus.getDefault().post(RaceWaitSeatUpdateEvent(playUsers))
                    break
                }
                i++
            }
        }

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
     * wantSing 增加人
     */
    fun addWantSingChange(choiceID: Int, userID: Int) {
        val raceWantSingInfo = RaceWantSingInfo().apply {
            this.choiceID = choiceID
            this.userID = userID
            this.timeMs = System.currentTimeMillis()
        }

        if (!wantSingInfos.contains(raceWantSingInfo)) {
            userID?.let {
                wantSingInfos.add(raceWantSingInfo)
                EventBus.getDefault().post(RaceWantSingChanceEvent(choiceID, it))
            }
        }
    }

    fun addBLightUser(notify: Boolean, userID: Int, subRoundSeq: Int, bLightCnt: Int) {
        scores.getOrNull(subRoundSeq - 1)?.addBLightUser(notify, userID, bLightCnt)
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
        if (roundInfo.games.size > 0) {
            //有数据
            if (this.games.isEmpty()) {
                this.games.addAll(roundInfo.games)
            } else {
                // 都有数据

            }
        }
        if (roundInfo.subRoundInfo.size > 0) {
            //有数据
            if (this.subRoundInfo.isEmpty()) {
                this.subRoundInfo.addAll(roundInfo.subRoundInfo)
            } else {
                // 都有数据

            }
        }

        if (roundInfo.scores.size > 0) {
            //有数据
            if (this.scores.isEmpty()) {
                this.scores.addAll(roundInfo.scores)
            } else {
                // 都有数据
            }
        }
        if (this.subRoundSeq != roundInfo.subRoundSeq && this.status == roundInfo.status) {
            val old = this.subRoundSeq
            this.subRoundSeq = roundInfo.subRoundSeq
            // 子轮次有切换
            EventBus.getDefault().post(RaceSubRoundChangeEvent(this, old))
        }
        // 更新 sub
        updateStatus(notify, roundInfo.status)
        return
    }

    /**
     * 看这个userID 是不是这个大轮次的演唱者
     *
     */
    fun isSingerByUserId(userId: Int): Boolean {
        if (this.subRoundInfo.getOrNull(0)?.userID == userId) {
            return true
        }
        if (this.subRoundInfo.getOrNull(1)?.userID == userId) {
            return true
        }
        return false
    }

    /**
     * 当前子轮次是不是正由 userID 在演唱
     */
    fun isSingerNowByUserId(userId: Int): Boolean {
        if (this.subRoundInfo.getOrNull(subRoundSeq - 1)?.userID == userId) {
            return true
        }
        return false
    }

    /**
     * 此时此刻是否由自己演唱
     */
    fun isSingerNowBySelf(): Boolean {
        return isSingerNowByUserId(MyUserInfoManager.getInstance().uid.toInt())
    }

    /**
     * 此时此刻演唱的歌曲信息
     */
    fun getSongModelNow(): SongModel? {
        return getSongModelByChoiceId(subRoundInfo.getOrNull(subRoundSeq - 1)?.choiceID ?: 0)
    }

    /**
     * 此时此刻演唱的歌曲信息 根据 choiceID 查找
     */
    fun getSongModelByChoiceId(choiceID: Int): SongModel? {
        return games.getOrNull(choiceID - 1)?.commonMusic
    }

    /**
     * 此时此刻的轮次是否是伴奏模式
     */
    fun isAccRoundNow(): Boolean {
        return isAccRoundBySubRoundSeq(subRoundSeq)
    }

    /**
     * * 轮次是否是伴奏模式 更具子轮次 seq 查找
     */
    fun isAccRoundBySubRoundSeq(subRoundSeq: Int): Boolean {
        return subRoundInfo.getOrNull(subRoundSeq - 1)?.wantSingType == ERWantSingType.ERWST_ACCOMPANY.value
    }

    /**
     *  当前轮次的总时间
     */
    fun getSingTotalMs(): Int {
        val endMs = subRoundInfo.getOrNull(subRoundSeq - 1)?.endMs ?: 0
        val beginMs = subRoundInfo.getOrNull(subRoundSeq - 1)?.beginMs ?: 0
        var totalMs = endMs - beginMs
        if (totalMs <= 0) {
            totalMs = 20 * 1000
        }
        return totalMs
    }

    fun getSingerIdNow(): Int {
        return subRoundInfo.getOrNull(subRoundSeq - 1)?.userID ?:0
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

internal fun parseFromRoundInfoPB(pb: RaceRoundInfo): RaceRoundInfoModel {
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
    model.introBeginMs = pb.introBeginMs
    model.introEndMs = pb.introEndMs
    pb.wantSingInfosList.forEach {
        model.wantSingInfos.add(parseFromWantSingInfoPB(it))
    }
    return model
}

