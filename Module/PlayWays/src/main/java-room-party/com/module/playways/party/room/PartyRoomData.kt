package com.module.playways.party.room

import com.common.core.myinfo.MyUserInfo
import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.component.busilib.constans.GameModeType
import com.module.playways.BaseRoomData
import com.module.playways.party.match.model.JoinPartyRoomRspModel
import com.module.playways.party.room.event.*
import com.module.playways.party.room.model.PartyActorInfoModel
import com.module.playways.party.room.model.PartyPlayerInfoModel
import com.module.playways.party.room.model.PartyRoundInfoModel
import com.module.playways.party.room.model.PartySeatInfoModel
import com.module.playways.room.prepare.model.PlayerInfoModel
import com.zq.live.proto.PartyRoom.EPRoundStatus
import com.zq.live.proto.PartyRoom.EPUserRole
import org.greenrobot.eventbus.EventBus


class PartyRoomData : BaseRoomData<PartyRoundInfoModel>() {

    var roomName = ""
        //房间名称
        set(value) {
            if (value != field) {
                field = value
                EventBus.getDefault().post(PartyRoomNameChangeEvent())
            }
        }
    var topicName = ""
        //房间主题
        set(value) {
            if (value != field) {
                field = value
                EventBus.getDefault().post(PartyTopicNameChangeEvent())
            }
        }
    var notice = ""
        // 房间公告
        set(value) {
            if (value != field) {
                field = value
                EventBus.getDefault().post(PartyNoticeChangeEvent())
            }
        }
    var onlineUserCnt = 0 //在线人数
        set(value) {
            if (value != field) {
                field = value
                EventBus.getDefault().post(PartyOnlineUserCntChangeEvent())
            }
        }
    var applyUserCnt = 0 //申请人数
        set(value) {
            if (value != field) {
                field = value
                EventBus.getDefault().post(PartyApplyUserCntChangeEvent())
            }
        }

    var enterPermission = 2 // 2都可以进入  1 只有邀请能进
        set(value) {
            if (value != field) {
                field = value
                EventBus.getDefault().post(PartyEnterPermissionEvent())
            }
        }

    var users = ArrayList<PartyPlayerInfoModel>() // 当前的用户信息 包括 主持人管理员 以及 嘉宾
    var usersMap = HashMap<Int, PartyPlayerInfoModel>()  // 根据id找人

    var seats = ArrayList<PartySeatInfoModel>() // 座位信息
    var seatsSeatIdMap = HashMap<Int, PartySeatInfoModel>() // 根据座位id找座位
    var seatsUserIdMap = HashMap<Int, PartySeatInfoModel>() // 根据用户id找座位

    // 题目信息在轮次信息里 轮次信息在父类的 realRoundInfo 中
    var hostId = 0 //主持人id
        set(value) {
            if (field != value) {
                field = value
                EventBus.getDefault().post(PartyHostChangeEvent(field))
            }
        }
    /**
     * 本人的用户信息
     */
    var myUserInfo: PartyPlayerInfoModel? = null
        set(value) {
            if (field == null && value == null) {

            } else {
                if (field != null && value != null) {
                    if (field!!.same(value)) {

                    } else {
                        field = value
                        EventBus.getDefault().post(PartyMyUserInfoChangeEvent())
                    }
                } else {
                    field = value
                    EventBus.getDefault().post(PartyMyUserInfoChangeEvent())
                }
            }
        }

    /**
     * 本人的座位信息
     */
    var mySeatInfo: PartySeatInfoModel? = null
        set(value) {
            if (field == null && value == null) {

            } else {
                if (field != null && value != null) {
                    if (field!!.same(value)) {

                    } else {
                        field = value
                        EventBus.getDefault().post(PartyMySeatInfoChangeEvent())
                    }
                } else {
                    field = value
                    EventBus.getDefault().post(PartyMySeatInfoChangeEvent())
                }
            }
        }

    companion object {
    }

    override fun getPlayerAndWaiterInfoList(): List<PlayerInfoModel> {
        return users
    }

    override fun getInSeatPlayerInfoList(): List<PlayerInfoModel> {
        return users
    }

    override val gameType: Int
        get() = GameModeType.GAME_MODE_PARTY


    init {

    }

    /**
     * 根据 userId 找 用户信息
     * 找不到观众的
     */
    fun getPlayerInfoById(userId: Int): PartyPlayerInfoModel? {
        return usersMap[userId]
    }

    /**
     * 根据 座位编号 找 座位信息
     */
    fun getSeatInfoBySeq(seatSeq: Int): PartySeatInfoModel? {
        return seatsSeatIdMap[seatSeq]
    }

    /**
     * 根据 userId 找 座位信息
     */
    fun getSeatInfoByUserId(userId: Int): PartySeatInfoModel? {
        return seatsUserIdMap[userId]
    }

    /**
     * 根据 座位编号 找 用户信息
     */
    fun getPlayerInfoBySeq(seatSeq: Int): PartyPlayerInfoModel? {
        var seatInfo = seatsSeatIdMap[seatSeq]
        seatInfo?.userID?.let {
            return getPlayerInfoById(it)
        }
        return null
    }

    /**
     *  座位信息 key为座位序号  value 的座位状态 和该位置上的嘉宾信息
     */
    fun getSeatInfoMap(): HashMap<Int, PartyActorInfoModel> {
        var seatsMap = HashMap<Int, PartyActorInfoModel>()
        this.seats?.let {
            for (info in it) {
                var partyActorInfoModel = PartyActorInfoModel()
                partyActorInfoModel.seat = info
                if (info.userID > 0) {
                    partyActorInfoModel.player = getPlayerInfoById(info.userID)
                }
                seatsMap[info.seatSeq] = partyActorInfoModel
            }
        }
        return seatsMap
    }

    /**
     * 得到自己在Party中的角色等信息
     */
    fun getMyUserInfoInParty(): PartyPlayerInfoModel {
        if (myUserInfo == null) {
            // 如果找不到，则说明自己是观众
            var myinfo = PartyPlayerInfoModel()
            myinfo?.role.add(EPUserRole.EPUR_AUDIENCE.value)
            myinfo.popularity = 0
            myinfo.isOnline = true
            myinfo.userInfo = MyUserInfo.toUserInfoModel(MyUserInfoManager.myUserInfo)
            return myinfo
        } else {
            return myUserInfo!!
        }
    }

    /**
     * 得到自己在Party中的座位等信息
     */
    fun getMySeatInfoInParty(): PartySeatInfoModel? {
        return mySeatInfo
    }

    fun updateUsers(list: ArrayList<PartyPlayerInfoModel>?) {
        if (list?.isNotEmpty() == true) {
            var hasMy = false
            users.clear()
            users.addAll(list)
            usersMap.clear()
            for (info in users) {
                usersMap[info.userID] = info
                if (info.userID == MyUserInfoManager.uid.toInt()) {
                    myUserInfo = info
                    hasMy = true
                }
                if (info.isHost()) {
                    hostId = info.userID
                }
            }
            if (!hasMy) {
                myUserInfo = null
            }
        }
    }

    fun updateSeats(list: ArrayList<PartySeatInfoModel>?) {
        if (list?.isNotEmpty() == true) {
            seats.clear()
            seats.addAll(list)
            seatsSeatIdMap.clear()
            seatsUserIdMap.clear()
            var hasMy = false
            for (info in seats) {
                seatsSeatIdMap[info.seatSeq] = info
                if (info.userID > 0) {
                    seatsUserIdMap[info.userID] = info
                }
                if (info.userID == MyUserInfoManager.uid.toInt()) {
                    mySeatInfo = info
                    hasMy = true
                }
            }
            if (!hasMy) {
                mySeatInfo = null
            }
            EventBus.getDefault().post(PartySeatInfoChangeEvent(-1))
        }
    }

    fun updateSeat(seatInfoModel: PartySeatInfoModel?) {
        var hasSeatChange = false
        if (seatInfoModel != null) {
            var ss = seatsSeatIdMap[seatInfoModel.seatSeq]
            if (ss != null) {
                if (ss.same(seatInfoModel)) {

                } else {
                    seats.remove(ss)
                    seats.add(seatInfoModel)
                    seatsSeatIdMap[seatInfoModel.seatSeq] = seatInfoModel
                    seatsUserIdMap[seatInfoModel.userID] = seatInfoModel
                    hasSeatChange = true
                }
            } else {
                seats.add(seatInfoModel)
                seatsSeatIdMap[seatInfoModel.seatSeq] = seatInfoModel
                seatsUserIdMap[seatInfoModel.userID] = seatInfoModel
                hasSeatChange = true
            }
        }
        if (hasSeatChange) {
            // 座位信息有变化
            EventBus.getDefault().post(PartySeatInfoChangeEvent(seatInfoModel!!.seatSeq))
            if (seatInfoModel.userID == MyUserInfoManager.uid.toInt()) {
                mySeatInfo = seatInfoModel
            }
        }
    }

    fun updateUser(playerInfoModel: PartyPlayerInfoModel, seatInfoModel: PartySeatInfoModel?) {
        // 判断是否要更新用户
        if (playerInfoModel.isNotOnlyAudience()) {
            var hasUserChange = false
            var uu = usersMap[playerInfoModel.userID]
            if (uu != null) {
                if (uu.same(playerInfoModel)) {
                } else {
                    users.remove(uu)
                    users.add(playerInfoModel)
                    usersMap[playerInfoModel.userID] = playerInfoModel
                    hasUserChange = true
                }
            } else {
                users.add(playerInfoModel)
                usersMap[playerInfoModel.userID] = playerInfoModel
                hasUserChange = true
            }
            if (hasUserChange) {
                // 座位信息有变化
                if (playerInfoModel.userID == MyUserInfoManager.uid.toInt()) {
                    myUserInfo = playerInfoModel
                }
            }
        } else {
            // 是观众了 去除信息
            usersMap.remove(playerInfoModel.userID)
            if (playerInfoModel.userID == MyUserInfoManager.uid.toInt()) {
                myUserInfo = null
            }
        }
        updateSeat(seatInfoModel)
    }

    fun removeUser(playerInfoModel: PartyPlayerInfoModel) {
        usersMap.remove(playerInfoModel.userID)
        if (playerInfoModel.userID == MyUserInfoManager.uid.toInt()) {
            myUserInfo = null
        }
        if (seatsUserIdMap.containsKey(playerInfoModel.userID)) {
            var seat = seatsUserIdMap[playerInfoModel.userID]
            seat?.userID = 0
            EventBus.getDefault().post(PartySeatInfoChangeEvent(seat?.seatSeq ?: -1))
        }
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
                lastRoundInfoModel?.updateStatus(false, EPRoundStatus.PRS_END.value)
                realRoundInfo = null
                EventBus.getDefault().post(PartyRoundChangeEvent(lastRoundInfoModel, null))
            }
            return
        }
        MyLog.d(TAG, "尝试切换轮次 checkRoundInEachMode mExpectRoundInfo.roundSeq=" + expectRoundInfo!!.roundSeq)
        if ((expectRoundInfo?.roundSeq ?: 0) > (realRoundInfo?.roundSeq
                        ?: 0) || realRoundInfo == null) {
            // 轮次大于，才切换
            val lastRoundInfoModel = realRoundInfo
            lastRoundInfoModel?.updateStatus(false, EPRoundStatus.PRS_END.value)
            realRoundInfo = expectRoundInfo
            EventBus.getDefault().post(PartyRoundChangeEvent(lastRoundInfoModel, realRoundInfo))
        }
    }

    fun loadFromRsp(rsp: JoinPartyRoomRspModel) {
        this.gameId = rsp.roomID
        this.agoraToken = rsp.agoraToken
        this.applyUserCnt = rsp.applyUserCnt ?: 0
        this.gameStartTs = (rsp.gameStartTimeMs ?: 0L)
        this.notice = rsp.notice ?: ""
        this.onlineUserCnt = rsp.onlineUserCnt ?: 0
        this.roomName = rsp.roomName ?: ""
        this.topicName = rsp.topicName ?: ""
        updateSeats(rsp.seats)
        updateUsers(rsp.users)
        this.expectRoundInfo = rsp.currentRound
        this.enterPermission = rsp.enterPermission
    }
}
