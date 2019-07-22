package com.module.feeds

import android.content.Context
import android.support.v4.app.Fragment
import com.alibaba.android.arouter.facade.annotation.Route
import com.module.RouterConstants
import com.module.feeds.watch.FeedsWatchFragment

@Route(path = RouterConstants.SERVICE_FEEDS, name = "测试服务")
class FeedsServiceImpl : IFeedsModuleService {

    val TAG = "FeedsServiceImpl"

    override fun getFeedsFragment(): Fragment {
        return FeedsWatchFragment()
    }

    override fun init(context: Context?) {
    }

}
