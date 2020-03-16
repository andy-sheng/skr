package com.module.playways.battle.room.ui

import com.module.playways.battle.room.model.BattleRoundInfoModel
import com.module.playways.party.room.model.PartyPlayerInfoModel
import com.module.playways.party.room.model.PartyRoundInfoModel
import com.zq.live.proto.PartyRoom.PBeginVote

interface IBattleRoomView {
    // 游戏结束
    fun gameOver(from: String)

    // 显示导唱
    fun showIntro()

    // 显示结果页 完了回调回来
    fun showRoundOver(lastRound: BattleRoundInfoModel, callback: () -> Unit)

    // A 使用了帮唱卡 重新倒计时 显示弹窗等
    fun useHelpSing()

    // 显示自己演唱阶段
    fun showSelfSing()

    // 显示他人演唱阶段
    fun showOtherSing()

    // 显示对战开始提示阶段
    fun showBeginTips(callback: () -> Unit)

    fun receiveScoreEvent(score: Int, songLineNum: Int)
}