package com.module.playways

import com.common.core.userinfo.model.UserInfoModel
import com.module.playways.room.prepare.model.PlayerInfoModel
import com.module.playways.room.prepare.model.BaseRoundInfoModel
import com.module.playways.room.song.model.SongModel

import java.io.Serializable


/**
 * 房间内所有数据的聚合类
 * 每种模式的房间内状态信息都由其存储
 */
abstract class BaseRoomData<T : BaseRoundInfoModel> : Serializable {
    val TAG = "RoomData"

    var gameId: Int = 0 // 房间id

    var sysAvatar: String?=null // 系统头像

    /**
     * 当要拿服务器时间和本地时间比较时，请将服务器时间加上这个矫正值
     * 如
     * if(System.currentTimeMillis() > mGameStartTs + mShiftts){
     *
     *
     * }
     */
    var shiftTs: Int = 0// 本地时间比服务器快多少毫秒，比如快1秒，mShiftTs = 1000;

    var gameCreateTs: Long = 0// 游戏创建时间,服务器的

    var gameStartTs: Long = 0// 游戏开始时间,服务器的

    var gameOverTs: Long = 0// 游戏结束时间,服务器的

    var lastSyncTs: Long = 0// 上次同步服务器状态时间,服务器的

    var songModel: SongModel?=null // 歌曲信息

    var expectRoundInfo: T?=null// 按理的 期望的当前的轮次

    var realRoundInfo: T? = null// 实际的当前轮次信息

    var isIsGameFinish = false // 游戏开始了

    var isMute = false//是否mute

    var agoraToken: String?=null // 声网token

    abstract val gameType: Int

    val realRoundSeq: Int
        get() = if (realRoundInfo != null) {
            realRoundInfo!!.roundSeq
        } else -1

    abstract fun checkRoundInEachMode()

    override fun toString(): String {
        return "RoomData{" +
                "mGameType=" + gameType +
                ", gameId=" + gameId +
                ", mSysAvatar='" + sysAvatar + '\''.toString() +
                ", mShiftTs=" + shiftTs +
                ", mGameCreateTs=" + gameCreateTs +
                ", mGameStartTs=" + gameStartTs +
                ", mGameOverTs=" + gameOverTs +
                ", mLastSyncTs=" + lastSyncTs +
                ", mSongModel=" + songModel +
                ", mExpectRoundInfo=" + expectRoundInfo +
                ", mRealRoundInfo=" + realRoundInfo +
                ", mIsGameFinish=" + isIsGameFinish +
                ", mMute=" + isMute +
                '}'.toString()
    }

    abstract fun <T : PlayerInfoModel> getPlayerInfoList(): List<T>?

    fun getUserInfo(userID: Int): UserInfoModel? {
        if (userID == 0) {
            return null
        }
        val l = getPlayerInfoList<PlayerInfoModel>() ?: return null
        for (playerInfo in l) {
            if (playerInfo.userInfo.userId == userID) {
                return playerInfo.userInfo
            }
        }
        return null
    }

    companion object {

        val RANK_BATTLE_START_SVGA = "http://res-static.inframe.mobi/app/rank_battle_start.svga"
        val RANK_RESULT_WIN_SVGA = "http://res-static.inframe.mobi/app/rank_result_win.svga"
        val RANK_RESULT_LOSE_SVGA = "http://res-static.inframe.mobi/app/rank_result_lose.svga"
        val RANK_RESULT_DRAW_SVGA = "http://res-static.inframe.mobi/app/rank_result_draw.svga"
        val GRAB_BURST_BIG_SVGA = "http://res-static.inframe.mobi/app/grab_burst_big_animation.svga"

        val PK_MAIN_STAGE_WEBP = "http://res-static.inframe.mobi/app/pk_main_stage.webp"
        val READY_GO_SVGA_URL = "http://res-static.inframe.mobi/app/sige_go.svga"
        val ROOM_STAGE_SVGA = "http://res-static.inframe.mobi/app/main_stage_people.svga"
        val ROOM_SPECAIL_EMOJI_DABIAN = "http://res-static.inframe.mobi/app/emoji_bianbian.svga"
        val ROOM_SPECAIL_EMOJI_AIXIN = "http://res-static.inframe.mobi/app/emoji_love.svga"
        val AUDIO_FOR_AI_PATH = "audioforai.aac"
        val MATCHING_SCORE_FOR_AI_PATH = "matchingscore.json"
    }

}
