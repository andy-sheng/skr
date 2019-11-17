package com.module.playways.audition.view

import android.content.Context
import android.util.AttributeSet
import android.widget.CompoundButton
import android.widget.LinearLayout

import com.common.utils.U
import com.kyleduo.switchbutton.SwitchButton
import com.module.playways.grab.room.GrabRoomData
import com.component.voice.control.VoiceControlPanelView
import com.module.playways.R

class AudienceVoiceControlPanelView(context: Context, attrs: AttributeSet) : VoiceControlPanelView(context, attrs) {
    override fun getLayout(): Int {
        return R.layout.voice_control_panel_layout
    }
}
