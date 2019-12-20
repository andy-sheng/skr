package com.module.playways.relay.room.view

import android.view.View
import android.view.ViewStub
import android.widget.TextView
import com.common.log.MyLog
import com.common.utils.U
import com.common.utils.dp
import com.common.view.ExViewStub
import com.common.view.ex.ExView
import com.component.busilib.model.EffectModel
import com.component.busilib.view.GameEffectBgView
import com.component.lyrics.LyricAndAccMatchManager
import com.component.lyrics.LyricsReader
import com.component.lyrics.model.LyricsLineInfo
import com.component.lyrics.widget.ManyLyricsView
import com.component.lyrics.widget.VoiceScaleView
import com.module.playways.R
import com.module.playways.grab.room.view.normal.NormalOthersSingCardView
import com.module.playways.relay.room.RelayRoomData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class RelaySingCardView(viewStub: ViewStub) : ExViewStub(viewStub) {
    val TAG = "RelaySingCardView"

    var effectBgView: GameEffectBgView? = null
    var othersSingCardView: NormalOthersSingCardView? = null

    lateinit var dotView: ExView
    lateinit var songNameTv: TextView
    lateinit var songPlayProgressTv: TextView
    lateinit var manyLyricsView: ManyLyricsView
    lateinit var voiceScaleView: VoiceScaleView
    lateinit var otherSingTipsTv: TextView
    lateinit var noSongTipsTv: TextView
    lateinit var singBeginTipsTv1: TextView
    lateinit var singBeginTipsTv2: TextView

    var roomData: RelayRoomData? = null

    val lyricAndAccMatchManager = LyricAndAccMatchManager()

    var countDownJob: Job? = null

    override fun init(parentView: View) {
        dotView = parentView.findViewById(R.id.dot_view)
        songNameTv = parentView.findViewById(R.id.song_name_tv)
        songPlayProgressTv = parentView.findViewById(R.id.song_play_progress_tv)
        manyLyricsView = parentView.findViewById(R.id.many_lyrics_view)
        voiceScaleView = parentView.findViewById(R.id.voice_scale_view)
        otherSingTipsTv = parentView.findViewById(R.id.other_sing_tips_tv)
        noSongTipsTv = parentView.findViewById(R.id.no_song_tips_tv)
        singBeginTipsTv1 = parentView.findViewById(R.id.sing_begin_tips_tv1)
        singBeginTipsTv2 = parentView.findViewById(R.id.sing_begin_tips_tv2)
        turnNoSong()
    }

    override fun layoutDesc(): Int {
        return R.layout.relay_sing_card_view_layout
    }

    fun turnSingBegin() {
        dotView.visibility = View.VISIBLE
        songNameTv.visibility = View.VISIBLE
        songPlayProgressTv.visibility = View.VISIBLE
        manyLyricsView.visibility = View.VISIBLE
        voiceScaleView.visibility = View.VISIBLE

        noSongTipsTv.visibility = View.GONE
        singBeginTipsTv1.visibility = View.GONE
        singBeginTipsTv2.visibility = View.GONE

        processTurnChange()
        if (roomData?.isSingByMeNow() == true) {
            dotView.setBackgroundResource(R.drawable.relay_sing_card_dot_view_bg2)
            otherSingTipsTv.visibility = View.GONE
        } else {
            dotView.setBackgroundResource(R.drawable.relay_sing_card_dot_view_bg1)
            otherSingTipsTv.visibility = View.VISIBLE
        }

        val music = roomData?.realRoundInfo?.music
        songNameTv.text = "《${music?.displaySongName}》"
        countDownJob = launch {
            var t = music?.endMs!! - music?.beginMs
            repeat(t / 1000) {
                var leftTs = (roomData?.getSingCurPosition() ?: 0)
                if (leftTs < 0) {
                    leftTs = 0
                }
                songPlayProgressTv.text = U.getDateTimeUtils().formatVideoTime(leftTs);
                delay(1000)
            }
        }
//        voiceScaleView?.durationProvider = {
//            roomData?.getSingCurPosition() ?: 0L
//        }
        manyLyricsView?.setDownLineNum(2)
        manyLyricsView?.setUpLineNum(1)
        manyLyricsView?.shiftY = 0.4f
//        manyLyricsView?.setSplitChorusArray(music?.relaySegments)
//        manyLyricsView?.setFirstSingByMe(roomData?.isFirstSingByMe() == true)

        val configParams = LyricAndAccMatchManager.ConfigParams()
        configParams.manyLyricsView = manyLyricsView
        configParams.lyricUrl = music?.lyric
        configParams.lyricBeginTs = music?.beginMs!!
        configParams.lyricEndTs = music?.endMs!!
        configParams.accBeginTs = music?.beginMs!!
        configParams.accEndTs = music?.endMs!!
        configParams.voiceScaleView = voiceScaleView
        configParams.needScore = false
        // 间隔 是否第一个唱
        lyricAndAccMatchManager!!.setArgs(configParams)
        lyricAndAccMatchManager!!.start(object : LyricAndAccMatchManager.Listener() {

            override fun onLyricParseSuccess(reader: LyricsReader) {
                var lineNum = 0
                var fisrtLine: LyricsLineInfo? = null
                // 判断歌词归属
                music?.relaySegments?.let {
                    for ((index, sp) in it.withIndex()) {
                        var singByMe = true
                        if (roomData?.isFirstSingByMe() == true) {
                            singByMe = (index % 2 == 0)
                        } else {
                            singByMe = (index % 2 != 0)
                        }
                        while (lineNum < reader.lrcLineInfos.size + 10) {
                            var lineInfo = reader.lrcLineInfos[lineNum++]
                            var jixu = true
                            lineInfo?.let { l ->
                                if (l.startTime < sp) {
                                    l.singByMe = singByMe
                                } else {
                                    l.spilit = true
                                    lineNum--
                                    jixu = false
                                }
                                if (fisrtLine == null) {
                                    fisrtLine = l
                                }
                            }
                            if (!jixu) {
                                break
                            }
                            //MyLog.d(TAG,"1lineLyrics=${lineInfo?.lineLyrics} singByMe=${lineInfo?.singByMe}")
                        }
                    }
                    var singByMe = true
                    if (roomData?.isFirstSingByMe() == true) {
                        singByMe = (it.size % 2 == 0)
                    } else {
                        singByMe = (it.size % 2 != 0)
                    }
                    // 余下的
                    while (lineNum < reader.lrcLineInfos.size + 10) {
                        var lineInfo = reader.lrcLineInfos[lineNum++]
                        lineInfo?.let { l ->
                            l.singByMe = singByMe
                            if (fisrtLine == null) {
                                fisrtLine = l
                            }
                        }
                        //MyLog.d(TAG,"2lineLyrics=${lineInfo?.lineLyrics} singByMe=${lineInfo?.singByMe}")
                    }
                    fisrtLine?.spilit = true
                }
//                mSvlyric?.visibility = View.GONE
            }
        })
//        LyricsManager
//                .loadStandardLyric("http://song-static.inframe.mobi/lrc/4ee4ac0711c74d6f333fcac10c113239.zrce")
//                .subscribe({ lyricsReader ->
//                    manyLyricsView?.visibility = View.VISIBLE
//                    manyLyricsView?.initLrcData()
//                    manyLyricsView?.lyricsReader = lyricsReader
//                    manyLyricsView?.setSplitChorusArray(intArrayOf(43*1000,65*1000,87*1000))
//                    manyLyricsView?.setFirstSingByMe(false)
//                    val set = HashSet<Int>()
//                    set.add(lyricsReader.getLineInfoIdByStartTs(0))
//                    set.add(19)
//                    manyLyricsView?.needCountDownLine = set
//                    manyLyricsView?.play(18*1000)
//                }, { throwable ->
//                    MyLog.e(TAG, throwable)
//                    DebugLogView.println(TAG, "歌词下载失败，采用不滚动方式播放歌词")
//                })
    }

    private fun processTurnChange() {
        if (roomData?.isSingByMeNow() == true) {
            dotView.setBackgroundResource(R.drawable.relay_sing_card_dot_view_bg2)
            otherSingTipsTv.visibility = View.GONE
            voiceScaleView.setStatus(3)
            if (roomData?.myEffectModel?.sourcesJson?.isNotEmpty() == true) {
                effectBgView?.showBgEffect(roomData?.myEffectModel?.effectModel)
            } else {
                effectBgView?.hideBg()
            }
        } else {
            dotView.setBackgroundResource(R.drawable.relay_sing_card_dot_view_bg1)
            otherSingTipsTv.visibility = View.VISIBLE
            voiceScaleView.setStatus(1)
            if (roomData?.peerEffectModel?.sourcesJson?.isNotEmpty() == true) {
                effectBgView?.showBgEffect(roomData?.peerEffectModel?.effectModel)
            } else {
                effectBgView?.hideBg()
            }
        }
        othersSingCardView?.bindData()
    }

    fun turnSingTurnChange() {
        processTurnChange()
    }

    fun turnNoSong() {
        dotView.visibility = View.GONE
        songNameTv.visibility = View.GONE
        songPlayProgressTv.visibility = View.GONE
        manyLyricsView.visibility = View.GONE
        voiceScaleView.visibility = View.GONE
        otherSingTipsTv.visibility = View.GONE
        noSongTipsTv.visibility = View.VISIBLE
        singBeginTipsTv1.visibility = View.GONE
        singBeginTipsTv2.visibility = View.GONE
        lyricAndAccMatchManager.stop()
        countDownJob?.cancel()
        effectBgView?.hideBg()
        othersSingCardView?.setVisibility(View.GONE)
    }

    fun turnSingPrepare() {
        dotView.visibility = View.GONE
        songNameTv.visibility = View.GONE
        songPlayProgressTv.visibility = View.GONE
        manyLyricsView.visibility = View.GONE
        voiceScaleView.visibility = View.GONE
        otherSingTipsTv.visibility = View.GONE
        noSongTipsTv.visibility = View.GONE
        singBeginTipsTv1.visibility = View.VISIBLE
        singBeginTipsTv2.visibility = View.VISIBLE
        singBeginTipsTv2.text = "《${roomData?.realRoundInfo?.music?.displaySongName}》"
        lyricAndAccMatchManager.stop()
        countDownJob?.cancel()
        effectBgView?.hideBg()
        othersSingCardView?.setVisibility(View.GONE)
    }

    fun turnMyChangePrepare() {
        otherSingTipsTv.visibility = View.GONE
        voiceScaleView.setStatus(2)
    }

    fun destroy() {
        lyricAndAccMatchManager.stop()
        countDownJob?.cancel()
        effectBgView?.hideBg()
        manyLyricsView?.release()
    }

}