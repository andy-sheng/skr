package com.module.playways.doubleplay.activity

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.doubleplay.fragment.DoubleGameEndFragment


@Route(path = RouterConstants.ACTIVITY_DOUBLE_END)
class DoubleEndActivity : BaseActivity() {

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.empty_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        val doubleRoomData = intent.getSerializableExtra("roomData")
        U.getFragmentUtils().addFragment(FragmentUtils.newAddParamsBuilder(this, DoubleGameEndFragment::class.java)
                .setAddToBackStack(false)
                .addDataBeforeAdd(0, doubleRoomData)
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
