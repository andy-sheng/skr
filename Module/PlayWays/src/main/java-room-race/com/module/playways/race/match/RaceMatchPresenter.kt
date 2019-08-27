package com.module.playways.race.match

import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.module.RouterConstants
import com.module.playways.race.RaceRoomServerApi
import com.module.playways.race.match.model.JoinRaceRoomRspModel
import com.module.playways.room.msg.event.raceroom.RJoinActionEvent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RaceMatchPresenter(val mIRaceMatchingView: IRaceMatchingView) : RxLifeCyclePresenter() {
    val raceRoomServerApi = ApiManager.getInstance().createService(RaceRoomServerApi::class.java)

    init {

    }

    fun startLoopMatchTask() {
        launch {
            repeat(Int.MAX_VALUE) {
                val map = mutableMapOf("platform" to 20)
                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                val result = subscribe { raceRoomServerApi.queryMatch(body) }
                delay(10000)
            }
        }
    }

    // 匹配到了
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(e: RJoinActionEvent) {
        joinRoom(e)
    }

    // 进入房间
    fun joinRoom(e: RJoinActionEvent) {
        launch {
            val map = mutableMapOf(
                    "platform" to 20,
                    "roomID" to e.pb.gameID
            )
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe { raceRoomServerApi.joinRoom(body) }
            if (result.errno == 0) {
                val rsp = JSON.parseObject(result.data.toJSONString(), JoinRaceRoomRspModel::class.java)
                rsp.roomID = e.pb.gameID
                // TODO 跳到RaceRoomActivity
                mIRaceMatchingView.matchRaceSucess(rsp)
            }
        }
    }

    fun cancelMatch() {
        launch {
            val map = mutableMapOf("platform" to 20)
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe { raceRoomServerApi.cancelMatch(body) }
        }
    }
}