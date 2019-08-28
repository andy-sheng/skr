package com.module.playways.race.room.presenter

import android.os.Handler
import android.os.Message
import android.support.annotation.CallSuper
import com.alibaba.fastjson.JSON
import com.common.core.account.UserAccountManager
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.engine.ScoreConfig
import com.common.jiguang.JiGuangPush
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.common.statistics.StatisticsAdapter
import com.common.utils.ActivityUtils
import com.common.utils.U
import com.component.lyrics.LyricAndAccMatchManager
import com.component.lyrics.utils.SongResUtils
import com.engine.EngineEvent
import com.engine.Params
import com.engine.arccloud.RecognizeConfig
import com.module.ModuleServiceManager
import com.module.common.ICallback
import com.module.playways.race.RaceRoomServerApi
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.event.RaceRoundChangeEvent
import com.module.playways.race.room.event.RaceRoundStatusChangeEvent
import com.module.playways.race.room.event.RaceSubRoundChangeEvent
import com.module.playways.race.room.inter.IRaceRoomView
import com.module.playways.race.room.model.RacePlayerInfoModel
import com.module.playways.race.room.model.RaceRoundInfoModel
import com.module.playways.race.room.model.parseFromGameInfoPB
import com.module.playways.race.room.model.parseFromRoundInfoPB
import com.module.playways.room.gift.event.GiftBrushMsgEvent
import com.module.playways.room.gift.event.UpdateCoinEvent
import com.module.playways.room.gift.event.UpdateMeiliEvent
import com.module.playways.room.msg.event.GiftPresentEvent
import com.module.playways.room.msg.event.raceroom.*
import com.module.playways.room.msg.filter.PushMsgFilter
import com.module.playways.room.msg.manager.RaceRoomMsgManager
import com.module.playways.room.room.comment.model.CommentSysModel
import com.module.playways.room.room.event.PretendCommentMsgEvent
import com.zq.live.proto.RaceRoom.ERWantSingType
import com.zq.live.proto.RaceRoom.ERaceRoundStatus
import com.zq.live.proto.RaceRoom.ERoundOverType
import com.zq.live.proto.Room.RoomMsg
import com.zq.mediaengine.kit.ZqEngineKit
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RaceCorePresenter(var mRoomData: RaceRoomData, var mIRaceRoomView: IRaceRoomView) : RxLifeCyclePresenter() {

    val raceRoomServerApi = ApiManager.getInstance().createService(RaceRoomServerApi::class.java)

    internal val MSG_ENSURE_IN_RC_ROOM = 9// 确保在融云的聊天室，保证融云的长链接

    internal val MSG_ENSURE_SWITCH_BROADCAST_SUCCESS = 21 // 确保用户切换成主播成功，防止引擎不回调的保护

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
                MSG_ENSURE_SWITCH_BROADCAST_SUCCESS -> {
//                    onChangeBroadcastSuccess()
                }
            }
        }
    }

    internal var mPushMsgFilter: PushMsgFilter<*> = PushMsgFilter<RoomMsg> { msg ->
        val b = msg != null && msg.roomID == mRoomData.gameId
        b
    }

    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        //添加房间消息过滤器
        RaceRoomMsgManager.addFilter(mPushMsgFilter)
        val commentSysModel = CommentSysModel("欢迎来到Race房间", CommentSysModel.TYPE_ENTER_ROOM)
        EventBus.getDefault().post(PretendCommentMsgEvent(commentSysModel))
        joinRoomAndInit(true)
    }

    /**
     * 加入引擎房间
     * 加入融云房间
     * 系统消息弹幕
     */
    private fun joinRoomAndInit(first: Boolean) {
        MyLog.w(TAG, "joinRoomAndInit" + " first=" + first + ", gameId is " + mRoomData.gameId)

        if (mRoomData.gameId > 0) {
            var reInit = false
            if (first) {
                reInit = true
            }
            if (reInit) {
                val params = Params.getFromPref().apply {
                    scene = Params.Scene.grab
                    isEnableAudio = false
                }
                ZqEngineKit.getInstance().init("raceroom", params)
            }
            ZqEngineKit.getInstance().joinRoom(mRoomData.gameId.toString(), UserAccountManager.getInstance().uuidAsLong.toInt(), false, mRoomData.agoraToken)
            // 不发送本地音频, 会造成第一次抢没声音
            ZqEngineKit.getInstance().muteLocalAudioStream(true)
        }
        joinRcRoom(-1)
        if (mRoomData.gameId > 0) {
            mRoomData.getPlayerInfoList<RacePlayerInfoModel>()?.let {
                for (playerInfoModel in it) {
                    if (!playerInfoModel.isOnline()) {
                        continue
                    }
//                    pretendEnterRoom(playerInfoModel)
                }
            }
//            pretendRoomNameSystemMsg(mRoomData.getRoomName(), CommentSysModel.TYPE_ENTER_ROOM)
        }
        startHeartbeat()
        startSyncRaceStatus()
    }

    private fun joinRcRoom(deep: Int) {
        if (deep > 4) {
            MyLog.d(TAG, "加入融云房间，重试5次仍然失败，放弃")
            return
        }
        if (mRoomData.gameId > 0) {
            ModuleServiceManager.getInstance().msgService.joinChatRoom(mRoomData.gameId.toString(), -1, object : ICallback {
                override fun onSucess(obj: Any) {
                    MyLog.d(TAG, "加入融云房间成功")
                }

                override fun onFailed(obj: Any, errcode: Int, message: String) {
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

    /**
     * 开场动画播放完毕
     */
    fun onOpeningAnimationOver() {
        mRoomData.checkRoundInEachMode()
        ensureInRcRoom()
    }


    /**
     * 相当于告知服务器，我不抢
     */
    fun sendIntroOver() {
        launch {
            val map = mutableMapOf(
                    "roomID" to mRoomData.gameId,
                    "roundSeq" to mRoomData.realRoundSeq
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe { raceRoomServerApi.introOver(body) }
            if (result.errno == 0) {

            } else {

            }
        }
    }

    /**
     * 选择歌曲
     */
    fun wantSingChance(choiceID: Int) {
        var wantSingType = ERWantSingType.ERWST_DEFAULT.value

        val songModel = mRoomData.realRoundInfo?.getSongModelByChoiceId(choiceID)
        if (mRoomData.isAccEnable && (songModel?.acc?.isNotBlank() == true)) {
            wantSingType = ERWantSingType.ERWST_ACCOMPANY.value
        }
        launch {
            val map = mutableMapOf(
                    "choiceID" to choiceID,
                    "roomID" to mRoomData.gameId,
                    "roundSeq" to mRoomData.realRoundSeq,
                    "wantSingType" to wantSingType
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe { raceRoomServerApi.wantSingChance(body) }
            if (result.errno == 0) {
                mRoomData?.realRoundInfo?.addWantSingChange(choiceID, MyUserInfoManager.getInstance().uid.toInt())
            } else {

            }
        }
    }

    /**
     * 爆灯&投票
     */
    fun sendBLight() {
        launch {
            val map = mutableMapOf(
                    "roomID" to mRoomData.gameId,
                    "roundSeq" to mRoomData.realRoundSeq,
                    "subRoundSeq" to mRoomData.realRoundInfo?.subRoundSeq
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe { raceRoomServerApi.bLight(body) }
            if (result.errno == 0) {

            } else {

            }
        }
    }

    /**
     * 放弃演唱
     */
    fun giveupSing() {
        launch {
            val map = mutableMapOf(
                    "roomID" to mRoomData.gameId,
                    "roundSeq" to mRoomData.realRoundSeq,
                    "subRoundSeq" to mRoomData.realRoundInfo?.subRoundSeq
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe { raceRoomServerApi.giveup(body) }
            if (result.errno == 0) {

            } else {

            }
        }
    }

    /**
     * 主动告诉服务器我演唱完毕
     */
    fun sendSingComplete() {
        launch {
            val map = mutableMapOf(
                    "roomID" to mRoomData.gameId,
                    "roundSeq" to mRoomData.realRoundSeq,
                    "subRoundSeq" to mRoomData.realRoundInfo?.subRoundSeq
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe { raceRoomServerApi.roundOver(body) }
            if (result.errno == 0) {

            } else {

            }
        }
    }

    /**
     * 退出房间
     */
    fun exitRoom(from: String) {
        MyLog.d(TAG, "exitRoom from = $from")
        launch {
            val map = mutableMapOf(
                    "roomID" to mRoomData.gameId
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe { raceRoomServerApi.exitRoom(body) }
            if (result.errno == 0) {
                mRoomData.hasExitGame = true
            } else {

            }
        }
    }

    var heartbeatJob: Job? = null

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = launch {
            val map = mutableMapOf(
                    "roomID" to mRoomData.gameId,
                    "userID" to MyUserInfoManager.getInstance().uid
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe { raceRoomServerApi.heartbeat(body) }
            if (result.errno == 0) {

            } else {

            }
            delay(60 * 1000)
        }
    }

    /**
     * 如果确定是自己唱了,预先可以做的操作
     */
    private fun preOpWhenSelfRound() {
        var needAcc = false
        var needScore = false
        val p = ZqEngineKit.getInstance().params
        if (p != null) {
            p.isGrabSingNoAcc = false
        }
        if (mRoomData.isAccEnable) {
            needAcc = true
            needScore = true
        } else {
            if (p != null) {
                p.isGrabSingNoAcc = true
                needScore = true
            }
        }
        val songModel = mRoomData?.realRoundInfo?.getSongModelNow()
        if (needAcc) {
            // 1. 开启伴奏的，预先下载 melp 资源
            songModel?.let {
                val midiFile = SongResUtils.getMIDIFileByUrl(it.midi)
                if (midiFile != null && !midiFile.exists()) {
                    U.getHttpUtils().downloadFileAsync(it.midi, midiFile, true, null)
                }
            }

        }
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

        songModel?.let {
            // 开始acr打分
            if (ScoreConfig.isAcrEnable()) {
                if (needAcc) {
                    ZqEngineKit.getInstance().startRecognize(RecognizeConfig.newBuilder()
                            .setSongName(it.itemName)
                            .setArtist(it.owner)
                            .setMode(RecognizeConfig.MODE_MANUAL)
                            .build())
                } else {
                    if (needScore) {
                        // 清唱还需要打分，那就只用 acr 打分
                        ZqEngineKit.getInstance().startRecognize(RecognizeConfig.newBuilder()
                                .setSongName(it.itemName)
                                .setArtist(it.owner)
                                .setMode(RecognizeConfig.MODE_AUTO)
                                .setAutoTimes(3)
                                .setMResultListener { result, list, targetSongInfo, lineNo ->
                                    var mAcrScore = 0
                                    if (targetSongInfo != null) {
                                        mAcrScore = (targetSongInfo.score * 100).toInt()
                                    }
                                    EventBus.getDefault().post(LyricAndAccMatchManager.ScoreResultEvent("preOpWhenSelfRound", -1, mAcrScore, 0))
                                }
                                .build())
                    } else {

                    }
                }
            }
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
     * 轮次切换事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RaceRoundChangeEvent) {
        processStatusChange(event.thisRound)
    }

    /**
     * 轮次内 状态切换事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RaceRoundStatusChangeEvent) {
        processStatusChange(event.thisRound)
    }

    /**
     * 轮次内 演唱阶段子轮次切换事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RaceSubRoundChangeEvent) {
        processStatusChange(event.thisRound)
    }

    private fun processStatusChange(thisRound: RaceRoundInfoModel?) {
        mUiHandler.removeMessages(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS)
        closeEngine()
        ZqEngineKit.getInstance().stopRecognize()
        if (thisRound?.status == ERaceRoundStatus.ERRS_WAITING.value) {
            mIRaceRoomView.showWaiting(true)
        } else if (thisRound?.status == ERaceRoundStatus.ERRS_CHOCING.value) {
            mIRaceRoomView.showChoicing(true)
        } else if (thisRound?.status == ERaceRoundStatus.ERRS_ONGOINE.value) {
            if (thisRound?.subRoundSeq == 1) {
                // 变为演唱阶段，第一轮
                val subRound1 = thisRound.subRoundInfo.get(0)
                if (subRound1.userID == MyUserInfoManager.getInstance().uid.toInt()) {
                    mIRaceRoomView.singBySelfFirstRound(mRoomData.getChoiceInfoById(subRound1.choiceID))
                    preOpWhenSelfRound()
                } else {
                    mIRaceRoomView.singByOtherFirstRound(mRoomData.getChoiceInfoById(subRound1.choiceID), mRoomData.getUserInfo(subRound1.userID))
                }
            } else if (thisRound?.subRoundSeq == 2) {
                // 变为演唱阶段，第二轮
                val subRound2 = thisRound.subRoundInfo.get(1)
                if (subRound2.userID == MyUserInfoManager.getInstance().uid.toInt()) {
                    mIRaceRoomView.singBySelfSecondRound(mRoomData.getChoiceInfoById(subRound2.choiceID))
                    preOpWhenSelfRound()
                } else {
                    mIRaceRoomView.singByOtherSecondRound(mRoomData.getChoiceInfoById(subRound2.choiceID), mRoomData.getUserInfo(subRound2.userID))
                }
            }
        } else if (thisRound?.status == ERaceRoundStatus.ERRS_END.value) {
            // 结束
            mIRaceRoomView.roundOver(thisRound?.overReason)
        }
    }

    /**
     * 用户选择了某个歌曲
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: RWantSingChanceEvent) {
        ensureInRcRoom()
        if (event.pb.roundSeq == mRoomData.realRoundSeq) {
            mRoomData?.realRoundInfo?.addWantSingChange(event.pb.choiceID, event.pb.userID)
        }
    }

    /**
     * 用户得到了演唱机会
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: RGetSingChanceEvent) {
        ensureInRcRoom()
        val roundInfoModel = parseFromRoundInfoPB(event.pb.currentRound)
        if (roundInfoModel.roundSeq == mRoomData.realRoundSeq) {
            // 轮次符合，子轮次信息应该都有了
            mRoomData.realRoundInfo?.tryUpdateRoundInfoModel(roundInfoModel, true)
        }
    }

    /**
     * 有人加入了房间
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: RJoinNoticeEvent) {
        ensureInRcRoom()
        val racePlayerInfoModel = RacePlayerInfoModel()
        racePlayerInfoModel.userInfo = UserInfoModel.parseFromPB(event.pb.user)
        racePlayerInfoModel.role = event.pb.role.value
        mRoomData.realRoundInfo?.joinUser(racePlayerInfoModel)

        if (event.pb.newRoundBegin) {
            // 游戏开始了
            if (mRoomData.realRoundInfo?.status == ERaceRoundStatus.ERRS_WAITING.value) {
                mRoomData.realRoundInfo?.updateStatus(true, ERaceRoundStatus.ERRS_CHOCING.value)
            }
        }
    }

    /**
     * 有人退出了房间
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: RExitGameEvent) {
        ensureInRcRoom()
        mRoomData.realRoundInfo?.exitUser(event.pb.userID)
    }

    /**
     * 用户爆灯投票
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: RBLightEvent) {
        ensureInRcRoom()
        MyLog.d(TAG, "onEvent event = $event")
        if (event.pb.roundSeq == mRoomData.realRoundSeq) {
            mRoomData.realRoundInfo?.addBLightUser(true, event.pb.userID, event.pb.subRoundSeq, event.pb.bLightCnt)
        }
    }

    /**
     * 轮次结束
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: RRoundOverEvent) {
        ensureInRcRoom()
        MyLog.d(TAG, "onEvent event = $event")
        if (event.pb.overType == ERoundOverType.EROT_MAIN_ROUND_OVER) {
            // 主轮次结束
            val curRoundInfo = parseFromRoundInfoPB(event.pb.currentRound)
            val nextRoundInfo = parseFromRoundInfoPB(event.pb.nextRound)
            event.pb.gamesList.forEach {
                nextRoundInfo.games.add(parseFromGameInfoPB(it))
            }
            if (curRoundInfo.roundSeq == mRoomData.realRoundSeq) {
//            mRoomData.realRoundInfo?.tryUpdateRoundInfoModel(curRoundInfo,true)
                mRoomData.expectRoundInfo = nextRoundInfo
                mRoomData.checkRoundInEachMode()
            }
        } else if (event.pb.overType == ERoundOverType.EROT_SUB_ROUND_OVER) {
            val curRoundInfo = parseFromRoundInfoPB(event.pb.currentRound)
            mRoomData.realRoundInfo?.tryUpdateRoundInfoModel(curRoundInfo, true)
        }
    }


    var syncJob: Job? = null

    fun startSyncRaceStatus() {
        syncJob?.cancel()
        syncJob = launch {
            delay(8000)
            val result = subscribe { raceRoomServerApi.syncStatus(mRoomData.gameId.toLong()) }
            if (result.errno == 0) {
                val syncStatusTimeMs = result.data.getLong("syncStatusTimeMs")
                if (syncStatusTimeMs > mRoomData.lastSyncTs) {
                    mRoomData.lastSyncTs = syncStatusTimeMs
                    val raceRoundInfoModel = JSON.parseObject(result.data.getString("currentRound"), RaceRoundInfoModel::class.java)
                    processSyncResult(raceRoundInfoModel)
                }
            } else {

            }
        }
    }

    /**
     * 收到服务器的push sync
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: RSyncStatusEvent) {
        ensureInRcRoom()
        syncJob?.cancel()
        startSyncRaceStatus()
        if (event.pb.syncStatusTimeMs > mRoomData.lastSyncTs) {
            mRoomData.lastSyncTs = event.pb.syncStatusTimeMs
            val raceRoundInfoModel = parseFromRoundInfoPB(event.pb.currentRound)
            processSyncResult(raceRoundInfoModel)
        }
    }

    private fun processSyncResult(raceRoundInfoModel: RaceRoundInfoModel) {
        if (raceRoundInfoModel.roundSeq == mRoomData.realRoundSeq) {
            mRoomData.realRoundInfo?.tryUpdateRoundInfoModel(raceRoundInfoModel, true)
        } else if (raceRoundInfoModel.roundSeq > mRoomData.realRoundSeq) {
            MyLog.w(TAG, "sync 回来的轮次大，要替换 roundinfo 了")
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(giftPresentEvent: GiftPresentEvent) {
        MyLog.d(TAG, "onEvent giftPresentEvent=$giftPresentEvent")
        EventBus.getDefault().post(GiftBrushMsgEvent(giftPresentEvent.mGPrensentGiftMsgModel))

        if (giftPresentEvent.mGPrensentGiftMsgModel.propertyModelList != null) {
            for (property in giftPresentEvent.mGPrensentGiftMsgModel.propertyModelList) {
                if (property.userID.toLong() == MyUserInfoManager.getInstance().uid) {
                    if (property.coinBalance != -1f) {
                        UpdateCoinEvent.sendEvent(property.coinBalance.toInt(), property.lastChangeMs)
                    }
                    if (property.hongZuanBalance != -1f) {
                        //mRoomData.setHzCount(property.hongZuanBalance, property.lastChangeMs)
                    }
                }
                if (property.curRoundSeqMeiliTotal > 0) {
                    // 他人的只关心魅力值的变化
                    EventBus.getDefault().post(UpdateMeiliEvent(property.userID, property.curRoundSeqMeiliTotal.toInt(), property.lastChangeMs))
                }
            }
        }

        if (giftPresentEvent.mGPrensentGiftMsgModel.receiveUserInfo.userId.toLong() == MyUserInfoManager.getInstance().uid) {
            if (giftPresentEvent.mGPrensentGiftMsgModel.giftInfo.price <= 0) {
                StatisticsAdapter.recordCountEvent("grab", "game_getflower", null)
            } else {
                StatisticsAdapter.recordCountEvent("grab", "game_getgift", null)
            }
        }
    }

    /**
     * 游戏切后台或切回来
     *
     * @param out 切出去
     * @param in  切回来
     */
    fun swapGame(out: Boolean, inB: Boolean) {
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
//                mExoPlayer.setMuteAudio(true)
//            }
        } else {
            // 如果打开静音
//            if (mExoPlayer != null) {
//                mExoPlayer.setMuteAudio(false)
//            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ActivityUtils.ForeOrBackgroundChange) {
        MyLog.w(TAG, if (event.foreground) "切换到前台" else "切换到后台")
        swapGame(!event.foreground, event.foreground)
        if (event.foreground) {
            muteAllRemoteAudioStreams(mRoomData.isMute, false)
        } else {
            muteAllRemoteAudioStreams(true, false)
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
                if (roundInfoModel != null && roundInfoModel.isSingerNowBySelf()) {
                    MyLog.d(TAG, "演唱环节切换主播成功")
                    onChangeBroadcastSuccess()
                }
            }
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
            if (mRoomData.realRoundInfo?.isSingerNowBySelf() == false) {
                MyLog.d(TAG, "onChangeBroadcastSuccess,但已经不是你的轮次了，cancel")
                return@Runnable
            }
            var songModel = mRoomData?.realRoundInfo?.getSongModelNow()
            if (songModel == null) {
                return@Runnable
            }
            // 开始开始混伴奏，开始解除引擎mute
            val accFile = SongResUtils.getAccFileByUrl(songModel.acc)
            // midi不需要在这下，只要下好，native就会解析，打分就能恢复
            val midiFile = SongResUtils.getMIDIFileByUrl(songModel.midi)

            if (mRoomData.isAccEnable && (mRoomData?.realRoundInfo?.isAccRoundNow() == true)) {
                val songBeginTs = songModel.beginMs
                if (accFile != null && accFile.exists()) {
                    // 伴奏文件存在
                    ZqEngineKit.getInstance().startAudioMixing(MyUserInfoManager.getInstance().uid.toInt(), accFile.absolutePath, midiFile.absolutePath, songBeginTs.toLong(), false, false, 1)
                } else {
                    ZqEngineKit.getInstance().startAudioMixing(MyUserInfoManager.getInstance().uid.toInt(), songModel.acc, midiFile.absolutePath, songBeginTs.toLong(), false, false, 1)
                }
            }
        })
    }

    @CallSuper
    override fun destroy() {
        super.destroy()
        if (!mRoomData.hasExitGame) {
            exitRoom("destroy")
        }
        mUiHandler.removeCallbacksAndMessages(null)
        Params.save2Pref(ZqEngineKit.getInstance().params)
        ZqEngineKit.getInstance().destroy("raceroom")
        RaceRoomMsgManager.removeFilter(mPushMsgFilter)
        ModuleServiceManager.getInstance().msgService.leaveChatRoom(mRoomData.gameId.toString())
        JiGuangPush.exitSkrRoomId(mRoomData.gameId.toString())
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }

}