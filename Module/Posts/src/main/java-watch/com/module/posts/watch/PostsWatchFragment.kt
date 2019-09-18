package com.module.posts.watch

import android.os.Bundle
import com.common.base.BaseFragment
import com.common.view.ninegrid.NineGridLayout
import com.module.posts.R
import com.module.posts.view.PostsNineGridLayout

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

