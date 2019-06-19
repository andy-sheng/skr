package com.module.playways.doubleplay.fragment

import android.os.Bundle

import com.common.base.BaseFragment
import com.module.playways.R

class EditInfoFragment : BaseFragment() {
    override fun initView(): Int {
        return R.layout.empty_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {

    }

    override fun useEventBus(): Boolean {
        return false
    }
}
