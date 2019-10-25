package com.module.playways.mic.room

import android.os.Bundle
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.utils.FragmentUtils
import com.common.utils.U
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.mic.create.fragment.MicRoomCreateFragment

@Route(path = RouterConstants.ACTIVITY_CREATE_MIC_ROOM)
class MicRoomCreateActivity : BaseActivity() {
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.empty_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        U.getFragmentUtils().addFragment(
                FragmentUtils.newAddParamsBuilder(this, MicRoomCreateFragment::class.java)
                        .setAddToBackStack(false)
                        .setHasAnimation(false)
                        .build())
        U.getStatusBarUtil().setTransparentBar(this, false)
    }

    override fun useEventBus(): Boolean {
        return false
    }
}
