package com.module.playways.relay.room

import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.component.busilib.constans.GameModeType
import com.component.busilib.model.GameBackgroundEffectModel
import com.module.playways.BaseRoomData
import com.module.playways.RoomDataUtils
import com.module.playways.relay.match.model.JoinRelayRoomRspModel
import com.module.playways.relay.room.event.RelayLockChangeEvent
import com.module.playways.relay.room.event.RelayRoundChangeEvent
import com.module.playways.relay.room.model.RelayConfigModel
import com.module.playways.relay.room.model.RelayPlayerInfoModel
import com.module.playways.relay.room.model.RelayRoundInfoModel
import com.module.playways.room.prepare.model.PlayerInfoModel
import com.zq.live.proto.RelayRoom.ERRoundStatus
import org.greenrobot.eventbus.EventBus
import java.io.Serializable


class RelayRoomData : BaseRoomData<RelayRoundInfoModel>() {
    companion object {
        var MUSIC_PUBLISH_VOLUME = 85
    }

    override fun getPlayerAndWaiterInfoList(): List<PlayerInfoModel> {
        if (!isPersonArrive()) {
            return ArrayList()
        } else {
            return listOf(peerUser) as List<PlayerInfoModel>
        }
    }

    override fun getInSeatPlayerInfoList(): List<PlayerInfoModel> {
        if (!isPersonArrive()) {
            return ArrayList()
        } else {
            return listOf(peerUser) as List<PlayerInfoModel>
        }
    }

    override fun getCanGiveGiftList(): List<PlayerInfoModel> {
        return getInSeatPlayerInfoList()
    }

    var sentenceCnt = 10 //当前演唱歌曲的句子总数，用于打分用

    var lastSingerID: Int? = null

    /**
     * 本地时间比服务器时间快多少，使用专门的校时接口校对 t1+(t2-t1)/2 - s1
     * t1为本地发包时间 t2为本地收包时间  s1 为服务器收包时间
     *
     * createTs + beginTs 代表服务器期望的两端商量好的演唱开始时间
     * System.currentTimeMillis() - shiftTsForRelay 与 createTs + beginTs 进行比较能得到演唱应该的进度
     */
    var configModel = RelayConfigModel()// 一唱到底配置
    var peerUser: RelayPlayerInfoModel? = null
    var enableNoLimitDuration: Boolean = false    // 开启没有限制的持续时间

    var unLockMe = false // 我是否解锁
        set(value) {
            if (value != field) {
                field = value
                if (field && unLockPeer) {
                    enableNoLimitDuration = true
                }

                if (!isEnterFromInvite()) {
                    EventBus.getDefault().post(RelayLockChangeEvent())
                }
            }
        }
    var unLockPeer = false // 对方是否解锁
        set(value) {
            if (value != field) {
                field = value
                if (field && unLockPeer) {
                    enableNoLimitDuration = true
                }

                if (!isEnterFromInvite()) {
                    EventBus.getDefault().post(RelayLockChangeEvent())
                }
            }
        }
    var leftSeat = true   // 我的未知是否在左边
    var myEffectModel: GameBackgroundEffectModel? = null
    var peerEffectModel: GameBackgroundEffectModel? = null

    var enterType: EnterType = EnterType.NORMAL

    override val gameType: Int
        get() = GameModeType.GAME_MODE_RELAY

    fun hasTimeLimit(): Boolean {
        return enableNoLimitDuration
    }

    //是否是邀请进来玩的房间
    fun isEnterFromInvite(): Boolean {
        return enterType == EnterType.INVITE
    }

    init {

    }

    /**
     * 返回当前歌曲应该到达的进度
     * 如果是负数则说明还在准备阶段 未开始播放
     */
    fun getSingCurPosition(): Long {
        realRoundInfo?.singBeginMs?.let {
            if (it > 0) {
                return (System.currentTimeMillis() - shiftTsForRelay) - (gameCreateTs + it)
            }
        }
        return Long.MAX_VALUE
    }

    fun hasOverThisRound(): Boolean {
        var d = (realRoundInfo?.singEndMs ?: 0) - (realRoundInfo?.singBeginMs ?: 0)
        if (d > 0) {
            var t = getSingCurPosition()
            if (t != Long.MAX_VALUE) {
                if (t > d) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * 返回当前正在演唱的选手id
     * 如果还未演唱，返回的即将演唱的选手id
     */
    fun getSingerIdNow(): Int {
        var now = getSingCurPosition()
        if (now != Long.MAX_VALUE) {
            // 拿到歌曲分段信息
            realRoundInfo?.music?.relaySegments?.let {
                var index = 0
                for (s in it) {
                    if (now < s) {
                        break
                    }
                    index++
                }
                if (index % 2 == 0) {
                    // 第一个人演唱阶段 看发起人
                    return realRoundInfo?.userID!!
                } else {
                    // 第二个人演唱阶段
                    if (realRoundInfo?.userID!! == MyUserInfoManager.uid.toInt()) {
                        return peerUser?.userID!!
                    } else {
                        return MyUserInfoManager.uid.toInt()
                    }
                }
            }
        }
        return 0
    }

    /**
     * 算出下一次轮次切换的时间 如果没有轮次切换了 返回-1
     */
    fun getNextTurnChangeTs(): Long {
        var now = getSingCurPosition()
        if (now != Long.MAX_VALUE) {
            // 拿到歌曲分段信息
            realRoundInfo?.music?.relaySegments?.let {
                var index = 0
                for (s in it) {
                    if (now < s) {
                        return s - now;
                    }
                    index++
                }
            }
        }
        return -1
    }

    //房间人员到齐了没有
    fun isPersonArrive(): Boolean {
        return peerUser != null
    }

    /**
     * 当前是否是我唱
     */
    fun isSingByMeNow(): Boolean {
        // 第二个人演唱阶段
        return getSingerIdNow() == MyUserInfoManager.uid.toInt()
    }

    /**
     * 我是否第一个唱
     */
    fun isFirstSingByMe(): Boolean {
        // 第二个人演唱阶段
        return MyUserInfoManager.uid.toInt() == realRoundInfo?.userID
    }

    /**
     * 检查轮次信息是否需要更新
     */
    override fun checkRoundInEachMode() {
        if (isIsGameFinish) {
            MyLog.d(TAG, "游戏结束了，不需要再checkRoundInEachMode")
            return
        }
        if (expectRoundInfo == null) {
            MyLog.d(TAG, "尝试切换轮次 checkRoundInEachMode mExpectRoundInfo == null")
            // 结束状态了
            if (realRoundInfo != null) {
                val lastRoundInfoModel = realRoundInfo
                lastRoundInfoModel?.updateStatus(false, ERRoundStatus.RRS_END.value)
                realRoundInfo = null
                EventBus.getDefault().post(RelayRoundChangeEvent(lastRoundInfoModel, null))
            }
            return
        }
        MyLog.d(TAG, "尝试切换轮次 checkRoundInEachMode mExpectRoundInfo.roundSeq=" + expectRoundInfo!!.roundSeq)
        if (RoomDataUtils.roundSeqLarger<RelayRoundInfoModel>(expectRoundInfo, realRoundInfo) || realRoundInfo == null) {
            // 轮次大于，才切换
            val lastRoundInfoModel = realRoundInfo
            lastRoundInfoModel?.updateStatus(false, ERRoundStatus.RRS_END.value)
            realRoundInfo = expectRoundInfo
            EventBus.getDefault().post(RelayRoundChangeEvent(lastRoundInfoModel, realRoundInfo))
        }
    }

    fun loadFromRsp(rsp: JoinRelayRoomRspModel) {
        this.gameId = rsp.roomID
        this.gameCreateTs = rsp.createTimeMs
        rsp.tokens?.forEach {
            if (it.userID == MyUserInfoManager.uid.toInt()) {
                this.agoraToken = it.token
                return@forEach
            }
        }
        this.configModel = rsp.config ?: RelayConfigModel()

        rsp.users?.forEachIndexed { index, userInfoModel ->
            var a = rsp.showInfos.getOrNull(index)
            MyLog.w("chengsimin", "peerEffectModel1=${a} index=${index}")
            if (userInfoModel.userId != MyUserInfoManager.uid.toInt()) {
                this.peerUser = RelayPlayerInfoModel()
                this.peerUser?.userInfo = userInfoModel
                this.peerEffectModel = a
                this.leftSeat = index != 0
                this.peerUser?.isOnline = true
            } else {
                this.myEffectModel = a
            }
        }

        this.expectRoundInfo = rsp.currentRound
        this.enableNoLimitDuration = rsp.enableNoLimitDuration
        this.enterType = rsp.enterType
        MyLog.w("chengsimin", "peerEffectModel2=${this.peerEffectModel}")
    }

    override fun toString(): String {
        return "RelayRoomData(shiftTsForRelay=$shiftTsForRelay, configModel=$configModel, peerUser=$peerUser, unLockMe=$unLockMe, unLockPeer=$unLockPeer, leftSeat=$leftSeat, isHasExitGame=$isHasExitGame)"
    }

    enum class EnterType : Serializable {
        //NORMAL 第一个是匹配或者直接进入别人的房间
        //INVITE 通过邀请玩的房间（自己创建房间再邀请也算）
        NORMAL,
        INVITE
    }
}
