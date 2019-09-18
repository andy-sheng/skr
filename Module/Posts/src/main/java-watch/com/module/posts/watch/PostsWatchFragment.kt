package com.module.posts.watch

import android.os.Bundle
import com.common.base.BaseFragment
import com.module.posts.R

class PostsWatchFragment : BaseFragment() {
    override fun initView(): Int {
        return R.layout.posts_watch_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
    }

    override fun useEventBus(): Boolean {
        return false
    }
}

