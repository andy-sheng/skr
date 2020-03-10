package com.module.playways.battle.room.presenter

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
import com.common.rxretrofit.ControlType
import com.common.rxretrofit.RequestControl
import com.common.rxretrofit.subscribe
import com.common.statistics.StatisticsAdapter
import com.common.utils.ActivityUtils
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.videocache.MediaCacheManager
import com.component.lyrics.LyricAndAccMatchManager
import com.component.lyrics.utils.SongResUtils
import com.engine.EngineEvent
import com.engine.Params
import com.engine.arccloud.RecognizeConfig
import com.module.ModuleServiceManager
import com.module.common.ICallback
import com.module.msg.CustomMsgType
import com.module.playways.battle.room.BattleRoomData
import com.module.playways.battle.room.BattleRoomServerApi
import com.module.playways.battle.room.event.BattleRoundChangeEvent
import com.module.playways.battle.room.event.BattleRoundStatusChangeEvent
import com.module.playways.battle.room.model.BattlePlayerInfoModel
import com.module.playways.battle.room.model.BattleRoundInfoModel
import com.module.playways.battle.room.ui.IBattleRoomView
import com.module.playways.room.data.H
import com.module.playways.room.gift.event.GiftBrushMsgEvent
import com.module.playways.room.gift.event.UpdateCoinEvent
import com.module.playways.room.gift.event.UpdateMeiliEvent
import com.module.playways.room.msg.event.GiftPresentEvent
import com.module.playways.room.msg.event.MachineScoreEvent
import com.module.playways.room.msg.filter.PushMsgFilter
import com.module.playways.room.msg.manager.BattleRoomMsgManager
import com.module.playways.room.room.comment.model.CommentModel
import com.module.playways.room.room.comment.model.CommentNoticeModel
import com.module.playways.room.room.comment.model.CommentSysModel
import com.module.playways.room.room.comment.model.CommentTextModel
import com.module.playways.room.room.event.PretendCommentMsgEvent
import com.module.playways.room.room.score.MachineScoreItem
import com.zq.live.proto.BattleRoom.*
import com.zq.live.proto.Common.ESex
import com.zq.live.proto.Common.UserInfo
import com.zq.live.proto.GrabRoom.EMsgPosType
import com.zq.live.proto.GrabRoom.ERoomMsgType
import com.zq.live.proto.GrabRoom.MachineScore
import com.zq.live.proto.GrabRoom.RoomMsg
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


class BattleCorePresenter(var mRoomData: BattleRoomData, var roomView: IBattleRoomView) : RxLifeCyclePresenter() {

    companion object {
        internal val MSG_ENSURE_IN_RC_ROOM = 9// 确保在融云的聊天室，保证融云的长链接
    }

    internal var mAbsentTimes = 0

    internal var mRoomServerApi = ApiManager.getInstance().createService(BattleRoomServerApi::class.java)

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


    internal var mPushMsgFilter: PushMsgFilter<*> = PushMsgFilter<BattleRoomMsg> { msg ->
        msg != null && msg.roomID == mRoomData.gameId
    }

    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        BattleRoomMsgManager.addFilter(mPushMsgFilter)
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
        mAbsentTimes = 0

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
                ZqEngineKit.getInstance().init("battleroom", params)
            }
            // 自采集以主播身份进入房间 当token有问题时 会导致没声
            ZqEngineKit.getInstance().joinRoom(mRoomData.gameId.toString(), UserAccountManager.uuidAsLong.toInt(), false, mRoomData.agoraToken)
            ZqEngineKit.getInstance().setClientRole(true)
        } else {
            MyLog.e(TAG, "房间号不合法 mRoomData.gameId=" + mRoomData.gameId)
        }
        joinRcRoom(-1)

        if (mRoomData.gameId > 0) {
            var roundInfoModel = mRoomData.realRoundInfo
            if (roundInfoModel == null) {
                roundInfoModel = mRoomData.expectRoundInfo
            }
            pretendNoticeMsg("房间公告", "欢迎加入主题房")
        }

        pretendSystemMsg("撕歌倡导绿色健康游戏，并24小时对语音房进行巡查。如发现违规行为，官方将封号处理。")
        pretendSystemMsg("温馨提示，连麦时佩戴耳机效果将提高游戏体验。")

        startHeartbeat()
        startSyncGameStatus()
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

    private fun pretendNoticeMsg(title: String, content: String) {
        val noticeModel = CommentNoticeModel(title, content)
        EventBus.getDefault().post(PretendCommentMsgEvent(noticeModel))
    }

    private fun pretendSystemMsg(text: String) {
        val commentSysModel = CommentSysModel(mRoomData.gameType, text)
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
        ZqEngineKit.getInstance().destroy("battleroom")
        mUiHandler.removeCallbacksAndMessages(null)
        BattleRoomMsgManager.removeFilter(mPushMsgFilter)
        ModuleServiceManager.getInstance().msgService.leaveChatRoom(mRoomData.gameId.toString())
        JiGuangPush.exitSkrRoomId(mRoomData.gameId.toString())
        MyLog.d(TAG, "destroy over")
    }

    /**
     * 上报轮次结束信息
     */
    fun sendRoundOverInfo() {
        if (mRoomData?.realRoundInfo?.hasSendRoundOverInfo == false) {
            MyLog.w(TAG, "上报我的演唱结束")
            mRoomData?.realRoundInfo?.hasSendRoundOverInfo = true
            val map = HashMap<String, Any>()
            map["roomID"] = mRoomData.gameId
            map["roundSeq"] = mRoomData.realRoundInfo?.roundSeq ?: 0

            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            launch {
                var result = subscribe { mRoomServerApi.sendRoundOver(body) }
                if (result.errno == 0) {
                    MyLog.w(TAG, "演唱结束上报成功 traceid is " + result.traceId)
                } else {
                    MyLog.w(TAG, "演唱结束上报失败 traceid is " + result.traceId)
                }
            }
        } else {
            MyLog.w(TAG, "已经上报过演唱结束")
        }
    }


    /**
     * 放弃演唱接口
     */
    fun giveUpSing(okCallback: (() -> Unit)?) {
        MyLog.w(TAG, "我放弃演唱")
        val map = HashMap<String, Any?>()
        map["roomID"] = mRoomData.gameId
        map["roundSeq"] = mRoomData.realRoundInfo?.roundSeq ?: 0
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        launch {
            var result = subscribe(RequestControl("giveUpSing", ControlType.CancelThis)) {
                mRoomServerApi.giveUpSing(body)
            }
            if (result.errno == 0) {
                //closeEngine()
                okCallback?.invoke()
                MyLog.w(TAG, "放弃演唱上报成功 traceid is " + result.traceId)
            } else {
                MyLog.w(TAG, "放弃演唱上报失败 traceid is " + result.traceId)
            }
        }
    }

    /**
     * 正常演唱结束
     */
    fun overSing() {
        MyLog.w(TAG, "overSing")
        val map = HashMap<String, Any?>()
        map["roomID"] = mRoomData.gameId
        map["roundSeq"] = mRoomData.realRoundInfo?.roundSeq ?: 0
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        launch {
            var result = subscribe(RequestControl("overSing", ControlType.CancelThis)) {
                mRoomServerApi.overSing(body)
            }
            if (result.errno == 0) {
                //closeEngine()
                MyLog.w(TAG, "overSing 上报成功 traceid is " + result.traceId)
            } else {
                MyLog.w(TAG, "overSing 上报失败 traceid is " + result.traceId)
            }
        }
    }

    /**
     * 正常等待结束
     */
    fun overWait() {
        MyLog.w(TAG, "overWait")
        val map = HashMap<String, Any?>()
        map["roomID"] = mRoomData.gameId
        map["roundSeq"] = mRoomData.realRoundInfo?.roundSeq ?: 0
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        launch {
            var result = subscribe(RequestControl("overWait", ControlType.CancelThis)) {
                mRoomServerApi.overWait(body)
            }
            if (result.errno == 0) {
                //closeEngine()
                MyLog.w(TAG, "overWait 上报成功 traceid is " + result.traceId)
            } else {
                MyLog.w(TAG, "overWait 上报失败 traceid is " + result.traceId)
            }
        }
    }

    /**
     * 使用帮唱卡
     */
    fun reqHelpSing() {
        MyLog.w(TAG, "reqHelpSing")
        val map = HashMap<String, Any?>()
        map["roomID"] = mRoomData.gameId
        map["roundSeq"] = mRoomData.realRoundInfo?.roundSeq ?: 0
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        launch {
            var result = subscribe(RequestControl("reqHelpSing", ControlType.CancelThis)) {
                mRoomServerApi.reqHelpSing(body)
            }
            if (result.errno == 0) {
                //closeEngine()
                MyLog.w(TAG, "reqHelpSing 成功 traceid is " + result.traceId)
            } else {
                MyLog.w(TAG, "reqHelpSing 失败 traceid is " + result.traceId)
            }
        }
    }

    /**
     * 使用换歌卡
     */
    fun reqSwitchSing() {
        MyLog.w(TAG, "reqSwitchSing")
        val map = HashMap<String, Any?>()
        map["roomID"] = mRoomData.gameId
        map["roundSeq"] = mRoomData.realRoundInfo?.roundSeq ?: 0
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        launch {
            var result = subscribe(RequestControl("reqSwitchSing", ControlType.CancelThis)) {
                mRoomServerApi.reqSwitchSing(body)
            }
            if (result.errno == 0) {
                //closeEngine()
                MyLog.w(TAG, "reqSwitchSing 成功 traceid is " + result.traceId)
            } else {
                MyLog.w(TAG, "reqSwitchSing 失败 traceid is " + result.traceId)
            }
        }
    }

    /**
     * 抢唱
     */
    fun grabSing() {
        MyLog.w(TAG, "grabSing")
        val map = HashMap<String, Any?>()
        map["roomID"] = mRoomData.gameId
        map["roundSeq"] = mRoomData.realRoundInfo?.roundSeq ?: 0
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        launch {
            var result = subscribe(RequestControl("grabSing", ControlType.CancelThis)) {
                mRoomServerApi.grabSing(body)
            }
            if (result.errno == 0) {
                //closeEngine()
                MyLog.w(TAG, "grabSing 成功 traceid is " + result.traceId)
            } else {
                MyLog.w(TAG, "grabSing 失败 traceid is " + result.traceId)
            }
        }
    }

    fun rspHelpSing(roundSeq: Int, op: Int) {
        MyLog.w(TAG, "rspHelpSing")
        val map = HashMap<String, Any?>()
        map["roomID"] = mRoomData.gameId
        map["roundSeq"] = roundSeq
        map["helpSingType"] = op
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        launch {
            var result = subscribe(RequestControl("rspHelpSing", ControlType.CancelThis)) {
                mRoomServerApi.rspHelpSing(body)
            }
            if (result.errno == 0) {
                //closeEngine()
                MyLog.w(TAG, "rspHelpSing 成功 traceid is " + result.traceId)
            } else {
                MyLog.w(TAG, "rspHelpSing 失败 traceid is " + result.traceId)
            }
        }
    }

    /**
     * 退出房间
     */
    fun exitRoom(from: String) {
        MyLog.w(TAG, "exitRoom from=$from")
        if (mRoomData.isHasExitGame == false) {
            val map = HashMap<String, Any>()
            map["roomID"] = mRoomData.gameId
            map["roundSeq"] = mRoomData.realRoundInfo?.roundSeq ?: 0
            mRoomData.isHasExitGame = true
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            // 不想 destroy 时被取消
            GlobalScope.launch {
                var result = subscribe { mRoomServerApi.userExit(body) }
                if (result.errno == 0) {

                }
            }
        }
    }

    var heartbeatJob: Job? = null

    /**
     * 主持人心跳
     */
    private fun startHeartbeat() {
        heartbeatJob?.cancel()
//        heartbeatJob = launch {
//            while (true) {
//                if (mRoomData?.getMyUserInfoInBattle()?.isHost()) {
//                    val map = mutableMapOf(
//                            "roomID" to mRoomData.gameId,
//                            "hostUserID" to MyUserInfoManager.uid
//                    )
//                    val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
//                    val result = subscribe { mRoomServerApi.heartbeat(body) }
//                    if (result.errno == 0) {
//
//                    } else {
//
//                    }
//                    delay(60 * 1000)
//                } else {
//                    heartbeatJob?.cancel()
//                    break
//                }
//            }
//        }
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
                delay(12 * 1000)
                syncGameStatusInner()
            }
        }
    }

    private fun syncGameStatusInner() {
        launch {
            val result = subscribe {
                mRoomServerApi.syncStatus(mRoomData.gameId.toLong())
            }
            if (result.errno == 0) {
                val syncStatusTimeMs = result.data.getLongValue("serverSendTimeMs")
                if (syncStatusTimeMs > mRoomData.lastSyncTs) {
                    mRoomData.lastSyncTs = syncStatusTimeMs
                    val thisRound = JSON.parseObject(result.data.getString("currentRound"), BattleRoundInfoModel::class.java)
                    val gameOverTimeMs = result.data.getLongValue("gameOverTimeMs")
                    // 延迟10秒sync ，一旦启动sync 间隔 5秒 sync 一次
                    processSyncResult(false, gameOverTimeMs, thisRound)
                }
            } else {

            }
        }
    }


    /**
     * 轮次切换事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: BattleRoundChangeEvent) {
        MyLog.d(TAG, "RelayRoundChangeEvent = $event")
        processStatusChange(1, event.lastRound, event.newRound)
    }

    /**
     * 轮次内状态更新
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: BattleRoundStatusChangeEvent) {
        MyLog.d(TAG, "RelayRoundStatusChangeEvent =$event")
        processStatusChange(2, null, event.roundInfo)
    }

    private fun processStatusChange(from: Int, lastRound: BattleRoundInfoModel?, thisRound: BattleRoundInfoModel?) {
        DebugLogView.println(TAG, "processStatusChange from=$from roundSeq=${thisRound?.roundSeq} statusNow=${thisRound?.status}")
        if (thisRound == null) {
            // 游戏结束了
            roomView.gameOver("thisRound == null")
            return
        }
        // 屏蔽所有对手的声音
        closeEngine()
        for (u in mRoomData.opTeamInfo) {
            ZqEngineKit.getInstance().muteRemoteAudioStream(u.userID, false)
        }

        if (thisRound.status == EBRoundStatus.BRS_INTRO.value) {
            var r = {
                if (thisRound.userID == MyUserInfoManager.uid.toInt()) {
                    tryDownloadAccIfSelfSing()
                }
                roomView.showIntro()
            }
            // 导唱阶段
            if (lastRound == null) {
                // 没有上一轮，直接显示导唱卡
                if (thisRound.musicSeq == 1) {
                    // 先显示对战开始的tips
                    roomView.showBeginTips {
                        r.invoke()
                    }
                } else {
                    r.invoke()
                }
            } else {
                // 先尝试显示上一轮结果
                if (lastRound.overReason != EBRoundOverReason.BROR_UNKNOWN.value
                        && lastRound.overReason != EBRoundOverReason.BROR_REQ_HELP_SING.value
                        && lastRound.overReason != EBRoundOverReason.BROR_REQ_SWITCH_SING.value) {
                    // 如果上一轮的结束原因是 使用帮唱卡 使用换歌卡 则不会显示结果页
                    r.invoke()
                } else {
                    roomView.showRoundOver(lastRound) {
                        r.invoke()
                    }
                }
            }
        } else if (thisRound.status == EBRoundStatus.BRS_HELP.value) {
            if (thisRound.userID == MyUserInfoManager.uid.toInt()) {
                tryDownloadAccIfSelfSing()
            }
            // 使用了帮唱卡
            roomView.useHelpSing()
        } else if (thisRound.status == EBRoundStatus.BRS_SING.value) {
            // 进入演唱阶段
            if (thisRound.userID == MyUserInfoManager.uid.toInt()) {
                // 打开自己的麦
                ZqEngineKit.getInstance().muteLocalAudioStream(false)
                roomView.showSelfSing()
                onChangeBroadcastSuccess()
            } else {
                roomView.showOtherSing()
                // 其他人要接收演唱者的音频流了
                ZqEngineKit.getInstance().muteRemoteAudioStream(thisRound.userID, false)
            }
        }
    }


    private fun tryDownloadAccIfSelfSing() {
        /**
         * 有的网络伴奏在线播有问题，比如公司网络，这里尝试提前下载一下伴奏
         */
        mRoomData.realRoundInfo?.music?.let { songModel ->
            MediaCacheManager.preCache(songModel.acc)
            val midiFile = SongResUtils.getMIDIFileByUrl(songModel?.midi)
            // 下载midi
            if (midiFile != null && !midiFile.exists()) {
                MyLog.d(TAG, "onChangeBroadcastSuccess 下载midi文件 url=${songModel.midi} => local=${midiFile.path}")
                U.getHttpUtils().downloadFileAsync(songModel.midi, midiFile, true, null)
            }
        }
    }

    /**
     * 成功切换为主播  开始演唱了
     */
    private fun onChangeBroadcastSuccess() {
        var songModel = mRoomData?.realRoundInfo?.music
        if (songModel == null) {
            return
        }
        // 开始开始混伴奏，开始解除引擎mute
//            val accFile = SongResUtils.getAccFileByUrl(songModel.acc)
        // midi不需要在这下，只要下好，native就会解析，打分就能恢复
        val midiFile = SongResUtils.getMIDIFileByUrl(songModel?.midi)
        // 下载midi
        if (midiFile != null && !midiFile.exists()) {
            if (U.getHttpUtils().isDownloading(songModel.midi)) {

            } else {
                MyLog.d(TAG, "onChangeBroadcastSuccess 下载midi文件 url=${songModel.midi} => local=${midiFile.path}")
                U.getHttpUtils().downloadFileAsync(songModel.midi, midiFile, true, null)
            }
        }

        //  播放伴奏
        val songBeginTs = songModel.beginMs
        ZqEngineKit.getInstance().startAudioMixing(MyUserInfoManager.uid.toInt(), songModel.accWithCdnInfosJson, midiFile.absolutePath, songBeginTs.toLong(), 1)
        // 启动acr打分识别
        //有伴奏模式，开启acr自动模式
        ZqEngineKit.getInstance().startRecognize(RecognizeConfig.newBuilder()
                .setSongName(songModel.itemName)
                .setArtist(songModel.owner)
                .setMode(RecognizeConfig.MODE_MANUAL)
                .build())
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


    private fun pretendEnterRoomMsg(playerInfoModel: BattlePlayerInfoModel) {
        val commentModel = CommentTextModel()
        commentModel.userInfo = playerInfoModel.userInfo
        commentModel.avatarColor = CommentModel.AVATAR_COLOR
        val nameBuilder =
                SpanUtils().append(playerInfoModel.userInfo.nicknameRemark + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                        .create()
        commentModel.nameBuilder = nameBuilder
        val stringBuilder = SpanUtils()
                .append("加入了房间").setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
                .create()
        commentModel.stringBuilder = stringBuilder
        EventBus.getDefault().post(PretendCommentMsgEvent(commentModel))
    }

//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onEvent(event: PExitGameMsg) {
//        MyLog.d(TAG, "onEvent event = $event")
//        mRoomData.applyUserCnt = event.applyUserCnt
//        mRoomData.onlineUserCnt = event.onlineUserCnt
//        var u = BattlePlayerInfoModel.parseFromPb(event.user)
//        mRoomData.updateUser(u, null)
//    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: BGameOverMsg) {
        MyLog.d(TAG, "onEvent event = $event")
//        event.teamResultList?.forEach {
//            it.
//        }
        mRoomData.expectRoundInfo = null
        mRoomData.checkRoundInEachMode()
    }

    /**
     * 轮次变化
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: BNextRoundMsg) {
        MyLog.w(TAG, "收到服务器的某一个人轮次结束的push currentRound:${event.currentRound}")
        MyLog.w(TAG, "收到服务器的某一个人轮次结束的push nextRound:${event.nextRound}")
        ensureInRcRoom()
//        roomView.showSongCount(event.musicCnt)
        var currentRound = BattleRoundInfoModel.parseFromRoundInfo(event.currentRound)
        if (mRoomData.realRoundSeq == currentRound.roundSeq) {
            currentRound.result?.teamScore?.forEach {
                if (mRoomData?.myTeamTag == it.teamTag) {
                    // 得到我的队伍的总分
                    mRoomData?.myTeamScore = it.teamScore
                } else {
                    // 得到对方队伍的总分
                    mRoomData?.opTeamScore = it.teamScore
                }
            }
        }
        var nextRound = BattleRoundInfoModel.parseFromRoundInfo(event.nextRound)
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

    /**
     * 同步
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: BSyncMsg) {
        ensureInRcRoom()
        MyLog.w(TAG, "收到服务器 sync push更新状态 ,event=$event")
        if (event.syncStatusTimeMs > mRoomData.lastSyncTs) {
            mRoomData.lastSyncTs = event.syncStatusTimeMs
            var thisRound = BattleRoundInfoModel.parseFromRoundInfo(event.currentRound)
            // 延迟10秒sync ，一旦启动sync 间隔 5秒 sync 一次
            startSyncGameStatus()
            processSyncResult(true, 0, thisRound)
        }
    }

    /**
     * 明确数据可以刷新
     */
    private fun processSyncResult(fromPush: Boolean, gameOverTimeMs: Long, thisRound: BattleRoundInfoModel?) {
        mRoomData.gameOverTs = gameOverTimeMs
        if (gameOverTimeMs > 0) {
            DebugLogView.println(TAG, "gameOverTimeMs=${gameOverTimeMs} 游戏结束时间>0,游戏结束")
            mRoomData.gameOverTs = gameOverTimeMs
            // 游戏结束了，停服了
            mRoomData.expectRoundInfo = null
            mRoomData.checkRoundInEachMode()
        } else {
            if (thisRound?.roundSeq == mRoomData.realRoundSeq) {
                mRoomData.realRoundInfo?.tryUpdateRoundInfoModel(thisRound, true)
            } else if ((thisRound?.roundSeq ?: 0) > mRoomData.realRoundSeq) {
                MyLog.w(TAG, "sync 回来的轮次大，要替换 roundInfo 了")
                // 主轮次结束
                mRoomData.expectRoundInfo = thisRound
                mRoomData.checkRoundInEachMode()
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ActivityUtils.ForeOrBackgroundChange) {
        MyLog.w(TAG, if (event.foreground) "切换到前台" else "切换到后台")
//        if (event.foreground) {
//            muteAllRemoteAudioStreams(mRoomData.isMute, false)
//        } else {
//            muteAllRemoteAudioStreams(true, false)
//        }
    }

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
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MachineScoreEvent) {
        //收到其他人的机器打分消息，比较复杂，暂时简单点，轮次正确就直接展示
        if (mRoomData?.realRoundInfo?.userID == event.userId) {
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
            sendScoreToServer(score, line)
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
        val map = java.util.HashMap<String, Any>()
        val infoModel = mRoomData.realRoundInfo ?: return
        map["userID"] = MyUserInfoManager.uid

        var itemID = 0
        if (infoModel.music != null) {
            itemID = infoModel?.music?.itemID ?: 0
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
        map["teamTag"] = mRoomData.myTeamTag
//        map["segmentCnt"] = (mRoomData.realRoundInfo?.music?.relaySegments?.size ?: 0) + 1
//        map["sentenceCnt"] = mRoomData.sentenceCnt

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
                StatisticsAdapter.recordCountEvent("party", "game_getflower", null)
            } else {
                StatisticsAdapter.recordCountEvent("party", "game_getgift", null)
            }
        }
    }

}