package com.module.playways.doubleplay.presenter

import android.os.Handler
import android.os.Message
import com.alibaba.fastjson.JSON
import com.common.core.account.UserAccountManager
import com.common.jiguang.JiGuangPush
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.engine.Params
import com.module.ModuleServiceManager
import com.module.common.ICallback
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.doubleplay.DoubleRoomServerApi
import com.module.playways.doubleplay.event.ChangeSongEvent
import com.module.playways.doubleplay.event.StartDoubleGameEvent
import com.module.playways.doubleplay.event.UpdateLockEvent
import com.module.playways.doubleplay.event.UpdateNoLimitDuraionEvent
import com.module.playways.doubleplay.inter.IDoublePlayView
import com.module.playways.doubleplay.pushEvent.*
import com.zq.mediaengine.kit.ZqEngineKit
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class DoubleCorePresenter(private val mRoomData: DoubleRoomData, private val mIDoublePlayView: IDoublePlayView) : RxLifeCyclePresenter() {
    val tag = "DoubleCorePresenter"

    val SYNC_MSG = 0
    val SYNC_DURATION = 12000L

    internal var syncStatusTimeMs: Long = 0 //状态同步时的毫秒时间戳
    private var mDoubleRoomServerApi = ApiManager.getInstance().createService(DoubleRoomServerApi::class.java)

    var uiHandler: Handler

    init {
        EventBus.getDefault().register(this)
        joinRoomAndInit(true)
        uiHandler = object : Handler() {
            override fun handleMessage(msg: Message?) {
                if (msg?.what == SYNC_MSG) {
                    syncStatus()
                }
            }
        }

        uiHandler.sendEmptyMessageDelayed(SYNC_MSG, SYNC_DURATION)
    }

    /**
     * 加入引擎房间
     * 加入融云房间
     * 系统消息弹幕
     */
    private fun joinRoomAndInit(first: Boolean) {
//        MyLog.w(TAG, "joinRoomAndInit" + " first=" + first + ", gameId is " + mRoomData.getGameId())

        if (mRoomData.gameId > 0) {
            if (first) {
                val params = Params.getFromPref()
                params.scene = Params.Scene.grab
                params.isEnableVideo = false
                ZqEngineKit.getInstance().init("doubleRoom", params)
            }
            ZqEngineKit.getInstance().joinRoom(mRoomData.gameId.toString(), UserAccountManager.getInstance().uuidAsLong.toInt(), false, mRoomData.getToken())
            // 不发送本地音频, 会造成第一次抢没声音
            ZqEngineKit.getInstance().muteLocalAudioStream(true)
        }

        joinRcRoom(-1)
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
                override fun onSucess(obj: Any) {
                    MyLog.d(tag, "加入融云房间成功")
                }

                override fun onFailed(obj: Any, errcode: Int, message: String) {
                    MyLog.d(tag, "加入融云房间失败， msg is $message, errcode is $errcode")
                    joinRcRoom(deep + 1)
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
        val mutableSet1 = mutableMapOf<String, Objects>()
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
        ApiMethods.subscribe(mDoubleRoomServerApi.enterRoomFailed(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {

            }
        }, this@DoubleCorePresenter)
    }

    fun pickOther() {
        val mutableSet1 = mutableMapOf<String, Objects>()
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
        ApiMethods.subscribe(mDoubleRoomServerApi.pickOther(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {

            }
        }, this@DoubleCorePresenter)
    }

    fun closeByTimeOver() {
        val mutableSet1 = mutableMapOf<String, Objects>()
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
        ApiMethods.subscribe(mDoubleRoomServerApi.closeByTimerOver(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {

            }
        }, this@DoubleCorePresenter)
    }

    fun exit() {
        val mutableSet1 = mutableMapOf<String, Objects>()
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
        ApiMethods.subscribe(mDoubleRoomServerApi.exitRoom(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {

            }
        }, this@DoubleCorePresenter)
    }

    fun nextSong() {
        val mutableSet1 = mutableMapOf<String, Objects>()
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
        ApiMethods.subscribe(mDoubleRoomServerApi.nextSong(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {

            }
        }, this@DoubleCorePresenter)
    }

    fun syncStatus() {
        ApiMethods.subscribe(mDoubleRoomServerApi.syncStatus(), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                uiHandler.removeMessages(SYNC_MSG)
                uiHandler.sendEmptyMessageDelayed(SYNC_MSG, SYNC_DURATION)
            }
        }, this@DoubleCorePresenter)
    }

    fun unLockInfo() {
        val mutableSet1 = mutableMapOf<String, Objects>()
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
        ApiMethods.subscribe(mDoubleRoomServerApi.unLock(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {

            }
        }, this@DoubleCorePresenter)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: UpdateLockEvent) {
        mIDoublePlayView.showLockState(event.userID, event.isLock)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: UpdateNoLimitDuraionEvent) {
        mIDoublePlayView.showNoLimitDurationState(event.isEnableNoLimitDuration)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: StartDoubleGameEvent) {
        mIDoublePlayView.startGame(event.songModel, event.nextDec)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ChangeSongEvent) {
        mIDoublePlayView.changeRound(event.songModel, event.nextDec)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DoublePickPushEvent) {
        mIDoublePlayView.picked()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DoubleCombineRoomSycPushEvent) {
        if (event.doubleSyncModel.syncStatusTimeMs > syncStatusTimeMs) {
            syncStatusTimeMs = event.doubleSyncModel.syncStatusTimeMs
            mRoomData!!.syncRoomInfo(event.doubleSyncModel)
            uiHandler.removeMessages(SYNC_MSG)
            uiHandler.sendEmptyMessageDelayed(SYNC_MSG, SYNC_DURATION)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DoubleEndCombineRoomPushEvent) {
        if (event.basePushInfo.timeMs > syncStatusTimeMs) {
            mRoomData!!.updateGameState(DoubleRoomData.DoubleGameState.END)
            mIDoublePlayView.gameEnd(event)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DoubleLoadMusicInfoPushEvent) {
        if (event.basePushInfo.timeMs > syncStatusTimeMs) {
            mRoomData!!.updateCombineRoomMusic(event.currentMusic, event.nextMusicDesc)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DoubleUnlockUserInfoPushEvent) {
        if (event.basePushInfo.timeMs > syncStatusTimeMs) {
            mRoomData!!.updateLockInfo(event.userLockInfo, event.isEnableNoLimitDuration)
        }
    }

    override fun destroy() {
        MyLog.d(tag, "destroy begin")
        super.destroy()
        EventBus.getDefault().unregister(this)
        Params.save2Pref(ZqEngineKit.getInstance().params)
        ZqEngineKit.getInstance().destroy("doubleRoom")
        JiGuangPush.exitSkrRoomId(mRoomData.gameId.toString())
        uiHandler.removeCallbacksAndMessages(null)
        MyLog.d(tag, "destroy end")
    }
}
