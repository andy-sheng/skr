package com.module.feeds.make.view

import android.content.Context
import android.util.AttributeSet
import android.widget.RadioGroup
import android.widget.SeekBar
import com.common.log.MyLog
import com.component.voice.control.VoiceControlPanelView
import com.engine.Params
import com.module.feeds.R
import com.zq.mediaengine.kit.ZqAudioEditorKit
import com.zq.mediaengine.kit.ZqEngineKit

class FeedsMakeVoiceControlPanelView(context: Context?, attrs: AttributeSet?) : VoiceControlPanelView(context, attrs) {

    constructor(context: Context?):this(context,null)

    override fun getLayout(): Int {
        return R.layout.feeds_editor_voice_control_panel_layout
    }


    override fun setListener() {
        super.setListener()
        mPeopleVoiceSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                ZqEngineKit.getInstance().params.recordingSignalVolume = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

    }

}