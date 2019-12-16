package com.module.club

import android.content.Context
import android.content.Intent
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.alibaba.fastjson.JSON
import com.common.core.myinfo.MyUserInfoManager
import com.common.core.userinfo.UserInfoServerApi
import com.common.core.userinfo.model.ClubMemberInfo
import com.common.log.MyLog
import com.common.rxretrofit.ApiManager
import com.common.rxretrofit.ApiMethods
import com.common.rxretrofit.ApiObserver
import com.common.rxretrofit.ApiResult
import com.common.utils.U
import com.component.busilib.event.GrabJoinRoomFailEvent
import com.component.toast.CommonToastView
import com.module.RouterConstants
import com.module.club.homepage.MyClubHomepageActivity
import com.module.club.homepage.OtherClubHomepageActivity
import io.reactivex.disposables.Disposable
import org.greenrobot.eventbus.EventBus

@Route(path = RouterConstants.SERVICE_CLUB, name = "测试服务")
class ClubServiceImpl : IClubModuleService {

    val TAG = "ClubServiceImpl"

    override fun init(context: Context?) {
    }

    override fun tryGoClubHomePage(clubID: Int) {
        val userServerApi = ApiManager.getInstance().createService(UserInfoServerApi::class.java)
        ApiMethods.subscribe(userServerApi.getMyClubInfo(MyUserInfoManager.uid.toInt()), object : ApiObserver<ApiResult>() {
            override fun process(result: ApiResult) {
                if (result.errno == 0) {
                    val clubMemberInfo = JSON.parseObject(result.data.getString("info"), ClubMemberInfo::class.java)
                    MyUserInfoManager.myUserInfo?.clubInfo = clubMemberInfo
                    // 判断进那个主页
                    if (MyUserInfoManager.clubID == clubID) {
                        val intent = Intent(U.getActivityUtils().topActivity, MyClubHomepageActivity::class.java)
                        U.getActivityUtils().topActivity.startActivity(intent)
                    } else {
                        val intent = Intent(U.getActivityUtils().topActivity, OtherClubHomepageActivity::class.java)
                        intent.putExtra("clubID", clubID)
                        U.getActivityUtils().topActivity.startActivity(intent)
                    }
                } else {

                }
            }

            override fun onNetworkError(errorType: ApiObserver.ErrorType) {
                super.onNetworkError(errorType)
            }
        })
    }
}
