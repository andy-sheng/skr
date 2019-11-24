package com.component.person.presenter

import com.alibaba.fastjson.JSON
import com.common.core.userinfo.UserInfoManager
import com.common.core.userinfo.model.UserInfoModel
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.userinfo.event.RelationChangeEvent
import com.common.mvp.RxLifeCyclePresenter
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.component.busilib.friends.VoiceInfoModel
import com.component.person.view.IOtherPersonView

import com.component.person.model.RelationNumModel
import com.component.person.model.ScoreDetailModel
import okhttp3.MediaType
import okhttp3.RequestBody
import org.greenrobot.eventbus.EventBus
import java.util.HashMap

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
                    val guardList = JSON.parseArray(result.data?.getString("guardUserList"), UserInfoModel::class.java)

                    val isFriend = result.data?.getJSONObject("userMateInfo")?.getBooleanValue("isFriend")
                            ?: false
                    val isFollow = result.data?.getJSONObject("userMateInfo")?.getBooleanValue("isFollow")
                            ?: false
                    val isSpFollow = result.data?.getJSONObject("userMateInfo")?.getBooleanValue("isSPFollow")
                            ?: false

                    userInfoModel.isFriend = isFriend
                    userInfoModel.isFollow = isFollow
                    userInfoModel.isSPFollow = isSpFollow
                    if (isFollow) {
                        UserInfoManager.getInstance().insertUpdateDBAndCache(userInfoModel)
                    }

                    val meiLiCntTotal = result.data?.getIntValue("meiLiCntTotal") ?: 0
                    val qinMiCntTotal = result.data?.getIntValue("qinMiCntTotal") ?: 0

                    view.showHomePageInfo(userInfoModel, relationNumModes, meiLiCntTotal, qinMiCntTotal, scoreDetailModel, voiceInfoModel, guardList)
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

    fun addSpFollow(userID: Int) {
        val map = HashMap<String, Any>()
        map["toUserID"] = userID
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mUserInfoServerApi.addSpecialFollow(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val isFriend = result.data?.getJSONObject("relationInfo")?.getBooleanValue("isFriend")
                            ?: false
                    val isFollow = result.data?.getJSONObject("relationInfo")?.getBooleanValue("isFollow")
                            ?: false
                    val isSpFollow = result.data?.getJSONObject("relationInfo")?.getBooleanValue("isSPFollow")
                            ?: false
                    EventBus.getDefault().post(RelationChangeEvent(RelationChangeEvent.SP_FOLLOW_TYPE, userID, isFriend, isFollow, isSpFollow))
                    view.refreshRelation(isFriend, isFollow, isSpFollow)
                    U.getToastUtil().showShort("已开启对ta的特别关注哦")
                } else {
                    when {
                        result.errno == 8302701 -> // 普通关注数量触上限
                            view.showSpFollowVip()
                        result.errno == 8302702 -> // 特别关注数量触及vip上限
                            U.getToastUtil().showShort(result.errmsg)
                        else -> U.getToastUtil().showShort(result.errmsg)
                    }
                }
            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                super.onNetworkError(errorType)
                U.getToastUtil().showShort("网络异常，检测网络后再试吧")
            }
        }, this)
    }

    fun delSpFollow(userID: Int) {
        val map = HashMap<String, Any>()
        map["toUserID"] = userID
        val body = RequestBody.create(MediaType.parse(ApiManager.APPLICATION_JSON), JSON.toJSONString(map))
        ApiMethods.subscribe(mUserInfoServerApi.delSpecialFollow(body), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val isFriend = result.data?.getJSONObject("relationInfo")?.getBooleanValue("isFriend")
                            ?: false
                    val isFollow = result.data?.getJSONObject("relationInfo")?.getBooleanValue("isFollow")
                            ?: false
                    val isSpFollow = result.data?.getJSONObject("relationInfo")?.getBooleanValue("isSPFollow")
                            ?: false
                    EventBus.getDefault().post(RelationChangeEvent(RelationChangeEvent.UN_SP_FOLLOW_TYPE, userID, isFriend, isFollow, isSpFollow))
                    view.refreshRelation(isFriend, isFollow, isSpFollow)
                    U.getToastUtil().showShort("取消特别关注成功")
                } else {
                    U.getToastUtil().showShort(result.errmsg)
                }
            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                super.onNetworkError(errorType)
                U.getToastUtil().showShort("网络异常，检测网络后再试吧")
            }
        }, this)

    }
}
