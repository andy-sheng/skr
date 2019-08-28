package com.module.playways.race

import com.common.rxretrofit.ApiResult
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface RaceRoomServerApi {

    /*
    请求匹配
    {
    "platform": "PF_UNKNOWN"
    }
    {
    "data": {},
    "errmsg": "string",
    "errno": 0,
    "traceId": "string"
    }
     */
    @PUT("http://dev.game.inframe.mobi/v1/raceroom/query-match")
    fun queryMatch(@Body body: RequestBody): Call<ApiResult>

    /*
    取消匹配
    {
    }
    {
    "data": {},
    "errmsg": "string",
    "errno": 0,
    "traceId": "string"
    }
     */
    @PUT("http://dev.game.inframe.mobi/v1/raceroom/cancel-match")
    fun cancelMatch(@Body body: RequestBody): Call<ApiResult>

    /*
     加入房间
    {
    "platform": "PF_UNKNOWN",
    "roomID": 0
    }

    {
        "data": {
        "config": {},
        "currentRound": {
        "overReason": "ERROR_UNKNOWN",
        "roundSeq": 0,
        "scores": [{
        "bLightCnt": 0,
        "isEscape": true,
        "winType": "RWT_UNKNOWN"
    }],
        "status": "ERRS_UNKNOWN",
        "subRoundInfo": [{
        "beginMs": 0,
        "choiceID": 0,
        "endMs": 0,
        "overReason": "ESROR_UNKNOWN",
        "subRoundSeq": 0,
        "userID": 0
    }],
        "subRoundSeq": 0
    },
        "elapsedTimeMs": 0,
        "gameStartTimeMs": 0,
        "games": [{
        "commonMusic": {
        "acc": "string",
        "beginMs": 0,
        "cover": "string",
        "endMs": 0,
        "isBlank": true,
        "itemID": 0,
        "itemName": "string",
        "lyric": "string",
        "midi": "string",
        "ori": "string",
        "owner": "string",
        "rankBgm": "string",
        "rankLrcBeginT": 0,
        "rankLrcEndT": 0,
        "rankUserVoice": "string",
        "standIntro": "string",
        "standIntroBeginT": 0,
        "standIntroEndT": 0,
        "standLrc": "string",
        "standLrcBeginT": 0,
        "standLrcEndT": 0,
        "standTotalMs": 0,
        "task": 0,
        "totalMs": 0,
        "zip": "string"
    },
        "roundGameType": "ERGT_UNKNOWN"
    }]
    },
        "errmsg": "string",
        "errno": 0,
        "traceId": "string"
    }
     */
    @PUT("http://dev.game.inframe.mobi/v1/raceroom/join-room")
    fun joinRoom(@Body body: RequestBody): Call<ApiResult>

    /*
      退出房间
      {
          "roomID": 0
        }

        {
          "data": {},
          "errmsg": "string",
          "errno": 0,
          "traceId": "string"
        }
     */
    @PUT("http://dev.game.inframe.mobi/v1/raceroom/exit-room")
    fun exitRoom(@Body body: RequestBody): Call<ApiResult>

    /*
      房间心跳，一分钟一次
      {
          "roomID": 0,
          "userID": 0
      }

        {
          "data": {},
          "errmsg": "string",
          "errno": 0,
          "traceId": "string"
        }
     */
    @PUT("http://dev.game.inframe.mobi/v1/raceroom/heartbeat")
    fun heartbeat(@Body body: RequestBody): Call<ApiResult>

    /**from racegame.json**/

    /*
    投票
    {
      "roomID": 0,
      "roundSeq": 0,
      "subRoundSeq": 0
    }
    {
      "data": {
        "bLightFailedMsg": "string",
        "isBLightSuccess": true,
        "roundSeq": 0,
        "subRoundSeq": 0
      },
      "errmsg": "string",
      "errno": 0,
      "traceId": "string"
    }
     */
    @PUT("http://dev.game.inframe.mobi/v1/racegame/b-light")
    fun bLight(@Body body: RequestBody): Call<ApiResult>

    /*
   放弃演唱
   {
     "roomID": 0,
     "roundSeq": 0,
     "subRoundSeq": 0
   }
   {
      "data": {
        "roundSeq": 0,
        "subRoundSeq": 0
      },
      "errmsg": "string",
      "errno": 0,
      "traceId": "string"
    }
    */
    @PUT("http://dev.game.inframe.mobi/v1/racegame/give-up")
    fun giveup(@Body body: RequestBody): Call<ApiResult>

    /*
   导唱结束
   {
     "roomID": 0,
     "roundSeq": 0,
   }
  {
      "data": {},
      "errmsg": "string",
      "errno": 0,
      "traceId": "string"
    }
    */
    @PUT("http://dev.game.inframe.mobi/v1/racegame/intro-over")
    fun introOver(@Body body: RequestBody): Call<ApiResult>

    /*
     获取段位结果
    {
      "data": {
        "userScoreResult": [
          {
            "battleIndexChange": [
              {
                "index": 0,
                "score": 0,
                "why": "string"
              }
            ],
            "expChange": [
              {
                "index": 0,
                "score": 0,
                "why": "string"
              }
            ],
            "sss": 0,
            "starChange": [
              {
                "index": 0,
                "score": 0,
                "why": "string"
              }
            ],
            "states": [
              {
                "currBattleIndex": "string",
                "currExp": "string",
                "currStar": "string",
                "mainRanking": "string",
                "maxBattleIndex": "string",
                "maxExp": "string",
                "maxStar": "string",
                "protectBattleIndex": "string",
                "rankingDesc": "string",
                "seq": 0,
                "subRanking": "string",
                "totalScore": "string",
                "userID": 0
              }
            ],
            "userID": 0,
            "winType": "InvalidEWinType"
          }
        ]
      },
      "errmsg": "string",
      "errno": 0,
      "traceId": "string"
    }
  */
    @GET("http://dev.game.inframe.mobi/v1/racegame/result")
    fun getResult(@Query("roomID") roomID: Int,
                  @Query("userID") userID: Int,
                  @Query("roundSeq") roundSeq: Int): Call<ApiResult>

    /*
  演唱结束
  {
  "roomID": 0,
  "roundSeq": 0,
  "subRoundSeq": 0
    }
    {
      "data": {},
      "errmsg": "string",
      "errno": 0,
      "traceId": "string"
    }
   */
    @PUT("http://dev.game.inframe.mobi/v1/racegame/round-over")
    fun roundOver(@Body body: RequestBody): Call<ApiResult>

    /*
     同步轮次信息
     {
      "data": {
        "currentRound": {
          "overReason": "ERROR_UNKNOWN",
          "roundSeq": 0,
          "scores": [
            {
              "bLightCnt": 0,
              "isEscape": true,
              "winType": "RWT_UNKNOWN"
            }
          ],
          "status": "ERRS_UNKNOWN",
          "subRoundInfo": [
            {
              "beginMs": 0,
              "choiceID": 0,
              "endMs": 0,
              "overReason": "ESROR_UNKNOWN",
              "subRoundSeq": 0,
              "userID": 0
            }
          ],
          "subRoundSeq": 0
        },
        "gameOverTimeMs": 0,
        "syncStatusTimeMs": 0
      },
      "errmsg": "string",
      "errno": 0,
      "traceId": "string"
    }
     */
    @GET("http://dev.game.inframe.mobi/v1/racegame/sync-status")
    fun syncStatus(@Query("roomID") roomID: Long): Call<ApiResult>


    /*
    抢唱
    {
      "choiceID": 0,
      "roomID": 0,
      "roundSeq": 0
    }
    {
      "data": {},
      "errmsg": "string",
      "errno": 0,
      "traceId": "string"
    }
    */
    @PUT("http://dev.game.inframe.mobi/v1/racegame/want-sing-chance")
    fun wantSingChance(@Body body: RequestBody): Call<ApiResult>
}