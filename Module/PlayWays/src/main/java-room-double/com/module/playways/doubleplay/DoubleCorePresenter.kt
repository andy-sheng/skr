package com.module.playways.doubleplay

import com.alibaba.fastjson.JSON
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.module.playways.doubleplay.event.ChangeSongEvent
import com.module.playways.doubleplay.event.StartDoubleGameEvent
import com.module.playways.doubleplay.event.UpdateLockEvent
import com.module.playways.doubleplay.event.UpdateNoLimitDuraionEvent
import com.module.playways.doubleplay.inter.IDoublePlayView
import com.module.playways.doubleplay.pushEvent.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class DoubleCorePresenter(internal var mIDoublePlayView: IDoublePlayView) : RxLifeCyclePresenter() {
    private var mRoomData: DoubleRoomData? = null

    internal var syncStatusTimeMs: Long = 0 //状态同步时的毫秒时间戳
    private var mDoubleRoomServerApi = ApiManager.getInstance().createService(DoubleRoomServerApi::class.java)

    init {
        EventBus.getDefault().register(this)
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

    fun enterRoomFailed() {
        val mutableSet1 = mutableMapOf<String, Objects>()
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
        ApiMethods.subscribe(mDoubleRoomServerApi.enterRoomFailed(body), object : ApiObserver<ApiResult>() {
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
        super.destroy()
        EventBus.getDefault().unregister(this)
    }
}
