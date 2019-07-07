package com.module.home.updateinfo.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.module.RouterConstants
import com.module.home.R
import com.module.home.updateinfo.fragment.EditInfoAgeTagFragment


@Route(path = RouterConstants.ACTIVITY_EDIT_AGE)
class EditAgeTagActivity : BaseActivity() {
    companion object {
        var runnable: Runnable? = null

        fun setRun(r: Runnable?) {
            runnable = r
        }
    }

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.empty_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        var from: Int? = intent.getIntExtra("from", 0)
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, EditInfoAgeTagFragment::class.java)
                .setAddToBackStack(false)
                .addDataBeforeAdd(0, from ?: 0)
                .setHasAnimation(false)
                .build())
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun canSlide(): Boolean {
        return false
    }
}
