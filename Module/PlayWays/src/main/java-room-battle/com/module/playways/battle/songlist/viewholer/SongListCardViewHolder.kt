package com.module.playways.battle.songlist.viewholer

import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import com.module.playways.R
import com.module.playways.battle.songlist.model.BattleSongModel

class SongListCardViewHolder(item: View) : RecyclerView.ViewHolder(item) {

    private val songNameTv: TextView = item.findViewById(R.id.song_name_tv)

    fun bindData(model: BattleSongModel, pos: Int) {
        songNameTv.text = model.songName
        if (model.hasSing == true) {
            songNameTv.setTextColor(Color.parseColor("#4A90E2"))
        } else {
            songNameTv.setTextColor(Color.parseColor("#9B9B9B"))
        }
    }
}