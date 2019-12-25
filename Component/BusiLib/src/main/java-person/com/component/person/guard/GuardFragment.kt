package com.component.person.guard

import android.os.Bundle
import com.common.base.BaseFragment
import com.component.busilib.R

// 守护列表
class GuardFragment : BaseFragment() {

    override fun initView(): Int {
        return R.layout.guard_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {

    }

    override fun useEventBus(): Boolean {
        return false
    }
}