package com.module.playways.mic.home

import android.os.Bundle
import android.widget.ImageView
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.common.base.BaseActivity
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.core.view.setDebounceViewClickListener
import com.common.view.titlebar.CommonTitleBar
import com.module.RouterConstants
import com.module.playways.R

@Route(path = RouterConstants.ACTIVITY_MIC_HOME)
class MicHomeActivity : BaseActivity() {

    lateinit var titlebar: CommonTitleBar
    lateinit var quickBegin: ImageView
    lateinit var createRoom: ImageView

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.mic_home_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        titlebar = findViewById(R.id.titlebar)
        quickBegin = findViewById(R.id.quick_begin)
        createRoom = findViewById(R.id.create_room)

        titlebar.leftTextView.setDebounceViewClickListener { finish() }
        quickBegin.setAnimateDebounceViewClickListener {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_MIC_MATCH)
                    .navigation()
        }
        createRoom.setAnimateDebounceViewClickListener {
            ARouter.getInstance().build(RouterConstants.ACTIVITY_CREATE_MIC_ROOM)
                    .navigation()
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }
}