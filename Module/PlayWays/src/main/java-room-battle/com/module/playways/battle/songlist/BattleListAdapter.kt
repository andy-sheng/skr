package com.module.playways.battle.songlist

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.module.playways.R

class BattleListAdapter : RecyclerView.Adapter<BattleListViewHolder>() {

    var mDataList = ArrayList<BattleTagModel>()

    var onClickListener: ((model: BattleTagModel?, position:Int)  -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BattleListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.battle_song_item_layout, parent, false)
        return BattleListViewHolder(view, onClickListener)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: BattleListViewHolder, position: Int) {
        holder.bindData(mDataList[position], position)
    }
}