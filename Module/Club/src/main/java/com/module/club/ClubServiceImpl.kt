package com.module.club

import android.content.Context
import com.alibaba.android.arouter.facade.annotation.Route
import com.module.RouterConstants

@Route(path = RouterConstants.SERVICE_CLUB, name = "测试服务")
class ClubServiceImpl : IClubModuleService {

    val TAG = "ClubServiceImpl"

    override fun init(context: Context?) {
    }

//    override fun getPersonFeedsWall(basefragment: Any?, userInfo: Any?, requestCall: Any?): IPersonFeedsWall {
//        return PersonWatchView(basefragment as BaseFragment, userInfo as UserInfoModel, requestCall as RequestCallBack)
//    }
//
//    override fun getFragment(): Fragment {
//        return FeedsWatchFragment()
//    }
//
//    override fun getLikeWorkFragment(): Fragment {
//        return LikeWorksFragment()
//    }
//
//    override fun getRefuseCommentFragment(): Fragment {
//        return RefuseFeedsFragment()
//    }
}
