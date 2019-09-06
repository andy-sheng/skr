package com.module.playways.race.room.view

import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.view.View
import android.view.ViewStub
import android.widget.ScrollView
import android.widget.TextView
import com.common.log.MyLog
import com.common.utils.SpanUtils
import com.common.view.ExViewStub
import com.component.lyrics.LyricAndAccMatchManager
import com.component.lyrics.LyricsManager
import com.component.lyrics.LyricsReader
import com.component.lyrics.widget.ManyLyricsView
import com.component.lyrics.widget.VoiceScaleView
import com.module.playways.R
import com.module.playways.grab.room.view.SingCountDownView2
import com.module.playways.grab.room.view.control.SelfSingCardView
import com.module.playways.race.room.RaceRoomData
import com.module.playways.race.room.model.RaceRoundInfoModel
import com.module.playways.room.song.model.SongModel
import com.zq.mediaengine.kit.ZqEngineKit
import io.reactivex.disposables.Disposable

class RaceSelfSingLyricView(viewStub: ViewStub, protected var mRoomData: RaceRoomData?) : ExViewStub(viewStub) {
    val TAG = "RaceSelfSingLyricView"

    protected lateinit var mSvlyric: ScrollView
    protected lateinit var mTvLyric: TextView
    protected lateinit var mManyLyricsView: ManyLyricsView
    internal lateinit var mSingCountDownView2: SingCountDownView2
    internal lateinit var mVoiceScaleView: VoiceScaleView

    internal var mDisposable: Disposable? = null
    internal var mSongModel: SongModel? = null
    internal var mLyricAndAccMatchManager: LyricAndAccMatchManager? = LyricAndAccMatchManager()

    override fun init(parentView: View) {
        parentView?.let {
            mSvlyric = it.findViewById(R.id.sv_lyric)
            mTvLyric = it.findViewById(R.id.tv_lyric)
            mManyLyricsView = it.findViewById(R.id.many_lyrics_view)
            mVoiceScaleView = it.findViewById(R.id.voice_scale_view)
            mSingCountDownView2 = it.findViewById(R.id.sing_count_down_view)
        }
    }

    override fun layoutDesc(): Int {
        return R.layout.race_self_sing_lyric_layout
    }

    private fun initLyric() {
        mSvlyric.visibility = View.VISIBLE
        mManyLyricsView!!.visibility = View.GONE
        mManyLyricsView!!.initLrcData()

        if (mVoiceScaleView != null) {
            mVoiceScaleView!!.visibility = View.GONE
        }
    }

    fun startFly(call: (() -> Unit)?) {
        tryInflate()
        val infoModel = mRoomData!!.realRoundInfo
        mSingCountDownView2.startPlay(0, infoModel!!.getSingTotalMs(), true)
        mSingCountDownView2.setListener(SelfSingCardView.Listener {
            call?.invoke()
        })

        var withAcc = false
        if (infoModel != null) {
            if (infoModel.isAccRoundNow() && mRoomData != null && mRoomData!!.isAccEnable) {
                withAcc = true
            }
            if (!withAcc) {
                playWithNoAcc(infoModel.getSongModelNow())
            } else {
                playWithAcc(infoModel, infoModel.getSingTotalMs())
            }
        }
    }

    fun playWithAcc(infoModel: RaceRoundInfoModel?, totalTs: Int) {
        if (infoModel == null) {
            MyLog.w(TAG, "playWithAcc infoModel = null totalTs=$totalTs")
            return
        }

        initLyric()
        mSongModel = infoModel.getSongModelNow()
        var curSong = mSongModel
        if (curSong == null) {
            MyLog.w(TAG, "playWithAcc curSong = null totalTs=$totalTs")
            return
        }
        val configParams = LyricAndAccMatchManager.ConfigParams()
        configParams.manyLyricsView = mManyLyricsView
        configParams.voiceScaleView = mVoiceScaleView
        configParams.lyricUrl = curSong.lyric
        configParams.lyricBeginTs = curSong.standLrcBeginT
        configParams.lyricEndTs = curSong.standLrcBeginT + totalTs
        configParams.accBeginTs = curSong.beginMs
        configParams.accEndTs = curSong.beginMs + totalTs
        configParams.authorName = curSong.uploaderName
        mLyricAndAccMatchManager!!.setArgs(configParams)
        val finalCurSong = curSong
        mLyricAndAccMatchManager!!.start(object : LyricAndAccMatchManager.Listener {

            override fun onLyricParseSuccess(reader: LyricsReader) {
                mSvlyric.visibility = View.GONE
            }

            override fun onLyricParseFailed() {
                playWithNoAcc(finalCurSong)
            }

            override fun onLyricEventPost(lineNum: Int) {
                //                mRoomData.setSongLineNum(lineNum);
            }

        })
        ZqEngineKit.getInstance().setRecognizeListener { result, list, targetSongInfo, lineNo -> mLyricAndAccMatchManager!!.onAcrResult(result, list, targetSongInfo, lineNo) }
    }

    fun playWithNoAcc(songModel: SongModel?) {
        if (songModel == null) {
            return
        }

        initLyric()
        mManyLyricsView!!.visibility = View.GONE
        mSvlyric.visibility = View.VISIBLE
        mSvlyric.scrollTo(0, 0)
        if (mDisposable != null && !mDisposable!!.isDisposed) {
            mDisposable!!.dispose()
        }
        mDisposable = LyricsManager
                .loadGrabPlainLyric(songModel.standLrc)
                .subscribe({ s ->
                    val ssb = createLyricSpan(s, songModel)
                    if (ssb == null) {
                        mTvLyric.text = s
                    } else {
                        mTvLyric.text = ssb
                    }
                }, { throwable -> MyLog.e(TAG, "accept throwable=$throwable") })
        mLyricAndAccMatchManager!!.stop()
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
