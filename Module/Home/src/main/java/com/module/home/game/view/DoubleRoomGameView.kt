package com.module.home.game.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.alibaba.android.arouter.launcher.ARouter
import com.common.view.DebounceViewClickListener
import com.module.RouterConstants
import com.module.home.R
import kotlinx.android.synthetic.main.double_room_view_layout.view.*

/**
 * 邂逅好声音
 */
class DoubleRoomGameView : RelativeLayout {
    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.double_room_view_layout, this)

        start_match_iv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                ARouter.getInstance()
                        .build(RouterConstants.ACTIVITY_DOUBLE_PLAY)
                        .navigation()
            }
        })
    }

    fun destory() {

    }
}
