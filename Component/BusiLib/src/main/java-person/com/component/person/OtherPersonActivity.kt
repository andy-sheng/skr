package com.component.person

import android.os.Bundle

import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.component.busilib.R
import com.module.RouterConstants
import com.component.person.fragment.OtherPersonFragment4

@Route(path = RouterConstants.ACTIVITY_OTHER_PERSON)
class OtherPersonActivity : BaseActivity() {

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.empty_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        val bundle = intent.extras
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, OtherPersonFragment4::class.java)
                .setBundle(bundle)
                .setAddToBackStack(false)
                .setHasAnimation(false)
                .build())
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun canSlide(): Boolean {
        return false
    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }

    companion object {
        const val BUNDLE_USER_ID = "bundle_user_id"
    }
}
