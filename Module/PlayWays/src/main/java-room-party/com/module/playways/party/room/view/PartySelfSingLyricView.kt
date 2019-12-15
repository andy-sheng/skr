package com.module.playways.party.room.view

import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.view.View
import android.view.ViewStub
import com.common.log.MyLog
import com.common.utils.SpanUtils
import com.common.view.ExViewStub
import com.component.lyrics.LyricAndAccMatchManager
import com.component.lyrics.LyricsReader
import com.component.lyrics.widget.ManyLyricsView
import com.module.playways.R
import com.module.playways.grab.room.view.SingCountDownView2
import com.module.playways.party.room.PartyRoomData
import com.module.playways.party.room.model.PartyRoundInfoModel
import com.module.playways.room.song.model.SongModel
import com.zq.mediaengine.kit.ZqEngineKit
import io.reactivex.disposables.Disposable

class PartySelfSingLyricView(viewStub: ViewStub, protected var mRoomData: PartyRoomData?) : ExViewStub(viewStub) {
    val TAG = "RaceSelfSingLyricView"

    internal lateinit var mManyLyricsView: ManyLyricsView
    internal lateinit var mSingCountDownView2: SingCountDownView2

    internal var mDisposable: Disposable? = null
    internal var mSongModel: SongModel? = null
    internal var mLyricAndAccMatchManager: LyricAndAccMatchManager? = LyricAndAccMatchManager()

    override fun init(parentView: View) {
        parentView?.let {
            mManyLyricsView = it.findViewById(R.id.many_lyrics_view)
            mSingCountDownView2 = it.findViewById(R.id.sing_count_down_view)
        }
    }

    override fun layoutDesc(): Int {
        return R.layout.party_self_sing_lyric_layout
    }

    private fun initLyric() {
        mManyLyricsView?.visibility = View.GONE
        mManyLyricsView?.initLrcData()
    }

    fun startFly(offset: Int, isSelf: Boolean, call: (() -> Unit)?) {
        tryInflate()
        val infoModel = mRoomData?.realRoundInfo
        val totalMs = mRoomData?.realRoundInfo?.sceneInfo?.ktv?.singTimeMs ?: 0
        mSingCountDownView2.startPlay(0, totalMs, true)
        mSingCountDownView2.setListener {
            call?.invoke()
        }

        playWithAcc(offset, isSelf, infoModel, totalMs)
    }

    private fun playWithAcc(offset: Int, isSelf: Boolean, infoModel: PartyRoundInfoModel?, totalTs: Int) {
        if (infoModel == null) {
            MyLog.w(TAG, "playWithAcc infoModel = null totalTs=$totalTs")
            return
        }

        initLyric()
        mSongModel = infoModel.sceneInfo?.ktv?.music
        var curSong = mSongModel
        if (curSong == null) {
            MyLog.w(TAG, "playWithAcc curSong = null totalTs=$totalTs")
            return
        }
        mManyLyricsView.setEnableVerbatim(isSelf)
        val configParams = LyricAndAccMatchManager.ConfigParams()
        configParams.manyLyricsView = mManyLyricsView
        configParams.lyricUrl = curSong.lyric
        configParams.lyricBeginTs = curSong.standLrcBeginT
        configParams.lyricEndTs = curSong.standLrcBeginT + totalTs
        configParams.accBeginTs = curSong.beginMs
        configParams.accEndTs = curSong.beginMs + totalTs
        configParams.authorName = curSong.uploaderName
        configParams.needScore = false
        configParams.needWaitAAC = isSelf

        mLyricAndAccMatchManager!!.setArgs(configParams)
        val finalCurSong = curSong
        mLyricAndAccMatchManager!!.start(object : LyricAndAccMatchManager.Listener {

            override fun onLyricParseSuccess(reader: LyricsReader) {
//                mSvlyric.visibility = View.GONE
                if (offset > 0) {
                    configParams.manyLyricsView?.seekTo(curSong.beginMs + offset)
                }
            }

            override fun onLyricParseFailed() {
//                playWithNoAcc(finalCurSong)
            }

            override fun onLyricEventPost(lineNum: Int) {
                //                mRoomData.setSongLineNum(lineNum);
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
