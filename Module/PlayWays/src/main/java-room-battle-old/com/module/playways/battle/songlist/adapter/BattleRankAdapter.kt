package com.module.playways.battle.songlist.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import com.module.playways.R
import com.module.playways.battle.songlist.model.BattleRankInfoModel
import com.module.playways.battle.songlist.viewholer.BattleRankViewHolder

class BattleRankAdapter : RecyclerView.Adapter<BattleRankViewHolder>() {

    var mDataList = ArrayList<BattleRankInfoModel>()
    var onClickListener: ((model: BattleRankInfoModel?, position:Int)  -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BattleRankViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.battle_rank_item_layout, parent, false)
        return BattleRankViewHolder(view, onClickListener)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun onBindViewHolder(holder: BattleRankViewHolder, position: Int) {
        holder.bindData(mDataList[position], position)
    }
}