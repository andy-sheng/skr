package com.component.person.presenter

import com.alibaba.fastjson.JSON
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.userinfo.UserInfoServerApi
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.component.busilib.friends.VoiceInfoModel
import com.component.person.view.IOtherPersonView

import com.component.person.model.RelationNumModel
import com.component.person.model.ScoreDetailModel

class OtherPersonPresenter(internal var view: IOtherPersonView) : RxLifeCyclePresenter() {

    internal val mUserInfoServerApi: UserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)

    fun getHomePage(userID: Int) {
        ApiMethods.subscribe(mUserInfoServerApi.getHomePage(userID.toLong()), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val userInfoModel = JSON.parseObject(result.data?.getString("userBaseInfo"), UserInfoModel::class.java)
                    val relationNumModes = JSON.parseArray(result.data?.getJSONObject("userRelationCntInfo")?.getString("cnt"), RelationNumModel::class.java)
                    val scoreDetailModel = JSON.parseObject(result.data?.getString("scoreDetail"), ScoreDetailModel::class.java)
                    val voiceInfoModel = JSON.parseObject(result.data?.getString("voiceInfo"), VoiceInfoModel::class.java)

                    val isFriend = result.data?.getJSONObject("userMateInfo")?.getBooleanValue("isFriend")
                            ?: false
                    val isFollow = result.data?.getJSONObject("userMateInfo")?.getBooleanValue("isFollow")
                            ?: false
                    val isSpFollow = result.data?.getJSONObject("userMateInfo")?.getBooleanValue("isSPFollow")
                            ?: false

                    if (isFollow) {
                        userInfoModel.isFriend = isFriend
                        userInfoModel.isFollow = isFollow
                        userInfoModel.isSPFollow = isSpFollow
                        UserInfoManager.getInstance().insertUpdateDBAndCache(userInfoModel)
                    }

                    val meiLiCntTotal = result.data?.getIntValue("meiLiCntTotal") ?: 0
                    val qinMiCntTotal = result.data?.getIntValue("qinMiCntTotal") ?: 0

                    view.showHomePageInfo(userInfoModel, relationNumModes, meiLiCntTotal, qinMiCntTotal, scoreDetailModel, voiceInfoModel)
                } else {
                    view.getHomePageFail()
                }
            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                super.onNetworkError(errorType)
                view.getHomePageFail()
            }
        }, this)
    }
}
