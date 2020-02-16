package com.module.feeds.watch

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.module.RouterConstants
import com.module.feeds.R

@Route(path = RouterConstants.ACTIVITY_PERSON_FEED)
class PersonFeedActivity : BaseActivity() {
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.person_feed_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {

    }

    override fun useEventBus(): Boolean {
        return false
    }
}