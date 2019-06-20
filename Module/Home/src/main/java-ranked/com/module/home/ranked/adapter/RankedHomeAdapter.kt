package com.module.home.ranked.adapter

import android.view.LayoutInflater
import android.view.ViewGroup

import com.common.view.recyclerview.DiffAdapter
import com.common.view.recyclerview.RecyclerOnItemClickListener
import com.module.home.R
import com.module.home.ranked.model.RankHomeCardModel
import com.module.home.ranked.view.RankedCardViewHolder

class RankedHomeAdapter(internal var mListener: RecyclerOnItemClickListener<RankHomeCardModel>) : DiffAdapter<RankHomeCardModel, RankedCardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankedCardViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.ranked_home_card_item_layout, parent, false)
        return RankedCardViewHolder(view, mListener)
    }

    override fun onBindViewHolder(holder: RankedCardViewHolder, position: Int) {
        val rankHomeCardModel = mDataList[position]
        holder.bindData(rankHomeCardModel, position)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }
}
