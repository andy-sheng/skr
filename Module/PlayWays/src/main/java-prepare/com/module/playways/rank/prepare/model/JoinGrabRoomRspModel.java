package com.module.playways.rank.prepare.model;

import com.module.playways.grab.room.model.GrabConfigModel;
import com.module.playways.grab.room.model.GrabRoundInfoModel;

import java.io.Serializable;
import java.util.List;

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
public class JoinGrabRoomRspModel implements Serializable {
    private int coin; // 金币数目
    private GrabRoundInfoModel currentRound; // 目前轮次
    private GrabRoundInfoModel nextRound; // 下个轮次，其实没用
    private int elapsedTimeMs;//代表这个阶段已经经过了多少毫秒，主要用作中途进来的人播放资源
    private int gameOverTimeMs;// 结束时间
    private int roomID;// 房间id
    private int syncStatusTimeMs; // 同步时间
    private int tagID;// 剧本游戏
    private boolean isNewGame;// 是否是一局新游戏
    private GrabConfigModel config;

    public JoinGrabRoomRspModel() {

    }

    public int getCoin() {
        return coin;
    }

    public void setCoin(int coin) {
        this.coin = coin;
    }

    public int getElapsedTimeMs() {
        return elapsedTimeMs;
    }

    public void setElapsedTimeMs(int elapsedTimeMs) {
        this.elapsedTimeMs = elapsedTimeMs;
    }

    public int getGameOverTimeMs() {
        return gameOverTimeMs;
    }

    public void setGameOverTimeMs(int gameOverTimeMs) {
        this.gameOverTimeMs = gameOverTimeMs;
    }

    public int getRoomID() {
        return roomID;
    }

    public void setRoomID(int roomID) {
        this.roomID = roomID;
    }

    public int getSyncStatusTimeMs() {
        return syncStatusTimeMs;
    }

    public void setSyncStatusTimeMs(int syncStatusTimeMs) {
        this.syncStatusTimeMs = syncStatusTimeMs;
    }

    public GrabRoundInfoModel getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(GrabRoundInfoModel currentRound) {
        this.currentRound = currentRound;
    }

    public GrabRoundInfoModel getNextRound() {
        return nextRound;
    }

    public void setNextRound(GrabRoundInfoModel nextRound) {
        this.nextRound = nextRound;
    }

    public int getTagID() {
        return tagID;
    }

    public void setTagID(int tagID) {
        this.tagID = tagID;
    }

    public boolean isNewGame() {
        return isNewGame;
    }

    public void setNewGame(boolean newGame) {
        isNewGame = newGame;
    }

    public GrabConfigModel getConfig() {
        return config;
    }

    public void setConfig(GrabConfigModel config) {
        this.config = config;
    }
}
