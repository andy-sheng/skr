package com.module.playways.grab.room.presenter

import android.animation.ValueAnimator
import android.os.Handler
import android.os.Message
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import com.alibaba.fastjson.JSON
import com.common.base.BaseActivity
import com.common.core.account.UserAccountManager
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.engine.ScoreConfig
import com.common.jiguang.JiGuangPush
import com.common.log.DebugLogView
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.player.AndroidMediaPlayer
import com.common.player.ExoPlayer
import com.common.player.IPlayer
import com.common.player.PlayerCallbackAdapter
import com.common.rxretrofit.*
import com.common.statistics.StatisticsAdapter
import com.common.utils.ActivityUtils
import com.common.utils.HandlerTaskTimer
import com.common.utils.SpanUtils
import com.common.utils.U
import com.common.view.AnimateClickListener
import com.component.busilib.constans.GrabRoomType
import com.component.busilib.recommend.RA
import com.component.lyrics.LyricAndAccMatchManager
import com.component.lyrics.utils.SongResUtils
import com.component.lyrics.utils.ZipUrlResourceManager
import com.dialog.view.TipsDialogView
import com.engine.EngineEvent
import com.engine.Params
import com.engine.arccloud.RecognizeConfig
import com.module.ModuleServiceManager
import com.module.common.ICallback
import com.module.msg.CustomMsgType
import com.module.playways.BuildConfig
import com.module.playways.RoomDataUtils
import com.module.playways.event.GrabChangeRoomEvent
import com.module.playways.grab.room.GrabResultData
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.grab.room.GrabRoomServerApi
import com.module.playways.grab.room.event.*
import com.module.playways.grab.room.inter.IGrabRoomView
import com.module.playways.grab.room.model.*
import com.module.playways.race.room.model.LevelResultModel
import com.module.playways.room.gift.event.GiftBrushMsgEvent
import com.module.playways.room.gift.event.UpdateCoinEvent
import com.module.playways.room.gift.event.UpdateMeiliEvent
import com.module.playways.room.msg.event.*
import com.module.playways.room.msg.filter.PushMsgFilter
import com.module.playways.room.msg.manager.ChatRoomMsgManager
import com.module.playways.room.prepare.model.JoinGrabRoomRspModel
import com.module.playways.room.room.SwapStatusType
import com.module.playways.room.room.comment.model.CommentLightModel
import com.module.playways.room.room.comment.model.CommentModel
import com.module.playways.room.room.comment.model.CommentSysModel
import com.module.playways.room.room.comment.model.CommentTextModel
import com.module.playways.room.room.event.PretendCommentMsgEvent
import com.module.playways.room.room.score.MachineScoreItem
import com.module.playways.room.room.score.RobotScoreHelper
import com.module.playways.room.song.model.SongModel
import com.module.playways.songmanager.event.MuteAllVoiceEvent
import com.module.playways.songmanager.event.RoomNameChangeEvent
import com.orhanobut.dialogplus.DialogPlus
import com.orhanobut.dialogplus.ViewHolder
import com.zq.live.proto.Common.ESex
import com.zq.live.proto.Common.StandPlayType
import com.zq.live.proto.Common.UserInfo
import com.zq.live.proto.Room.*
import com.zq.mediaengine.kit.ZqEngineKit
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.greenrobot.greendao.annotation.NotNull
import java.util.*

class GrabCorePresenter(@param:NotNull internal var mIGrabView: IGrabRoomView, @param:NotNull internal var mRoomData: GrabRoomData, internal var mBaseActivity: BaseActivity) : RxLifeCyclePresenter() {

    internal var mFirstKickOutTime: Long = -1 //用时间和次数来判断一个人有没有在一个房间里

    internal var mAbsenTimes = 0

    internal var mRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi::class.java)

    internal var mSyncGameStateTask: HandlerTaskTimer? = null

    internal var mRobotScoreHelper: RobotScoreHelper? = null

    internal var mDestroyed = false

    internal var mExoPlayer: IPlayer? = null

    internal var mSwitchRooming = false

    internal var mGrabRedPkgPresenter: GrabRedPkgPresenter? = null

    internal var mZipUrlResourceManager: ZipUrlResourceManager? = null

    internal var mEngineParamsTemp: EngineParamsTemp? = null

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
                MSG_ROBOT_SING_BEGIN -> robotSingBegin()
                MSG_ENSURE_SWITCH_BROADCAST_SUCCESS -> onChangeBroadcastSuccess()
                MSG_RECOVER_VOLUME -> {
                    if (mEngineParamsTemp != null) {
                        ZqEngineKit.getInstance().adjustAudioMixingPlayoutVolume(mEngineParamsTemp!!.audioVolume, false)
                        ZqEngineKit.getInstance().adjustRecordingSignalVolume(mEngineParamsTemp!!.recordVolume, false)

                        if (ZqEngineKit.getInstance().params.isAnchor) {
                            val audioVolume = ZqEngineKit.getInstance().params.audioMixingPlayoutVolume
                            val recordVolume = ZqEngineKit.getInstance().params.recordingSignalVolume
                            ZqEngineKit.getInstance().adjustAudioMixingPlayoutVolume(audioVolume, false)
                            ZqEngineKit.getInstance().adjustRecordingSignalVolume(recordVolume, false)
                        } else {
                            MyLog.d(TAG, "我不是主播，忽略")
                        }
                        mEngineParamsTemp = null
                    }
                    if (mExoPlayer != null) {
                        val valueAnimator = ValueAnimator.ofFloat(0f, mExoPlayer!!.volume)
                        valueAnimator.addUpdateListener { animation ->
                            val v = animation.animatedValue as Float
                            if (mExoPlayer != null) {
                                mExoPlayer!!.setVolume(v, false)
                            }
                        }
                        valueAnimator.duration = 1000
                        valueAnimator.start()
                    }
                }
                MSG_ENSURE_EXIT -> if (mIGrabView != null) {
                    mIGrabView.onGetGameResult(false)
                }
            }
        }
    }

    internal var mPushMsgFilter: PushMsgFilter<*> = PushMsgFilter<RoomMsg> { msg ->
        msg != null && msg.roomID == mRoomData.gameId
    }

    internal var mGrabSongResPresenter: GrabSongResPresenter? = GrabSongResPresenter()

    init {
        ChatRoomMsgManager.getInstance().addFilter(mPushMsgFilter)
        joinRoomAndInit(true)
        U.getFileUtils().deleteAllFiles(U.getAppInfoUtils().getSubDirPath("WonderfulMoment"))
    }

    fun setGrabRedPkgPresenter(grabRedPkgPresenter: GrabRedPkgPresenter) {
        mGrabRedPkgPresenter = grabRedPkgPresenter
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
            if (!reInit && ZqEngineKit.getInstance().params.isEnableVideo != mRoomData.isVideoRoom) {
                MyLog.d(TAG, "音视频模式发生切换")
                mIGrabView.changeRoomMode(mRoomData.isVideoRoom)
                // 发出通知
                reInit = true
            }
            if (reInit) {
                val params = Params.getFromPref()
                //            params.setStyleEnum(Params.AudioEffect.none);
                params.scene = Params.Scene.grab
                params.isEnableAudio = true
                params.isEnableVideo = mRoomData.isVideoRoom
                ZqEngineKit.getInstance().init("grabroom", params)
            }
            ZqEngineKit.getInstance().joinRoom(mRoomData.gameId.toString(), UserAccountManager.getInstance().uuidAsLong.toInt(), false, mRoomData.agoraToken)
            // 不发送本地音频, 会造成第一次抢没声音
            ZqEngineKit.getInstance().muteLocalAudioStream(true)
            if (mRoomData.isVideoRoom) {
                ZqEngineKit.getInstance().unbindAllRemoteVideo()
            }
        }
        joinRcRoom(-1)
        if (mRoomData.gameId > 0) {
            for (playerInfoModel in mRoomData.getPlayerAndWaiterInfoList()) {
                if (!playerInfoModel.isOnline) {
                    continue
                }
                pretendEnterRoom(playerInfoModel)
            }
            if (mRoomData.roomType == GrabRoomType.ROOM_TYPE_PLAYBOOK) {
                pretendRoomNameSystemMsg(mRoomData.roomName, CommentSysModel.TYPE_ENTER_ROOM_PLAYBOOK)
            } else {
                pretendRoomNameSystemMsg(mRoomData.roomName, CommentSysModel.TYPE_ENTER_ROOM)
            }
        }
        if (mRoomData.hasGameBegin()) {
            startSyncGameStateTask(sSyncStateTaskInterval)
        } else {
            cancelSyncGameStateTask()
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

    override fun start() {
        super.start()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    /**
     * 由ui层告知
     * 开场动画结束
     */
    fun onOpeningAnimationOver() {
        // 开始触发触发轮次变化
        if (mRoomData.hasGameBegin()) {
            mRoomData.checkRoundInEachMode()
            ensureInRcRoom()
        } else {
            MyLog.d(TAG, "onOpeningAnimationOver 游戏未开始")
        }
    }

    /**
     * 播放导唱
     */
    fun playGuide() {
        if (mDestroyed) {
            return
        }
        val now = mRoomData.realRoundInfo
        if (now != null) {
            if (mExoPlayer == null) {
                mExoPlayer = AndroidMediaPlayer()
                if (mRoomData.isMute || !U.getActivityUtils().isAppForeground) {
                    mExoPlayer!!.volume = 0f
                } else {
                    mExoPlayer!!.volume = 1f
                }
            }
            mExoPlayer?.setCallback(object : PlayerCallbackAdapter() {
                override fun onPrepared() {
                    super.onPrepared()
                    if (!now.isParticipant && now.enterStatus == EQRoundStatus.QRS_INTRO.value) {
                        MyLog.d(TAG, "这轮刚进来，导唱需要seek")
                        mExoPlayer!!.seekTo(now.elapsedTimeMs.toLong())
                    }
                }
            })
            mExoPlayer?.startPlay(now.music.standIntro)
        }
    }

    /**
     * 停止播放导唱
     */
    fun stopGuide() {
        mExoPlayer?.stop()
    }

    /**
     * 如果确定是自己唱了,预先可以做的操作
     */
    internal fun preOpWhenSelfRound() {
        var needAcc = false
        var needScore = false
        val now = mRoomData.realRoundInfo
        var songModel: SongModel? = null
        if (now != null) {
            songModel = now.music
            if (now.status == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.value) {
                songModel = songModel!!.pkMusic
            }
            val p = ZqEngineKit.getInstance().params
            if (p != null) {
                p.isGrabSingNoAcc = false
            }
            if (now.wantSingType == EWantSingType.EWST_SPK.value) {
                needAcc = true
                needScore = true
            } else if (now.wantSingType == EWantSingType.EWST_CHORUS.value) {
                needAcc = false
                needScore = false
            } else if (now.wantSingType == EWantSingType.EWST_MIN_GAME.value) {
                needAcc = false
                needScore = false
            } else if (mRoomData.isAccEnable && songModel != null && !TextUtils.isEmpty(songModel.acc)) {
                needAcc = true
                needScore = true
            } else {
                if (p != null) {
                    p.isGrabSingNoAcc = true
                    needScore = true
                }
            }
        }
        if (needAcc) {
            // 1. 开启伴奏的，预先下载 melp 资源
            if (songModel != null) {
                val midiFile = SongResUtils.getMIDIFileByUrl(songModel.midi)
                if (midiFile != null && !midiFile.exists()) {
                    U.getHttpUtils().downloadFileAsync(now!!.music.midi, midiFile, true, null)
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

        // 开始acr打分
        if (ScoreConfig.isAcrEnable() && now != null && now.music != null) {
            if (needAcc) {
                ZqEngineKit.getInstance().startRecognize(RecognizeConfig.newBuilder()
                        .setSongName(now.music.itemName)
                        .setArtist(now.music.owner)
                        .setMode(RecognizeConfig.MODE_MANUAL)
                        .build())
            } else {
                if (needScore) {
                    // 清唱还需要打分，那就只用 acr 打分
                    ZqEngineKit.getInstance().startRecognize(RecognizeConfig.newBuilder()
                            .setSongName(now.music.itemName)
                            .setArtist(now.music.owner)
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

    fun preOpWhenOtherRound(uid: Int) {
        val playerInfo = RoomDataUtils.getPlayerInfoById(mRoomData, uid)
        if (playerInfo == null) {
            MyLog.w(TAG, "切换别人的时候PlayerInfo为空")
            return
        }
        /**
         * 机器人
         */
        if (playerInfo.isSkrer) {
            MyLog.d(TAG, "checkMachineUser uid=$uid is machine")
            //这个时间现在待定
            //移除之前的要发生的机器人演唱
            mUiHandler.removeMessages(MSG_ROBOT_SING_BEGIN)
            val message = mUiHandler.obtainMessage(MSG_ROBOT_SING_BEGIN)
            mUiHandler.sendMessage(message)
        }

        // 别人的轮次
        //        if (mRoomData.isVideoRoom()) {
        //            // 如果是语音房间
        //            GrabRoundInfoModel infoModel = mRoomData.getRealRoundInfo();
        //            if (infoModel != null) {
        //                if (infoModel.isPKRound()) {
        //                    if (infoModel.getsPkRoundInfoModels().size() >= 2) {
        //                        int userId1 = infoModel.getsPkRoundInfoModels().get(0).getUserID();
        //                        int userId2 = infoModel.getsPkRoundInfoModels().get(1).getUserID();
        //                        if (MyUserInfoManager.getInstance().getUid() == userId1 ||
        //                                MyUserInfoManager.getInstance().getUid() == userId2) {
        //                            // 万一这个人是一个人 这个人点不唱了
        //                            //join房间也变成主播
        //                            if (!ZqEngineKit.getInstance().getParams().isAnchor()) {
        //                                ZqEngineKit.getInstance().setClientRole(true);
        //                            }
        //                            // 不发声
        //                            ZqEngineKit.getInstance().muteLocalAudioStream(true);
        //                        }
        //                    }
        //                }
        //            }
        //        }
    }

    /**
     * 真正打开引擎开始演唱
     */
    fun beginSing() {
        // 打开引擎，变为主播
        val now = mRoomData.realRoundInfo
        if (mRoomData.openAudioRecording()) {
            // 需要上传音频伪装成机器人
            if (now != null && !now.isMiniGameRound) {
                val fileName = String.format("wm_%s_%s.aac", mRoomData.gameId, now.roundSeq)
                val savePath = U.getAppInfoUtils().getFilePathInSubDir("WonderfulMoment", fileName)
                ZqEngineKit.getInstance().startAudioRecording(savePath, false)
            }
        }

        /**
         * if (now != null) {
         * if (mRobotScoreHelper == null) {
         * mRobotScoreHelper = new RobotScoreHelper();
         * }
         * mRobotScoreHelper.reset();
         * }
         */


    }

    /**
     * 房主点击开始游戏
     */
    fun ownerBeginGame() {
        MyLog.d(TAG, "ownerBeginGame")
        val map = HashMap<String, Any>()
        map["roomID"] = mRoomData.gameId

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mRoomServerApi.ownerBeginGame(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val rsp = JSON.parseObject(result.data!!.toJSONString(), JoinGrabRoomRspModel::class.java)
                    // 模拟服务器push，触发游戏更新
                    val event = QGameBeginEvent()
                    event.roomID = rsp.roomID
                    event.mGrabConfigModel = rsp.config
                    event.mInfoModel = rsp.currentRound
                    onEvent(event)
                }
            }

            override fun onError(e: Throwable) {

            }
        }, this, RequestControl("ownerBeginGame", ControlType.CancelThis))
    }

    /**
     * 抢唱歌权
     */
    fun grabThisRound(seq: Int, challenge: Boolean) {
        MyLog.d(TAG, "grabThisRound" + " seq=" + seq + " challenge=" + challenge + " accenable=" + mRoomData.isAccEnable)


        val infoModel = mRoomData.realRoundInfo
        if (infoModel != null) {
            if (infoModel.wantSingInfos.contains(WantSingerInfo(MyUserInfoManager.getInstance().uid.toInt()))) {
                MyLog.w(TAG, "grabThisRound cancel 想唱列表中已经有你了")
                return
            }
        }

        val map = HashMap<String, Any>()
        map["roomID"] = mRoomData.gameId
        map["roundSeq"] = seq


        var songModel: SongModel? = null
        if (infoModel != null && infoModel.music != null) {
            songModel = infoModel.music
        }

        var preAccUrl = ""

        val wantSingType: Int
        // 根据玩法决定抢唱类型
        if (songModel != null && songModel.playType == StandPlayType.PT_SPK_TYPE.value) {
            wantSingType = EWantSingType.EWST_SPK.value
            if (infoModel != null) {
                if (infoModel.wantSingInfos.isEmpty()) {
                    // 自己大概率第一个唱
                    if (infoModel.music != null) {
                        preAccUrl = infoModel.music.acc
                    }
                } else {
                    //  自己大概率不是第一个唱
                    if (infoModel.music != null) {
                        val pkSongModel = infoModel.music.pkMusic
                        if (pkSongModel != null) {
                            preAccUrl = pkSongModel.acc
                        }
                    }
                }
            }
        } else if (songModel != null && songModel.playType == StandPlayType.PT_CHO_TYPE.value) {
            wantSingType = EWantSingType.EWST_CHORUS.value
        } else if (songModel != null && songModel.playType == StandPlayType.PT_MINI_GAME_TYPE.value) {
            wantSingType = EWantSingType.EWST_MIN_GAME.value
        } else {
            if (challenge) {
                if (mRoomData.getCoin() < 1) {
                    MyLog.w(TAG, "没有充足金币,无法进行挑战")
                    U.getToastUtil().showShort("没有充足的金币")
                    return
                }
                if (mRoomData.isAccEnable && songModel != null && !TextUtils.isEmpty(songModel.acc)) {
                    wantSingType = EWantSingType.EWST_ACCOMPANY_OVER_TIME.value
                    preAccUrl = songModel.acc
                } else {
                    wantSingType = EWantSingType.EWST_COMMON_OVER_TIME.value
                }
            } else {
                if (mRoomData.isAccEnable && songModel != null && !TextUtils.isEmpty(songModel.acc)) {
                    wantSingType = EWantSingType.EWST_ACCOMPANY.value
                    preAccUrl = songModel.acc
                } else {
                    wantSingType = EWantSingType.EWST_DEFAULT.value
                }
            }
        }

        map["wantSingType"] = wantSingType
        map["hasPassedCertify"] = MyUserInfoManager.getInstance().hasGrabCertifyPassed()

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mRoomServerApi.wangSingChance(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                MyLog.w(TAG, "grabThisRound erro code is " + result.errno + ",traceid is " + result.traceId)
                if (result.errno == 0) {
                    //true为已经认证过了或者无需认证，false为未认证
                    val mHasPassedCertify = result.data!!.getBoolean("hasPassedCertify")!!

                    if (mHasPassedCertify) {
                        MyUserInfoManager.getInstance().setGrabCertifyPassed(mHasPassedCertify)
                        //抢成功了
                        val now = mRoomData.realRoundInfo
                        if (now != null && now.roundSeq == seq) {
                            val wantSingerInfo = WantSingerInfo()
                            wantSingerInfo.wantSingType = wantSingType
                            wantSingerInfo.userID = MyUserInfoManager.getInstance().uid.toInt()
                            wantSingerInfo.timeMs = System.currentTimeMillis()
                            now.addGrabUid(true, wantSingerInfo)

                            if (result.data!!.getBoolean("success")!!) {
                                val coin = result.data!!.getIntValue("coin")
                                mRoomData.setCoin(coin)
                            }
                        } else {
                            MyLog.w(TAG, "now != null && now.getRoundSeq() == seq 条件不满足，" + result.traceId)
                        }
                    } else {
                        if (mDialogPlus != null) {
                            mDialogPlus!!.dismiss()
                        }

                        val tipsDialogView = TipsDialogView.Builder(mBaseActivity)
                                .setMessageTip("亲～实名认证通过后即可参与抢唱啦！")
                                .setConfirmTip("立即认证")
                                .setCancelTip("残忍拒绝")
                                .setConfirmBtnClickListener(object : AnimateClickListener() {
                                    override fun click(view: View) {
                                        if (mDialogPlus != null) {
                                            mDialogPlus!!.dismiss()
                                        }
                                        mIGrabView.beginOuath()
                                    }
                                })
                                .setCancelBtnClickListener(object : AnimateClickListener() {
                                    override fun click(view: View) {
                                        if (mDialogPlus != null) {
                                            mDialogPlus!!.dismiss()
                                        }
                                    }
                                })
                                .build()

                        mDialogPlus = DialogPlus.newDialog(mBaseActivity)
                                .setContentHolder(ViewHolder(tipsDialogView))
                                .setGravity(Gravity.BOTTOM)
                                .setContentBackgroundResource(com.component.busilib.R.color.transparent)
                                .setOverlayBackgroundResource(com.component.busilib.R.color.black_trans_80)
                                .setExpanded(false)
                                .create()
                        mDialogPlus!!.show()
                    }
                } else if (result.errno == 8346144) {
                    MyLog.w(TAG, "grabThisRound failed 没有充足金币 ")
                    U.getToastUtil().showShort(result.errmsg)
                } else {
                    MyLog.w(TAG, "grabThisRound failed, " + result.traceId)
                }
            }

            override fun onError(e: Throwable) {
                MyLog.e(TAG, "grabThisRound error $e")

            }
        }, this)

        if (!TextUtils.isEmpty(preAccUrl)) {
            mGrabSongResPresenter!!.tryDownloadAcc(preAccUrl)
        }
    }

    /**
     * 灭灯
     */
    fun lightsOff() {
        val now = mRoomData.realRoundInfo ?: return
        if (!now.isSingStatus) {
            MyLog.d(TAG, "lightsOff 不在演唱状态，cancel status=" + now.status + " roundSeq=" + now.roundSeq)
            return
        }
        val roundSeq = now.roundSeq
        val map = HashMap<String, Any>()
        map["roomID"] = mRoomData.gameId
        map["roundSeq"] = roundSeq
        if (now.status == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.value) {
            map["subRoundSeq"] = 0
        } else if (now.status == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.value) {
            map["subRoundSeq"] = 1
        }
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mRoomServerApi.lightOff(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                MyLog.e(TAG, "lightsOff erro code is " + result.errno + ",traceid is " + result.traceId)
                if (result.errno == 0) {
                    val now = mRoomData.realRoundInfo
                    if (now != null && now.roundSeq == roundSeq) {
                        val noPassingInfo = MLightInfoModel()
                        noPassingInfo.userID = MyUserInfoManager.getInstance().uid.toInt()
                        now.addLightOffUid(true, noPassingInfo)
                    }
                } else {

                }
            }

            override fun onError(e: Throwable) {
                MyLog.e(TAG, "lightsOff error $e")

            }
        }, this)
    }

    /**
     * 爆灯
     */
    fun lightsBurst() {
        val now = mRoomData.realRoundInfo ?: return
        if (!now.isSingStatus) {
            MyLog.d(TAG, "lightsBurst 不在演唱状态，cancel status=" + now.status + " roundSeq=" + now.roundSeq)
            return
        }
        if (RA.hasTestList()) {
            val map = HashMap<String, String>()
            map.put("testList", RA.getTestList())
            StatisticsAdapter.recordCountEvent("ra", "burst", map)
        }
        val map = HashMap<String, Any>()
        map["roomID"] = mRoomData.gameId
        val roundSeq = now.roundSeq
        map["roundSeq"] = mRoomData.realRoundSeq

        if (now.status == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.value) {
            map["subRoundSeq"] = 0
        } else if (now.status == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.value) {
            map["subRoundSeq"] = 1
        }
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mRoomServerApi.lightBurst(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                MyLog.e(TAG, "lightsBurst erro code is " + result.errno + ",traceid is " + result.traceId)
                if (result.errno == 0) {
                    val now = mRoomData.realRoundInfo
                    if (now != null && now.roundSeq == roundSeq) {
                        val coin = result.data!!.getIntValue("coin")
                        mRoomData.setCoin(coin)
                        if (result.data!!.getBoolean("isBLightSuccess")!!) {
                            val m = BLightInfoModel()
                            m.userID = MyUserInfoManager.getInstance().uid.toInt()
                            now.addLightBurstUid(true, m)
                        } else {
                            val reason = result.data!!.getString("bLightFailedMsg")
                            if (!TextUtils.isEmpty(reason)) {
                                U.getToastUtil().showShort(reason)
                            }
                        }
                    }
                } else {

                }
            }

            override fun onError(e: Throwable) {
                MyLog.e(TAG, "lightsOff error $e")

            }
        }, this)
    }

    private fun robotSingBegin() {
        var skrerUrl: String? = null
        val grabRoundInfoModel = mRoomData.realRoundInfo
        if (grabRoundInfoModel != null) {
            val grabSkrResourceModel = grabRoundInfoModel.skrResource
            if (grabSkrResourceModel != null) {
                skrerUrl = grabSkrResourceModel.audioURL
            }
        }
        if (mRobotScoreHelper == null) {
            mRobotScoreHelper = RobotScoreHelper()
        }

        if (mExoPlayer == null) {
            mExoPlayer = ExoPlayer()
        }
        mExoPlayer!!.startPlay(skrerUrl)
        mExoPlayer!!.setCallback(object : PlayerCallbackAdapter() {
            override fun onPrepared() {
                if (!grabRoundInfoModel!!.isParticipant && grabRoundInfoModel.enterStatus == EQRoundStatus.QRS_SING.value) {
                    MyLog.d(TAG, "进来时已经时演唱阶段了，则机器人资源要seek一下 " + grabRoundInfoModel.elapsedTimeMs)
                    mExoPlayer!!.seekTo(grabRoundInfoModel.elapsedTimeMs.toLong())
                }
            }
        })
        if (mRoomData.isMute || !U.getActivityUtils().isAppForeground) {
            mExoPlayer!!.volume = 0f
        } else {
            mExoPlayer!!.volume = 1f
        }
    }

    private fun tryStopRobotPlay() {
        if (mExoPlayer != null) {
            mExoPlayer!!.reset()
        }
    }

    fun muteAllRemoteAudioStreams(mute: Boolean, fromUser: Boolean) {
        if (fromUser) {
            mRoomData.isMute = mute
        }
        ZqEngineKit.getInstance().muteAllRemoteAudioStreams(mute)
        // 如果是机器人的话
        if (mute) {
            // 如果是静音
            if (mExoPlayer != null) {
                mExoPlayer!!.setMuteAudio(true)
            }
        } else {
            // 如果打开静音
            if (mExoPlayer != null) {
                mExoPlayer!!.setMuteAudio(false)
            }
        }
    }

    /**
     * 自己的轮次结束了
     *
     * @param roundInfoModel
     */
    private fun onSelfRoundOver(roundInfoModel: GrabRoundInfoModel) {
        // 上一轮演唱是自己，开始上传资源
        //        if (SkrConfig.getInstance().isNeedUploadAudioForAI(GameModeType.GAME_MODE_GRAB)) {
        //            //属于需要上传音频文件的状态
        //            // 上一轮是我的轮次，暂停录音
        //            if (mRoomData.getGameId() > 0) {
        //                ZqEngineKit.getInstance().stopAudioRecording();
        //            }
        //            // 上传打分
        //            if (mRobotScoreHelper != null) {
        //                if (mRobotScoreHelper.isScoreEnough()) {
        //                    if (roundInfoModel.getOverReason() == EQRoundOverReason.ROR_LAST_ROUND_OVER.getValue()
        //                            && roundInfoModel.getResultType() == EQRoundResultType.ROT_TYPE_1.getValue()) {
        //                        // 是一唱到底的才上传
        //                        roundInfoModel.setSysScore(mRobotScoreHelper.getAverageScore());
        //                        uploadRes1ForAi(roundInfoModel);
        //                    } else {
        //                        MyLog.d(TAG, "没有唱到一唱到底不上传");
        //                    }
        //                } else {
        //                    MyLog.d(TAG, "isScoreEnough false");
        //                }
        //            }
        //        }

        if (mGrabRedPkgPresenter != null && mGrabRedPkgPresenter!!.isCanReceive) {
            mGrabRedPkgPresenter!!.getRedPkg()
        }

        if (mRoomData.openAudioRecording() && !roundInfoModel.isMiniGameRound) {
            var songModel: SongModel? = null
            var baodeng = false
            if (roundInfoModel.overReason == EQRoundOverReason.ROR_CHO_SUCCESS.value || roundInfoModel.overReason == EQRoundOverReason.ROR_LAST_ROUND_OVER.value) {
                if (roundInfoModel.resultType == EQRoundResultType.ROT_TYPE_1.value) {
                    // 一唱到底 或者是 是pk轮次，正常结束
                    songModel = roundInfoModel.music
                    baodeng = !roundInfoModel.getbLightInfos().isEmpty()
                }
            }
            if (roundInfoModel.getsPkRoundInfoModels().size == 2) {
                if (roundInfoModel.getsPkRoundInfoModels()[0].userID.toLong() == MyUserInfoManager.getInstance().uid && roundInfoModel.getsPkRoundInfoModels()[0].overReason == EQRoundOverReason.ROR_LAST_ROUND_OVER.value) {
                    // 第一轮我唱
                    songModel = roundInfoModel.music
                    baodeng = !roundInfoModel.getsPkRoundInfoModels()[0].getbLightInfos().isEmpty()
                } else if (roundInfoModel.getsPkRoundInfoModels()[1].userID.toLong() == MyUserInfoManager.getInstance().uid && roundInfoModel.getsPkRoundInfoModels()[1].overReason == EQRoundOverReason.ROR_LAST_ROUND_OVER.value) {
                    // 第一轮我唱
                    if (roundInfoModel.music != null) {
                        songModel = roundInfoModel.music.pkMusic
                    }
                    baodeng = !roundInfoModel.getsPkRoundInfoModels()[1].getbLightInfos().isEmpty()
                }
            }
            if (songModel != null) {
                MyLog.d(TAG, "添加到待选作品")
                val fileName = String.format("wm_%s_%s.aac", mRoomData.gameId, roundInfoModel.roundSeq)
                val savePath = U.getAppInfoUtils().getFilePathInSubDir("WonderfulMoment", fileName)
                mRoomData.addWorksUploadModel(WorksUploadModel(savePath, songModel, baodeng))
            }
        }
    }

    //    /**
    //     * 上传音频文件用作机器人
    //     *
    //     * @param roundInfoModel
    //     */
    //    private void uploadRes1ForAi(BaseRoundInfoModel roundInfoModel) {
    //        if (mRobotScoreHelper != null) {
    //            MyLog.d(TAG, "uploadRes1ForAi 开始上传资源 得分:" + roundInfoModel.getSysScore());
    //            UploadParams.newBuilder(RoomDataUtils.getSaveAudioForAiFilePath())
    //                    .setFileType(UploadParams.FileType.audioAi)
    //                    .startUploadAsync(new UploadCallback() {
    //                        @Override
    //                        public void onProgressNotInUiThread(long currentSize, long totalSize) {
    //
    //                        }
    //
    //                        @Override
    //                        public void onSuccessNotInUiThread(String url) {
    //                            MyLog.w(TAG, "uploadRes1ForAi 上传成功 url=" + url);
    //                            sendUploadRequest(roundInfoModel, url);
    //                        }
    //
    //                        @Override
    //                        public void onFailureNotInUiThread(String msg) {
    //
    //                        }
    //                    });
    //        }
    //    }

    //    /**
    //     * 上传机器人资源相关文件到服务器
    //     *
    //     * @param roundInfoModel
    //     * @param audioUrl
    //     */
    //    private void sendUploadRequest(BaseRoundInfoModel roundInfoModel, String audioUrl) {
    //        long timeMs = System.currentTimeMillis();
    //        HashMap<String, Object> map = new HashMap<>();
    //        map.put("roomID", mRoomData.getGameId());
    //        map.put("itemID", roundInfoModel.getPlaybookID());
    //        map.put("sysScore", roundInfoModel.getSysScore());
    //        map.put("audioURL", audioUrl);
    ////        map.put("midiURL", midiUrl);
    //        map.put("timeMs", timeMs);
    //        StringBuilder sb = new StringBuilder();
    //        sb.append("skrer")
    //                .append("|").append(mRoomData.getGameId())
    //                .append("|").append(roundInfoModel.getPlaybookID())
    //                .append("|").append(roundInfoModel.getSysScore())
    //                .append("|").append(audioUrl)
    ////                .append("|").append(midiUrl)
    //                .append("|").append(timeMs);
    //        String sign = U.getMD5Utils().MD5_32(sb.toString());
    //        map.put("sign", sign);
    //        RequestBody body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map));
    //        ApiMethods.subscribe(mRoomServerApi.saveRes(body), new ApiObserver<ApiResult>() {
    //            @Override
    //            public void process(ApiResult result) {
    //                if (result.getErrno() == 0) {
    //                    MyLog.e(TAG, "sendAiUploadRequest success");
    //                } else {
    //                    MyLog.e(TAG, "sendAiUploadRequest failed， errno is " + result.getErrmsg());
    //                }
    //            }
    //
    //            @Override
    //            public void onError(Throwable e) {
    //                MyLog.e(TAG, "sendUploadRequest error " + e);
    //            }
    //        }, this);
    //    }

    override fun destroy() {
        MyLog.d(TAG, "destroy begin")
        super.destroy()
        mDestroyed = true
        Params.save2Pref(ZqEngineKit.getInstance().params)
        if (mGrabSongResPresenter != null) {
            mGrabSongResPresenter!!.destroy()
        }
        if (!mRoomData.isHasExitGame) {
            exitRoom("destroy")
        }
        cancelSyncGameStateTask()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
        ZqEngineKit.getInstance().destroy("grabroom")
        mUiHandler.removeCallbacksAndMessages(null)
        ChatRoomMsgManager.getInstance().removeFilter(mPushMsgFilter)
        if (mExoPlayer != null) {
            mExoPlayer!!.release()
            mExoPlayer = null
        } else {
            MyLog.d(TAG, "mExoPlayer == null ")
        }

        if (mZipUrlResourceManager != null) {
            mZipUrlResourceManager!!.cancelAllTask()
        }
        ModuleServiceManager.getInstance().msgService.leaveChatRoom(mRoomData.gameId.toString())
        JiGuangPush.exitSkrRoomId(mRoomData.gameId.toString())
        MyLog.d(TAG, "destroy over")
    }

    /**
     * 告知我的的抢唱阶段结束了
     */
    fun sendMyGrabOver(from: String) {
        MyLog.d(TAG, "上报我的抢唱结束 from=$from")
        val roundInfoModel = mRoomData.realRoundInfo ?: return
        val map = HashMap<String, Any>()
        map["roomID"] = mRoomData.gameId
        map["roundSeq"] = roundInfoModel.roundSeq

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mRoomServerApi.sendGrapOver(body), object : ApiObserver<ApiResult>() {

            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    MyLog.w(TAG, "我的抢唱结束上报成功 traceid is " + result.traceId)
                } else {
                    MyLog.w(TAG, "我的抢唱结束上报失败 traceid is " + result.traceId)
                }
            }

            override fun onError(e: Throwable) {
                MyLog.w(TAG, "sendRoundOverInfo error $e")
            }
        }, this)
    }

    /**
     * 上报轮次结束信息
     */
    fun sendRoundOverInfo() {
        MyLog.w(TAG, "上报我的演唱结束")
        estimateOverTsThisRound()

        val roundInfoModel = mRoomData.realRoundInfo
        if (roundInfoModel == null || !roundInfoModel.singBySelf()) {
            return
        }
        val map = HashMap<String, Any>()
        map["roomID"] = mRoomData.gameId
        map["roundSeq"] = roundInfoModel.roundSeq
        if (roundInfoModel.status == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.value) {
            map["subRoundSeq"] = 0
        } else if (roundInfoModel.status == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.value) {
            map["subRoundSeq"] = 1
        }

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mRoomServerApi.sendRoundOver(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    MyLog.w(TAG, "演唱结束上报成功 traceid is " + result.traceId)
                } else {
                    MyLog.w(TAG, "演唱结束上报失败 traceid is " + result.traceId)
                }
            }

            override fun onError(e: Throwable) {
                MyLog.w(TAG, "sendRoundOverInfo error $e")
            }
        }, this)
    }


    /**
     * 放弃演唱接口
     */
    fun giveUpSing(ownerControl: Boolean) {
        if (ownerControl) {
            MyLog.w(TAG, "房主结束小游戏")
            estimateOverTsThisRound()
            val now = mRoomData.realRoundInfo
            if (now == null || !mRoomData.isOwner) {
                return
            }
            val map = HashMap<String, Any>()
            map["roomID"] = mRoomData.gameId
            map["roundSeq"] = now.roundSeq
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            if (now.isFreeMicRound) {
                ApiMethods.subscribe(mRoomServerApi.stopFreeMicroByOwner(body), object : ApiObserver<ApiResult>() {
                    override fun process(result: ApiResult) {
                        if (result.errno == 0) {
                            mIGrabView.giveUpSuccess(now.roundSeq)
                            closeEngine()
                            MyLog.w(TAG, "房主结束自由麦成功 traceid is " + result.traceId)
                        } else {
                            MyLog.w(TAG, "房主结束自由麦成功 traceid is " + result.traceId)
                        }
                    }

                    override fun onError(e: Throwable) {
                        MyLog.w(TAG, "stopFreeMicroByOwner error $e")
                    }
                }, this)
            } else {
                ApiMethods.subscribe(mRoomServerApi.stopMiniGameByOwner(body), object : ApiObserver<ApiResult>() {
                    override fun process(result: ApiResult) {
                        if (result.errno == 0) {
                            mIGrabView.giveUpSuccess(now.roundSeq)
                            closeEngine()
                            MyLog.w(TAG, "房主结束小游戏成功 traceid is " + result.traceId)
                        } else {
                            MyLog.w(TAG, "房主结束小游戏成功 traceid is " + result.traceId)
                        }
                    }

                    override fun onError(e: Throwable) {
                        MyLog.w(TAG, "stopMiniGameByOwner error $e")
                    }
                }, this)
            }

        } else {
            MyLog.w(TAG, "我放弃演唱")
            estimateOverTsThisRound()
            val now = mRoomData.realRoundInfo
            if (now == null || !now.singBySelf()) {
                return
            }
            val map = HashMap<String, Any>()
            map["roomID"] = mRoomData.gameId
            map["roundSeq"] = now.roundSeq
            if (now.music != null) {
                map["playType"] = now.music.playType
            }
            if (now.status == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.value) {
                map["subRoundSeq"] = 0
            } else if (now.status == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.value) {
                map["subRoundSeq"] = 1
            }
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            ApiMethods.subscribe(mRoomServerApi.giveUpSing(body), object : ApiObserver<ApiResult>() {
                override fun process(result: ApiResult) {
                    if (result.errno == 0) {
                        mIGrabView.giveUpSuccess(now.roundSeq)
                        closeEngine()
                        MyLog.w(TAG, "放弃演唱上报成功 traceid is " + result.traceId)
                    } else {
                        MyLog.w(TAG, "放弃演唱上报失败 traceid is " + result.traceId)
                    }
                }

                override fun onError(e: Throwable) {
                    MyLog.w(TAG, "giveUpSing error $e")
                }
            }, this)
        }
    }

    /**
     * 房主小游戏控场 开麦 闭麦
     *
     * @param mute true 开麦
     */
    fun miniOwnerMic(mute: Boolean) {
        MyLog.d(TAG, "miniOwnerMic mute=$mute")
        if (mute) {
            if (ZqEngineKit.getInstance().params.isAnchor) {
                ZqEngineKit.getInstance().setClientRole(false)
            }
            ZqEngineKit.getInstance().muteLocalAudioStream(true)
        } else {
            if (!ZqEngineKit.getInstance().params.isAnchor) {
                ZqEngineKit.getInstance().setClientRole(true)
            }
            ZqEngineKit.getInstance().muteLocalAudioStream(false)
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
        map["kickUserID"] = userId
        map["roomID"] = mRoomData.gameId
        map["roundSeq"] = roundInfoModel.roundSeq

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mRoomServerApi.reqKickUser(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
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

            override fun onError(e: Throwable) {

            }
        }, this)
    }


    /**
     * 回应踢人请求
     *
     * @param isAgree      是否同意
     * @param userId       被踢人ID
     * @param sourceUserId 发起人ID
     */
    fun voteKickUser(isAgree: Boolean, userId: Int, sourceUserId: Int) {
        val map = HashMap<String, Any>()
        map["agree"] = isAgree
        map["kickUserID"] = userId
        map["roomID"] = mRoomData.gameId
        map["sourceUserID"] = sourceUserId

        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mRoomServerApi.rspKickUser(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    U.getToastUtil().showShort("同意踢人成功")
                } else {
                    U.getToastUtil().showShort(result.errmsg)
                }
            }

            override fun onError(e: Throwable) {

            }
        }, this)

    }

    /**
     * 退出房间
     */
    fun exitRoom(from: String) {
        MyLog.w(TAG, "exitRoom from=$from")
        val map = HashMap<String, Any>()
        map["roomID"] = mRoomData.gameId
        mUiHandler.sendEmptyMessageDelayed(MSG_ENSURE_EXIT, 5000)
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mRoomServerApi.exitRoom(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                mUiHandler.removeMessages(MSG_ENSURE_EXIT)
                if (result.errno == 0) {
                    mRoomData.isHasExitGame = true
                    val models = JSON.parseArray(result.data!!.getString("numericDetail"), NumericDetailModel::class.java)
                    val levelResultModel = JSON.parseObject(result.data!!.getString("userScoreChange"), LevelResultModel::class.java)
                    val starCnt = result.data.getInteger("starCnt")
                    if (models != null) {
                        // 得到结果
                        mRoomData.grabResultData = GrabResultData(models, levelResultModel, starCnt)
                        mIGrabView.onGetGameResult(true)
                    } else {
                        mIGrabView.onGetGameResult(false)
                    }
                } else {
                    mIGrabView.onGetGameResult(false)
                }
            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                mUiHandler.removeMessages(MSG_ENSURE_EXIT)
                super.onNetworkError(errorType)
                mIGrabView.onGetGameResult(false)
            }
        })
    }

    /**
     * 切换房间
     */
    fun changeRoom() {
        if (mSwitchRooming) {
            U.getToastUtil().showShort("切换中")
            return
        }
        //        if(true){
        //            stopGuide();
        //            mRoomData.setRealRoundInfo(null);
        //            mIGrabView.hideAllCardView();
        //            joinRoomAndInit(false);
        //            ZqEngineKit.getInstance().unbindAllRemoteVideo();
        //            mRoomData.checkRoundInEachMode();
        //            mIGrabView.onChangeRoomResult(true, null);
        //            mIGrabView.dimissKickDialog();
        //            return;
        //        }
        mSwitchRooming = true
        val map = HashMap<String, Any>()
        map["roomID"] = mRoomData.gameId
        map["tagID"] = mRoomData.tagId
        map["vars"] = RA.getVars()
        map["testList"] = RA.getTestList()
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mRoomServerApi.changeRoom(body), object : ApiObserver<ApiResult>() {

            override fun onNext(result: ApiResult) {
                if (result.errno == 0) {
                    val joinGrabRoomRspModel = JSON.parseObject(result.data!!.toJSONString(), JoinGrabRoomRspModel::class.java)
                    onChangeRoomSuccess(joinGrabRoomRspModel)
                } else {
                    mIGrabView.onChangeRoomResult(false, result.errmsg)
                }
                mSwitchRooming = false
            }

            override fun process(obj: ApiResult) {

            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                super.onNetworkError(errorType)
                mSwitchRooming = false
                mIGrabView.onChangeRoomResult(false, "网络错误")
            }
        }, this, RequestControl("changeRoom", ControlType.CancelThis))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: GrabChangeRoomEvent) {
        onChangeRoomSuccess(event.mJoinGrabRoomRspModel)
    }

    fun onChangeRoomSuccess(joinGrabRoomRspModel: JoinGrabRoomRspModel?) {
        MyLog.d(TAG, "onChangeRoomSuccess joinGrabRoomRspModel=$joinGrabRoomRspModel")
        if (joinGrabRoomRspModel != null) {
            EventBus.getDefault().post(GrabSwitchRoomEvent())
            stopGuide()
            mRoomData.loadFromRsp(joinGrabRoomRspModel)
            joinRoomAndInit(false)
            mIGrabView.onChangeRoomResult(true, null)
            mRoomData.checkRoundInEachMode()
            mIGrabView.dimissKickDialog()
        }
    }

    /**
     * 游戏切后台或切回来
     *
     * @param out 切出去
     * @param in  切回来
     */
    fun swapGame(out: Boolean, `in`: Boolean) {
        MyLog.w(TAG, "swapGame out=$out in=$`in`")
        val map = HashMap<String, Any>()
        map["roomID"] = mRoomData.gameId
        if (out) {
            map["status"] = SwapStatusType.SS_SWAP_OUT
        } else if (`in`) {
            map["status"] = SwapStatusType.SS_SWAP_IN
        }
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mRoomServerApi.swap(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    //                    U.getToastUtil().showShort("切换请求发送成功");
                } else {
                    MyLog.e(TAG, "swapGame result errno is " + result.errmsg)
                }
            }

            override fun onError(e: Throwable) {
                MyLog.e(TAG, "swapGame error $e")
            }
        }, this)
    }

    /**
     * 轮询同步状态task
     */
    fun startSyncGameStateTask(delayTime: Long) {
        cancelSyncGameStateTask()

        if (mRoomData.isIsGameFinish) {
            MyLog.w(TAG, "游戏结束了，还特么Sync")
            return
        }

        mSyncGameStateTask = HandlerTaskTimer.newBuilder()
                .delay(delayTime)
                .interval(sSyncStateTaskInterval)
                .take(-1)
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(t: Int) {
                        MyLog.w(TAG, (sSyncStateTaskInterval / 1000).toString() + "秒钟的 syncGameTask 去更新状态了")
                        syncGameStatus(mRoomData.gameId)
                    }
                })
    }

    fun cancelSyncGameStateTask() {
        if (mSyncGameStateTask != null) {
            mSyncGameStateTask!!.dispose()
        }
    }

    // 同步游戏详情状态(检测不到长连接调用)
    fun syncGameStatus(gameID: Int) {
        ApiMethods.subscribe(mRoomServerApi.syncGameStatus(gameID), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    if (gameID != mRoomData.gameId) {
                        MyLog.d(TAG, "syncGameStatus gameID 不一致")
                        return
                    }
                    val syncStatusTimes = result.data!!.getLongValue("syncStatusTimeMs")  //状态同步时的毫秒时间戳
                    val gameOverTimeMs = result.data!!.getLongValue("gameOverTimeMs")  //游戏结束时间
                    val currentInfo = JSON.parseObject(result.data!!.getString("currentRound"), GrabRoundInfoModel::class.java) //当前轮次信息
                    val nextInfo = JSON.parseObject(result.data!!.getString("nextRound"), GrabRoundInfoModel::class.java) //当前轮次信息

                    var msg = if (currentInfo != null) {
                        "syncGameStatus成功了, currentRound 是 $currentInfo"
                    } else {
                        "syncGameStatus成功了, currentRound 是 null"
                    }

                    msg = msg + ",traceid is " + result.traceId
                    MyLog.w(TAG, msg)

                    if (currentInfo == null) {
                        onGameOver("syncGameStatus", gameOverTimeMs)
                        return
                    }

                    updatePlayerState(gameOverTimeMs, syncStatusTimes, currentInfo, gameID)
                    //                    fetchAcc(nextInfo);
                } else {
                    MyLog.w(TAG, "syncGameStatus失败 traceid is " + result.traceId)
                    estimateOverTsThisRound()
                }
            }

            override fun onError(e: Throwable) {
                MyLog.w(TAG, "syncGameStatus失败了，errno是$e")
            }
        }, this)
    }

    /**
     * 根据时间戳更新选手状态,目前就只有两个入口，SyncStatusEvent push了sycn，不写更多入口
     */
    @Synchronized
    private fun updatePlayerState(gameOverTimeMs: Long, syncStatusTimes: Long, newRoundInfo: GrabRoundInfoModel, gameId: Int) {
        MyLog.w(TAG, "updatePlayerState" + " gameOverTimeMs=" + gameOverTimeMs + " syncStatusTimes=" + syncStatusTimes + " currentInfo=" + newRoundInfo.roundSeq + ",gameId is " + gameId)
        if (!newRoundInfo.isContainInRoom) {
            MyLog.w(TAG, "updatePlayerState, 不再当前的游戏里， game id is $gameId")
            if (mFirstKickOutTime == -1L) {
                mFirstKickOutTime = System.currentTimeMillis()
            }
            mAbsenTimes++
            if (System.currentTimeMillis() - mFirstKickOutTime > 15000 && mAbsenTimes > 10) {
                MyLog.w(TAG, "超过15秒 && 缺席次数是10以上，需要退出")
                exitRoom("updatePlayerState")
                return
            }
        } else {
            mFirstKickOutTime = -1
            mAbsenTimes = 0
        }

        if (syncStatusTimes > mRoomData.lastSyncTs) {
            mRoomData.lastSyncTs = syncStatusTimes
        }

        if (gameOverTimeMs != 0L) {
            if (gameOverTimeMs > mRoomData.gameStartTs) {
                MyLog.w(TAG, "gameOverTimeMs ！= 0 游戏应该结束了")
                // 游戏结束了
                onGameOver("sync", gameOverTimeMs)
            } else {
                MyLog.w(TAG, "服务器结束时间不合法 startTs:" + mRoomData.gameStartTs + " overTs:" + gameOverTimeMs)
            }
        } else {
            // 没结束 current 不应该为null
            if (newRoundInfo != null) {
                // 服务下发的轮次已经大于当前轮次了，说明本地信息已经不对了，更新
                if (!mRoomData.hasGameBegin()) {
                    MyLog.w(TAG, "updatePlayerState 游戏未开始，但同步到轮次信息，更新")
                    // 轮次确实比当前的高，可以切换
                    mRoomData.setHasGameBegin(true)
                    mRoomData.expectRoundInfo = newRoundInfo
                    mRoomData.checkRoundInEachMode()
                } else if (RoomDataUtils.roundSeqLarger<GrabRoundInfoModel>(newRoundInfo, mRoomData.expectRoundInfo)) {
                    MyLog.w(TAG, "updatePlayerState sync 发现本地轮次信息滞后，更新")
                    // 轮次确实比当前的高，可以切换
                    mRoomData.expectRoundInfo = newRoundInfo
                    mRoomData.checkRoundInEachMode()
                } else if (RoomDataUtils.isCurrentExpectingRound(newRoundInfo.roundSeq, mRoomData)) {
                    /**
                     * 是当前轮次，最近状态就更新整个轮次
                     */
                    if (syncStatusTimes >= mRoomData.lastSyncTs) {
                        MyLog.w(TAG, "updatePlayerState sync 更新当前轮次")
                        mRoomData.expectRoundInfo!!.tryUpdateRoundInfoModel(newRoundInfo, true)
                    }
                }
            } else {
                MyLog.w(TAG, "服务器结束时间不合法 currentInfo=null")
            }
        }
    }

    /**
     * 通知游戏结束
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.POSTING, priority = 9)
    fun onEvent(event: GrabGameOverEvent) {
        MyLog.d(TAG, "GrabGameOverEvent")
        estimateOverTsThisRound()
        tryStopRobotPlay()
        ZqEngineKit.getInstance().stopRecognize()
        mRoomData.isIsGameFinish = true
        cancelSyncGameStateTask()
        // 游戏结束了,处理相应的ui逻辑
        mUiHandler.post { mIGrabView.roundOver(event.lastRoundInfo, false, null) }
        // 销毁引擎，减小成本
        ZqEngineKit.getInstance().destroy("grabroom")
        mUiHandler.postDelayed({ mIGrabView.gameFinish() }, 2000)
    }

    /**
     * 轮次信息有更新
     */
    @Subscribe(threadMode = ThreadMode.POSTING, priority = 9)
    fun onEvent(event: GrabRoundChangeEvent) {
        DebugLogView.println(TAG, "---轮次" + event.newRoundInfo.roundSeq + "开始--- ")
        MyLog.d(TAG, "GrabRoundChangeEvent event=$event")
        // 轮次变化尝试更新头像
        estimateOverTsThisRound()
        mUiHandler.removeMessages(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS)
        closeEngine()
        tryStopRobotPlay()
        ZqEngineKit.getInstance().stopRecognize()
        val now = event.newRoundInfo
        if (now != null) {
            EventBus.getDefault().post(GrabPlaySeatUpdateEvent(now.playUsers))
            EventBus.getDefault().post(GrabWaitSeatUpdateEvent(now.waitUsers))
            var size = 0
            for (playerInfoModel in now.playUsers) {
                if (playerInfoModel.userID == 2) {
                    continue
                }
                size++
            }
            val finalSize = size
            if(mRoomData.roomType==GrabRoomType.ROOM_TYPE_PLAYBOOK){

            }else{
                mUiHandler.post { mIGrabView.showPracticeFlag(finalSize <= 1) }
            }
        }

        if (now!!.status == EQRoundStatus.QRS_INTRO.value) {
            //抢唱阶段，播抢唱卡片
            //TODO 再梳理整个流程
            if (event.lastRoundInfo != null && event.lastRoundInfo.status >= EQRoundStatus.QRS_SING.value) {
                // 新一轮的抢唱阶段，得告诉上一轮演唱结束了啊，上一轮演唱结束卡片播完，才播歌曲卡片
                mUiHandler.post { mIGrabView.roundOver(event.lastRoundInfo, true, now) }
                if (event.lastRoundInfo.singBySelf()) {
                    onSelfRoundOver(event.lastRoundInfo)
                }
            } else {
                mUiHandler.post { mIGrabView.grabBegin(now.roundSeq, now.music) }
            }
        } else if (now.isSingStatus) {
            // 演唱阶段
            if (now.singBySelf()) {
                mUiHandler.post { mIGrabView.singBySelf() }
                preOpWhenSelfRound()
            } else {
                mUiHandler.post { mIGrabView.singByOthers() }
                preOpWhenOtherRound(now.userID)
            }
        } else if (now.status == EQRoundStatus.QRS_END.value) {
            MyLog.w(TAG, "GrabRoundChangeEvent 刚切换到该轮次就告诉我轮次结束？？？roundSeq:" + now.roundSeq)
            MyLog.w(TAG, "自动切换到下个轮次")
        }

        mUiHandler.post { mIGrabView.hideManageTipView() }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: GrabWaitSeatUpdateEvent) {
        MyLog.d(TAG, "onEvent event=$event")
        if (event.list != null && event.list.size > 0) {
            mIGrabView.hideInviteTipView()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: SomeOneJoinWaitSeatEvent) {
        MyLog.d(TAG, "onEvent event=$event")
        mIGrabView.hideInviteTipView()
    }

    /**
     * 轮次内状态更新
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(event: GrabRoundStatusChangeEvent) {
        MyLog.d(TAG, "GrabRoundStatusChangeEvent event=$event")
        estimateOverTsThisRound()
        mUiHandler.removeMessages(MSG_ENSURE_SWITCH_BROADCAST_SUCCESS)
        val now = event.roundInfo

        var needCloseEngine = true
        if (mRoomData.isVideoRoom) {
            if (now.isPKRound && now.getsPkRoundInfoModels().size >= 2) {
                if (now.status == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.value) {
                    // pk的第一轮
                    val pkRoundInfoModel2 = now.getsPkRoundInfoModels()[1]
                    if (MyUserInfoManager.getInstance().uid == pkRoundInfoModel2.userID.toLong()) {
                        // 本人第二个唱
                        if (pkRoundInfoModel2.overReason == EQRoundOverReason.ROR_IN_ROUND_PLAYER_EXIT.value || pkRoundInfoModel2.overReason == EQRoundOverReason.ROR_SELF_GIVE_UP.value) {
                            needCloseEngine = true
                        } else {
                            needCloseEngine = false
                        }
                    }
                } else if (now.status == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.value) {
                    // pk第二轮
                    val pkRoundInfoModel1 = now.getsPkRoundInfoModels()[0]
                    if (MyUserInfoManager.getInstance().uid == pkRoundInfoModel1.userID.toLong()) {
                        // 本人第二个唱
                        if (pkRoundInfoModel1.overReason == EQRoundOverReason.ROR_IN_ROUND_PLAYER_EXIT.value || pkRoundInfoModel1.overReason == EQRoundOverReason.ROR_SELF_GIVE_UP.value) {
                            needCloseEngine = true
                        } else {
                            needCloseEngine = false
                        }
                    }
                }
            }
        }

        if (needCloseEngine) {
            closeEngine()
        } else {
            // pk第二轮，只把混音关了
            if (!ZqEngineKit.getInstance().params.isAnchor) {
                ZqEngineKit.getInstance().setClientRole(true)
            }
            // 不发声
            ZqEngineKit.getInstance().muteLocalAudioStream(true)
            ZqEngineKit.getInstance().stopAudioMixing()
            ZqEngineKit.getInstance().stopAudioRecording()
        }
        tryStopRobotPlay()
        if (now.status == EQRoundStatus.QRS_INTRO.value) {
            //抢唱阶段，播抢唱卡片
            mUiHandler.post { mIGrabView.grabBegin(now.roundSeq, now.music) }
        } else if (now.isSingStatus) {
            // 演唱阶段
            if (now.singBySelf()) {
                mUiHandler.post { mIGrabView.singBySelf() }
                preOpWhenSelfRound()
            } else {
                mUiHandler.post { mIGrabView.singByOthers() }
                preOpWhenOtherRound(now.userID)
            }
        }
    }

    private fun closeEngine() {
        if (mRoomData.gameId > 0) {
            ZqEngineKit.getInstance().stopAudioMixing()
            ZqEngineKit.getInstance().stopAudioRecording()
            if (mRoomData.isSpeaking) {
                MyLog.d(TAG, "closeEngine 正在抢麦说话，无需闭麦")
            } else {
                //                if (mRoomData.isOwner()) {
                //                    MyLog.d(TAG, "closeEngine 是房主 mute即可");
                //                    ZqEngineKit.getInstance().muteLocalAudioStream(true);
                //                } else {
                if (ZqEngineKit.getInstance().params.isAnchor) {
                    ZqEngineKit.getInstance().setClientRole(false)
                }
                //                }
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
                } else if (mRoomData.isSpeaking) {
                    MyLog.d(TAG, "房主抢麦切换主播成功")
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
     * 弱化声音
     *
     * @param time
     */
    private fun weakVolume(time: Int) {
        mUiHandler.removeMessages(MSG_RECOVER_VOLUME)
        mUiHandler.sendEmptyMessageDelayed(MSG_RECOVER_VOLUME, time.toLong())
        if (ZqEngineKit.getInstance().params.isAnchor) {
            if (mEngineParamsTemp == null) {
                val audioVolume = ZqEngineKit.getInstance().params.audioMixingPlayoutVolume
                val recordVolume = ZqEngineKit.getInstance().params.recordingSignalVolume
                mEngineParamsTemp = EngineParamsTemp(audioVolume, recordVolume)
                ZqEngineKit.getInstance().adjustAudioMixingPlayoutVolume((audioVolume * 0.2).toInt(), false)
                ZqEngineKit.getInstance().adjustRecordingSignalVolume((recordVolume * 0.2).toInt(), false)
            }
        } else {
            MyLog.d(TAG, "我不是主播，忽略")
        }
        if (mExoPlayer != null) {
            mExoPlayer!!.setVolume(mExoPlayer!!.volume * 0.0f, false)
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
            if (infoModel.status == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.value) {
                songModel = songModel!!.pkMusic
            }
            if (songModel == null) {
                return@Runnable
            }
            // 开始开始混伴奏，开始解除引擎mute
            val accFile = SongResUtils.getAccFileByUrl(songModel.acc)
            // midi不需要在这下，只要下好，native就会解析，打分就能恢复
            val midiFile = SongResUtils.getMIDIFileByUrl(songModel.midi)

            if (mRoomData.isAccEnable && infoModel.isAccRound || infoModel.isPKRound) {
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

    /**
     * 想要演唱机会的人
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: QWantSingChanceMsgEvent) {
        if (RoomDataUtils.isCurrentExpectingRound(event.getRoundSeq()!!, mRoomData)) {
            MyLog.w(TAG, "有人想唱：userID " + event.getUserID() + ", seq " + event.getRoundSeq())
            val roundInfoModel = mRoomData.expectRoundInfo

            val wantSingerInfo = WantSingerInfo()
            wantSingerInfo.userID = event.getUserID()!!
            wantSingerInfo.timeMs = System.currentTimeMillis()
            wantSingerInfo.wantSingType = event.getWantSingType()

            if (roundInfoModel!!.status == EQRoundStatus.QRS_INTRO.value) {
                roundInfoModel.addGrabUid(true, wantSingerInfo)
            } else {
                MyLog.d(TAG, "但不是抢唱阶段，不发通知")
                roundInfoModel.addGrabUid(false, wantSingerInfo)
            }
        } else {
            MyLog.w(TAG, "有人想唱,但是不是这个轮次：userID " + event.getUserID() + ", seq " + event.getRoundSeq() + "，当前轮次是 " + mRoomData.expectRoundInfo)
        }
    }

    /**
     * 抢到演唱机会的人
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: QGetSingChanceMsgEvent) {
        ensureInRcRoom()
        if (RoomDataUtils.isCurrentExpectingRound(event.getRoundSeq()!!, mRoomData)) {
            MyLog.w(TAG, "抢到唱歌权：userID " + event.getUserID() + ", roundInfo" + event.currentRound)
            val roundInfoModel = mRoomData.expectRoundInfo
            roundInfoModel!!.isHasSing = true
            roundInfoModel.userID = event.getUserID()!!
            roundInfoModel.tryUpdateRoundInfoModel(event.getCurrentRound(), true)
            // 加入抢唱状态后，不能用这个 updateStatus了
            //roundInfoModel.updateStatus(true, EQRoundStatus.QRS_SING.getValue());
        } else {
            MyLog.w(TAG, "抢到唱歌权,但是不是这个轮次：userID " + event.getUserID() + ", seq " + event.getRoundSeq() + "，当前轮次是 " + mRoomData.expectRoundInfo)
        }
    }

    /**
     * 有人灭灯
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: QLightOffMsgEvent) {
        if (RoomDataUtils.isCurrentExpectingRound(event.roundSeq, mRoomData)) {
            MyLog.w(TAG, "有人灭灯了：userID " + event.userID + ", seq " + event.roundSeq)
            val roundInfoModel = mRoomData.expectRoundInfo
            //都开始灭灯肯定是已经开始唱了
            //roundInfoModel.updateStatus(true, EQRoundStatus.QRS_SING.getValue());
            val noPassingInfo = MLightInfoModel()
            noPassingInfo.userID = event.userID
            roundInfoModel!!.addLightOffUid(true, noPassingInfo)
        } else {
            MyLog.w(TAG, "有人灭灯了,但是不是这个轮次：userID " + event.userID + ", seq " + event.roundSeq + "，当前轮次是 " + mRoomData.expectRoundInfo)
        }
    }

    /**
     * 有人爆灯
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: QLightBurstMsgEvent) {
        if (RoomDataUtils.isCurrentExpectingRound(event.roundSeq, mRoomData)) {
            MyLog.w(TAG, "有人爆灯了：userID " + event.userID + ", seq " + event.roundSeq)
            val roundInfoModel = mRoomData.expectRoundInfo
            //都开始灭灯肯定是已经开始唱了
            //            roundInfoModel.updateStatus(true, EQRoundStatus.QRS_SING.getValue());
            val noPassingInfo = BLightInfoModel()
            noPassingInfo.userID = event.userID
            roundInfoModel!!.addLightBurstUid(true, noPassingInfo)
        } else {
            MyLog.w(TAG, "有人爆灯了,但是不是这个轮次：userID " + event.userID + ", seq " + event.roundSeq + "，当前轮次是 " + mRoomData.expectRoundInfo)
        }
    }

    /**
     * 这里来伪装弹幕的好处，是sych下来的爆灭灯变化也会触发这个时间
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: GrabSomeOneLightOffEvent) {
        MyLog.d(TAG, "onEvent event=$event")
        if (event.roundInfo.status == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.value) {
            if (event.roundInfo.getsPkRoundInfoModels().size > 0) {
                pretendLightMsgComment(event.roundInfo.getsPkRoundInfoModels()[0].userID, event.uid, false)
            }
        } else if (event.roundInfo.status == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.value) {
            if (event.roundInfo.getsPkRoundInfoModels().size > 1) {
                pretendLightMsgComment(event.roundInfo.getsPkRoundInfoModels()[1].userID, event.uid, false)
            }
        } else {
            pretendLightMsgComment(event.roundInfo.userID, event.uid, false)
        }
    }

    /**
     * 同上
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: GrabSomeOneLightBurstEvent) {
        if (event.roundInfo.status == EQRoundStatus.QRS_SPK_FIRST_PEER_SING.value) {
            if (event.roundInfo.getsPkRoundInfoModels().size > 0) {
                pretendLightMsgComment(event.roundInfo.getsPkRoundInfoModels()[0].userID, event.uid, true)
            }
        } else if (event.roundInfo.status == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.value) {
            if (event.roundInfo.getsPkRoundInfoModels().size > 1) {
                pretendLightMsgComment(event.roundInfo.getsPkRoundInfoModels()[1].userID, event.uid, true)
            }
        } else {
            pretendLightMsgComment(event.roundInfo.userID, event.uid, true)
        }

        val now = mRoomData.realRoundInfo
        if (now != null) {
            if (now.singBySelf()) {
                StatisticsAdapter.recordCountEvent("grab", "game_getlike", null)
            }
        }
    }

    /**
     * 伪装爆灭灯消息
     *
     * @param singerId 被灭灯演唱者
     * @param uid      灭灯操作者
     */
    private fun pretendLightMsgComment(singerId: Int, uid: Int, isBao: Boolean) {
        val singerModel = RoomDataUtils.getPlayerInfoById(mRoomData, singerId)
        val playerInfoModel = RoomDataUtils.getPlayerInfoById(mRoomData, uid)
        MyLog.d(TAG, "pretendLightMsgComment singerId=$singerModel uid=$playerInfoModel isBao=$isBao")
        if (singerModel != null && playerInfoModel != null) {
            var isChorus = false
            var isMiniGame = false
            val now = mRoomData.realRoundInfo
            if (now != null) {
                isMiniGame = now.isMiniGameRound
                isChorus = now.isChorusRound
            }
            val commentLightModel = CommentLightModel(mRoomData.gameType, playerInfoModel, singerModel, isBao, isChorus, isMiniGame)
            EventBus.getDefault().post(PretendCommentMsgEvent(commentLightModel))
        }
    }

    /**
     * 有人加入房间
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: QJoinNoticeEvent) {
        var canAdd = false
        val playerInfoModel = event.infoModel
        MyLog.d(TAG, "有人加入房间,id=" + playerInfoModel!!.userID + " name=" + playerInfoModel.userInfo.nicknameRemark + " role=" + playerInfoModel.role + " roundSeq=" + event.roundSeq)
        if (playerInfoModel != null && playerInfoModel.userID.toLong() == MyUserInfoManager.getInstance().uid) {
            /**
             * 自己加入房间不提示
             * 因为会有一个bug，
             * 场景如下，A中途进入房间，返回的轮次信息里waitlist里没有A，但是会下发一个 A 以观众身份加入房间的push，导致提示语重复
             */
            canAdd = false
        } else if (RoomDataUtils.isCurrentExpectingRound(event.roundSeq, mRoomData)) {
            val grabRoundInfoModel = mRoomData.expectRoundInfo
            if (grabRoundInfoModel != null && grabRoundInfoModel.addUser(true, playerInfoModel)) {
                canAdd = true
            }
        } else if (!mRoomData.hasGameBegin()) {
            canAdd = true
            if (mRoomData.roomType == GrabRoomType.ROOM_TYPE_PLAYBOOK) {
                mRoomData?.playbookRoomDataWhenNotStart?.addUser(true, playerInfoModel)
            }
        } else {
            MyLog.w(TAG, "有人加入房间了,但是不是这个轮次：userID " + event.infoModel.userID + ", seq " + event.roundSeq + "，当前轮次是 " + mRoomData.expectRoundInfo)
        }
        //TODO 如果加入房间提示有遗漏，可以考虑接受 SomeOne 事件，一担用户有变化都会回调
        if (canAdd) {
            //  加入房间不提示
            //pretendEnterRoom(playerInfoModel);
            mIGrabView.joinNotice(event.infoModel.userInfo)
        }
    }

    /**
     * 某人退出游戏
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: QExitGameMsgEvent) {
        if (RoomDataUtils.isCurrentExpectingRound(event.roundSeq, mRoomData)) {
            MyLog.d(TAG, "有人离开房间,id=" + event.userID)
            val grabRoundInfoModel = mRoomData.expectRoundInfo
            grabRoundInfoModel!!.removeUser(true, event.userID)

        } else {
            MyLog.w(TAG, "有人离开房间了,但是不是这个轮次：userID " + event.userID + ", seq " + event.roundSeq + "，当前轮次是 " + mRoomData.expectRoundInfo)
        }
    }

    /**
     * 某人离开选手席
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: SomeOneLeavePlaySeatEvent) {
        val grabRoundInfoModel = mRoomData.realRoundInfo
        if (grabRoundInfoModel != null) {
            for (chorusRoundInfoModel in grabRoundInfoModel.chorusRoundInfoModels) {
                if (event.mPlayerInfoModel != null) {
                    if (chorusRoundInfoModel.userID == event.mPlayerInfoModel.userID) {
                        chorusRoundInfoModel.userExit()
                        pretendGiveUp(mRoomData.getPlayerOrWaiterInfo(event.mPlayerInfoModel.userID))
                    }
                }
            }
        }
    }


    /**
     * 金币变化
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: QCoinChangeEvent) {
        if (event.userID.toLong() == MyUserInfoManager.getInstance().uid) {
            if (event.remainCoin > 0) {
                mRoomData.setCoin(event.remainCoin)
            }
            if (event.reason.value == 1) {
                pretendSystemMsg("你获取了" + event.changeCoin + "金币奖励")
            }
        }
    }

    /**
     * 合唱某人放弃了演唱
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: QChoGiveUpEvent) {
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
                            if (event.userID.toLong() == MyUserInfoManager.getInstance().uid) {
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
    fun onEvent(event: QPkInnerRoundOverEvent) {
        MyLog.d(TAG, "QPkInnerRoundOverEvent event=$event")
        if (RoomDataUtils.isCurrentRunningRound(event.mRoundInfoModel.roundSeq, mRoomData)) {
            val now = mRoomData.realRoundInfo
            if (now != null) {
                now.tryUpdateRoundInfoModel(event.mRoundInfoModel, true)
                //                // PK 第一个人不唱了 加个弹幕
                if (now.getsPkRoundInfoModels().size > 0) {
                    if (now.getsPkRoundInfoModels()[0].overReason == EQRoundOverReason.ROR_SELF_GIVE_UP.value) {
                        val userInfoModel = mRoomData.getPlayerOrWaiterInfo(now.getsPkRoundInfoModels()[0].userID)
                        pretendGiveUp(userInfoModel)
                    }
                }
                if (now.getsPkRoundInfoModels().size > 1) {
                    if (now.getsPkRoundInfoModels()[1].overReason == EQRoundOverReason.ROR_SELF_GIVE_UP.value) {
                        val userInfoModel = mRoomData.getPlayerOrWaiterInfo(now.getsPkRoundInfoModels()[1].userID)
                        pretendGiveUp(userInfoModel)
                    }
                }
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

    private fun pretendEnterRoom(playerInfoModel: GrabPlayerInfoModel) {
        val commentModel = CommentTextModel()
        commentModel.userInfo = playerInfoModel.userInfo
        commentModel.avatarColor = CommentModel.AVATAR_COLOR
        val stringBuilder: SpannableStringBuilder
        if (playerInfoModel.userInfo.userId == UserAccountManager.SYSTEM_GRAB_ID) {
            stringBuilder = SpanUtils()
                    .append(playerInfoModel.userInfo.nicknameRemark + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                    .append("我是撕歌最傲娇小助手多音，来和你们一起唱歌卖萌~").setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
                    .create()
        } else {
            val spanUtils = SpanUtils()
                    .append(playerInfoModel.userInfo.nicknameRemark + " ").setForegroundColor(CommentModel.GRAB_NAME_COLOR)
                    .append("加入了房间").setForegroundColor(CommentModel.GRAB_TEXT_COLOR)
            if (BuildConfig.DEBUG) {
                spanUtils.append(" 角色为" + playerInfoModel.role)
                        .append(" 在线状态为" + playerInfoModel.isOnline)
            }
            stringBuilder = spanUtils.create()
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
    fun onEvent(event: QRoundOverMsgEvent) {
        MyLog.w(TAG, "收到服务器的某一个人轮次结束的push event:$event")
        ensureInRcRoom()
        //        if (mRoomData.getLastSyncTs() >= event.getInfo().getTimeMs()) {
        //            MyLog.w(TAG, "但是是个旧数据");
        //            return;
        //        }
        if (RoomDataUtils.isCurrentRunningRound(event.getCurrentRound().roundSeq, mRoomData)) {
            // 如果是当前轮次
            mRoomData.realRoundInfo!!.tryUpdateRoundInfoModel(event.currentRound, true)
            if (event.myCoin >= 0) {
                mRoomData.setCoin(event.myCoin)
            }
            if (event.totalRoundNum > 0) {
                mRoomData.grabConfigModel.totalGameRoundSeq = event.totalRoundNum
            }

            //非PK和合唱轮次 加上不唱了弹幕 产品又让加回来了
            val infoModel = mRoomData.realRoundInfo
            if (!infoModel!!.isPKRound && !infoModel.isChorusRound) {
                if (infoModel.overReason == EQRoundOverReason.ROR_SELF_GIVE_UP.value) {
                    pretendGiveUp(mRoomData.getPlayerOrWaiterInfo(infoModel.userID))
                }
            }
        }
        if (!mRoomData.hasGameBegin()) {
            MyLog.w(TAG, "收到 QRoundOverMsgEvent，游戏未开始？将游戏设置为开始状态")
            mRoomData.setHasGameBegin(true)
            mRoomData.expectRoundInfo = event.nextRound
            mRoomData.checkRoundInEachMode()
        } else if (RoomDataUtils.roundSeqLarger<GrabRoundInfoModel>(event.nextRound, mRoomData.expectRoundInfo)) {
            // 游戏轮次结束
            // 轮次确实比当前的高，可以切换
            MyLog.w(TAG, "轮次确实比当前的高，可以切换")
            mRoomData.expectRoundInfo = event.nextRound
            mRoomData.checkRoundInEachMode()
        } else {
            MyLog.w(TAG, "轮次比当前轮次还小,直接忽略 当前轮次:" + mRoomData.expectRoundInfo!!.roundSeq
                    + " push轮次:" + event.currentRound.roundSeq)
        }
    }

    /**
     * 游戏结束事件
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: QRoundAndGameOverMsgEvent) {
        cancelSyncGameStateTask()
        if (RoomDataUtils.isCurrentRunningRound(event.roundInfoModel.roundSeq, mRoomData)) {
            // 如果是当前轮次
            mRoomData.realRoundInfo!!.tryUpdateRoundInfoModel(event.roundInfoModel, true)
            if (event.myCoin >= 0) {
                mRoomData.setCoin(event.myCoin)
            }
        }
        onGameOver("QRoundAndGameOverMsgEvent", event.roundOverTimeMs)
        if (event.mOverReason == EQGameOverReason.GOR_OWNER_EXIT) {
            MyLog.w(TAG, "房主离开了游戏，房间解散")
            U.getToastUtil().showLong("房主离开了游戏，房间解散")
        }
    }

    /**
     * 同步
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: QSyncStatusMsgEvent) {
        if (event.getInfo().roomID != mRoomData.gameId) {
            MyLog.w(TAG, "onEvent QSyncStatusMsgEvent， current roomid is " + mRoomData.gameId + ", event.getInfo().getRoomID() is " + event.getInfo().roomID)
            return
        }

        ensureInRcRoom()
        MyLog.w(TAG, "收到服务器 sync push更新状态,event.currentRound是" + event.getCurrentRound().roundSeq + ", timeMs 是" + event.info.timeMs)
        // 延迟10秒sync ，一旦启动sync 间隔 5秒 sync 一次
        startSyncGameStateTask(sSyncStateTaskInterval * 2)
        updatePlayerState(event.getGameOverTimeMs()!!, event.getSyncStatusTimeMs()!!, event.getCurrentRound(), event.getInfo().roomID)
        //        fetchAcc(event.getNextRound());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: UpdateMeiliEvent) {
        // TODO: 2019-06-05 暂时只对pk做个特殊处理
        val grabRoundInfoModel = mRoomData.realRoundInfo
        if (grabRoundInfoModel != null && grabRoundInfoModel.isPKRound) {
            for (roundInfoModel in grabRoundInfoModel.getsPkRoundInfoModels()) {
                if (roundInfoModel.userID == event.userID) {
                    roundInfoModel.meiliTotal = event.value
                    return
                }
            }
        }
    }

    private fun onGameOver(from: String, gameOverTs: Long) {
        MyLog.w(TAG, "游戏结束 gameOverTs=$gameOverTs from:$from")
        if (gameOverTs > mRoomData.gameStartTs && gameOverTs > mRoomData.gameOverTs) {
            cancelSyncGameStateTask()
            mRoomData.gameOverTs = gameOverTs
            mRoomData.expectRoundInfo = null
            mRoomData.checkRoundInEachMode()
        } else {
            MyLog.w(TAG, "游戏结束 gameOverTs 不合法，取消")
        }
    }

    /**
     * 房主 被告知游戏开始
     *
     * @param event
     */
    @Subscribe
    fun onEvent(event: QGameBeginEvent) {
        MyLog.d(TAG, "onEvent QGameBeginEvent !!收到游戏开始的push $event")
        if (mRoomData.hasGameBegin()) {
            MyLog.d(TAG, "onEvent 游戏开始的标记为已经为true event=$event")
            mRoomData.grabConfigModel = event.mGrabConfigModel
        } else {
            mRoomData.setHasGameBegin(true)
            mRoomData.grabConfigModel = event.mGrabConfigModel
            mRoomData.expectRoundInfo = event.mInfoModel
            mRoomData.checkRoundInEachMode()
        }
        if (mRoomData.hasGameBegin()) {
            startSyncGameStateTask(sSyncStateTaskInterval)
        } else {
            cancelSyncGameStateTask()
        }
        ensureInRcRoom()
    }

    @Subscribe
    fun onEvent(event: QChangeMusicTagEvent) {
        MyLog.d(TAG, "onEvent QChangeMusicTagEvent !!切换专场 $event")
        if (mRoomData.gameId == event.info.roomID) {
            pretendSystemMsg(String.format("房主已将歌单切换为 %s 专场", event.tagName))
        }
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
        swapGame(!event.foreground, event.foreground)
        if (event.foreground) {
            muteAllRemoteAudioStreams(mRoomData.isMute, false)
            if (mRoomData.isVideoRoom) {
                ZqEngineKit.getInstance().muteLocalVideoStream(false)
            }
        } else {
            muteAllRemoteAudioStreams(true, false)
            if (mRoomData.isVideoRoom) {
                if (ZqEngineKit.getInstance().params.isAnchor) {
                    // 我是主播
                    ZqEngineKit.getInstance().muteLocalVideoStream(true)
                }
            }
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

    private fun estimateOverTsThisRound(): Int {
        //        int pt = RoomDataUtils.estimateTs2End(mRoomData, mRoomData.getRealRoundInfo());
        //        MyLog.w(TAG, "估算出距离本轮结束还有" + pt + "ms");
        return 0
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
        val infoModel = mRoomData.realRoundInfo
        if (infoModel != null && infoModel.singByUserId(event.userId)) {
            mIGrabView.updateScrollBarProgress(event.score, event.lineNum)
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

    internal fun processScore(score: Int, line: Int) {
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
        if (mRobotScoreHelper != null) {
            mRobotScoreHelper!!.add(machineScoreItem)
        }
        mUiHandler.post { mIGrabView.updateScrollBarProgress(score, mRoomData.songLineNum) }
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
                    .setUserID(MyUserInfoManager.getInstance().uid.toInt())
                    .setNickName(MyUserInfoManager.getInstance().nickName)
                    .setAvatar(MyUserInfoManager.getInstance().avatar)
                    .setSex(ESex.fromValue(MyUserInfoManager.getInstance().sex))
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
                                .setUserID(MyUserInfoManager.getInstance().uid.toInt())
                                .setNo(machineScoreItem.no)
                                .setScore(machineScoreItem.score)
                                .setItemID(now.music.itemID)
                                .setLineNum(mRoomData.songLineNum)
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
    fun sendScoreToServer(score: Int, line: Int) {
        //score = (int) (Math.mRandom()*100);
        val map = HashMap<String, Any>()
        val infoModel = mRoomData.realRoundInfo ?: return
        map["userID"] = MyUserInfoManager.getInstance().uid

        var itemID = 0
        if (infoModel.music != null) {
            itemID = infoModel.music.itemID
            if (infoModel.status == EQRoundStatus.QRS_SPK_SECOND_PEER_SING.value) {
                val pkSong = infoModel.music.pkMusic
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
                .append("|").append(MyUserInfoManager.getInstance().uid)
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
        ApiMethods.subscribe(mRoomServerApi.sendPkPerSegmentResult(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    // TODO: 2018/12/13  当前postman返回的为空 待补充
                    MyLog.w(TAG, "单句打分上报成功")
                } else {
                    MyLog.w(TAG, "单句打分上报失败" + result.errno)
                }
            }

            override fun onError(e: Throwable) {
                MyLog.e(e)
            }
        }, this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(qKickUserReqEvent: QKickUserReqEvent) {
        MyLog.d(TAG, "onEvent qKickUserReqEvent=$qKickUserReqEvent")
        // 踢人的请求
        mIGrabView.showKickVoteDialog(qKickUserReqEvent.kickUserID, qKickUserReqEvent.sourceUserID)
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
                        mRoomData.setHzCount(property.hongZuanBalance, property.lastChangeMs)
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(qKickUserResultEvent: QKickUserResultEvent) {
        MyLog.d(TAG, "onEvent qKickUserResultEvent=$qKickUserResultEvent")
        // 踢人的结果
        if (qKickUserResultEvent.kickUserID.toLong() == MyUserInfoManager.getInstance().uid) {
            // 自己被踢出去
            if (qKickUserResultEvent.isKickSuccess) {
                if (mRoomData.ownerId == qKickUserResultEvent.sourceUserID) {
                    mIGrabView.kickBySomeOne(true)
                } else {
                    mIGrabView.kickBySomeOne(false)
                }
            }
        } else {
            // 别人被踢出去
            mIGrabView.dimissKickDialog()
            pretendSystemMsg(qKickUserResultEvent.kickResultContent)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: GrabSpeakingControlEvent) {
        mRoomData.isSpeaking = event.speaking
        // 踢人的结果
        if (event.speaking) {
            ZqEngineKit.getInstance().muteLocalAudioStream(false)
            val v = ZqEngineKit.getInstance().params.playbackSignalVolume / 4
            ZqEngineKit.getInstance().adjustPlaybackSignalVolume(v, false)
            if (mExoPlayer != null) {
                mExoPlayer!!.setVolume(mExoPlayer!!.volume * 0.0f, false)
            }
        } else {
            // 要闭麦
            val infoModel = mRoomData.realRoundInfo
            if (infoModel != null && infoModel.singBySelf()) {
                MyLog.d(TAG, "自己的轮次，无需闭麦")
            } else {
                ZqEngineKit.getInstance().muteLocalAudioStream(true)
            }
            val v = ZqEngineKit.getInstance().params.playbackSignalVolume
            ZqEngineKit.getInstance().adjustPlaybackSignalVolume(v, false)
            if (mExoPlayer != null) {
                mExoPlayer!!.setVolume(mExoPlayer!!.volume, false)
            }
        }
    }

    class EngineParamsTemp(internal var audioVolume: Int, internal var recordVolume: Int)

    companion object {

        private val sSyncStateTaskInterval: Long = 5000

        internal val MSG_ENSURE_IN_RC_ROOM = 9// 确保在融云的聊天室，保证融云的长链接

        internal val MSG_ROBOT_SING_BEGIN = 10

        internal val MSG_ENSURE_SWITCH_BROADCAST_SUCCESS = 21 // 确保用户切换成主播成功，防止引擎不回调的保护

        internal val MSG_RECOVER_VOLUME = 22 // 房主说话后 恢复音量

        internal val MSG_ENSURE_EXIT = 8 // 房主说话后 恢复音量
    }
}
