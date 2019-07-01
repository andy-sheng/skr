package com.module.playways.doubleplay.presenter

import com.alibaba.fastjson.JSON
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.notification.event.CRStartByMatchPushEvent
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.statistics.StatisticsAdapter
import com.common.utils.HandlerTaskTimer
import com.common.utils.U
import com.module.playways.doubleplay.DoubleRoomData
import com.module.playways.doubleplay.DoubleRoomServerApi
import com.module.playways.doubleplay.inter.IMatchView
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

    fun startMatch(isMan: Boolean) {
        StatisticsAdapter.recordCountEvent("cp", "pairing_ready", null)
        handlerTaskTimer?.dispose()
        handlerTaskTimer = HandlerTaskTimer
                .newBuilder()
                .interval(intervalDuration)
                .take(7)
                .compose(this@DoubleMatchPresenter)
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(t: Int) {
                        MyLog.d(mTag, "startMatch onNext")
                        val mutableSet = mutableMapOf<String, Any>("platform" to 20, "sex" to if (isMan) 1 else 2)
                        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet))
                        ApiMethods.subscribe(doubleRoomServerApi.quaryMatch(body), object : ApiObserver<ApiResult>() {
                            override fun process(obj: ApiResult?) {
                                if (obj?.errno == 0) {
                                    val hasMatchedRoom = obj.data.getBoolean("hasMatchedRoom")
                                    if (hasMatchedRoom) {
                                        handlerTaskTimer?.dispose()
                                        val doubleRoomData = DoubleRoomData.makeRoomDataFromJsonObject(obj.data)
                                        doubleRoomData.doubleRoomOri = DoubleRoomData.DoubleRoomOri.MATCH
                                        iMatchView.matchSuccessFromHttp(doubleRoomData)
                                    }
                                }
                            }
                        }, this@DoubleMatchPresenter)
                    }

                    override fun onComplete() {
                        iMatchView.finishActivity()
                        U.getToastUtil().showShort("当前匹配的人比较少，请稍后再试")
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

    fun getBgMusic() {
        ApiMethods.subscribe(doubleRoomServerApi.doubleMatchMusic, object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult?) {
                if (result?.errno == 0) {
                    var list: List<String>? = JSON.parseArray(result.data.getString("musicURL"), String::class.java)
                    if (list != null && list.isNotEmpty()) {
                        Collections.shuffle(list)
                        iMatchView.playBgMusic(list[0])
                    }
                } else {
                    // 请求出错
                }
            }
        }, this@DoubleMatchPresenter)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: CRStartByMatchPushEvent) {
        iMatchView.matchSuccessFromPush(event)
    }

    override fun destroy() {
        super.destroy()
        EventBus.getDefault().unregister(this)
        handlerTaskTimer?.dispose()
        cancelMatch()
    }
}
