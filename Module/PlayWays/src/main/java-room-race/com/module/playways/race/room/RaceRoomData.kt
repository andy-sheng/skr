package com.module.playways.race.room

import com.component.busilib.constans.GameModeType
import com.module.playways.BaseRoomData
import com.module.playways.RoomDataUtils
import com.module.playways.grab.room.event.GrabRoundChangeEvent
import com.module.playways.grab.room.model.GrabRoundInfoModel
import com.module.playways.race.match.model.JoinRaceRoomRspModel
import com.module.playways.race.room.event.RaceRoundChangeEvent
import com.module.playways.race.room.model.RaceConfigModel
import com.module.playways.race.room.model.RacePlayerInfoModel
import com.module.playways.race.room.model.RaceRoundInfoModel
import com.module.playways.room.prepare.model.PlayerInfoModel
import com.module.playways.room.song.model.SongModel
import org.greenrobot.eventbus.EventBus
import java.util.ArrayList

class RaceRoomData : BaseRoomData<RaceRoundInfoModel>() {


    override val gameType: Int
        get() = GameModeType.GAME_MODE_RACE

    var raceConfigModel: RaceConfigModel? = null
    var hasExitGame = false


    /**
     * 检查轮次是否要更新
     */
    override fun checkRoundInEachMode() {
        if (this.isIsGameFinish) {
            return
        }
        if (this.expectRoundInfo == null && this.realRoundInfo != null) {
            // 发送游戏结束
            return
        }
        if (RoomDataUtils.roundSeqLarger(this.expectRoundInfo, this.realRoundInfo) || this.realRoundInfo == null) {
            // TODO
            val lastRound = this.realRoundInfo
//            (this.realRoundInfo as RaceRoundInfoModel).updateStatus(false,轮次结束事件)
            this.realRoundInfo = this.expectRoundInfo
//            (this.realRoundInfo as RaceRoundInfoModel).updateStatus(false,轮次开始事件)
            // TODO 发送轮次切换事件
            EventBus.getDefault().post(RaceRoundChangeEvent(lastRound, this.realRoundInfo))
//            EventBus.getDefault().post(GrabRoundChangeEvent(lastRoundInfoModel, mRealRoundInfo as GrabRoundInfoModel))
        }
    }

    override fun <T : PlayerInfoModel> getPlayerInfoList(): List<T>? {
        val l = ArrayList<T>()
        realRoundInfo?.let {
            l.addAll(it.playUsers as List<T>)
            l.addAll(it.waitUsers as List<T>)
        }
        return l
    }

    fun getChoiceInfoById(choiceID: Int): SongModel? {
        return this.realRoundInfo?.games?.getOrNull(choiceID - 1)?.commonMusic
    }

    fun getAllPlayer(): ArrayList<RacePlayerInfoModel> {
        val list = ArrayList<RacePlayerInfoModel>()
        val playUsers = realRoundInfo?.playUsers
        val waitUsers = realRoundInfo?.waitUsers
        if (!playUsers.isNullOrEmpty()) {
            list.addAll(playUsers)
        }
        if (!waitUsers.isNullOrEmpty()) {
            list.addAll(waitUsers)
        }
        return list
    }

    fun loadFromRsp(rsp: JoinRaceRoomRspModel) {
        this.gameId = rsp.roomID
        this.raceConfigModel = rsp.config
        this.expectRoundInfo = rsp.currentRound
        rsp.games?.let {
            this.expectRoundInfo?.games = it
        }
        this.realRoundInfo = null
        this.isIsGameFinish = false
        this.hasExitGame = false
        //this.agoraToken = rsp.agoraToken
//        this.gameCreateTs = rsp.gameStartTimeMs
        this.gameStartTs = rsp.gameStartTimeMs
//        this.xxxx = rsp.newRoundBegin
    }

}
