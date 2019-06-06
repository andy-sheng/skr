package com.module.home.musictest.activity

import android.os.Bundle

import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.module.RouterConstants
import com.module.home.R
import com.module.home.musictest.fragment.MusicTestFragment

@Route(path = RouterConstants.ACTIVITY_MUSIC_TEST)
class MusicTestActivity : BaseActivity() {
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.empty_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(this, MusicTestFragment::class.java)
                        .setAddToBackStack(false)
                        .setHasAnimation(false)
                        .build())
    }

    override fun useEventBus(): Boolean {
        return false
    }
}