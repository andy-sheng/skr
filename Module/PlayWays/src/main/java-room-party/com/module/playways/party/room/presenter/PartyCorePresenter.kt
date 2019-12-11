package com.module.playways.party.room.presenter

import android.os.Handler
import android.os.Message
import com.alibaba.fastjson.JSON
import com.common.core.account.UserAccountManager
import com.common.core.myinfo.MyUserInfoManager
import com.common.jiguang.JiGuangPush
import com.common.log.DebugLogView
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.common.statistics.StatisticsAdapter
import com.common.utils.ActivityUtils
import com.engine.EngineEvent
import com.engine.Params
import com.module.ModuleServiceManager
import com.module.common.ICallback
import com.module.playways.party.room.PartyRoomData
import com.module.playways.party.room.PartyRoomServerApi
import com.module.playways.party.room.event.*
import com.module.playways.party.room.model.PartyPlayerInfoModel
import com.module.playways.party.room.model.PartyRoundInfoModel
import com.module.playways.party.room.model.PartySeatInfoModel
import com.module.playways.party.room.ui.IPartyRoomView
import com.module.playways.room.gift.event.GiftBrushMsgEvent
import com.module.playways.room.gift.event.UpdateCoinEvent
import com.module.playways.room.gift.event.UpdateMeiliEvent
import com.module.playways.room.msg.event.GiftPresentEvent
import com.module.playways.room.msg.filter.PushMsgFilter
import com.module.playways.room.msg.manager.PartyRoomMsgManager
import com.module.playways.room.room.comment.model.CommentSysModel
import com.module.playways.room.room.event.PretendCommentMsgEvent
import com.zq.live.proto.PartyRoom.*
import com.zq.mediaengine.kit.ZqEngineKit
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


class PartyCorePresenter(var mRoomData: PartyRoomData, var roomView: IPartyRoomView) : RxLifeCyclePresenter() {

    companion object {

        internal val MSG_ENSURE_IN_RC_ROOM = 9// 确保在融云的聊天室，保证融云的长链接

    }

    internal var mAbsenTimes = 0

    internal var mRoomServerApi = ApiManager.getInstance().createService(PartyRoomServerApi::class.java)

    internal var mDestroyed = false

    internal var mUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_ENSURE_IN_RC_ROOM -> {
                    MyLog.d(TAG, "handleMessage 长时间没收到push，重新进入融云房间容错")
                    ModuleServiceManager.getInstance().msgService.leaveChatRoom(mRoomData.gameId.toString() + "")
                    joinRcRoom(0)
                    ensureInRcRoom()
                }
            }
        }
    }


    internal var mPushMsgFilter: PushMsgFilter<*> = PushMsgFilter<PartyRoomMsg> { msg ->
        msg != null && msg.roomID == mRoomData.gameId
    }

    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        PartyRoomMsgManager.addFilter(mPushMsgFilter)
        joinRoomAndInit(true)
        startSyncGameStatus()
    }

    /**
     * 加入引擎房间
     * 加入融云房间
     * 系统消息弹幕
     */
    private fun joinRoomAndInit(first: Boolean) {
        MyLog.w(TAG, "joinRoomAndInit" + " first=" + first + ", gameId is " + mRoomData.gameId)
        mAbsenTimes = 0

        if (mRoomData.gameId > 0) {
            var reInit = false
            if (first) {
                reInit = true
            }
            if (reInit) {
                val params = Params.getFromPref()
                //            params.setStyleEnum(Params.AudioEffect.none);
                params.scene = Params.Scene.doubleChat
                params.isEnableAudio = true
                ZqEngineKit.getInstance().init("partyroom", params)
            }
            var isAnchor = mRoomData?.getMyUserInfoInParty()?.isRole(EPUserRole.EPUR_HOST.value, EPUserRole.EPUR_GUEST.value)
            DebugLogView.println(TAG, "isAnchor=$isAnchor")
            ZqEngineKit.getInstance().joinRoom(mRoomData.gameId.toString(), UserAccountManager.uuidAsLong.toInt(), isAnchor, mRoomData.agoraToken)
            // 不发送本地音频, 会造成第一次抢没声音
            //ZqEngineKit.getInstance().muteLocalAudioStream(true)
        } else {
            MyLog.e(TAG, "房间号不合法 mRoomData.gameId=" + mRoomData.gameId)
        }
        joinRcRoom(-1)
        if (mRoomData.gameId > 0) {
//            for (playerInfoModel in mRoomData.getPlayerAndWaiterInfoList()) {
//                if (!playerInfoModel.isOnline) {
//                    continue
//                }
//                pretendEnterRoom(playerInfoModel)
//            }
            pretendRoomNameSystemMsg("${mRoomData.roomName}", CommentSysModel.TYPE_ENTER_ROOM)
        }
        startHeartbeat()
        startSyncGameStatus()
    }

//    fun changeMatchState(isChecked: Boolean) {
//        launch {
//            val map = mutableMapOf(
//                    "roomID" to mRoomData?.gameId,
//                    "matchStatus" to (if (isChecked) 2 else 1)
//            )
//
//            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
//            val result = subscribe(RequestControl("$TAG changeMatchState", ControlType.CancelLast)) {
//                mRoomServerApi.changeMatchStatus(body)
//            }
//
//            if (result.errno == 0) {
//                if (isChecked) {
////                    val commentSysModel = CommentSysModel(GameModeType.GAME_MODE_RACE, "房主已将房间设置为 不允许用户匹配进入")
////                    EventBus.getDefault().post(PretendCommentMsgEvent(commentSysModel))
//                } else {
////                    val commentSysModel = CommentSysModel(GameModeType.GAME_MODE_RACE, "房主已将房间设置为 允许用户匹配进入")
////                    EventBus.getDefault().post(PretendCommentMsgEvent(commentSysModel))
//                }
//            } else {
//                U.getToastUtil().showShort(result.errmsg)
//            }
//        }
//    }

    private fun joinRcRoom(deep: Int) {
        if (deep > 4) {
            MyLog.d(TAG, "加入融云房间，重试5次仍然失败，放弃")
            return
        }
        if (mRoomData.gameId > 0) {
            ModuleServiceManager.getInstance().msgService.joinChatRoom(mRoomData.gameId.toString(), -1, object : ICallback {
                override fun onSucess(obj: Any?) {
                    MyLog.d(TAG, "加入融云房间成功")
                }

                override fun onFailed(obj: Any?, errcode: Int, message: String?) {
                    MyLog.d(TAG, "加入融云房间失败， msg is $message, errcode is $errcode")
                    joinRcRoom(deep + 1)
                }
            })
            if (deep == -1) {
                /**
                 * 说明是初始化时那次加入房间，这时加入极光房间做个备份，使用tag的方案
                 */
                JiGuangPush.joinSkrRoomId(mRoomData.gameId.toString())
            }
        }
    }

    private fun ensureInRcRoom() {
        mUiHandler.removeMessages(MSG_ENSURE_IN_RC_ROOM)
        mUiHandler.sendEmptyMessageDelayed(MSG_ENSURE_IN_RC_ROOM, (30 * 1000).toLong())
    }

    private fun pretendSystemMsg(text: String) {
        val commentSysModel = CommentSysModel(mRoomData.gameType, text)
        EventBus.getDefault().post(PretendCommentMsgEvent(commentSysModel))
    }

    private fun pretendRoomNameSystemMsg(roomName: String?, type: Int) {
        val commentSysModel = CommentSysModel(roomName ?: "", type)
        EventBus.getDefault().post(PretendCommentMsgEvent(commentSysModel))
    }

    /**
     * 由ui层告知
     * 开场动画结束
     */
    fun onOpeningAnimationOver() {
        // 开始触发触发轮次变化
        mRoomData.checkRoundInEachMode()
        ensureInRcRoom()
        userStatisticForIntimacy()
    }

    /**
     * 如果确定是自己唱了,预先可以做的操作
     */
    private fun preOpWhenSelfRound() {

    }

    private fun preOpWhenOtherRound() {
    }

    /**
     * 真正打开引擎开始演唱
     */
    fun beginSing() {
        // 打开引擎，变为主播
        // 需要上传音频伪装成机器人
//        if (mRoomData.realRoundInfo?.isNormalRound == true) {
//            /**
//             * 个人标签声音
//             */
//            val fileName = String.format(PERSON_LABEL_SAVE_PATH_FROMAT, mRoomData.gameId, mRoomData.realRoundInfo?.roundSeq)
//            val savePath = U.getAppInfoUtils().getFilePathInSubDir("grab_save", fileName)
//            ZqEngineKit.getInstance().startAudioRecording(savePath, false)
//        }
    }

    fun muteAllRemoteAudioStreams(mute: Boolean, fromUser: Boolean) {
        if (fromUser) {
            mRoomData.isMute = mute
        }
        ZqEngineKit.getInstance().muteAllRemoteAudioStreams(mute)
        // 如果是机器人的话
        if (mute) {
            // 如果是静音
//            if (mExoPlayer != null) {
//                mExoPlayer!!.setMuteAudio(true)
//            }
        } else {
            // 如果打开静音
//            if (mExoPlayer != null) {
//                mExoPlayer!!.setMuteAudio(false)
//            }
        }
    }


    override fun destroy() {
        MyLog.d(TAG, "destroy begin")
        super.destroy()
        mDestroyed = true
        Params.save2Pref(ZqEngineKit.getInstance().params)
        if (!mRoomData.isHasExitGame) {
            exitRoom("destroy")
        }
        cancelSyncGameStatus()
        heartbeatJob?.cancel()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        ZqEngineKit.getInstance().destroy("partyroom")
        mUiHandler.removeCallbacksAndMessages(null)
        PartyRoomMsgManager.removeFilter(mPushMsgFilter)
        ModuleServiceManager.getInstance().msgService.leaveChatRoom(mRoomData.gameId.toString())
        JiGuangPush.exitSkrRoomId(mRoomData.gameId.toString())
        MyLog.d(TAG, "destroy over")
    }

    /**
     * 上报轮次结束信息
     */
    fun sendRoundOverInfo() {
//        if(mRoomData?.realRoundInfo?.hasSendRoundOverInfo == false){
//            MyLog.w(TAG, "上报我的演唱结束")
//            mRoomData?.realRoundInfo?.hasSendRoundOverInfo = true
//            val map = HashMap<String, Any>()
//            map["roomID"] = mRoomData.gameId
//            map["roundSeq"] = mRoomData.realRoundInfo?.roundSeq ?: 0
//
//            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
//            launch {
//                var result = subscribe { mRoomServerApi.sendRoundOver(body) }
//                if (result.errno == 0) {
//                    MyLog.w(TAG, "演唱结束上报成功 traceid is " + result.traceId)
//                } else {
//                    MyLog.w(TAG, "演唱结束上报失败 traceid is " + result.traceId)
//                }
//            }
//        }else{
//            MyLog.w(TAG, "已经上报过演唱结束")
//        }
    }


    /**
     * 放弃演唱接口
     */
    fun giveUpSing(okCallback: (() -> Unit)?) {
        MyLog.w(TAG, "我放弃演唱")
//        val map = HashMap<String, Any?>()
//        map["roomID"] = mRoomData.gameId
//        map["roundSeq"] = mRoomData.realRoundInfo?.roundSeq ?: 0
//        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
//        launch {
//            var result = subscribe(RequestControl("giveUpSing", ControlType.CancelThis)) {
//                mRoomServerApi.giveUpSing(body)
//            }
//            if (result.errno == 0) {
//                //closeEngine()
//                okCallback?.invoke()
//                MyLog.w(TAG, "放弃演唱上报成功 traceid is " + result.traceId)
//            } else {
//                MyLog.w(TAG, "放弃演唱上报失败 traceid is " + result.traceId)
//            }
//        }
    }

    /**
     * 退出房间
     */
    fun exitRoom(from: String) {
        MyLog.w(TAG, "exitRoom from=$from")
        val map = HashMap<String, Any>()
        map["roomID"] = mRoomData.gameId
        mRoomData.isHasExitGame = true
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        // 不想 destroy 时被取消
        GlobalScope.launch {
            var result = subscribe { mRoomServerApi.exitRoom(body) }
            if (result.errno == 0) {

            }
        }
    }

    var heartbeatJob: Job? = null

    /**
     * 主持人心跳
     */
    private fun startHeartbeat() {
        if (mRoomData?.getMyUserInfoInParty()?.isHost()) {
            heartbeatJob?.cancel()
            heartbeatJob = launch {
                while (true) {
                    val map = mutableMapOf(
                            "roomID" to mRoomData.gameId,
                            "hostUserID" to MyUserInfoManager.uid
                    )
                    val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                    val result = subscribe { mRoomServerApi.heartbeat(body) }
                    if (result.errno == 0) {

                    } else {

                    }
                    delay(60 * 1000)
                }
            }
        }
    }

    private fun cancelSyncGameStatus() {
        syncJob?.cancel()
    }

    var syncJob: Job? = null

    private fun startSyncGameStatus() {
        if (mRoomData.isIsGameFinish) {
            MyLog.w(TAG, "游戏结束了，还特么Sync")
            return
        }
        syncJob?.cancel()
        syncJob = launch {
            while (true) {
                delay(10 * 1000)
                val result = subscribe { mRoomServerApi.syncStatus(mRoomData.gameId.toLong()) }
                if (result.errno == 0) {
                    val gameOverTimeMs = result.data.getLongValue("gameOverTimeMs")
                    if (gameOverTimeMs > 0) {
                        mRoomData.gameOverTs = gameOverTimeMs
                        DebugLogView.println(TAG, "gameOverTimeMs=${gameOverTimeMs} 游戏结束时间>0 ，游戏结束，退出房间")
                        // 游戏结束了，停服了
                        mRoomData.expectRoundInfo = null
                        mRoomData.checkRoundInEachMode()
                    } else {
                        val syncStatusTimeMs = result.data.getLongValue("syncStatusTimeMs")
                        if (syncStatusTimeMs > mRoomData.lastSyncTs) {
                            mRoomData.lastSyncTs = syncStatusTimeMs
                            val thisRound = JSON.parseObject(result.data.getString("currentRound"), PartyRoundInfoModel::class.java)
                            val onlineUserCnt = result.data.getIntValue("onlineUserCnt")
                            val applyUserCnt = result.data.getIntValue("applyUserCnt")
                            val seats = JSON.parseArray(result.data.getString("seats"), PartySeatInfoModel::class.java)
                            var users = JSON.parseArray(result.data.getString("users"), PartyPlayerInfoModel::class.java)
                            // 延迟10秒sync ，一旦启动sync 间隔 5秒 sync 一次
                            processSyncResult(onlineUserCnt, applyUserCnt, seats, users, thisRound)
                        }
                    }
                } else {

                }
            }
        }
    }

    /**
     * 为了方便服务器亲密度结算
     */
    private fun userStatisticForIntimacy() {
//        launch {
//            while (true) {
//                delay(10 * 60 * 1000)
//                val l1 = java.util.ArrayList<Int>()
//                for (m in mRoomData.getPlayerAndWaiterInfoList()) {
//                    if (m.userID != MyUserInfoManager.uid.toInt()) {
//                        if (mRoomData.preUserIDsSnapShots.contains(m.userID)) {
//                            l1.add(m.userID)
//                        }
//                    }
//                }
//                if (l1.isNotEmpty()) {
//                    val map = java.util.HashMap<String, Any>()
//                    map["gameID"] = mRoomData.gameId
//                    map["mode"] = GameModeType.GAME_MODE_GRAB
//                    val ts = System.currentTimeMillis()
//                    map["timeMs"] = ts
//                    map["sign"] = U.getMD5Utils().MD5_32("skrer|" + MyUserInfoManager.uid + "|" + ts)
//                    map["userID"] = MyUserInfoManager.uid
//                    map["preUserIDs"] = l1
//                    val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
//                    val result = subscribe { mRoomServerApi.userStatistic(body) }
//                    if (result.errno == 0) {
//
//                    }
//                } else {
//                    break
//                }
//            }
//        }
    }

    /**
     * 换房间 或者 接受邀请时 你已经在派对房了
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyChangeRoomEvent) {
        mRoomData.loadFromRsp(event.mJoinGrabRoomRspModel)
        joinRoomAndInit(true)
        onOpeningAnimationOver()
    }

    /**
     * 我的座位信息变化了，主要处理开闭麦下麦等
     */
    @Subscribe(threadMode = ThreadMode.MAIN, priority = Int.MAX_VALUE)
    fun onEvent(event: PartyMySeatInfoChangeEvent) {
        DebugLogView.println(TAG, "PartyMySeatInfoChangeEvent 我是${mRoomData.myUserInfo?.role} 座位 ${mRoomData.mySeatInfo}")
        if (mRoomData.mySeatInfo?.seatStatus == ESeatStatus.SS_OPEN.value) {
            // 我至少是个主播
            if (!ZqEngineKit.getInstance().params.isAnchor) {
                ZqEngineKit.getInstance().setClientRole(true)
            }
            if (mRoomData.mySeatInfo?.micStatus == EMicStatus.MS_OPEN.value) {
                // 我得开着麦
                mRoomData.isMute = false
                ZqEngineKit.getInstance().muteLocalAudioStream(false)
            } else {
                mRoomData.isMute = true
                ZqEngineKit.getInstance().muteLocalAudioStream(true)
            }
        } else {
            if (mRoomData.myUserInfo?.isHost() == true) {

            } else {
                // 我不是主播
                ZqEngineKit.getInstance().setClientRole(false)
            }
        }
    }

    /**
     * 我的角色变化了
     * 判断我的最新角色 然后做相应逻辑
     */
    @Subscribe(threadMode = ThreadMode.MAIN, priority = Int.MAX_VALUE)
    fun onEvent(event: PartyMyUserInfoChangeEvent) {
        DebugLogView.println(TAG, "PartyMyUserInfoChangeEvent 我是${mRoomData.myUserInfo?.role} 座位 ${mRoomData.mySeatInfo}")
        if (mRoomData.myUserInfo?.isHost() == true) {
            if (!ZqEngineKit.getInstance().params.isAnchor) {
                ZqEngineKit.getInstance().setClientRole(true)
                mRoomData.isMute = false
            }
        } else if (mRoomData.myUserInfo?.isAdmin() == true) {
            //我是管理员了
        } else if (mRoomData.myUserInfo?.isGuest() == true) {
            // 我是嘉宾了 开麦闭麦交给座位事件处理
        }
    }

    /**
     * 轮次切换事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyRoundChangeEvent) {
        MyLog.d(TAG, "RelayRoundChangeEvent = $event")
        processStatusChange(1, event.lastRound, event.newRound)
    }

    /**
     * 轮次内状态更新
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyRoundStatusChangeEvent) {
        MyLog.d(TAG, "RelayRoundStatusChangeEvent =$event")
        processStatusChange(2, null, event.roundInfo)
    }

    private fun processStatusChange(from: Int, lastRound: PartyRoundInfoModel?, thisRound: PartyRoundInfoModel?) {
        DebugLogView.println(TAG, "processStatusChange from=$from roundSeq=${thisRound?.roundSeq} statusNow=${thisRound?.status}")
        // 轮次变化尝试更新头像
//        mUiHandler.removeMessages(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS)
//        closeEngine()
//        ZqEngineKit.getInstance().stopRecognize()
        if (thisRound == null) {
            // 游戏结束了
//            roomView.gameOver()
            return
        }

        if (thisRound.status == EPRoundStatus.PRS_WAITING.value) {
            // 等待阶段
            roomView.showRoundOver(lastRound) {
                roomView.showWaiting()
            }

        } else if (thisRound.status == EPRoundStatus.PRS_PLAY_GAME.value) {

            roomView.showRoundOver(lastRound) {
                // 演唱阶段
//                val size = U.getActivityUtils().activityList.size
//                var needTips = false
//                for (i in size - 1 downTo 0) {
//                    val activity = U.getActivityUtils().activityList[i]
//                    if (activity is RelayRoomActivity) {
//                        break
//                    } else {
//                        activity.finish()
//                        needTips = true
//                    }
//                }
//                if (needTips) {
//                    U.getToastUtil().showLong("你的演唱开始了")
//                }
                roomView.gameBegin(thisRound)
            }
        } else if (thisRound.status == EPRoundStatus.PRS_END.value) {

        }
    }


    private fun closeEngine() {
        if (mRoomData.gameId > 0) {
            ZqEngineKit.getInstance().stopAudioMixing()
//            mUiHandler.removeMessages(MSG_TURN_CHANGE)
//            mUiHandler.removeMessages(MSG_LAUNER_MUSIC)
//            ZqEngineKit.getInstance().stopAudioRecording()
//            if (ZqEngineKit.getInstance().params.isAnchor) {
//                ZqEngineKit.getInstance().setClientRole(false)
//            }
        }
    }

    /**
     * 引擎相关事件
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: EngineEvent) {
        if (event.getType() == EngineEvent.TYPE_MUSIC_PLAY_STATE_CHANGE) {
//            var state = event.obj as EngineEvent.MusicStateChange
//            if (state.isPlayOk && mRoomData.realRoundInfo?.accLoadingOk == false) {
//                mRoomData.realRoundInfo?.accLoadingOk = true
//                var progress = mRoomData.getSingCurPosition()
//                DebugLogView.println(TAG, "伴奏加载ok progress=${progress}")
//                if (progress != Long.MAX_VALUE) {
//                    if (progress > 0) {
//                        DebugLogView.println(TAG, "EngineEvent 超时上车了")
//                        ZqEngineKit.getInstance().setAudioMixingPosition(progress.toInt())
//                        mUiHandler.post {
//                            realSingBegin()
//                        }
//                    } else {
//                        DebugLogView.println(TAG, "EngineEvent 先暂停 ${-progress}ms后 resume")
//                        ZqEngineKit.getInstance().pauseAudioMixing()
//                        mUiHandler.removeMessages(MSG_LAUNER_MUSIC)
//                        mUiHandler.sendEmptyMessageDelayed(MSG_LAUNER_MUSIC, -progress)
//                    }
//                } else {
//                    MyLog.e(TAG, "当前播放进度非法2")
//                }
//            }
        } else if (event.getType() == EngineEvent.TYPE_MUSIC_PLAY_FINISH) {
//            DebugLogView.println(TAG, "伴奏播放完毕")
//            sendRoundOverInfo()
        } else if (event.getType() == EngineEvent.TYPE_MUSIC_PLAY_TIME_FLY_LISTENER) {
            //DebugLogView.println(TAG, "伴奏播放进度")
//            val timeInfo = event.getObj() as EngineEvent.MixMusicTimeInfo
//            //这个是唱的时间，先在按长度算时间
//            var progress = mRoomData.getSingCurPosition()
//            val shift = progress -timeInfo.current
//            //DebugLogView.println(TAG, "当前伴奏与预定时间的偏移为${shift}")
//            if (abs(shift) >1000 && progress>=0) {
//                DebugLogView.println(TAG, "当前伴奏与预定时间的偏移过大 为${shift}")
//                // 伴奏对齐，重新发送轮次切换
//                ZqEngineKit.getInstance().setAudioMixingPosition(mRoomData.getSingCurPosition().toInt())
//                launcherNextTurn()
//            }
//            if(mRoomData.hasOverThisRound()){
//                sendRoundOverInfo()
//            }
        } else if (event.getType() == EngineEvent.TYPE_USER_ROLE_CHANGE) {
//            val roleChangeInfo = event.getObj<EngineEvent.RoleChangeInfo>()
//            if (roleChangeInfo.newRole == 1) {
//                val roundInfoModel = mRoomData.realRoundInfo
//                if (roundInfoModel != null && roundInfoModel.singBySelf()) {
//                    MyLog.d(TAG, "演唱环节切换主播成功")
//                    onChangeBroadcastSuccess()
//                }
//            }
        } else if (event.getType() == EngineEvent.TYPE_USER_MUTE_AUDIO) {
            //            UserStatus userStatus = event.getUserStatus();
            //            if (userStatus != null) {
            //                MyLog.d(TAG, "有人mute变化 uid=" + userStatus.getUserId());
            //                if (userStatus.getUserId() == mRoomData.getOwnerId()) {
            //                    if (mRoomData.isOwner()) {
            //                        MyLog.d(TAG, "自己就是房主，忽略");
            //                    } else {
            //                        if (!userStatus.isAudioMute()) {
            //                            MyLog.d(TAG, "房主解开mute，如果检测到房主说话，音量就衰减");
            //                            weakVolume(1000);
            //                        } else {
            //                            MyLog.d(TAG, "房主mute了，恢复音量");
            //                            mUiHandler.removeMessages(MSG_RECOVER_VOLUME);
            //                            mUiHandler.sendEmptyMessage(MSG_RECOVER_VOLUME);
            //                        }
            //                    }
            //                }
            //            }
        } else if (event.getType() == EngineEvent.TYPE_USER_AUDIO_VOLUME_INDICATION) {
            //            List<EngineEvent.UserVolumeInfo> list = event.getObj();
            //            for (EngineEvent.UserVolumeInfo uv : list) {
            //                //    MyLog.d(TAG, "UserVolumeInfo uv=" + uv);
            //                if (uv != null) {
            //                    int uid = uv.getUid();
            //                    if (uid == 0) {
            //                        uid = (int) MyUserInfoManager.getInstance().getUid();
            //                    }
            //                    if (mRoomData != null
            //                            && uid == mRoomData.getOwnerId()
            //                            && uv.getVolume() > 40
            //                            && !mRoomData.isOwner()) {
            //                        MyLog.d(TAG, "房主在说话");
            //                        weakVolume(1000);
            //                    }
            //                }
            //            }
        } else {
            // 可以考虑监听下房主的说话提示 做下容错
        }
    }

//    /**
//     * 成功切换为主播
//     */
//    private fun onChangeBroadcastSuccess() {
//        MyLog.d(TAG, "onChangeBroadcastSuccess 我的演唱环节")
//        mUiHandler.removeMessages(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS)
//        mUiHandler.post(Runnable {
//            var songModel: SongModel? = mRoomData.realRoundInfo?.music
//            if (songModel == null) {
//                return@Runnable
//            }
//            // 开始开始混伴奏，开始解除引擎mute
//            val accFile = SongResUtils.getAccFileByUrl(songModel?.acc)
//            // midi不需要在这下，只要下好，native就会解析，打分就能恢复
//            val midiFile = SongResUtils.getMIDIFileByUrl(songModel?.midi)
//            MyLog.d(TAG, "onChangeBroadcastSuccess 我的演唱环节 info=${songModel.toSimpleString()} acc=${songModel.acc} midi=${songModel.midi} ")
//
//            // 下载midi
//            if (midiFile != null && !midiFile.exists()) {
//                MyLog.d(TAG, "onChangeBroadcastSuccess 下载midi文件 url=${songModel.midi} => local=${midiFile.path}")
//                U.getHttpUtils().downloadFileAsync(songModel.midi, midiFile, true, null)
//            }
//
//            //  播放伴奏
//            val songBeginTs = songModel.beginMs
//            if (accFile != null && accFile.exists()) {
//                // 伴奏文件存在
//                ZqEngineKit.getInstance().startAudioMixing(MyUserInfoManager.uid.toInt(), accFile.absolutePath, midiFile.absolutePath, songBeginTs.toLong(), false, false, 1)
//            } else {
//                ZqEngineKit.getInstance().startAudioMixing(MyUserInfoManager.uid.toInt(), songModel.acc, midiFile.absolutePath, songBeginTs.toLong(), false, false, 1)
//            }
//
//        })
//    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: MAddMusicMsg) {
//        MyLog.d(TAG, "MAddMusicMsg event=$event")
//        roomView.showSongCount(event.musicCnt)
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: MDelMusicMsg) {
//        MyLog.d(TAG, "MAddMusicMsg event=$event")
//        roomView.showSongCount(event.musicCnt)
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PJoinNoticeMsg) {
        MyLog.d(TAG, "onEvent event = $event")
        var playerInfoModel = PartyPlayerInfoModel.parseFromPb(event.user)
        var seatInfoModel: PartySeatInfoModel? = null
        if (event.hasSeat()) {
            seatInfoModel = PartySeatInfoModel.parseFromPb(event.seat)
        }
        roomView.joinNotice(playerInfoModel)
        mRoomData.updateUser(playerInfoModel, seatInfoModel)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PFixRoomNoticeMsg) {
        MyLog.d(TAG, "onEvent event = $event")
        mRoomData.notice = event.newRoomNotice
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyNoticeChangeEvent) {
        pretendSystemMsg("房主将公告修改为 ${mRoomData.notice}")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PChangeRoomTopicMsg) {
        mRoomData.topicName = event.newTopic
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyTopicNameChangeEvent) {
        pretendSystemMsg("房主将主题修改为 ${mRoomData.topicName}")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PChangeRoomEnterPermissionMsg) {
        mRoomData.enterPermission = event.permission.value
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PartyEnterPermissionEvent) {
        pretendSystemMsg("房主将进房间权限修改为 ${mRoomData.enterPermission}")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PSetRoomAdminMsg) {
        MyLog.d(TAG, "onEvent event = $event")
        if (event.setType == ESetAdminType.SAT_ADD) {
            pretendSystemMsg("${event.user.userInfo.nickName} 被 ${event.opUser.userInfo.nickName} 设置为管理员")
        } else {
            pretendSystemMsg("${event.user.userInfo.nickName} 被 ${event.opUser.userInfo.nickName} 删除了管理员")
        }
        val p = PartyPlayerInfoModel.parseFromPb(event.user)
        mRoomData.updateUser(p, null)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PSetAllMemberMicMsg) {
        MyLog.d(TAG, "onEvent event = $event")
        if (event.micStatus.value == EMicStatus.MS_CLOSE.value) {
            pretendSystemMsg("${event.opUser.userInfo.nickName} 设置为 全员禁麦")
        } else if (event.micStatus.value == EMicStatus.MS_OPEN.value) {
            pretendSystemMsg("${event.opUser.userInfo.nickName} 设置为 解除全员禁麦")
        }
        mRoomData.updateSeats(PartySeatInfoModel.parseFromPb(event.seatsList))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PSetUserMicMsg) {
        MyLog.d(TAG, "onEvent event = $event")
        var partySeatInfoModel = PartySeatInfoModel()
        partySeatInfoModel.micStatus = event.micStatus.value
        partySeatInfoModel.seatSeq = event.seatSeq
        partySeatInfoModel.userID = event.userID
        partySeatInfoModel.seatStatus = ESeatStatus.SS_OPEN.value
        mRoomData.updateSeat(partySeatInfoModel)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PSetSeatStatusMsg) {
        MyLog.d(TAG, "onEvent event = $event")
        var partySeatInfoModel = PartySeatInfoModel()
        partySeatInfoModel.seatSeq = event.seatSeq
        partySeatInfoModel.seatStatus = event.seatStatus.value
        partySeatInfoModel.micStatus = mRoomData.getSeatInfoBySeq(event.seatSeq)?.micStatus ?: 0
        partySeatInfoModel.userID = mRoomData.getSeatInfoBySeq(event.seatSeq)?.userID ?: 0
        mRoomData.updateSeat(partySeatInfoModel)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PApplyForGuest) {
        MyLog.d(TAG, "onEvent event = $event")
//        if(mRoomData.myUserInfo?.isHost() == true || mRoomData.){
//        }
        pretendSystemMsg("${event.user.userInfo.nickName} 申请上麦")
        mRoomData.applyUserCnt = event.applyUserCnt
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PGetSeatMsg) {
        MyLog.d(TAG, "onEvent event = $event")
        mRoomData.updateUser(PartyPlayerInfoModel.parseFromPb(event.user), PartySeatInfoModel.parseFromPb(event.seatInfo))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PBackSeatMsg) {
        MyLog.d(TAG, "onEvent event = $event")
        var partySeatInfoModel = PartySeatInfoModel()
        var n = mRoomData.getSeatInfoBySeq(event.seatSeq)
        partySeatInfoModel.seatSeq = event.seatSeq
        partySeatInfoModel.userID = 0
        partySeatInfoModel.micStatus = n?.micStatus ?: 0
        partySeatInfoModel.seatStatus = n?.seatStatus ?: 0
        mRoomData.updateUser(PartyPlayerInfoModel.parseFromPb(event.user), partySeatInfoModel)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PKickoutUserMsg) {
        MyLog.d(TAG, "onEvent event = $event")
        pretendSystemMsg("${event.kickResultContent}")
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PExitGameMsg) {
        MyLog.d(TAG, "onEvent event = $event")
        mRoomData.applyUserCnt = event.applyUserCnt
        mRoomData.onlineUserCnt = event.onlineUserCnt
        var u = PartyPlayerInfoModel.parseFromPb(event.user)
        mRoomData.removeUser(u)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PGameOverMsg) {
        MyLog.d(TAG, "onEvent event = $event")
        mRoomData.expectRoundInfo = null
        mRoomData.checkRoundInEachMode()
    }


    /**
     * 轮次变化
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PNextRoundMsg) {
        MyLog.w(TAG, "收到服务器的某一个人轮次结束的push currentRound:${event.currentRound}")
        MyLog.w(TAG, "收到服务器的某一个人轮次结束的push nextRound:${event.nextRound}")
        ensureInRcRoom()
//        roomView.showSongCount(event.musicCnt)
        var currentRound = PartyRoundInfoModel.parseFromRoundInfo(event.currentRound)
        var nextRound = PartyRoundInfoModel.parseFromRoundInfo(event.nextRound)
        if (nextRound.roundSeq > (mRoomData.expectRoundInfo?.roundSeq ?: 0)) {
            // 游戏轮次结束
            // 轮次确实比当前的高，可以切换
            MyLog.w(TAG, "nextRound.roundSeq=${nextRound.roundSeq} 轮次确实比当前的高，可以切换")
            mRoomData.expectRoundInfo = nextRound
            mRoomData.checkRoundInEachMode()
        } else {
            MyLog.w(TAG, "轮次比当前轮次还小,直接忽略 当前轮次:" + mRoomData.expectRoundInfo?.roundSeq
                    + " push轮次:" + event.currentRound.roundSeq)
        }
    }


    // TODO sync
    /**
     * 同步
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: PSyncMsg) {
        ensureInRcRoom()
        MyLog.w(TAG, "收到服务器 sync push更新状态 ,event=$event")
        if (event.syncStatusTimeMs > mRoomData.lastSyncTs) {
            mRoomData.lastSyncTs = event.syncStatusTimeMs
            var onlineUserCnt = event.onlineUserCnt
            var applyUserCnt = event.applyUserCnt

            var seats = PartySeatInfoModel.parseFromPb(event.seatsList)
            var users = PartyPlayerInfoModel.parseFromPb(event.usersList)
            var thisRound = PartyRoundInfoModel.parseFromRoundInfo(event.currentRound)
            // 延迟10秒sync ，一旦启动sync 间隔 5秒 sync 一次
            startSyncGameStatus()
            processSyncResult(onlineUserCnt, applyUserCnt, seats, users, thisRound)
        }
    }

    /**
     * 明确数据可以刷新
     */
    private fun processSyncResult(onlineUserCnt: Int, applyUserCnt: Int, seats: List<PartySeatInfoModel>, users: List<PartyPlayerInfoModel>, thisRound: PartyRoundInfoModel) {
        mRoomData.onlineUserCnt = onlineUserCnt
        mRoomData.applyUserCnt = applyUserCnt
        mRoomData.updateSeats(seats as ArrayList<PartySeatInfoModel>)
        mRoomData.updateUsers(users as ArrayList<PartyPlayerInfoModel>)
        if (thisRound.roundSeq == mRoomData.realRoundSeq) {
            mRoomData.realRoundInfo?.tryUpdateRoundInfoModel(thisRound, true)
        } else if (thisRound.roundSeq > mRoomData.realRoundSeq) {
            MyLog.w(TAG, "sync 回来的轮次大，要替换 roundInfo 了")
            // 主轮次结束
            launch {
                mRoomData.expectRoundInfo = thisRound
                mRoomData.checkRoundInEachMode()
            }
        }
    }

//    @Subscribe
//    fun onEvent(event: QChangeRoomNameEvent) {
//        MyLog.d(TAG, "onEvent QChangeRoomNameEvent !!改变房间名 $event")
//        if (mRoomData.gameId == event.info.roomID) {
//            pretendRoomNameSystemMsg(event.newName, CommentSysModel.TYPE_MODIFY_ROOM_NAME)
//        }
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ActivityUtils.ForeOrBackgroundChange) {
        MyLog.w(TAG, if (event.foreground) "切换到前台" else "切换到后台")
//        if (event.foreground) {
//            muteAllRemoteAudioStreams(mRoomData.isMute, false)
//        } else {
//            muteAllRemoteAudioStreams(true, false)
//        }
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: RUnlockMsg) {
//        ensureInRcRoom()
//        MyLog.w(TAG, "event=$event")
//        for (us in event.userLockInfoList) {
//            if (us.userID == MyUserInfoManager.uid.toInt()) {
//                mRoomData.unLockMe = !us.hasLock
//            } else if (us.userID == mRoomData.peerUser?.userID) {
//                mRoomData.unLockPeer = !us.hasLock
//            }
//        }
//    }
//
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: RGameOverMsg) {
//        ensureInRcRoom()
//        roomView.gameOver()
//    }


//    /**
//     * 录制小游戏事件，防止录进去背景音
//     *
//     * @param event
//     */
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: MuteAllVoiceEvent) {
//        MyLog.d(TAG, "onEvent event=$event")
//        if (event.begin) {
//            muteAllRemoteAudioStreams(true, false)
//        } else {
//            muteAllRemoteAudioStreams(mRoomData.isMute, false)
//        }
//    }


    /*打分相关*/

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: MachineScoreEvent) {
//        //收到其他人的机器打分消息，比较复杂，暂时简单点，轮次正确就直接展示
//        if (mRoomData?.realRoundInfo?.singByUserId(event.userId) == true) {
//            roomView.receiveScoreEvent(event.score)
//        }
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: LyricAndAccMatchManager.ScoreResultEvent) {
//        val line = event.line
//        val acrScore = event.acrScore
//        val melpScore = event.melpScore
//        val from = event.from
//        if (acrScore > melpScore) {
//            processScore(acrScore, line)
//        } else {
//            processScore(melpScore, line)
//        }
//    }
//
//    private fun processScore(score: Int, line: Int) {
//        if (score < 0) {
//            return
//        }
//        MyLog.d(TAG, "onEvent 得分=$score")
//        val machineScoreItem = MachineScoreItem()
//        machineScoreItem.score = score
//        // 这有时是个耗时操作
//        //        long ts = ZqEngineKit.getInstance().getAudioMixingCurrentPosition();
//        val ts: Long = -1
//        machineScoreItem.ts = ts
//        machineScoreItem.no = line
//        // 打分信息传输给其他人
//        sendScoreToOthers(machineScoreItem)
//        roomView.receiveScoreEvent(score)
//        //打分传给服务器
//        val now = mRoomData.realRoundInfo
//        if (now != null) {
//            /**
//             * pk 与 普通 都发送
//             */
//            if (now.isPKRound || now.isNormalRound) {
//                sendScoreToServer(score, line)
//            }
//        }
//    }
//
//    /**
//     * 将自己的分数传给其他人
//     *
//     * @param machineScoreItem
//     */
//    private fun sendScoreToOthers(machineScoreItem: MachineScoreItem) {
//        // 后续加个优化，如果房间里两人都是机器人就不加了
//        val msgService = ModuleServiceManager.getInstance().msgService
//        if (msgService != null) {
//            val ts = System.currentTimeMillis()
//            val senderInfo = UserInfo.Builder()
//                    .setUserID(MyUserInfoManager.uid.toInt())
//                    .setNickName(MyUserInfoManager.nickName)
//                    .setAvatar(MyUserInfoManager.avatar)
//                    .setSex(ESex.fromValue(MyUserInfoManager.sex))
//                    .setDescription("")
//                    .setIsSystem(false)
//                    .build()
//
//            val now = mRoomData.realRoundInfo
//            if (now != null && now.music != null) {
//                val roomMsg = RoomMsg.Builder()
//                        .setTimeMs(ts)
//                        .setMsgType(ERoomMsgType.RM_ROUND_MACHINE_SCORE)
//                        .setRoomID(mRoomData.gameId)
//                        .setNo(ts)
//                        .setPosType(EMsgPosType.EPT_UNKNOWN)
//                        .setSender(senderInfo)
//                        .setMachineScore(MachineScore.Builder()
//                                .setUserID(MyUserInfoManager.uid.toInt())
//                                .setNo(machineScoreItem.no)
//                                .setScore(machineScoreItem.score)
//                                .setItemID(now?.music?.itemID)
////                                .setLineNum(mRoomData.songLineNum)
//                                .build()
//                        )
//                        .build()
//                val contnet = U.getBase64Utils().encode(roomMsg.toByteArray())
//                msgService.sendChatRoomMessage(mRoomData.gameId.toString(), CustomMsgType.MSG_TYPE_ROOM, contnet, null)
//            }
//        }
//    }
//
//
//    /**
//     * 单句打分上报,只在pk模式上报
//     *
//     * @param score
//     * @param line
//     */
//    private fun sendScoreToServer(score: Int, line: Int) {
//        val map = HashMap<String, Any>()
//        val infoModel = mRoomData.realRoundInfo ?: return
//        map["userID"] = MyUserInfoManager.uid
//
//        var itemID = 0
//        if (infoModel.music != null) {
//            itemID = infoModel?.music?.itemID ?: 0
//            if (infoModel.status == EMRoundStatus.MRS_SPK_SECOND_PEER_SING.value) {
//                val pkSong = infoModel?.music?.pkMusic
//                if (pkSong != null) {
//                    itemID = pkSong.itemID
//                }
//            }
//        }
//
//        map["itemID"] = itemID
//        map["score"] = score
//        map["no"] = line
//        map["gameID"] = mRoomData.gameId
//        map["mainLevel"] = 0
//        map["singSecond"] = 0
//        val roundSeq = infoModel.roundSeq
//        map["roundSeq"] = roundSeq
//        val nowTs = System.currentTimeMillis()
//        map["timeMs"] = nowTs
//
//
//        val sb = StringBuilder()
//        sb.append("skrer")
//                .append("|").append(MyUserInfoManager.uid)
//                .append("|").append(itemID)
//                .append("|").append(score)
//                .append("|").append(line)
//                .append("|").append(mRoomData.gameId)
//                .append("|").append(0)
//                .append("|").append(0)
//                .append("|").append(roundSeq)
//                .append("|").append(nowTs)
//        map["sign"] = U.getMD5Utils().MD5_32(sb.toString())
//        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
//        launch {
//            var result = subscribe {
//                mRoomServerApi.sendPkPerSegmentResult(body)
//            }
//            if (result.errno == 0) {
//                // TODO: 2018/12/13  当前postman返回的为空 待补充
//                MyLog.w(TAG, "单句打分上报成功")
//            } else {
//                MyLog.w(TAG, "单句打分上报失败" + result.errno)
//            }
//        }
//    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(giftPresentEvent: GiftPresentEvent) {
        MyLog.d(TAG, "onEvent giftPresentEvent=$giftPresentEvent")
        EventBus.getDefault().post(GiftBrushMsgEvent(giftPresentEvent.mGPrensentGiftMsgModel))

        if (giftPresentEvent.mGPrensentGiftMsgModel.propertyModelList != null) {
            for (property in giftPresentEvent.mGPrensentGiftMsgModel.propertyModelList) {
                if (property.userID.toLong() == MyUserInfoManager.uid) {
                    if (property.coinBalance != -1f) {
                        UpdateCoinEvent.sendEvent(property.coinBalance.toInt(), property.lastChangeMs)
                    }
                    if (property.hongZuanBalance != -1f) {
                        mRoomData.setHzCount(property.hongZuanBalance, property.lastChangeMs)
                    }
                }
                if (property.curRoundSeqMeiliTotal > 0) {
                    // 他人的只关心魅力值的变化
                    EventBus.getDefault().post(UpdateMeiliEvent(property.userID, property.curRoundSeqMeiliTotal.toInt(), property.lastChangeMs))
                }
            }
        }

        if (giftPresentEvent.mGPrensentGiftMsgModel.receiveUserInfo.userId.toLong() == MyUserInfoManager.uid) {
            if (giftPresentEvent.mGPrensentGiftMsgModel.giftInfo.price <= 0) {
                StatisticsAdapter.recordCountEvent("party", "game_getflower", null)
            } else {
                StatisticsAdapter.recordCountEvent("party", "game_getgift", null)
            }
        }
    }

//    fun sendUnlock() {
//        MyLog.w(TAG, "解锁爱心")
//        val map = HashMap<String, Any>()
//        map["roomID"] = mRoomData.gameId
//
//        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
//        launch {
//            var result = subscribe { mRoomServerApi.sendUnlock(body) }
//            if (result.errno == 0) {
//                val ja = result.data.getJSONArray("userLockInfo")
//
//                for (i in 0 until ja.size) {
//                    var userID = ja.getJSONObject(i).getIntValue("userID")
//                    var hasLock = ja.getJSONObject(i).getBooleanValue("hasLock")
//                    if (userID == MyUserInfoManager.uid.toInt()) {
//                        mRoomData.unLockMe = !hasLock
//                    } else if (userID == mRoomData.peerUser?.userID) {
//                        mRoomData.unLockPeer = !hasLock
//                    }
//                }
//            }
//        }
//    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: MCancelMusic) {
//        MyLog.d(TAG, "onEvent MCancelMusic=$event")
//        pretendSystemMsg(event.cancelMusicMsg)
//    }


}