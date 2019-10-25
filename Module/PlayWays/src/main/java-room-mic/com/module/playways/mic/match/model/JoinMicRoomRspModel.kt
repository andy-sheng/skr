package com.module.playways.mic.match.model

import com.module.playways.mic.room.model.MicConfigModel
import com.module.playways.mic.room.model.MicPlayerInfoModel
import com.module.playways.mic.room.model.MicRoundInfoModel
import java.io.Serializable

class JoinMicRoomRspModel : Serializable {
    var inChallenge = false
    var maxGetBLightCnt = -1
    var coin: Int = 0 // 金币数目
    var config: MicConfigModel? = null//游戏的配置信息
    var currentRound: MicRoundInfoModel? = null // 目前轮次
    var nextRound: MicRoundInfoModel? = null // 下个轮次，其实没用
    var elapsedTimeMs: Int = 0//代表这个阶段已经经过了多少毫秒，主要用作中途进来的人播放资源
    var gameOverTimeMs: Int = 0// 结束时间
    var roomID: Int = 0// 房间id
    var syncStatusTimeMs: Int = 0 // 同步时间
    var tagID: Int = 0// 剧本游戏
    var isNewGame: Boolean = false// 是否是一局新游戏
    var agoraToken: String? = null// 声网token
    var roomType: Int = 0// 一唱到底房间类型，公开，好友，私密，普通 5是歌单
    var ownerID: Int = 0// 房主id
    var gameCreateTimeMs: Long = 0// 房间创建时间，绝对时间
    var gameStartTimeMs: Long = 0// 游戏开始时间，相对时间，状态机运行时间，相对于 gameCreateTimeMs
    var isHasGameBegin: Boolean? = null// 游戏是否已经开始
    var isChallengeAvailable = false// 是否有挑战资格
    var roomName: String? = null    //房间名称
    var hongZuan: Float = 0.toFloat() // 红钻
    var mediaType: Int = 0// 2为视频房
    var waitUsers: List<MicPlayerInfoModel>? = null // 歌单战等待状态给的用户信息

    override fun toString(): String {
        return "JoinMicRoomRspModel{" +
                "coin=" + coin +
                ", config=" + config +
                ", currentRound=" + currentRound +
                ", nextRound=" + nextRound +
                ", elapsedTimeMs=" + elapsedTimeMs +
                ", gameOverTimeMs=" + gameOverTimeMs +
                ", roomID=" + roomID +
                ", syncStatusTimeMs=" + syncStatusTimeMs +
                ", tagID=" + tagID +
                ", isNewGame=" + isNewGame +
                ", agoraToken='" + agoraToken + '\''.toString() +
                ", roomType=" + roomType +
                ", ownerID=" + ownerID +
                ", gameStartTimeMs=" + gameStartTimeMs +
                ", hasGameBegin=" + isHasGameBegin +
                ", challengeAvailable=" + isChallengeAvailable +
                ", roomName='" + roomName + '\''.toString() +
                '}'.toString()
    }
}