package com.module.playways.grab.room.presenter

import com.common.mvp.RxLifeCyclePresenter
import com.common.utils.ActivityUtils
import com.module.playways.grab.room.inter.IGrabVipView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class VipEnterPresenter(val view: IGrabVipView) : RxLifeCyclePresenter() {
    val mTag = "VipEnterPresenter"

    init {
        EventBus.getDefault().register(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: ActivityUtils.ForeOrBackgroundChange) {

    }

    override fun destroy() {
        super.destroy()
        EventBus.getDefault().unregister(this)
    }
}