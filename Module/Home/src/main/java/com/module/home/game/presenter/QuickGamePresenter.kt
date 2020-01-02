package com.module.home.game.presenter

import android.text.TextUtils
import com.alibaba.fastjson.JSON
import com.common.base.BaseFragment
import com.common.core.account.event.AccountEvent
import com.common.core.myinfo.event.MyUserInfoEvent
import com.common.core.userinfo.UserInfoServerApi
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.*
import com.common.utils.HandlerTaskTimer
import com.common.utils.U
import com.component.busilib.friends.GrabSongApi
import com.component.busilib.model.PartyRoomInfoModel
import com.component.person.model.UserRankModel
import com.module.home.MainPageSlideApi
import com.module.home.event.CheckInSuccessEvent
import com.module.home.game.view.IQuickGameView3
import com.module.home.model.SlideShowModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class QuickGamePresenter(val fragment: BaseFragment, internal var mIGameView3: IQuickGameView3) : RxLifeCyclePresenter() {

    private val mMainPageSlideApi: MainPageSlideApi = ApiManager.getInstance().createService(MainPageSlideApi::class.java)
    private val userInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)
    private val mGrabSongApi: GrabSongApi = ApiManager.getInstance().createService(GrabSongApi::class.java)

    private var mLastUpdateOperaArea = 0L    //广告位上次更新成功时间
    private var mLastUpdateRemainTime = 0L   // 上次拉取唱聊剩余次数时间
    private var mRecommendTimer: HandlerTaskTimer? = null
    private var mLastUpdateRecomendInfo = 0L    //上次拉去推荐房间剩余次数的时间
    private var mLastUpdateGameType = 0L // 上次拉去首页游戏列表时间
    private var mLastUpdateReginDiff = 0L // 上传拉diff的时间
    private var mIsFirstQuick = true   // 是否第一次拉去首页
    var mRecommendInterval: Int = 0    // 拉去推荐房的时间间隔

    var isUserInfoChange = false

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

    fun getRegionDiff(isFlag: Boolean) {
        val now = System.currentTimeMillis()
        if (!isFlag) {
            // diff 接口更新
            if (now - mLastUpdateReginDiff < 5 * 60 * 1000) {
                return
            }
        }
        ApiMethods.subscribe(userInfoServerApi.regionDiff, object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    mLastUpdateReginDiff = System.currentTimeMillis()
                    val userRankModel = JSON.parseObject(result.data!!.getString("diff"), UserRankModel::class.java)
                    mIGameView3.setRegionDiff(userRankModel)
                }
            }

            override fun onError(e: Throwable) {
                U.getToastUtil().showShort("网络异常")
            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                U.getToastUtil().showShort("网络超时")
            }
        }, this)
    }

    fun getPartyRoomList() {
        ApiMethods.subscribe(mMainPageSlideApi.getPartyRoomList(0, 20), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val list = JSON.parseArray(result.data.getString("roomInfo"), PartyRoomInfoModel::class.java)
                    mIGameView3.setPartyRoomList(list)
                }
            }

            override fun onError(e: Throwable) {
                U.getToastUtil().showShort("网络异常")
            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                U.getToastUtil().showShort("网络超时")
            }
        }, this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: AccountEvent.SetAccountEvent) {
        initOperationArea(true)
        getRegionDiff(true)
        getPartyRoomList()
        checkTaskRedDot()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: MyUserInfoEvent.UserInfoChangeEvent) {
        isUserInfoChange = true
        // 在页面才去刷新去
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    fun onEvent(checkInSuccessEvent: CheckInSuccessEvent) {
        checkTaskRedDot()
    }


    override fun destroy() {
        super.destroy()
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this)
        }
    }
}

