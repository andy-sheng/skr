package com.module.playways.party.room.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.party.room.model.PartyPunishInfoModel

class PartyPunishItemAdapter : RecyclerView.Adapter<PartyPunishItemAdapter.PartyPunishViewHolder>() {

    var mDataList = ArrayList<PartyPunishInfoModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartyPunishViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.party_punish_item_layout, parent, false)
        return PartyPunishViewHolder(view)
    }

    override fun getItemCount(): Int {
        return Int.MAX_VALUE
    }

    override fun onBindViewHolder(holder: PartyPunishViewHolder, position: Int) {
        if (!mDataList.isNullOrEmpty()) {
            holder.bindData(mDataList[position % mDataList.size], position)
        }
    }


    inner class PartyPunishViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val titleTv: ExTextView = itemView.findViewById(R.id.title_tv)
        private val descTv: ExTextView = itemView.findViewById(R.id.desc_tv)

        fun bindData(model: PartyPunishInfoModel, pos: Int) {
            titleTv.text = if (model.punishType == 1) "【真心话】" else "【大冒险】"
            descTv.text = model.punishDesc
        }
    }
}
