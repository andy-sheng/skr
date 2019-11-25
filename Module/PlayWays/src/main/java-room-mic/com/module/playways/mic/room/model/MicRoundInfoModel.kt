package com.module.playways.mic.room.model

import com.alibaba.fastjson.annotation.JSONField
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.component.busilib.model.BackgroundEffectModel
import com.module.playways.grab.room.event.GrabChorusUserStatusChangeEvent
import com.module.playways.grab.room.model.ChorusRoundInfoModel
import com.module.playways.grab.room.model.SPkRoundInfoModel
import com.module.playways.mic.room.event.MicPlaySeatUpdateEvent
import com.module.playways.mic.room.event.MicRoundStatusChangeEvent
import com.module.playways.room.prepare.model.BaseRoundInfoModel
import com.module.playways.room.song.model.SongModel
import com.zq.live.proto.Common.StandPlayType
import com.zq.live.proto.MicRoom.EMRoundStatus
import com.zq.live.proto.MicRoom.EMWantSingType
import com.zq.live.proto.MicRoom.MRoundInfo
import org.greenrobot.eventbus.EventBus
import java.util.*


class MicRoundInfoModel : BaseRoundInfoModel() {

    /* 一唱到底使用 */
    var status = EMRoundStatus.MRS_UNKNOWN.value

    @JSONField(name = "users")
    private var playUsers: ArrayList<MicPlayerInfoModel> = ArrayList() // 参与这轮游戏中的人，包括离线

    //0未知
    //1有种优秀叫一唱到底（全部唱完）
    //2有种结束叫刚刚开始（t<30%）
    //3有份悲伤叫都没及格(30%<=t <60%)
    //4有种遗憾叫明明可以（60%<=t<90%）
    //5有种可惜叫我觉得你行（90%<=t<=100%)
//    var resultType: Int = 0 // 结果类型

    var isParticipant = true// 我是不是这局的参与者

    var elapsedTimeMs: Int = 0//这个轮次当前状态已经经过的时间，一般用于中途加入者使用

    var enterStatus: Int = 0//你进入这个轮次处于的状态

    /**
     * EWST_DEFAULT = 0; //默认抢唱类型：普通
     * EWST_ACCOMPANY = 1; //带伴奏抢唱
     * EWST_COMMON_OVER_TIME = 2; //普通加时抢唱
     * EWST_ACCOMPANY_OVER_TIME = 3; //带伴奏加时抢唱
     */
    var wantSingType = EMWantSingType.MWST_UNKNOWN.value

    @JSONField(name = "CHORoundInfos")
    internal var chorusRoundInfoModels: ArrayList<ChorusRoundInfoModel> = ArrayList()

    @JSONField(name = "SPKRoundInfos")
    internal var sPkRoundInfoModels: ArrayList<SPkRoundInfoModel> = ArrayList()

    @JSONField(name = "showInfos")
    internal var showInfos: ArrayList<BackgroundEffectModel> = ArrayList()

    var userID: Int = 0// 本人在演唱的人
    var music: SongModel? = null//本轮次要唱的歌儿的详细信息
    var singBeginMs: Int = 0 // 轮次开始时间
    var singEndMs: Int = 0 // 轮次结束时间
    var startTs: Long = 0// 开始时间，服务器的
    var endTs: Long = 0// 结束时间，服务器的
    var sysScore: Int = 0//本轮系统打分，先搞个默认60分
    var isHasSing = false// 是否已经在演唱，依据时引擎等回调，不是作为是否演唱阶段的依据

    var commonRoundResult: MicRoundResult? = null
    /**
     * 是否还在房间，用来sync优化
     * @return
     */
    val isContainInRoom: Boolean
        get() {
            for (grabPlayerInfoModel in playUsers) {
                if (grabPlayerInfoModel.userID.toLong() == MyUserInfoManager.uid) {
                    return true
                }
            }

            return false
        }

    val isEnterInSingStatus: Boolean
        get() = (enterStatus == EMRoundStatus.MRS_SING.value
                || enterStatus == EMRoundStatus.MRS_CHO_SING.value
                || enterStatus == EMRoundStatus.MRS_SPK_FIRST_PEER_SING.value
                || enterStatus == EMRoundStatus.MRS_SPK_SECOND_PEER_SING.value)

    val isAccRound: Boolean
        get() = wantSingType == EMWantSingType.MWST_ACCOMPANY.value || wantSingType == EMWantSingType.MWST_SPK.value

    /**
     * 判断是各种演唱阶段
     *
     * @return
     */
    val isSingStatus: Boolean
        get() = (status == EMRoundStatus.MRS_SING.value
                || status == EMRoundStatus.MRS_CHO_SING.value
                || status == EMRoundStatus.MRS_SPK_FIRST_PEER_SING.value
                || status == EMRoundStatus.MRS_SPK_SECOND_PEER_SING.value)


    /**
     * 是否是pk 游戏中 轮次
     *
     * @return
     */
    val isNormalRound: Boolean
        get() = music?.playType == StandPlayType.PT_COMMON_TYPE.value

    /**
     * 是否是合唱 游戏中 轮次
     *
     * @return
     */
    val isChorusRound: Boolean
        get() = music?.playType == StandPlayType.PT_CHO_TYPE.value


    /**
     * 是否是pk 游戏中 轮次
     *
     * @return
     */
    val isPKRound: Boolean
        get() = music?.playType == StandPlayType.PT_SPK_TYPE.value


    /**
     * 返回当前演唱者的id信息
     *
     * @return
     */
    val singUserIds: List<Int>
        get() {
            val singerUserIds = ArrayList<Int>()
            if (isPKRound) {
                for (infoModel in getsPkRoundInfoModels()) {
                    singerUserIds.add(infoModel.userID)
                }
            } else if (isChorusRound) {
                for (infoModel in getChorusRoundInfoModels()) {
                    singerUserIds.add(infoModel.userID)
                }
            } else {
                singerUserIds.add(userID)
            }
            return singerUserIds
        }

    /**
     * 该轮次的总时间，之前用的是歌曲内的总时间，但是不灵活，现在都放在服务器的轮次信息的 begin 和 end 里
     *
     */
    /**
     * pk第一轮和第二轮的演唱时间 和 歌曲截取的部位不一样
     */
    val singTotalMs: Int
        get() {
            var totalTs = 0
            if (status == EMRoundStatus.MRS_SPK_SECOND_PEER_SING.value && getsPkRoundInfoModels().size > 1) {
                totalTs = getsPkRoundInfoModels()[1].singEndMs - getsPkRoundInfoModels()[1].singBeginMs
            } else if (status == EMRoundStatus.MRS_SPK_FIRST_PEER_SING.value && getsPkRoundInfoModels().size > 0) {
                totalTs = getsPkRoundInfoModels()[0].singEndMs - getsPkRoundInfoModels()[0].singBeginMs
            } else {
                totalTs = singEndMs - singBeginMs
            }
            if (totalTs <= 0) {
                MyLog.d(TAG, "playLyric" + " totalTs时间不合法,做矫正")
                if (wantSingType == EMWantSingType.MWST_COMMON.value) {
                    totalTs = 20 * 1000
                } else if (wantSingType == EMWantSingType.MWST_ACCOMPANY.value) {
                    totalTs = 30 * 1000
                } else if (wantSingType == EMWantSingType.MWST_CHORUS.value) {
                    totalTs = 40 * 1000
                } else if (wantSingType == EMWantSingType.MWST_SPK.value) {
                    totalTs = 30 * 1000
                } else {
                    totalTs = 20 * 1000
                }
            }
            return totalTs
        }

    override fun getType(): Int {
        return BaseRoundInfoModel.TYPE_MIC
    }

    fun getPlayUsers(): ArrayList<MicPlayerInfoModel> {
        return playUsers
    }

    fun setPlayUsers(playUsers: ArrayList<MicPlayerInfoModel>) {
        this.playUsers = playUsers
    }

    fun updateStatus(notify: Boolean, statusGrab: Int) {
        if (getStatusPriority(status) < getStatusPriority(statusGrab)) {
            val old = status
            status = statusGrab
            if (notify) {
                EventBus.getDefault().post(MicRoundStatusChangeEvent(this, old))
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


    fun addPlayUser(notify: Boolean, grabPlayerInfoModel: MicPlayerInfoModel): Boolean {
        if (!playUsers.contains(grabPlayerInfoModel)) {
            playUsers.add(grabPlayerInfoModel)
            if (notify) {
                val event = MicPlaySeatUpdateEvent(playUsers)
                EventBus.getDefault().post(event)
            }
            return true
        }
        return false
    }

    private fun updatePlayUsers(l: List<MicPlayerInfoModel>, notify: Boolean) {
        playUsers.clear()
        playUsers.addAll(l)
        if (notify) {
            EventBus.getDefault().post(MicPlaySeatUpdateEvent(playUsers))
        }
    }

    /**
     * 一唱到底使用
     */
    override fun tryUpdateRoundInfoModel(round: BaseRoundInfoModel?, notify: Boolean) {
        if (round == null) {
            MyLog.e("JsonRoundInfo RoundInfo == null")
            return
        }
        val roundInfo = round as MicRoundInfoModel?
        this.userID = roundInfo!!.userID
        this.setRoundSeq(roundInfo.getRoundSeq())
        this.singBeginMs = roundInfo.singBeginMs
        this.singEndMs = roundInfo.singEndMs
        if (this.music == null) {
            this.music = roundInfo.music
        }
        // 观众席与玩家席更新，以最新的为准
        run {
            var needUpdate = false
            if (playUsers.size == roundInfo.getPlayUsers().size) {
                var i = 0
                while (i < roundInfo.getPlayUsers().size && i < playUsers.size) {
                    val infoModel1 = playUsers[i]
                    val infoModel2 = roundInfo.getPlayUsers()[i]
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
                updatePlayUsers(roundInfo.getPlayUsers(), notify)
            }
        }

        if (roundInfo.getOverReason() > 0) {
            this.setOverReason(roundInfo.getOverReason())
        }
//        if (roundInfo.resultType > 0) {
//            this.resultType = roundInfo.resultType
//        }
        this.wantSingType = roundInfo.wantSingType

        // 更新合唱信息
        if (wantSingType == EMWantSingType.MWST_CHORUS.value) {
            if (this.getChorusRoundInfoModels().size <= 1) {
                // 不满足两人通知全量更新
                this.getChorusRoundInfoModels().clear()
                this.getChorusRoundInfoModels().addAll(roundInfo.getChorusRoundInfoModels())
            } else {
                var i = 0
                while (i < this.getChorusRoundInfoModels().size && i < roundInfo.getChorusRoundInfoModels().size) {
                    val chorusRoundInfoModel1 = this.getChorusRoundInfoModels()[i]
                    val chorusRoundInfoModel2 = roundInfo.getChorusRoundInfoModels()[i]
                    chorusRoundInfoModel1.tryUpdateRoundInfoModel(chorusRoundInfoModel2)
                    i++
                }
            }
        }

        // 更新pk信息
        if (wantSingType == EMWantSingType.MWST_SPK.value) {
            // pk房间
            if (this.getsPkRoundInfoModels().size <= 1) {
                // 不满足两人通知全量更新
                this.getsPkRoundInfoModels().clear()
                this.getsPkRoundInfoModels().addAll(roundInfo.getsPkRoundInfoModels())
            } else {
                var i = 0
                while (i < this.getsPkRoundInfoModels().size && i < roundInfo.getsPkRoundInfoModels().size) {
                    val sPkRoundInfoModel1 = this.getsPkRoundInfoModels()[i]
                    val sPkRoundInfoModel2 = roundInfo.getsPkRoundInfoModels()[i]
                    sPkRoundInfoModel1.tryUpdateRoundInfoModel(sPkRoundInfoModel2)
                    i++
                }
            }
        }
        if (commonRoundResult == null || commonRoundResult?.finalMsg?.isEmpty() == true) {
            commonRoundResult = roundInfo.commonRoundResult
        }

        showInfos.clear()
        if (round.showInfos != null && round.showInfos.size > 0) {
            showInfos.addAll(round.showInfos)
        }
        updateStatus(notify, roundInfo.status)
        return
    }

    /**
     * 一唱到底合唱某人放弃了演唱
     *
     * @param userID
     */
    fun giveUpInChorus(userID: Int) {
        for (i in 0 until this.getChorusRoundInfoModels().size) {
            val chorusRoundInfoModel = this.getChorusRoundInfoModels()[i]
            if (chorusRoundInfoModel.userID == userID) {
                if (!chorusRoundInfoModel.isHasGiveUp) {
                    chorusRoundInfoModel.isHasGiveUp = true
                    EventBus.getDefault().post(GrabChorusUserStatusChangeEvent(chorusRoundInfoModel))
                }
            }
        }
    }

    fun addUser(b: Boolean, playerInfoModel: MicPlayerInfoModel): Boolean {
        return addPlayUser(b, playerInfoModel)
    }

    fun removeUser(notify: Boolean, uid: Int) {
        for (i in playUsers.indices) {
            val infoModel = playUsers[i]
            if (infoModel.userID == uid) {
                playUsers.remove(infoModel)
                if (notify) {
                    EventBus.getDefault().post(MicPlaySeatUpdateEvent(playUsers))
                }
                break
            }
        }
    }

    fun getChorusRoundInfoModels(): ArrayList<ChorusRoundInfoModel> {
        return chorusRoundInfoModels
    }

    fun setChorusRoundInfoModels(chorusRoundInfoModels: ArrayList<ChorusRoundInfoModel>) {
        this.chorusRoundInfoModels = chorusRoundInfoModels
    }

    fun getsPkRoundInfoModels(): ArrayList<SPkRoundInfoModel> {
        return sPkRoundInfoModels
    }

    fun setsPkRoundInfoModels(sPkRoundInfoModels: ArrayList<SPkRoundInfoModel>) {
        this.sPkRoundInfoModels = sPkRoundInfoModels
    }


    /**
     * 判断当前是否是自己的演唱轮次
     *
     * @return
     */
    fun singBySelf(): Boolean {
        if (status == EMRoundStatus.MRS_SING.value) {
            return userID.toLong() == MyUserInfoManager.uid
        } else if (status == EMRoundStatus.MRS_CHO_SING.value) {
            for (roundInfoModel in chorusRoundInfoModels) {
                if (roundInfoModel.userID.toLong() == MyUserInfoManager.uid && isParticipant) {
                    return true
                }
            }
        } else if (status == EMRoundStatus.MRS_SPK_FIRST_PEER_SING.value) {
            if (getsPkRoundInfoModels().isNotEmpty()) {
                return getsPkRoundInfoModels()[0].userID.toLong() == MyUserInfoManager.uid
            }
        } else if (status == EMRoundStatus.MRS_SPK_SECOND_PEER_SING.value) {
            if (getsPkRoundInfoModels().size > 1) {
                return getsPkRoundInfoModels()[1].userID.toLong() == MyUserInfoManager.uid
            }
        } else if (status == EMRoundStatus.MRS_END.value) {
            // 如果轮次都结束了 还要判断出这个轮次是不是自己唱的
            if (userID.toLong() == MyUserInfoManager.uid) {
                return true
            }
            for (roundInfoModel in chorusRoundInfoModels) {
                if (roundInfoModel.userID.toLong() == MyUserInfoManager.uid && isParticipant) {
                    return true
                }
            }
            if (getsPkRoundInfoModels().isNotEmpty()) {
                if (getsPkRoundInfoModels()[0].userID.toLong() == MyUserInfoManager.uid) {
                    return true
                }
                if (getsPkRoundInfoModels().size > 1) {
                    if (getsPkRoundInfoModels()[1].userID.toLong() == MyUserInfoManager.uid) {
                        return true
                    }
                }
            }
        }
        return false
    }


    /**
     * 当前轮次当前阶段是否由 userId 演唱
     *
     * @param userId
     * @return
     */
    fun singByUserId(userId: Int): Boolean {
        if (status == EMRoundStatus.MRS_SING.value) {
            return userID == userId
        } else if (status == EMRoundStatus.MRS_CHO_SING.value) {
            for (roundInfoModel in chorusRoundInfoModels) {
                if (roundInfoModel.userID == userId) {
                    return true
                }
            }
        } else if (status == EMRoundStatus.MRS_SPK_FIRST_PEER_SING.value) {
            if (getsPkRoundInfoModels().size > 0) {
                return getsPkRoundInfoModels()[0].userID == userId
            }
        } else if (status == EMRoundStatus.MRS_SPK_SECOND_PEER_SING.value) {
            if (getsPkRoundInfoModels().size > 1) {
                return getsPkRoundInfoModels()[1].userID == userId
            }
        }
        return false
    }

    override fun toString(): String {
        return "MicRoundInfoModel{" +
                "roundSeq=" + roundSeq +
                ", status=" + status +
                ", userID=" + userID +
                ", wantSingType=" + wantSingType +
                ", songModel=" + (if (music == null) "" else music!!.toSimpleString()) +
                ", singBeginMs=" + singBeginMs +
                ", singEndMs=" + singEndMs +
                //                ", startTs=" + startTs +
                //                ", endTs=" + endTs +
                //                ", sysScore=" + sysScore +
                ", hasSing=" + isHasSing +
                ", overReason=" + overReason +
                ", playUsers=" + playUsers +
//                ", resultType=" + resultType +
                ", isParticipant=" + isParticipant +
                ", elapsedTimeMs=" + elapsedTimeMs +
                ", enterStatus=" + enterStatus +
                ",chorusRoundInfoModels=" + chorusRoundInfoModels +
                ", sPkRoundInfoModels=" + sPkRoundInfoModels +
                ", MicRoundResult=" + commonRoundResult +
                '}'.toString()
    }

    companion object {

        fun parseFromRoundInfo(roundInfo: MRoundInfo): MicRoundInfoModel {
            val roundInfoModel = MicRoundInfoModel()
            roundInfoModel.userID = roundInfo.userID
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


            // 玩家
            for (m in roundInfo.usersList) {
                val grabPlayerInfoModel = parseFromROnlineInfoPB(m)
                roundInfoModel.addPlayUser(false, grabPlayerInfoModel)
            }
            // 想唱类型
            roundInfoModel.wantSingType = roundInfo.wantSingType.value

            for (qchoInnerRoundInfo in roundInfo.choRoundInfosList) {
                val chorusRoundInfoModel = ChorusRoundInfoModel.parse(qchoInnerRoundInfo)
                roundInfoModel.getChorusRoundInfoModels().add(chorusRoundInfoModel)
            }

            for (qspkInnerRoundInfo in roundInfo.spkRoundInfosList) {
                val pkRoundInfoModel = SPkRoundInfoModel.parse(qspkInnerRoundInfo)
                roundInfoModel.getsPkRoundInfoModels().add(pkRoundInfoModel)
            }
            roundInfoModel.commonRoundResult = MicRoundResult.parseFromInfoPB(roundInfo.commonRoundResult)

            if (roundInfo.hasShowInfosList() && roundInfo.showInfosList.size > 0) {
                roundInfoModel.showInfos.addAll(BackgroundEffectModel.parseBackgroundEffectModelListFromPb(roundInfo.showInfosList))
            }
            return roundInfoModel
        }
    }

}
