package com.module.home.game.presenter

import com.alibaba.fastjson.JSON
import com.common.log.MyLog
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.engine.Params
import com.module.home.MainPageSlideApi
import com.module.home.game.view.IGameView3
import com.module.home.model.GameKConfigModel

class GamePresenter3(internal var mIGameView: IGameView3) : RxLifeCyclePresenter() {

    internal var mMainPageSlideApi: MainPageSlideApi

    internal var mIsKConfig = false  //标记是否拉到过游戏配置信息

    init {
        mMainPageSlideApi = ApiManager.getInstance().createService(MainPageSlideApi::class.java)
    }

    fun initGameKConfig() {
        if (mIsKConfig) {
            return
        }
        mMainPageSlideApi = ApiManager.getInstance().createService(MainPageSlideApi::class.java)
        ApiMethods.subscribe(mMainPageSlideApi.kConfig, object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    mIsKConfig = true
                    val gameKConfigModel = JSON.parseObject(result.data!!.getString("common"), GameKConfigModel::class.java)
                    U.getPreferenceUtils().setSettingBoolean(Params.PREF_KEY_TOKEN_ENABLE, gameKConfigModel.isAgoraTokenEnable)
                    mIGameView.setGameConfig(gameKConfigModel)

                    val homepagesitefirstBean = gameKConfigModel.homepagesitefirst
                    if (homepagesitefirstBean != null && homepagesitefirstBean.isEnable) {
                        mIGameView.showRedOperationView(homepagesitefirstBean)
                    } else {
                        MyLog.w(TAG, "initGameKConfig first operation area is empty")
                        mIGameView.hideRedOperationView()
                    }
                } else {
                    mIsKConfig = false
                    mIGameView.hideRedOperationView()
                }
            }

            override fun onError(e: Throwable) {
                U.getToastUtil().showShort("网络异常")
            }
        }, this, ApiMethods.RequestControl("getKConfig", ApiMethods.ControlType.CancelThis))
    }

}
