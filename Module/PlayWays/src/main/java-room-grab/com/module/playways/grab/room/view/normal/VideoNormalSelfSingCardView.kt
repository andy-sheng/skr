package com.module.playways.grab.room.view.normal

import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.view.MotionEvent
import android.view.View
import android.view.ViewStub

import com.common.log.MyLog
import com.common.utils.SpanUtils
import com.common.utils.U
import com.module.playways.R
import com.module.playways.grab.room.GrabRoomData
import com.module.playways.grab.room.model.GrabRoundInfoModel
import com.module.playways.grab.room.view.GrabRootView
import com.module.playways.grab.room.view.control.SelfSingCardView
import com.module.playways.grab.room.view.normal.view.SelfSingLyricView
import com.module.playways.room.data.H
import com.module.playways.room.song.model.SongModel

/**
 * 你的主场景歌词
 */
class VideoNormalSelfSingCardView(viewStub: ViewStub, private val mGrabRootView: GrabRootView) : SelfSingLyricView(viewStub) {

    internal var mOverListener: (()->Unit)? = null

    override fun init(parentView: View) {
        super.init(parentView)
        mManyLyricsView!!.spaceLineHeight = U.getDisplayUtils().dip2px(10f).toFloat()
        mGrabRootView.addOnTouchListener(View.OnTouchListener { v, event ->
            if (mSvlyric!!.isShown) {
                mSvlyric!!.onTouchEvent(event)
            } else false
        })
    }

    override fun layoutDesc(): Int {
        return R.layout.grab_video_normal_self_sing_card_stub_layout
    }


    fun playLyric() {
        val infoModel = H.grabRoomData?.realRoundInfo
        if (infoModel == null) {
            MyLog.d(TAG, "infoModel 是空的")
            return
        }
        tryInflate()
        setVisibility(View.VISIBLE)
        if (infoModel!!.music == null) {
            MyLog.d(TAG, "songModel 是空的")
            return
        }

        val totalTs = infoModel!!.singTotalMs
        var withAcc = false
        if (infoModel!!.isAccRound && H.grabRoomData?.isAccEnable == true || infoModel!!.isPKRound) {
            withAcc = true
        }
        if (!withAcc) {
            playWithNoAcc(H.getSongModel())
        } else {
            playWithAcc(H.getSongModel(), totalTs)
        }
    }

    override fun createLyricSpan(lyric: String, songModel: SongModel?): SpannableStringBuilder? {
        var spanUtils = SpanUtils()
        spanUtils = spanUtils.append(lyric)
                .setShadow(U.getDisplayUtils().dip2px(1f).toFloat(), 0f, U.getDisplayUtils().dip2px(1f).toFloat(), U.getColor(R.color.black_trans_50))
        if (songModel != null && !TextUtils.isEmpty(songModel.uploaderName)) {
            spanUtils = spanUtils.append("\n")
                    .append("上传者:" + songModel.uploaderName).setFontSize(12, true)
                    .setShadow(U.getDisplayUtils().dip2px(1f).toFloat(), 0f, U.getDisplayUtils().dip2px(1f).toFloat(), U.getColor(R.color.black_trans_50))
        }
        return spanUtils.create()
    }
}
