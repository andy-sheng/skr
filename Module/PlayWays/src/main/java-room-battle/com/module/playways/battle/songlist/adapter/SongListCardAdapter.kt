package com.module.playways.battle.songlist.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.module.playways.R
import com.module.playways.battle.songlist.model.BattleSongModel
import com.module.playways.battle.songlist.viewholer.SongListCardViewHolder

class SongListCardAdapter : RecyclerView.Adapter<SongListCardViewHolder>() {

    val mDataList = ArrayList<BattleSongModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongListCardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.battle_song_card_itme_layout, parent, false)
        return SongListCardViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: SongListCardViewHolder, position: Int) {
        holder.bindData(mDataList[position], position)
    }
}