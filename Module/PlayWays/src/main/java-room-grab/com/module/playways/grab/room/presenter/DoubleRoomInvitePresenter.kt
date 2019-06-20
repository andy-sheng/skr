package com.module.playways.grab.room.presenter

import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.core.userinfo.model.UserInfoModel
import com.common.mvp.RxLifeCyclePresenter
import com.common.notification.event.CombineRoomSyncInviteUserNotifyEvent
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.HandlerTaskTimer
import com.common.utils.U
import com.module.RouterConstants
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.doubleplay.DoubleRoomServerApi
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import java.util.*

class DoubleRoomInvitePresenter() : RxLifeCyclePresenter() {
    private val mTag = "DoubleRoomInvitePresenter"
    private var mDoubleRoomServerApi = ApiManager.getInstance().createService(DoubleRoomServerApi::class.java)
    private var handlerTaskTimer: HandlerTaskTimer? = null

    init {
        EventBus.getDefault().register(this)
    }

    /**
     * 邀请一个人去双人房
     */
    fun inviteToDoubleRoom() {
        val mutableSet1 = mutableMapOf<String, Objects>()
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
        ApiMethods.subscribe(mDoubleRoomServerApi.sendInvite(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    startCheckLoop()
                } else {
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
                .interval(5000)
                .delay(500)
                .take(6)
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
        val mutableSet1 = mutableMapOf<String, Objects>()
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
        ApiMethods.subscribe(mDoubleRoomServerApi.getInviteEnterResult(), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {
                if (obj?.errno == 0) {
                    val event = obj as CombineRoomSyncInviteUserNotifyEvent
                    val doubleRoomData = DoubleRoomData()
                    doubleRoomData.gameId = event.roomID
                    doubleRoomData.enableNoLimitDuration = true
                    doubleRoomData.passedTimeMs = event.passedTimeMs
                    doubleRoomData.config = event.config

                    val hashMap = HashMap<Int, UserInfoModel>()
                    for (userInfoModel in event.users) {
                        hashMap.put(userInfoModel.userId, userInfoModel)
                    }

                    doubleRoomData.userInfoList = hashMap
                    ARouter.getInstance().build(RouterConstants.ACTIVITY_DOUBLE_PLAY)
                            .withSerializable("roomData", doubleRoomData)
                            .navigation()
                }
            }
        }, this)
    }

    override fun destroy() {
        super.destroy()
        handlerTaskTimer?.dispose()
        EventBus.getDefault().unregister(this)
    }
}