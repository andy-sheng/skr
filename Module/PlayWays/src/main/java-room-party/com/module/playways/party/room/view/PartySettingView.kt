package com.module.playways.party.room.view

import android.graphics.Rect
import android.view.View
import android.view.ViewStub
import android.widget.TextView
import com.common.core.view.setAnimateDebounceViewClickListener
import com.common.utils.U
import com.common.view.ExViewStub
import com.module.playways.R

class PartySettingView(viewStub: ViewStub) : ExViewStub(viewStub) {

    lateinit var gameSettingTv: TextView
    lateinit var soundSettingTv: TextView
    lateinit var muteSettingTv: TextView

    var isAllMute = false
    var listener: Listener? = null

    override fun init(parentView: View) {
        gameSettingTv = parentView.findViewById(R.id.game_setting_tv)
        soundSettingTv = parentView.findViewById(R.id.sound_setting_tv)
        muteSettingTv = parentView.findViewById(R.id.mute_setting_tv)

        gameSettingTv.setAnimateDebounceViewClickListener {
            listener?.onClickGameSetting()
        }

        soundSettingTv.setAnimateDebounceViewClickListener {
            listener?.onClickGameSound()
        }

        muteSettingTv.setAnimateDebounceViewClickListener {
            listener?.onClickAllMute(!isAllMute)
        }
    }

    override fun layoutDesc(): Int {
        return R.layout.party_setting_view_layout
    }

    fun bindData() {
        tryInflate()
        // todo 补全，主要是全员闭麦或者开麦
        var drawable = U.getDrawable(R.drawable.party_setting_all_unmute)
        if (isAllMute) {
            muteSettingTv.text = "全员开麦"
        } else {
            drawable = U.getDrawable(R.drawable.party_setting_all_mute)
            muteSettingTv.text = "全员闭麦"
        }
        drawable.bounds = Rect(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
        muteSettingTv.setCompoundDrawables(null, drawable, null, null)
    }

    interface Listener {
        fun onClickGameSetting()
        fun onClickGameSound()
        fun onClickAllMute(isMute: Boolean)
    }
}
