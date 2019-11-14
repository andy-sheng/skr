package com.module.playways.race.match

import com.alibaba.fastjson.JSON
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.subscribe
import com.common.utils.U
import com.module.playways.race.RaceRoomServerApi
import com.module.playways.race.match.model.JoinRaceRoomRspModel
import com.module.playways.race.match.pbLocalModel.LocalRJoinActionMsg
import com.zq.live.proto.RaceRoom.RJoinActionMsg
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class RaceMatchPresenter(val mIRaceMatchingView: IRaceMatchingView,val audience:Boolean) : RxLifeCyclePresenter() {
    val mTag = "RaceMatchPresenter"
    val raceRoomServerApi = ApiManager.getInstance().createService(RaceRoomServerApi::class.java)

    init {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this)
        }
    }

    override fun destroy() {
        super.destroy()
        EventBus.getDefault().unregister(this)
    }

    fun startLoopMatchTask() {
        launch {
            repeat(Int.MAX_VALUE) {
                val map = mutableMapOf("platform" to 20)
                val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
                val result = subscribe {
                    if(audience){
                        raceRoomServerApi.audienceQueryMatch(body)
                    }else{
                        raceRoomServerApi.queryMatch(body)
                    }
                }
                if (result.errno == 0) {
                    val model = JSON.parseObject(result.data.getString("mathedInfo"), LocalRJoinActionMsg::class.java)
                    if (model != null) {
                        MyLog.d(mTag, "model is = $model")
                        joinRoom(model)
                    }
                } else {
                    U.getToastUtil().showShort(result.errmsg)
                }
                delay(10000)
            }
        }
    }

    // 匹配到了
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(e: RJoinActionMsg) {
        MyLog.d(mTag, "onEvent e = ${e}")
        joinRoom(LocalRJoinActionMsg.toLocalModel(e))
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
                val result = subscribe {
                    if(audience){
                        raceRoomServerApi.audienceJoinRoom(body)
                    }else{
                        raceRoomServerApi.joinRoom(body)
                    }
                }
                if (result.errno == 0) {
                    val rsp = JSON.parseObject(result.data.toJSONString(), JoinRaceRoomRspModel::class.java)
                    rsp.isAudience = audience
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
            subscribe {
                if(audience){
                    raceRoomServerApi.audienceCancelMatch(body)
                }else{
                    raceRoomServerApi.cancelMatch(body)
                }
            }
        }
    }

}