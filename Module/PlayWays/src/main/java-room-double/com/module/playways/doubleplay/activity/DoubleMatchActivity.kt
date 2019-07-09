package com.module.playways.doubleplay.activity

import android.os.Bundle
import android.view.WindowManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.statistics.StatisticsAdapter
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.doubleplay.fragment.DoubleGameMatchFragment


@Route(path = RouterConstants.ACTIVITY_DOUBLE_MATCH)
class DoubleMatchActivity : BaseActivity() {

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.empty_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        val bundle = intent.getBundleExtra("bundle")
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, DoubleGameMatchFragment::class.java)
                .setAddToBackStack(false)
                .addDataBeforeAdd(0, bundle)
                .setHasAnimation(false)
                .build())
        StatisticsAdapter.recordCountEvent("cp", "pairing", null)
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun destroy() {
        super.destroy()
        if (window != null) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun canSlide(): Boolean {
        return false
    }
}
