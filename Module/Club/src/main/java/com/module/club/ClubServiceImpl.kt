package com.module.club

import android.content.Context
import android.content.Intent
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.userinfo.model.ClubMemberInfo
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.module.RouterConstants
import com.module.club.home.ClubHomeView
import com.module.club.homepage.ClubHomepageActivity
import com.module.club.homepage.ClubHomepageActivity2

@Route(path = RouterConstants.SERVICE_CLUB, name = "测试服务")
class ClubServiceImpl : IClubModuleService {

    val TAG = "ClubServiceImpl"

    override fun init(context: Context?) {
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
}
