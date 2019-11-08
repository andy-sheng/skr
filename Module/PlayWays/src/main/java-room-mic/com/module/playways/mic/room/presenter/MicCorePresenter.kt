package com.module.playways.mic.room.presenter

import android.os.Handler
import android.os.Message
import com.alibaba.fastjson.JSON
import com.common.core.account.UserAccountManager
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.jiguang.JiGuangPush
import com.common.log.DebugLogView
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.statistics.StatisticsAdapter
import com.common.utils.ActivityUtils
import com.common.utils.SpanUtils
import com.common.utils.U
import com.component.busilib.constans.GameModeType
import com.component.lyrics.LyricAndAccMatchManager
import com.component.lyrics.utils.SongResUtils
import com.engine.EngineEvent
import com.engine.Params
import com.engine.arccloud.RecognizeConfig
import com.module.ModuleServiceManager
import com.module.common.ICallback
import com.module.msg.CustomMsgType
import com.module.playways.BuildConfig
import com.module.playways.RoomDataUtils
import com.module.playways.mic.match.model.JoinMicRoomRspModel
import com.module.playways.mic.room.MicRoomActivity
import com.module.playways.mic.room.MicRoomData
import com.module.playways.mic.room.MicRoomServerApi
import com.module.playways.mic.room.event.MicChangeRoomEvent
import com.module.playways.mic.room.event.MicRoundChangeEvent
import com.module.playways.mic.room.event.MicRoundStatusChangeEvent
import com.module.playways.mic.room.model.MicPlayerInfoModel
import com.module.playways.mic.room.model.MicRoundInfoModel
import com.module.playways.mic.room.ui.IMicRoomView
import com.module.playways.room.gift.event.GiftBrushMsgEvent
import com.module.playways.room.gift.event.UpdateCoinEvent
import com.module.playways.room.gift.event.UpdateMeiliEvent
import com.module.playways.room.msg.event.GiftPresentEvent
import com.module.playways.room.msg.event.MachineScoreEvent
import com.module.playways.room.msg.event.QChangeRoomNameEvent
import com.module.playways.room.msg.filter.PushMsgFilter
import com.module.playways.room.msg.manager.MicRoomMsgManager
import com.module.playways.room.room.comment.model.CommentModel
import com.module.playways.room.room.comment.model.CommentSysModel
import com.module.playways.room.room.comment.model.CommentTextModel
import com.module.playways.room.room.event.PretendCommentMsgEvent
import com.module.playways.room.room.score.MachineScoreItem
import com.module.playways.room.song.model.SongModel
import com.module.playways.songmanager.event.MuteAllVoiceEvent
import com.module.playways.songmanager.event.RoomNameChangeEvent
import com.orhanobut.dialogplus.DialogPlus
import com.zq.live.proto.Common.ESex
import com.zq.live.proto.Common.UserInfo
import com.zq.live.proto.GrabRoom.EMsgPosType
import com.zq.live.proto.GrabRoom.ERoomMsgType
import com.zq.live.proto.GrabRoom.MachineScore
import com.zq.live.proto.GrabRoom.RoomMsg
import com.zq.live.proto.MicRoom.*
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
import java.util.*

class MicCorePresenter(var mRoomData: MicRoomData, var roomView: IMicRoomView) : RxLifeCyclePresenter() {

    internal var mFirstKickOutTime: Long = -1 //用时间和次数来判断一个人有没有在一个房间里

    internal var mAbsenTimes = 0

    internal var mRoomServerApi = ApiManager.getInstance().createService(MicRoomServerApi::class.java)

    internal var mDestroyed = false

    internal var mDialogPlus: DialogPlus? = null

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
                MSG_ENSURE_SWITCH_BROADCAST_SUCCESS -> onChangeBroadcastSuccess()
            }
        }
    }

    internal var mPushMsgFilter: PushMsgFilter<*> = PushMsgFilter<MicRoomMsg> { msg ->
        msg != null && msg.roomID == mRoomData.gameId
    }

    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        MicRoomMsgManager.addFilter(mPushMsgFilter)
        joinRoomAndInit(true)
        U.getFileUtils().deleteAllFiles(U.getAppInfoUtils().getSubDirPath("grab_save"))
        startSyncGameStatus()
    }

    /**
     * 加入引擎房间
     * 加入融云房间
     * 系统消息弹幕
     */
    private fun joinRoomAndInit(first: Boolean) {
        MyLog.w(TAG, "joinRoomAndInit" + " first=" + first + ", gameId is " + mRoomData.gameId)
        mFirstKickOutTime = -1
        mAbsenTimes = 0

        if (mRoomData.gameId > 0) {
            var reInit = false
            if (first) {
                reInit = true
            }
            if (reInit) {
                val params = Params.getFromPref()
                //            params.setStyleEnum(Params.AudioEffect.none);
                params.scene = Params.Scene.grab
                params.isEnableAudio = true
                ZqEngineKit.getInstance().init("microom", params)
            }
            ZqEngineKit.getInstance().joinRoom(mRoomData.gameId.toString(), UserAccountManager.uuidAsLong.toInt(), false, mRoomData.agoraToken)
            // 不发送本地音频, 会造成第一次抢没声音
            ZqEngineKit.getInstance().muteLocalAudioStream(true)
        }
        joinRcRoom(-1)
        if (mRoomData.gameId > 0) {
            for (playerInfoModel in mRoomData.getPlayerAndWaiterInfoList()) {
                if (!playerInfoModel.isOnline) {
                    continue
                }
                pretendEnterRoom(playerInfoModel)
            }
            pretendRoomNameSystemMsg(mRoomData.roomName, CommentSysModel.TYPE_MIC_ENTER_ROOM)
        }
        startHeartbeat()
        startSyncGameStatus()
    }

    fun changeMatchState(isChecked: Boolean) {
        launch {
            val map = mutableMapOf(
                    "roomID" to mRoomData?.gameId,
                    "matchStatus" to (if (isChecked) 2 else 1)
            )

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe(RequestControl("$TAG changeMatchState", ControlType.CancelLast)) {
                mRoomServerApi.changeMatchStatus(body)
            }

            if (result.errno == 0) {
                if (isChecked) {
//                    val commentSysModel = CommentSysModel(GameModeType.GAME_MODE_RACE, "房主已将房间设置为 不允许用户匹配进入")
//                    EventBus.getDefault().post(PretendCommentMsgEvent(commentSysModel))
                } else {
//                    val commentSysModel = CommentSysModel(GameModeType.GAME_MODE_RACE, "房主已将房间设置为 允许用户匹配进入")
//                    EventBus.getDefault().post(PretendCommentMsgEvent(commentSysModel))
                }
            } else {
                U.getToastUtil().showShort(result.errmsg)
            }
        }
    }

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
        val commentSysModel = CommentSysModel(roomName, type)
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
    internal fun preOpWhenSelfRound() {
        var needAcc = mRoomData?.realRoundInfo?.isAccRound == true

        val p = ZqEngineKit.getInstance().params
        p.isGrabSingNoAcc = !needAcc

        if (!ZqEngineKit.getInstance().params.isAnchor) {
            ZqEngineKit.getInstance().setClientRole(true)
            ZqEngineKit.getInstance().muteLocalAudioStream(false)
            if (needAcc) {
                // 如果需要播放伴奏，一定要在角色切换成功才能播
                mUiHandler.removeMessages(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS)
                mUiHandler.sendEmptyMessageDelayed(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS, 2000)
            }
        } else {
            // 如果是房主,不在这里 解禁，会录进去音效的声音 延后一些再解开
            mUiHandler.postDelayed({
                ZqEngineKit.getInstance().muteLocalAudioStream(false)
                onChangeBroadcastSuccess()
            }, 500)
        }
    }

    fun preOpWhenOtherRound() {
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

    /**
     * 自己的轮次结束了
     *
     * @param roundInfoModel
     */
    private fun onSelfRoundOver(roundInfoModel: MicRoundInfoModel) {
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
        ZqEngineKit.getInstance().destroy("microom")
        mUiHandler.removeCallbacksAndMessages(null)
        MicRoomMsgManager.removeFilter(mPushMsgFilter)
        ModuleServiceManager.getInstance().msgService.leaveChatRoom(mRoomData.gameId.toString())
        JiGuangPush.exitSkrRoomId(mRoomData.gameId.toString())
        MyLog.d(TAG, "destroy over")
    }

    /**
     * 上报轮次结束信息
     */
    fun sendRoundOverInfo() {
        MyLog.w(TAG, "上报我的演唱结束")
        val roundInfoModel = mRoomData.realRoundInfo
        if (roundInfoModel == null || !roundInfoModel.singBySelf()) {
            return
        }
        val map = HashMap<String, Any>()
        map["roomID"] = mRoomData.gameId
        map["roundSeq"] = roundInfoModel.roundSeq
        if (roundInfoModel.status == EMRoundStatus.MRS_SPK_FIRST_PEER_SING.value) {
            map["subRoundSeq"] = 0
        } else if (roundInfoModel.status == EMRoundStatus.MRS_SPK_SECOND_PEER_SING.value) {
            map["subRoundSeq"] = 1
        }

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        launch {
            var result = subscribe { mRoomServerApi.sendRoundOver(body) }
            if (result.errno == 0) {
                MyLog.w(TAG, "演唱结束上报成功 traceid is " + result.traceId)
            } else {
                MyLog.w(TAG, "演唱结束上报失败 traceid is " + result.traceId)
            }
        }

    }


    /**
     * 放弃演唱接口
     */
    fun giveUpSing(okCallback: (() -> Unit)?) {
        MyLog.w(TAG, "我放弃演唱")
        val now = mRoomData.realRoundInfo
        if (now == null || !now.singBySelf()) {
            return
        }
        val map = HashMap<String, Any?>()
        map["roomID"] = mRoomData.gameId
        map["roundSeq"] = now.roundSeq
        if (now.music != null) {
            map["playType"] = now?.music?.playType
        }
        if (now.status == EMRoundStatus.MRS_SPK_FIRST_PEER_SING.value) {
            map["subRoundSeq"] = 0
        } else if (now.status == EMRoundStatus.MRS_SPK_SECOND_PEER_SING.value) {
            map["subRoundSeq"] = 1
        }
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        launch {
            var result = subscribe(RequestControl("giveUpSing", ControlType.CancelThis)) {
                mRoomServerApi.giveUpSing(body)
            }
            if (result.errno == 0) {
//                        roomView.giveUpSuccess(now.roundSeq)
                closeEngine()
                okCallback?.invoke()
                MyLog.w(TAG, "放弃演唱上报成功 traceid is " + result.traceId)
            } else {
                MyLog.w(TAG, "放弃演唱上报失败 traceid is " + result.traceId)
            }
        }
    }

    /**
     * 请求踢人
     *
     * @param userId 被踢人id
     */
    fun reqKickUser(userId: Int) {
        val roundInfoModel = mRoomData.realRoundInfo ?: return
        val map = HashMap<String, Any>()
        map["kickoutUserID"] = userId
        map["roomID"] = mRoomData.gameId

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        launch {
            var result = subscribe(RequestControl("reqKickUser", ControlType.CancelThis)) {
                mRoomServerApi.reqKickUser(body)
            }
            if (result.errno == 0) {
                if (mRoomData.isOwner) {
                    val kickTimes = result.data!!.getIntValue("resKickUserTimes")
                    // TODO: 2019/5/8 更新剩余次数
                    mRoomData.ownerKickTimes = kickTimes
                    if (kickTimes > 0) {
                        U.getToastUtil().showShort("踢人成功")
                    } else {
                        U.getToastUtil().showShort("发起踢人请求成功")
                    }
                } else {
                    U.getToastUtil().showShort("发起踢人请求成功")
                }
                val coin = result.data!!.getIntValue("coin")
                mRoomData.setCoin(coin)
            } else {
                U.getToastUtil().showShort("" + result.errmsg)
            }
        }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MicChangeRoomEvent) {
        roomView.ensureActivtyTop()
        onChangeRoomSuccess(event.mJoinGrabRoomRspModel)
    }

    private fun onChangeRoomSuccess(joinGrabRoomRspModel: JoinMicRoomRspModel?) {
        MyLog.d(TAG, "onChangeRoomSuccess joinGrabRoomRspModel=$joinGrabRoomRspModel")
        if (joinGrabRoomRspModel != null) {
//            EventBus.getDefault().post(GrabSwitchRoomEvent())
            mRoomData.loadFromRsp(joinGrabRoomRspModel)
            joinRoomAndInit(false)
            mRoomData.checkRoundInEachMode()
            roomView.dismissKickDialog()
            roomView.invitedToOtherRoom()
        }
    }

    var heartbeatJob: Job? = null

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = launch {
            while (true) {
                val map = mutableMapOf(
                        "roomID" to mRoomData.gameId,
                        "userID" to MyUserInfoManager.uid
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
                    val gameOverTimeMs = result.data.getLong("gameOverTimeMs")
                    if (gameOverTimeMs > 0) {
                        mRoomData.gameOverTs = gameOverTimeMs
                        DebugLogView.println(TAG, "gameOverTimeMs=${gameOverTimeMs} 游戏结束时间>0 ，游戏结束，退出房间")
                        // 游戏结束了，停服了
                        mRoomData.expectRoundInfo = null
                        mRoomData.checkRoundInEachMode()
                    } else {
                        val syncStatusTimeMs = result.data.getLong("syncStatusTimeMs")
                        if (syncStatusTimeMs > mRoomData.lastSyncTs) {
                            mRoomData.lastSyncTs = syncStatusTimeMs
                            val roundInfo = JSON.parseObject(result.data.getString("currentRound"), MicRoundInfoModel::class.java)
                            processSyncResult(roundInfo)
                        }
                    }
                } else {

                }
            }
        }
    }


    private fun processSyncResult(roundInfo: MicRoundInfoModel) {
        if (roundInfo.roundSeq == mRoomData.realRoundSeq) {
            mRoomData.realRoundInfo?.tryUpdateRoundInfoModel(roundInfo, true)
        } else if (roundInfo.roundSeq > mRoomData.realRoundSeq) {
            MyLog.w(TAG, "sync 回来的轮次大，要替换 roundInfo 了")
            // 主轮次结束
            launch {
                mRoomData.expectRoundInfo = roundInfo
                mRoomData.checkRoundInEachMode()
            }
        }
    }

    /**
     * 为了方便服务器亲密度结算
     */
    private fun userStatisticForIntimacy(){
        launch {
            while(true) {
                delay(10*60*1000)
                val l1 = java.util.ArrayList<Int>()
                for (m in mRoomData.getPlayerAndWaiterInfoList()) {
                    if(m.userID!=MyUserInfoManager.uid.toInt()){
                        if(mRoomData.preUserIDsSnapShots.contains(m.userID)){
                            l1.add(m.userID)
                        }
                    }
                }
                if (l1.isNotEmpty()) {
                    val map = java.util.HashMap<String, Any>()
                    map["gameID"] = mRoomData.gameId
                    map["mode"] = GameModeType.GAME_MODE_GRAB
                    val ts = System.currentTimeMillis()
                    map["timeMs"] = ts
                    map["sign"] = U.getMD5Utils().MD5_32("skrer|" + MyUserInfoManager.uid + "|" + ts)
                    map["userID"] = MyUserInfoManager.uid
                    map["preUserIDs"] = l1
                    val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                    val result = subscribe { mRoomServerApi.userStatistic(body) }
                    if (result.errno == 0) {

                    }
                }else{
                    break
                }
            }
        }
    }


    /**
     * 轮次切换事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MicRoundChangeEvent) {
        MyLog.d(TAG, "MicRoundChangeEvent = $event")
        processStatusChange(1, event.lastRound, event.newRound)
    }

    /**
     * 轮次内状态更新
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MicRoundStatusChangeEvent) {
        MyLog.d(TAG, "GrabRoundStatusChangeEvent event=$event")
        processStatusChange(2, null, event.roundInfo)
    }

    private fun processStatusChange(from: Int, lastRound: MicRoundInfoModel?, thisRound: MicRoundInfoModel?) {
        DebugLogView.println(TAG, "processStatusChange from=$from roundSeq=${thisRound?.roundSeq} statusNow=${thisRound?.status}")
        // 轮次变化尝试更新头像
        mUiHandler.removeMessages(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS)
        closeEngine()
        ZqEngineKit.getInstance().stopRecognize()
        if (thisRound == null) {
            // 游戏结束了
            roomView.gameOver()
            return
        }
        if (thisRound.status == EMRoundStatus.MRS_INTRO.value) {
            // 等待阶段
            roomView.showRoundOver(lastRound) {
                roomView.showWaiting()
            }
        } else if (thisRound.isSingStatus) {
            roomView.showRoundOver(lastRound) {
                // 演唱阶段
                if (thisRound.singBySelf()) {
                    val size = U.getActivityUtils().activityList.size
                    var needTips = false
                    for (i in size - 1 downTo 0) {
                        val activity = U.getActivityUtils().activityList[i]
                        if (activity is MicRoomActivity) {
                            break
                        } else {
                            activity.finish()
                            needTips = true
                        }
                    }
                    if (needTips) {
                        U.getToastUtil().showLong("你的演唱开始了")
                    }
                    roomView.singBySelf(lastRound) {
                        preOpWhenSelfRound()
                    }
                } else {
                    preOpWhenOtherRound()
                    roomView.singByOthers(lastRound)
                }
            }
        } else if (thisRound.status == EMRoundStatus.MRS_END.value) {

        }
    }

    private fun closeEngine() {
        if (mRoomData.gameId > 0) {
            ZqEngineKit.getInstance().stopAudioMixing()
            ZqEngineKit.getInstance().stopAudioRecording()
            if (ZqEngineKit.getInstance().params.isAnchor) {
                ZqEngineKit.getInstance().setClientRole(false)
            }
        }
    }

    /**
     * 引擎相关事件
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: EngineEvent) {
        if (event.getType() == EngineEvent.TYPE_USER_ROLE_CHANGE) {
            val roleChangeInfo = event.getObj<EngineEvent.RoleChangeInfo>()
            if (roleChangeInfo.newRole == 1) {
                val roundInfoModel = mRoomData.realRoundInfo
                if (roundInfoModel != null && roundInfoModel.singBySelf()) {
                    MyLog.d(TAG, "演唱环节切换主播成功")
                    onChangeBroadcastSuccess()
                }
            }
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

    /**
     * 成功切换为主播
     */
    private fun onChangeBroadcastSuccess() {
        MyLog.d(TAG, "onChangeBroadcastSuccess 我的演唱环节")
        mUiHandler.removeMessages(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS)
        mUiHandler.post(Runnable {
            val infoModel = mRoomData.realRoundInfo
            if (infoModel == null || !infoModel.singBySelf()) {
                MyLog.d(TAG, "onChangeBroadcastSuccess,但已经不是你的轮次了，cancel")
                return@Runnable
            }
            var songModel: SongModel? = infoModel.music ?: return@Runnable
            if (infoModel.status == EMRoundStatus.MRS_SPK_SECOND_PEER_SING.value) {
                songModel = songModel!!.pkMusic
            }
            if (songModel == null) {
                return@Runnable
            }
            // 开始开始混伴奏，开始解除引擎mute
            val accFile = SongResUtils.getAccFileByUrl(songModel.acc)
            // midi不需要在这下，只要下好，native就会解析，打分就能恢复
            val midiFile = SongResUtils.getMIDIFileByUrl(songModel.midi)
            MyLog.d(TAG, "onChangeBroadcastSuccess 我的演唱环节 info=${songModel.toSimpleString()} acc=${songModel.acc} midi=${songModel.midi} accRound=${mRoomData?.realRoundInfo?.isAccRound} mRoomData.isAccEnable=${mRoomData.isAccEnable}")
            val needAcc = mRoomData?.realRoundInfo?.isAccRound == true && songModel.acc.isNotEmpty()
            if (needAcc) {
                // 下载midi
                if (midiFile != null && !midiFile.exists()) {
                    MyLog.d(TAG, "onChangeBroadcastSuccess 下载midi文件 url=${songModel.midi} => local=${midiFile.path}")
                    U.getHttpUtils().downloadFileAsync(songModel.midi, midiFile, true, null)
                }

                //  播放伴奏
                val songBeginTs = songModel.beginMs
                if (accFile != null && accFile.exists()) {
                    // 伴奏文件存在
                    ZqEngineKit.getInstance().startAudioMixing(MyUserInfoManager.uid.toInt(), accFile.absolutePath, midiFile.absolutePath, songBeginTs.toLong(), false, false, 1)
                } else {
                    ZqEngineKit.getInstance().startAudioMixing(MyUserInfoManager.uid.toInt(), songModel.acc, midiFile.absolutePath, songBeginTs.toLong(), false, false, 1)
                }
            }
            // 启动acr打分识别
            if (needAcc) {
                //有伴奏模式，手动开启acc
                ZqEngineKit.getInstance().startRecognize(RecognizeConfig.newBuilder()
                        .setSongName(songModel.itemName)
                        .setArtist(songModel.owner)
                        .setMode(RecognizeConfig.MODE_MANUAL)
                        .build())
            } else {
                // 清唱还需要打分，那就只用 acr 打分
                ZqEngineKit.getInstance().startRecognize(RecognizeConfig.newBuilder()
                        .setSongName(songModel.itemName)
                        .setArtist(songModel.owner)
                        .setMode(RecognizeConfig.MODE_AUTO)
                        .setAutoTimes(4)
                        .setMResultListener { result, list, targetSongInfo, lineNo ->
                            var mAcrScore = 0
                            if (targetSongInfo != null) {
                                mAcrScore = (targetSongInfo.score * 100).toInt()
                            }
                            EventBus.getDefault().post(LyricAndAccMatchManager.ScoreResultEvent("onChangeBroadcastSuccess", -1, mAcrScore, 0))
                        }
                        .build())
            }

        })
    }


    /**
     * 有人加入房间
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MJoinNoticeMsg) {
        val playerInfoModel = MicPlayerInfoModel()
        playerInfoModel.userInfo = UserInfoModel.parseFromPB(event.userInfo)
        playerInfoModel.role = event.role.value

        mRoomData.realRoundInfo?.addUser(true, playerInfoModel)
        roomView.joinNotice(playerInfoModel)
    }

    /**
     * 某人退出游戏
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MExitGameMsg) {
        mRoomData.realRoundInfo?.removeUser(true, event.userID)
        val grabRoundInfoModel = mRoomData.realRoundInfo
        if (grabRoundInfoModel != null) {
            for (chorusRoundInfoModel in grabRoundInfoModel.chorusRoundInfoModels) {
                if (chorusRoundInfoModel.userID == event.userID) {
                    chorusRoundInfoModel.userExit()
                    pretendGiveUp(mRoomData.getPlayerOrWaiterInfo(event.userID))
                }
            }
        }
    }

    /**
     * 房主改变
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MChangeRoomOwnerMsg) {
        MyLog.d(TAG, "onEvent event = $event")
        mRoomData.ownerId = event.userID
    }

    /**
     * 合唱某人放弃了演唱
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MCHOGiveUpMsg) {
        MyLog.d(TAG, "QChoGiveUpEvent event=$event")
        val now = mRoomData.realRoundInfo
        if (now != null) {
            if (now.roundSeq == event.roundSeq) {
                now.giveUpInChorus(event.userID)
                val list = now.chorusRoundInfoModels
                if (list != null) {
                    for (chorusRoundInfoModel in list) {
                        if (chorusRoundInfoModel.userID == event.userID) {
                            val userInfoModel = mRoomData.getPlayerOrWaiterInfo(event.userID)
                            if (event.userID.toLong() == MyUserInfoManager.uid) {
                                // 是我自己不唱了
                                U.getToastUtil().showShort("你已经退出合唱")
                            } else if (now.singBySelf()) {
                                // 是我的对手不唱了
                                if (userInfoModel != null) {
                                    U.getToastUtil().showShort(userInfoModel.nicknameRemark + "已经退出合唱")
                                }
                            } else {
                                // 观众视角，有人不唱了
                                if (userInfoModel != null) {
                                    U.getToastUtil().showShort(userInfoModel.nicknameRemark + "已经退出合唱")
                                }
                            }
                            pretendGiveUp(userInfoModel)
                            break
                        }
                    }
                }
            }
        }
    }

    /**
     * 轮次结束
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MSPKInnerRoundOverMsg) {
        MyLog.d(TAG, "QPkInnerRoundOverEvent event=$event")
        var roundInfo = MicRoundInfoModel.parseFromRoundInfo(event.currentRound)
        if (RoomDataUtils.isCurrentRunningRound(roundInfo.roundSeq, mRoomData)) {
            val now = mRoomData.realRoundInfo
            if (now != null) {
                now.tryUpdateRoundInfoModel(roundInfo, true)
                //                // PK 第一个人不唱了 加个弹幕
                if (now.getsPkRoundInfoModels().size > 0) {
                    if (now.getsPkRoundInfoModels()[0].overReason == EMRoundOverReason.MROR_SELF_GIVE_UP.value) {
                        val userInfoModel = mRoomData.getPlayerOrWaiterInfo(now.getsPkRoundInfoModels()[0].userID)
                        pretendGiveUp(userInfoModel)
                    }
                }
                if (now.getsPkRoundInfoModels().size > 1) {
                    if (now.getsPkRoundInfoModels()[1].overReason == EMRoundOverReason.MROR_SELF_GIVE_UP.value) {
                        val userInfoModel = mRoomData.getPlayerOrWaiterInfo(now.getsPkRoundInfoModels()[1].userID)
                        pretendGiveUp(userInfoModel)
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MAddMusicMsg) {
        MyLog.d(TAG, "MAddMusicMsg event=$event")
        roomView.showSongCount(event.musicCnt)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MDelMusicMsg) {
        MyLog.d(TAG, "MAddMusicMsg event=$event")
        roomView.showSongCount(event.musicCnt)
    }

    private fun pretendGiveUp(userInfoModel: UserInfoModel?) {
        if (userInfoModel != null) {
            val commentModel = CommentTextModel()
            commentModel.userInfo = userInfoModel
            commentModel.avatarColor = CommentModel.AVATAR_COLOR
            val nameBuilder = SpanUtils()
                    .append(userInfoModel.nicknameRemark + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                    .create()
            commentModel.nameBuilder = nameBuilder

            val stringBuilder = SpanUtils()
                    .append("不唱了").setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
                    .create()
            commentModel.stringBuilder = stringBuilder
            EventBus.getDefault().post(PretendCommentMsgEvent(commentModel))
        }
    }

    private fun pretendEnterRoom(playerInfoModel: MicPlayerInfoModel) {
        val commentModel = CommentTextModel()
        commentModel.userInfo = playerInfoModel.userInfo
        commentModel.avatarColor = CommentModel.AVATAR_COLOR
        val nameBuilder = SpanUtils()
                .append(playerInfoModel.userInfo.nicknameRemark + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                .create()
        commentModel.nameBuilder = nameBuilder

        val stringBuilder = when {
            playerInfoModel.userInfo.userId != UserAccountManager.SYSTEM_GRAB_ID -> {
                val spanUtils = SpanUtils()
                        .append("加入了房间").setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
                if (BuildConfig.DEBUG) {
                    spanUtils.append(" 角色为" + playerInfoModel.role)
                            .append(" 在线状态为" + playerInfoModel.isOnline)
                }
                spanUtils.create()
            }
            else -> SpanUtils()
                    .append("我是撕歌最傲娇小助手多音，来和你们一起唱歌卖萌~").setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
                    .create()
        }
        commentModel.stringBuilder = stringBuilder
        EventBus.getDefault().post(PretendCommentMsgEvent(commentModel))
    }

    /**
     * 轮次变化
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MRoundOverMsg) {
        MyLog.w(TAG, "收到服务器的某一个人轮次结束的push currentRound:${event.currentRound}")
        MyLog.w(TAG, "收到服务器的某一个人轮次结束的push nextRound:${event.nextRound}")
        ensureInRcRoom()
        roomView.showSongCount(event.musicCnt)
        var currentRound = MicRoundInfoModel.parseFromRoundInfo(event.currentRound)
        var nextRound = MicRoundInfoModel.parseFromRoundInfo(event.nextRound)
        if (currentRound.roundSeq == mRoomData.realRoundInfo?.roundSeq) {
            // 如果是当前轮次
            mRoomData.realRoundInfo!!.tryUpdateRoundInfoModel(currentRound, false)

            //非PK和合唱轮次 加上不唱了弹幕 产品又让加回来了
            val infoModel = mRoomData.realRoundInfo
            if (!infoModel!!.isPKRound && !infoModel.isChorusRound) {
                if (infoModel.overReason == EMRoundOverReason.MROR_SELF_GIVE_UP.value) {
                    pretendGiveUp(mRoomData.getPlayerOrWaiterInfo(infoModel.userID))
                }
            }
        }
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
    fun onEvent(event: MSyncStatusMsg) {
        ensureInRcRoom()
        MyLog.w(TAG, "收到服务器 sync push更新状态 ,event=$event")
        var thisRound = MicRoundInfoModel.parseFromRoundInfo(event.currentRound)
        // 延迟10秒sync ，一旦启动sync 间隔 5秒 sync 一次
        startSyncGameStatus()
        processSyncResult(thisRound)
    }

    @Subscribe
    fun onEvent(event: QChangeRoomNameEvent) {
        MyLog.d(TAG, "onEvent QChangeRoomNameEvent !!改变房间名 $event")
        if (mRoomData.gameId == event.info.roomID) {
            pretendRoomNameSystemMsg(event.newName, CommentSysModel.TYPE_MODIFY_ROOM_NAME)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ActivityUtils.ForeOrBackgroundChange) {
        MyLog.w(TAG, if (event.foreground) "切换到前台" else "切换到后台")
        if (event.foreground) {
            muteAllRemoteAudioStreams(mRoomData.isMute, false)
        } else {
            muteAllRemoteAudioStreams(true, false)
        }
    }

    /**
     * 录制小游戏事件，防止录进去背景音
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MuteAllVoiceEvent) {
        MyLog.d(TAG, "onEvent event=$event")
        if (event.begin) {
            muteAllRemoteAudioStreams(true, false)
        } else {
            muteAllRemoteAudioStreams(mRoomData.isMute, false)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RoomNameChangeEvent) {
        MyLog.w(TAG, "onEvent event=$event")
        mRoomData.roomName = event.mRoomName
    }


    /*打分相关*/

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MachineScoreEvent) {
        //收到其他人的机器打分消息，比较复杂，暂时简单点，轮次正确就直接展示
        if (mRoomData?.realRoundInfo?.singByUserId(event.userId) == true) {
            roomView.receiveScoreEvent(event.score)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: LyricAndAccMatchManager.ScoreResultEvent) {
        val line = event.line
        val acrScore = event.acrScore
        val melpScore = event.melpScore
        val from = event.from
        if (acrScore > melpScore) {
            processScore(acrScore, line)
        } else {
            processScore(melpScore, line)
        }
    }

    private fun processScore(score: Int, line: Int) {
        if (score < 0) {
            return
        }
        MyLog.d(TAG, "onEvent 得分=$score")
        val machineScoreItem = MachineScoreItem()
        machineScoreItem.score = score
        // 这有时是个耗时操作
        //        long ts = ZqEngineKit.getInstance().getAudioMixingCurrentPosition();
        val ts: Long = -1
        machineScoreItem.ts = ts
        machineScoreItem.no = line
        // 打分信息传输给其他人
        sendScoreToOthers(machineScoreItem)
        roomView.receiveScoreEvent(score)
        //打分传给服务器
        val now = mRoomData.realRoundInfo
        if (now != null) {
            /**
             * pk 与 普通 都发送
             */
            if (now.isPKRound || now.isNormalRound) {
                sendScoreToServer(score, line)
            }
        }
    }

    /**
     * 将自己的分数传给其他人
     *
     * @param machineScoreItem
     */
    private fun sendScoreToOthers(machineScoreItem: MachineScoreItem) {
        // 后续加个优化，如果房间里两人都是机器人就不加了
        val msgService = ModuleServiceManager.getInstance().msgService
        if (msgService != null) {
            val ts = System.currentTimeMillis()
            val senderInfo = UserInfo.Builder()
                    .setUserID(MyUserInfoManager.uid.toInt())
                    .setNickName(MyUserInfoManager.nickName)
                    .setAvatar(MyUserInfoManager.avatar)
                    .setSex(ESex.fromValue(MyUserInfoManager.sex))
                    .setDescription("")
                    .setIsSystem(false)
                    .build()

            val now = mRoomData.realRoundInfo
            if (now != null && now.music != null) {
                val roomMsg = RoomMsg.Builder()
                        .setTimeMs(ts)
                        .setMsgType(ERoomMsgType.RM_ROUND_MACHINE_SCORE)
                        .setRoomID(mRoomData.gameId)
                        .setNo(ts)
                        .setPosType(EMsgPosType.EPT_UNKNOWN)
                        .setSender(senderInfo)
                        .setMachineScore(MachineScore.Builder()
                                .setUserID(MyUserInfoManager.uid.toInt())
                                .setNo(machineScoreItem.no)
                                .setScore(machineScoreItem.score)
                                .setItemID(now?.music?.itemID)
//                                .setLineNum(mRoomData.songLineNum)
                                .build()
                        )
                        .build()
                val contnet = U.getBase64Utils().encode(roomMsg.toByteArray())
                msgService.sendChatRoomMessage(mRoomData.gameId.toString(), CustomMsgType.MSG_TYPE_ROOM, contnet, null)
            }
        }
    }


    /**
     * 单句打分上报,只在pk模式上报
     *
     * @param score
     * @param line
     */
    private fun sendScoreToServer(score: Int, line: Int) {
        val map = HashMap<String, Any>()
        val infoModel = mRoomData.realRoundInfo ?: return
        map["userID"] = MyUserInfoManager.uid

        var itemID = 0
        if (infoModel.music != null) {
            itemID = infoModel?.music?.itemID ?: 0
            if (infoModel.status == EMRoundStatus.MRS_SPK_SECOND_PEER_SING.value) {
                val pkSong = infoModel?.music?.pkMusic
                if (pkSong != null) {
                    itemID = pkSong.itemID
                }
            }
        }

        map["itemID"] = itemID
        map["score"] = score
        map["no"] = line
        map["gameID"] = mRoomData.gameId
        map["mainLevel"] = 0
        map["singSecond"] = 0
        val roundSeq = infoModel.roundSeq
        map["roundSeq"] = roundSeq
        val nowTs = System.currentTimeMillis()
        map["timeMs"] = nowTs


        val sb = StringBuilder()
        sb.append("skrer")
                .append("|").append(MyUserInfoManager.uid)
                .append("|").append(itemID)
                .append("|").append(score)
                .append("|").append(line)
                .append("|").append(mRoomData.gameId)
                .append("|").append(0)
                .append("|").append(0)
                .append("|").append(roundSeq)
                .append("|").append(nowTs)
        map["sign"] = U.getMD5Utils().MD5_32(sb.toString())
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        launch {
            var result = subscribe {
                mRoomServerApi.sendPkPerSegmentResult(body)
            }
            if (result.errno == 0) {
                // TODO: 2018/12/13  当前postman返回的为空 待补充
                MyLog.w(TAG, "单句打分上报成功")
            } else {
                MyLog.w(TAG, "单句打分上报失败" + result.errno)
            }
        }
    }

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
                StatisticsAdapter.recordCountEvent("mic", "game_getflower", null)
            } else {
                StatisticsAdapter.recordCountEvent("mic", "game_getgift", null)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MKickoutUserMsg) {
        MyLog.d(TAG, "onEvent qKickUserResultEvent=$event")
        // 踢人的结果
        if (event.kickUserID.toLong() == MyUserInfoManager.uid) {
            // 自己被踢出去
            roomView.kickBySomeOne(true)
        } else {
            // 别人被踢出去
            roomView.dismissKickDialog()
            pretendSystemMsg(event.kickResultContent)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MCancelMusic) {
        MyLog.d(TAG, "onEvent MCancelMusic=$event")
        pretendSystemMsg(event.cancelMusicMsg)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MMatchStatusMsg) {
        MyLog.d(TAG, "onEvent MCancelMusic=$event")
        if (event.matchStatus.value == ERoomMatchStatus.EMMS_CLOSED.value) {
            mRoomData.matchStatusOpen = false
            pretendSystemMsg("房主已将房间设置为 不允许用户匹配进入")
        } else if (event.matchStatus.value == ERoomMatchStatus.EMMS_OPEN.value) {
            mRoomData.matchStatusOpen = true
            pretendSystemMsg("房主已将房间设置为 允许用户匹配进入")
        }
    }


    companion object {

        internal val MSG_ENSURE_IN_RC_ROOM = 9// 确保在融云的聊天室，保证融云的长链接

        internal val MSG_ENSURE_SWITCH_BROADCAST_SUCCESS = 21 // 确保用户切换成主播成功，防止引擎不回调的保护


    }

}