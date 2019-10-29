package com.module.playways.mic.room

import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.utils.U
import com.component.busilib.constans.GameModeType
import com.component.busilib.constans.GrabRoomType
import com.module.playways.BaseRoomData
import com.module.playways.RoomDataUtils
import com.module.playways.mic.match.model.JoinMicRoomRspModel
import com.module.playways.mic.room.event.MicRoundChangeEvent
import com.module.playways.mic.room.model.MicConfigModel
import com.module.playways.mic.room.model.MicPlayerInfoModel
import com.module.playways.mic.room.model.MicRoundInfoModel
import com.zq.live.proto.MicRoom.EMRoundStatus
import com.zq.live.proto.MicRoom.EMUserRole
import org.greenrobot.eventbus.EventBus
import java.util.ArrayList

class MicRoomData : BaseRoomData<MicRoundInfoModel>() {
    var configModel = MicConfigModel()// 一唱到底配置
    var roomType: Int = GrabRoomType.ROOM_TYPE_COMMON// 一唱到底房间类型，公开，好友，私密，普通 5为歌单房间
    var isHasExitGame = false// 是否已经正常退出房间

    private var mIsAccEnable = false// 是否开启伴奏,只代表设置里伴奏开关

    var ownerId: Int = 0// 房主id

//    private var hasGameBegin = true// 游戏是否已经开始

    var roomName: String = ""  // 房间名称

    var ownerKickTimes = 100  // 房主剩余踢人的次数,默认为100

//    var isNewUser = false   // 是否是新手引导房间

//    private var mOpenRecording = -1 // 是否开启高光时刻

    /**
     * 是否在选手席位
     */
    val isInPlayerList: Boolean
        get() {
            val p = getPlayerOrWaiterInfoModel(MyUserInfoManager.uid.toInt())
            if (p != null && (p.role == EMUserRole.MQUR_ROOM_OWNER.value || p.role == EMUserRole.MRUR_PLAY_USER.value)) {
                return true
            }
            return false
        }

    override val gameType: Int
        get() = GameModeType.GAME_MODE_MIC

    var isAccEnable: Boolean
        get() = mIsAccEnable
        set(accEnable) {
            mIsAccEnable = accEnable
            U.getPreferenceUtils().setSettingBoolean("grab_acc_enable1", mIsAccEnable)
        }

    /**
     * 是不是房主
     *
     * @return
     */
    val isOwner: Boolean
        get() = this.ownerId.toLong() == MyUserInfoManager.uid

//    val worksUploadModel: List<WorksUploadModel>
//        get() = mWorksUploadList

    var inChallenge = false // 是否在挑战中

    var maxGetBLightCnt = 0 // 之前获得过的最大爆灯数

    var starCnt = -1 //人气挑战阶段性的星际评价

    var enterRoundSeq = 0 // 刚进入房间时，所处的轮次

    init {
        mIsAccEnable = U.getPreferenceUtils().getSettingBoolean("grab_acc_enable1", false)
    }


    /**
     * 一般不用直接拿来用
     * @return
     */
    override fun getPlayerAndWaiterInfoList(): ArrayList<MicPlayerInfoModel> {
        val l = ArrayList<MicPlayerInfoModel>()
        if (expectRoundInfo != null) {
            l.addAll(expectRoundInfo!!.getPlayUsers())
        } else {
        }
        return l
    }

    fun getPlayerOrWaiterInfoModel(userID: Int?): MicPlayerInfoModel? {
        if (userID == null || userID == 0) {
            return null
        }
        val playerInfoModel = userInfoMap[userID] as MicPlayerInfoModel?
        if (playerInfoModel == null || playerInfoModel.role == EMUserRole.MRUR_PLAY_USER.value) {
            val l = getPlayerAndWaiterInfoList()
            for (playerInfo in l) {
                if (playerInfo.userInfo.userId == userID) {
                    userInfoMap.put(playerInfo.userInfo.userId, playerInfo)
                    return playerInfo
                }
            }
        } else {
            return playerInfoModel
        }
        return null
    }

    override fun getInSeatPlayerInfoList(): List<MicPlayerInfoModel> {
        val l = ArrayList<MicPlayerInfoModel>()
        if (expectRoundInfo != null) {
            l.addAll(expectRoundInfo!!.getPlayUsers())
        }
        return l
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
                lastRoundInfoModel?.updateStatus(false, EMRoundStatus.MRS_END.value)
                realRoundInfo = null
                EventBus.getDefault().post(MicRoundChangeEvent(lastRoundInfoModel,null))
            }
            return
        }
        MyLog.d(TAG, "尝试切换轮次 checkRoundInEachMode mExpectRoundInfo.roundSeq=" + expectRoundInfo!!.roundSeq)
        if (RoomDataUtils.roundSeqLarger<MicRoundInfoModel>(expectRoundInfo, realRoundInfo) || realRoundInfo == null) {
            // 轮次大于，才切换
            val lastRoundInfoModel = realRoundInfo
            lastRoundInfoModel?.updateStatus(false, EMRoundStatus.MRS_END.value)
            realRoundInfo = expectRoundInfo
            if (realRoundInfo != null) {
                (realRoundInfo as MicRoundInfoModel).updateStatus(false, EMRoundStatus.MRS_INTRO.value)
            }
            EventBus.getDefault().post(MicRoundChangeEvent(lastRoundInfoModel, realRoundInfo))
        }
    }

//    fun hasGameBegin(): Boolean {
//        return hasGameBegin
//    }
//
//    fun setHasGameBegin(hasGameBegin: Boolean) {
//        this.hasGameBegin = hasGameBegin
//    }

    fun loadFromRsp(rsp: JoinMicRoomRspModel) {
        this.gameId = rsp.roomID
        this.setCoin(rsp.coin)
        this.setHzCount(rsp.hongZuan, 0)
        if (rsp.config != null) {
            this.configModel = rsp.config ?: MicConfigModel()
        } else {
            MyLog.w(TAG, "JoinGrabRoomRspModel rsp==null")
        }
        val grabRoundInfoModel = rsp.currentRound
        if (grabRoundInfoModel != null) {
            if (rsp.isNewGame) {
                grabRoundInfoModel.isParticipant = true
            } else {
                grabRoundInfoModel.isParticipant = false
                grabRoundInfoModel.enterStatus = grabRoundInfoModel.status
            }
            grabRoundInfoModel.elapsedTimeMs = rsp.elapsedTimeMs
        }
        this.expectRoundInfo = grabRoundInfoModel
        this.realRoundInfo = null
        //            mRoomData.setRealRoundInfo(rsp.getCurrentRound());
//        this.tagId = rsp.tagID

        this.isIsGameFinish = false
        this.isHasExitGame = false
        this.agoraToken = rsp.agoraToken
        this.roomType = rsp.roomType
        this.ownerId = rsp.ownerID
        this.gameCreateTs = rsp.gameCreateTimeMs
        this.gameStartTs = rsp.gameStartTimeMs
        // 游戏未开始
//        if (rsp.isHasGameBegin == null) {
//            if (this.gameStartTs > 0) {
//                this.setHasGameBegin(true)
//            } else {
//                this.setHasGameBegin(false)
//            }
//        } else {
//            this.setHasGameBegin(rsp.isHasGameBegin!!)
//        }

//        this.isChallengeAvailable = rsp.isChallengeAvailable
        this.roomName = rsp.roomName ?: ""
//        this.isVideoRoom = rsp.mediaType == 2
//        if (roomType == GrabRoomType.ROOM_TYPE_PLAYBOOK && !hasGameBegin) {
//            playbookRoomDataWhenNotStart = PlaybookRoomDataWhenNotStart()
//            rsp.waitUsers?.let {
//                playbookRoomDataWhenNotStart?.waitUsers?.addAll(it)
//            }
//        }
        this.enterRoundSeq = this.expectRoundInfo?.roundSeq ?: 0

        this.inChallenge = rsp.inChallenge

        this.maxGetBLightCnt = rsp.maxGetBLightCnt

    }

    override fun toString(): String {
        return "MicRoomData{" +
//                ", mTagId=" + tagId +
                ", configModel=" + configModel +
                ", roomType=" + roomType +
                ", ownerId=" + ownerId +
//                ", hasGameBegin=" + hasGameBegin +
                ", mAgoraToken=" + agoraToken +
                '}'.toString()
    }


    //    GrabGuideInfoModel mGrabGuideInfoModel;
    //
    //    public void setGrabGuideInfoModel(GrabGuideInfoModel grabGuideInfoModel) {
    //        mGrabGuideInfoModel = grabGuideInfoModel;
    //    }
    //
    //    public GrabGuideInfoModel getGrabGuideInfoModel() {
    //        return mGrabGuideInfoModel;
    //    }

//    fun openAudioRecording(): Boolean {
//        if (true) {
//            return false
//        }
//        if (mOpenRecording == -1) {
//            if (U.getDeviceUtils().level == DeviceUtils.LEVEL.BAD) {
//                MyLog.w(TAG, "设备太差，不开启录制")
//                mOpenRecording = 0
//            } else {
//                mOpenRecording = 1
//            }
//        }
//        return mOpenRecording == 1
//    }
//
//
//    fun addWorksUploadModel(savePath: WorksUploadModel) {
//        mWorksUploadList.add(savePath)
//    }
}
