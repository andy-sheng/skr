package com.module.playways.grab.room.presenter

import com.common.core.myinfo.MyUserInfoManager
import com.common.log.MyLog
import com.common.mvp.PresenterEvent
import com.common.mvp.RxLifeCyclePresenter
import com.common.rx.RxRetryAssist
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.ActivityUtils
import com.common.utils.HandlerTaskTimer
import com.common.utils.U
import com.module.playways.grab.room.GrabRoomServerApi
import com.module.playways.grab.room.view.IUpdateFreeGiftCountView
import com.module.playways.room.gift.event.CancelGiftCountDownEvent
import com.module.playways.room.gift.event.StartGiftCountDownEvent
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Action
import io.reactivex.functions.Consumer
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.*

class GiftTimerPresenter(val mUpdateCallBack: IUpdateFreeGiftCountView) : RxLifeCyclePresenter() {
    val mTag = "GiftTimerPresenter"

    /**
     * 最近一次开始任务的时间
     */
    var mRecentStartTs: Long = 0

    var mHandlerTaskTimer: HandlerTaskTimer? = null

    internal var mRoomServerApi = ApiManager.getInstance().createService(GrabRoomServerApi::class.java)

    private val mDuration: Long = 61000

    /**
     * startTimer的时候需要delay的时间，因为中途timer可以被打断
     */
    private var mFirstDelay = mDuration

    init {
        EventBus.getDefault().register(this)
    }

    fun startTimer() {
        MyLog.d(mTag, "startTimer()")
        mRecentStartTs = System.currentTimeMillis()

        mHandlerTaskTimer?.dispose()

        mHandlerTaskTimer = HandlerTaskTimer.newBuilder().delay(mFirstDelay).start(object : HandlerTaskTimer.ObserverW() {
            override fun onNext(t: Int) {
                getFreeGiftCount()
            }
        })
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ActivityUtils.ForeOrBackgroundChange) {
        MyLog.d(mTag, if (event.foreground) "切换到前台" else "切换到后台")
        if (event.foreground) {
            startTimer()
            EventBus.getDefault().post(StartGiftCountDownEvent())
        } else {
            pauseTimer()
            EventBus.getDefault().post(CancelGiftCountDownEvent())
        }
    }

    fun getCountDownSecond(): Long {
        if (mHandlerTaskTimer?.isDisposed() ?: true) {
            MyLog.d(mTag, "mHandlerTaskTimer is null or is disposed")
            return mDuration
        }

        val second = mDuration - (mFirstDelay - (System.currentTimeMillis() - mRecentStartTs))
        MyLog.d(mTag, "getCountDownSecond() second is " + second)
        return second
    }

    private fun getFreeGiftCount() {
        MyLog.d(mTag, "getFreeGiftCount()")
        //得到还剩下多少，更新界面
        val ts = System.currentTimeMillis()
        val map = HashMap<String, Any>()
        map["timestamp"] = ts
        map["userID"] = MyUserInfoManager.getInstance().uid
        map["appSecret"] = "bf9502ede1260d1109c46c2301721efa"

        val signV2 = U.getMD5Utils().signReq(map)

        Observable.create<ApiResult> {
            ApiMethods.subscribe(mRoomServerApi.punch(ts, signV2), object : ApiObserver<ApiResult>() {
                override fun process(obj: ApiResult?) {
                    if (obj?.errno == 0) {
                        mUpdateCallBack?.updateGiftCount(obj.data.getIntValue("flower"), obj.data.getLongValue("timestamp"))
                    }

                    it.onComplete()
                }

                override fun onNetworkError(errorType: ErrorType?) {
                    it.onError(Throwable("网络延迟"))
                }

                override fun onError(e: Throwable) {
                    it.onError(Throwable("请求错误"))
                }
            }, this@GiftTimerPresenter)
        }.retryWhen(RxRetryAssist(3, 1, false))
                .observeOn(AndroidSchedulers.mainThread())
                .compose(this@GiftTimerPresenter.bindUntilEvent(PresenterEvent.DESTROY))
                .subscribe(
                        Consumer { newReincarnation() }
                        , Consumer {
                    newReincarnation()
                    MyLog.e(mTag, it)
                }, Action { newReincarnation() })
    }

    private fun newReincarnation() {
        mFirstDelay = mDuration
        startTimer()
        MyLog.d(mTag, "getFreeGiftCount() mRecentStartTs is ${mRecentStartTs}, mFirstDelay is ${mFirstDelay}")
        EventBus.getDefault().post(StartGiftCountDownEvent())
    }

    private fun pauseTimer() {
        mHandlerTaskTimer?.dispose()
        mFirstDelay = mFirstDelay.minus((System.currentTimeMillis() - mRecentStartTs))
        MyLog.d(mTag, "pauseTimer() mFirstDelay is ${mFirstDelay}")
    }

    override fun destroy() {
        super.destroy()
        mHandlerTaskTimer?.dispose()
        EventBus.getDefault().unregister(this)
    }
}