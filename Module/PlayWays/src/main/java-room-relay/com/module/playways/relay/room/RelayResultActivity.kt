package com.module.playways.relay.room

import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.widget.TextView
import com.alibaba.android.arouter.facade.annotation.Route
import com.common.base.BaseActivity
import com.common.core.view.setDebounceViewClickListener
import com.component.busilib.view.AvatarLevelView
import com.module.RouterConstants
import com.module.playways.R

// 接唱结果页
@Route(path = RouterConstants.ACTIVITY_RELAY_RESULT)
class RelayResultActivity : BaseActivity() {


    lateinit var reportTv: TextView
    lateinit var contentArea: ConstraintLayout
    lateinit var avatarLevel: AvatarLevelView
    lateinit var gameStatusTv: TextView
    lateinit var gameTimeTv: TextView
    lateinit var followTv: TextView
    lateinit var backTv: TextView

    override fun initView(savedInstanceState: Bundle?): Int {
        return R.layout.relay_result_activity_layout
    }

    override fun initData(savedInstanceState: Bundle?) {
        reportTv = findViewById(R.id.report_tv)
        contentArea = findViewById(R.id.content_area)
        avatarLevel = findViewById(R.id.avatar_level)
        gameStatusTv = findViewById(R.id.game_status_tv)
        gameTimeTv = findViewById(R.id.game_time_tv)
        followTv = findViewById(R.id.follow_tv)
        backTv = findViewById(R.id.back_tv)

        followTv.setDebounceViewClickListener { }
        backTv.setDebounceViewClickListener { }
    }

    override fun useEventBus(): Boolean {
        return false
    }

    override fun canSlide(): Boolean {
        return false
    }
}