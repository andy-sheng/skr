package com.module.playways.battle.room.view

import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.view.View
import android.view.ViewStub
import android.widget.FrameLayout
import com.common.log.MyLog
import com.common.utils.SpanUtils
import com.common.utils.U
import com.component.lyrics.LyricAndAccMatchManager
import com.component.lyrics.LyricsReader
import com.component.lyrics.widget.ManyLyricsView
import com.component.lyrics.widget.VoiceScaleView
import com.module.playways.R
import com.module.playways.battle.room.BattleRoomData
import com.module.playways.battle.room.model.BattleRoundInfoModel
import com.module.playways.grab.room.view.SingCountDownView2
import com.module.playways.room.song.model.SongModel
import com.zq.mediaengine.kit.ZqEngineKit
import io.reactivex.disposables.Disposable

class BattleSelfSingLyricView(viewStub: ViewStub, protected var mRoomData: BattleRoomData?) : BaseSceneView(viewStub) {
    val TAG = "RaceSelfSingLyricView"

    internal lateinit var mManyLyricsView: ManyLyricsView
    internal lateinit var mSingCountDownView2: SingCountDownView2
    internal lateinit var mVoiceScaleView: VoiceScaleView

    internal var mDisposable: Disposable? = null
    internal var mSongModel: SongModel? = null
    internal var mLyricAndAccMatchManager: LyricAndAccMatchManager? = LyricAndAccMatchManager()

    override fun init(parentView: View) {
        parentView?.let {
            mManyLyricsView = it.findViewById(R.id.many_lyrics_view)
            mSingCountDownView2 = it.findViewById(R.id.sing_count_down_view)
            mVoiceScaleView = it.findViewById(R.id.voice_scale_view)
        }
    }

    override fun layoutDesc(): Int {
        return R.layout.battle_self_sing_lyric_layout
    }

    private fun initLyric() {
        mManyLyricsView?.visibility = View.GONE
        mManyLyricsView?.initLrcData()
        mVoiceScaleView?.visibility = View.GONE
    }

    fun show(offset: Int, call: (() -> Unit)?) {
        var battleRoundInfoModel = mRoomData?.realRoundInfo
        if (battleRoundInfoModel == null) {
            battleRoundInfoModel = mRoomData?.expectRoundInfo
        }

        battleRoundInfoModel?.let {
            enterAnimation()
            val totalMs = it.music?.totalMs ?: 0
            mSingCountDownView2.startPlay(offset, totalMs - offset, true)
            mSingCountDownView2.setListener {
                call?.invoke()
            }

            playWithAcc(offset, true, it, totalMs)
        }
    }

    fun hide() {
        leaveAnimation()
    }

    private fun playWithAcc(offset: Int, isSelf: Boolean, infoModel: BattleRoundInfoModel?, totalTs: Int) {
        if (infoModel == null) {
            MyLog.w(TAG, "playWithAcc infoModel = null totalTs=$totalTs")
            return
        }

        initLyric()
        mSongModel = infoModel.music
        var curSong = mSongModel
        if (curSong == null) {
            MyLog.w(TAG, "playWithAcc curSong = null totalTs=$totalTs")
            return
        }
        mManyLyricsView.setEnableVerbatim(isSelf)
        val configParams = LyricAndAccMatchManager.ConfigParams()
        configParams.manyLyricsView = mManyLyricsView
        configParams.voiceScaleView = mVoiceScaleView
        configParams.lyricUrl = curSong.lyric
        configParams.lyricBeginTs = curSong.standLrcBeginT
//        configParams.lyricEndTs = curSong.rankLrcBeginT+totalTs
        // 和佳胜讨论，改成伴奏开始 + 总时间 胜哥说 prefectotal
        configParams.lyricEndTs = curSong.beginMs + totalTs - 5000

        configParams.accBeginTs = curSong.beginMs
        configParams.accEndTs = curSong.beginMs + totalTs
        configParams.authorName = curSong.uploaderName
        configParams.needScore = false
        configParams.needWaitACC = isSelf

        mLyricAndAccMatchManager!!.setArgs(configParams)

        (mManyLyricsView.layoutParams as FrameLayout.LayoutParams).topMargin = U.getDisplayUtils().dip2px(-38f)
        mVoiceScaleView.visibility = View.VISIBLE
        mManyLyricsView.setUpLineNum(1)
        mManyLyricsView.setDownLineNum(1)

        val finalCurSong = curSong
        mLyricAndAccMatchManager!!.start(object : LyricAndAccMatchManager.Listener() {
            override fun onLyricBindSuccess(lyricsReader: LyricsReader?) {
                if (offset > 0) {
                    configParams.manyLyricsView?.seekTo(curSong.beginMs + offset)
                }
            }
        })

        ZqEngineKit.getInstance().setRecognizeListener { result, list, targetSongInfo, lineNo -> mLyricAndAccMatchManager!!.onAcrResult(result, list, targetSongInfo, lineNo) }
    }

    protected fun createLyricSpan(lyric: String, songModel: SongModel?): SpannableStringBuilder? {
        return if (songModel != null && !TextUtils.isEmpty(songModel.uploaderName)) {
            SpanUtils()
                    .append(lyric)
                    .append("\n")
                    .append("上传者:" + songModel.uploaderName).setFontSize(12, true)
                    .create()
        } else null
    }

    override fun onViewDetachedFromWindow(v: View) {
        super.onViewDetachedFromWindow(v)
        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable!!.dispose()
        }
        if (mLyricAndAccMatchManager != null) {
            mLyricAndAccMatchManager!!.stop()
        }
    }

    fun destroy() {
        if (mManyLyricsView != null) {
            mManyLyricsView!!.release()
        }
    }

    fun reset() {
        if (mParentView != null) {
            MyLog.d(TAG, "reset")
            mManyLyricsView?.lyricsReader = null
            mLyricAndAccMatchManager?.stop()
            mSingCountDownView2?.reset()
        }
    }

    override fun setVisibility(visibility: Int) {
        super.setVisibility(visibility)
        if (visibility == View.GONE) {
            reset()
        }
    }
}
