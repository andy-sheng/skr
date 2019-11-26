package com.module.playways.relay.room.view

import android.view.View
import android.view.ViewStub
import android.widget.TextView
import com.common.log.DebugLogView
import com.common.log.MyLog
import com.common.view.ExViewStub
import com.common.view.ex.ExView
import com.component.lyrics.LyricsManager
import com.component.lyrics.LyricsReader
import com.component.lyrics.widget.AbstractLrcView
import com.component.lyrics.widget.ManyLyricsView
import com.component.lyrics.widget.VoiceScaleView
import com.module.playways.R
import com.module.playways.relay.room.RelayRoomData
import com.zq.mediaengine.kit.ZqEngineKit
import java.util.HashSet

class RelaySingCardView(viewStub: ViewStub) :ExViewStub(viewStub) {

    val TAG = "RelaySingCardView"

    lateinit var dotView: ExView
    lateinit var songNameTv: TextView
    lateinit var songPlayProgressTv:TextView
    lateinit var manyLyricsView: ManyLyricsView
    lateinit var voiceScaleView: VoiceScaleView
    lateinit var otherSingTipsTv:TextView
    lateinit var noSongTipsTv:TextView
    lateinit var singBeginTipsTv1:TextView
    lateinit var singBeginTipsTv2:TextView

    var roomData:RelayRoomData?=null

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
    }

    override fun layoutDesc(): Int {
        return R.layout.relay_sing_card_view_layout
    }

    fun turnSingBegin(){
        dotView.visibility = View.VISIBLE
        songNameTv.visibility = View.VISIBLE
        songPlayProgressTv.visibility = View.VISIBLE
        manyLyricsView.visibility = View.VISIBLE
        voiceScaleView.visibility = View.VISIBLE

        otherSingTipsTv.visibility = View.GONE
        noSongTipsTv.visibility = View.GONE
        singBeginTipsTv1.visibility = View.GONE
        singBeginTipsTv2.visibility = View.GONE

        LyricsManager
                .loadStandardLyric("http://song-static.inframe.mobi/lrc/4ee4ac0711c74d6f333fcac10c113239.zrce")
                .subscribe({ lyricsReader ->
                    manyLyricsView?.visibility = View.VISIBLE
                    manyLyricsView?.initLrcData()
                    manyLyricsView?.lyricsReader = lyricsReader
                    manyLyricsView?.setSplitChorusArray(intArrayOf(43*1000,65*1000,87*1000))
                    manyLyricsView?.setFirstSingByMe(false)
                    val set = HashSet<Int>()
                    set.add(lyricsReader.getLineInfoIdByStartTs(0))
                    set.add(19)
                    manyLyricsView?.needCountDownLine = set
                    manyLyricsView?.play(18*1000)
                }, { throwable ->
                    MyLog.e(TAG, throwable)
                    DebugLogView.println(TAG, "歌词下载失败，采用不滚动方式播放歌词")
                })
    }

    fun turnNoSong(){
        dotView.visibility = View.GONE
        songNameTv.visibility = View.GONE
        songPlayProgressTv.visibility = View.GONE
        manyLyricsView.visibility = View.GONE
        voiceScaleView.visibility = View.GONE
        otherSingTipsTv.visibility = View.GONE
        noSongTipsTv.visibility = View.VISIBLE
        singBeginTipsTv1.visibility = View.GONE
        singBeginTipsTv2.visibility = View.GONE
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
    }
}