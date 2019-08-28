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
import com.zq.live.proto.RaceRoom.ERaceRoundStatus
import org.greenrobot.eventbus.EventBus
import java.util.ArrayList

class RaceRoomData : BaseRoomData<RaceRoundInfoModel>() {


    override val gameType: Int
        get() = GameModeType.GAME_MODE_RACE

    var raceConfigModel: RaceConfigModel? = null
    var hasExitGame = false
    var isAccEnable = false// 是否开启伴奏,只代表设置里伴奏开关


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
        this.agoraToken = rsp.agoraToken
        this.gameCreateTs = rsp.gameCreateTimeMs
        this.gameStartTs = rsp.gameStartTimeMs

        this.expectRoundInfo?.enterStatus = this.expectRoundInfo?.status
                ?: ERaceRoundStatus.ERRS_UNKNOWN.value
        if (rsp.elapsedTimeMs > 0) {
            // 演唱轮次进来，不能是本局参与者
            this.expectRoundInfo?.isParticipant = false
            this.expectRoundInfo?.elapsedTimeMs = rsp.elapsedTimeMs
        } else {
            this.expectRoundInfo?.isParticipant = true
        }
    }

}
