package com.module.playways.party.room.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.party.room.model.PartyPlayerInfoModel

class PartyGameListRecyclerAdapter : RecyclerView.Adapter<PartyGameListRecyclerAdapter.PartyGameListHolder>() {
    val mRaceGamePlayInfoList = ArrayList<PartyPlayerInfoModel>()
    var mOpMethod: ((Int, PartyPlayerInfoModel) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartyGameListHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.party_game_list_item_layout, parent, false)
        return PartyGameListHolder(view)
    }

    override fun getItemCount(): Int {
        return mRaceGamePlayInfoList.size
    }

    override fun onBindViewHolder(holder: PartyGameListHolder, position: Int) {
        holder.bindData(position, mRaceGamePlayInfoList.get(position))
    }

    override fun onBindViewHolder(holder: PartyGameListHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            holder.bindData(position, mRaceGamePlayInfoList.get(position))
        } else {
            // 局部刷新
            holder.updateText(position, mRaceGamePlayInfoList.get(position))
        }
    }

    fun addData(list: List<PartyPlayerInfoModel>) {
        list?.let {
            if (it.size > 0) {
                val startNotifyIndex = if (mRaceGamePlayInfoList.size > 0) mRaceGamePlayInfoList.size - 1 else 0
                mRaceGamePlayInfoList.addAll(list)
                notifyItemRangeChanged(startNotifyIndex, mRaceGamePlayInfoList.size - startNotifyIndex)
            }
        }
    }

    inner class PartyGameListHolder : RecyclerView.ViewHolder {
        val TAG = "PartyGameListHolder"
        var gameNameTv: ExTextView
        var detailIv: ExImageView
        var addTv: ExTextView
        var moreTv: ExImageView

        constructor(itemView: View) : super(itemView) {
            gameNameTv = itemView.findViewById(R.id.game_name_tv)
            detailIv = itemView.findViewById(R.id.detail_iv)
            addTv = itemView.findViewById(R.id.add_tv)
            moreTv = itemView.findViewById(R.id.more_tv)


        }

        fun bindData(position: Int, model: PartyPlayerInfoModel) {

        }

        //会变化的内容
        fun updateText(position: Int, model: PartyPlayerInfoModel) {

        }
    }
}