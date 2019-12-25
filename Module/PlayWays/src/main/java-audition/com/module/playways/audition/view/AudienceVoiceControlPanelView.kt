package com.module.playways.audition.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.CompoundButton
import android.widget.LinearLayout

import com.common.utils.U
import com.kyleduo.switchbutton.SwitchButton
import com.module.playways.grab.room.GrabRoomData
import com.component.voice.control.VoiceControlPanelView
import com.module.playways.R
import com.zq.mediaengine.kit.ZqEngineKit

class AudienceVoiceControlPanelView(context: Context, attrs: AttributeSet) : VoiceControlPanelView(context, attrs) {
    protected var mMixSb: SwitchButton? = null
    protected var mLowLatencySb: SwitchButton? = null

    override fun getLayout(): Int {
        return R.layout.audition_voice_control_panel_layout
    }

    override fun init(context: Context?) {
        super.init(context)
        mMixSb = this.findViewById(com.component.busilib.R.id.mix_sb)
        mLowLatencySb = this.findViewById(com.component.busilib.R.id.low_latency_sb)
    }

    override fun setListener() {
        super.setListener()
        mLowLatencySb?.setOnCheckedChangeListener { buttonView, isChecked ->
            ZqEngineKit.getInstance().setEnableAudioLowLatency(isChecked)
        }
        mEarSb?.setOnCheckedChangeListener { buttonView, isChecked ->
            // TODO: 测试用途
//            ZqEngineKit.getInstance().enableInEarMonitoring(isChecked)
            if (isChecked) {
                mMixSb?.setCheckedNoEvent(false)
            }
            ZqEngineKit.getInstance().setEnableAudioPreviewLatencyTest(isChecked)
        }
        mMixSb?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                mEarSb?.setCheckedNoEvent(false)
            }
            ZqEngineKit.getInstance().setEnableAudioMixLatencyTest(isChecked)
        }
    }

    override fun bindData() {
        super.bindData()
        if (ZqEngineKit.getInstance().params != null) {
            mEarSb?.setCheckedNoEvent(ZqEngineKit.getInstance().params.isEnableAudioPreviewLatencyTest)
            mMixSb?.setCheckedNoEvent(ZqEngineKit.getInstance().params.isEnableAudioMixLatencyTest)
            mLowLatencySb?.setCheckedNoEvent(ZqEngineKit.getInstance().params.isEnableAudioLowLatency)
        }
    }
}
