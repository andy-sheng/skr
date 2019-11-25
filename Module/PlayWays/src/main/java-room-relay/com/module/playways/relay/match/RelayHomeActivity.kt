package com.module.playways.relay.match

import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.WindowManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.U
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.view.recyclercardview.SpeedRecyclerView
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.relay.match.adapter.RelayHomeSongAdapter

@Route(path = RouterConstants.ACTIVITY_RELAY_HOME)
class RelayHomeActivity : BaseActivity() {

    var titlebar: CommonTitleBar? = null
    var speedRecyclerView: SpeedRecyclerView? = null

    var adapter: RelayHomeSongAdapter? = null

    /**
     * 存起该房间一些状态信息
     */
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.relay_home_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        for (activity in U.getActivityUtils().activityList) {
            if (U.getActivityUtils().isHomeActivity(activity)) {
                continue
            }
            if (activity === this) {
                continue
            }
            activity.finish()
        }

        titlebar = findViewById(R.id.titlebar)
        speedRecyclerView = findViewById(R.id.speed_recyclerView)

        speedRecyclerView?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


        titlebar?.leftTextView?.setDebounceViewClickListener { finish() }
        titlebar?.rightTextView?.setDebounceViewClickListener {
            // todo 去搜歌
        }
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun onResume() {
        super.onResume()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }


    override fun destroy() {
        super.destroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun canSlide(): Boolean {
        return false
    }

    override fun resizeLayoutSelfWhenKeybordShow(): Boolean {
        return true
    }
}
