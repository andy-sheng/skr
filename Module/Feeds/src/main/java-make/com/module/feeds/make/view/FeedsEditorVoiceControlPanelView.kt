package com.module.feeds.make.view

import android.content.Context
import android.util.AttributeSet
import android.widget.SeekBar
import com.common.log.MyLog
import com.component.busilib.R
import com.component.voice.control.VoiceControlPanelView
import com.engine.Params
import com.zq.mediaengine.kit.ZqAudioEditorKit

class FeedsEditorVoiceControlPanelView(context: Context?, attrs: AttributeSet?) : VoiceControlPanelView(context, attrs) {

    var mZqAudioEditorKit:ZqAudioEditorKit?=null

    var mPeopleVoiceIndex = 0

    override fun setListener() {
        mPeopleVoiceSeekbar.max = 100
        mPeopleVoiceSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mZqAudioEditorKit?.setInputVolume(mPeopleVoiceIndex,progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        mMusicVoiceSeekbar.max = 100
        mMusicVoiceSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mZqAudioEditorKit?.setInputVolume(0,progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        mScenesBtnGroup.setOnCheckedChangeListener { group, checkedId ->
            MyLog.d(TAG, "onCheckedChanged group=$group checkedId=$checkedId")
            if (checkedId == R.id.default_sbtn) {
                mZqAudioEditorKit?.setAudioEffect(mPeopleVoiceIndex,Params.AudioEffect.dianyin.ordinal)
            } else if (checkedId == R.id.ktv_sbtn) {
            } else if (checkedId == R.id.rock_sbtn) {
            } else if (checkedId == R.id.dianyin_sbtn) {
            } else if (checkedId == R.id.kongling_sbtn) {
            }
        }
    }

    override fun bindData() {
        var styleEnum: Params.AudioEffect? = null
        if (styleEnum == Params.AudioEffect.dianyin) {
            mScenesBtnGroup.check(R.id.dianyin_sbtn)
        } else if (styleEnum == Params.AudioEffect.kongling) {
            mScenesBtnGroup.check(R.id.kongling_sbtn)
        } else if (styleEnum == Params.AudioEffect.ktv) {
            mScenesBtnGroup.check(R.id.ktv_sbtn)
        } else if (styleEnum == Params.AudioEffect.rock) {
            mScenesBtnGroup.check(R.id.rock_sbtn)
        } else {
            mScenesBtnGroup.check(R.id.default_sbtn)
        }

        mPeopleVoiceSeekbar.progress = mZqAudioEditorKit?.getAudioEffect(mPeopleVoiceIndex)?:50
        mMusicVoiceSeekbar.progress = mZqAudioEditorKit?.getAudioEffect(0)?:50

    }
}