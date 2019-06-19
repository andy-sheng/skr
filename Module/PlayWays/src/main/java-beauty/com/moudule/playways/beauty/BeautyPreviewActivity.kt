package com.moudule.playways.beauty


import android.os.Bundle

import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.module.RouterConstants
import com.module.playways.R
import com.moudule.playways.beauty.fragment.BeautyPreviewFragment

@Route(path = RouterConstants.ACTIVITY_BEAUTY_PREVIEW)
class BeautyPreviewActivity : BaseActivity() {
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.empty_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        val bundle = intent.extras
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, BeautyPreviewFragment::class.java)
                .setBundle(bundle)
                .setAddToBackStack(false)
                .setHasAnimation(false)
                .build())
        hasCreate = true
    }

    override fun finish() {
        super.finish()
        hasCreate = false
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun canSlide(): Boolean {
        return false
    }

    companion object {
        @JvmField
        var hasCreate: Boolean = false
    }
}
