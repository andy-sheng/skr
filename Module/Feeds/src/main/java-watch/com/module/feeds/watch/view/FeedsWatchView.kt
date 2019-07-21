package com.module.feeds.watch.view

import android.support.constraint.ConstraintLayout
import android.view.View
import com.common.base.BaseFragment
import com.module.feeds.R

class FeedsWatchView(var fragment: BaseFragment, var type: Int) : ConstraintLayout(fragment.context) {

    companion object {
        const val TYPE_RECOMMEND = 1
        const val TYPE_FOLLOW = 2
    }

    init {
        View.inflate(context, R.layout.feed_watch_view_layout, this)
    }

    fun destory() {

    }
}