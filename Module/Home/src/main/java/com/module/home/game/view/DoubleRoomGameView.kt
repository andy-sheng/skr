package com.module.home.game.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.alibaba.android.arouter.launcher.ARouter
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.module.RouterConstants
import com.module.home.R
import com.module.home.event.ShowConfirmInfoEvent
import kotlinx.android.synthetic.main.double_room_view_layout.view.*
import org.greenrobot.eventbus.EventBus

/**
 * 邂逅好声音
 */
class DoubleRoomGameView : RelativeLayout {
    companion object {
        const val SP_HAS_CONFIRM_INFO: String = "sp_has_confirm_info"   // 双人房是否确认过信息
    }

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {}

    init {
        View.inflate(context, R.layout.double_room_view_layout, this)

        start_match_iv.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                var hasConfirm = U.getPreferenceUtils().getSettingBoolean(SP_HAS_CONFIRM_INFO, false)
                if (!hasConfirm) {
                    EventBus.getDefault().post(ShowConfirmInfoEvent())
                } else {
                    ARouter.getInstance()
                            .build(RouterConstants.ACTIVITY_DOUBLE_PLAY)
                            .navigation()
                }
            }
        })
    }

    fun destory() {

    }
}
