package com.module.feeds.watch

import android.os.Bundle
import com.common.base.BaseFragment
import com.module.feeds.R

class FeedsWatchFragment: BaseFragment() {
    override fun initView(): Int {
        return R.layout.feeds_watch_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
