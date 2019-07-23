package com.module.feeds.detail.fragment

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.View
import com.common.base.AbsCoroutineFragment
import com.common.view.DebounceViewClickListener
import com.common.view.titlebar.CommonTitleBar
import com.module.feeds.R
import com.module.feeds.detail.view.FeedsCommentView

class FeedsCommentDetailFragment : AbsCoroutineFragment() {
    val mTag = "FeedsCommentDetailFragment"
    var mTitleComment: ConstraintLayout? = null
    var mTitlebar: CommonTitleBar? = null
    var mFeedCommentView: FeedsCommentView? = null

    override fun initView(): Int {
        return com.module.feeds.R.layout.feeds_comment_detail_fragment_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        mTitlebar = mRootView.findViewById(R.id.titlebar)
        mTitleComment = mRootView.findViewById(R.id.title_comment)
        mFeedCommentView = mRootView.findViewById(R.id.feed_comment_view)

        mTitlebar?.leftTextView?.setDebounceViewClickListener {
            activity?.finish()
        }

    }

    fun View.setDebounceViewClickListener(click: (view: View?) -> Unit) {
        this.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View?) {
                click(v)
            }
        })
    }

    override fun useEventBus(): Boolean {
        return false
    }
}