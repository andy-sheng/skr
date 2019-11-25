package com.module.playways.relay.room

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.module.RouterConstants
import com.module.playways.R

// 接唱结果页
@Route(path = RouterConstants.ACTIVITY_RELAY_RESULT)
class RelayResultActivity : BaseActivity() {
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.relay_result_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun canSlide(): Boolean {
        return false
    }
}