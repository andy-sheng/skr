package com.module.playways.doubleplay.presenter

import android.os.Handler
import android.os.Message
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.core.account.UserAccountManager
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.jiguang.JiGuangPush
import com.common.log.MyLog
import com.common.mvp.PresenterEvent
import com.common.mvp.RxLifeCyclePresenter
import com.common.notification.event.CRStartByCreateNotifyEvent
import com.common.rx.RxRetryAssist
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.engine.Params
import com.module.ModuleServiceManager
import com.module.RouterConstants
import com.module.common.ICallback
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.doubleplay.DoubleRoomServerApi
import com.module.playways.doubleplay.event.*
import com.module.playways.doubleplay.inter.IDoublePlayView
import com.module.playways.doubleplay.model.DoubleSyncModel
import com.module.playways.doubleplay.pushEvent.*
import com.zq.live.proto.CombineRoom.ECombineStatus
import com.zq.mediaengine.kit.ZqEngineKit
import io.reactivex.Observable
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class DoubleCorePresenter(private val mRoomData: DoubleRoomData, private val mIDoublePlayView: IDoublePlayView) : RxLifeCyclePresenter() {
    val tag = "DoubleCorePresenter"

    val SYNC_MSG = 0
    val PICK_MSG = 1
    val SYNC_DURATION = 12000L

    internal var mSyncStatusTimeMs: Long = 0 //状态同步时的毫秒时间戳
    private var mDoubleRoomServerApi = ApiManager.getInstance().createService(DoubleRoomServerApi::class.java)

    var mUiHandler: Handler

    var mPickNum: Int = 0

    var mIsCloseByTimeOver: Boolean = false

    init {
        EventBus.getDefault().register(this)
        mUiHandler = object : Handler() {
            override fun handleMessage(msg: Message?) {
                when (msg?.what) {
                    SYNC_MSG -> syncStatus()
                    PICK_MSG -> {
                        val mutableSet1 = mutableMapOf("count" to mPickNum, "fromPickuserID" to MyUserInfoManager.getInstance().uid, "roomID" to mRoomData.gameId, "toPickUserID" to mRoomData.getAntherUser()?.userId)
                        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
                        ApiMethods.subscribe(mDoubleRoomServerApi.pickOther(body), object : ApiObserver<ApiResult>() {
                            override fun process(obj: ApiResult?) {

                            }
                        }, this@DoubleCorePresenter)
                        mPickNum = 0
                    }
                }
            }
        }

        if (mRoomData.isRoomPrepared()) {
            mUiHandler.sendEmptyMessageDelayed(SYNC_MSG, SYNC_DURATION)
            joinRoomAndInit(true)
        }

        joinRcRoom(-1)
    }

    /**
     * 加入引擎房间
     * 系统消息弹幕
     */
    private fun joinRoomAndInit(first: Boolean) {
        MyLog.w(TAG, "joinRoomAndInit" + " first=$first , gameId is ${mRoomData.gameId}")
        mIDoublePlayView.joinAgora()
        if (mRoomData.gameId > 0) {
            if (first) {
                val params = Params.getFromPref()
                params.scene = Params.Scene.doubleChat
                params.isEnableVideo = false
                ZqEngineKit.getInstance().init("doubleRoom", params)
            }
            ZqEngineKit.getInstance().joinRoom(mRoomData.gameId.toString(), UserAccountManager.getInstance().uuidAsLong.toInt(), true, mRoomData.getToken())
            // 不发送本地音频, 会造成第一次抢没声音
            ZqEngineKit.getInstance().muteLocalAudioStream(false)
        }
    }

    private fun joinRcRoom(deep: Int) {
        if (deep > 4) {
            MyLog.d(tag, "加入融云房间，重试5次仍然失败，放弃")
            sendFailedToServer()
            mIDoublePlayView.finishActivityWithError()
            return
        }

        if (mRoomData.gameId > 0) {
            ModuleServiceManager.getInstance().msgService.joinChatRoom(mRoomData.gameId.toString(), -1, object : ICallback {
                override fun onSucess(obj: Any?) {
                    MyLog.d(tag, "加入融云房间成功")
                }

                override fun onFailed(obj: Any?, errcode: Int, message: String?) {
                    MyLog.d(tag, "加入融云房间失败， msg is $message, errcode is $errcode")
                }
            })
            if (deep == -1) {
                /**
                 * 说明是初始化时那次加入房间，这时加入极光房间做个备份，使用tag的方案
                 */
                JiGuangPush.joinSkrRoomId(mRoomData.gameId.toString())
            }
        } else {
            MyLog.e(tag, "房间 gameId 不合法")
        }
    }

    private fun sendFailedToServer() {
        MyLog.e(tag, "sendFailedToServer, roomID is ${mRoomData.gameId}")
        val mutableSet1 = mutableMapOf("roomID" to mRoomData.gameId)
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
        ApiMethods.subscribe(mDoubleRoomServerApi.enterRoomFailed(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {

            }
        }, this@DoubleCorePresenter)

        mIDoublePlayView.finishActivity()
        ARouter.getInstance()
                .build(RouterConstants.ACTIVITY_DOUBLE_END)
                .withSerializable("roomData", mRoomData)
                .navigation()
    }

    fun pickOther() {
        if (!mUiHandler.hasMessages(PICK_MSG)) {
            mUiHandler.sendEmptyMessageDelayed(PICK_MSG, 200)
        }

        mPickNum++
    }

    fun closeByTimeOver() {
        MyLog.w(tag, "closeByTimeOver, roomID is ${mRoomData.gameId}")
        val mutableSet = mutableMapOf("roomID" to mRoomData.gameId)
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet))
        ApiMethods.subscribe(mDoubleRoomServerApi.closeByTimerOver(body), null)
        mIsCloseByTimeOver = true

        mIDoublePlayView.finishActivity()
        ARouter.getInstance()
                .build(RouterConstants.ACTIVITY_DOUBLE_END)
                .withSerializable("roomData", mRoomData)
                .navigation()
    }

    /**
     * 点击结束按钮或者返回，弹出确认退出弹窗之后退出，使用这个接口
     */
    fun exit() {
        MyLog.w(tag, "exit(), roomID is ${mRoomData.gameId}")
        val mutableSet1 = mutableMapOf("roomID" to mRoomData.gameId)
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
        ApiMethods.subscribe(mDoubleRoomServerApi.exitRoom(body), null)
    }

    fun nextSong() {
        val mutableSet1 = mutableMapOf("roomID" to mRoomData.gameId)
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
        ApiMethods.subscribe(mDoubleRoomServerApi.nextSong(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    MyLog.d(tag, "nextSong success")
                } else {
                    U.getToastUtil().showShort(obj?.errmsg)
                }
            }

            override fun onError(e: Throwable) {
                U.getToastUtil().showShort("网络错误")
            }

        }, this@DoubleCorePresenter)
    }

    fun syncStatus() {
        mUiHandler.removeMessages(SYNC_MSG)
        mUiHandler.sendEmptyMessageDelayed(SYNC_MSG, SYNC_DURATION)
        MyLog.d(tag, "syncStatus 1")
        ApiMethods.subscribe(mDoubleRoomServerApi.syncStatus(mRoomData.gameId), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    MyLog.d(tag, "syncStatus 2")
                    val model = JSON.parseObject(obj.data.toJSONString(), DoubleSyncModel::class.java)
                    if (model.status == ECombineStatus.CS_Finished.value) {
                        mIDoublePlayView.finishActivity()
                        ARouter.getInstance()
                                .build(RouterConstants.ACTIVITY_DOUBLE_END)
                                .withSerializable("roomData", mRoomData)
                                .navigation()
                    } else {
                        mRoomData.syncRoomInfo(model)
                    }
                }
            }
        }, this@DoubleCorePresenter)
    }

    fun unLockInfo() {
        val mutableSet1 = mutableMapOf("roomID" to mRoomData.gameId)
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
        ApiMethods.subscribe(mDoubleRoomServerApi.unLock(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    MyLog.d(tag, "解锁成功")
                    mIDoublePlayView.unLockSelfSuccess()
                    mRoomData.selfUnLock()
                } else {
                    MyLog.e(tag, obj?.errmsg)
                }
            }
        }, this@DoubleCorePresenter)
    }

    /**
     * 双方添加的音乐
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DoubleAddMusicEvent) {
        // 双人房都可以点歌
        if (event.mNeedLoad) {
            mRoomData.updateCombineRoomMusic(event.mCombineRoomMusic, "", false)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: CRStartByCreateNotifyEvent) {
        MyLog.d(tag, "onEvent StartCombineRoomByCreateNotifyEvent 1")
        //在唱聊房邀请别人之后当对方同意之后收到的push，如果房间人数已经2个了就说名这个房间已经
        if (mRoomData.gameId == event.roomID && !mRoomData.isRoomPrepared()) {
            //游戏开始了
            mRoomData.config = event.config
            val hashMap = HashMap<Int, UserInfoModel>()
            for (userInfoModel in event.users) {
                hashMap.put(userInfoModel.userId, userInfoModel)
            }
            mRoomData.userInfoListMap = hashMap
            mRoomData.inviterId = event.inviterId
            syncStatus()
            joinRoomAndInit(true)
            MyLog.d(tag, "onEvent StartCombineRoomByCreateNotifyEvent 2")
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: UpdateLockEvent) {
        mIDoublePlayView.showLockState(event.userID, event.isLock)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: UpdateNextSongDecEvent) {
        mIDoublePlayView.updateNextSongDec(event.dec, event.isHasNext)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: UpdateNoLimitDuraionEvent) {
        mIDoublePlayView.showNoLimitDurationState(event.isEnableNoLimitDuration)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: StartDoubleGameEvent) {
        mIDoublePlayView.startGame(event.music, event.nextDec, event.isHasNext)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ChangeSongEvent) {
        mIDoublePlayView.changeRound(event.music, event.nextDec, event.isHasNext)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DoublePickPushEvent) {
        if (event.fromPickUserID.toLong() != MyUserInfoManager.getInstance().uid) {
            mIDoublePlayView.picked(event.count)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DoubleCombineRoomSycPushEvent) {
        MyLog.d(tag, "onEvent DoubleCombineRoomSycPushEvent")
        if (event.doubleSyncModel.syncStatusTimeMs > mSyncStatusTimeMs) {
            MyLog.d(tag, "onEvent SycPush is $event")
            mSyncStatusTimeMs = event.doubleSyncModel.syncStatusTimeMs
            if (event.doubleSyncModel.status == ECombineStatus.CS_Finished.value) {
                mIDoublePlayView.finishActivity()
                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_DOUBLE_END)
                        .withSerializable("roomData", mRoomData)
                        .navigation()
            } else {
                mRoomData!!.syncRoomInfo(event.doubleSyncModel)
                /**
                 * sync开始了，但房间里的人还是少于2个，需要短链接拉一下房间人数,
                 * 这种情况是，别人进来的push没有收到的时候的一个补救
                 */
                val map = mRoomData.userInfoListMap
                if (map == null || map.size < 2) {
                    syncRoomPlayer()
                }
                mUiHandler.removeMessages(SYNC_MSG)
                mUiHandler.sendEmptyMessageDelayed(SYNC_MSG, SYNC_DURATION)
            }
        }
    }

    /**
     * 这个信息必须拉到!!
     */
    private fun syncRoomPlayer() {
        MyLog.d(tag, "syncRoomPlayer")
        Observable.create<Any> {
            ApiMethods.subscribe(mDoubleRoomServerApi.getRoomUserInfo(mRoomData.gameId), object : ApiObserver<ApiResult>() {
                override fun process(obj: ApiResult?) {
                    MyLog.w(tag, "syncRoomPlayer obj is $obj")
                    if (obj?.errno == 0 && mRoomData.userInfoListMap?.size ?: 0 < 2) {
                        val userList: List<UserInfoModel>? = JSON.parseArray(obj.data.getString("users"), UserInfoModel::class.java)

                        if (userList != null) {
                            val hashMap = HashMap<Int, UserInfoModel>()
                            for (userInfoModel in userList) {
                                hashMap.put(userInfoModel.userId, userInfoModel)
                            }
                            mRoomData.userInfoListMap = hashMap
                        }

                        joinRoomAndInit(true)
                        syncStatus()
                    }

                    it.onComplete()
                }

                override fun onError(e: Throwable) {
                    it.onError(Throwable("网络错误"))
                }

                override fun onNetworkError(errorType: ErrorType?) {
                    it.onError(Throwable("网络延迟"))
                }
            }, this@DoubleCorePresenter)
        }.compose(this@DoubleCorePresenter.bindUntilEvent(PresenterEvent.DESTROY))
                .retryWhen(RxRetryAssist(5, 2, false)).subscribe()

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DoubleEndCombineRoomPushEvent) {
        MyLog.d(tag, "DoubleEndCombineRoomPushEvent 1")
        if (event.basePushInfo.timeMs > mSyncStatusTimeMs) {
            MyLog.d(tag, "DoubleEndCombineRoomPushEvent 2")
            mRoomData!!.updateGameState(DoubleRoomData.DoubleGameState.END)
            mIDoublePlayView.gameEnd(event)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DoubleLoadMusicInfoPushEvent) {
        if (event.basePushInfo.timeMs > mSyncStatusTimeMs) {
            mRoomData!!.updateCombineRoomMusic(event.currentMusic, event.nextMusicDesc, event.isHasNext)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DoubleUnlockUserInfoPushEvent) {
        if (event.basePushInfo.timeMs > mSyncStatusTimeMs) {
            mRoomData!!.updateLockInfo(event.userLockInfo, event.isEnableNoLimitDuration)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: NoMusicEvent) {
        mIDoublePlayView.noMusic()
    }

    override fun destroy() {
        MyLog.d(tag, "destroy begin")
        super.destroy()
        if (!mIsCloseByTimeOver) exit()
        EventBus.getDefault().unregister(this)
        Params.save2Pref(ZqEngineKit.getInstance().params)
        ZqEngineKit.getInstance().destroy("doubleRoom")
        JiGuangPush.exitSkrRoomId(mRoomData.gameId.toString())
        mUiHandler.removeCallbacksAndMessages(null)
        MyLog.d(tag, "destroy end")
    }
}
