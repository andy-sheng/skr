package com.module.feeds.detail.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.detail.fragment.FeedsCommentDetailFragment
import com.module.feeds.detail.model.FirstLevelCommentModel

@Route(path = RouterConstants.ACTIVITY_FEEDS_SECOND_DETAIL)
class FeedsSecondCommentDetailActivity : BaseActivity() {
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feeds_detail_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        val model = intent.getSerializableExtra("comment_model")
        val feedsId = intent.getIntExtra("feed_id", 0)
        if (model == null) {
            finish()
            return
        }

        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this@FeedsSecondCommentDetailActivity, FeedsCommentDetailFragment::class.java)
                .setAddToBackStack(false)
                .setHasAnimation(false)
                .addDataBeforeAdd(0, model as FirstLevelCommentModel)
                .addDataBeforeAdd(1, feedsId)
                .build())
    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
