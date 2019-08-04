package com.module.home.game.presenter

import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.common.core.account.event.AccountEvent
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.*
import com.common.utils.HandlerTaskTimer
import com.common.utils.U
import com.component.busilib.friends.GrabSongApi
import com.component.busilib.friends.RecommendModel
import com.component.busilib.recommend.RA
import com.module.home.MainPageSlideApi
import com.module.home.event.CheckInSuccessEvent
import com.module.home.game.view.IQuickGameView3
import com.module.home.model.SlideShowModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class QuickGamePresenter(internal var mIGameView3: IQuickGameView3) : RxLifeCyclePresenter() {

    private val mMainPageSlideApi: MainPageSlideApi = ApiManager.getInstance().createService(MainPageSlideApi::class.java)
    private val mGrabSongApi: GrabSongApi = ApiManager.getInstance().createService(GrabSongApi::class.java)

    private var mLastUpdateOperaArea = 0L    //广告位上次更新成功时间
    private var mLastUpdateRemainTime = 0L   // 上次拉取唱聊剩余次数时间
    //    private var mLastUpdateQuickInfo: Long = 0    //快速加入房间更新成功时间
//    private var mIsFirstQuick = true
    private var mRecommendTimer: HandlerTaskTimer? = null
    private var mLastUpdateRecomendInfo = 0L    //上次拉去推荐房间剩余次数的时间
    var mRecommendInterval: Int = 0    // 拉去推荐房的时间间隔

    init {
        addToLifeCycle()
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    fun checkTaskRedDot() {
        ApiMethods.subscribe(mMainPageSlideApi.taskRedDotState(), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult) {
                if (obj.errno == 0) {
                    val showRedDot = JSON.parseObject(obj.data!!.getString("has"), Boolean::class.java)
                    mIGameView3.showTaskRedDot(showRedDot!!)
                } else {
                    MyLog.w(TAG, "checkTaskRedDot $obj")
                }
            }
        }, this, RequestControl("checkTaskRedDot", ControlType.CancelThis))
    }

    fun initOperationArea(isFlag: Boolean) {
        val now = System.currentTimeMillis()
        if (!isFlag) {
            // 距离上次拉去已经超过30秒了
            if (now - mLastUpdateOperaArea < 30 * 1000) {
                return
            }
        }

        val slideshow = U.getPreferenceUtils().getSettingString("slideshow", "")
        if (!TextUtils.isEmpty(slideshow)) {
            try {
                val slideShowModelList = JSON.parseArray(slideshow, SlideShowModel::class.java)
                mIGameView3.setBannerImage(slideShowModelList)
            } catch (e: Exception) {
                MyLog.e(TAG, e)
            }

        }

        ApiMethods.subscribe(mMainPageSlideApi.slideList, object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    mLastUpdateOperaArea = System.currentTimeMillis()
                    var slideShowModelList: List<SlideShowModel>? = JSON.parseArray(result.data!!.getString("slideshow"), SlideShowModel::class.java)
                    U.getPreferenceUtils().setSettingString("slideshow", result.data!!.getString("slideshow"))
                    mIGameView3.setBannerImage(slideShowModelList)
                }
            }

            override fun onError(e: Throwable) {
                MyLog.e(TAG, "slideList error " + e)
                U.getToastUtil().showShort("网络异常")
            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                U.getToastUtil().showShort("网络超时")
            }
        }, this, RequestControl("getSlideList", ControlType.CancelThis))
    }

//    fun initQuickRoom(isFlag: Boolean) {
//        MyLog.d(TAG, "initQuickRoom isFlag=$isFlag")
//        val now = System.currentTimeMillis()
//        if (!isFlag) {
//            // 半个小时更新一次吧
//            if (now - mLastUpdateQuickInfo < 30 * 60 * 1000) {
//                return
//            }
//        }
//
//        var spResult = ""
//        if (mIsFirstQuick) {
//            // 先用SP里面的
//            mIsFirstQuick = false
//            spResult = U.getPreferenceUtils().getSettingString(U.getPreferenceUtils().longlySp(), "quick_romms", "")
//            if (!TextUtils.isEmpty(spResult)) {
//                try {
//                    var jsonObject = JSON.parseObject(spResult, JSONObject::class.java)
//                    var list = JSON.parseArray(jsonObject.getString("tags"), SpecialModel::class.java)
//                    var offset = jsonObject.getIntValue("offset")
//                    mIGameView3.setQuickRoom(list, offset)
//                } catch (e: Exception) {
//                }
//
//            }
//        }
//
//        val finalSpResult = spResult
//        ApiMethods.subscribe(mGrabSongApi.getSepcialList(0, 20), object : ApiObserver<ApiResult>() {
//            override fun process(obj: ApiResult) {
//                if (obj.errno == 0) {
//                    mLastUpdateQuickInfo = System.currentTimeMillis()
//                    if (obj.data!!.toJSONString() != finalSpResult) {
//                        U.getPreferenceUtils().setSettingString(U.getPreferenceUtils().longlySp(), "quick_romms", obj.data!!.toJSONString())
//                        val list = JSON.parseArray(obj.data!!.getString("tags"), SpecialModel::class.java)
//                        val offset = obj.data!!.getIntValue("offset")
//                        mIGameView3.setQuickRoom(list, offset)
//                    }
//                }
//            }
//        }, this, RequestControl("getSepcialList", ControlType.CancelThis))
//    }

    fun getRemainTimes(isFlag: Boolean) {
        val now = System.currentTimeMillis()
        if (!isFlag) {
            // 距离上次拉去已经超过5秒了
            if (now - mLastUpdateRemainTime < 5 * 1000) {
                return
            }
        }
        ApiMethods.subscribe(mMainPageSlideApi.remainTime, object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult?) {
                if (result?.errno == 0) {
                    mLastUpdateRemainTime = System.currentTimeMillis()
                    var totalRemainTimes = result.data.getIntValue("todayResTimes");
                    mIGameView3.showRemainTimes(totalRemainTimes)
                } else {
                    // 请求出错了
                }
            }
        }, this, RequestControl("getRemainTimes", ControlType.CancelThis))
    }

    //TODO 这个接口得换，等服务器更新
    fun initRecommendRoom(flag: Boolean, interval: Int) {
        mRecommendInterval = interval
        if (interval <= 0) {
            mRecommendInterval = 15
        }

        if (!flag) {
            var now = System.currentTimeMillis();
            if ((now - mLastUpdateRecomendInfo) > mRecommendInterval * 1000) {
                // 距离上次已经过去一个时间间隔
                starTimer(0)
            } else {
                // 没过去一个间隔
                var delayTime = mRecommendInterval * 1000 - (now - mLastUpdateRecomendInfo)
                starTimer(delayTime)
            }
        } else {
            // 立即更新
            starTimer(0)
        }
    }

    fun starTimer(delayTimeMill: Long) {
        stopTimer()
        mRecommendTimer = HandlerTaskTimer.newBuilder()
                .delay(delayTimeMill)
                .take(-1)
                .interval((mRecommendInterval * 1000).toLong())
                .start(object : HandlerTaskTimer.ObserverW() {
                    override fun onNext(t: Int) {
                        loadRecommendRoomData()
                    }
                })
    }

    fun stopTimer() {
        if (mRecommendTimer != null) {
            mRecommendTimer!!.dispose()
        }
    }

    private fun loadRecommendRoomData() {
        ApiMethods.subscribe(mGrabSongApi.getFirstPageRecommendRoomList(RA.getTestList(), RA.getVars()), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult) {
                if (obj.errno == 0) {
                    mLastUpdateRecomendInfo = System.currentTimeMillis()
                    val list = JSON.parseArray(obj.data!!.getString("rooms"), RecommendModel::class.java)
                    mIGameView3.setRecommendInfo(list)
                }
            }
        }, this, RequestControl("getRecommendRoomList", ControlType.CancelThis))
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: AccountEvent.SetAccountEvent) {
        initOperationArea(true)
        initRecommendRoom(true, mRecommendInterval)
        getRemainTimes(true)
//        initQuickRoom(true)
        checkTaskRedDot()
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(checkInSuccessEvent: CheckInSuccessEvent) {
        checkTaskRedDot()
    }


    override fun destroy() {
        super.destroy()
        stopTimer()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}

