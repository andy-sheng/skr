package com.module.playways.room.prepare.model

import com.module.playways.grab.room.model.GrabConfigModel
import com.module.playways.grab.room.model.GrabPlayerInfoModel
import com.module.playways.grab.room.model.GrabRoundInfoModel

import java.io.Serializable

/*
{
  "data": {
    "coin": 0,
    "currentRound": {
      "bLightInfo": [
        {
          "roundSeq": 0,
          "userID": 0
        }
      ],
      "getSingMs": 0,
      "introBeginMs": 0,
      "introEndMs": 0,
      "mLightInfo": [
        {
          "roundSeq": 0,
          "userID": 0
        }
      ],
      "music": {
        "acc": "string",
        "beginTimeMs": 0,
        "cover": "string",
        "endTimeMs": 0,
        "isBlank": true,
        "itemID": 0,
        "itemName": "string",
        "lyric": "string",
        "midi": "string",
        "ori": "string",
        "owner": "string",
        "rankLrcBeginT": 0,
        "rankLrcEndT": 0,
        "rankUserVoice": "string",
        "standIntro": "string",
        "standIntroBeginT": 0,
        "standIntroEndT": 0,
        "standLrc": "string",
        "standLrcBeginT": 0,
        "standLrcEndT": 0,
        "totalTimeMs": 0,
        "zip": "string"
      },
      "noPassSingInfos": [
        {
          "timeMs": 0,
          "userID": 0
        }
      ],
      "overReason": "ROR_UNKNOWN",
      "playbookID": 0,
      "resultType": "ROT_UNKNOWN",
      "roundInit": {
        "userCnt": 0
      },
      "roundSeq": 0,
      "singBeginMs": 0,
      "singEndMs": 0,
      "skrResource": {
        "audioURL": "string",
        "itemID": 0,
        "midiURL": "string",
        "resourceID": 0,
        "sysScore": 0
      },
      "status": "QRS_UNKNOWN",
      "userID": 0,
      "wantSingInfos": [
        {
          "timeMs": 0,
          "userID": 0
        }
      ]
    },
    "elapsedTimeMs": 0,
    "gameOverTimeMs": 0,
    "nextRound": {
      "bLightInfo": [
        {
          "roundSeq": 0,
          "userID": 0
        }
      ],
      "getSingMs": 0,
      "introBeginMs": 0,
      "introEndMs": 0,
      "mLightInfo": [
        {
          "roundSeq": 0,
          "userID": 0
        }
      ],
      "music": {
        "acc": "string",
        "beginTimeMs": 0,
        "cover": "string",
        "endTimeMs": 0,
        "isBlank": true,
        "itemID": 0,
        "itemName": "string",
        "lyric": "string",
        "midi": "string",
        "ori": "string",
        "owner": "string",
        "rankLrcBeginT": 0,
        "rankLrcEndT": 0,
        "rankUserVoice": "string",
        "standIntro": "string",
        "standIntroBeginT": 0,
        "standIntroEndT": 0,
        "standLrc": "string",
        "standLrcBeginT": 0,
        "standLrcEndT": 0,
        "totalTimeMs": 0,
        "zip": "string"
      },
      "noPassSingInfos": [
        {
          "timeMs": 0,
          "userID": 0
        }
      ],
      "overReason": "ROR_UNKNOWN",
      "playbookID": 0,
      "resultType": "ROT_UNKNOWN",
      "roundInit": {
        "userCnt": 0
      },
      "roundSeq": 0,
      "singBeginMs": 0,
      "singEndMs": 0,
      "skrResource": {
        "audioURL": "string",
        "itemID": 0,
        "midiURL": "string",
        "resourceID": 0,
        "sysScore": 0
      },
      "status": "QRS_UNKNOWN",
      "userID": 0,
      "wantSingInfos": [
        {
          "timeMs": 0,
          "userID": 0
        }
      ]
    },
    "onlineInfo": [
      {
        "isOnline": true,
        "isSkrer": true,
        "userID": 0,
        "userInfo": {
          "avatar": "string",
          "description": "string",
          "isSystem": true,
          "mainLevel": 0,
          "nickName": "string",
          "sex": "SX_UNKNOWN",
          "userID": 0
        }
      }
    ],
    "syncStatusTimeMs": 0
  },
  "errmsg": "string",
  "errno": 0,
  "traceId": "string"
}
 */
class JoinGrabRoomRspModel : Serializable {
    var coin: Int = 0 // 金币数目
    var config: GrabConfigModel? = null//游戏的配置信息
    var currentRound: GrabRoundInfoModel? = null // 目前轮次
    var nextRound: GrabRoundInfoModel? = null // 下个轮次，其实没用
    var elapsedTimeMs: Int = 0//代表这个阶段已经经过了多少毫秒，主要用作中途进来的人播放资源
    var gameOverTimeMs: Int = 0// 结束时间
    var roomID: Int = 0// 房间id
    var syncStatusTimeMs: Int = 0 // 同步时间
    var tagID: Int = 0// 剧本游戏
    var isNewGame: Boolean = false// 是否是一局新游戏
    var agoraToken: String? = null// 声网token
    var roomType: Int = 0// 一唱到底房间类型，公开，好友，私密，普通 5是歌单
    var ownerID: Int = 0// 房主id
    var gameStartTimeMs: Long = 0// 游戏创建时间,<=0 代表游戏未创建
    var isHasGameBegin:Boolean? = null// 游戏是否已经开始
    var isChallengeAvailable = false// 是否有挑战资格
    var roomName: String? = null    //房间名称
    var hongZuan: Float = 0.toFloat() // 红钻
    var mediaType: Int = 0// 2为视频房
    var waitUsers: List<GrabPlayerInfoModel>? = null // 歌单战等待状态给的用户信息

    override fun toString(): String {
        return "JoinGrabRoomRspModel{" +
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
