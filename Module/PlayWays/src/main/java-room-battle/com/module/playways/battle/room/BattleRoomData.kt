package com.module.playways.battle.room

import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.component.busilib.constans.GameModeType
import com.module.playways.BaseRoomData
import com.module.playways.battle.match.model.JoinBattleRoomRspModel
import com.module.playways.battle.room.event.BattleRoundChangeEvent
import com.module.playways.battle.room.model.BattlePlayerInfoModel
import com.module.playways.battle.room.model.BattleRoomConfig
import com.module.playways.battle.room.model.BattleRoundInfoModel
import com.module.playways.room.prepare.model.PlayerInfoModel
import com.zq.live.proto.BattleRoom.EBRoundStatus
import org.greenrobot.eventbus.EventBus


class BattleRoomData : BaseRoomData<BattleRoundInfoModel>() {

    var sentenceCnt = 10 //当前演唱歌曲的句子总数，用于打分用
    /**
     * 我的队伍信息
     */
    var myTeamInfo = ArrayList<BattlePlayerInfoModel>()

    /**
     * 对方队伍信息
     */
    var opTeamInfo = ArrayList<BattlePlayerInfoModel>()

    /**
     * 我的队伍标识
     */
    var myTeamTag = ""

    /**
     * 包含 换歌卡 帮唱卡 个数 以及总的歌曲轮次
     */
    var config = BattleRoomConfig()

    /**
     * 我的队伍的总分
     */
    var myTeamScore = 0

    /**
     * 对方队伍的总分
     */
    var opTeamScore = 0

    override val gameType: Int
        get() = GameModeType.GAME_MODE_BATTLE


    init {
        syncServerTs()
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
                lastRoundInfoModel?.updateStatus(false, EBRoundStatus.BRS_END.value)
                realRoundInfo = null
                EventBus.getDefault().post(BattleRoundChangeEvent(lastRoundInfoModel, null))
            }
            return
        }
        MyLog.d(TAG, "尝试切换轮次 checkRoundInEachMode mExpectRoundInfo.roundSeq=" + expectRoundInfo!!.roundSeq)
        if ((expectRoundInfo?.roundSeq ?: 0) > (realRoundInfo?.roundSeq
                        ?: 0) || realRoundInfo == null) {
            // 轮次大于，才切换
            val lastRoundInfoModel = realRoundInfo
            lastRoundInfoModel?.updateStatus(false, EBRoundStatus.BRS_END.value)
            realRoundInfo = expectRoundInfo
            EventBus.getDefault().post(BattleRoundChangeEvent(lastRoundInfoModel, realRoundInfo))
        }
    }

    override fun getPlayerAndWaiterInfoList(): List<PlayerInfoModel> {
        val list = ArrayList<PlayerInfoModel>()
        list.addAll(myTeamInfo)
        list.addAll(opTeamInfo)
        return list
    }

    override fun getInSeatPlayerInfoList(): List<PlayerInfoModel> {
        val list = ArrayList<PlayerInfoModel>()
        list.addAll(myTeamInfo)
        return list
    }

    override fun getCanGiveGiftList(): List<PlayerInfoModel> {
        // 只能送给队友
        return getTeammates()
    }

    /**
     * 得到我的所有队友
     */
    fun getTeammates(): List<PlayerInfoModel> {
        var sl = ArrayList<BattlePlayerInfoModel>()
        for (p in myTeamInfo) {
            if (p.userID != MyUserInfoManager.uid.toInt()) {
                sl.add(p)
            }
        }
        return sl
    }

    /**
     * 得到我的第一个队友
     */
    fun getFirstTeammate(): BattlePlayerInfoModel? {
        for (p in myTeamInfo) {
            if (p.userID != MyUserInfoManager.uid.toInt()) {
                return p
            }
        }
        return null
    }

    fun getPlayerInfoById(userId: Int): BattlePlayerInfoModel? {
        for (p in myTeamInfo) {
            if (p.userID == userId) {
                return p
            }
        }
        for (p in opTeamInfo) {
            if (p.userID == userId) {
                return p
            }
        }
        return null
    }

    // 判断这个人是否是对方选手
    fun isOpTeam(userId: Int): Boolean {
        opTeamInfo.forEach {
            if (it.userID == userId) {
                return true
            }
        }
        return false
    }

    fun loadFromRsp(rsp: JoinBattleRoomRspModel) {
        this.gameId = rsp.roomID
        rsp.tokens?.forEach {
            if (it.userID == MyUserInfoManager.uid.toInt()) {
                this.agoraToken = it.token
                return@forEach
            }
        }
        this.gameCreateTs = rsp.createdTimeMs
        myTeamInfo.clear()
        opTeamInfo.clear()
        // 载入队伍信息
        for (t in rsp.teams) {
            for (u in t.teamUsers) {
                if (u.userId == MyUserInfoManager.uid.toInt()) {
                    myTeamTag = t.teamTag
                    break
                }
            }
        }
        for (t in rsp.teams) {
            for (u in t.teamUsers) {
                var uu = BattlePlayerInfoModel()
                uu.userInfo = u
                uu.isOnline = true
                if (myTeamTag == t.teamTag) {
                    myTeamInfo.add(uu)
                } else {
                    opTeamInfo.add(uu)
                }
            }
        }

        rsp.config?.let {
            this.config = it
        }
        this.realRoundInfo = null
        this.expectRoundInfo = rsp.currentRound
    }


}
