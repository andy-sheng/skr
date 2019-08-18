package com.module.feeds

import android.content.Context
import android.support.v4.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseFragment
import com.common.core.userinfo.model.UserInfoModel
import com.component.person.view.RequestCallBack
import com.module.RouterConstants
import com.module.feeds.detail.fragment.LikeWorksFragment
import com.module.feeds.detail.fragment.RefuseFeedsFragment
import com.module.feeds.watch.FeedsWatchFragment
import com.module.feeds.watch.watchview.PersonWatchView

@Route(path = RouterConstants.SERVICE_FEEDS, name = "测试服务")
class FeedsServiceImpl : IFeedsModuleService {
    override fun getPersonFeedsWall(basefragment: Any?, userInfo: Any?, requestCall: Any?): IPersonFeedsWall {
        return PersonWatchView(basefragment as BaseFragment, userInfo as UserInfoModel, requestCall as RequestCallBack)
    }

    val TAG = "FeedsServiceImpl"

    override fun getFeedsFragment(): Fragment {
        return FeedsWatchFragment()
    }

    override fun init(context: Context?) {
    }

    override fun getLikeWorkFragment(): Fragment {
        return LikeWorksFragment()
    }

    override fun getRefuseCommentFragment(): Fragment {
        return RefuseFeedsFragment()
    }
}
