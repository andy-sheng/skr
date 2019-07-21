package com.module.feeds.watch.view

import android.support.constraint.ConstraintLayout
import android.view.View
import com.common.base.BaseFragment
import com.module.feeds.R

class FeedsWatchView(var fragment: BaseFragment) : ConstraintLayout(fragment.context) {

    init {
        View.inflate(context, R.layout.feed_watch_view_layout, this)
    }
}