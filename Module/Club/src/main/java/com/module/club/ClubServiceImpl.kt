package com.module.club

import android.content.Context
import android.content.Intent
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.userinfo.model.ClubMemberInfo
import com.common.rxretrofit.*
import com.common.utils.U
import com.component.busilib.event.DynamicPostsEvent
import com.component.busilib.event.PostsWatchTabRefreshEvent
import com.component.club.model.ClubMemberInfoModel
import com.module.RouterConstants
import com.module.club.home.ClubHomeView
import com.module.club.homepage.ClubHomepageActivity2
import com.module.common.ICallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus

@Route(path = RouterConstants.SERVICE_CLUB, name = "测试服务")
class ClubServiceImpl : IClubModuleService, CoroutineScope by MainScope() {


    val TAG = "ClubServiceImpl"

    private val PREF_KEY_CLUB_APPLY_COUNT_WATER = "CLUB_APPLY_COUNT_WATER"
    private val clubServerApi = ApiManager.getInstance().createService(ClubServerApi::class.java)

    override fun init(context: Context?) {
    }

    override fun getClubApplyCount(clubID: Int, callback: ICallback) {
        // 水位持久化
        val waterLevel = U.getPreferenceUtils().getSettingLong(PREF_KEY_CLUB_APPLY_COUNT_WATER, 0L)
        launch {
            val apiResult = subscribe (RequestControl("", ControlType.CancelThis)){
                clubServerApi.getCountMemberApply(clubID, waterLevel)
            }

            if(apiResult.errno == 0){
                callback.onSucess(apiResult.data["total"])
                U.getPreferenceUtils().setSettingLong(PREF_KEY_CLUB_APPLY_COUNT_WATER, apiResult.data["timeMs"].toString().toLong())
            }else{
                callback.onFailed(null, apiResult.errno, apiResult.errmsg)
            }
        }
    }

    override fun getClubMembers(clubID: Int, callback: ICallback) {
        var hasMore = true
        val userInfoList = mutableListOf<MutableMap<String, String>>()


        launch {

            var offset = 0
            val cnt = 50

            //一次性获取所有家族成员
            while (hasMore) {
                val result = subscribe(RequestControl("getClubMemberList", ControlType.CancelThis)) {
                    clubServerApi.getClubMemberList(clubID, offset, cnt)
                }

                if (result.errno == 0) {

                    offset = result.data.getIntValue("offset")
                    hasMore = result.data.getBooleanValue("hasMore")
                    val list = JSON.parseArray(result.data.getString("items"), ClubMemberInfoModel::class.java)


                    userInfoList.addAll(list.map {
                        val userInfoModel = it.userInfoModel
                                ?: return@map mutableMapOf<String, String>()
                        mutableMapOf(Pair("userId", userInfoModel.userId.toString()),
                                Pair("nickname", userInfoModel.nickname),
                                Pair("avatar", userInfoModel.avatar))
                    })
                } else {
                    callback.onFailed(userInfoList, result.errno, result.errmsg)
                    break
                }

            }

            callback.onSucess(userInfoList)
        }
    }

    override fun tryGoClubHomePage(clubID: Int) {
        val userServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)
        ApiMethods.subscribe(userServerApi.getClubMemberInfo(MyUserInfoManager.uid.toInt(), clubID), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val clubMemberInfo = JSON.parseObject(result.data.getString("info"), ClubMemberInfo::class.java)
                    val intent = Intent(U.getActivityUtils().topActivity, ClubHomepageActivity2::class.java)
                    intent.putExtra("clubMemberInfo", clubMemberInfo)
                    U.getActivityUtils().topActivity.startActivity(intent)
                } else {
                    U.getToastUtil().showShort(result.errmsg)
                }
            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                super.onNetworkError(errorType)
            }
        })
    }

    override fun getClubHomeView(context: Context): IClubHomeView {
        return ClubHomeView(context)
    }

    override fun finishClubWorkUpload() {
        EventBus.getDefault().post(DynamicPostsEvent(DynamicPostsEvent.EVENT_WORK))
    }

    override fun finishClubPostUpload() {
        EventBus.getDefault().post(DynamicPostsEvent(DynamicPostsEvent.EVENT_POST))
    }
}
