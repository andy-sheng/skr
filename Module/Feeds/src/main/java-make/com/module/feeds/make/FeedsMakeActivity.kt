package com.module.feeds.make

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route

import com.common.base.BaseActivity
import com.module.RouterConstants
import com.module.feeds.R

@Route(path = RouterConstants.ACTIVITY_FEEDS_MAKE)
class FeedsMakeActivity : BaseActivity() {
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feeds_make_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
