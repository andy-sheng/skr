package com.module.playways.party.room.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.common.core.view.setDebounceViewClickListener
import com.common.view.ex.ExImageView
import com.common.view.ex.ExTextView
import com.module.playways.R
import com.module.playways.party.room.model.PartyPlayRule

class PartyGamePlayListRecyclerAdapter : RecyclerView.Adapter<PartyGamePlayListRecyclerAdapter.PartyGamePlayListHolder>() {
    val mPartyPlayRuleList = ArrayList<PartyPlayRule>()
    var mAddMethod: ((PartyPlayRule) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PartyGamePlayListHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.party_game_list_item_layout, parent, false)
        return PartyGamePlayListHolder(view)
    }

    override fun getItemCount(): Int {
        return mPartyPlayRuleList.size
    }

    override fun onBindViewHolder(holder: PartyGamePlayListHolder, position: Int) {
        holder.bindData(position, mPartyPlayRuleList.get(position))
    }

    fun addData(list: List<PartyPlayRule>) {
        list?.let {
            if (it.size > 0) {
                val startNotifyIndex = if (mPartyPlayRuleList.size > 0) mPartyPlayRuleList.size - 1 else 0
                mPartyPlayRuleList.addAll(list)
                notifyItemRangeChanged(startNotifyIndex, mPartyPlayRuleList.size - startNotifyIndex)
            }
        }
    }

    inner class PartyGamePlayListHolder : RecyclerView.ViewHolder {
        val TAG = "PartyGamePlayListHolder"
        var gameNameTv: ExTextView
        var detailIv: ExImageView
        var addTv: ExTextView
        var moreTv: ExImageView
        var pos = -1
        var model: PartyPlayRule? = null

        constructor(itemView: View) : super(itemView) {
            gameNameTv = itemView.findViewById(R.id.game_name_tv)
            detailIv = itemView.findViewById(R.id.detail_iv)
            addTv = itemView.findViewById(R.id.add_tv)
            moreTv = itemView.findViewById(R.id.more_tv)

            addTv.setDebounceViewClickListener {
                mAddMethod?.invoke(model!!)
            }

            detailIv.visibility = View.GONE
            addTv.visibility = View.VISIBLE
        }

        fun bindData(position: Int, model: PartyPlayRule) {
            pos = position
            this.model = model
            gameNameTv.text = model.playName
        }
    }
}