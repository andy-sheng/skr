package com.module.msg.activity

import android.os.Bundle

import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.module.RouterConstants
import com.module.msg.follow.LastFollowFragment

import io.rong.imkit.R

@Route(path = RouterConstants.ACTIVITY_LAST_FOLLOW)
class LastFollowActivity : BaseActivity() {
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.empty_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(this, LastFollowFragment::class.java)
                        .setAddToBackStack(false)
                        .setHasAnimation(false)
                        .build())
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }
}
