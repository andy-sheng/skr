package com.component.report.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.component.report.fragment.QuickFeedbackFragment
import com.module.RouterConstants

/**
 * 快速一键反馈
 */
@Route(path = RouterConstants.ACTIVITY_FEEDBACK)
class QuickFeedbackActivity : BaseActivity() {
    override fun initView(savedInstanceState: Bundle?): Int {
        return 0
    }

    override fun initData(savedInstanceState: Bundle?) {
        // 举报
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(this, QuickFeedbackFragment::class.java)
                        .setAddToBackStack(false)
                        .setHasAnimation(true)
                        .addDataBeforeAdd(0, intent.getIntExtra("from", 0))
                        .addDataBeforeAdd(1, intent.getIntExtra("actionType", 0))
                        .addDataBeforeAdd(2, intent.getIntExtra("targetId", 0))
                        .addDataBeforeAdd(3, intent.getIntExtra("roomId", 0))
                        .build())
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
