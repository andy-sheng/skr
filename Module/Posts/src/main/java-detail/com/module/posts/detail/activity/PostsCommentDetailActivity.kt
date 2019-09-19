package com.module.posts.detail.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.module.RouterConstants
import com.module.posts.R
import com.module.posts.detail.fragment.PostsCommentDetailFragment
import com.module.posts.watch.model.PostsWatchModel

@Route(path = RouterConstants.ACTIVITY_POSTS_COMMENT_DETAIL)
class PostsCommentDetailActivity : BaseActivity() {
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.posts_detail_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {

        val model = intent.getSerializableExtra("model") as PostsWatchModel?
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this@PostsCommentDetailActivity, PostsCommentDetailFragment::class.java)
                .setAddToBackStack(false)
                .setHasAnimation(false)
                .addDataBeforeAdd(0, model)
                .build())
    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
