package com.module.home.game.presenter

import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.common.core.account.event.AccountEvent
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.HandlerTaskTimer
import com.common.utils.U
import com.component.busilib.friends.GrabSongApi
import com.component.busilib.friends.RecommendModel
import com.component.busilib.friends.SpecialModel
import com.module.home.MainPageSlideApi
import com.module.home.event.CheckInSuccessEvent
import com.module.home.game.view.IQuickGameView3
import com.module.home.model.SlideShowModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class QuickGamePresenter(internal var mIGameView3: IQuickGameView3) : RxLifeCyclePresenter() {

    internal var mMainPageSlideApi: MainPageSlideApi
    internal var mGrabSongApi: GrabSongApi

    internal var mLastUpdateOperaArea: Long = 0    //广告位上次更新成功时间
    internal var mLastUpdateQuickInfo: Long = 0    //快速加入房间更新成功时间
    internal var mIsFirstQuick = true

    internal var mRecommendTimer: HandlerTaskTimer? = null

    internal var mLastUpdateRecomendInfo: Long = 0    //上次拉去剩余次数的时间
    var mRecommendInterval: Int = 0

    init {
        mMainPageSlideApi = ApiManager.getInstance().createService(MainPageSlideApi::class.java)
        mGrabSongApi = ApiManager.getInstance().createService(GrabSongApi::class.java)
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
        }, this, ApiMethods.RequestControl("checkTaskRedDot", ApiMethods.ControlType.CancelThis))
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
        }, this, ApiMethods.RequestControl("getSlideList", ApiMethods.ControlType.CancelThis))
    }

    fun initQuickRoom(isFlag: Boolean) {
        MyLog.d(TAG, "initQuickRoom isFlag=$isFlag")
        val now = System.currentTimeMillis()
        if (!isFlag) {
            // 半个小时更新一次吧
            if (now - mLastUpdateQuickInfo < 30 * 60 * 1000) {
                return
            }
        }

        var spResult = ""
        if (mIsFirstQuick) {
            // 先用SP里面的
            mIsFirstQuick = false
            spResult = U.getPreferenceUtils().getSettingString(U.getPreferenceUtils().longlySp(), "quick_romms", "")
            if (!TextUtils.isEmpty(spResult)) {
                try {
                    var jsonObject = JSON.parseObject(spResult, JSONObject::class.java)
                    var list = JSON.parseArray(jsonObject.getString("tags"), SpecialModel::class.java)
                    var offset = jsonObject.getIntValue("offset")
                    mIGameView3.setQuickRoom(list, offset)
                } catch (e: Exception) {
                }

            }
        }

        val finalSpResult = spResult
        ApiMethods.subscribe(mGrabSongApi.getSepcialList(0, 20), object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult) {
                if (obj.errno == 0) {
                    mLastUpdateQuickInfo = System.currentTimeMillis()
                    if (obj.data!!.toJSONString() != finalSpResult) {
                        U.getPreferenceUtils().setSettingString(U.getPreferenceUtils().longlySp(), "quick_romms", obj.data!!.toJSONString())
                        val list = JSON.parseArray(obj.data!!.getString("tags"), SpecialModel::class.java)
                        val offset = obj.data!!.getIntValue("offset")
                        mIGameView3.setQuickRoom(list, offset)
                    }
                }
            }
        }, this, ApiMethods.RequestControl("getSepcialList", ApiMethods.ControlType.CancelThis))
    }

    //TODO 这个接口得换，等服务器更新
    fun initRecommendRoom(interval: Int) {
        mRecommendInterval = interval
        if (interval <= 0) {
            mRecommendInterval = 15
        }
        stopTimer()
        mRecommendTimer = HandlerTaskTimer.newBuilder()
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
        ApiMethods.subscribe(mGrabSongApi.firstPageRecommendRoomList, object : ApiObserver<ApiResult>() {
            override fun process(obj: ApiResult) {
                if (obj.errno == 0) {
                    mLastUpdateRecomendInfo = System.currentTimeMillis()
                    val list = JSON.parseArray(obj.data!!.getString("rooms"), RecommendModel::class.java)
                    mIGameView3.setRecommendInfo(list)
                }
            }
        }, this, ApiMethods.RequestControl("getRecommendRoomList", ApiMethods.ControlType.CancelThis))
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: AccountEvent.SetAccountEvent) {
        initOperationArea(true)
        initRecommendRoom(mRecommendInterval)
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

