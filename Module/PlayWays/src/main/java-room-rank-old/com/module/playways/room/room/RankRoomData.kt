package com.module.playways.room.room

import com.common.core.account.UserAccountManager
import com.common.log.MyLog
import com.component.busilib.constans.GameModeType
import com.module.playways.BaseRoomData
import com.module.playways.RoomDataUtils
import com.module.playways.room.prepare.model.BaseRoundInfoModel
import com.module.playways.room.room.event.PkSomeOneOnlineChangeEvent
import com.module.playways.room.room.model.RankGameConfigModel
import com.module.playways.room.prepare.model.PlayerInfoModel
import com.module.playways.room.room.model.RankPlayerInfoModel
import com.module.playways.room.room.model.RankRoundInfoModel
import com.module.playways.room.room.event.PkMyBurstSuccessEvent
import com.module.playways.room.room.event.PkMyLightOffSuccessEvent
import com.module.playways.room.room.event.RankRoundInfoChangeEvent
import com.module.playways.room.room.model.RecordData

import org.greenrobot.eventbus.EventBus

class RankRoomData : BaseRoomData<RankRoundInfoModel>() {
    override fun getInSeatPlayerInfoList(): List<PlayerInfoModel> {
        return arrayListOf()
    }

    var roundInfoModelList: List<RankRoundInfoModel>? = null //所有的轮次信息

    var recordData: RecordData? = null // PK赛的结果信息

    protected var mPlayerInfoList: List<RankPlayerInfoModel>? = null//选手信息

    protected var mGameConfigModel: RankGameConfigModel? = null// 配置信息

    var leftBurstLightTimes: Int = 0
        protected set //剩余爆灯次数
    var leftLightOffTimes: Int = 0
        protected set //剩余灭灯次数
    var songLineNum: Int = 0// 歌词行数
    var curSongTotalScore: Int = 0 // 歌曲部分演唱的累计总分
    var singBeginTs: Long = 0// 本人开始演唱的时间戳

    protected var mHasGoVoiceRoom = false // 是要要去语音房

    override val gameType: Int
        get() = GameModeType.GAME_MODE_CLASSIC_RANK

    var gameConfigModel: RankGameConfigModel?
        get() = mGameConfigModel
        set(gameConfigModel) {
            mGameConfigModel = gameConfigModel
            leftLightOffTimes = mGameConfigModel?.getpKMaxShowMLightTimes() ?: 2
            leftBurstLightTimes = mGameConfigModel?.getpKMaxShowBLightTimes() ?: 2
        }

    val aiJudgeInfo: PlayerInfoModel?
        get() {
            if (getPlayerAndWaiterInfoList() != null && getPlayerAndWaiterInfoList().size > 0) {
                for (playerInfoModel in getPlayerAndWaiterInfoList()) {
                    if (playerInfoModel.isAI()) {
                        return playerInfoModel
                    }
                }
            }
            return null
        }

    /**
     * 检查轮次信息是否需要更新
     */
    override fun checkRoundInEachMode() {
        MyLog.d(TAG, "checkRound mExcpectRoundInfo=$expectRoundInfo mRealRoundInfo=$realRoundInfo")
        if (isIsGameFinish) {
            MyLog.d(TAG, "游戏结束了，不需要再check")
            return
        }
        if (expectRoundInfo == null) {
            // 结束状态了
            if (realRoundInfo != null) {
                val lastRoundInfoModel = realRoundInfo
                realRoundInfo = null
                EventBus.getDefault().post(RankRoundInfoChangeEvent(false, lastRoundInfoModel))
            }
            return
        }
        if (!RoomDataUtils.roundInfoEqual(expectRoundInfo, realRoundInfo)) {
            // 轮次需要更新了
            val lastRoundInfoModel = realRoundInfo
            realRoundInfo = expectRoundInfo
            if (realRoundInfo!!.userID.toLong() == UserAccountManager.getInstance().uuidAsLong) {
                // 轮到自己唱了。开始发心跳，开始倒计时，3秒后 开始开始混伴奏，开始解除引擎mute，
                EventBus.getDefault().post(RankRoundInfoChangeEvent(true, lastRoundInfoModel))
            } else {
                // 别人唱，本人的引擎mute，取消本人心跳。监听他人的引擎是否 unmute,开始混制歌词
                EventBus.getDefault().post(RankRoundInfoChangeEvent(false, lastRoundInfoModel))
            }
        }
    }

    fun consumeBurstLightTimes(which: BaseRoundInfoModel) {
        leftBurstLightTimes = leftBurstLightTimes - 1
        EventBus.getDefault().post(PkMyBurstSuccessEvent(which))
    }

    fun consumeLightOffTimes(which: BaseRoundInfoModel) {
        leftLightOffTimes = leftLightOffTimes - 1
        EventBus.getDefault().post(PkMyLightOffSuccessEvent(which))
    }

    fun setPlayerInfoList(playerInfoList: List<RankPlayerInfoModel>) {
        mPlayerInfoList = playerInfoList
    }

    override fun getPlayerAndWaiterInfoList(): List<RankPlayerInfoModel> {
        return mPlayerInfoList ?: ArrayList()
    }

    fun getPlayerInfoModel(userID: Int): RankPlayerInfoModel? {
        if (userID == 0) {
            return null
        }
        if (mPlayerInfoList == null) {
            return null
        }
        for (playerInfo in mPlayerInfoList!!) {
            if (playerInfo.userInfo.userId == userID) {
                return playerInfo
            }
        }
        return null
    }

    fun setOnline(userID: Int, online: Boolean) {
        //        if (mIsGameFinish) {
        //            MyLog.w(TAG, "游戏结束了，忽略某人退出房间消息，因为语音房还要复用这里的online状态");
        //            return;
        //        }
        if (mPlayerInfoList != null) {
            for (playerInfo in mPlayerInfoList!!) {
                if (playerInfo.userInfo.userId == userID) {
                    if (playerInfo.isOnline != online) {
                        playerInfo.isOnline = online
                        EventBus.getDefault().post(PkSomeOneOnlineChangeEvent(playerInfo))
                    }
                }
            }
        }
    }

    fun setHasGoVoiceRoom(hasGoVoiceRoom: Boolean) {
        mHasGoVoiceRoom = hasGoVoiceRoom
    }

    fun hasGoVoiceRoom(): Boolean {
        return mHasGoVoiceRoom
    }

    override fun toString(): String {
        return "RankRoomData{" +
                ", mGameConfigModel=" + mGameConfigModel +
                ", mSongLineNum=" + songLineNum +
                ", mAgoraToken=" + agoraToken +
                "}\n" + super.toString()
    }

}
