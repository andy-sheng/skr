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
import com.zq.live.proto.RaceRoom.RJoinActionMsg
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RaceMatchPresenter : RxLifeCyclePresenter() {
    val raceRoomServerApi = ApiManager.getInstance().createService(RaceRoomServerApi::class.java)

    init {

    }

    fun queryMatch() {
        launch {
            val map = mutableMapOf("platform" to 20)
            val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
            val result = subscribe { raceRoomServerApi.queryMatch(body) }
            // 处理结果
        }
    }

    // 匹配到了
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(e: RJoinActionEvent) {
        joinRoom(e)
    }


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
                ARouter.getInstance().build(RouterConstants.ACTIVITY_RACE_ROOM)
                        .withSerializable("JoinRaceRoomRspModel", rsp)
                        .navigation()
            }
        }
    }
}