package com.module.playways.grab.room.voicemsg

import android.os.Handler
import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import com.common.utils.U
import com.module.playways.R
import com.common.view.ExViewStub

class VoiceRecordTipsView(viewStub: ViewStub) : ExViewStub(viewStub) {

    var mHintText: TextView? = null
    var mVoiceHintIv: ImageView? = null
    var mVoiceVolumeView: VoiceVolumeView? = null
    var mHandler: Handler? = null

    override fun init(parentView: View?) {
        mHintText = parentView?.findViewById(R.id.hint_text)
        mVoiceHintIv = parentView?.findViewById(R.id.voice_hint_iv)
        mVoiceVolumeView = parentView?.findViewById(R.id.voice_volume_view)
        parentView?.setOnTouchListener { _, _ -> true }
        mHandler = Handler()
    }

    override fun layoutDesc(): Int {
        return R.layout.voice_record_tips_view_layout
    }

    fun show() {
        tryInflate()
        // 需要reset
        mHintText?.text = "手指上滑，取消发送"
        mHintText?.setTextColor(U.getColor(R.color.white_trans_60))
        mVoiceHintIv?.background = U.getDrawable(R.drawable.voice_tips_speak_icon)
        mVoiceVolumeView?.setVoiceLevel(1)
        mVoiceVolumeView?.visibility = View.VISIBLE
        mHandler?.removeCallbacksAndMessages(null)
        mParentView.visibility = View.VISIBLE
    }

    fun hide() {
        mParentView.visibility = View.GONE
    }

    fun changeVoiceLevel(level: Int) {
        mVoiceVolumeView?.setVoiceLevel(level)
    }

    fun cancelRecord() {
        mHintText?.text = "松开手指，取消发送"
        mHintText?.setTextColor(U.getColor(R.color.white))
        mVoiceHintIv?.background = U.getDrawable(R.drawable.voice_tips_cancel_icon)
        mVoiceVolumeView?.visibility = View.GONE
    }

    fun remainTime(text: String) {
        mHintText?.text = text
        mHintText?.setTextColor(U.getColor(R.color.white))
    }

    fun showLimitTime(short: Boolean) {
        mVoiceHintIv?.background = U.getDrawable(R.drawable.voice_tips_error_icon)
        mVoiceVolumeView?.visibility = View.GONE
        mHintText?.setTextColor(U.getColor(R.color.white))
        if (short) {
            mHintText?.text = "说话时间太短"
        } else {
            mHintText?.text = "说话时间太长"
        }
        mHandler?.postDelayed(Runnable {
            hide()
        }, 1000)
    }
}
