package com.module.feeds.report

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.component.dialog.FeedsMoreDialogView
import com.module.RouterConstants
import com.module.feeds.R
import com.module.feeds.report.fragment.FeedReportFragment

@Route(path = RouterConstants.ACTIVITY_FEEDS_REPORT)
class FeedReportActivity : BaseActivity() {
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.empty_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        val from = intent.getIntExtra("from", FeedsMoreDialogView.FROM_FEED)
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this@FeedReportActivity, FeedReportFragment::class.java)
                .setAddToBackStack(false)
                .setHasAnimation(false)
                .addDataBeforeAdd(0, from)
                .build())
    }

    override fun useEventBus(): Boolean {
        return false
    }
}