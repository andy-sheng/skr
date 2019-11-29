package com.module.playways.grab.room.view.freemic

import android.os.Handler
import android.view.View
import android.view.ViewStub
import android.widget.ScrollView
import android.widget.TextView

import com.common.log.MyLog
import com.common.utils.U
import com.common.view.DebounceViewClickListener
import com.common.view.ExViewStub
import com.common.view.ex.ExImageView
import com.module.playways.R
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.grab.room.model.GrabRoundInfoModel
import com.module.playways.grab.room.view.SingCountDownView2
import com.module.playways.grab.room.view.control.SelfSingCardView
import com.zq.live.proto.GrabRoom.EQRoundStatus
import com.component.lyrics.LyricsManager
import com.zq.mediaengine.kit.ZqEngineKit

import io.reactivex.functions.Consumer

/**
 * 自由麦自己视角的卡片
 */
class FreeMicSelfSingCardView(viewStub: ViewStub, internal var mRoomData: GrabRoomData?) : ExViewStub(viewStub) {

    val TAG = "MiniGameSelfSingCardView"

    internal var mIvBg: ExImageView?=null
    internal var mSvLyric: ScrollView?=null
    internal var mTvLyric: TextView?=null
    internal var mSingCountDownView: SingCountDownView2? = null
    internal var mmMicControlBtn: ExImageView?=null
    var mOverListener:(()->Unit)?=null
//    var handler = Handler()

    override fun init(parentView: View) {
        mIvBg = parentView.findViewById(R.id.iv_bg)
        mSvLyric = parentView.findViewById(R.id.sv_lyric)
        mTvLyric = parentView.findViewById(R.id.tv_lyric)
        mSingCountDownView = parentView.findViewById(R.id.sing_count_down_view)
        mSingCountDownView!!.setListener(mOverListener)
        mmMicControlBtn = parentView.findViewById(R.id.mic_control_btn)
        mmMicControlBtn?.setOnClickListener(object : DebounceViewClickListener() {
            override fun clickValid(v: View) {
                if (ZqEngineKit.getInstance().params.isAnchor) {
                    ZqEngineKit.getInstance().setClientRole(false)
                    mmMicControlBtn?.setImageResource(R.drawable.free_mic_close_mic)
                } else {
                    ZqEngineKit.getInstance().setClientRole(true)
                    ZqEngineKit.getInstance().muteLocalAudioStream(false)
                    mmMicControlBtn?.setImageResource(R.drawable.free_mic_open_mic)
                }
            }
        })
    }

    override fun layoutDesc(): Int {
        return R.layout.grab_free_mic_self_sing_card_stub_layout
    }

    fun playLyric(): Boolean {
        tryInflate()
        setVisibility(View.VISIBLE)
        val infoModel = mRoomData?.realRoundInfo
        if (infoModel != null) {
            val totalMs = infoModel.music.standIntroEndT - infoModel.music.standIntroBeginT
            val progress: Int  //当前进度条
            val leaveTime: Int //剩余时间
            MyLog.d(TAG, "countDown isParticipant:" + infoModel.isParticipant + " enterStatus=" + infoModel.enterStatus)
            if (!infoModel.isParticipant && infoModel.enterStatus == EQRoundStatus.QRS_INTRO.value) {
                MyLog.d(TAG, "演唱阶段加入的，倒计时没那么多")
                progress = infoModel.elapsedTimeMs * 100 / totalMs
                leaveTime = totalMs - infoModel.elapsedTimeMs
            } else {
                progress = 1
                leaveTime = totalMs
            }
            mSingCountDownView!!.startPlay(progress, leaveTime, true)
            val url = infoModel.music.standLrc

            LyricsManager
                    .loadGrabPlainLyric(url)
                    .subscribe({ s -> mTvLyric?.text = s }, { throwable -> MyLog.e(throwable) })
        }
        if (mRoomData?.isInPlayerList == true) {
            mmMicControlBtn?.visibility = View.VISIBLE
            if (ZqEngineKit.getInstance().params.isAnchor) {
                mmMicControlBtn?.setImageResource(R.drawable.free_mic_open_mic)
            } else {
                mmMicControlBtn?.setImageResource(R.drawable.free_mic_close_mic)
            }
        } else {
            mmMicControlBtn?.visibility = View.GONE
        }
        return true
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            if (mSingCountDownView != null) {
                mSingCountDownView!!.reset()
            }
        }
    }

    fun destroy() {

    }

}
