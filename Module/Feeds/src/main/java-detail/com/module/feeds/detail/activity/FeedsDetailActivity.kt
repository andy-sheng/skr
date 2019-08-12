package com.module.feeds.detail.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.detail.fragment.FeedsDetailFragment
import com.module.feeds.watch.model.FeedsWatchModel

@Route(path = RouterConstants.ACTIVITY_FEEDS_DETAIL)
class FeedsDetailActivity : BaseActivity() {

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.feeds_detail_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        val feedID = intent.getIntExtra("feed_ID", -1)
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this@FeedsDetailActivity, FeedsDetailFragment::class.java)
                .setAddToBackStack(false)
                .setHasAnimation(false)
                .addDataBeforeAdd(0, feedID)
                .build())

    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }

    override fun useEventBus(): Boolean {
        return false
    }

}
