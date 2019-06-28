package com.module.home.ranked

import android.os.Bundle

import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.module.RouterConstants
import com.module.home.R
import com.module.home.ranked.fragment.RankedHomeFragment

@Route(path = RouterConstants.ACTIVITY_RANKED)
class RankedActivity : BaseActivity() {
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.empty_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(this, RankedHomeFragment::class.java)
                        .setAddToBackStack(false)
                        .setHasAnimation(false)
                        .build())
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun canSlide(): Boolean {
        return false
    }
}
