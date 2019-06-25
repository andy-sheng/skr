package com.module.playways.doubleplay.presenter

import com.alibaba.fastjson.JSON
import com.common.core.userinfo.model.LocalCombineRoomConfig
import com.common.core.userinfo.model.UserInfoModel
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.HandlerTaskTimer
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.doubleplay.DoubleRoomServerApi
import com.module.playways.doubleplay.event.EnterDoubleRoomEvent
import com.module.playways.doubleplay.inter.IMatchView
import com.module.playways.doubleplay.pbLocalModel.LocalAgoraTokenInfo
import com.common.notification.event.DoubleStartCombineRoomByMatchPushEvent
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class DoubleMatchPresenter(val iMatchView: IMatchView) : RxLifeCyclePresenter() {
    val mTag = "DoubleMatchPresenter"
    val intervalDuration = 10000L
    private var doubleRoomServerApi = ApiManager.getInstance().createService(DoubleRoomServerApi::class.java)
    private var handlerTaskTimer: HandlerTaskTimer? = null

    init {
        EventBus.getDefault().register(this)
    }

    fun startMatch() {
        handlerTaskTimer?.dispose()
        handlerTaskTimer = HandlerTaskTimer
                .newBuilder()
                .interval(intervalDuration)
                .compose(this@DoubleMatchPresenter)
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(t: Int) {
                        MyLog.d(mTag, "startMatch onNext")
                        val mutableSet = mutableMapOf<String, Any>("platform" to 20)
                        mutableSet["userClick"] = t == 1
                        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet))
                        ApiMethods.subscribe(doubleRoomServerApi.quaryMatch(body), object : ApiObserver<ApiResult>() {
                            override fun process(obj: ApiResult?) {
                                if (obj?.errno == 0) {
                                    val hasMatchedRoom = obj.data.getBoolean("hasMatchedRoom")
                                    if (hasMatchedRoom) {
                                        handlerTaskTimer?.dispose()

                                        val doubleRoomData = DoubleRoomData()
                                        doubleRoomData.gameId = obj.data.getIntValue("roomID")
                                        doubleRoomData.enableNoLimitDuration = false
                                        doubleRoomData.passedTimeMs = obj.data.getLongValue("passedTimeMs")
                                        doubleRoomData.config = JSON.parseObject(obj.data.getString("config"), LocalCombineRoomConfig::class.java)
                                        val userList = JSON.parseArray(obj.data.getString("users"), UserInfoModel::class.java)

                                        val hashMap = HashMap<Int, UserInfoModel>()
                                        for (userInfoModel in userList) {
                                            hashMap.put(userInfoModel.userId, userInfoModel)
                                        }
                                        doubleRoomData.userInfoListMap = hashMap

                                        doubleRoomData.tokens = JSON.parseArray(obj.data.getString("tokens"), LocalAgoraTokenInfo::class.java)
                                        doubleRoomData.needMaskUserInfo = obj.data.getBooleanValue("needMaskUserInfo")
                                        doubleRoomData.doubleRoomOri = DoubleRoomData.DoubleRoomOri.MATCH

                                        iMatchView.matchSuccessFromHttp(doubleRoomData)
                                    }
                                }
                            }
                        }, this@DoubleMatchPresenter)
                    }
                })
    }

    fun cancelMatch() {
        val mutableSet1 = mutableMapOf("platform" to 20)
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
        ApiMethods.subscribe(doubleRoomServerApi.cancleMatch(body), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult?) {

            }
        }, this@DoubleMatchPresenter)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: DoubleStartCombineRoomByMatchPushEvent) {
        iMatchView.matchSuccessFromPush(event)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EnterDoubleRoomEvent) {
        iMatchView.toDoubleRoomByPush()
    }

    override fun destroy() {
        super.destroy()
        EventBus.getDefault().unregister(this)
        handlerTaskTimer?.dispose()
        cancelMatch()
    }
}
