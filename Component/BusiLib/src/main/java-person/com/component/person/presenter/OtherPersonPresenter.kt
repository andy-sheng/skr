package com.component.person.presenter

import com.alibaba.fastjson.JSON
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.model.GameStatisModel
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.userinfo.model.UserRankModel
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.component.person.view.IOtherPersonView

import com.component.person.model.RelationNumModel

import com.common.core.userinfo.model.UserLevelModel

class OtherPersonPresenter(internal var view: IOtherPersonView) : RxLifeCyclePresenter() {

    internal val mUserInfoServerApi: UserInfoServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)

    fun getHomePage(userID: Int) {
        ApiMethods.subscribe(mUserInfoServerApi.getHomePage(userID.toLong()), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val userInfoModel = JSON.parseObject(result.data?.getString("userBaseInfo"), UserInfoModel::class.java)
                    val userRankModels = JSON.parseArray(result.data?.getJSONObject("userRankInfo")?.getString("seqInfo"), UserRankModel::class.java)
                    val relationNumModes = JSON.parseArray(result.data?.getJSONObject("userRelationCntInfo")?.getString("cnt"), RelationNumModel::class.java)
                    val userLevelModels = JSON.parseArray(result.data?.getJSONObject("userScoreInfo")?.getString("userScore"), UserLevelModel::class.java)
                    val userGameStatisModels = JSON.parseArray(result.data?.getJSONObject("userGameStatisticsInfo")?.getString("statistic"), GameStatisModel::class.java)

                    val isFriend = result.data!!.getJSONObject("userMateInfo").getBooleanValue("isFriend")
                    val isFollow = result.data!!.getJSONObject("userMateInfo").getBooleanValue("isFollow")

                    if (isFollow) {
                        userInfoModel.isFriend = isFriend
                        userInfoModel.isFollow = isFollow
                        UserInfoManager.getInstance().insertUpdateDBAndCache(userInfoModel)
                    }

                    val meiLiCntTotal = result.data!!.getIntValue("meiLiCntTotal")

                    view.showHomePageInfo(userInfoModel, relationNumModes, userRankModels, userLevelModels, userGameStatisModels, isFriend, isFollow, meiLiCntTotal)
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
