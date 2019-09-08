package com.module.playways.race.room.presenter

import android.os.Handler
import android.os.Message
import android.support.annotation.CallSuper
import android.text.SpannableStringBuilder
import com.alibaba.fastjson.JSON
import com.common.core.account.UserAccountManager
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.jiguang.JiGuangPush
import com.common.log.DebugLogView
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.common.statistics.StatisticsAdapter
import com.common.utils.ActivityUtils
import com.common.utils.SpanUtils
import com.common.utils.U
import com.component.busilib.constans.GameModeType
import com.component.lyrics.utils.SongResUtils
import com.engine.EngineEvent
import com.engine.Params
import com.module.ModuleServiceManager
import com.module.common.ICallback
import com.module.playways.race.RaceRoomServerApi
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.event.RaceRoundChangeEvent
import com.module.playways.race.room.event.RaceRoundStatusChangeEvent
import com.module.playways.race.room.event.RaceSubRoundChangeEvent
import com.module.playways.race.room.inter.IRaceRoomView
import com.module.playways.race.room.model.*
import com.module.playways.room.gift.event.GiftBrushMsgEvent
import com.module.playways.room.gift.event.UpdateCoinEvent
import com.module.playways.room.gift.event.UpdateMeiliEvent
import com.module.playways.room.msg.event.GiftPresentEvent
import com.module.playways.room.msg.event.raceroom.*
import com.module.playways.room.msg.filter.PushMsgFilter
import com.module.playways.room.msg.manager.RaceRoomMsgManager
import com.module.playways.room.room.comment.model.CommentModel
import com.module.playways.room.room.comment.model.CommentSysModel
import com.module.playways.room.room.comment.model.CommentTextModel
import com.module.playways.room.room.event.PretendCommentMsgEvent
import com.module.playways.songmanager.event.MuteAllVoiceEvent
import com.zq.live.proto.RaceRoom.*
import com.zq.mediaengine.kit.ZqEngineKit
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.Runnable

class RaceCorePresenter(var mRoomData: RaceRoomData, var mIRaceRoomView: IRaceRoomView) : RxLifeCyclePresenter() {

    val raceRoomServerApi = ApiManager.getInstance().createService(RaceRoomServerApi::class.java)

    internal val MSG_ENSURE_IN_RC_ROOM = 9// 确保在融云的聊天室，保证融云的长链接

    internal val MSG_ENSURE_SWITCH_BROADCAST_SUCCESS = 21 // 确保用户切换成主播成功，防止引擎不回调的保护

    internal var mUiHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                MSG_ENSURE_IN_RC_ROOM -> {
                    DebugLogView.println(TAG, "handleMessage 长时间没收到push，重新进入融云房间容错")
                    ModuleServiceManager.getInstance().msgService.leaveChatRoom(mRoomData.gameId.toString() + "")
                    joinRcRoom(0)
                    ensureInRcRoom()
                }
                MSG_ENSURE_SWITCH_BROADCAST_SUCCESS -> {
                    onChangeBroadcastSuccess()
                }
            }
        }
    }

    internal var mPushMsgFilter: PushMsgFilter<*> = PushMsgFilter<RaceRoomMsg> { msg ->
        val b = msg != null && msg.roomID == mRoomData.gameId
        b
    }

    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
        //添加房间消息过滤器
        RaceRoomMsgManager.addFilter(mPushMsgFilter)
        val commentSysModel = CommentSysModel(GameModeType.GAME_MODE_RACE, "欢迎加入撕歌排位赛，撕歌倡导文明竞演、理性投票，如遇恶意玩家请点击头像进行举报")
        EventBus.getDefault().post(PretendCommentMsgEvent(commentSysModel))
        joinRoomAndInit(true)
    }

    /**
     * 加入引擎房间
     * 加入融云房间
     * 系统消息弹幕
     */
    private fun joinRoomAndInit(first: Boolean) {
        DebugLogView.println(TAG, "joinRoomAndInit" + " first=" + first + ", gameId is " + mRoomData.gameId)

        if (mRoomData.gameId > 0) {
            var reInit = false
            if (first) {
                reInit = true
            }
            if (reInit) {
                val params = Params.getFromPref().apply {
                    scene = Params.Scene.grab
                    isEnableAudio = true
                    isEnableVideo = false
                }
                ZqEngineKit.getInstance().init("raceroom", params)
            }
            ZqEngineKit.getInstance().joinRoom(mRoomData.gameId.toString(), UserAccountManager.getInstance().uuidAsLong.toInt(), false, mRoomData.agoraToken)
            // 不发送本地音频, 会造成第一次抢没声音
            ZqEngineKit.getInstance().muteLocalAudioStream(true)
        }
        joinRcRoom(-1)
        if (mRoomData.gameId > 0) {
            for (playerInfoModel in mRoomData.getPlayerInfoList()) {
                if (!playerInfoModel.isOnline) {
                    continue
                }
//                    pretendEnterRoom(playerInfoModel)
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

                override fun onSucess(obj: Any?) {
                    DebugLogView.println(TAG, "加入融云房间成功")
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
    fun wantSingChance(choiceID: Int, seq: Int) {
        if (seq != mRoomData.realRoundSeq) {
            return
        }

        var wantSingType = ERWantSingType.ERWST_DEFAULT.value

        val songModel = mRoomData.realRoundInfo?.getSongModelByChoiceId(choiceID)
        if (mRoomData.isAccEnable && (songModel?.acc?.isNotBlank() == true)) {
            wantSingType = ERWantSingType.ERWST_ACCOMPANY.value
        }
        launch {
            val map = mutableMapOf(
                    "choiceID" to choiceID,
                    "roomID" to mRoomData.gameId,
                    "roundSeq" to seq,
                    "wantSingType" to wantSingType
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe { raceRoomServerApi.wantSingChance(body) }
            if (result.errno == 0) {
                if (seq == mRoomData.realRoundSeq) {
                    mRoomData?.realRoundInfo?.addWantSingChange(choiceID, MyUserInfoManager.getInstance().uid.toInt())
                }
            } else {

            }
        }
    }

    /**
     * 爆灯&投票
     */
    fun sendBLight(callback: ((isSucess: Boolean) -> Unit)?) {
        launch {
            val map = mutableMapOf(
                    "roomID" to mRoomData.gameId,
                    "roundSeq" to mRoomData.realRoundSeq,
                    "subRoundSeq" to mRoomData.realRoundInfo?.subRoundSeq
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe { raceRoomServerApi.bLight(body) }
            if (result.errno == 0) {
                callback?.invoke(true)
            } else {
                if (result.errno == 8412159) {
                    // 已经投过票
                    U.getToastUtil().showShort(result.errmsg)
                    callback?.invoke(true)
                } else {
                    callback?.invoke(false)
                }
            }
        }
    }

    /**
     * 放弃演唱
     */
    fun giveupSing(callback: ((isSucess: Boolean) -> Unit)?) {
        launch {
            val map = mutableMapOf(
                    "roomID" to mRoomData.gameId,
                    "roundSeq" to mRoomData.realRoundSeq,
                    "subRoundSeq" to mRoomData.realRoundInfo?.subRoundSeq
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe { raceRoomServerApi.giveup(body) }
            if (result.errno == 0) {
                callback?.invoke(true)
            } else {
                callback?.invoke(false)
            }
        }
    }

    /**
     * 主动告诉服务器我演唱完毕
     */
    fun sendSingComplete(from: String) {
        MyLog.d(TAG, "sendSingComplete from = $from")
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

    companion object {
        /**
         * 退出房间
         */
        fun ComExitRoom(from: String, gameID: Int, raceRoomServerApi: RaceRoomServerApi) {
            MyLog.d("RaceCorePresenter", "exitRoom from = $from")
            GlobalScope.launch {
                val map = mutableMapOf(
                        "roomID" to gameID
                )
                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                subscribe { raceRoomServerApi.exitRoom(body) }
            }
        }
    }

    fun exitRoom(from: String) {
        MyLog.d("RaceCorePresenter", "exitRoom from = $from")
        mRoomData.hasExitGame = true
        ComExitRoom(from, mRoomData.gameId, raceRoomServerApi)
    }

    fun goResultPage(lastRound: RaceRoundInfoModel) {
        exitRoom("goResultPage")
        mIRaceRoomView.goResultPage(lastRound)
    }

    var heartbeatJob: Job? = null

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = launch {
            while (true) {
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
    }

    /**
     * 如果确定是自己唱了,预先可以做的操作
     */
    private fun preOpWhenSelfRound() {
        var needAcc = false
        val songModel = mRoomData?.realRoundInfo?.getSongModelNow()
        if (mRoomData.isAccEnable && mRoomData?.realRoundInfo?.isAccRoundNow() == true) {
            needAcc = true
        }

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

//        songModel?.let {
//            // 开始acr打分
//            if (ScoreConfig.isAcrEnable()) {
//                if (needAcc) {
//                    ZqEngineKit.getInstance().startRecognize(RecognizeConfig.newBuilder()
//                            .setSongName(it.itemName)
//                            .setArtist(it.owner)
//                            .setMode(RecognizeConfig.MODE_MANUAL)
//                            .build())
//                } else {
//                    if (needScore) {
//                        // 清唱还需要打分，那就只用 acr 自动打分
//                        ZqEngineKit.getInstance().startRecognize(RecognizeConfig.newBuilder()
//                                .setSongName(it.itemName)
//                                .setArtist(it.owner)
//                                .setMode(RecognizeConfig.MODE_AUTO)
//                                .setAutoTimes(3)
//                                .setMResultListener { result, list, targetSongInfo, lineNo ->
//                                    var mAcrScore = 0
//                                    if (targetSongInfo != null) {
//                                        mAcrScore = (targetSongInfo.score * 100).toInt()
//                                    }
//                                    EventBus.getDefault().post(LyricAndAccMatchManager.ScoreResultEvent("preOpWhenSelfRound", -1, mAcrScore, 0))
//                                }
//                                .build())
//                    } else {
//
//                    }
//                }
//            }
//        }
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
        MyLog.d(TAG, "onRaceRoundChangeEvent = $event")
        if (event.lastRound != null) {
            DebugLogView.println(TAG, "上一轮结果 overReason = ${event.lastRound?.overReason} " +
                    "subReason1 = ${event.lastRound?.subRoundInfo.getOrNull(0)?.overReason} " +
                    "subReason2 = ${event.lastRound?.subRoundInfo.getOrNull(1)?.overReason} " +
                    "票数 ${event.lastRound?.scores.getOrNull(0)?.bLightCnt}:${event.lastRound?.scores.getOrNull(1)?.bLightCnt} " +
                    "win ${event.lastRound?.scores.getOrNull(0)?.winType}:${event.lastRound?.scores.getOrNull(1)?.winType}")

        }
        DebugLogView.println(TAG, "新一轮 roundSeq=${event.thisRound?.roundSeq}")
        processStatusChange(1, event.lastRound, event.thisRound)
    }

    /**
     * 轮次内 状态切换事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RaceRoundStatusChangeEvent) {
        MyLog.d(TAG, "onRaceRoundStatusChangeEvent = $event")
        processStatusChange(2, null, event.thisRound)
    }

    /**
     * 轮次内 演唱阶段子轮次切换事件
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: RaceSubRoundChangeEvent) {
        MyLog.d(TAG, "onRaceSubRoundChangeEvent = $event")
        processStatusChange(3, null, event.thisRound)
    }

    private fun processStatusChange(from: Int, lastRound: RaceRoundInfoModel?, thisRound: RaceRoundInfoModel?) {
        DebugLogView.println(TAG, "状态更新 from = $from, status = ${thisRound?.status} subRoundSeq = ${thisRound?.subRoundSeq}")
        mUiHandler.removeMessages(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS)
        closeEngine()
        ZqEngineKit.getInstance().stopRecognize()
        if (thisRound?.status == ERaceRoundStatus.ERRS_WAITING.value) {
            if (lastRound != null) {
                // 有上一轮，等待中要飞过来
                mIRaceRoomView.showRoundOver(lastRound) {
                    //如果我是上一轮的演唱者，要退出房间
                    if (lastRound.isSingerByUserId(MyUserInfoManager.getInstance().uid.toInt())) {
                        goResultPage(lastRound)
                    } else {
                        mIRaceRoomView.showWaiting(true)
                    }
                }
            } else {
                mIRaceRoomView.showWaiting(false)
            }
        } else if (thisRound?.status == ERaceRoundStatus.ERRS_CHOCING.value) {
            if (lastRound != null) {
                // 有上一轮，等待中要飞过来
                mIRaceRoomView.showRoundOver(lastRound) {
                    //如果我是上一轮的演唱者，要退出房间
                    if (lastRound.isSingerByUserId(MyUserInfoManager.getInstance().uid.toInt())) {
                        goResultPage(lastRound)
                    } else {
                        mIRaceRoomView.showChoicing(true)
                    }
                }
            } else {
                mIRaceRoomView.showChoicing(false)
            }
        } else if (thisRound?.status == ERaceRoundStatus.ERRS_ONGOINE.value) {
            if (thisRound?.subRoundSeq == 1) {
                tryDownloadAccIfSelfSing()
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
//            mIRaceRoomView.roundOver(thisRound?.overReason)
        }
    }

    private fun tryDownloadAccIfSelfSing() {
        /**
         * 有的网络伴奏在线播有问题，比如公司网络，这里尝试提前下载一下伴奏
         * 下载第一轮伴奏来不及，在第一轮下载第二轮伴奏才差不多
         */
        var accUrl: String? = null
//        if (mRoomData?.realRoundInfo?.subRoundInfo?.getOrNull(0)?.userID == MyUserInfoManager.getInstance().uid.toInt()) {
//            // 第一轮是自己唱
//            if (mRoomData?.realRoundInfo?.isAccRoundBySubRoundSeq(1) == true) {
//                // 第一轮是伴奏演唱
//                mRoomData?.realRoundInfo?.subRoundInfo?.getOrNull(0)?.choiceID?.let {
//                    val songModel = mRoomData?.realRoundInfo?.getSongModelByChoiceId(it)
//                    songModel?.acc?.let {
//                        accUrl = it
//                    }
//                }
//            }
//        }
        if (mRoomData?.realRoundInfo?.subRoundInfo?.getOrNull(1)?.userID == MyUserInfoManager.getInstance().uid.toInt()) {
            // 第一轮是自己唱
            if (mRoomData?.realRoundInfo?.isAccRoundBySubRoundSeq(2) == true) {
                // 第一轮是伴奏演唱
                mRoomData?.realRoundInfo?.subRoundInfo?.getOrNull(1)?.choiceID?.let {
                    val songModel = mRoomData?.realRoundInfo?.getSongModelByChoiceId(it)
                    songModel?.acc?.let {
                        accUrl = it
                    }
                }
            }
        }
        accUrl?.let {
            //尝试下载伴奏
            launch(Dispatchers.IO) {
                val f = SongResUtils.getAccFileByUrl(it)
                if (f != null && !f.exists()) {
                    if (!U.getHttpUtils().isDownloading(it)) {
                        MyLog.d(TAG, "tryDownloadAccIfSelfSing 开始下载伴奏 acc=${it}")
                        U.getHttpUtils().downloadFileSync(it, f, true, null)
                    }
                }
            }
        }

    }

    /**
     * 用户选择了某个歌曲
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: RWantSingChanceEvent) {
        MyLog.d(TAG, "onEvent event = ${event.pb}")
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
        DebugLogView.println(TAG, "RGetSingChanceEvent 确定演唱者 ${event.pb.currentRound.subRoundInfoList.getOrNull(0)?.userID} pk ${event.pb.currentRound.subRoundInfoList.getOrNull(1)?.userID}")
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
        DebugLogView.println(TAG, "RJoinNoticeEvent ${event.pb.user.userID} 加入房间")
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
        mIRaceRoomView.joinNotice(UserInfoModel.parseFromPB(event.pb.user))
    }

    /**
     * 有人退出了房间
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: RExitGameEvent) {
        DebugLogView.println(TAG, "RExitGameEvent ${event.pb.userID} 退出房间")
        ensureInRcRoom()
        mRoomData.realRoundInfo?.exitUser(event.pb.userID)
    }

    /**
     * 用户爆灯投票
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: RBLightEvent) {
        DebugLogView.println(TAG, "RBLightEvent ${event.pb.userID} 爆灯")
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
        if (event.pb.overType == ERoundOverType.EROT_MAIN_ROUND_OVER) {
            DebugLogView.println(TAG, "RRoundOverEvent 主轮次结束 reason=${event.pb.currentRound.overReason}")
            // 主轮次结束
            val curRoundInfo = parseFromRoundInfoPB(event.pb.currentRound)
            val nextRoundInfo = parseFromRoundInfoPB(event.pb.nextRound)
            event.pb.gamesList.forEach {
                nextRoundInfo.games.add(parseFromGameInfoPB(it))
            }
            if (curRoundInfo.roundSeq == mRoomData.realRoundSeq) {
                mRoomData.realRoundInfo?.tryUpdateRoundInfoModel(curRoundInfo, false)
                mRoomData.expectRoundInfo = nextRoundInfo
                mRoomData.checkRoundInEachMode()
            }
            // 第二轮次结束原因
            if (curRoundInfo.subRoundInfo.getOrNull(1)?.overReason == ESubRoundOverReason.ESROR_SELF_GIVE_UP.value) {
                pretendGiveUp(mRoomData.getUserInfo(curRoundInfo.subRoundInfo.getOrNull(1)?.userID))
            }
        } else if (event.pb.overType == ERoundOverType.EROT_SUB_ROUND_OVER) {
            DebugLogView.println(TAG, "RRoundOverEvent 子轮次结束 reason=${event.pb.currentRound.subRoundInfoList.getOrNull(0)?.overReason}")
            val curRoundInfo = parseFromRoundInfoPB(event.pb.currentRound)
            mRoomData.realRoundInfo?.tryUpdateRoundInfoModel(curRoundInfo, true)
            // 第一轮次结束原因
            if (event.pb.currentRound.subRoundInfoList.getOrNull(0)?.overReason?.value == ESubRoundOverReason.ESROR_SELF_GIVE_UP.value) {
                pretendGiveUp(mRoomData.getUserInfo(event.pb.currentRound.subRoundInfoList.getOrNull(0)?.userID))
            }
        }
    }

    private fun pretendGiveUp(userInfoModel: UserInfoModel?) {
        if (userInfoModel != null) {
            val commentModel = CommentTextModel()
            commentModel.userInfo = userInfoModel
            commentModel.avatarColor = CommentModel.AVATAR_COLOR
            val stringBuilder: SpannableStringBuilder
            val spanUtils = SpanUtils()
                    .append(userInfoModel.nicknameRemark + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                    .append("不唱了").setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
            stringBuilder = spanUtils.create()
            commentModel.stringBuilder = stringBuilder
            EventBus.getDefault().post(PretendCommentMsgEvent(commentModel))
        }
    }


    var syncJob: Job? = null

    fun startSyncRaceStatus() {
        syncJob?.cancel()
        syncJob = launch {
            while (true) {
                delay(10 * 1000)
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
    }

    /**
     * 收到服务器的push sync
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: RSyncStatusEvent) {
        DebugLogView.println(TAG, "RSyncStatusEvent")
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
            // 主轮次结束
            launch {
                if (raceRoundInfoModel.games.isEmpty()) {
                    val result = subscribe { raceRoomServerApi.getGameChoices(mRoomData.gameId, raceRoundInfoModel.roundSeq) }
                    if (result.errno == 0) {
                        val games = JSON.parseArray(result.data.getString("games"), RaceGamePlayInfo::class.java)
                        raceRoundInfoModel.games.addAll(games)
                    } else {

                    }
                }
                mRoomData.expectRoundInfo = raceRoundInfoModel
                mRoomData.checkRoundInEachMode()
            }

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

    /**
     * 成功切换为主播
     */
    private fun onChangeBroadcastSuccess() {
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
            MyLog.d(TAG, "onChangeBroadcastSuccess 我的演唱环节 info=${songModel.toSimpleString()} acc=${songModel.acc} midi=${songModel.midi} accRound=${mRoomData?.realRoundInfo?.isAccRoundNow()} mRoomData.isAccEnable=${mRoomData.isAccEnable}")
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