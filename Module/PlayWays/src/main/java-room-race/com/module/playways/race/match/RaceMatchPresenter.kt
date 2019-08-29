package com.module.playways.race.match

import com.alibaba.fastjson.JSON
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.module.playways.race.RaceRoomServerApi
import com.module.playways.race.match.model.JoinRaceRoomRspModel
import com.module.playways.race.match.pbLocalModel.LocalRJoinActionMsg
import com.module.playways.room.msg.event.raceroom.RJoinActionEvent
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RaceMatchPresenter(val mIRaceMatchingView: IRaceMatchingView) : RxLifeCyclePresenter() {
    val mTag = "RaceMatchPresenter"
    val raceRoomServerApi = ApiManager.getInstance().createService(RaceRoomServerApi::class.java)

    init {

    }

    fun startLoopMatchTask() {
        launch {
            repeat(Int.MAX_VALUE) {
                val map = mutableMapOf("platform" to 20)
                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                val result = subscribe { raceRoomServerApi.queryMatch(body) }
                if (result.errno == 0) {
                    val model = JSON.parseObject(result.data.getString("mathedInfo"), LocalRJoinActionMsg::class.java)
                    if (model != null) {
                        MyLog.d(mTag, "model is = $model")
                        joinRoom(model)
                    }
                }
                delay(10000)
            }
        }
    }

    // 匹配到了
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(e: RJoinActionEvent) {
        MyLog.d(mTag, "onEvent e = ${e.pb}")
        joinRoom(LocalRJoinActionMsg.toLocalModel(e.pb))
    }

    // 进入房间
    fun joinRoom(localRJoinActionMsg: LocalRJoinActionMsg?) {
        MyLog.w(TAG, "joinRoom pb = $localRJoinActionMsg")
        localRJoinActionMsg?.let {
            launch {
                val map = mutableMapOf(
                        "platform" to 20,
                        "roomID" to it.gameID
                )
                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                val result = subscribe { raceRoomServerApi.joinRoom(body) }
                if (result.errno == 0) {
                    val rsp = JSON.parseObject(result.data.toJSONString(), JoinRaceRoomRspModel::class.java)
                    rsp.roomID = it.gameID
                    rsp.gameCreateTimeMs = it.createTimeMs
                    rsp.agoraToken = it.agoraToken
                    // TODO 跳到RaceRoomActivity
                    mIRaceMatchingView.matchRaceSucess(rsp)
                }
            }
        }
    }

    fun cancelMatch() {
        GlobalScope.launch {
            val map = mutableMapOf("platform" to 20)
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            subscribe { raceRoomServerApi.cancelMatch(body) }
        }
    }
}