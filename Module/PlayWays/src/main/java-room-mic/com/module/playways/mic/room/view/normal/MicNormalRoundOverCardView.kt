package com.module.playways.mic.room.view.normal

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.ViewStub
import android.widget.ImageView
import android.widget.TextView
import com.common.log.MyLog
import com.common.view.ExViewStub
import com.module.playways.R
import com.module.playways.listener.SVGAListener
import com.module.playways.mic.room.model.MicRoundInfoModel
import com.module.playways.room.data.H
import com.zq.live.proto.MicRoom.MScoreTipType

/**
 * 轮次结束 合唱和正常结束都用此板
 */
class MicNormalRoundOverCardView(viewStub: ViewStub) : ExViewStub(viewStub) {

    val MSG_HIDE = 1
    var resultBgIv: ImageView? = null
    var songNameTv: TextView? = null
    var singerNameTv: TextView? = null
    var scoreDescIv: ImageView? = null
    var rankDescTv: TextView? = null

    var overListener: SVGAListener? = null

    val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message?) {
            super.handleMessage(msg)
            if (msg?.what == MSG_HIDE) {
                overListener?.onFinished()
            }
        }
    }

    override fun init(parentView: View) {
        resultBgIv = parentView.findViewById(R.id.result_bg_iv)
        songNameTv = parentView.findViewById(R.id.song_name_tv)
        singerNameTv = parentView.findViewById(R.id.singer_name_tv)
        scoreDescIv = parentView.findViewById(R.id.score_desc_iv)
        rankDescTv = parentView.findViewById(R.id.rank_desc_tv)
    }

    override fun layoutDesc(): Int {
        return R.layout.mic_normal_round_over_card_stub_layout
    }

    fun bindData(lastRoundInfo: MicRoundInfoModel?, overListener: SVGAListener) {
        MyLog.d("MicNormalRoundOverCardView", "bindData lastRoundInfo = $lastRoundInfo, overListener = $overListener")
        this.overListener = overListener
        tryInflate()
        setVisibility(View.VISIBLE)
        songNameTv?.text = "《${lastRoundInfo?.music?.displaySongName}》"
        singerNameTv?.text = "演唱者:${H.micRoomData?.getPlayerOrWaiterInfo(lastRoundInfo?.userID)?.nicknameRemark}"
        when {
            lastRoundInfo?.commonRoundResult?.finalTip == MScoreTipType.MST_NICE_PERFECT.value -> scoreDescIv?.setImageResource(R.drawable.xkf_pingjia_wanmei)
            lastRoundInfo?.commonRoundResult?.finalTip == MScoreTipType.MST_NOT_BAD.value -> scoreDescIv?.setImageResource(R.drawable.xkf_pingjia_bucuo)
            lastRoundInfo?.commonRoundResult?.finalTip == MScoreTipType.MST_VERY_GOOD.value -> scoreDescIv?.setImageResource(R.drawable.xkf_pingjia_bang)
            lastRoundInfo?.commonRoundResult?.finalTip == MScoreTipType.MST_TOO_BAD.value -> scoreDescIv?.setImageResource(R.drawable.xkf_pingjia_jiayou)
            lastRoundInfo?.commonRoundResult?.finalTip == MScoreTipType.MST_UNKNOWN.value -> scoreDescIv?.setImageResource(R.drawable.xkf_pingjia_jiayou)
        }
        rankDescTv?.text = lastRoundInfo?.commonRoundResult?.finalMsg
        handler.removeMessages(MSG_HIDE)
        handler.sendEmptyMessageDelayed(MSG_HIDE, 3000)
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            handler.removeCallbacksAndMessages(null)
        }
    }
}
