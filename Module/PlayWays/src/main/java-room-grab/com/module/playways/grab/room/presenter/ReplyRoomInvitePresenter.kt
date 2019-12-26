package com.module.playways.grab.room.presenter

import android.content.Intent
import com.alibaba.fastjson.JSON
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.HandlerTaskTimer
import com.common.utils.U
import com.module.playways.doubleplay.DoubleRoomServerApi
import com.module.playways.relay.match.model.JoinRelayRoomRspModel
import com.module.playways.relay.room.RelayRoomActivity
import com.module.playways.relay.room.RelayRoomData
import okhttp3.MediaType
import okhttp3.RequestBody

class ReplyRoomInvitePresenter() : RxLifeCyclePresenter() {
    private val mTag = "ReplyRoomInvitePresenter"
    private var mDoubleRoomServerApi = ApiManager.getInstance().createService(DoubleRoomServerApi::class.java)
    private var handlerTaskTimer: HandlerTaskTimer? = null

    /**
     * 邀请一个人去双人房
     */
    fun inviteToReplyRoom(inviteUserID: Int) {
        sendInvite(inviteUserID)
    }

    private fun sendInvite(inviteUserID: Int) {
        val mutableSet1 = mutableMapOf("inviteUserID" to inviteUserID)
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
        ApiMethods.subscribe(mDoubleRoomServerApi.sendRelayInvite(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    startCheckLoop()
                    U.getToastUtil().showShort("邀请成功")
                } else {
//                    if (obj?.errno == 8376040) {
//                        StatisticsAdapter.recordCountEvent("cp", "invite2_outchance", null)
//                    }
                    U.getToastUtil().showShort(obj?.errmsg)
                }
            }

            override fun onNetworkError(errorType: ErrorType?) {
                U.getToastUtil().showShort("网络错误")
            }

            override fun onError(e: Throwable) {
                U.getToastUtil().showShort("请求错误")
            }
        }, this)
    }

    private fun startCheckLoop() {
        handlerTaskTimer?.dispose()
        handlerTaskTimer = HandlerTaskTimer
                .newBuilder()
                .interval(3000)
                .delay(500)
                .take(3)
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(t: Int) {
                        getInviteState()
                    }
                })
    }

    /**
     * 发出邀请之后轮询检查对方的进房情况，因为push有可能会丢
     */
    private fun getInviteState() {
        ApiMethods.subscribe(mDoubleRoomServerApi.getRelayInviteEnterResult(), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0 && obj.data.getBooleanValue("hasInvitedRoom")) {
                    val relayRoomData = RelayRoomData()
                    val joinRelayRoomRspModel = JSON.parseObject(obj.data.toJSONString(), JoinRelayRoomRspModel::class.java)
                    relayRoomData.loadFromRsp(joinRelayRoomRspModel)
                    joinRelayRoomRspModel.enterType = RelayRoomData.EnterType.INVITE

                    val intent = Intent(U.app(), RelayRoomActivity::class.java)
                    intent.putExtra("JoinRelayRoomRspModel", joinRelayRoomRspModel)
                    U.app().startActivity(intent)
                }
            }
        }, this)
    }

    override fun destroy() {
        super.destroy()
        handlerTaskTimer?.dispose()
    }
}