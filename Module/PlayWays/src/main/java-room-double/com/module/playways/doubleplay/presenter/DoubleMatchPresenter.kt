package com.module.playways.doubleplay.presenter

import com.alibaba.fastjson.JSON
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.HandlerTaskTimer
import com.module.playways.doubleplay.DoubleRoomServerApi
import com.module.playways.doubleplay.event.EnterDoubleRoomEvent
import com.module.playways.doubleplay.inter.IMatchView
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class DoubleMatchPresenter(val iMatchView: IMatchView) : RxLifeCyclePresenter() {
    private var doubleRoomServerApi = ApiManager.getInstance().createService(DoubleRoomServerApi::class.java)
    private var handlerTaskTimer: HandlerTaskTimer? = null

    init {
        EventBus.getDefault().register(this)
    }

    fun startMatch() {
        handlerTaskTimer?.dispose()
        handlerTaskTimer = HandlerTaskTimer
                .newBuilder()
                .interval(5000)
                .compose(this@DoubleMatchPresenter)
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(t: Int) {
                        val mutableSet1 = mutableMapOf<String, Objects>()
                        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(mutableSet1))
                        ApiMethods.subscribe(doubleRoomServerApi.quaryMatch(body), object : ApiObserver<ApiResult>() {
                            override fun process(obj: ApiResult?) {
                                TODO("假数据")
                                if (obj?.errno == 0) {
                                    iMatchView.matchSuccess()
                                }
                            }
                        }, this@DoubleMatchPresenter)
                    }
                })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: EnterDoubleRoomEvent) {
        iMatchView.toDoubleRoomByPush()
    }

    override fun destroy() {
        super.destroy()
        EventBus.getDefault().unregister(this)
        handlerTaskTimer?.dispose()
    }
}
