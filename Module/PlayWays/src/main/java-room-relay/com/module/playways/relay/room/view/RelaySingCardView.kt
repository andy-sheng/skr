package com.module.playways.relay.room.view

import android.support.constraint.Group
import android.view.View
import android.view.ViewStub
import android.widget.TextView
import com.common.view.ExViewStub
import com.common.view.ex.ExView
import com.component.lyrics.widget.ManyLyricsView
import com.component.lyrics.widget.VoiceScaleView
import com.module.playways.R

class RelaySingCardView(viewStub: ViewStub) :ExViewStub(viewStub) {
    lateinit var dotView: ExView
    lateinit var songNameTv: TextView
    lateinit var songPlayProgressTv:TextView
    lateinit var manyLyricsView: ManyLyricsView
    lateinit var voiceScaleView: VoiceScaleView
    lateinit var otherSingTipsTv:TextView
    lateinit var noSongTipsTv:TextView
    lateinit var singBeginTipsTv1:TextView
    lateinit var singBeginTipsTv2:TextView
    lateinit var singBeginTipsTv: Group
    
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
        singBeginTipsTv = parentView.findViewById(R.id.sing_begin_tips_tv)
    }

    override fun layoutDesc(): Int {
        return R.layout.relay_sing_card_view_layout
    }

}