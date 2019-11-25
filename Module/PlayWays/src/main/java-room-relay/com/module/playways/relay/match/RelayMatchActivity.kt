package com.module.playways.relay.match

import android.os.Bundle
import android.view.WindowManager
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.core.view.setDebounceViewClickListener
import com.common.utils.U
import com.common.view.ex.ExTextView
import com.common.view.titlebar.CommonTitleBar
import com.component.busilib.view.recyclercardview.SpeedRecyclerView
import com.module.RouterConstants
import com.module.playways.R
import com.module.playways.relay.match.adapter.RelayRoomAdapter

@Route(path = RouterConstants.ACTIVITY_RELAY_MATCH)
class RelayMatchActivity : BaseActivity() {

    private var titlebar: CommonTitleBar? = null
    private var joinTipsTv: ExTextView? = null
    private var speedRecyclerView: SpeedRecyclerView? = null

    var adapter: RelayRoomAdapter? = null

    /**
     * 存起该房间一些状态信息
     */
    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.relay_match_activity_layout
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
        joinTipsTv = findViewById(R.id.join_tips_tv)
        speedRecyclerView = findViewById(R.id.speed_recyclerView)

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
